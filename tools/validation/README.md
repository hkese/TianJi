# Reproduce external regression fixtures

`reference-qimen.mts` calls a separate MIT-licensed implementation. It does not import TianJi. Checkout https://github.com/anthonylee1994/qimen at `77fa5605e3d7477e43e1fcc569e4c9594856afd1` in a separate directory. Install only `tsx@4.20.6 lunar-typescript@1.8.6` there with `npm install --ignore-scripts --no-audit --no-fund --no-save`; do not run its application scripts. Then:

```
node --import /absolute/reference/node_modules/tsx/dist/loader.mjs tools/validation/reference-qimen.mts /absolute/reference contracts/fixtures/cross-engine-charts.json 77fa5605e3d7477e43e1fcc569e4c9594856afd1
./gradlew :android:core-qimen:test
```

The checked-in corpus contains 351 cases from a predeclared grid of 360 timestamps (2024/2026, each month's 8/18/28, hours 0/4/10/16/20 at minute 30). Nine whole-day solar-term disagreements are explicitly excluded before comparing TianJi. Reference year/month pillars use a different convention and are not comparison targets; day/hour, Yuan, Dun, Ju and every palace's door, primary star, deity and main/hosted stems are compared. Tianqin hosting is checked separately.

Both implementations use the 6tail calendar family. These are cross-implementation regression checks, NOT 351 independent expert goldens and NOT evidence of divination accuracy. Separate tests use the published Qimena worked example and HKO official solar terms. The public example's expected values are written directly in `TraditionalCoreTest`, not produced by this script.
