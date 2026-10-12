# Known limitations
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
