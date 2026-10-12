from __future__ import annotations
import hashlib
import math
import numpy as np
from sklearn.feature_extraction import DictVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import brier_score_loss, log_loss, mean_absolute_error
from sklearn.pipeline import make_pipeline
from .data import canonical, dataset, tokenize
from .models import Event

MODELS = ("A", "B", "C", "CALENDAR", "SHUFFLED_QIMEN")
CONTEXT = {"effort", "sleep_hours", "task_difficulty"}

def features(event: Event, history: list[Event], model: str, tokens=None):
    local = event.occurred_at_utc.astimezone(__import__("zoneinfo").ZoneInfo(event.timezone))
    row = {"hour_sin": math.sin(2 * math.pi * local.hour / 24),
           "hour_cos": math.cos(2 * math.pi * local.hour / 24),
           "weekday": str(local.weekday()), "month": str(local.month)}
    if model == "CALENDAR":
        return row
    for name in CONTEXT:
        value = event.context.get(name)
        if isinstance(value, (int, float)) and math.isfinite(value):
            row["context:" + name] = float(value)
    row.update({"action:kind": event.action.kind, "action:cost": event.action.cost})
    prior = [h for h in history if h.subject_pseudonym == event.subject_pseudonym
             and h.outcome is not None and h.outcome_at_utc < event.occurred_at_utc
             and h.occurred_at_utc < event.occurred_at_utc]
    row["history:count"] = len(prior)
    if model in ("B", "C", "SHUFFLED_QIMEN"):
        row.update(tokens if tokens is not None else tokenize(event.chart_ref))
    if model == "C":
        row["history:available"] = bool(prior)
        row["history:rate"] = np.mean([h.outcome for h in prior]) if prior else 0.5
    return row

def protocols(events: list[Event], seed: int):
    known = sorted([e for e in events if e.outcome is not None], key=lambda e: e.occurred_at_utc)
    if len(known) < 40:
        raise ValueError("At least 40 labeled events required for a demonstration")
    cutoff = known[int(len(known) * 0.70)].occurred_at_utc
    train = [e for e in known if e.occurred_at_utc < cutoff and e.outcome_at_utc < cutoff]
    test = [e for e in known if e.occurred_at_utc >= cutoff]
    subjects = sorted({e.subject_pseudonym for e in known})
    if len(subjects) < 4:
        raise ValueError("At least four subjects required for grouped holdout")
    rng = np.random.default_rng(seed)
    holdout = set(rng.permutation(subjects)[:max(1, len(subjects) // 4)])
    grouped_train = [e for e in train if e.subject_pseudonym not in holdout]
    grouped_test = [e for e in test if e.subject_pseudonym in holdout]
    result = {"forward_time": (train, test), "future_unseen_subjects": (grouped_train, grouped_test)}
    for name, (tr, te) in result.items():
        if len(tr) < 12 or len(te) < 4 or len({e.outcome for e in tr}) < 2:
            raise ValueError(f"Insufficient samples/classes for {name}")
    return result

def bootstrap(loss, groups, seed):
    rng = np.random.default_rng(seed)
    names = sorted(set(groups))
    values = []
    for _ in range(200):
        selected = rng.choice(names, len(names), replace=True)
        values.append(float(np.mean(np.concatenate([loss[groups == g] for g in selected]))))
    return [float(v) for v in np.quantile(values, [0.025, 0.975])]

def evaluate(events: list[Event], seed: int = 42):
    digest, _ = dataset(events)
    report = {"schema_version": 1, "dataset_hash": digest, "seed": seed,
              "synthetic": any(e.source == "synthetic-demo" for e in events),
              "known_outcomes": sum(e.outcome is not None for e in events),
              "unknown_outcomes": sum(e.outcome is None for e in events),
              "evidence_label": "RESEARCH_ONLY", "causal_claim": False,
              "deployable": False, "bootstrap": "200 subject-cluster resamples; exploratory intervals",
              "protocols": {}}
    for protocol, (train, test) in protocols(events, seed).items():
        y_train = [e.outcome for e in train]
        y = np.array([e.outcome for e in test])
        groups = np.array([e.subject_pseudonym for e in test])
        losses = {}
        result = {"train_count": len(train), "test_count": len(test), "test_subjects": len(set(groups)),
                  "train_ids": [str(e.event_id) for e in train], "test_ids": [str(e.event_id) for e in test], "models": {}}
        for model in MODELS:
            rng = np.random.default_rng(seed)
            train_tokens = [tokenize(e.chart_ref) for e in train]
            test_tokens = [tokenize(e.chart_ref) for e in test]
            if model == "SHUFFLED_QIMEN":
                # Permute within each partition; never copy future test information into training.
                train_tokens = [train_tokens[i] for i in rng.permutation(len(train_tokens))]
                test_tokens = [test_tokens[i] for i in rng.permutation(len(test_tokens))]
            x_train = [features(e, train, model, tok) for e, tok in zip(train, train_tokens)]
            x_test = [features(e, train, model, tok) for e, tok in zip(test, test_tokens)]
            estimator = make_pipeline(DictVectorizer(), LogisticRegression(C=1.0, max_iter=500, random_state=seed))
            estimator.fit(x_train, y_train)
            p = estimator.predict_proba(x_test)[:, 1]
            losses[model] = (p - y) ** 2
            calibration = []
            for low in np.arange(0, 1, 0.2):
                mask = (p >= low) & (p < low + 0.2 + (1e-10 if low > 0.79 else 0))
                if mask.any():
                    calibration.append({"count": int(mask.sum()), "predicted": float(p[mask].mean()), "observed": float(y[mask].mean())})
            result["models"][model] = {"brier": float(brier_score_loss(y, p)), "log_loss": float(log_loss(y, p, labels=[0, 1])),
                                       "binary_probability_mae": float(mean_absolute_error(y, p)), "calibration": calibration,
                                       "brier_ci95": bootstrap(losses[model], groups, seed)}
        result["b_minus_a_brier_ci95"] = bootstrap(losses["B"] - losses["A"], groups, seed)
        report["protocols"][protocol] = result
    report["run_id"] = hashlib.sha256(canonical(report).encode()).hexdigest()
    return report
