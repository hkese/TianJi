import csv
import hashlib
import json
from pathlib import Path
from .models import Event

def canonical(value):
    return json.dumps(value, sort_keys=True, ensure_ascii=False, separators=(",", ":"), allow_nan=False)

def dataset(events: list[Event]):
    if not events:
        raise ValueError("Empty dataset")
    if len({e.event_id for e in events}) != len(events):
        raise ValueError("Duplicate event IDs")
    records = [e.model_dump(mode="json") for e in sorted(events, key=lambda e: str(e.event_id))]
    body = "\n".join(canonical(r) for r in records) + "\n"
    return hashlib.sha256(body.encode()).hexdigest(), body

def import_file(path: Path, output: Path):
    if path.stat().st_size > 50 * 1024 * 1024:
        raise ValueError("Input exceeds 50 MiB")
    if path.suffix.lower() == ".csv":
        with path.open(newline="", encoding="utf-8") as stream:
            records = list(csv.DictReader(stream))
        for r in records:
            for name in ("context", "action", "chart_ref"):
                r[name] = json.loads(r.get(name) or "null")
            r["outcome"] = int(r["outcome"]) if r.get("outcome") else None
            for name in ("outcome_at_utc", "location_coarse"):
                r[name] = r.get(name) or None
    else:
        records = [json.loads(line) for line in path.read_text().splitlines() if line.strip()]
    events = [Event.model_validate(r) for r in records]
    digest, body = dataset(events)
    output.mkdir(parents=True, exist_ok=True)
    destination = output / (digest + ".jsonl")
    if destination.exists():
        if destination.read_text() != body:
            raise ValueError("Immutable dataset hash collision")
    else:
        with destination.open("x") as stream:
            stream.write(body)
    return digest, events

def tokenize(chart):
    if chart is None or chart.status == "BLOCKED":
        return {"qimen:availability": "blocked-or-missing"}
    prefix = f"qimen:{chart.ruleset.id}@{chart.ruleset.version}"
    tokens = {"qimen:availability": chart.status}
    for p in chart.palaces:
        for field in ("door", "star", "deity", "heaven_stem", "earth_stem"):
            tokens[f"{prefix}:palace:{p.index}:{field}"] = getattr(p, field) or "missing"
    return tokens
