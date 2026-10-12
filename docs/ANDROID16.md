# Android 16 compatibility — version 0.2.1

The owner uses Android 16. This revision keeps minSDK 28 and compile/target SDK 36; it increments versionCode to 3 and versionName to 0.2.1.

## Changes
- Explicit edge-to-edge setup on supported versions, with system icon contrast following the selected app theme.
- Safe drawing insets around onboarding and the main scaffold.
- IME resizing/padding and consumed scaffold insets keep forms accessible while the keyboard is open.
- The complete device journey now requires a real visible keyboard, checks that Save remains above it, verifies exactly three alternatives, and exercises system Back after replanning.
- GitHub CI now runs the Room/Keystore and UI suites on an accelerated API 36 emulator, alongside Kotlin/Python tests and lint.
- SDK setup explicitly requests platform-tools; the action's obsolete default tools package caused the first CI setup failure.

## Verification status
Local build, instrumentation compilation, lint and debug signature verification pass. Native 64-bit ELF load segments and APK ZIP entries pass 16KB alignment checks. The development certificate matches the previously delivered 0.2.0 APK, permitting an in-place upgrade when that exact APK is installed.

**All checks passed** for source commit `b8b83c07816a28623bc5c14432f358908e21fd65`: Kotlin/Python/lint/build CI, 3 Room/migration/Keystore tests and 1 complete Android 16 UI journey (11.443 seconds for the suite). The final workflow also collected all five screenshots successfully. JUnit XML and the screenshots are committed under `docs/evidence/android16/`.

CI run: https://github.com/hkese/TianJi/actions/runs/38227645836 No physical Samsung/One UI validation or 16KB-page runtime test has been performed. Results will be recorded after the device jobs finish.

## Primary references
- https://developer.android.com/about/versions/16/behavior-changes-16
- https://developer.android.com/develop/ui/compose/system/insets-ui
- https://github.com/android-actions/setup-android/blob/v3/action.yml
- https://github.com/ReactiveCircus/android-emulator-runner/blob/v2/action.yml

APK SHA-256: `56d521cafbb24eb56a9b163240f91e63ec710518a14ebec54a8a056ec1568723`.
