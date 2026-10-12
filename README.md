# TianJi Navigator
Native, offline-first Android planning and reflection, plus a separate optional Python Research Lab. Traditional Chinese (Hong Kong), with an English switch and dark mode.

**Qimen calculation is BLOCKED, not simulated.** No authoritative complete ruleset or independent fixtures were supplied. The app uses useful practical heuristics without invented chart symbols, success percentages or claims of scientific proof. See `KNOWN_LIMITATIONS.md` and `docs/RULESET_REFERENCES.md`.

## Android build
- JDK 17, Android SDK platform 36, build-tools 35.0.0, minimum phone Android 9 (API 28).
- Gradle wrapper 8.13 with SHA-256 pin; Android Gradle Plugin 8.11.1; Kotlin 2.1.20; Compose BOM 2025.04.01; Room 2.7.1.
- Set `ANDROID_HOME` or add `sdk.dir=/absolute/path/to/android-sdk` to untracked `local.properties`.
- From repository root: `./gradlew :android:app:assembleDebug` (or `./gradlew assembleDebug`).
- Unit tests: `./gradlew :android:core-domain:test :android:core-qimen:test :android:core-data:testDebugUnitTest`.
- Lint: `./gradlew :android:app:lintDebug`.
- Connected tests: `./gradlew :android:core-data:connectedDebugAndroidTest :android:app:connectedDebugAndroidTest`. The journey test requires a fresh app profile (test device only).
- APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
- Install: `adb install -r android/app/build/outputs/apk/debug/app-debug.apk`.
- Debug builds use the local Android debug signing key. For a release, create your own signing configuration through Android Studio; do not commit a keystore or credentials.

## Offline daily flow
1. Start the app; review privacy/evidence explanation. Default HK timezone can be changed; no account or GPS is needed.
2. Create a goal with measurable success criteria, deadline, priority, available minutes and spending limit; add milestones.
3. Open “Plan next step” and generate three alternatives. They share a bounded time window and cost zero; choose one.
4. Record the actual outcome. Unknown is kept separate from failure.
5. Add an observation and replan; prior versions remain inspectable.
6. Settings offers JSON backup/restore, opt-in reminders (quiet 22:00–08:00), language, theme and delete-all.

## Optional AI
Configure an HTTPS OpenAI-compatible base URL (usually ending `/v1`), model and API key. Credentials use Android Keystore AES-GCM. “Test connection” contacts `/models` only when tapped. Each comparison shows destination and the exact goal/options payload and requires consent before `/chat/completions`. Journal notes and credentials are excluded from the context. Responses may only reorder existing actions and select a comparison emphasis; chart claims, extra fields and fabricated probabilities are rejected. The offline plan remains available after any failure. A real configured provider was not supplied, so validation uses a local TLS mock.

## Research Lab
Python 3.12 required. It is independent of the Android application.
```
cd research
python -m venv .venv
. .venv/bin/activate
pip install -e '.[test]'
python -m pytest -q
ruff check tianji_research tests
python -m tianji_research.cli demo --output output/demo
python -m tianji_research.cli import /path/to/consented-events.jsonl --output output
python -m tianji_research.cli evaluate /path/to/consented-events.jsonl --output output
```
Run Kotlin tests first to produce/verify the cross-language blocked-chart fixture, which is also committed.

The demonstration generates 480 **fictional** events across 24 synthetic subjects. It evaluates A (ordinary features), B (+position/version Qimen tokens), C (+available richer history), calendar-only and shuffled-token controls with the same forward-time and future-unseen-subject partitions. Brier, log loss, calibration, binary probability MAE, sample counts and subject-cluster bootstrap intervals are reported. Missing outcomes are excluded from labels, not recoded as failures. Feature construction sees only training history observed before the current action. Synthetic results cannot approve a model. Observational performance cannot establish causation.

Phone research export is explicit, pseudonymized and local. It excludes goal text, journal notes and secrets, but remains potentially identifiable. Current phone exports are retrospective records, not blind forecasts. Registry outputs are metadata only; executable model deployment, Transformer/world models and quantum research are deferred.

API: copy `research/.env.example` to `.env`, set a random token, then run `uvicorn tianji_research.api:create_app --factory --host 127.0.0.1 --port 8000`. All `/v1` routes require `Authorization: Bearer ...`; `/healthz` is public. Docker Compose binds only `127.0.0.1:8000`. Docker deployment has not been executed here.

## Contracts, privacy and recovery
Current Kotlin/Python chart and event contracts use integer schema version 1. Palace order, when present, is 1..9. Current blocked engine is `shijia-zhuanpan-chaibu@0.0.1-blocked`; legacy format 1.0 snapshots from the recovered initial APK remain preserved by the database migration. The version change records serialization changes, not newly verified calculation.

No analytics, automatic uploads, location permission, cloud backup or embedded provider credentials. Backups exclude provider credentials and consent records. Restore validates IDs, links, times and blocked chart hashes before a transactional replacement. Delete-all clears records, credentials and reminders; external backups remain under your control.

The original working directory was pruned during a stalled GitHub login. The first installed APK was recovered; this source is a separately rebuilt revision. `docs/TEST_RESULTS.md` records current evidence, not results borrowed from the lost revision. `docs/TRACEABILITY.md` maps each specification requirement. `PROGRESS.md` tracks remaining gates.

## GitHub
Repository: https://github.com/hkese/TianJi (owner: **hkese**). The repository includes Android/Python CI and the observed local test evidence. The delivered APK targets Android 16 / API 36, with minimum Android 9 / API 28. Android 16 runtime verification is in progress; target SDK alone is not a runtime compatibility test.

## License
License choice pending owner decision. No open-source license grant is implied. See `LICENSE-PENDING.md`.
