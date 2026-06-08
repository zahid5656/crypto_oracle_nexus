from pathlib import Path
import re

target = Path("app/src/main/java/com/example/ui/MarketRadarScreen.kt")
if not target.exists():
    raise SystemExit("ERROR: MarketRadarScreen.kt not found.")

text = target.read_text(encoding="utf-8")


def ensure_import(source: str, import_line: str) -> str:
    if import_line in source:
        return source

    lines = source.splitlines()
    last_import_index = -1

    for i, line in enumerate(lines):
        if line.startswith("import "):
            last_import_index = i

    if last_import_index == -1:
        raise SystemExit("ERROR: import section not found.")

    lines.insert(last_import_index + 1, import_line)
    return "\n".join(lines) + "\n"


for imp in [
    "import androidx.compose.runtime.Immutable",
    "import androidx.compose.runtime.remember",
    "import androidx.compose.foundation.layout.heightIn",
    "import androidx.compose.foundation.layout.width",
    "import kotlin.math.absoluteValue",
]:
    text = ensure_import(text, imp)


def remove_enum(source: str, name: str) -> str:
    pattern = re.compile(r"\nenum\s+class\s+" + re.escape(name) + r"\s*\{")
    match = pattern.search(source)
    if not match:
        return source

    start = match.start() + 1
    brace_start = source.find("{", match.start())
    depth = 0
    end = None

    for i in range(brace_start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break

    if end is None:
        raise SystemExit(f"ERROR: could not remove enum {name}")

    while end < len(source) and source[end].isspace():
        end += 1

    return source[:start] + source[end:]


def remove_data_class(source: str, name: str) -> str:
    pattern = re.compile(r"\n(?:@Immutable\s+)?data\s+class\s+" + re.escape(name) + r"\s*\(")
    match = pattern.search(source)
    if not match:
        return source

    start = match.start() + 1
    paren_start = source.find("(", match.start())
    depth = 0
    end = None

    for i in range(paren_start, len(source)):
        if source[i] == "(":
            depth += 1
        elif source[i] == ")":
            depth -= 1
            if depth == 0:
                end = i + 1
                break

    if end is None:
        raise SystemExit(f"ERROR: could not remove data class {name}")

    j = end
    while j < len(source) and source[j].isspace():
        j += 1

    if j < len(source) and source[j] == "{":
        depth = 0
        body_end = None
        for k in range(j, len(source)):
            if source[k] == "{":
                depth += 1
            elif source[k] == "}":
                depth -= 1
                if depth == 0:
                    body_end = k + 1
                    break
        if body_end is None:
            raise SystemExit(f"ERROR: could not remove data class body {name}")
        end = body_end

    while end < len(source) and source[end].isspace():
        end += 1

    return source[:start] + source[end:]


def remove_function(source: str, name: str) -> str:
    pattern = re.compile(
        r"\n(?:@Composable\s+)?fun\s+"
        + re.escape(name)
        + r"\s*\("
    )
    match = pattern.search(source)
    if not match:
        return source

    start = match.start() + 1
    brace_start = source.find("{", match.end())
    if brace_start == -1:
        raise SystemExit(f"ERROR: opening brace not found for function {name}")

    depth = 0
    end = None

    for i in range(brace_start, len(source)):
        if source[i] == "{":
            depth += 1
        elif source[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break

    if end is None:
        raise SystemExit(f"ERROR: could not remove function {name}")

    while end < len(source) and source[end].isspace():
        end += 1

    return source[:start] + source[end:]


# Remove old Beta Guard calls from all sections to avoid duplicate cards.
text = re.sub(
    r'''\n\s*LiveRadarBetaDivergenceGuard\(
\s*symbol\s*=\s*symbol,
\s*timeframe\s*=\s*timeframe,
\s*isLong\s*=\s*target\s*>?=\s*basePrice,
\s*isBengali\s*=\s*isBengali
\s*\)

\s*Spacer\(modifier\s*=\s*Modifier\.height\(\s*12\.dp\s*\)\)
''',
    "\n",
    text,
    flags=re.MULTILINE
)

# Remove older helper definitions.
for enum_name in [
    "DivergenceState",
    "ExecutionGuardStatus",
]:
    text = remove_enum(text, enum_name)

for data_class_name in [
    "BetaDivergenceGuardState",
    "LiveRadarMarketSnapshot",
]:
    text = remove_data_class(text, data_class_name)

for function_name in [
    "LiveRadarBetaDivergenceGuard",
    "BetaGuardMiniTile",
    "BetaGuardPenaltyTile",
    "BetaGuardAiImpactTile",
    "buildBetaDivergenceGuardState",
    "buildLiveRadarMarketSnapshot",
    "safeAbsHash",
    "directionalDivergenceState",
    "latencyStateFromSnapshot",
    "marketOutflowStateFromSnapshot",
    "derivativesStressStateFromSnapshot",
    "spreadLiquidityStateFromSnapshot",
    "assetVelocityShockStateFromSnapshot",
    "resolveEcosystemLeaderName",
    "divergenceStateColor",
    "executionGuardColor",
    "guardStatusLabel",
    "dataSyncStateLabel",
    "btcDeltaStateLabel",
    "ecosystemStateLabel",
    "marketFlowStateLabel",
    "derivativesStateLabel",
    "spreadStateLabel",
    "assetShockStateLabel",
    "readinessActionLabel",
]:
    text = remove_function(text, function_name)


guard_call = '''                    LiveRadarBetaDivergenceGuard(
                        symbol = symbol,
                        timeframe = timeframe,
                        isLong = target >= basePrice,
                        isBengali = isBengali
                    )

                    Spacer(modifier = Modifier.height(8.dp))

'''

# Insert Beta Guard into every expanded card description block:
# Hot Spot Top 3 + Futures Long Top 3 + Futures Short Top 3.
desc_pattern = re.compile(
    r'''(                    Text\(
                        text = if \(isBengali\) details\["desc_bn"\]!! else details\["desc"\]!!,
                        fontSize = 11\.sp,
                        color = TextSecondary,
                        lineHeight = 16\.sp
                    \)

)''',
    re.MULTILINE
)

text, inserted_count = desc_pattern.subn(r"\1" + guard_call, text)

if inserted_count < 3:
    print(f"WARNING: Expanded description insertion count was {inserted_count}. Trying fallback before TAKE PROFIT TARGET MATRIX anchors.")

    text = re.sub(
        r'\n\s*LiveRadarBetaDivergenceGuard\([\s\S]*?Spacer\(modifier\s*=\s*Modifier\.height\(\s*8\.dp\s*\)\)\s*\n',
        "\n",
        text,
        flags=re.MULTILINE
    )

    tp_anchor_pattern = re.compile(
        r'''(                    Text\(
                        text = "TAKE PROFIT TARGET MATRIX",)''',
        re.MULTILINE
    )

    text, inserted_count = tp_anchor_pattern.subn(guard_call + r"\1", text)

if inserted_count < 3:
    raise SystemExit(f"ERROR: Expected at least 3 Beta Guard insertions for Hot Spot + Futures Long + Futures Short, inserted {inserted_count}.")

print(f"OK: Inserted Beta Guard into {inserted_count} expanded signal card blocks.")


helpers = r'''
// ============================================================================
// LIVE RADAR — BETA DIVERGENCE GUARD FINAL COMPACT ENGINE
// Scope: Live Radar expanded signal cards only.
// Applied to: Hot Spot Top 3, Futures Long Top 3, Futures Short Top 3.
// Signal Pro / Mission Center / StartTradeFlow / Accept Signal are untouched.
// ============================================================================

enum class DivergenceState {
    STABLE,
    WARNING,
    DANGER,
    BLIND
}

enum class ExecutionGuardStatus {
    GO,
    CAUTION,
    DANGER,
    BLIND
}

@Immutable
data class LiveRadarMarketSnapshot(
    val exchangeLatencyMs: Int,
    val lastTickAgeMs: Int,
    val btcDelta5mPct: Double,
    val ecosystemLeaderName: String,
    val ecosystemLeaderDelta5mPct: Double,
    val usdtDominanceVelocityPct: Double,
    val total3VelocityPct: Double,
    val openInterestSpikePct: Double,
    val fundingBiasPct: Double,
    val liquidationPressurePct: Double,
    val spreadBps: Double,
    val takerFeeBps: Double,
    val orderBookDepthScore: Int,
    val assetVolume1mMultiple: Double,
    val assetAtr1mMultiple: Double
)

@Immutable
data class BetaDivergenceGuardState(
    val snapshot: LiveRadarMarketSnapshot,
    val latencyState: DivergenceState,
    val btcDeltaState: DivergenceState,
    val ecosystemLeaderState: DivergenceState,
    val marketOutflowState: DivergenceState,
    val derivativesStressState: DivergenceState,
    val spreadLiquidityState: DivergenceState,
    val assetVelocityShockState: DivergenceState,
    val finalGuardStatus: ExecutionGuardStatus,
    val executionReadinessPenalty: Int,
    val adjustedReadinessScore: Int,
    val narrativeEnglish: String,
    val narrativeBengali: String
)

@Composable
fun LiveRadarBetaDivergenceGuard(
    symbol: String,
    timeframe: String,
    isLong: Boolean,
    isBengali: Boolean
) {
    val snapshot = remember(symbol, timeframe, isLong) {
        buildLiveRadarMarketSnapshot(
            symbol = symbol,
            timeframe = timeframe,
            isLong = isLong
        )
    }

    val guardState = remember(snapshot, isLong) {
        buildBetaDivergenceGuardState(
            snapshot = snapshot,
            isLong = isLong
        )
    }

    val accentColor = executionGuardColor(guardState.finalGuardStatus)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        accentColor.copy(alpha = 0.038f),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(11.dp)
            )
            .border(0.75.dp, accentColor.copy(alpha = 0.44f), RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BETA DIVERGENCE GUARD",
                    fontSize = 8.6.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 0.85.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isBengali) {
                        "Market + Asset Shock + Data Safety"
                    } else {
                        "Market + Asset Shock + Data Safety"
                    },
                    fontSize = 8.7.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.115f), RoundedCornerShape(7.dp))
                    .border(0.65.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = guardStatusLabel(guardState.finalGuardStatus, isBengali),
                    fontSize = 9.2.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        BetaGuardAiImpactTile(
            status = guardState.finalGuardStatus,
            readiness = guardState.adjustedReadinessScore,
            penalty = guardState.executionReadinessPenalty,
            isBengali = isBengali
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = "Data Sync",
                state = guardState.latencyState,
                value = dataSyncStateLabel(guardState.latencyState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "BTC Delta",
                state = guardState.btcDeltaState,
                value = btcDeltaStateLabel(guardState.btcDeltaState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = guardState.snapshot.ecosystemLeaderName,
                state = guardState.ecosystemLeaderState,
                value = ecosystemStateLabel(guardState.ecosystemLeaderState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Market Flow",
                state = guardState.marketOutflowState,
                value = marketFlowStateLabel(guardState.marketOutflowState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = "Derivatives",
                state = guardState.derivativesStressState,
                value = derivativesStateLabel(guardState.derivativesStressState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Spread Risk",
                state = guardState.spreadLiquidityState,
                value = spreadStateLabel(guardState.spreadLiquidityState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = "Asset Shock",
                state = guardState.assetVelocityShockState,
                value = assetShockStateLabel(guardState.assetVelocityShockState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardPenaltyTile(
                penalty = guardState.executionReadinessPenalty,
                readiness = guardState.adjustedReadinessScore,
                status = guardState.finalGuardStatus,
                isBengali = isBengali,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF02050D),
                            accentColor.copy(alpha = 0.060f),
                            Color(0xFF02050D)
                        )
                    ),
                    RoundedCornerShape(8.dp)
                )
                .border(0.65.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (isBengali) guardState.narrativeBengali else guardState.narrativeEnglish,
                fontSize = 9.8.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BetaGuardAiImpactTile(
    status: ExecutionGuardStatus,
    readiness: Int,
    penalty: Int,
    isBengali: Boolean
) {
    val color = executionGuardColor(status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        color.copy(alpha = 0.10f),
                        Color(0xFF03111B),
                        color.copy(alpha = 0.055f)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(0.65.dp, color.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI Guard Impact",
                fontSize = 7.8.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = readinessActionLabel(status, isBengali),
                fontSize = 9.4.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(7.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$readiness/100",
                fontSize = 13.2.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1
            )

            Text(
                text = "-$penalty pts",
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BetaGuardMiniTile(
    label: String,
    state: DivergenceState,
    value: String,
    modifier: Modifier = Modifier
) {
    val color = divergenceStateColor(state)

    Column(
        modifier = modifier
            .heightIn(min = 42.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(7.dp))
            .border(0.58.dp, color.copy(alpha = 0.34f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 6.9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 8.4.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BetaGuardPenaltyTile(
    penalty: Int,
    readiness: Int,
    status: ExecutionGuardStatus,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val color = executionGuardColor(status)

    Column(
        modifier = modifier
            .heightIn(min = 42.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(7.dp))
            .border(0.58.dp, color.copy(alpha = 0.34f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readiness",
            fontSize = 6.9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$readiness/100 • -$penalty",
            fontSize = 8.4.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun buildLiveRadarMarketSnapshot(
    symbol: String,
    timeframe: String,
    isLong: Boolean
): LiveRadarMarketSnapshot {
    val seed = safeAbsHash(symbol) + safeAbsHash(timeframe) + if (isLong) 101 else 211
    val ecosystemLeader = resolveEcosystemLeaderName(symbol)

    // Real-data-ready binding contract:
    // Replace these deterministic placeholders later with Binance Sync / WebSocket / orderbook / OI / funding feeds.
    val exchangeLatencyMs = 120 + (seed % 620)
    val lastTickAgeMs = 80 + ((seed / 2) % 620)

    val rareBlind = seed % 97 == 0
    val adjustedExchangeLatency = if (rareBlind) 1550 else exchangeLatencyMs
    val adjustedLastTickAge = if (rareBlind) 1510 else lastTickAgeMs

    val btcDelta = (((seed / 3) % 13) - 6) / 12.0
    val leaderDelta = (((seed / 5) % 13) - 6) / 12.0

    val rareOutflow = seed % 43 == 0
    val usdtVelocity = if (rareOutflow) 0.72 else (((seed / 7) % 8) - 2) / 12.0
    val total3Velocity = if (rareOutflow) -0.58 else (((seed / 11) % 9) - 4) / 12.0

    val rareDerivativeStress = seed % 47 == 0
    val oiSpike = if (rareDerivativeStress) 30.0 else 3.0 + ((seed / 13) % 18)
    val fundingBias = if (rareDerivativeStress) 0.08 else (((seed / 17) % 13) - 6) / 180.0
    val liquidationPressure = if (rareDerivativeStress) 31.0 else ((seed / 19) % 22).toDouble()

    val rarePoorLiquidity = seed % 53 == 0
    val spreadBps = if (rarePoorLiquidity) 30.0 else 2.0 + ((seed / 23) % 17)
    val takerFeeBps = 6.0
    val depthScore = if (rarePoorLiquidity) 58 else (72 + ((seed / 29) % 24)).coerceIn(0, 100)

    val rareAssetShock = seed % 41 == 0
    val volumeMultiple = if (rareAssetShock) 3.2 else 0.8 + (((seed / 31) % 18) / 10.0)
    val atrMultiple = if (rareAssetShock) 3.1 else 0.8 + (((seed / 37) % 17) / 10.0)

    return LiveRadarMarketSnapshot(
        exchangeLatencyMs = adjustedExchangeLatency,
        lastTickAgeMs = adjustedLastTickAge,
        btcDelta5mPct = btcDelta,
        ecosystemLeaderName = ecosystemLeader,
        ecosystemLeaderDelta5mPct = leaderDelta,
        usdtDominanceVelocityPct = usdtVelocity,
        total3VelocityPct = total3Velocity,
        openInterestSpikePct = oiSpike,
        fundingBiasPct = fundingBias,
        liquidationPressurePct = liquidationPressure,
        spreadBps = spreadBps,
        takerFeeBps = takerFeeBps,
        orderBookDepthScore = depthScore,
        assetVolume1mMultiple = volumeMultiple,
        assetAtr1mMultiple = atrMultiple
    )
}

fun buildBetaDivergenceGuardState(
    snapshot: LiveRadarMarketSnapshot,
    isLong: Boolean
): BetaDivergenceGuardState {
    val latencyState = latencyStateFromSnapshot(snapshot)

    val btcDeltaState = directionalDivergenceState(
        deltaPct = snapshot.btcDelta5mPct,
        isLong = isLong,
        warningAbs = 0.42,
        dangerAbs = 0.78
    )

    val ecosystemLeaderState = directionalDivergenceState(
        deltaPct = snapshot.ecosystemLeaderDelta5mPct,
        isLong = isLong,
        warningAbs = 0.38,
        dangerAbs = 0.74
    )

    val marketOutflowState = marketOutflowStateFromSnapshot(snapshot)
    val derivativesStressState = derivativesStressStateFromSnapshot(snapshot)
    val spreadLiquidityState = spreadLiquidityStateFromSnapshot(snapshot)
    val assetVelocityShockState = assetVelocityShockStateFromSnapshot(snapshot)

    val allStates = listOf(
        latencyState,
        btcDeltaState,
        ecosystemLeaderState,
        marketOutflowState,
        derivativesStressState,
        spreadLiquidityState,
        assetVelocityShockState
    )

    val penalty = allStates.sumOf { state ->
        when (state) {
            DivergenceState.STABLE -> 0
            DivergenceState.WARNING -> 4
            DivergenceState.DANGER -> 10
            DivergenceState.BLIND -> 24
        }
    }.coerceIn(0, 52)

    val finalStatus = when {
        latencyState == DivergenceState.BLIND -> ExecutionGuardStatus.BLIND
        allStates.any { it == DivergenceState.DANGER } -> ExecutionGuardStatus.DANGER
        penalty >= 16 -> ExecutionGuardStatus.CAUTION
        else -> ExecutionGuardStatus.GO
    }

    val adjustedReadiness = (100 - penalty).coerceIn(0, 100)

    val narrativeEnglish = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Guard clear. Market, liquidity and asset-shock layers support validation."

        ExecutionGuardStatus.CAUTION ->
            "Caution: some safety layers weakened. Verify entry and reduce size."

        ExecutionGuardStatus.DANGER ->
            "Warning: divergence or execution risk detected. Readiness reduced."

        ExecutionGuardStatus.BLIND ->
            "Data sync drift detected. Wait for fresh market data."
    }

    val narrativeBengali = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Guard clear. Market, liquidity এবং asset-shock layer validation support করছে।"

        ExecutionGuardStatus.CAUTION ->
            "সতর্কতা: কিছু safety layer দুর্বল। entry verify করুন এবং size কমান।"

        ExecutionGuardStatus.DANGER ->
            "সতর্কতা: divergence অথবা execution risk ধরা পড়েছে। readiness কমেছে।"

        ExecutionGuardStatus.BLIND ->
            "ডেটা sync drift ধরা পড়েছে। fresh market data অপেক্ষা করুন।"
    }

    return BetaDivergenceGuardState(
        snapshot = snapshot,
        latencyState = latencyState,
        btcDeltaState = btcDeltaState,
        ecosystemLeaderState = ecosystemLeaderState,
        marketOutflowState = marketOutflowState,
        derivativesStressState = derivativesStressState,
        spreadLiquidityState = spreadLiquidityState,
        assetVelocityShockState = assetVelocityShockState,
        finalGuardStatus = finalStatus,
        executionReadinessPenalty = penalty,
        adjustedReadinessScore = adjustedReadiness,
        narrativeEnglish = narrativeEnglish,
        narrativeBengali = narrativeBengali
    )
}

fun safeAbsHash(value: String): Int {
    val hash = value.hashCode()
    return if (hash == Int.MIN_VALUE) 0 else hash.absoluteValue
}

fun directionalDivergenceState(
    deltaPct: Double,
    isLong: Boolean,
    warningAbs: Double,
    dangerAbs: Double
): DivergenceState {
    val adverseMove = if (isLong) -deltaPct else deltaPct

    return when {
        adverseMove >= dangerAbs -> DivergenceState.DANGER
        adverseMove >= warningAbs -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun latencyStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val maxDelay = maxOf(snapshot.exchangeLatencyMs, snapshot.lastTickAgeMs)

    return when {
        maxDelay > 1500 -> DivergenceState.BLIND
        maxDelay > 500 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun marketOutflowStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val outflowPressure = snapshot.usdtDominanceVelocityPct > 0.45 && snapshot.total3VelocityPct < -0.35

    return when {
        snapshot.usdtDominanceVelocityPct > 0.70 && snapshot.total3VelocityPct < -0.55 -> DivergenceState.DANGER
        outflowPressure -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun derivativesStressStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val fundingStress = snapshot.fundingBiasPct.absoluteValue >= 0.07
    val liquidationStress = snapshot.liquidationPressurePct >= 28.0
    val oiStress = snapshot.openInterestSpikePct >= 25.0

    return when {
        (fundingStress && liquidationStress) || (oiStress && liquidationStress) -> DivergenceState.DANGER
        fundingStress || liquidationStress || oiStress -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun spreadLiquidityStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val totalCostBps = snapshot.spreadBps + snapshot.takerFeeBps
    val depthWeak = snapshot.orderBookDepthScore < 68

    return when {
        totalCostBps >= 34.0 || snapshot.orderBookDepthScore < 62 -> DivergenceState.DANGER
        totalCostBps >= 22.0 || depthWeak -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun assetVelocityShockStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    return when {
        snapshot.assetVolume1mMultiple >= 3.0 || snapshot.assetAtr1mMultiple >= 3.0 -> DivergenceState.DANGER
        snapshot.assetVolume1mMultiple >= 2.2 || snapshot.assetAtr1mMultiple >= 2.2 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun resolveEcosystemLeaderName(symbol: String): String {
    val upper = symbol.uppercase()

    return when {
        upper.contains("ARB") || upper.contains("OP") || upper.contains("MATIC") || upper.contains("UNI") || upper.contains("LINK") -> "ETH Leader"
        upper.contains("SOL") || upper.contains("BONK") || upper.contains("JUP") || upper.contains("RAY") || upper.contains("PYTH") -> "SOL Leader"
        upper.contains("BNB") || upper.contains("CAKE") || upper.contains("TWT") -> "BNB Leader"
        upper.contains("AVAX") || upper.contains("JOE") -> "AVAX Leader"
        else -> "BTC Leader"
    }
}

fun divergenceStateColor(state: DivergenceState): Color {
    return when (state) {
        DivergenceState.STABLE -> CryptoGreen.copy(alpha = 0.90f)
        DivergenceState.WARNING -> AccentGold.copy(alpha = 0.88f)
        DivergenceState.DANGER -> Color(0xFFE95772)
        DivergenceState.BLIND -> Color(0xFFE96B82)
    }
}

fun executionGuardColor(status: ExecutionGuardStatus): Color {
    return when (status) {
        ExecutionGuardStatus.GO -> CryptoGreen.copy(alpha = 0.90f)
        ExecutionGuardStatus.CAUTION -> AccentGold.copy(alpha = 0.88f)
        ExecutionGuardStatus.DANGER -> Color(0xFFE95772)
        ExecutionGuardStatus.BLIND -> Color(0xFFE96B82)
    }
}

fun guardStatusLabel(status: ExecutionGuardStatus, isBengali: Boolean): String {
    return when (status) {
        ExecutionGuardStatus.GO -> "GO"
        ExecutionGuardStatus.CAUTION -> "CAUTION"
        ExecutionGuardStatus.DANGER -> "DANGER"
        ExecutionGuardStatus.BLIND -> "BLIND"
    }
}

fun dataSyncStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    val maxDelay = maxOf(snapshot.exchangeLatencyMs, snapshot.lastTickAgeMs)

    return when (state) {
        DivergenceState.STABLE -> "${maxDelay}ms OK"
        DivergenceState.WARNING -> "${maxDelay}ms Delay"
        DivergenceState.DANGER -> "${maxDelay}ms Drift"
        DivergenceState.BLIND -> "${maxDelay}ms Blind"
    }
}

fun btcDeltaStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return String.format("%.2f%%", snapshot.btcDelta5mPct)
}

fun ecosystemStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return String.format("%.2f%%", snapshot.ecosystemLeaderDelta5mPct)
}

fun marketFlowStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return when (state) {
        DivergenceState.STABLE -> "Neutral"
        DivergenceState.WARNING -> "Outflow"
        DivergenceState.DANGER -> "Drain"
        DivergenceState.BLIND -> "Blind"
    }
}

fun derivativesStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return when (state) {
        DivergenceState.STABLE -> "Normal"
        DivergenceState.WARNING -> "Crowded"
        DivergenceState.DANGER -> "Squeeze"
        DivergenceState.BLIND -> "Blind"
    }
}

fun spreadStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    val cost = snapshot.spreadBps + snapshot.takerFeeBps

    return when (state) {
        DivergenceState.STABLE -> String.format("%.1fbps OK", cost)
        DivergenceState.WARNING -> String.format("%.1fbps Drag", cost)
        DivergenceState.DANGER -> String.format("%.1fbps Poor", cost)
        DivergenceState.BLIND -> "Blind"
    }
}

fun assetShockStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    val maxShock = maxOf(snapshot.assetVolume1mMultiple, snapshot.assetAtr1mMultiple)

    return when (state) {
        DivergenceState.STABLE -> String.format("%.1fx Clear", maxShock)
        DivergenceState.WARNING -> String.format("%.1fx Fast", maxShock)
        DivergenceState.DANGER -> String.format("%.1fx Shock", maxShock)
        DivergenceState.BLIND -> "Blind"
    }
}

fun readinessActionLabel(
    status: ExecutionGuardStatus,
    isBengali: Boolean
): String {
    return when (status) {
        ExecutionGuardStatus.GO -> "Validate entry"
        ExecutionGuardStatus.CAUTION -> "Reduce size; verify"
        ExecutionGuardStatus.DANGER -> "Avoid chase; wait"
        ExecutionGuardStatus.BLIND -> "Wait for fresh data"
    }
}
'''

text = text.rstrip() + "\n\n" + helpers + "\n"

target.write_text(text, encoding="utf-8")

print("OK: Live Radar Beta Guard applied to all expanded sections and compact-refined.")
print("Only MarketRadarScreen.kt modified.")
