# TITAN ORACLE — Oracle Feed Final Compact UI Patch

Target branch: `pre-backend-test-runtime-ux-final`

## Modified file
- `app/src/main/java/com/example/feature/oracle_feed/OracleFeedScreen.kt`

## Scope
- Oracle Feed / Nexus Terminal UI formatting only.
- No backend/API/database/execution changes.

## Main changes
- Restored compact Oracle Reference Index table with 5 coins.
- Restored compact Market Dashboard table with 10 assets.
- Removed unnecessary per-coin boxed rows and reduced wasted spacing.
- Preserved cyan-blue section headers and current dark contrast.
- Restored compact Oracle Analytical Insights alignment.
- Clamped long LONG/SHORT percentage values to readable precision.
- Replaced broken Market Feed body typography with clearer SansSerif text while keeping badge colors.
- Kept FX Open-Market Index single and compact.

## Suggested commands

```bash
git checkout pre-backend-test-runtime-ux-final
cp -f app/src/main/java/com/example/feature/oracle_feed/OracleFeedScreen.kt /path/to/repo/app/src/main/java/com/example/feature/oracle_feed/OracleFeedScreen.kt
git status
git add app/src/main/java/com/example/feature/oracle_feed/OracleFeedScreen.kt
git commit -F COMMIT_MESSAGE_ORACLE_FEED_FINAL.txt
git push origin pre-backend-test-runtime-ux-final
```

Then run:

```bash
./gradlew assembleDebug --stacktrace --no-daemon
```

or use GitHub Actions: `Build Android Debug APK` on branch `pre-backend-test-runtime-ux-final`.
