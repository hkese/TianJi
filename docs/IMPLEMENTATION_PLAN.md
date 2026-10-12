# 0.3.0 implementation plan — requested basic offline calculation
1. Select one documented convention, primary traditional/calendrical references and independently published comparison charts. **Complete.**
2. Implement native deterministic Core, transparent basic Chinese reading, accessible date/time/city pickers and saved chart replay. **Implemented.**
3. Run independent chart/calendar checks, contract/backup tests and Android 16 device journey; fix observed failures. **In progress.**
4. Publish source to hkese/TianJi and deliver a same-signature debug APK with honest validation limits. **Pending.**

Full M2 verified-core acceptance still needs 100 independent human goldens; the requested basic mode is explicitly `TRADITIONAL`, not `VERIFIED`. One public worked example, cross-engine regression cases and official solar-term checks are tracked separately. No test count is presented as proof of predictive efficacy.

---

# Implementation and recovery plan
1. M0: Restore deterministic source, pinned Gradle modules and contracts. Save a source checkpoint.
2. M1: Offline onboarding, goal/milestone editing, constrained alternative plans, linked journal, immutable replan history and atomic backup round trip.
3. M2: Pure Kotlin ruleset interface, canonical blocked snapshots and provenance. Actual traditional computation remains BLOCKED until an authoritative convention and 100 independent fixtures are provided.
4. M3: Keystore credentials, explicit request review, HTTPS provider adapter and strict structured comparison validation.
5. M4: Consent-aware import, leakage-safe time/group experiments, A/B/C and calendar/shuffle controls, registry and lockbox.
6. M5: Compile debug APK, unit tests, lint, device journey where environment permits, persist source and release evidence.
7. GitHub: complete. Owner created hkese/TianJi; source and CI were published through the connected account. Android 16 device verification also passed; see docs/ANDROID16.md.

Recovery incident: automated scratch cleanup removed the original source after the first build. The installed 35,398,738-byte APK was recovered from the still-running test emulator and saved. Its SHA-256 is cce7d837531fa7184b84eeb11d048a56e442f19b9ce40cfdae6041b5de7cd545. Reconstructed source is a new revision and must be tested separately. Earlier 20 Kotlin/20 Python test results cannot substitute for tests of this revision.
