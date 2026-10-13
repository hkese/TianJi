# Requirement traceability

Source paths are relative to this repository; Kotlin class files reside under each module’s `src/.../kotlin/com/tianji/` package. Current evidence is in TEST_RESULTS.md. Versions 0.2.1 and 0.2.2 pass Android 16 / API 36 device tests, including real keyboard/Save positioning and system Back; evidence and actual screenshots are in docs/evidence/android16/. Device and unit results are recorded separately; passing software checks do not validate traditional computation.

| ID | Priority | Code / evidence | Validation | Status and limits |
|---|---|---|---|---|
| NAV-001 | P0 | `android/app/MainActivity.kt` | JourneyTest: device pass | Implemented; 1 tap default start; no account/GPS |
| NAV-002 | P0 | `android/feature-home` | JourneyTest: device pass | Implemented; offline state and next action |
| NAV-003 | P0 | `android/feature-chart` | CoreTest | Basic calculated grid, palace details and traditional readings implemented; 0.3.0 Android 16 device flow passed |
| NAV-004 | P0 | `android/feature-goals; android/core-data` | RepositoryTest: device pass | Implemented; CRUD, deadlines, constraints, milestones, persistence |
| NAV-005 | P0 | `android/core-domain/Planner` | PlannerTest: 7 tests pass | 3 alternatives; enforced minutes/deadline/zero cost; text constraints need user review |
| NAV-006 | P0 | `android/feature-journal` | RepositoryTest: device pass | Implemented; linked action → plan + chart; unknown distinct |
| NAV-007 | P0 | `android/feature-planner` | RepositoryTest: device pass | Implemented immutable plan history and rationale |
| NAV-008 | P1 | `android/feature-chart` | Not applicable until verified Core | BLOCKED; no fabricated opportunity ranking |
| NAV-009 | P1 | `android/core-data/Ai; android/app` | AiTest 5 validation tests; ProviderTest 2 TLS mock tests pass | Implemented opt-in adapter; real configured provider unavailable |
| NAV-010 | P1 | `android/core-data; android/feature-settings` | quietHours unit test and Room/device tests pass | Implemented quiet reminder, atomic import/export, delete-all |
| NAV-011 | P2 | `android/app/AppModel; research/registry` | Lab consent and approval tests pass | Explicit pseudonym export; executable model import DEFERRED |
| NAV-012 | P0 | `android/core-ui/Evidence; chart and settings` | Source + unit checks; light/dark screenshots reviewed | Implemented labels/limitations; no VALIDATED prediction |
| CORE-001 | P0 | `android/core-qimen/ShiJiaChaiBu; docs/RULESET_REFERENCES.md` | TraditionalCoreTest: published chart, 351 cross-engine cases, 12 HKO terms, DST and 23:00 boundaries | Implemented fixed basic convention; limited independent coverage; TRADITIONAL status |
| CORE-002 | P0 | `android/core-qimen/ShiJiaChaiBu; docs/RULESET_REFERENCES.md` | TraditionalCoreTest: published chart, 351 cross-engine cases, 12 HKO terms, DST and 23:00 boundaries | Implemented fixed basic convention; limited independent coverage; TRADITIONAL status |
| CORE-004 | P0 | `android/core-qimen/ShiJiaChaiBu; docs/RULESET_REFERENCES.md` | TraditionalCoreTest: published chart, 351 cross-engine cases, 12 HKO terms, DST and 23:00 boundaries | Implemented fixed basic convention; limited independent coverage; TRADITIONAL status |
| CORE-005 | P0 | `android/core-qimen; contracts/fixtures` | 1 published worked example; 351 cross-engine regressions are not human goldens | INCOMPLETE: 100 independent human-golden target not met |
| CORE-003 | P0 | `android/core-qimen/QimenCore` | CoreTest: 8 tests pass | Canonical deterministic traditional chart plus legacy blocked snapshots; independent chart and cross-language tests |
| CORE-006 | P1 | `android/core-qimen/QimenCore` | CoreTest batch replay pass | Batch/cache/variant hooks implemented; new and legacy rulesets retained; saved historical replay added |
| LAB-001 | P0 | `research/tianji_research/data.py` | CSV, consent, immutable hash tests pass | Implemented explicit JSONL/CSV import |
| LAB-002 | P0 | `research/tianji_research/data.py` | position/version and leakage tests pass | Deterministic position-aware tokenizer; fictional tokens clearly marked |
| LAB-003 | P0 | `research/tianji_research/experiment.py` | baseline demo + identical protocol test pass | A ordinary time/context/action/history availability |
| LAB-004 | P0 | `research/tianji_research/experiment.py` | identical protocol test pass | B +tokens; C +prior observed history; no test outcomes in features |
| LAB-005 | P0 | `research/tianji_research/experiment.py` | time/group + reproducibility tests pass | Metrics and subject-cluster intervals; exploratory, no causal inference |
| LAB-006 | P0 | `research/tianji_research/experiment.py` | calendar/shuffle baseline tests pass | Separate-partition shuffles and calendar-only control |
| LAB-007 | P1 | `research/tianji_research/registry.py` | synthetic approval rejection passes | Metadata registry implemented; Transformer/world model DEFERRED |
| LAB-008 | P1 | `research/tianji_research/registry.py` | lockbox and human gate tests pass | Exclusive pre-action records and hashes; executable deployment disabled |
| LAB-009 | P2 | `KNOWN_LIMITATIONS.md` | No experiment run | DEFERRED; no quantum claim |

## 0.2.2 user feedback coverage

| Feedback | Implementation | Verification |
|---|---|---|
| Choose time zone without typing codes | `core-ui/TimeZonePicker.kt`, `core-domain/TimeZones.kt`, onboarding/settings | TimeZonesTest covers Chinese/English aliases, valid presets, Pacific DST boundary and Hong Kong offsets; expanded JourneyTest passes on Android 16 |
| Chinese instructions and status | `core-ui/GuideScreen.kt`, `Labels.kt`, `docs/USER_GUIDE_ZH_HK.md`, feature screens | Expanded JourneyTest covers guide navigation, Chinese date dialog, status labels and persistent language choice; passed on Android 16 |
| Blank chart looks broken | `feature-chart/ChartScreen.kt` | Clear unavailable state, practical planning CTA, optional fixed position legend and Chinese verification details; zero invented chart data |

Final 0.2.2 evidence: `docs/evidence/022/` and https://github.com/hkese/TianJi/actions/runs/38233591473 . 27 Kotlin tests, 20 Python tests, lint (0 errors/6 warnings), 3 Room tests and 1 full UI journey pass. The initial visual-review finding in the date picker was corrected and regression-tested before delivery.

## 0.3.0 basic chart delivery

`TraditionalCoreTest`, `ReadingTest`, `RepositoryTest` and the expanded `JourneyTest` cover the real offline core, sourced interpretation, center hosting, civil-time boundaries, Chinese date/time pickers, stored charts, legacy/current backup coexistence and tampered-symbol rejection. 37 Kotlin unit tests, 21 Python tests, lint and all 5 Android 16 instrumented tests pass. See `docs/evidence/030/` and CI run 38241131970. No scientific prediction or 100-human-golden completion claim.

## 0.4 navigation continuation (verified in CI 38322606240)

| Requirement | Implementation | Verification |
|---|---|---|
| NAV-002/005 | `Navigation.kt`, purpose picker, plan-frozen goal, selected first step on Home and Planner | `NavigationTest`, expanded `JourneyTest` |
| NAV-005/012 | Canonical chart-linked door/palace direction, displayed star/deity, heuristic/traditional separation, three concrete steps and completion evidence | Published-chart direction fixture, 96 purpose/time combinations |
| NAV-006/007 | Existing journal/replan links, immutable frozen goal/steps, expired-window refresh | Backup round trip, Room journey, UI journey |
| NAV-009 | `AnalysisPrompt.kt`, `AnalysisHandoff.kt`: review, copy, share chooser, explicit ChatGPT open | Exact chart/selected-option/privacy/delimiter unit tests, Android clipboard assertion |
| NAV-010 | New defaulted payload fields; former backups remain readable | Legacy JSON omission test and Room round trip |

## 0.5 sourced directional itinerary (CI 38338847939)

| Requirement / clarification | Implementation | Verification and limits |
|---|---|---|
| D0 / CORE-001/004 / NAV-012 | `core-qimen/DirectionRules.kt`, `docs/DIRECTION_RULES_1_0.md`: fixed R1/R7 conditions and conservative exclusions | `DirectionRulesTest`; R7 example geometry and actual matches checked. Basic traditional profile only; CORE-005 human-golden target still incomplete. |
| D1 / NAV-005 | `core-domain/DirectionalJourney.kt`: 24/72/168-hour search, separate question/candidate charts, chronological matches, explicit no-match result, deadline/budget limits | `DirectionJourneyTest`, DST/term rule tests and native UI; no invented filler candidate or success probability. |
| D2 | `feature-planner/DirectionDevices.kt`: optional foreground fixes, fixed origin, relative bearing/distance, magnetic/true-north distinction | Domain bearing/coordinate tests and compass display visual review. Physical GPS/permission dialogs/accuracy remain untested; no street/city routing. |
| D3 / NAV-002/006/007/010 | `DirectionPlannerScreen.kt`, repository stage persistence, Chinese guide, optional installed offline TTS | Stage/domain tests, Room restore and Android 16 selection/history/restart journey. Actual physical walking stages and Cantonese playback not exercised. Stay is user scheduling. |
| D4 / NAV-009/012 | Direction-specific manual AI prompt, canonical candidate charts/rules, private-coordinate exclusion, sensitive clipboard flag | Prompt/delimiter/privacy unit tests, native handoff/clipboard assertions. No automatic upload/response import or fabricated ritual instructions. |
| D5 | API 36 CI, signed 0.5 debug APK, same 0.4 identity | 60 Kotlin / 21 Python, 5 Room tests, 1 full UI journey, lint/build, v2/v3 and ZIP alignment pass. See TEST_RESULTS.md for exact source, artifact hashes, failed attempts and physical-device limits. |
