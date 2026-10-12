from datetime import datetime, timedelta, timezone
from uuid import NAMESPACE_URL, uuid5
import numpy as np
from .models import Event

def synthetic_events(count=480, seed=42):
    rng = np.random.default_rng(seed)
    start = datetime(2024, 1, 1, tzinfo=timezone.utc)
    result = []
    for i in range(count):
        instant = start + timedelta(hours=4*i)
        effort = float(rng.uniform())
        chart = {"schema_version": 1, "chart_id": str(uuid5(NAMESPACE_URL, f"chart-{seed}-{i}")), "status": "SYNTHETIC",
                 "input": {"instant_utc": instant.isoformat(), "timezone": "Asia/Hong_Kong"},
                 "ruleset": {"id": "synthetic-demo", "version": "1", "source": "FICTIONAL demonstration symbols"},
                 "calendar": {}, "dun_type": None, "ju_number": None,
                 "palaces": [{"index": j, "direction": f"position-{j}", "door": f"fictional-{rng.integers(3)}", "star": None, "deity": None, "heaven_stem": None, "earth_stem": None} for j in range(1, 10)],
                 "derivation_steps": ["Random fictional tokens; not a Qimen chart."], "warnings": ["SYNTHETIC_NOT_EVIDENCE"], "input_hash": "0"*64}
        result.append(Event.model_validate({"event_id": str(uuid5(NAMESPACE_URL, f"event-{seed}-{i}")), "subject_pseudonym": f"synthetic-{i%24:02}",
                    "occurred_at_utc": instant, "timezone": "Asia/Hong_Kong", "context": {"effort": effort}, "action": {"kind": "SMALL_STEP", "cost": 0},
                    "outcome": int(rng.random() < 0.2 + 0.6*effort), "outcome_at_utc": instant + timedelta(hours=1), "source": "synthetic-demo",
                    "consent_scope": "synthetic-example", "chart_ref": chart, "dataset_version": "1"}))
    return result
