package com.example.feature.live_radar

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.radar.RadarAlert
import com.example.feature.signal_pro.StartTradeFlow
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import kotlin.random.Random

// Extracted from LiveRadarScreen.kt to keep the public screen entry point compact.
@Composable
internal fun ConsensusEngineTile(
    name: String,
    score: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 40.dp)
            .background(LiveRadarTileDark, RoundedCornerShape(9.dp))
            .border(0.55.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(9.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = name,
            fontSize = 9.4.sp,
            fontWeight = FontWeight.Black,
            color = LiveRadarSoftWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = score,
            fontSize = 13.2.sp,
            fontWeight = FontWeight.Black,
            color = LiveRadarInstitutionalGreen,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
@Composable
internal fun ConsensusSummaryStrip(
    confidence: Int,
    direction: String,
    riskProfile: String,
    accent: Color,
    modifier: Modifier = Modifier,
    isBengali: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(LiveRadarTileDark, RoundedCornerShape(10.dp))
            .border(0.55.dp, accent, RoundedCornerShape(10.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConsensusSummaryMetric(
            label = if (isBengali) "কনসেনসাস" else "Consensus Confidence",
            value = "$confidence%",
            valueColor = probabilityScoreColor("$confidence"),
            modifier = Modifier.weight(1f)
        )
        ConsensusSummaryMetric(
            label = if (isBengali) "দিক" else "Direction",
            value = direction,
            valueColor = directionColor(direction),
            modifier = Modifier.weight(1f)
        )
        ConsensusSummaryMetric(
            label = if (isBengali) "কনসেনসাস বায়াস" else "Consensus Bias",
            value = riskProfile,
            valueColor = riskProfileColor(riskProfile),
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
internal fun ConsensusSummaryMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = value,
            fontSize = 12.2.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
internal fun AllocationSizingTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 39.dp)
            .background(LiveRadarTileDark, RoundedCornerShape(9.dp))
            .border(0.55.dp, allocationProfileColor(label).copy(alpha = 0.56f), RoundedCornerShape(9.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(1.dp))

        val allocationParts = value.split(" ", limit = 2)
        Text(
            text = allocationParts.getOrNull(0) ?: value,
            fontSize = 11.8.sp,
            fontWeight = FontWeight.Black,
            color = allocationProfileColor(label),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = allocationParts.getOrNull(1).orEmpty(),
            fontSize = 8.2.sp,
            fontWeight = FontWeight.Bold,
            color = allocationProfileColor(label).copy(alpha = 0.86f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
internal fun allocationProfileColor(label: String): Color = titanAllocationProfileColor(label)

internal fun probabilityScoreColor(rawValue: String): Color {
    val score = rawValue.filter { it.isDigit() }.toIntOrNull() ?: return LiveRadarSoftWhite
    return titanPositiveScoreColor(score)
}
internal fun conservativeConsensusRiskProfile(
    confidence: Int,
    potential: Double
): String {
    return when {
        confidence >= 88 && potential <= 12.0 -> "AGGRESSIVE"
        confidence >= 76 && potential <= 18.0 -> "MODERATE"
        else -> "CONSERVATIVE"
    }
}
internal fun directionColor(direction: String): Color {
    val normalized = direction.uppercase()
    return when {
        normalized.contains("BULLISH") || normalized.contains("LONG") || normalized.contains("উর্ধ্বমুখী") -> LiveRadarInstitutionalGreen
        normalized.contains("BEARISH") || normalized.contains("SHORT") || normalized.contains("নিম্নমুখী") -> LiveRadarDangerRed
        else -> LiveRadarInstitutionalYellow
    }
}
internal fun riskProfileColor(riskProfile: String): Color = titanRiskProfileColor(riskProfile)
internal fun buildLiveRadarBetaGuardUiState(
    symbol: String,
    ecosystemLeaderName: String,
    isLong: Boolean
): LiveRadarBetaGuardUiState {
    val seed = safeRadarHash("$symbol|$ecosystemLeaderName|$isLong")
    val latencyMs = 110 + (seed % 640)
    val signedBias = ((seed % 180) - 90) / 100.0
    val btcDelta = if (isLong) signedBias else -signedBias
    val ecosystemDelta = (((seed / 7) % 160) - 80) / 100.0
    val spreadBps = 4.0 + ((seed / 11) % 32)
    val assetShock = 0.9 + ((seed / 13) % 26) / 10.0
    val derivativeStress = 6 + ((seed / 17) % 36)
    val marketFlow = ((seed / 19) % 140) - 70

    val dataSyncSeverity = when {
        latencyMs >= 700 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }

    val btcSeverity = directionalSeverity(delta = btcDelta, isLong = isLong)
    val ecosystemSeverity = directionalSeverity(delta = ecosystemDelta, isLong = isLong)

    val marketSeverity = when {
        marketFlow <= -48 -> LiveRadarGuardSeverity.DANGER
        marketFlow <= -24 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }

    val derivativesSeverity = when {
        derivativeStress >= 34 -> LiveRadarGuardSeverity.DANGER
        derivativeStress >= 22 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }

    val spreadSeverity = when {
        spreadBps >= 30.0 -> LiveRadarGuardSeverity.DANGER
        spreadBps >= 20.0 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }

    val shockSeverity = when {
        assetShock >= 3.0 -> LiveRadarGuardSeverity.DANGER
        assetShock >= 2.2 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }

    val penalty = listOf(
        dataSyncSeverity,
        btcSeverity,
        ecosystemSeverity,
        marketSeverity,
        derivativesSeverity,
        spreadSeverity,
        shockSeverity
    ).sumOf { severity ->
        when (severity) {
            LiveRadarGuardSeverity.CLEAR -> 0
            LiveRadarGuardSeverity.WARNING -> 4
            LiveRadarGuardSeverity.DANGER -> 10
            LiveRadarGuardSeverity.BLIND -> 22
        }
    }.coerceIn(0, 52)

    val readiness = (100 - penalty).coerceIn(0, 100)

    val readinessSeverity = when {
        readiness < 72 -> LiveRadarGuardSeverity.DANGER
        readiness < 86 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }

    return LiveRadarBetaGuardUiState(
        dataSyncSeverity = dataSyncSeverity,
        btcDeltaSeverity = btcSeverity,
        ecosystemSeverity = ecosystemSeverity,
        marketFlowSeverity = marketSeverity,
        derivativesSeverity = derivativesSeverity,
        spreadSeverity = spreadSeverity,
        assetShockSeverity = shockSeverity,
        readinessSeverity = readinessSeverity,
        latencyMs = latencyMs,
        btcDeltaText = String.format("%.2f%%", btcDelta),
        ecosystemDeltaText = String.format("%.2f%%", ecosystemDelta),
        marketFlowText = if (marketFlow < 0) "Outflow" else "Neutral",
        derivativesText = "${derivativeStress}%",
        spreadText = String.format("%.1fbps", spreadBps),
        assetShockText = String.format("%.1fx", assetShock),
        readinessScore = readiness,
        penaltyPoints = penalty
    )
}
internal fun directionalSeverity(
    delta: Double,
    isLong: Boolean
): LiveRadarGuardSeverity {
    val adverseMove = if (isLong) -delta else delta

    return when {
        adverseMove >= 0.72 -> LiveRadarGuardSeverity.DANGER
        adverseMove >= 0.38 -> LiveRadarGuardSeverity.WARNING
        else -> LiveRadarGuardSeverity.CLEAR
    }
}
internal fun ecosystemLeaderNameFor(symbol: String): String {
    val upper = symbol.uppercase()

    return when {
        upper.contains("ETH") || upper.contains("ARB") || upper.contains("OP") ||
            upper.contains("MATIC") || upper.contains("LINK") || upper.contains("UNI") ||
            upper.contains("AAVE") || upper.contains("SHIB") || upper.contains("PEPE") -> "ETH Leader"

        upper.contains("SOL") || upper.contains("JUP") || upper.contains("PYTH") ||
            upper.contains("WIF") || upper.contains("BONK") || upper.contains("RAY") ||
            upper.contains("RNDR") || upper.contains("RENDER") -> "SOL Leader"

        upper.contains("BNB") || upper.contains("CAKE") || upper.contains("TWT") ||
            upper.contains("FLOKI") -> "BNB Leader"

        upper.contains("AVAX") || upper.contains("JOE") -> "AVAX Leader"
        upper.contains("ADA") -> "ADA Leader"
        upper.contains("DOGE") -> "DOGE Leader"
        upper.contains("NEAR") -> "NEAR Leader"
        else -> "BTC Leader"
    }
}
internal fun liveRadarGuardColor(severity: LiveRadarGuardSeverity): Color {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> LiveRadarInstitutionalGreen
        LiveRadarGuardSeverity.WARNING -> LiveRadarInstitutionalYellow
        LiveRadarGuardSeverity.DANGER -> LiveRadarDangerRed
        LiveRadarGuardSeverity.BLIND -> LiveRadarDangerRed
    }
}
internal fun dataSyncLabel(severity: LiveRadarGuardSeverity): String {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> "Fresh"
        LiveRadarGuardSeverity.WARNING -> "Delayed"
        LiveRadarGuardSeverity.DANGER -> "Drift"
        LiveRadarGuardSeverity.BLIND -> "Blind"
    }
}
internal fun deltaLabel(severity: LiveRadarGuardSeverity): String {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> "Aligned"
        LiveRadarGuardSeverity.WARNING -> "Diverge"
        LiveRadarGuardSeverity.DANGER -> "Conflict"
        LiveRadarGuardSeverity.BLIND -> "Blind"
    }
}
internal fun flowLabel(severity: LiveRadarGuardSeverity): String {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> "Stable"
        LiveRadarGuardSeverity.WARNING -> "Caution"
        LiveRadarGuardSeverity.DANGER -> "Drain"
        LiveRadarGuardSeverity.BLIND -> "Blind"
    }
}
internal fun stressLabel(severity: LiveRadarGuardSeverity): String {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> "Normal"
        LiveRadarGuardSeverity.WARNING -> "Crowded"
        LiveRadarGuardSeverity.DANGER -> "Stress"
        LiveRadarGuardSeverity.BLIND -> "Blind"
    }
}
internal fun spreadLabel(severity: LiveRadarGuardSeverity): String {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> "Clean"
        LiveRadarGuardSeverity.WARNING -> "Wide"
        LiveRadarGuardSeverity.DANGER -> "Poor"
        LiveRadarGuardSeverity.BLIND -> "Blind"
    }
}
internal fun shockLabel(severity: LiveRadarGuardSeverity): String {
    return when (severity) {
        LiveRadarGuardSeverity.CLEAR -> "Clear"
        LiveRadarGuardSeverity.WARNING -> "Fast"
        LiveRadarGuardSeverity.DANGER -> "Shock"
        LiveRadarGuardSeverity.BLIND -> "Blind"
    }
}
internal fun safeRadarHash(value: String): Int {
    val hash = value.hashCode()
    return if (hash == Int.MIN_VALUE) 0 else if (hash < 0) -hash else hash
}
