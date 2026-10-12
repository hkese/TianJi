# Known limitations
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

- The owner confirmed installation of a previous APK with a screenshot; that is not a full physical-device validation of 0.3.0. The delivered local debug APK keeps the previous signing certificate; GitHub Actions artifacts use a separate ephemeral CI key.
