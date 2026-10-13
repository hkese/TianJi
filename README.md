# TianJi 0.5 — 傳統擇時・擇方行程

0.5 改為按心願搜尋傳統時段及方位，並用中文逐步記錄出發、到達、停留、返回。規則與限制見 [DIRECTION_RULES_1_0.md](docs/DIRECTION_RULES_1_0.md)，使用說明見 [USER_GUIDE_ZH.md](docs/USER_GUIDE_ZH.md)。此版本正在驗證，實際結果以 PROGRESS.md 與 docs/TEST_RESULTS.md 為準。

套件仍為 `com.tianji.navigator.daily`；versionCode 7 / 0.5.0。更新 APK 必須使用原 0.4 私有保留的開發簽署金鑰，CI 臨時簽署 APK 不可冒充同簽署更新。

---

> 0.4 行動導航更新：由目的產生三個有步驟的方案，連同實際盤面方位，並可複製完整提示詞交畀 ChatGPT／Codex。新版與舊版並排安裝，先保留舊 App。詳見 [繁體中文使用教學](docs/USER_GUIDE_ZH.md)。目前驗證狀態見 [PROGRESS.md](PROGRESS.md)。

# TianJi Navigator
Native, offline-first Android planning and reflection, plus a separate optional Python Research Lab. Traditional Chinese (Hong Kong), with an English switch and dark mode.

**0.3.0 adds real offline basic Qimen calculation and Chinese traditional readings.** One explicit Shi Jia rotating Chai Bu convention calculates the nine-palace symbols from the selected civil time and zone. Status is `TRADITIONAL`, not a scientifically validated forecast. Verification includes one published worked chart, 351 cross-implementation cases and 12 HKO major solar terms; the specification's 100 independently adjudicated human-golden target remains incomplete. See `KNOWN_LIMITATIONS.md` and `docs/RULESET_REFERENCES.md`.

## 中文使用說明

請看 [繁體中文操作指南](docs/USER_GUIDE_ZH_HK.md)，或直接在 App 的首頁／設定開啟「使用教學」。0.3.0 可直接「用現在時間起盤」，看中文簡讀、點九宮詳情，並儲存命盤重看；日期、時間、時區全部用選單選擇。

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
1. Start the app; review privacy/evidence explanation. Device timezone is the default on new installs; existing choices survive upgrades and a searchable city picker changes it; no account or GPS is needed.
2. Tap Qimen → Calculate for now. Read the traditional interpretation and tap palaces for details. Change date/time/zone with pickers; save and reopen charts offline.
3. Create a goal with measurable success criteria, deadline, priority, available minutes and spending limit; add milestones.
4. Open “Plan next step” and generate three alternatives. They share a bounded time window and cost zero; choose one.
5. Record the actual outcome. Unknown is kept separate from failure.
6. Add an observation and replan; prior versions remain inspectable.
7. Settings offers JSON backup/restore, opt-in reminders (quiet 22:00–08:00), language, theme and delete-all.

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
Run Kotlin tests first to produce/verify the legacy blocked and real traditional cross-language fixtures, which are also committed. `TRADITIONAL` tokens remain explicitly separate from `VERIFIED` and `SYNTHETIC`; algorithmic consistency is not evidence of predictive performance.

The demonstration generates 480 **fictional** events across 24 synthetic subjects. It evaluates A (ordinary features), B (+position/version Qimen tokens), C (+available richer history), calendar-only and shuffled-token controls with the same forward-time and future-unseen-subject partitions. Brier, log loss, calibration, binary probability MAE, sample counts and subject-cluster bootstrap intervals are reported. Missing outcomes are excluded from labels, not recoded as failures. Feature construction sees only training history observed before the current action. Synthetic results cannot approve a model. Observational performance cannot establish causation.

Phone research export is explicit, pseudonymized and local. It excludes goal text, journal notes and secrets, but remains potentially identifiable. Current phone exports are retrospective records, not blind forecasts. Registry outputs are metadata only; executable model deployment, Transformer/world models and quantum research are deferred.

API: copy `research/.env.example` to `.env`, set a random token, then run `uvicorn tianji_research.api:create_app --factory --host 127.0.0.1 --port 8000`. All `/v1` routes require `Authorization: Bearer ...`; `/healthz` is public. Docker Compose binds only `127.0.0.1:8000`. Docker deployment has not been executed here.

## Contracts, privacy and recovery
Current Kotlin/Python chart and event contracts use integer schema version 1. Palace order, when present, is 1..9. The default ruleset is `shijia-zhuanpan-chaibu-civil-kun2@1.0.0`; legacy `shijia-zhuanpan-chaibu@0.0.1-blocked` and recovered format 1.0 snapshots remain supported for backup restore. Center hosting is explicit: two stems separated by `、` mean main stem followed by the center stem. New charts use the additional `TRADITIONAL` status in schema version 1; consumers built against the old status enum must update.

No analytics, automatic uploads, location permission, cloud backup or embedded provider credentials. Backups exclude provider credentials and consent records. Restore validates IDs, links, times and legacy and traditional chart contents/hashes before a transactional replacement. Delete-all clears records, credentials and reminders; external backups remain under your control.

The original working directory was pruned during a stalled GitHub login. The first installed APK was recovered; this source is a separately rebuilt revision. `docs/TEST_RESULTS.md` records current evidence, not results borrowed from the lost revision. `docs/TRACEABILITY.md` maps each specification requirement. `PROGRESS.md` tracks remaining gates.

## GitHub
Repository: https://github.com/hkese/TianJi (owner: **hkese**). The repository includes Android/Python CI and the observed local test evidence. The delivered APK targets Android 16 / API 36, with minimum Android 9 / API 28. Earlier version **0.2.1** passed the complete offline UI journey and 3 Room/migration/Keystore device tests on an Android 16 / API 36 emulator. The journey checks a real open keyboard, Save visibility, system Back, persistence, English and dark mode. See `docs/TEST_RESULTS.md` for current-version evidence. Physical Samsung/One UI testing is still pending.

## License
License choice pending owner decision. No open-source license grant is implied. See `LICENSE-PENDING.md`.
