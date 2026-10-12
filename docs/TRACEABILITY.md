# Requirement traceability

Source paths are relative to this repository; Kotlin class files reside under each module’s `src/.../kotlin/com/tianji/` package. Current evidence is in TEST_RESULTS.md. Version 0.2.1 passes Android 16 / API 36 device tests, including real keyboard/Save positioning and system Back; evidence and actual screenshots are in docs/evidence/android16/. Device and unit results are recorded separately; passing software checks do not validate traditional computation.

| ID | Priority | Code / evidence | Validation | Status and limits |
|---|---|---|---|---|
| NAV-001 | P0 | `android/app/MainActivity.kt` | JourneyTest: device pass | Implemented; 1 tap default start; no account/GPS |
| NAV-002 | P0 | `android/feature-home` | JourneyTest: device pass | Implemented; offline state and next action |
| NAV-003 | P0 | `android/feature-chart` | CoreTest | BLOCKED symbols; accessible fixed position legend only |
| NAV-004 | P0 | `android/feature-goals; android/core-data` | RepositoryTest: device pass | Implemented; CRUD, deadlines, constraints, milestones, persistence |
| NAV-005 | P0 | `android/core-domain/Planner` | PlannerTest: 7 tests pass | 3 alternatives; enforced minutes/deadline/zero cost; text constraints need user review |
| NAV-006 | P0 | `android/feature-journal` | RepositoryTest: device pass | Implemented; linked action → plan + chart; unknown distinct |
| NAV-007 | P0 | `android/feature-planner` | RepositoryTest: device pass | Implemented immutable plan history and rationale |
| NAV-008 | P1 | `android/feature-chart` | Not applicable until verified Core | BLOCKED; no fabricated opportunity ranking |
| NAV-009 | P1 | `android/core-data/Ai; android/app` | AiTest 5 validation tests; ProviderTest 2 TLS mock tests pass | Implemented opt-in adapter; real configured provider unavailable |
| NAV-010 | P1 | `android/core-data; android/feature-settings` | quietHours unit test and Room/device tests pass | Implemented quiet reminder, atomic import/export, delete-all |
| NAV-011 | P2 | `android/app/AppModel; research/registry` | Lab consent and approval tests pass | Explicit pseudonym export; executable model import DEFERRED |
| NAV-012 | P0 | `android/core-ui/Evidence; chart and settings` | Source + unit checks; light/dark screenshots reviewed | Implemented labels/limitations; no VALIDATED prediction |
| CORE-001 | P0 | `android/core-qimen; docs/RULESET_REFERENCES.md` | 0/100 independent reference charts | BLOCKED pending authoritative conventions and independent fixtures |
| CORE-002 | P0 | `android/core-qimen; docs/RULESET_REFERENCES.md` | 0/100 independent reference charts | BLOCKED pending authoritative conventions and independent fixtures |
| CORE-004 | P0 | `android/core-qimen; docs/RULESET_REFERENCES.md` | 0/100 independent reference charts | BLOCKED pending authoritative conventions and independent fixtures |
| CORE-005 | P0 | `android/core-qimen; docs/RULESET_REFERENCES.md` | 0/100 independent reference charts | BLOCKED pending authoritative conventions and independent fixtures |
| CORE-003 | P0 | `android/core-qimen/QimenCore` | CoreTest: 8 tests pass | Canonical deterministic blocked snapshot, hash and version; not a real chart |
| CORE-006 | P1 | `android/core-qimen/QimenCore` | CoreTest batch replay pass | Batch/cache/variant hooks implemented; actual ruleset comparisons await rulesets |
| LAB-001 | P0 | `research/tianji_research/data.py` | CSV, consent, immutable hash tests pass | Implemented explicit JSONL/CSV import |
| LAB-002 | P0 | `research/tianji_research/data.py` | position/version and leakage tests pass | Deterministic position-aware tokenizer; fictional tokens clearly marked |
| LAB-003 | P0 | `research/tianji_research/experiment.py` | baseline demo + identical protocol test pass | A ordinary time/context/action/history availability |
| LAB-004 | P0 | `research/tianji_research/experiment.py` | identical protocol test pass | B +tokens; C +prior observed history; no test outcomes in features |
| LAB-005 | P0 | `research/tianji_research/experiment.py` | time/group + reproducibility tests pass | Metrics and subject-cluster intervals; exploratory, no causal inference |
| LAB-006 | P0 | `research/tianji_research/experiment.py` | calendar/shuffle baseline tests pass | Separate-partition shuffles and calendar-only control |
| LAB-007 | P1 | `research/tianji_research/registry.py` | synthetic approval rejection passes | Metadata registry implemented; Transformer/world model DEFERRED |
| LAB-008 | P1 | `research/tianji_research/registry.py` | lockbox and human gate tests pass | Exclusive pre-action records and hashes; executable deployment disabled |
| LAB-009 | P2 | `KNOWN_LIMITATIONS.md` | No experiment run | DEFERRED; no quantum claim |
