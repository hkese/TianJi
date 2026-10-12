# Verification evidence

The final debug APK hash is in `APK-SHA256.txt`; package/signature metadata is in `apk-*.log`.

- `android-final-build.log`: passing complete-journey build after the navigation fix.
- `android-palette-build.log`: final color-only polish build, 0 lint errors.
- `TEST-*.xml`: 23 Kotlin unit tests, all pass.
- `room-device.log`: 3 Room/migration/Keystore device tests, all pass.
- `ui-complete-device.log`: the full offline journey (1 test), including replan, activity recreation, English and dark mode.
- `ui-offline-device.log`: an earlier run of the same journey before expansion; do not count it as another distinct test.
- `pytest-final.log`: 20 Python tests pass. `ruff-final.log`: static checks pass.
- `synthetic-demo-metrics.json`: seeded fictional CPU experiment; not evidence of real predictive validity.
- `screenshots/`: actual emulator captures. `settings-dark.png` is from the final palette build. Home and planner are from the complete passing journey.
- `device-runtime.log`: complete-journey AndroidRuntime/TestRunner log.

Earlier build logs remain for provenance. Passing tests establish behavior covered by those tests; they do not validate Qimen rules or predictions. No golden reference charts have been verified.
