# 0.7 — direct daily guidance

The owner confirmed the ChatGPT connection now works but still could not identify what to do next. The primary requirement is direct traditional time/direction guidance for 趨吉避凶.

Implementation plan:

1. Make Home usable without writing a goal: one button uses 今日趨吉避凶, bounded by the selected zone's remaining local day. A separate optional entry handles a specific intention. Implemented.
2. Put the executable next step before AI explanation: wait/ready/active/completed/expired state, departure window, direction, one plain-language action and the selected compass itinerary. Implemented.
3. Display only actual excluded palaces from DirectionRules.decisions for that candidate's own chart and purpose, scoped to that departure window. Missing matches are not labelled bad luck; expired and completed guides show no avoidance list. No new calculation convention, causal forecast, success probability or whole-day danger classification is introduced. Implemented.
4. Preserve no-match results, with a daily-only one-tap next-window search retaining the chosen purpose. Update in-app instructions to three steps. Keep existing authorization, offline data and history. Implemented.
5. Run domain boundary/provenance/DST regressions and native one-tap/keyboard/persistence/compass flow; visually review Home, result, avoidance and three-step help; sign a retained-key APK. Complete: final source 16d580aeb4030e7ab4e744262088dab2024fdf21 passes CI 38823712581 (94 Kotlin, 21 Lab, 7 Android 16 data and 3 App tests, build/lint). Initial emulator SDK download failure passed on infrastructure retry without source changes. Signed 0.7.0 / versionCode 10 APK saved for delivery; exact hashes and evidence in TEST_RESULTS.md.

Source is the existing classical-direction-civil/1.0 profile described in DIRECTION_RULES_1_0.md. The displayed exclusions are the same conditions already used to reject candidates, including explicit app-profile restrictions. This is not a full professional chart synthesis or evidence of predictive effectiveness. Existing expert-golden and physical sensor limits remain.

AI receives the same computed exclusions alongside each candidate chart. Its free text explains the traditional choice; executable time/direction/exclusion fields remain calculated app fields. The owner confirmed connection only; live generated interpretation quality is not independently verified.
