import json
from pathlib import Path
from .models import Chart, Event

def write_contracts(directory: Path):
    directory.mkdir(parents=True, exist_ok=True)
    for name, model in (("chart", Chart), ("event", Event)):
        schema = model.model_json_schema()
        schema["$schema"] = "https://json-schema.org/draft/2020-12/schema"
        (directory / (name + ".schema.json")).write_text(json.dumps(schema, indent=2))
    prediction = {"$schema": "https://json-schema.org/draft/2020-12/schema", "title": "Research-only prediction lockbox record", "type": "object", "additionalProperties": False,
                  "required": ["event_id", "model_id", "dataset_hash", "seed", "action_at_utc", "prediction", "outcome"],
                  "properties": {"event_id": {"type": "string", "format": "uuid"}, "model_id": {"type": "string"}, "dataset_hash": {"type": "string", "pattern": "^[0-9a-f]{64}$"}, "seed": {"type": "integer"}, "action_at_utc": {"type": "string", "format": "date-time"}, "prediction": {"type": "number", "minimum": 0, "maximum": 1}, "outcome": {"type": "null"}}}
    (directory / "prediction.schema.json").write_text(json.dumps(prediction, indent=2))

if __name__ == "__main__":
    write_contracts(Path(__file__).parents[2] / "contracts")
