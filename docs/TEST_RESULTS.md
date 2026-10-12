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
