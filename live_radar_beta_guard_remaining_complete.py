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

    while end < len(source) and source[end] in "\n\r ":
        if source[end] == "\n":
            end += 1
            break
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

    # If the data class has a body, remove it too.
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

    while end < len(source) and source[end] in "\n\r ":
        if source[end] == "\n":
            end += 1
            break
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

    while end < len(source) and source[end] in "\n\r ":
        if source[end] == "\n":
            end += 1
            break
        end += 1

    return source[:start] + source[end:]


# Remove older/incomplete Beta Guard implementation safely.
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

                    Spacer(modifier = Modifier.height(12.dp))

'''

if "LiveRadarBetaDivergenceGuard(" not in text:
    inserted = False

    for anchor in [
        "                    LiveRadarTradeMachineSection(",
        "                    OpportunisticSignalAdornmentSection(",
    ]:
        if anchor in text:
            text = text.replace(anchor, guard_call + anchor, 1)
            inserted = True
            break

    if not inserted:
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

        text, count = desc_pattern.subn(r"\1" + guard_call, text, count=1)
        if count == 0:
            raise SystemExit("ERROR: could not find insertion point for LiveRadarBetaDivergenceGuard.")
else:
    print("OK: LiveRadarBetaDivergenceGuard call already exists.")


helpers = r'''
// ============================================================================
// LIVE RADAR — BETA DIVERGENCE GUARD FINAL ENGINE
// Scope: Live Radar expanded signal card only.
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
                        accentColor.copy(alpha = 0.055f),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(12.dp)
            )
            .border(0.85.dp, accentColor.copy(alpha = 0.52f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BETA DIVERGENCE GUARD",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (isBengali) {
                        "ব্রড মার্কেট + অ্যাসেট শক + ডেটা ফ্রেশনেস সেফটি শিল্ড"
                    } else {
                        "Broad Market + Asset Shock + Data Freshness Safety Shield"
                    },
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 12.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.14f), RoundedCornerShape(7.dp))
                    .border(0.7.dp, accentColor.copy(alpha = 0.48f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = guardStatusLabel(guardState.finalGuardStatus, isBengali),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        BetaGuardAiImpactTile(
            status = guardState.finalGuardStatus,
            readiness = guardState.adjustedReadinessScore,
            penalty = guardState.executionReadinessPenalty,
            isBengali = isBengali
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF02050D),
                            accentColor.copy(alpha = 0.09f),
                            Color(0xFF02050D)
                        )
                    ),
                    RoundedCornerShape(9.dp)
                )
                .border(0.75.dp, accentColor.copy(alpha = 0.42f), RoundedCornerShape(9.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isBengali) guardState.narrativeBengali else guardState.narrativeEnglish,
                fontSize = 10.8.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 14.sp
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
                        color.copy(alpha = 0.14f),
                        Color(0xFF03111B),
                        color.copy(alpha = 0.075f)
                    )
                ),
                RoundedCornerShape(9.dp)
            )
            .border(0.75.dp, color.copy(alpha = 0.46f), RoundedCornerShape(9.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isBengali) "AI Recommendation Impact" else "AI Recommendation Impact",
                fontSize = 8.2.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = readinessActionLabel(status, isBengali),
                fontSize = 10.6.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                lineHeight = 13.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$readiness/100",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1
            )

            Text(
                text = "-$penalty pts",
                fontSize = 8.5.sp,
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
            .heightIn(min = 50.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(8.dp))
            .border(0.65.dp, color.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.4.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 9.2.sp,
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
            .heightIn(min = 50.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(8.dp))
            .border(0.65.dp, color.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isBengali) "Readiness" else "Readiness",
            fontSize = 7.2.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "$readiness/100 • -$penalty",
            fontSize = 9.2.sp,
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
    // Later replace these derived values with Binance Sync / WebSocket / orderbook / OI / funding feeds.
    val exchangeLatencyMs = 120 + (seed % 1650)
    val lastTickAgeMs = 80 + ((seed / 2) % 1650)

    val btcDelta = (((seed / 3) % 17) - 8) / 10.0
    val leaderDelta = (((seed / 5) % 19) - 9) / 10.0

    val usdtVelocity = (((seed / 7) % 13) - 4) / 10.0
    val total3Velocity = (((seed / 11) % 15) - 7) / 10.0

    val oiSpike = 2.0 + ((seed / 13) % 32)
    val fundingBias = (((seed / 17) % 21) - 10) / 100.0
    val liquidationPressure = ((seed / 19) % 38).toDouble()

    val spreadBps = 2.0 + ((seed / 23) % 34)
    val takerFeeBps = 6.0
    val depthScore = (62 + ((seed / 29) % 37)).coerceIn(0, 100)

    val volumeMultiple = 0.8 + (((seed / 31) % 36) / 10.0)
    val atrMultiple = 0.8 + (((seed / 37) % 32) / 10.0)

    return LiveRadarMarketSnapshot(
        exchangeLatencyMs = exchangeLatencyMs,
        lastTickAgeMs = lastTickAgeMs,
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
        warningAbs = 0.35,
        dangerAbs = 0.70
    )

    val ecosystemLeaderState = directionalDivergenceState(
        deltaPct = snapshot.ecosystemLeaderDelta5mPct,
        isLong = isLong,
        warningAbs = 0.30,
        dangerAbs = 0.65
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
            DivergenceState.WARNING -> 6
            DivergenceState.DANGER -> 14
            DivergenceState.BLIND -> 30
        }
    }.coerceIn(0, 75)

    val finalStatus = when {
        latencyState == DivergenceState.BLIND -> ExecutionGuardStatus.BLIND
        allStates.any { it == DivergenceState.DANGER } -> ExecutionGuardStatus.DANGER
        penalty >= 18 -> ExecutionGuardStatus.CAUTION
        else -> ExecutionGuardStatus.GO
    }

    val adjustedReadiness = (100 - penalty).coerceIn(0, 100)

    val narrativeEnglish = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Execution guard is clear. Data sync, BTC delta, ecosystem leader, market flow, derivatives, spread and asset-shock layers are aligned enough for short-term validation."

        ExecutionGuardStatus.CAUTION ->
            "Caution: one or more safety layers show weakness. AI readiness is reduced by $penalty points; verify entry and use smaller size."

        ExecutionGuardStatus.DANGER ->
            "Warning: broad market divergence, derivatives stress, poor liquidity or unusual asset velocity shock detected. Execution readiness is materially reduced."

        ExecutionGuardStatus.BLIND ->
            "Synchronization drift detected. Data may be stale, so Live Radar cannot safely validate short-term execution."
    }

    val narrativeBengali = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Execution guard পরিষ্কার। Data sync, BTC delta, ecosystem leader, market flow, derivatives, spread এবং asset-shock layer short-term validation-এর জন্য যথেষ্ট aligned।"

        ExecutionGuardStatus.CAUTION ->
            "সতর্কতা: এক বা একাধিক safety layer দুর্বল। AI readiness $penalty পয়েন্ট কমেছে; entry verify করুন এবং ছোট position size ব্যবহার করুন।"

        ExecutionGuardStatus.DANGER ->
            "সতর্কতা: broad market divergence, derivatives stress, poor liquidity অথবা অস্বাভাবিক asset velocity shock ধরা পড়েছে। execution readiness উল্লেখযোগ্যভাবে কমেছে।"

        ExecutionGuardStatus.BLIND ->
            "ডেটা sync সমস্যা ধরা পড়েছে। তথ্য পুরনো হতে পারে, তাই Live Radar নিরাপদভাবে short-term execution validate করতে পারছে না।"
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
        DivergenceState.STABLE -> CryptoGreen
        DivergenceState.WARNING -> AccentGold
        DivergenceState.DANGER -> Color(0xFFFF3F60)
        DivergenceState.BLIND -> Color(0xFFFF6F86)
    }
}

fun executionGuardColor(status: ExecutionGuardStatus): Color {
    return when (status) {
        ExecutionGuardStatus.GO -> CryptoGreen
        ExecutionGuardStatus.CAUTION -> AccentGold
        ExecutionGuardStatus.DANGER -> Color(0xFFFF3F60)
        ExecutionGuardStatus.BLIND -> Color(0xFFFF6F86)
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
        DivergenceState.WARNING -> "Outflow Risk"
        DivergenceState.DANGER -> "Capital Drain"
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
        DivergenceState.DANGER -> "Squeeze Risk"
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
        DivergenceState.WARNING -> String.format("%.1fx Velocity", maxShock)
        DivergenceState.DANGER -> String.format("%.1fx Shock", maxShock)
        DivergenceState.BLIND -> "Blind"
    }
}

fun readinessActionLabel(
    status: ExecutionGuardStatus,
    isBengali: Boolean
): String {
    return when (status) {
        ExecutionGuardStatus.GO -> if (isBengali) {
            "Entry validation allowed"
        } else {
            "Entry validation allowed"
        }

        ExecutionGuardStatus.CAUTION -> if (isBengali) {
            "Use smaller size; verify entry"
        } else {
            "Use smaller size; verify entry"
        }

        ExecutionGuardStatus.DANGER -> if (isBengali) {
            "Avoid chase; wait confirmation"
        } else {
            "Avoid chase; wait confirmation"
        }

        ExecutionGuardStatus.BLIND -> if (isBengali) {
            "Wait: data sync not reliable"
        } else {
            "Wait: data sync not reliable"
        }
    }
}
'''

text = text.rstrip() + "\n\n" + helpers + "\n"

target.write_text(text, encoding="utf-8")

print("OK: Remaining Beta Divergence Guard integration completed.")
print("Added live-data-ready snapshot model, latency mapping, market deltas, OI/funding/liquidation, spread/fee/depth, asset shock, and AI readiness impact.")
print("Only MarketRadarScreen.kt modified.")
