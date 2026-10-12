# Progress — revision 0.2.2

| Milestone | Status | Observed exit / remaining gate |
|---|---|---|
| M0 Foundation | Implemented | Native app compiles; Room and shared contracts; CI configuration supplied |
| M1 Offline Navigator | Implemented; device flow passed | Goal/milestone CRUD, three bounded alternatives, linked journal, immutable replan, atomic backups; Room tests pass |
| M2 Verified Qimen Core | BLOCKED | Deterministic interface/provenance/cache are implemented; authoritative conventions and independent fixtures missing, **0/100** |
| M3 AI Options | Implemented with external test blocked | Keystore, per-request consent, strict structured adapter; TLS mock tests pass; no real provider credentials supplied |
| M4 Research Lab | Implemented | 20 tests pass; Ruff passes; CPU synthetic A/B/C/calendar/shuffle evaluation completed |
| M5 Polish / APK | Verified debug APK delivered | Latest debug build and v2 signature pass; lint 0 errors / 8 warnings; complete offline UI test passed, including replan, persistence, English and dark mode |
| M6 Advanced optional | Deferred | Requires enough consented real data and valid benchmarks; no executable model deployment |
| GitHub publication | Published | Full source is at https://github.com/hkese/TianJi; Kotlin/Python/lint/build CI passes; all 4 Android 16 device tests and screenshot collection pass |

Evidence: `docs/TEST_RESULTS.md`, `docs/evidence/`, and `docs/TRACEABILITY.md`.
Synthetic run: `afe3a603b68ad3d710bc8f1ace708ca0e95a48bab909aed7fe8575bfc93f9635` (480 fictional records, 24 fictional subjects). It establishes no predictive or causal validity.

Recovery: the original workspace was pruned during a stalled login. The original installed APK was recovered and saved. This source is a reconstructed revision with separately observed test results; it has not been pushed into an unrelated GitHub repository.

Android 16 follow-up: API 36 complete offline journey and 3 Room/Keystore tests passed on GitHub's accelerated emulator. Version 0.2.1 adds edge-to-edge safe areas, keyboard resizing and system bar theme contrast. The source is public in the owner-created repository. Local software-emulator startup did not complete; no local API 36 pass is claimed.

Final Android 16 CI: https://github.com/hkese/TianJi/actions/runs/38227645836 (both jobs passed). Verified app source: `b8b83c07816a28623bc5c14432f358908e21fd65`. Documentation-only publication after this run does not alter app or test code.

## 0.2.2 usability follow-up
The user supplied an installed-app screenshot: installation is confirmed, but actual Qimen remains blocked. Added offline city timezone selection (Chinese/English search, device shortcut and DST-aware labels), Chinese guide with navigation, localized states/theme/date picker and explicit unavailable-chart UI. Existing records and preferences are retained. Local build, 27 Kotlin unit tests, APK signature and lint pass. Android 16 device validation is pending; prior 0.2.1 evidence does not verify these changes.
