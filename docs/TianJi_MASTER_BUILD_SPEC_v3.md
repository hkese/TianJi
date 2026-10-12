# TianJi AI — ONE-FILE MASTER BUILD SPEC v3.0

**Hand this entire file to Codex.** 2026-10-11. Engineering specification in English; Android user-facing language Traditional Chinese (Hong Kong).

## CODEX: START HERE

You are the implementation agent. Build the entire TianJi project described here in one monorepo, not merely a prototype or UI mockup. First inspect your environment and create docs/IMPLEMENTATION_PLAN.md and docs/TRACEABILITY.md. Then implement in milestone order, run tests, fix errors, and produce an installable Android debug APK if Android SDK is available. If compilation is impossible, report the precise blocker and never claim an APK exists. Continue through all feasible milestones. Keep PROGRESS.md and KNOWN_LIMITATIONS.md updated. Do not invent Qimen calculations, scientific evidence, or success probabilities. Default to the technology choices below without asking routine questions. Ask only when a missing authoritative ruleset reference prevents correct computation. No credentials in code.

## VISION AND SCIENTIFIC STATUS

User hypothesis: a higher-dimensional civilization can see many possible timelines, and Qimen Dunjia is a compact navigation encoding of a much larger spacetime structure. It is a GPS-like state estimator, not a full map. Current chart plus personal context and chosen action may inform the next state. An AI may tokenize charts and learn next-state distributions. This is a speculative premise, not an established scientific claim. Double-slit and uncertainty principles do not prove prediction of individual quantum impacts, parallel-universe navigation, or guaranteed manifestation. The app must distinguish TRADITIONAL interpretations, HEURISTIC advice, RESEARCH_ONLY predictions, and VALIDATED empirical estimates. It must be useful even if the hypothesis is false.

## DELIVERABLES AND REPO STRUCTURE

ONE consumer Android app named TianJi Navigator, containing TianJi Core as an embedded offline Kotlin module; ONE optional PC/server Research Lab in the same repo. Android must work without account, cloud, PC, or paid AI. Repository: android/app, android/core-qimen, android/core-domain, android/core-data, android/feature-home, android/feature-chart, android/feature-goals, android/feature-planner, android/feature-journal, android/feature-settings; research/tianji_research, research/tests, research/examples/synthetic; contracts/chart.schema.json, contracts/event.schema.json, contracts/prediction.schema.json; docs/; .github/workflows; README.md, PROGRESS.md, KNOWN_LIMITATIONS.md. Output a working source tree, tests, setup guide, and android/app/build/outputs/apk/debug/app-debug.apk when build succeeds.

## ANDROID TECH STACK

Kotlin, Jetpack Compose, Material 3, MVVM with unidirectional data flow, Kotlin coroutines/Flow, Room, DataStore, WorkManager, Navigation Compose, Hilt or Koin, Gradle Kotlin DSL. Minimum Android SDK 28. Use stable installed target SDK and record versions. Traditional Chinese (Hong Kong) UI with English language switch. Support dark mode, dynamic text scaling, accessibility semantics, 360dp phone width, responsive larger screens, proper loading/empty/error states. Offline-first; store UTC instant and IANA timezone. GPS optional, permission only when needed; no continuous location tracking. Keystore-protected API credentials, opt-in remote AI, JSON export/delete, no research uploads by default.

## ANDROID PRODUCT REQUIREMENTS

NAV-001 P0: First-run privacy/science explanation, language and timezone selection; no account, GPS or AI required. AC: reach dashboard offline in <=5 taps.
NAV-002 P0: Home displays time, current chart summary, ruleset/version, active goal, next action and navigation tabs. AC: renders without Internet.
NAV-003 P0: 3x3 nine-palace chart with accessible palace details, doors/stars/deities/stems and derivation trace. AC: no fabricated chart values.
NAV-004 P0: Goal CRUD with title, deadline, success criteria, milestones, constraints, priority. AC: persists offline across restarts.
NAV-005 P0: Candidate planner proposes >=3 feasible actions with time window, optional direction/location, cost, risk, rationale and evidence label. AC: no invented probability percentages.
NAV-006 P0: Journal stores executed action, timestamp, outcome, optional score and notes. AC: links chart snapshot and recommendation version.
NAV-007 P0: Replan after new observations; preserve prior plan versions and rationale. AC: inspect historical recommendations.
NAV-008 P1: 7/30-day qualitative opportunity calendar using actual Core charts. AC: timezone/ruleset respected.
NAV-009 P1: Optional AI assistant using user-configured cloud or self-hosted OpenAI-compatible provider; explicit consent before sharing context. AC: fallback to offline heuristics.
NAV-010 P1: Opt-in WorkManager reminders, quiet hours, JSON import/export, delete-all. AC: export round trip and no unsolicited notifications.
NAV-011 P2: Explicit pseudonymized Research Lab export and approved model import with compatibility checks. AC: research off by default.
NAV-012 P0: All interpretations/recommendations display evidence source and limitations.

## QIMEN CORE REQUIREMENTS

Core must be a pure Kotlin deterministic library independent of Android UI and Internet. Choose ONE explicitly documented Shi Jia Qimen (時家奇門) ruleset for MVP, with variant plugin architecture. Schools differ; never silently mix flying/rotating or other conventions. Before calculation, document authoritative reference material and independently checked chart fixtures in docs/RULESET_REFERENCES.md. If references unavailable, build interface/tests but mark calculation BLOCKED rather than fabricating values.
CORE-001 P0: Solar terms, calendrical boundaries, sexagenary stems/branches, Yin/Yang Dun, Ju number, daylight saving, leap years and midnight transitions.
CORE-002 P0: Nine-palace mapping, eight doors, nine stars, eight deities, stems, duty symbols and explicit center handling.
CORE-003 P0: calculateChart(instant, timezone, rulesetId, optionalLocation) returns canonical JSON, deterministic hash and ruleset version.
CORE-004 P0: Explain each derivation step with rule provenance.
CORE-005 P0: >=100 independently checked golden fixtures including boundary cases; never claim this threshold before fixtures exist.
CORE-006 P1: Historical replay, batch charts, rule comparisons, cache.
Location may be ignored with explicit warning if chosen ruleset does not use location correction.

## DATA MODEL AND CONTRACTS

Room entities: Goal(id,title,description,createdAt,deadline,successCriteria,status,priority,constraintsJson); Milestone(id,goalId,title,dueAt,status); ChartSnapshot(id,instantUtc,timezone,rulesetId,rulesetVersion,chartJson,inputHash); PlanVersion(id,goalId,createdAt,parentVersionId,reason); ActionCandidate(id,planVersionId,chartId,title,plannedStart,plannedEnd,locationText,cost,risk,evidenceLabel,rationale); JournalEntry(id,actionId,occurredAt,outcome,score,notes); Consent(id,scope,grantedAt,revokedAt); AiProviderConfig(id,type,baseUrl,modelName,credentialAlias). Use UUID IDs, foreign keys, migrations, ISO8601 UTC timestamps, explicit nullable fields.
Chart JSON schema: {schema_version, chart_id, input:{instant_utc,timezone,latitude?,longitude?}, ruleset:{id,version,source}, calendar, dun_type, ju_number, palaces:[{index,direction,door,star,deity,heaven_stem,earth_stem}], derivation_steps, warnings}. Define fixed palace order and validate across Kotlin/Python.
Recommendation JSON: {goal_id,chart_id,plan_version,actions:[{title,planned_window,rationale,constraints,risks,evidence_label,provenance}],limitations}. Validate AI output and reject hallucinated chart facts.

## RESEARCH LAB REQUIREMENTS

Python 3.12, Pydantic v2, pandas or Polars, scikit-learn, PyTorch optional, FastAPI optional, pytest, Ruff; CPU baseline must work. Synthetic examples explicitly labeled. Never upload personal phone data automatically.
LAB-001 P0: Import consented event CSV/JSONL with provenance, validation and immutable dataset hash.
LAB-002 P0: Deterministic Qimen tokenizer preserving 3x3 palace position and ruleset version; keep future outcomes out of features.
LAB-003 P0: Baseline A: ordinary time/calendar/context/action/history features without Qimen.
LAB-004 P0: Model B = A plus Qimen tokens; Model C = B plus available richer history; identical evaluation protocol.
LAB-005 P0: Forward-in-time and subject-group holdouts, Brier score, log loss, calibration, MAE when appropriate, sample counts, confidence intervals.
LAB-006 P0: Calendar-only and shuffled-Qimen ablations; leakage/seasonality controls.
LAB-007 P1: Action-conditioned Transformer / latent world model, uncertainty estimates, model registry.
LAB-008 P1: Blind prediction lockbox, versioned seeds/config/data hashes, human approval gate before Navigator deployment.
LAB-009 P2: Optional separately preregistered quantum-data experiment; never imply quantum results proven.
Event schema: {event_id,subject_pseudonym,occurred_at_utc,timezone,location_coarse?,context,action,outcome,outcome_at_utc,source,consent_scope,chart_ref?,dataset_version}. Missing outcomes are unknown, not failures. No causal claims from observational correlations.

## OPTIONAL RESEARCH API

POST /v1/datasets/import; POST /v1/experiments; GET /v1/experiments/{id}; GET /v1/experiments/{id}/metrics; GET /v1/models; POST /v1/models/{id}/approve; GET /healthz. Bind localhost by default; authenticate any remote deployment. Import only files the user deliberately exports. Research Lab is not required for Android core features.

## PLANNING, SAFETY AND PRIVACY

Offline heuristic planner must function without any LLM. Optional AI can phrase and compare actions, but only the Core calculates charts. Never invent chart symbols, scientific validation, exact future outcomes or numeric success rates. Show provenance on every suggestion. For health, financial, legal and other consequential choices, provide general planning support rather than deterministic directives. Treat journal text and imported files as untrusted, defend against prompt injection, do not transmit secrets. User controls opt-in, export and delete. Android notification permission is optional. Remote AI settings require connection test and clear consent.

## UX SCREENS AND DESIGN SYSTEM

Bottom navigation: Home, Chart, Goals, Journal, Settings. Home: compact chart snapshot, current time, goal card and next-action CTA. Chart: 3x3 interactive grid, symbols, legend, derivation drawer, ruleset badge. Goals: goal list, editor, milestones, plan versions. Planner: side-by-side action cards, practical constraints, evidence labels, select/complete/replan. Journal: chronological timeline, outcome entry and export. Settings: language, timezone, location consent, AI provider, reminders, data management, ruleset, scientific caveat. Use polished Material 3 spacing, hierarchy, microinteractions, accessible touch targets >=48dp, light/dark themes. No outdated WebView UI.

## TEST PLAN AND QUALITY GATES

Unit tests: calendar boundaries, deterministic Core, serialization, schema validation, planner constraints, journal/goal CRUD, migrations. Integration: Android Room, UI flows, chart reference consistency, import/export. UI tests: onboarding offline, create goal, see three actions, journal outcome, replan, restart and verify persistence, switch language/dark mode. Research tests: synthetic dataset pipeline, leakage guard, reproducibility, baseline comparison, shuffled-token control, approval gate. Security: secrets not logged, export/delete, no network without consent. CI: Gradle tests/lint, Python pytest/ruff, schema validation. No success claim unless commands and results are recorded.

## MILESTONES AND EXIT CRITERIA

M0 Foundation: monorepo, CI, Gradle app boots, Room, theme, navigation, contracts, architecture docs. Exit: runnable blank app with tests.
M1 Offline Navigator: onboarding, goals, candidate heuristic planner, journal, replan, export. Exit: complete offline user journey with persisted data.
M2 Verified Qimen Core: documented ruleset, fixtures, derivation trace, chart UI and calendar. Exit: independent golden-chart checks; otherwise mark blocked, never fake.
M3 AI Options: provider adapter, explicit context consent, structured output validation, graceful offline fallback. Exit: mock provider and configured provider tests.
M4 Research Lab: import, tokenizer, baseline, ablations, metrics, registry. Exit: end-to-end synthetic demonstration clearly marked synthetic.
M5 Polish/Release: accessibility, localization, security, UI tests, README, signed/debug build instructions and APK. Exit: compiled APK where SDK available and all test results documented.
M6 Advanced optional: Transformer world model and approved artifact exchange, only after enough consented real data and valid benchmarks.

## DEFINITION OF DONE AND HANDOFF

Every requirement has ID, priority, code path, test ID, pass/fail and evidence in docs/TRACEABILITY.md. README includes Android Studio/SDK prerequisites, ./gradlew assembleDebug, adb install command, offline demo flow, Python lab commands, schema versions and limitations. Provide .env.example with no secrets, deterministic demo fixtures clearly labeled, Dockerfile/compose for Research Lab, migration scripts, license placeholders. Final Codex response must state: files created, features working, tests executed with results, exact APK path if compiled, unverified ruleset aspects, blocked items and recommended next tasks. Never report scientific hypothesis as verified by code alone.
