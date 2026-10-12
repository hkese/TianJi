import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path
from .data import canonical

def register(report, directory: Path):
    directory.mkdir(parents=True, exist_ok=True)
    metadata = {"id": report["run_id"], "dataset_hash": report["dataset_hash"], "status": "RESEARCH_ONLY", "deployable": False, "report": report}
    path = directory / (metadata["id"] + ".json")
    if not path.exists():
        with path.open("x") as stream:
            stream.write(canonical(metadata))
    return metadata

def approve(metadata, reviewer: str):
    report = metadata["report"]
    if not reviewer.strip() or report["synthetic"]:
        raise ValueError("Named human review and real consented data required")
    for protocol in report["protocols"].values():
        if protocol["test_count"] < 200 or protocol["test_subjects"] < 20 or protocol["b_minus_a_brier_ci95"][1] >= 0:
            raise ValueError("Exploratory engineering review gate not satisfied")
    return {**metadata, "status": "HUMAN_REVIEWED_METADATA_ONLY", "reviewer": reviewer,
            "reviewed_at": datetime.now(timezone.utc).isoformat(), "deployable": False,
            "limitation": "This gate is not scientific validation. Executable Navigator model exchange is disabled."}

def lock_prediction(directory: Path, payload: dict, now: datetime | None = None):
    now = now or datetime.now(timezone.utc)
    action_at = datetime.fromisoformat(payload["action_at_utc"].replace("Z", "+00:00"))
    if action_at.tzinfo is None or action_at <= now:
        raise ValueError("Lock predictions before the action")
    if payload.get("outcome") is not None:
        raise ValueError("Outcome must be unknown at lock time")
    for required in ("event_id", "model_id", "dataset_hash", "seed", "prediction"):
        if required not in payload:
            raise ValueError("Missing lockbox provenance")
    import math
    import re
    prediction = payload["prediction"]
    if isinstance(prediction, bool) or not isinstance(prediction, (int, float)) or not math.isfinite(prediction) or not 0 <= prediction <= 1:
        raise ValueError("Lockbox prediction must be a finite probability in [0, 1]")
    if not re.fullmatch("[0-9a-f]{64}", payload["dataset_hash"]):
        raise ValueError("Lockbox requires a SHA-256 dataset hash")
    if action_at.utcoffset().total_seconds() != 0:
        raise ValueError("Action timestamp must be UTC")
    from uuid import UUID
    name = str(UUID(payload["event_id"]))
    body = canonical({**payload, "locked_at_utc": now.isoformat()})
    record = {"payload": json.loads(body), "sha256": hashlib.sha256(body.encode()).hexdigest()}
    directory.mkdir(parents=True, exist_ok=True)
    with (directory / (name + ".json")).open("x") as stream:
        stream.write(canonical(record))
    return record
