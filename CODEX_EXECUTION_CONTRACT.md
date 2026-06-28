# CODEX EXECUTION CONTRACT

This contract is mandatory for all Codex work on TITAN CRYPTO ORACLE NEXUS.

## Active Branch

`pre-backend-test-codex-runtime-ux-001`

This branch is isolated for Codex runtime UX work. Do not work on `main`.

## Authority Model

- Product/architecture authority: ChatGPT Project Mode / owner.
- Execution authority: Codex only after explicit `MODE: APPLY ONLY`.
- Codex must not invent new requirements.
- Codex must not expand scope.

## Current Scope

Complete only the following tasks:

13. Leverage `X` suffix behavior:
   - During numeric input, `X` disappears from the editable box.
   - After user completes custom leverage input, `X` appears again.
   - Input accepts digits only.
   - Invalid text such as `GPT`, `3GPT`, `X3`, blank, negative, or decimal text must not be accepted as final leverage.

21. Runtime scroll/performance stabilization:
   - Reduce unnecessary continuous animation where safe.
   - Avoid heavy recomposition from dense Mission/Live Radar cockpit sections.
   - Use stable keys/state where appropriate.

22. Live Radar tile expand/collapse behavior:
   - Match Signal Pro behavior as closely as possible.
   - One expanded tile at a time.
   - Tapping another tile collapses previous and expands new tile smoothly.

23. Live Radar smooth animation:
   - Use Compose animation such as `animateContentSize`, `AnimatedVisibility`, or equivalent.
   - Avoid instant snap-open behavior.

24. Live Radar no jumpy screen behavior:
   - Avoid abrupt scroll position jumps.
   - Avoid full-list state resets.
   - Preserve stable item identity.

26. Signal Insight bottom buttons:
   - Keep bottom actions in normal scroll content flow.
   - Ensure BACK and ACCEPT SIGNAL are fully visible across device displays.
   - Replace current arrow style with clean text arrows.

27. Decision Brief spacing and transition speed:
   - Slightly improve button spacing.
   - Reduce TITAN Insight transition speed to align with VERIFY ENTRY motion pacing.

## Explicit Non-Scope

Do not handle these unless separately approved:

- Mission Center organization/sequence full redesign.
- Global warning meter mission numbering.
- Mission card serial numbering.
- Backend arbitration logic.
- Auto-Trading/Copilot execution backend logic.
- Binance sync.
- Real order execution.

## Allowed Files

Only modify:

- `app/src/main/java/com/example/feature/live_radar/LiveRadarComponentsPart1.kt`
- `app/src/main/java/com/example/feature/live_radar/LiveRadarScreen.kt`
- `app/src/main/java/com/example/feature/mission_center/MissionCenterComponentsPart3.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProComponentsPart3.kt`
- `app/src/main/java/com/example/feature/signal_pro/SignalProMockupScreen.kt`

## Forbidden Files

Do not modify:

- `GeminiService.kt`
- `CryptoViewModel.kt`
- `AppDatabase.kt`
- `SignalEntity.kt`
- `CryptoData.kt`
- DAO files
- Repository files
- Room/database schema files
- Binance/API/network files
- Gradle files
- `AndroidManifest.xml`
- signing/secrets/API-key files
- real trading execution files

## Verification

Before editing:

```bash
git status
git branch --show-current
git diff --name-only
```

After editing:

```bash
git diff --name-only
git diff --stat
git diff --check
```

Build check when requested:

```bash
./gradlew assembleDebug --stacktrace --no-daemon
```

## Failure Rule

If any unauthorized file changes, revert it immediately.
If build fails, do not make broad refactors. Report the exact error and propose targeted fix only.
