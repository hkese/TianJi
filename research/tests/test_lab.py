import csv
import json
from datetime import timedelta
from pathlib import Path
import pytest
from fastapi.testclient import TestClient
from tianji_research.data import dataset, import_file, tokenize
from tianji_research.experiment import evaluate, protocols, features, MODELS
from tianji_research.models import Event, Chart
from tianji_research.registry import approve, register, lock_prediction
from tianji_research.synthetic import synthetic_events
from tianji_research.api import create_app

@pytest.fixture(scope="module")
def events():
    return synthetic_events()

@pytest.fixture(scope="module")
def report(events):
    return evaluate(events)

def test_reproducible_dataset(events):
    assert dataset(events) == dataset(synthetic_events())
    assert dataset(events)[0] == dataset(list(reversed(events)))[0]

def test_duplicate_ids_rejected(events):
    with pytest.raises(ValueError):
        dataset(events + [events[0]])

def test_unknown_is_not_failure(events):
    e = events[0].model_copy(update={"outcome": None, "outcome_at_utc": None})
    assert e.outcome is None
    assert features(events[1], [e], "C")["history:available"] is False

def test_consent_required(events):
    record = events[0].model_dump(mode="json")
    record["source"] = "real"
    with pytest.raises(ValueError):
        Event.model_validate(record)

def test_future_chart_rejected(events):
    record = events[0].model_dump(mode="json")
    record["chart_ref"]["input"]["instant_utc"] = events[1].occurred_at_utc.isoformat()
    with pytest.raises(ValueError):
        Event.model_validate(record)

def test_position_and_version_preserved(events):
    tokens = tokenize(events[0].chart_ref)
    assert any(":palace:1:" in k for k in tokens)
    assert any(":palace:9:" in k for k in tokens)
    assert any("synthetic-demo@1" in k for k in tokens)

def test_wrong_palace_order_rejected(events):
    record = events[0].chart_ref.model_dump(mode="json")
    record["palaces"].reverse()
    with pytest.raises(ValueError):
        Chart.model_validate(record)

def test_context_allowlist_blocks_outcomes(events):
    malicious = events[0].model_copy(update={"context": {"outcome": 1, "future_label": 1, "effort": 0.2}})
    row = features(malicious, [], "A")
    assert "context:effort" in row
    assert not any("outcome" in k or "label" in k for k in row)

def test_time_and_group_holdouts(events):
    for name, (train, test) in protocols(events, 42).items():
        assert max(e.outcome_at_utc for e in train) < min(e.occurred_at_utc for e in test)
        assert {e.event_id for e in train}.isdisjoint(e.event_id for e in test)
        if name == "future_unseen_subjects":
            assert {e.subject_pseudonym for e in train}.isdisjoint(e.subject_pseudonym for e in test)

def test_future_outcomes_not_history(events):
    now = events[24]
    delayed = events[0].model_copy(update={"outcome_at_utc": now.occurred_at_utc + timedelta(days=1)})
    assert features(now, [delayed], "C")["history:count"] == 0

def test_all_models_identical_protocol(report):
    assert report["synthetic"] and not report["deployable"]
    for protocol in report["protocols"].values():
        assert set(protocol["models"]) == set(MODELS)
        for model in protocol["models"].values():
            assert 0 <= model["brier"] <= 1
            assert len(model["brier_ci95"]) == 2

def test_reproducible_experiment(events, report):
    assert evaluate(events) == report

def test_synthetic_never_approved(report, tmp_path):
    with pytest.raises(ValueError):
        approve(register(report, tmp_path), "reviewer")

def test_jsonl_hash_immutable(events, tmp_path):
    digest, body = dataset(events)
    source = tmp_path / "events.jsonl"
    source.write_text(body)
    actual, restored = import_file(source, tmp_path / "datasets")
    assert actual == digest and len(restored) == len(events)
    assert import_file(source, tmp_path / "datasets")[0] == digest

def test_csv_import(events, tmp_path):
    row = events[0].model_dump(mode="json")
    for field in ("action", "context", "chart_ref"):
        row[field] = json.dumps(row[field])
    path = tmp_path / "events.csv"
    with path.open("w", newline="") as stream:
        writer = csv.DictWriter(stream, fieldnames=row.keys())
        writer.writeheader()
        writer.writerow(row)
    _, restored = import_file(path, tmp_path / "datasets")
    assert restored == [events[0]]

def test_lockbox_before_action_only(events, tmp_path):
    now = events[0].occurred_at_utc
    payload = {"event_id": str(events[0].event_id), "action_at_utc": (now + timedelta(hours=1)).isoformat(), "model_id": "test", "dataset_hash": "a"*64, "seed": 42, "prediction": 0.5, "outcome": None}
    record = lock_prediction(tmp_path, payload, now)
    assert len(record["sha256"]) == 64
    with pytest.raises(FileExistsError):
        lock_prediction(tmp_path, payload, now)
    with pytest.raises(ValueError):
        lock_prediction(tmp_path, payload, now + timedelta(hours=2))

def test_authenticated_api(tmp_path, events):
    token = "test-only-local-auth-token-123"
    client = TestClient(create_app(tmp_path, token))
    assert client.get("/healthz").status_code == 200
    assert client.get("/v1/models").status_code == 401
    auth = {"Authorization": "Bearer " + token}
    assert client.get("/v1/models", headers=auth).status_code == 200
    response = client.post("/v1/datasets/import", headers=auth, json={"events": [e.model_dump(mode="json") for e in events[:2]]})
    assert response.status_code == 200

def test_kotlin_contract_fixture():
    path = Path(__file__).parents[2] / "contracts/fixtures/blocked-chart.json"
    chart = Chart.model_validate_json(path.read_text())
    assert chart.status == "BLOCKED" and chart.palaces == []
    assert chart.ju_number is None


def test_json_schema_interop(events):
    import jsonschema
    root = Path(__file__).parents[2] / "contracts"
    jsonschema.validate(json.loads((root / "fixtures/blocked-chart.json").read_text()), json.loads((root / "chart.schema.json").read_text()))
    jsonschema.validate(events[0].model_dump(mode="json"), json.loads((root / "event.schema.json").read_text()))


def test_lockbox_rejects_invalid_probability(events, tmp_path):
    now = events[0].occurred_at_utc
    payload = {"event_id": str(events[0].event_id), "action_at_utc": (now + timedelta(hours=1)).isoformat(), "model_id": "test", "dataset_hash": "a"*64, "seed": 42, "prediction": 1.2, "outcome": None}
    with pytest.raises(ValueError):
        lock_prediction(tmp_path, payload, now)
