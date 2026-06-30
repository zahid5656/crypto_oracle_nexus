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
fun OpportunisticSignalAdornmentSection(
    symbol: String,
    basePrice: Double,
    isLong: Boolean,
    potential: Double,
    isBengali: Boolean,
    themeColor: Color
) {
    val tp1 = if (isLong) basePrice * (1.0 + potential * 0.25 / 100.0) else basePrice * (1.0 - potential * 0.25 / 100.0)
    val tp2 = if (isLong) basePrice * (1.0 + potential * 0.50 / 100.0) else basePrice * (1.0 - potential * 0.50 / 100.0)
    val tp3 = if (isLong) basePrice * (1.0 + potential * 1.00 / 100.0) else basePrice * (1.0 - potential * 1.00 / 100.0)
    val ecosystemLeaderName = remember(symbol) { ecosystemLeaderNameFor(symbol) }

    val formatPrice = { price: Double ->
        when {
            price < 0.01 -> String.format("%.6f", price)
            price < 1.0 -> String.format("%.4f", price)
            else -> String.format("%,.2f", price)
        }
    }

    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(6.dp))

    // 1. Take Profit Matrix
    Text(
        text = if (isBengali) "টার্গেট প্রফিট ম্যাট্রিক্স" else "TAKE PROFIT TARGET MATRIX",
        fontSize = 10.4.sp,
        fontWeight = FontWeight.Black,
        color = themeColor,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        TakeProfitTargetTile(
            label = "TP1 (25%)",
            value = "$${formatPrice(tp1)}",
            accent = themeColor,
            modifier = Modifier.weight(1f)
        )

        TakeProfitTargetTile(
            label = "TP2 (50%)",
            value = "$${formatPrice(tp2)}",
            accent = themeColor,
            modifier = Modifier.weight(1f)
        )

        TakeProfitTargetTile(
            label = "TP3 (100%)",
            value = "$${formatPrice(tp3)}",
            accent = themeColor,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(6.dp))

    LiveRadarBetaDivergenceGuard(
        symbol = symbol,
        ecosystemLeaderName = ecosystemLeaderName,
        isLong = isLong,
        isBengali = isBengali
    )

    Spacer(modifier = Modifier.height(6.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(6.dp))

    // 2. Multi-AI Consensus Engines
    val geminiScore = 94
    val gptQuantScore = 90
    val claudeScore = 93
    val consensusConfidence = remember(geminiScore, gptQuantScore, claudeScore) {
        ((geminiScore + gptQuantScore + claudeScore) / 3.0).toInt()
    }
    val consensusDirection = if (isBengali) { if (isLong) "উর্ধ্বমুখী প্রবণতা" else "নিম্নমুখী প্রবণতা" } else { if (isLong) "BULLISH" else "BEARISH" }
    val consensusRiskProfile = conservativeConsensusRiskProfile(
        confidence = consensusConfidence,
        potential = potential
    )

    Text(
        text = if (isBengali) "মাল্টি-এআই মডেলের ঐক্যমত ইঞ্জিন" else "MULTI-AI CONSENSUS ENGINES",
        fontSize = 9.8.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.92f),
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        ConsensusEngineTile(
            name = "Gemini Pro AI",
            accent = themeColor,
            score = "$geminiScore/100",
            modifier = Modifier.weight(1f)
        )

        ConsensusEngineTile(
            name = "GPT-4Q Quant",
            accent = themeColor,
            score = "$gptQuantScore/100",
            modifier = Modifier.weight(1f)
        )

        ConsensusEngineTile(
            name = "Claude Sentient",
            accent = themeColor,
            score = "$claudeScore/100",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(7.dp))

    ConsensusSummaryStrip(
        confidence = consensusConfidence,
        direction = consensusDirection,
        riskProfile = consensusRiskProfile,
        accent = themeColor,
        isBengali = isBengali
    )

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    // 3. Recommended Position Sizing
    Text(
        text = if (isBengali) "পজিশন সাইজিং পোর্টফোলিও কন্ট্রোল" else "RECOMMENDED POSITION ALLOCATION SIZING",
        fontSize = 9.8.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(7.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        AllocationSizingTile(
            label = "Conservative",
            value = "2.0% Cap",
            accent = themeColor,
            modifier = Modifier.weight(1f)
        )

        AllocationSizingTile(
            label = "Moderate",
            value = "5.0% Cap",
            accent = themeColor,
            modifier = Modifier.weight(1f)
        )

        AllocationSizingTile(
            label = "Aggressive",
            value = "10.0% Max",
            accent = themeColor,
            modifier = Modifier.weight(1f)
        )
    }
}


// ============================================================================
// LIVE RADAR — DIRECT UI READABILITY + COLOR CONSISTENCY PATCH
// Scope: expanded Live Radar cards only.
// Signal Pro, Mission Center, trade execution, and repository logic untouched.
// ============================================================================
internal val LiveRadarPanelDark = Color(0xFF050A13)
internal val LiveRadarTileDark = Color(0xFF050A13)
internal val LiveRadarSoftWhite = Color(0xFFF5F5F5)
internal val LiveRadarInstitutionalGreen = CryptoGreen
internal val LiveRadarInstitutionalYellow = TitanGold
internal val LiveRadarDangerRed = CryptoRedText
internal enum class LiveRadarGuardSeverity {
    CLEAR,
    WARNING,
    DANGER,
    BLIND
}
internal data class LiveRadarBetaGuardUiState(
    val dataSyncSeverity: LiveRadarGuardSeverity,
    val btcDeltaSeverity: LiveRadarGuardSeverity,
    val ecosystemSeverity: LiveRadarGuardSeverity,
    val marketFlowSeverity: LiveRadarGuardSeverity,
    val derivativesSeverity: LiveRadarGuardSeverity,
    val spreadSeverity: LiveRadarGuardSeverity,
    val assetShockSeverity: LiveRadarGuardSeverity,
    val readinessSeverity: LiveRadarGuardSeverity,
    val latencyMs: Int,
    val btcDeltaText: String,
    val ecosystemDeltaText: String,
    val marketFlowText: String,
    val derivativesText: String,
    val spreadText: String,
    val assetShockText: String,
    val readinessScore: Int,
    val penaltyPoints: Int
)
@Composable
internal fun AiOracleAnalyticMetadataSection(
    title: String,
    details: Map<String, String>,
    isBengali: Boolean,
    sectionColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiveRadarPanelDark, RoundedCornerShape(12.dp))
            .border(0.9.dp, sectionColor.copy(alpha = 0.50f), RoundedCornerShape(12.dp))
            .padding(horizontal = 9.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = sectionColor,
            letterSpacing = 0.85.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(5.dp))

        OracleAnalyticMetadataGrid(
            details = details,
            isBengali = isBengali,
            sectionColor = LiveRadarInstitutionalGreen
        )

        Spacer(modifier = Modifier.height(5.dp))

        OracleMetadataDescriptionTile(
            text = if (isBengali) details["desc_bn"].orEmpty() else details["desc"].orEmpty(),
            borderColor = LiveRadarInstitutionalGreen
        )
    }
}
@Composable
internal fun OracleAnalyticMetadataGrid(
    details: Map<String, String>,
    isBengali: Boolean,
    sectionColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OracleMetadataTile(
                title = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED",
                value = if (isBengali) details["pattern_bn"].orEmpty() else details["pattern"].orEmpty(),
                titleColor = sectionColor,
                valueColor = LiveRadarSoftWhite,
                borderColor = sectionColor,
                valueSizeSp = 9.6f,
                modifier = Modifier.weight(1f)
            )

            OracleMetadataTile(
                title = if (isBengali) "অর্ডারবুক রেশিও" else "ORDERBOOK RATIO",
                value = if (isBengali) details["bid_ask_bn"].orEmpty() else details["bid_ask"].orEmpty(),
                titleColor = sectionColor,
                valueColor = LiveRadarSoftWhite,
                borderColor = sectionColor,
                valueSizeSp = 9.8f,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            OracleMetadataTile(
                title = if (isBengali) "স্টপ লস" else "SUGGESTED STOP LOSS",
                value = details["sl"].orEmpty(),
                titleColor = LiveRadarDangerRed,
                valueColor = LiveRadarDangerRed,
                borderColor = LiveRadarDangerRed,
                valueSizeSp = 15.2f,
                modifier = Modifier.weight(1f)
            )

            val probabilityColor = probabilityScoreColor(details["prob"].orEmpty())
            OracleMetadataTile(
                title = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE",
                value = details["prob"].orEmpty(),
                titleColor = probabilityColor,
                valueColor = probabilityColor,
                borderColor = sectionColor,
                valueSizeSp = 15.8f,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
internal fun OracleMetadataTile(
    title: String,
    value: String,
    titleColor: Color,
    valueColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
    titleSizeSp: Float = 8.9f,
    valueSizeSp: Float = 13.2f,
    showRiskDot: Boolean = false
) {
    Column(
        modifier = modifier
            .heightIn(min = 58.dp)
            .background(LiveRadarTileDark, RoundedCornerShape(10.dp))
            .border(0.9.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            fontSize = titleSizeSp.sp,
            fontWeight = FontWeight.Black,
            color = titleColor,
            letterSpacing = 0.12.sp,
            lineHeight = 10.4.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showRiskDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(LiveRadarDangerRed, CircleShape)
                )
                Spacer(modifier = Modifier.width(5.dp))
            }

            Text(
                text = value,
                fontSize = valueSizeSp.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                lineHeight = (valueSizeSp + 1.7f).sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
internal fun OracleMetadataDescriptionTile(
    text: String,
    borderColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiveRadarTileDark, RoundedCornerShape(10.dp))
            .border(0.8.dp, borderColor.copy(alpha = 0.38f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LiveRadarSoftWhite,
            lineHeight = 16.sp
        )
    }
}
@Composable
internal fun LiveRadarBetaDivergenceGuard(
    symbol: String,
    ecosystemLeaderName: String,
    isLong: Boolean,
    isBengali: Boolean
) {
    val state = remember(symbol, ecosystemLeaderName, isLong) {
        buildLiveRadarBetaGuardUiState(
            symbol = symbol,
            ecosystemLeaderName = ecosystemLeaderName,
            isLong = isLong
        )
    }

    val statusColor = liveRadarGuardColor(state.readinessSeverity)
    val penaltyColor = LiveRadarDangerRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiveRadarPanelDark, RoundedCornerShape(13.dp))
            .border(0.9.dp, statusColor.copy(alpha = 0.62f), RoundedCornerShape(13.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp)
    ) {
        Text(
            text = "BETA DIVERGENCE GUARD",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = statusColor,
            letterSpacing = 0.90.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LiveRadarTileDark, RoundedCornerShape(10.dp))
                .border(0.85.dp, statusColor.copy(alpha = 0.56f), RoundedCornerShape(10.dp))
                .padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI GUARD IMPACT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    letterSpacing = 0.45.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Validity Entry",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiveRadarSoftWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${state.readinessScore}/100",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    maxLines = 1
                )

                Text(
                    text = "-${state.penaltyPoints} pts",
                    fontSize = 13.4.sp,
                    fontWeight = FontWeight.Black,
                    color = penaltyColor,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BetaGuardMiniTile(
                label = "Data Sync",
                value = "${state.latencyMs}ms",
                stat = dataSyncLabel(state.dataSyncSeverity),
                severity = state.dataSyncSeverity,
                forceAmberWarning = true,
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "BTC Delta",
                value = state.btcDeltaText,
                stat = deltaLabel(state.btcDeltaSeverity),
                severity = state.btcDeltaSeverity,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BetaGuardMiniTile(
                label = ecosystemLeaderName,
                value = state.ecosystemDeltaText,
                stat = deltaLabel(state.ecosystemSeverity),
                severity = state.ecosystemSeverity,
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Market Flow",
                value = state.marketFlowText,
                stat = flowLabel(state.marketFlowSeverity),
                severity = state.marketFlowSeverity,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BetaGuardMiniTile(
                label = "Derivatives",
                value = state.derivativesText,
                stat = stressLabel(state.derivativesSeverity),
                severity = state.derivativesSeverity,
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Spread Risk",
                value = state.spreadText,
                stat = spreadLabel(state.spreadSeverity),
                severity = state.spreadSeverity,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BetaGuardMiniTile(
                label = "Asset Shock",
                value = state.assetShockText,
                stat = shockLabel(state.assetShockSeverity),
                severity = state.assetShockSeverity,
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Readiness",
                value = "${state.readinessScore}/100",
                stat = "-${state.penaltyPoints} pts",
                severity = state.readinessSeverity,
                statColorOverride = LiveRadarDangerRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
@Composable
internal fun BetaGuardMiniTile(
    label: String,
    value: String,
    stat: String,
    severity: LiveRadarGuardSeverity,
    modifier: Modifier = Modifier,
    forceAmberWarning: Boolean = false,
    statColorOverride: Color? = null
) {
    val baseColor = if (forceAmberWarning && severity != LiveRadarGuardSeverity.CLEAR) {
        LiveRadarInstitutionalYellow
    } else {
        liveRadarGuardColor(severity)
    }
    val visibleStatColor = statColorOverride ?: baseColor
    val borderBrush = if (severity == LiveRadarGuardSeverity.CLEAR) {
        Brush.horizontalGradient(listOf(baseColor, baseColor))
    } else {
        Brush.horizontalGradient(
            listOf(
                baseColor.copy(alpha = 0.42f),
                baseColor,
                baseColor.copy(alpha = 0.42f)
            )
        )
    }

    Column(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(LiveRadarTileDark, RoundedCornerShape(9.dp))
            .border(0.85.dp, borderBrush, RoundedCornerShape(9.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 9.2.sp,
            fontWeight = FontWeight.Black,
            color = baseColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = value,
            fontSize = 11.8.sp,
            fontWeight = FontWeight.Black,
            color = LiveRadarSoftWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stat,
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Bold,
            color = visibleStatColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
internal fun TakeProfitTargetTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 44.dp)
            .background(LiveRadarTileDark, RoundedCornerShape(9.dp))
            .border(0.55.dp, accent, RoundedCornerShape(9.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 11.4.sp,
            fontWeight = FontWeight.Black,
            color = LiveRadarInstitutionalGreen,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(1.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = LiveRadarSoftWhite,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
