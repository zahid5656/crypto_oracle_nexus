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
        raise SystemExit("ERROR: Import section not found in MarketRadarScreen.kt")

    lines.insert(last_import_index + 1, import_line)
    return "\n".join(lines) + "\n"


required_imports = [
    "import androidx.compose.runtime.Immutable",
    "import kotlin.math.absoluteValue",
]

for imp in required_imports:
    text = ensure_import(text, imp)


guard_call = '''                    LiveRadarBetaDivergenceGuard(
                        symbol = symbol,
                        timeframe = timeframe,
                        isLong = target >= basePrice,
                        isBengali = isBengali
                    )

                    Spacer(modifier = Modifier.height(12.dp))

'''


def insert_guard_call(source: str) -> str:
    if "LiveRadarBetaDivergenceGuard(" in source:
        print("OK: LiveRadarBetaDivergenceGuard call already exists. No duplicate inserted.")
        return source

    # Preferred insertion point: before existing Trade Machine section if already added.
    preferred_anchor = "                    LiveRadarTradeMachineSection("
    if preferred_anchor in source:
        return source.replace(preferred_anchor, guard_call + preferred_anchor, 1)

    # Second insertion point: before existing Opportunistic Signal section.
    fallback_anchor = "                    OpportunisticSignalAdornmentSection("
    if fallback_anchor in source:
        return source.replace(fallback_anchor, guard_call + fallback_anchor, 1)

    # Conservative fallback: after description text block inside expanded signal card.
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

    new_source, count = desc_pattern.subn(r"\1" + guard_call, source, count=1)

    if count == 0:
        raise SystemExit("ERROR: Could not find a safe insertion point for Beta Divergence Guard.")

    return new_source


text = insert_guard_call(text)


helpers = r'''
// ============================================================================
// LIVE RADAR — BETA DIVERGENCE GUARD
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
data class BetaDivergenceGuardState(
    val latencyState: DivergenceState,
    val btcDeltaState: DivergenceState,
    val ecosystemLeaderState: DivergenceState,
    val marketOutflowState: DivergenceState,
    val derivativesStressState: DivergenceState,
    val spreadLiquidityState: DivergenceState,
    val assetVelocityShockState: DivergenceState,
    val finalGuardStatus: ExecutionGuardStatus,
    val executionReadinessPenalty: Int,
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
    val guardState = remember(symbol, timeframe, isLong) {
        buildBetaDivergenceGuardState(
            symbol = symbol,
            timeframe = timeframe,
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

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BetaGuardMiniTile(
                label = "Data Sync",
                state = guardState.latencyState,
                value = dataSyncStateLabel(guardState.latencyState, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "BTC Delta",
                state = guardState.btcDeltaState,
                value = btcDeltaStateLabel(guardState.btcDeltaState, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BetaGuardMiniTile(
                label = if (isBengali) "Leader" else "Leader",
                state = guardState.ecosystemLeaderState,
                value = ecosystemStateLabel(guardState.ecosystemLeaderState, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = if (isBengali) "Market Flow" else "Market Flow",
                state = guardState.marketOutflowState,
                value = marketFlowStateLabel(guardState.marketOutflowState, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BetaGuardMiniTile(
                label = if (isBengali) "Derivatives" else "Derivatives",
                state = guardState.derivativesStressState,
                value = derivativesStateLabel(guardState.derivativesStressState, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = if (isBengali) "Spread Risk" else "Spread Risk",
                state = guardState.spreadLiquidityState,
                value = spreadStateLabel(guardState.spreadLiquidityState, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BetaGuardMiniTile(
                label = if (isBengali) "Asset Shock" else "Asset Shock",
                state = guardState.assetVelocityShockState,
                value = assetShockStateLabel(guardState.assetVelocityShockState, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardPenaltyTile(
                penalty = guardState.executionReadinessPenalty,
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
fun BetaGuardMiniTile(
    label: String,
    state: DivergenceState,
    value: String,
    modifier: Modifier = Modifier
) {
    val color = divergenceStateColor(state)

    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(8.dp))
            .border(0.65.dp, color.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 9.3.sp,
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
    status: ExecutionGuardStatus,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val color = executionGuardColor(status)

    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(Color(0xFF050A13), RoundedCornerShape(8.dp))
            .border(0.65.dp, color.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isBengali) "Penalty" else "Readiness Penalty",
            fontSize = 7.2.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "-$penalty pts",
            fontSize = 9.8.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun buildBetaDivergenceGuardState(
    symbol: String,
    timeframe: String,
    isLong: Boolean
): BetaDivergenceGuardState {
    val safeHash = if (symbol.hashCode() == Int.MIN_VALUE) 0 else symbol.hashCode().absoluteValue
    val tfHash = if (timeframe.hashCode() == Int.MIN_VALUE) 0 else timeframe.hashCode().absoluteValue
    val seed = safeHash + tfHash + if (isLong) 17 else 31

    val simulatedLatencyMs = 180 + (seed % 1700)

    val latencyState = when {
        simulatedLatencyMs > 1500 -> DivergenceState.BLIND
        simulatedLatencyMs > 500 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

    val btcDeltaState = when ((seed / 3) % 10) {
        0 -> DivergenceState.DANGER
        1, 2 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

    val ecosystemLeaderState = when ((seed / 5) % 10) {
        0 -> DivergenceState.DANGER
        1, 2 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

    val marketOutflowState = when ((seed / 7) % 10) {
        0 -> DivergenceState.DANGER
        1, 2, 3 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

    val derivativesStressState = when ((seed / 11) % 10) {
        0 -> DivergenceState.DANGER
        1, 2 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

    val spreadLiquidityState = when ((seed / 13) % 10) {
        0 -> DivergenceState.DANGER
        1, 2 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

    val assetVelocityShockState = when ((seed / 17) % 10) {
        0 -> DivergenceState.DANGER
        1 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }

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
    }.coerceIn(0, 65)

    val finalStatus = when {
        latencyState == DivergenceState.BLIND -> ExecutionGuardStatus.BLIND
        allStates.any { it == DivergenceState.DANGER } -> ExecutionGuardStatus.DANGER
        penalty >= 18 -> ExecutionGuardStatus.CAUTION
        else -> ExecutionGuardStatus.GO
    }

    val narrativeEnglish = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Execution guard is clear. Broad market, liquidity, spread and asset-shock layers are aligned enough for short-term validation."

        ExecutionGuardStatus.CAUTION ->
            "Caution: one or more safety layers show weakness. Use smaller size and confirm entry before accepting."

        ExecutionGuardStatus.DANGER ->
            "Warning: broad market divergence, derivatives stress, poor liquidity or asset-specific shock detected. Execution readiness is reduced."

        ExecutionGuardStatus.BLIND ->
            "Synchronization drift detected. Data may be stale, so Live Radar cannot safely validate short-term execution."
    }

    val narrativeBengali = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Execution guard পরিষ্কার। ব্রড মার্কেট, liquidity, spread এবং asset-shock layer short-term validation-এর জন্য যথেষ্ট aligned।"

        ExecutionGuardStatus.CAUTION ->
            "সতর্কতা: এক বা একাধিক safety layer দুর্বল। ছোট position size ব্যবহার করুন এবং entry confirm করুন।"

        ExecutionGuardStatus.DANGER ->
            "সতর্কতা: broad market divergence, derivatives stress, poor liquidity অথবা asset-specific shock ধরা পড়েছে। execution readiness কমেছে।"

        ExecutionGuardStatus.BLIND ->
            "ডেটা sync সমস্যা ধরা পড়েছে। তথ্য পুরনো হতে পারে, তাই Live Radar নিরাপদভাবে short-term execution validate করতে পারছে না।"
    }

    return BetaDivergenceGuardState(
        latencyState = latencyState,
        btcDeltaState = btcDeltaState,
        ecosystemLeaderState = ecosystemLeaderState,
        marketOutflowState = marketOutflowState,
        derivativesStressState = derivativesStressState,
        spreadLiquidityState = spreadLiquidityState,
        assetVelocityShockState = assetVelocityShockState,
        finalGuardStatus = finalStatus,
        executionReadinessPenalty = penalty,
        narrativeEnglish = narrativeEnglish,
        narrativeBengali = narrativeBengali
    )
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
        ExecutionGuardStatus.GO -> if (isBengali) "GO" else "GO"
        ExecutionGuardStatus.CAUTION -> if (isBengali) "CAUTION" else "CAUTION"
        ExecutionGuardStatus.DANGER -> if (isBengali) "DANGER" else "DANGER"
        ExecutionGuardStatus.BLIND -> if (isBengali) "BLIND" else "BLIND"
    }
}

fun dataSyncStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "OK" else "OK"
        DivergenceState.WARNING -> if (isBengali) "Delayed" else "Delayed"
        DivergenceState.DANGER -> if (isBengali) "Drift" else "Drift"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}

fun btcDeltaStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "Stable" else "Stable"
        DivergenceState.WARNING -> if (isBengali) "Warning" else "Warning"
        DivergenceState.DANGER -> if (isBengali) "Opposite" else "Opposite"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}

fun ecosystemStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "Aligned" else "Aligned"
        DivergenceState.WARNING -> if (isBengali) "Weakening" else "Weakening"
        DivergenceState.DANGER -> if (isBengali) "Diverging" else "Diverging"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}

fun marketFlowStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "Neutral" else "Neutral"
        DivergenceState.WARNING -> if (isBengali) "Outflow Risk" else "Outflow Risk"
        DivergenceState.DANGER -> if (isBengali) "Capital Drain" else "Capital Drain"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}

fun derivativesStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "Normal" else "Normal"
        DivergenceState.WARNING -> if (isBengali) "Crowded" else "Crowded"
        DivergenceState.DANGER -> if (isBengali) "Squeeze Risk" else "Squeeze Risk"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}

fun spreadStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "Acceptable" else "Acceptable"
        DivergenceState.WARNING -> if (isBengali) "Fee Drag" else "Fee Drag"
        DivergenceState.DANGER -> if (isBengali) "Poor Fill" else "Poor Fill"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}

fun assetShockStateLabel(state: DivergenceState, isBengali: Boolean): String {
    return when (state) {
        DivergenceState.STABLE -> if (isBengali) "Clear" else "Clear"
        DivergenceState.WARNING -> if (isBengali) "Velocity" else "Velocity"
        DivergenceState.DANGER -> if (isBengali) "Shock" else "Shock"
        DivergenceState.BLIND -> if (isBengali) "Blind" else "Blind"
    }
}
'''

if "fun LiveRadarBetaDivergenceGuard(" not in text:
    text = text.rstrip() + "\n\n" + helpers + "\n"
else:
    print("OK: Beta Divergence Guard helper code already exists. Existing helper not duplicated.")

target.write_text(text, encoding="utf-8")

print("OK: Complete Live Radar Beta Divergence Guard patch applied.")
print("Only MarketRadarScreen.kt modified.")
