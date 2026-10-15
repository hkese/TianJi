# 0.6.1 — device authorization recovery

Date: 2026-10-15. Executable source: `fc77d7b0e2113e765e130d385ed40fd33825a93e`.
CI: https://github.com/hkese/TianJi/actions/runs/38779648069 . Both build/verification and Android 16 gates passed. Later documentation-only publication does not alter the tested executable source.

| Gate | Observed result |
|---|---|
| Kotlin | 88 passed: domain 37, Core 23, data/network 28; zero failures/errors in CI XML. |
| New regression coverage | 3 TLS tests: actual dropped polling socket, server failure with identical device-code reuse, cached authorization-code exchange recovery. 5 recovery tests: retries, expiry/slowdown bounds, terminal denial/cancellation, reconstruction, stale-write/cancel guards. |
| Research Lab | 21 passed; Ruff passed. One existing dependency deprecation warning. |
| Build/lint | Gradle BUILD SUCCESSFUL in 2m41s; lint 0 errors / 27 warnings (preference/KTX style and dependency-version suggestions). |
| API 36 data | 7 Room/Keystore/backup tests passed (suite 4.726 s), including encrypted pending-stage restoration and backup exclusion. |
| API 36 app | 3 tests passed (suite 46.891 s): complete offline journey 41.073 s; recreated AppModel resumes exchange, retains login through catalog failure and retries catalog 1.679 s; temporary polling failure completes the same login 3.349 s. |
| Device environment | Google APIs x86_64 / API 36, 720×1440 at 320dpi, Wi-Fi/data disabled. Auth tests use local synthetic HTTPS only. Current account-page screenshot reviewed locally; no real account or phone screenshots published. |
| APK | Package `com.tianji.navigator.daily`, versionCode 9 / 0.6.1, minSdk 28 / target 36. 34,110,967 bytes. SHA-256 `f08f82d86d17897bb1556b8af49d23a8b3e8bf9d0e7e00996358faceb612e700`. |
| Signing/content | v2/v3 verified, retained certificate `e262754c09f6cef572c56aaa785debffa3e62e09ff87f93f87f0394e2e9de240`; 464 non-signature entries identical to CI. 264 uncompressed entries remain aligned: native .so at 16KB, others 4 bytes. CI zipalign passed. |

APK artifact 11995596276: ZIP SHA-256 `8c278bc73fcbe934b48c3188bf81748c575315f9fed0cd1d5649f26c48dea6a0`. Verification artifact 11995915239: ZIP SHA-256 `dbea3ae21b77097596dc9b1613a1d82f2d5197ee8f15355a77ac812f68872d36`. Archive checksums verified before extraction. Device artifact 11996445306: ZIP SHA-256 `cf76e31ccc89b226a602b447a2fa38b3f99df773bd9f1875d3f0d61c984da2b6`. All archive hashes verified. Device XML copies use LF line endings; test content is unchanged. Signing key remains private. Final signed APK saved for delivery; same signer/package permits identity-compatible 0.4–0.6 updates, but physical installer upgrade has not been exercised here.

The owner's screenshot establishes browser-side authorization success and an app-side failure, but not its exact exception/HTTP stage. Confirmed abort-on-IOException and missing pending recovery defects were fixed. The new tests use synthetic local HTTPS and actual Android AppModel/Keystore where stated; they do not establish a live successful authorization or prediction. See LOGIN_RECOVERY_0_6_1.md. If an authorization code was consumed by the server before its response was lost, a fresh login may still be necessary; credentials cannot be recreated by the client. Physical Samsung installation, service entitlement and live generated interpretations remain phone acceptance.

Initial CI 38778439413 passed build/unit/Lab gates, but the new native reconstruction regression exposed `NetworkOnMainThreadException` from TLS close-notify during `ChatGptClient.close`. Cleanup now runs on a dedicated background executor, with immediate logical cancellation. The same regression remains in the device gate; no StrictMode relaxation or assertion removal. See `evidence/061/initial-android16-failure.txt`.

---

# 0.6.0 — one intention and direct ChatGPT account AI

Date: 2026-10-14 UTC (2026-10-13 Pacific). Verified executable source: `f3624faf8c91def9e280981cae2dee2560341a3e`.
CI: https://github.com/hkese/TianJi/actions/runs/38489999031 . Both build/verification and Android 16 jobs passed. Subsequent documentation-only publication does not alter the tested executable source.

| Gate | Observed result |
|---|---|
| Kotlin unit suites | 80 passed: domain 37, Core 23, data/network 20. Exact CI XML in `evidence/060`. |
| New intention coverage | 8 tests: editable automatic routing, full-text preservation, local tonight/tomorrow/DST, deadline-constrained matches and no-match result, private-coordinate exclusion, unknown/expired/cross-plan action rejection, strict JSON and old payload defaults. |
| New account coverage | 10 tests: fixed-origin device start/pending/exchange, slowdown/denial/expiry/cancel, redirects and safe errors, one 401 refresh with rotation, second-401/429 no-loop, logout invalidation, completed-only and sparse-final SSE, failure/refusal rejection, actual self-identification and store=false. |
| API-key interpretation | 2 additional TLS mock tests verify the configured endpoint/auth/context and reject truncated completion. Existing provider/validation tests retained. |
| Research Lab | 21 passed; Ruff passed. Existing dependency deprecation warning remains. |
| Build/lint | Gradle BUILD SUCCESSFUL, 1m53s. Lint: 0 errors / 24 warnings (preference style, dependency updates, Kotlin convenience suggestions). |
| API 36 data | 6 Room/backup/Keystore tests passed (suite 4.703 s), including quick intention and AI annotation restoration without credentials. |
| API 36 UI | Full native offline journey passed (test 45.657 s; suite 47.27 s), Google APIs x86_64, 720×1440 at 320dpi, Wi-Fi and data disabled. |
| UI coverage | One-field submit with the real keyboard, account entry/return, immediate displayed direction without manual scrolling, persisted result after activity recreation, selected itinerary, and retained chart/time-zone/history/journal routes. Home, account, keyboard and guidance screenshots inspected locally. |
| Delivered APK checks | v2/v3 signatures pass; retained 0.4/0.5 certificate matches. CI zipalign `-c -P 16 4` passed; after signing all 264 uncompressed entries remain aligned (16KB for .so, 4 bytes otherwise). All 464 non-signature ZIP entries have identical digests to the verified CI APK. |

Two later device runs (38487647585 and diagnostic 38488969674) exposed a Home refresh failure after navigation and activity recreation: model state already contained the saved question/plan and success message, but Home rendered none of them. State is now collected at the activity and passed into the screen. The result card is brought into view; the test additionally requires it to be displayed without a manual scroll. See `evidence/060/ui-first-failure.txt`. Screenshots wait for a compositor frame after Compose becomes idle.

Initial CI 38487301448 failed Kotlin compilation at two `ensureActive()` calls inside a suspend helper without a receiver. They now use `currentCoroutineContext().ensureActive()`. The initial run is not a successful APK/device validation. No local full Gradle build is claimed in this pruned workspace; compilation and executable test evidence come from CI.

Package `com.tianji.navigator.daily`, launcher `天機・行動導航`, versionCode 8 / 0.6.0, minSdk 28 / target and compileSdk 36. Signed APK: 34,078,199 bytes; SHA-256 `f24a1b66cce4b919446ed78f07c8c0df03b17f3bbbaca2c8552947c8e743faf0`. Certificate SHA-256 `e262754c09f6cef572c56aaa785debffa3e62e09ff87f93f87f0394e2e9de240`.

CI APK artifact 11861745898: ZIP SHA-256 `3dac73ed0b7ca8bbcc28e747a037f5b6e03eb4378d7730225fc5144e8b759ad0`. Verification artifact 11860944757: ZIP SHA-256 `b9358b31d53e3ca5242a57452047646d4fe0addd5a0f2d553fb86e0fd385b9d5`. Download checksums verified before extraction/signing. Device XML copies use LF line endings; test content is unchanged. Device artifact 11862035007: ZIP SHA-256 `ad1959b59e152b073a9c69118d197272f95f76797185a01e36bfda2a98c8a418`. Signing material is private and absent from source/evidence/artifacts. The final signed APK was saved for delivery.

Real TianJi ChatGPT authorization, token refresh against a live account, model eligibility, live generated interpretation and physical Samsung installation/voice/GPS remain unverified. The signed APK is an identity-compatible 0.4/0.5 update; physical installer upgrade has not been exercised here. Native login uses the owner's keyboard's device-code compatibility protocol, independently implemented, with service-controlled eligibility. It is not the newer dynamic-agent SIWC contract. No keyboard credentials are imported.

AI interpretation text is not verified divination. It cannot supply the executable time/direction fields, and candidate selections are checked against current stored actions. Automatic intention/date routing is limited product logic, not full natural-language or specialized Qimen inference. Core's 100 independent expert golden-chart target remains incomplete. Software checks establish no predictive or causal efficacy.

---

# 0.5.0 — sourced time/direction itineraries

Date: 2026-10-13. Verified executable source: `5455294381c1238128d695069a1d085783e9629a`.
CI: https://github.com/hkese/TianJi/actions/runs/38338847939 — verify and Android 16 jobs successful. Subsequent documentation/evidence publication does not alter the tested executable source.

| Gate | Observed result |
|---|---|
| Kotlin JVM/unit | 60 passed: domain 29, Qimen 23, data 8. Local suite XML retained in `evidence/050`; final CI also passes these suites. |
| Direction rules | 5 new tests cover the R7 worked palace example, actual match conditions/exclusions, blocked input, unknown purpose and stable windows across DST/solar-term boundaries. The geometry example is not an additional independently adjudicated full-chart golden. |
| Journey domain | 9 new tests cover actual candidate charts, empty harmony results, the published geometry case, windows/deadlines/budgets, forged inputs, stage gating/persistence, geographic bearings, and prompt chart/privacy/delimiter handling. |
| Research Lab | 21 tests passed; Ruff passed. Existing synthetic research remains separate from predictive validation. |
| Android build/lint | Final CI Gradle build and lint gates passed; warning count not separately collected. |
| API 36 data | 5 Room/backup/Keystore tests passed in 1.713 s, including direction-plan snapshots, selection and backup/restore. |
| API 36 UI | 1 complete native journey passed in 22.115 s; Google APIs x86_64, 720×1440 / 320dpi (360dp width), Wi-Fi/data disabled. |
| UI scope | Chinese onboarding/city picker, keyboard/goal, seven-day general-outing search, candidate charts, AI preview and clipboard, selected itinerary/compass display, unknown observation, restart, replan/history, methods, English/dark mode, chart date/time and saved chart replay. |
| Visual review | Selected itinerary, handoff, compass and method screens inspected; text wraps at 360dp. Clipboard preview is redacted by the Android sensitive-content flag. The temporary system clipboard overlay is visible in captures. Public evidence is text-only. |
| APK verification | Package/version/min/target checked with aapt; v2/v3 verification passed with `apksigner verify --min-sdk-version 24`; `zipalign -c -P 16 4` passed. Manifest minimum remains API 28. |

**Observed failures and fixes:** initial compilation caught unbraced Chinese-adjacent Kotlin interpolation; fixed. One initial domain test incorrectly required a harmony match within every seven-day search; it was corrected to accept a legitimate empty result, with a separate positive worked example. Matching rules were not relaxed. CI runs `38338006188` and `38338604701` passed verification/data gates but the UI assertion matched both an observation field and its saved history entry. The final test selects the versioned saved-plan row, and passes. The first failing XML is retained as `android16-ui-first-failure.xml`; it is not a passing run.

Local compilation/unit work succeeded, including final domain tests and both instrumentation APK compilations (`BUILD SUCCESSFUL`, 3m26s). An earlier redundant full build was stopped during lint after APK assembly; that interrupted command is not claimed as a full local pass. The delivered APK is compiled by the final successful CI run above.

## Delivered 0.5 package

- Filename: `TianJi-Navigator-0.5.0-debug.apk`; build-output copy: `android/app/build/outputs/apk/debug/app-debug.apk`.
- Package `com.tianji.navigator.daily`; launcher `天機・行動導航`; versionCode 7 / versionName 0.5.0; minSdk 28, target/compileSdk 36.
- Size: 33,791,280 bytes. SHA-256: `bcfee9c851f803e1f2f052339dc6b1d2628e8125a71f0902df0e5f653aa5cca9`.
- Signing certificate SHA-256: `e262754c09f6cef572c56aaa785debffa3e62e09ff87f93f87f0394e2e9de240`, matching the delivered 0.4 daily app. No uninstall is required for that update. Older `com.tianji.navigator` remains a separate package and needs explicit JSON export/import.
- CI APK artifact `11785612833`, downloaded ZIP SHA-256 `883bfa601052eb203ac9a5843a614f4f4c61bd27b4896ac40f34e9a2f0c13fb2`. Download checksum verified, APK aligned and re-signed using the retained private daily-app development key. Every non-signature ZIP entry digest matches the CI APK. Key material is never published.
- Device artifact `11785806539`, downloaded ZIP SHA-256 `6a5d341ceda5cf3544d660028f420297264fffe0f2650b0f4cdeb5d9031bdc01`. Passing UI/data XML copied into `evidence/050`.

**Limits:** no physical Samsung/One UI test, actual GPS accuracy/permission-grant/denial dialog test, real Cantonese playback, full walking-stage UI simulation or physical 0.4-to-0.5 installer exercise. Identity and persistent-data compatibility are checked; 16 KB ZIP alignment is not a 16 KB-page runtime test. No automatic city/POI/street routing, gesture/step-count engine, auspicious return time, birth-chart calculation or full-pattern judgment is implemented. The 100 independent expert-human-golden goal remains incomplete. Software test success is not evidence that a direction causes the desired real-world outcome.

---

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
