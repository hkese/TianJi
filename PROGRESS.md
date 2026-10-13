# 0.5.0 — sourced time/direction itineraries (verified debug delivery)

- Selected `classical-direction-civil/1.0`: general outings use Qi-Men conjunction; harmony uses Ren Dun / Jade Maiden. Fixed exclusions and limits are documented in `docs/DIRECTION_RULES_1_0.md`.
- Implemented offline 24/72/168-hour searches, separate question and candidate charts, explicit no-match results and chronological candidates.
- Implemented persisted departure/arrival/stay/return stages, optional foreground location capture, compass with optional true-north correction, and installed offline Cantonese speech.
- New manual AI prompt explains the calculated itinerary and omits coordinates; legacy practical plans remain accessible as history. Added Chinese instructions and preserved the 0.4 package/signing identity for updates.
- Initial compile errors in Chinese string interpolation were fixed. Tests correctly allow a harmony search to have zero matches; no fallback or fabricated candidate is inserted.
- Verified app source: `5455294381c1238128d695069a1d085783e9629a`. Both jobs pass in https://github.com/hkese/TianJi/actions/runs/38338847939 .
- 60 Kotlin unit tests, 21 Python tests, Ruff and Android lint/build pass. Android 16 offline emulator: 5 Room/backup/Keystore tests plus the complete native UI journey pass. Two earlier runs failed an ambiguous history-test selector; the final selector targets the saved plan version. Native compass and method screens reviewed at 360dp.
- Signed APK: 0.5.0 / versionCode 7, 33,791,280 bytes; SHA-256 `bcfee9c851f803e1f2f052339dc6b1d2628e8125a71f0902df0e5f653aa5cca9`. v2/v3 signatures and 16 KB ZIP alignment pass. Same package and retained signer as 0.4; update without uninstalling. All non-signature APK entries match the successful CI build.
- APK saved for delivery; source is published. Physical Samsung GPS/compass accuracy, permission-dialog paths and actual Cantonese playback remain unverified. No automatic city/street routing, ritual gestures/counts, auspicious return time or full human-golden acceptance is claimed. Details: `docs/TEST_RESULTS.md`.

---

# 2026-10-13 product correction — directional Qimen navigation

The user clarified that the desired primary experience is traditional time/direction selection and an explicit departure, destination, action, stay and return itinerary. The 0.4.0 practical recipes do not meet that need. Its successful software tests do not establish product acceptance for this clarified requirement.

Historical checkpoint before implementation: primary-source investigation and the revised workflow/acceptance plan were recorded in `docs/DIRECTIONAL_NAVIGATION_PLAN_ZH.md`. At that checkpoint, code/deliverable remained 0.4.0. The verified 0.5 delivery above supersedes that status; unsourced gestures, counts and return timings remain excluded.

---

# Current revision 0.4.0 — goal-to-action navigation

Implemented and verified. Source commit `75eca621ee280abc11b83484b2d7743d8c8c7f40`; CI https://github.com/hkese/TianJi/actions/runs/38322606240 passes both jobs.

- 46 Kotlin unit tests, 21 Python tests, Ruff and Android lint/build gate pass.
- Android 16 (API 36), 360dp, Wi-Fi/data disabled: 4 Room/backup/Keystore tests and complete 30.99-second UI journey pass.
- Initial device run exposed a replan IME/Back race; explicit focus dismissal and synchronized system-Back test fixed it. The failing initial XML is retained alongside the passing result.
- Reviewed native screenshots for navigation steps, selected first action and AI handoff. Android's temporary clipboard preview appears on two captures; it is system UI, not clipped app content.
- Deliverable: 0.4.0 debug APK, 33,603,063 bytes, SHA-256 `52086ecdf68c5e179d8c0996c25970140cab0eddedb2f4693e6476c36f235e7e`. APK v2/v3 signature and 16 KB ZIP alignment pass.
- APK was compiled by the verified CI run, then re-signed with the privately retained daily-app debug key. Every non-signature ZIP entry is byte-identical to the CI artifact. Local 46 unit tests passed; the redundant local full build was stopped after CI verification. No local emulator or physical-device pass is claimed.

- Purpose picker: work, study, communication and everyday goals.
- Three concrete offline recipes per purpose, each with three steps, completion evidence and an actual chart-derived optional direction, door/star/deity.
- One canonical chart per plan; goal snapshot preserved for history. Selected first step pinned in planner and home. Expired windows offer a fresh chart/replan.
- Manual ChatGPT/Codex prompt review, clipboard, Android share and explicit browser open. No automatic upload or answer import. Existing opt-in configured-provider comparison unchanged.
- Source/key recovery: the ephemeral workspace was cleared. Source recovered from hkese/TianJi. Prior signing private key not found in the workspace or saved source archive. New package `com.tianji.navigator.daily`, label `天機・行動導航`, installs alongside the old app and imports its JSON backups. No uninstall required.
- Full-chart divination, calibrated predictions, 100 expert goldens, opportunity calendar and advanced Research Lab models remain unfinished; no scientific validation claim.

---

# Current revision 0.3.0 — basic on-device charts

- Ruleset/reference selection complete; native offline calculation and sourced basic Chinese interpretations implemented.
- Core checks passed: 1 independent published worked chart, 351 cross-implementation charts (9 predeclared whole-day-term convention exclusions), 12 HKO major terms, 4,380 full-year structural checks, civil zone/DST/23:00 boundaries.
- Human-golden target: incomplete. These counts are distinct and do not validate predictions.
- Chinese chart/date/time/city pickers, palace details, saved charts and backup compatibility implemented; full Android build and Android 16 device verification passed (4 data tests + 1 offline journey).
- 37 Kotlin unit tests, 21 Python tests and Ruff pass; lint 0 errors / 7 warnings.
- Same-signature 0.3.0 debug APK delivered; source commit `88365df07dbccc17aba2492779f003112600fd12`, CI https://github.com/hkese/TianJi/actions/runs/38241131970 passed.

The earlier milestone record follows for history; current results are in docs/TEST_RESULTS.md.

---

# Progress — revision 0.2.2

| Milestone | Status | Observed exit / remaining gate |
|---|---|---|
| M0 Foundation | Implemented | Native app compiles; Room and shared contracts; CI configuration supplied |
| M1 Offline Navigator | Implemented; device flow passed | Goal/milestone CRUD, three bounded alternatives, linked journal, immutable replan, atomic backups; Room tests pass |
| M2 Verified Qimen Core | BLOCKED | Deterministic interface/provenance/cache are implemented; authoritative conventions and independent fixtures missing, **0/100** |
| M3 AI Options | Implemented with external test blocked | Keystore, per-request consent, strict structured adapter; TLS mock tests pass; no real provider credentials supplied |
| M4 Research Lab | Implemented | 20 tests pass; Ruff passes; CPU synthetic A/B/C/calendar/shuffle evaluation completed |
| M5 Polish / APK | Verified debug APK delivered | Latest debug build and v2 signature pass; lint 0 errors / 6 warnings; complete offline UI test passed, including replan, persistence, English and dark mode |
| M6 Advanced optional | Deferred | Requires enough consented real data and valid benchmarks; no executable model deployment |
| GitHub publication | Published | Full source is at https://github.com/hkese/TianJi; Kotlin/Python/lint/build CI passes; all 4 Android 16 device tests and screenshot collection pass |

Evidence: `docs/TEST_RESULTS.md`, `docs/evidence/`, and `docs/TRACEABILITY.md`.
Synthetic run: `afe3a603b68ad3d710bc8f1ace708ca0e95a48bab909aed7fe8575bfc93f9635` (480 fictional records, 24 fictional subjects). It establishes no predictive or causal validity.

Recovery: the original workspace was pruned during a stalled login. The original installed APK was recovered and saved. This source is a reconstructed revision with separately observed test results; it has not been pushed into an unrelated GitHub repository.

Android 16 follow-up: API 36 complete offline journey and 3 Room/Keystore tests passed on GitHub's accelerated emulator. Version 0.2.1 adds edge-to-edge safe areas, keyboard resizing and system bar theme contrast. The source is public in the owner-created repository. Local software-emulator startup did not complete; no local API 36 pass is claimed.

Final Android 16 CI: https://github.com/hkese/TianJi/actions/runs/38227645836 (both jobs passed). Verified app source: `b8b83c07816a28623bc5c14432f358908e21fd65`. Documentation-only publication after this run does not alter app or test code.

## 0.2.2 usability follow-up
The user supplied an installed-app screenshot: installation is confirmed, but actual Qimen remains blocked. Added offline city timezone selection (Chinese/English search, device shortcut and DST-aware labels), Chinese guide with navigation, localized states/theme/date picker and explicit unavailable-chart UI. Existing records and preferences are retained. Local build, 27 Kotlin unit tests, APK signature and lint pass. Final Android 16 validation passes (3 Room tests plus 1 expanded UI journey). Visual QA caught and fixed the calendar dialog locale; the final screenshot and new Chinese-month assertion confirm it. CI: https://github.com/hkese/TianJi/actions/runs/38233591473 . Source: `32456b58c5f31b9c37799a859ad87737cec900dc`.
