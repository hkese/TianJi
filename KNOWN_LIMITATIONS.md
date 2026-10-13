# Known limitations

## 0.5 verified software scope and remaining limits
- CI 38338847939 passes the build, 60 Kotlin / 21 Python tests, 5 Room tests and one complete offline Android 16 UI journey. These checks establish the tested software behaviour, not predictive efficacy. See `docs/TEST_RESULTS.md`.
- Time/direction selection is a limited classical profile, not full professional divination. Void/horse/seasonal-strength synthesis and 100 expert human goldens remain unfinished. Harmony can have no match even in a seven-day search.
- Directional walking guidance and optional measured start/arrival bearings are implemented, but there is no street routing, offline map, automated city/POI choice, auspicious return time, post-return sitting direction, sourced gesture or step-count engine.
- Stay duration is chosen by the user, not a traditional auspicious number. View-only mode does not perform the traditional outing.
- Compass/GPS and Cantonese voice depend on the phone hardware, permissions, location accuracy and installed offline voice. Physical Samsung/One UI, real GPS accuracy and permission-grant/denial dialogs, and actual Cantonese playback have not been tested. The UI journey does not simulate a physical walking trip.
- The delivered 0.5 APK has the same package and retained signing certificate as 0.4. CI's temporary-signer APK is not the same-key update artifact. Upgrade compatibility is checked by identity and backup/persistence tests; a physical-phone 0.4-to-0.5 installer upgrade has not been exercised here.
- Selected position fixes are included in private backups, but not in the AI handoff or research export.

## Earlier revision notes
- The 2026-10-13 clarified requirement is traditional time/direction selection with a physical itinerary. Current 0.4.0 practical recipes do not satisfy it. The missing method/conditions and phased implementation are tracked in `docs/DIRECTIONAL_NAVIGATION_PLAN_ZH.md`; no sourced gesture/step-count/stay/return engine is implemented.
- 0.4.0 adds practical navigation and manual AI handoff. Directions come from the actual door palace; the purpose-to-door mapping is an app-authored symbolic lens, not a whole-chart auspiciousness score. No compass, GPS measurement, travel routing or favourable-time search.
- 0.4.0 uses a new side-by-side package/signing identity because the former private debug key was lost when the temporary environment was cleared. Keep the old app and export/import a JSON backup; the new app cannot read another package’s private data.
- External ChatGPT/Codex interpretation is manual, user-reviewed and unvalidated; responses do not automatically become app plans.
- Version 0.3.0 enables basic CORE-001/002/004 and NAV-003 under one documented `TRADITIONAL` convention. CORE-005 remains incomplete: one published worked chart and 351 cross-implementation checks do not constitute 100 expert-adjudicated human goldens.
- Civil IANA time only; 23:00 day boundary; center always hosts in Kun 2 and Tianqin follows Tianrui. No true-solar-time correction, flying plate, Zhi Run or mixed schools. Supported local dates: 1900–2100. Calendar precision is independently sampled against 12 HKO solar terms, not certified for every date in that interval.
- The simple reading uses only the hour-stem matter palace's door. It is not a full professional judgment: no void/horse/seasonal-strength/ten-stem pattern synthesis. NAV-008 opportunity calendar remains unavailable.
- Traditional Qimen and the speculative premise are not scientifically established. Software tests do not validate predictive claims.
- M6, Transformer, executable approved model import and quantum experiment are deferred until consented real data and valid benchmarks.
- Free-text practical constraints require human review. Heuristics enforce duration/deadline/cost bounds only.
- Source and CI are published at https://github.com/hkese/TianJi.
- Real configured AI provider and Docker deployment have not been exercised. HTTPS mock provider tests pass; executable deployment remains disabled.
- Version 0.2.0 was checked on an Android 9 software emulator; versions 0.2.1 and 0.2.2 pass Android 16 emulator tests at 360dp width. No physical-phone, Samsung/One UI, large-screen, screen-reader or 16KB-page runtime validation has been performed.
- The debug APK uses a development signing key. Future releases need a stable owner-controlled signing key; export backups before replacing builds signed with another key.
- Recovered APK predates reconstruction. Current source, final APK and test evidence are separately recorded in docs/TEST_RESULTS.md.

- The owner confirmed installation of a previous APK with a screenshot; that is not a full physical-device validation of 0.3.0. The 0.3.0 delivered APK kept the preceding certificate. 0.4.0 uses a new side-by-side identity; CI artifacts still use an ephemeral CI key and should not be treated as same-key upgrades.
