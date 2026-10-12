# 0.4.0 — goal-to-action navigation

Source: `75eca621ee280abc11b83484b2d7743d8c8c7f40`.
CI: https://github.com/hkese/TianJi/actions/runs/38322606240 — both jobs successful.

| Gate | Observed result |
|---|---|
| Kotlin JVM/unit | 46 tests, 0 failures/errors; XML in `evidence/040` |
| Navigation coverage | 9 new tests: 96 purpose/time combinations, published-chart direction, deadlines, frozen history, old payload defaults, full-chart prompt/selection/privacy, delimiter escape and invalid linkage/blocked-chart rejection |
| Research Lab | 21 passed; Ruff passed; existing Starlette deprecation warning retained in CI |
| Android lint + APK build | CI Gradle successful; lint gate passed (warning count not separately collected) |
| API 36 data | 4 tests passed in 2.807 s |
| API 36 UI | 1 complete offline journey passed in 30.99 s at 720×1440 / 320dpi; Wi-Fi/data disabled |
| UI scope | Chinese city picker, goal purpose, three actions/directions, full prompt review and clipboard contents, selected first step, journal, restart, replan/IME/system Back, English/dark mode, Chinese chart/date/time details and saved chart replay |
| Visual QA | Native navigation, selected-step and AI-handoff views reviewed locally. Long text wraps; selected first step and outcome CTA are readable. Temporary Android clipboard overlay is visible on two captures. Public evidence here is text-only; automatic approval review rejected publishing a screenshot because it might contain private goal data. |
| APK signature / packaging | v2 and v3 verification passed (`apksigner verify --min-sdk-version 24` exercises both schemes; the app manifest still requires API 28); zipalign `-c -P 16 4` passed |

The first CI run `38321883691` passed build/unit/data checks but failed the UI test waiting for Back after replan. Logs identified an IME race; the app now clears focus/hides the keyboard on replan, and the test waits for hidden IME and drains the shell Back command. Final run passes. This failure was not reported as a successful journey.

## Delivered package and reproduction

- Filename: `TianJi-Navigator-0.4.0-debug.apk`; build-output copy: `android/app/build/outputs/apk/debug/app-debug.apk`.
- Application: `com.tianji.navigator.daily`, label `天機・行動導航`, versionCode 6, minSdk 28, target/compileSdk 36.
- Size: 33,603,063 bytes.
- SHA-256: `52086ecdf68c5e179d8c0996c25970140cab0eddedb2f4693e6476c36f235e7e`.
- Certificate SHA-256: `e262754c09f6cef572c56aaa785debffa3e62e09ff87f93f87f0394e2e9de240`.
- CI artifact ID 11778909340, original ZIP SHA-256 `9f70ee85ba38f7cd00c93e6f87046bbe448f3bd05d6dbca1d20ed737035641bb`. Download checksum verified; re-signed locally using the retained daily-app debug key. All non-signature ZIP entry digests match the original CI APK.
- Local build recovery required JDK 17 (the preinstalled runtime lacked javac) and correcting a nested API 36 SDK directory. 46 local unit tests passed; redundant local packaging/lint work was stopped after the exact source passed CI and its compiled artifact was obtained. Do not describe that interrupted command as a successful local full build.
- New package installs alongside former `com.tianji.navigator`; former signer private key was lost in workspace cleanup. No same-signature update claim. Keep the old app, export its JSON, then import into the new app before adding new records. Signing recovery material is stored privately, never committed to GitHub.
- No physical Samsung/One UI, 16 KB runtime, screen-reader or large-font device test claim. ZIP alignment is not a 16 KB runtime test. External ChatGPT/Codex response quality and real configured provider remain unverified.
- Core ruleset unchanged from 0.3: simplified traditional interpretation, no full-pattern synthesis and no predictive-validation claim. The 100 human golden-chart target remains incomplete.

---

# Version 0.3.0 — basic offline Qimen

Date: 2026-10-12. App source commit: `88365df07dbccc17aba2492779f003112600fd12`.

- Local clean build, 37 Kotlin unit tests, both instrumentation APK compilations and lint: **BUILD SUCCESSFUL** (3m48s). Lint: 0 errors, 7 warnings (preferences style, dependency-version notices and Kotlin extension suggestions).
- Python: **21 tests passed**, Ruff passed. The real `TRADITIONAL` chart validates against Pydantic / JSON Schema and retains its status and hosted stems in position-aware research tokens. Legacy blocked fixtures remain supported.
- Independent worked reference: the published 2027-02-06 10:00 Shanghai chart matches day/hour/year/month, Dun/Ju/Yuan/duty symbols and all eight outer palaces' main stems/doors/stars/deities. Hosted center fields are separately asserted from the documented convention.
- Cross-implementation regression: **351 charts** match an independently implemented MIT reference engine; **9** predeclared whole-day solar-term convention differences are listed in the fixture. Both use the 6tail calendar family, so these are not independent ephemeris checks or human goldens.
- Calendar: **12** published 2026 HKO major solar terms checked within 90 seconds; tests cover the exact computed boundary, DST gaps/overlaps, civil-zone independence, leap day and 23:00 rollover. Full 2026 structure checks cover **4,380** two-hour timestamps including all center cases. Reading checks cover 720 times and all eight doors.
- APK: package `com.tianji.navigator`, versionCode **5**, versionName **0.3.0**, minSdk 28 / targetSdk 36. v2 signature and 16KB ZIP alignment pass. Certificate matches the delivered 0.2.2 APK.
- APK SHA-256: `d5a4af67915f119e6546920a9d58fdff1d783b9f8b589701759a7d63dd487eb5`.
- GitHub verify job: **PASS**, https://github.com/hkese/TianJi/actions/runs/38241131970 . Android 16 device job: **PASS**, 4 Room/migration/backup/Keystore tests and 1 expanded offline journey. The journey exercises Chinese chart date/time pickers, real grid details, saving/recreation, and the existing goal/journal/replan/keyboard/language/theme flow. Five chart screenshots were visually reviewed at 360dp width.
- Evidence: `docs/evidence/030/`. No physical Samsung or scientific-prediction validation is claimed.

The basic mode is explicitly `TRADITIONAL`. The 100 independently adjudicated human-golden goal remains incomplete; 351 software comparisons cannot be relabeled as 351 expert-verified charts. Advanced opportunity calendar / comprehensive pattern judgments are not implemented.

Build recovery: a Compose dialog delimiter error was corrected, then a stale incremental class-bundle conflict was resolved by cleaning only generated app/chart build outputs. The final full command above passes; no failed build is presented as successful.

---

# Version 0.2.2 — Chinese usability update

Date: 2026-10-12. Source commit: `32456b58c5f31b9c37799a859ad87737cec900dc`.

Local validation:
- `:android:core-domain:test :android:core-qimen:test :android:core-data:testDebugUnitTest :android:app:lintDebug :android:app:assembleDebug :android:app:assembleDebugAndroidTest`: **BUILD SUCCESSFUL**.
- 27 Kotlin unit tests pass (4 timezone tests, 7 planner, 8 Core contract, 6 AI validation, 2 TLS provider).
- Lint: 0 errors, 6 warnings.
- Final test-selector compilation: `:android:app:assembleDebugAndroidTest` passes.
- APK v2 signature verifies; certificate matches delivered 0.2.1. Package `com.tianji.navigator`, versionCode 4, versionName 0.2.2, minSdk 28, targetSdk 36.
- SHA-256: `608e0d3d14164fcd21f2362b4ed1ef23382fe5324a84b031bea59c5e52dd371d`.
- Evidence: `docs/evidence/022/`.

Android 16 device run: **PASS**, https://github.com/hkese/TianJi/actions/runs/38233591473 . All 3 Room/Keystore tests pass (2.068s suite), and the expanded offline UI journey passes (22.467s suite). GitHub verify also passes, including 20 Python tests and Ruff. No 0.2.2 physical-phone claim. The owner's screenshot confirms the previous APK was installed; the chart's missing calculations were an existing product limitation, not a loading state.

Changes: city timezone picker with DST-aware offsets, Chinese aliases and device shortcut; Chinese guide in Home/Settings; translated theme, outcome and goal status labels; localized date picker; explicit unavailable-chart page instead of a prominent empty grid. UTC event storage and existing preferences/records are preserved. Qimen remains 0/100 independently checked fixtures; no predictions were added.

Visual QA caught a remaining English calendar month/headline after the first successful device run. The dialog now reapplies the app locale inside its Android composition root, with a regression assertion that the month contains Chinese text and the English headline is absent. The final local rebuild/lint/instrumentation compilation and Android 16 regression test pass. The final date-picker screenshot shows Chinese headline, month and weekdays on the English-system emulator. Other reviewed screenshots cover guide, chart status, settings, planner and Save above the real keyboard.

---

# Android 16 update — 0.2.1

- GitHub source: https://github.com/hkese/TianJi
- Validated source commit: `b8b83c07816a28623bc5c14432f358908e21fd65`.
- Kotlin/Python/lint/build job passed on GitHub (23 Kotlin tests, 20 Python tests).
- Android 16 / API 36 emulator: 3 Room/migration/Keystore tests passed; 1 complete UI journey passed, including real keyboard visibility, Save position, three options, journal, persistence, replan, system Back, English and dark mode.
- Final GitHub workflow, including screenshot collection, passed: https://github.com/hkese/TianJi/actions/runs/38227645836
- Local 0.2.1 build/lint/signature passed. The certificate matches the delivered 0.2.0 APK. 64-bit native ELF and ZIP 16KB alignment checks pass; no 16KB-page runtime or physical-phone test is claimed.
- APK: `android/app/build/outputs/apk/debug/app-debug.apk`, versionCode 3 / versionName 0.2.1, SHA-256 `56d521cafbb24eb56a9b163240f91e63ec710518a14ebec54a8a056ec1568723`.
- Current evidence: `evidence/android16/`; details: `ANDROID16.md`.

The following section preserves the earlier 0.2.0 validation record.

# Verification — reconstructed revision 0.2.0
Commands run in the provided environment; actual output files are preserved in `docs/evidence/` at handoff.

| Check | Observed result |
|---|---|
| Android app assembleDebug | Successful debug APK produced (API 28 minimum / target 36, Java 17) |
| Kotlin PlannerTest | 7 passed |
| Kotlin CoreTest | 8 passed; deterministic BLOCKED behavior, not verified Qimen calculations |
| Kotlin AiTest | 6 passed including quiet hours |
| Kotlin ProviderTest | 2 passed, local HTTPS mock provider |
| Android Room/Keystore instrumentation | **3 tests passed** on Android 9 software emulator: persistent CRUD/plan/journal/replan/backup/delete; original-v1 migration; encrypted credential round trip |
| Research pytest | **20 passed**, one dependency deprecation warning in Starlette/AnyIO |
| Ruff | All checks passed |
| Cross-language contract | Kotlin-generated blocked chart validated by Pydantic and JSON Schema |
| Synthetic CPU demonstration | Completed; 480 artificial records / 24 artificial subjects; run afe3a603b68ad3d710bc8f1ace708ca0e95a48bab909aed7fe8575bfc93f9635 |
| Android lint | 0 errors, 8 warnings on the final build (style/dependency updates) |
| Full UI journey | **1 complete journey passed (88.34 s)**: offline onboarding, goal, three alternatives, select, journal, activity recreation/persistence, tab navigation, replan, English, dark mode persistence |
| Real AI provider | Not configured; not tested |
| Docker | Files supplied, not executed |
| GitHub CI | Workflow supplied; not run remotely or pushed yet |
| Qimen golden fixtures | **0 / 100**; calculation BLOCKED |

The earlier recovered APK has SHA-256 cce7d837531fa7184b84eeb11d048a56e442f19b9ce40cfdae6041b5de7cd545. Its previous results are distinct from this revision. The complete UI journey passes on a software Android 9 emulator (720×1440 px at 320dpi = 360dp width), with Wi-Fi and mobile data disabled. No AndroidRuntime fatal error appears in the collected run log. Screenshots are in `evidence/screenshots/`. This is emulator validation, not a physical-phone or accessibility audit.

## Reproducible commands and evidence

- `./gradlew :android:app:assembleDebug :android:app:assembleDebugAndroidTest :android:app:lintDebug` — final successful build, `evidence/android-final-build.log`.
- `./gradlew :android:core-domain:test :android:core-qimen:test :android:core-data:testDebugUnitTest` — 23 tests; individual JUnit XML files in `evidence/`.
- `adb shell am instrument -w com.tianji.coredata.test/androidx.test.runner.AndroidJUnitRunner` after installing the core-data instrumentation APK — `evidence/room-device.log`.
- `adb shell am instrument -w com.tianji.navigator.test/androidx.test.runner.AndroidJUnitRunner` after installing the app and app instrumentation APK on a fresh test profile with Wi-Fi/data off — `evidence/ui-complete-device.log`.
- `python -m pytest research/tests -q` — `evidence/pytest-final.log`.
- `ruff check research/tianji_research research/tests` — `evidence/ruff-final.log`.
- `python -m tianji_research.cli demo --output output/demo` from `research/` — `evidence/demo.log` and `evidence/synthetic-demo-metrics.json`.
- `apksigner verify --verbose android/app/build/outputs/apk/debug/app-debug.apk` — v2 signature passes; `evidence/apk-signature.log`.

Final APK SHA-256: `02dd739224894f0383443236ac8301c7ee28500e8363e22dbe726418180d33c7`.
Package: `com.tianji.navigator`, versionCode 2, versionName `0.2.0-recovery`, minSDK 28, targetSDK 36. The artifact is a development-signed debug APK, not a Play Store release.

The last change after the passing complete journey only adjusts dark theme colors. It was rebuilt with lint, installed over the tested version, and visually checked with the persisted goal, English and dark mode. See `evidence/android-palette-build.log`; the whole journey was not rerun for this color-only change.
