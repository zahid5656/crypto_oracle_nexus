# TITAN ORACLE Live Radar Sticky + Scroll Repair Report

## 1. Modifications Made

- Promoted the full Live Radar control area into one sticky control cluster:
  - `LIVE QUANT RADAR`
  - `Market Intelligence`
  - language toggle
  - current market regime card
  - `SHORT-TERM SCALP ORACLE`
  - timeframe selector row
- Reduced unnecessary vertical gaps around the sticky scalp oracle/timeframe section.
- Reduced the gap between the sticky timeframe area and the first radar trigger section.
- Removed the repeated `Tap for Deep Quant Info` prompt from expanded Live Radar cards.
  - The prompt remains visible only in collapsed mode.
- Removed `animateContentSize()` from Live Radar signal cards to reduce the expansion scroll-anchor jump that pushed the view toward the bottom `Accept Signal` area.
- Removed tinted/neon background fill from Live Radar consensus/profile tiles:
  - `Consensus Confidence`
  - `Direction`
  - `Risk Profile`
  - `Recommended Position Allocation` tiles
- Preserved the neon/semantic border treatment while keeping the tile body dark for clearer text readability.

## 2. Files Changed

- `app/src/main/java/com/example/feature/live_radar/LiveRadarScreen.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart1.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart3.kt`

## 3. Files Intentionally Not Touched

- `CryptoViewModel.kt`
- `GeminiService.kt`
- Room/database files
- Binance/API/network files
- trading/signal/radar calculation logic
- Mission Center logic
- Signal Pro logic
- icon PNGs
- launcher/adaptive icon resources
- `R.drawable.ic_oracle_runtime_mark`
- `AndroidManifest.xml`
- Gradle files
- package namespace
- applicationId
- app label

## 4. Build / Check Status

- Static Kotlin brace/parenthesis balance check: passed.
- Invalid `stickyHeader` import scan: passed.
- Live Radar signal-card `animateContentSize()` removal scan: passed.
- Collapsed-only `Tap for Deep Quant Info` logic check: passed.
- ZIP integrity test: passed after packaging.
- Gradle build was not executed in this sandbox because there is no executable `gradlew` script and no system Gradle command available.

## 5. Remaining Risks

- The sticky cluster is intentionally taller because it now includes the page title and market regime card as requested. Final comfort should be verified on the target phone density.
- The scroll-jump repair removes animated expansion from the affected cards. If any jump remains, the next fix should convert Live Radar signal cards into separate keyed `LazyColumn` items for stronger scroll anchoring.
- Visual compactness may need one more small spacing pass after testing on the real device.

## 6. Next Recommended Step

Run the real build and test on-device:

```bash
./gradlew assembleDebug
```

Then verify:

- The full top Live Radar control area remains sticky.
- The timeframe bottom spacing is compact.
- The first radar section is closer to the sticky area.
- Expanded cards no longer show `Tap for Deep Quant Info`.
- Expanded cards no longer jump directly to the `Accept Signal` area.
- Consensus/profile/allocation tile text remains clearly readable without colored background glow.
