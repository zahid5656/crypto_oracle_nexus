# TITAN ORACLE UI Source-of-Truth Cleanup Report

## Source ZIP used

`tiran_crypto_oracle_nexus-test-2.zip`

## 1. Active render paths found

The active UI render paths audited for this cleanup pass were:

- `app/src/main/java/com/example/MainActivity.kt`
- `app/src/main/java/com/example/ui/SignalProScreen.kt`
- `app/src/main/java/com/example/ui/LiveRadarScreen.kt`
- `app/src/main/java/com/example/ui/MissionCenterScreen.kt`
- `app/src/main/java/com/example/ui/AccuracyCenterScreen.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProScreen.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart1.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart2.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart3.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart4.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart5.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart6.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarScreen.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart1.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart2.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart3.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterScreen.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterComponentsPart1.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterComponentsPart2.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterComponentsPart3.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterComponentsPart4.kt`
- `app/src/main/java/com/example/feature/oracle_feed/OracleFeedScreen.kt`

## 2. Duplicate visual source definitions found

Confirmed duplicate semantic color aliases were found in:

- `OracleFeedScreen.kt`
- `MissionCenterComponentsPart1.kt`
- `LiveRadarComponentsPart2.kt`
- `AccuracyCenterComponentsPart1.kt`
- `AccuracyCenterScreen.kt`

The duplicated raw semantic red/green definitions were consolidated where safe without changing layout, typography, behavior, or component structure.

## 3. Duplicate color definitions found

Accepted source-of-truth colors remain defined in:

- `app/src/main/java/com/example/core/ui/theme/Color.kt`

Accepted values preserved:

```kotlin
Green = Color(0xFF34C785)
Red = Color(0xFFF6465D)
```

Consolidated safe duplicates:

- `LiveRadarInstitutionalGreen = Color(0xFF34C785)` → `LiveRadarInstitutionalGreen = CryptoGreen`
- `LiveRadarDangerRed = Color(0xFFF6465D)` → `LiveRadarDangerRed = CryptoRedText`
- `MissionCenter T_Green = Color(0xFF34C785)` → `T_Green = CryptoGreen`
- `MissionCenter T_Red = Color(0xFFF6465D)` → `T_Red = CryptoRedText`
- `OracleFeed T_Green = Color(0xFF34C785)` → `T_Green = CryptoGreen`
- `OracleFeed T_Red = Color(0xFFF6465D)` → `T_Red = CryptoRedText`
- `AccuracyCenter` old exact `Color(0xFFFF3B30)` loss/worst-trade usages → `CryptoRedText`

Remaining preserved color definitions requiring future manual review:

- `Color(0xFFFF3F60)` in `AccuracyCenterComponentsPart1.kt` is still used by active history/stat visuals. It was not changed in this cleanup because it was not one of the explicitly targeted stale values and changing it may alter the currently accepted visible chart/card appearance.

## 4. Duplicate text/label definitions found

Search hits were reviewed for:

- `CONSENSUS CONFIDENCE`
- `Consensus Confidence`
- `DIRECTION`
- `Direction`
- `RISK PROFILE`
- `Risk Profile`
- `ENTRY (LOCKED)`
- `CURRENT PRICE`
- `EXPECTED GAIN`
- `EXPECTED DROP`
- `PREDICTED`
- Bengali consensus/direction/risk variants

Action taken:

- Active visible labels were preserved.
- Stale Signal Pro comments in `SignalProComponentsPart4.kt` were updated to match the accepted visible label intent: `ENTRY`, `CURRENT`, `EXPECTED`.
- No broad Bengali glossary replacement was applied.
- Active Bengali/English label definitions were preserved because this task was source cleanup only.

Manual review still recommended for:

- Active Spot-card labels in `SignalProComponentsPart2.kt`, including `ENTRY (LOCKED)` and `CURRENT PRICE`. These were not modified because changing them would alter visible UI text and the prompt required preserving the current accepted visible UI.

## 5. Duplicate tile/style definitions found

Reviewed areas:

- Signal Pro consensus/direction/risk strip
- Live Radar consensus/direction/risk strip
- Live Radar recommended allocation tiles
- AI engine score tiles
- Mission Center timeframe/status badge style
- Oracle Feed right-side forecast/value rows

Action taken:

- No active tile/layout block was removed because the visible UI is already accepted and the duplicate status of these active blocks could not be proven dead without runtime screenshot traversal.
- No spacing, text size, border thickness, card shape, or layout value was changed.
- No monolithic screen was restored or rewritten.

## 6. Files changed

Kotlin files changed:

- `app/src/main/java/com/example/feature/accuracy_center/AccuracyCenterComponentsPart1.kt`
- `app/src/main/java/com/example/feature/accuracy_center/AccuracyCenterScreen.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart2.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterComponentsPart1.kt`
- `app/src/main/java/com/example/feature/oracle_feed/OracleFeedScreen.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart4.kt`

Documentation/root cleanup:

- Removed stale root-level `TITAN_ORACLE_*.md` patch/report artifacts from previous passes.
- Added current required report: `TITAN_ORACLE_UI_SOURCE_OF_TRUTH_CLEANUP_REPORT.md`.

## 7. Files intentionally not touched

Protected backend/business/data files were intentionally not touched:

- `app/src/main/java/com/example/viewmodel/CryptoViewModel.kt`
- `app/src/main/java/com/example/data/GeminiService.kt`
- `app/src/main/java/com/example/core/database/AppDatabase.kt`
- `app/src/main/java/com/example/core/database/SignalEntity.kt`
- Room DAO/entity files
- Binance/API/network files
- Trading/signal/radar calculation files
- Mission backend/state logic files

Protected identity/resource files were intentionally not touched:

- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- root Gradle files
- launcher/adaptive icon resources
- icon PNG resources
- `R.drawable.ic_oracle_runtime_mark`

## 8. Code removed

Removed from ZIP root:

- `TITAN_ORACLE_LIVE_RADAR_LABEL_CONTRAST_PATCH.md`
- `TITAN_ORACLE_LIVE_RADAR_SIGNAL_PRO_UI_APPLIED_SUMMARY.md`
- `TITAN_ORACLE_LIVE_RADAR_SIGNAL_PRO_UI_NEXT_PHASE.md`
- `TITAN_ORACLE_LIVE_RADAR_SIGNAL_PRO_UI_REPAIR_REPORT.md`
- `TITAN_ORACLE_LIVE_RADAR_STICKY_SCROLL_REPAIR_REPORT.md`
- `TITAN_ORACLE_PDF_UI_TARGETED_REPAIR_REPORT.md`
- `TITAN_ORACLE_REFACTOR_REPORT.md`
- `TITAN_ORACLE_SIGNAL_PRO_COMPACT_UI_REPAIR_REPORT.md`
- `TITAN_ORACLE_TIMEFRAME_COLOR_RADAR_UPGRADE_REPORT.md`
- `TITAN_ORACLE_VISUAL_PARITY_REPAIR_REPORT.md`

No active Kotlin composable implementation was removed.

## 9. Code consolidated

Consolidated semantic color source usage:

- Oracle Feed red/green aliases now resolve through shared UI bridge tokens.
- Mission Center red/green aliases now resolve through shared UI bridge tokens.
- Live Radar accepted red/green aliases now resolve through shared UI bridge tokens.
- Accuracy Center exact stale harsh red `0xFFFF3B30` usage now resolves through the accepted red token.

## 10. Code preserved because call-chain was uncertain

Preserved for safety:

- Active Signal Pro Spot-card labels and card layout blocks in `SignalProComponentsPart2.kt`.
- Active Signal Pro / Live Radar Bengali label branches.
- Active per-screen tile/style blocks that could affect accepted UI appearance.
- `Color(0xFFFF3F60)` in `AccuracyCenterComponentsPart1.kt` pending future visual review.

## 11. Protected backend/resource files confirmation

Confirmed unchanged by diff:

- `CryptoViewModel.kt`
- `GeminiService.kt`
- `AppDatabase.kt`
- `SignalEntity.kt`
- `AndroidManifest.xml`
- Gradle files
- `app/src/main/res/*`

Protected identity preserved:

- package namespace: `com.example`
- applicationId: `com.titancryptoraclenexus.app`
- app label: `Titan Oracle`
- launcher background: `#080B11`
- runtime icon reference: `R.drawable.ic_oracle_runtime_mark`

## 12. Remaining source-of-truth risks

Remaining risks:

1. Some active UI labels still exist in multiple modules by design. Consolidating them into shared helpers would be safer after screenshot-based parity tests exist.
2. `Color(0xFFFF3F60)` remains in Accuracy Center active visuals and should be reviewed separately before consolidation.
3. Bengali label sources are still distributed across modules. Full glossary replacement should remain a separate dedicated task.
4. Shared tile style extraction should be done gradually, one module at a time, to avoid accidental visual changes.

## 13. Next recommended patch target

Recommended next patch:

- Build the project externally with `./gradlew assembleDebug`.
- Run visual smoke tests on Signal Pro, Live Radar, Mission Center, Oracle Feed, and Accuracy Center.
- Then migrate one small label/color helper at a time into shared UI source-of-truth files only after screenshot parity is confirmed.

## Verification status

- Kotlin brace/parenthesis balance check: passed.
- Obvious invalid `stickyHeader` import scan: passed.
- Protected backend/resource diff check: passed.
- Icon/resource diff check: passed.
- No broad Bengali glossary replacement applied: confirmed.
- Accepted red/green source-of-truth values remain: confirmed.
- Old exact semantic values `0xFFFF3B30` and `0xFF34C759` removed/consolidated where found in Kotlin source: confirmed.
- Gradle build: not executed because this sandbox has no executable `gradlew` and no system `gradle`.
