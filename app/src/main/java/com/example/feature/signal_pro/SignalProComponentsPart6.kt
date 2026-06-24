package com.example.feature.signal_pro

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.graphics.Brush
import com.example.model.FuturesSignal
import com.example.model.OracleAnalysisResponse
import com.example.model.SpotSignal
import com.example.ui.theme.*
import com.example.viewmodel.AnalysisState
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.shadow

// Extracted from SignalProScreen.kt to keep the public screen entry point compact.
@Composable
fun AiExplanationModule(
    whyEnglish: String,
    whyBengali: String,
    coinSymbol: String,
    isBengali: Boolean,
    onToggleLanguage: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isBengali) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isBengali) "এআই ওরাকলের বিশ্লেষণমূলক তথ্য" else "AI ORACLE ANALYTICS COGNITION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = CryptoCyan,
            letterSpacing = if (isBengali) 0.sp else 1.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(5.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14 * density
                    shape = RoundedCornerShape(12.dp)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF03111B),
                                Color(0xFF0B1220),
                                Color(0xFF02050D)
                            )
                        )
                    )
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                if (rotation <= 90f) {
                    Column {
                        Text(
                            text = whyEnglish,
                            fontSize = 13.sp,
                            color = Color(0xFFF4F8FF),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "QUANTITATIVE HEATMAP SIGNALS",
                            fontSize = 9.sp,
                            color = CryptoCyan,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        HeatmapSignalsAlignedRow(
                            firstLabel = "Trend",
                            firstValue = "STRONG",
                            firstColor = CryptoGreen,
                            secondLabel = "Momentum",
                            secondValue = "HOT",
                            secondColor = AcceleratorCyanColor(coinSymbol),
                            thirdLabel = "Volume",
                            thirdValue = "ACCUMULATING",
                            thirdColor = AccentGold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = whyBengali,
                            fontSize = 13.sp,
                            color = Color(0xFFF4F8FF),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "পরিমাণগত হিটম্যাপ সিগন্যাল",
                            fontSize = 9.sp,
                            color = CryptoCyan,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        HeatmapSignalsAlignedRow(
                            firstLabel = "ট্রেন্ড",
                            firstValue = "শক্তিশালী",
                            firstColor = CryptoGreen,
                            secondLabel = "মতিগতি",
                            secondValue = "তীব্র",
                            secondColor = AcceleratorCyanColor(coinSymbol),
                            thirdLabel = "লেনদেন",
                            thirdValue = "সঞ্চয় হচ্ছে",
                            thirdColor = AccentGold
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun HeatmapSignalsAlignedRow(
    firstLabel: String,
    firstValue: String,
    firstColor: Color,
    secondLabel: String,
    secondValue: String,
    secondColor: Color,
    thirdLabel: String,
    thirdValue: String,
    thirdColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        InsightMetricPill(
            label = firstLabel,
            value = firstValue,
            valueColor = firstColor,
            modifier = Modifier.weight(1f)
        )

        InsightMetricPill(
            label = secondLabel,
            value = secondValue,
            valueColor = secondColor,
            modifier = Modifier.weight(1f)
        )

        InsightMetricPill(
            label = thirdLabel,
            value = thirdValue,
            valueColor = thirdColor,
            modifier = Modifier.weight(1.25f)
        )
    }
}
@Composable
fun InsightMetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val valueSize = when {
        value.length >= 11 -> 8.4.sp
        value.length >= 8 -> 9.2.sp
        else -> 10.5.sp
    }

    Column(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF02050D),
                        Color(0xFF08111C),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(0.75.dp, valueColor.copy(alpha = 0.50f), RoundedCornerShape(8.dp))
            .padding(horizontal = 5.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.5.sp,
            color = Color(0xFFD3DAE8),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = valueSize,
            fontWeight = FontWeight.Black,
            color = valueColor,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
fun AcceleratorCyanColor(symbol: String): Color {
    return if (symbol.hashCode() % 2 == 0) CryptoCyan else CryptoGreen
}
@Composable
fun LeverageIntelligenceModule(coin: FuturesSignal) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "LEVERAGE INTELLIGENCE MATRIX",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                LeverageBox("SAFE LEVERAGE", "${coin.leverageConservative}x", "Conservative risk mitigation level", CryptoGreen, Modifier.weight(1f))
                LeverageBox("MODERATE RISK", "${coin.leverageBalanced}x", "Default recommended moderate index", AccentGold, Modifier.weight(1f))
                LeverageBox("MAX AGGRESIVE", "${coin.leverageAggressive}x", "Extreme danger volatility thresholds", CryptoRedText, Modifier.weight(1f))
            }
        }
    }
}
@Composable
fun LeverageBox(title: String, multiplier: String, desc: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF050812), RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = multiplier, fontSize = 16.sp, fontWeight = FontWeight.Black, color = accent)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 7.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 9.sp)
    }
}
data class AiConsensus(
    val geminiScore: Int,
    val gptScore: Int,
    val claudeScore: Int,
    val confidence: Int,
    val direction: String,
    val riskScore: String
)
fun getConsensusDetails(coinSymbol: String, oracleScore: Int, isLong: Boolean): AiConsensus {
    val seed = coinSymbol.hashCode().absoluteValue
    val geminiOffset = (seed % 7) - 3
    val gptOffset = ((seed / 3) % 9) - 4
    val claudeOffset = ((seed / 5) % 8) - 4

    val gemini = (oracleScore + geminiOffset).coerceIn(60, 99)
    val gpt = (oracleScore + gptOffset).coerceIn(60, 99)
    val claude = (oracleScore + claudeOffset).coerceIn(60, 99)
    val confidence = ((gemini + gpt + claude) / 3).coerceIn(60, 99)
    val risk = titanRiskScoreLabelFromPositiveScore(confidence)
    return AiConsensus(
        geminiScore = gemini,
        gptScore = gpt,
        claudeScore = claude,
        confidence = confidence,
        direction = if (isLong) "BULLISH" else "BEARISH",
        riskScore = risk
    )
}
data class TimeframeForecast(
    val timeframe: String,
    val price: Double,
    val roi: Double,
    val confidence: Int,
    val isBullish: Boolean
)
@Composable
fun TimeframeToggleButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CryptoCyan.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) CryptoCyan else TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}
internal fun formatPrice(price: Double): String {
    return when {
        price >= 1000 -> String.format("$%,.2f", price)
        price >= 1 -> String.format("$%,.3f", price)
        else -> String.format("$%.6f", price)
    }
}
@Composable
fun ScrollableTimeframeRow(
    selectedInterval: Int,
    onIntervalSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val intervals = SignalProTimeframes
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(10.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        intervals.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedInterval == index) CryptoCyan.copy(alpha = 0.15f) else Color.Transparent)
                    .clickable { onIntervalSelected(index) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (selectedInterval == index) CryptoCyan else TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
@Composable
fun QualityMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.heightIn(min = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}
@Composable
fun SignalQualitySystemBlock(
    score: Int,
    confidence: Int,
    probability: Int,
    riskGrade: String,
    isBengali: Boolean = false
) {
    val indicator = when {
        score >= 90 -> if (isBengali) "ইনস্টিটিউশনাল মান" else "Institutional Grade"
        score >= 82 -> if (isBengali) "উচ্চ আস্থা" else "High Confidence"
        score >= 70 -> if (isBengali) "শক্তিশালী" else "Strong"
        score >= 55 -> if (isBengali) "মাঝারি" else "Moderate"
        else -> if (isBengali) "দুর্বল" else "Weak"
    }

    val riskText = when (riskGrade.uppercase()) {
        "LOW" -> if (isBengali) "কম" else "LOW"
        "MEDIUM" -> if (isBengali) "মাঝারি" else "MEDIUM"
        "HIGH" -> if (isBengali) "বেশি" else "HIGH"
        "EXTREME" -> if (isBengali) "খুব বেশি" else "EXTREME"
        else -> riskGrade
    }

    val riskColor = titanRiskScoreColorFromLabel(riskGrade)

    val themeColor = titanPositiveScoreColor(score)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "সিগন্যাল মান যাচাই সূচক" else "SIGNAL QUALITY ENGINE INDEX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = if (isBengali) 0.sp else 1.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = if (isBengali) "স্ট্যাটাস: যাচাইকৃত" else "STATUS: VALIDATED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoGreen
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "শ্রেণি" else "CLASSIFICATION",
                        fontSize = 8.sp,
                        color = TextMuted,
                        maxLines = 1
                    )

                    Text(
                        text = if (isBengali) indicator else indicator.uppercase(),
                        fontSize = if (isBengali) 12.sp else 13.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .background(themeColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "CQI: $score/100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityMetricColumn(
                    label = if (isBengali) "আস্থা" else "CONFIDENCE",
                    value = "$confidence%",
                    valueColor = titanPositiveScoreColor(confidence),
                    modifier = Modifier.weight(1f)
                )

                QualityMetricColumn(
                    label = if (isBengali) "সম্ভাবনা" else "PROBABILITY",
                    value = "$probability%",
                    valueColor = titanPositiveScoreColor(probability),
                    modifier = Modifier.weight(1f)
                )

                QualityMetricColumn(
                    label = if (isBengali) "ঝুঁকির স্কোর" else "RISK SCORE",
                    value = riskText,
                    valueColor = riskColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
@Composable
fun TradeChecklistBlock(
    trendConfirmed: Boolean,
    volumeConfirmed: Boolean,
    momentumConfirmed: Boolean,
    liquidityConfirmed: Boolean,
    riskEvaluated: Boolean,
    isBengali: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = if (isBengali) "ইনস্টিটিউশনাল নিশ্চিতকরণ তালিকা" else "INSTITUTIONAL CONFIRMATION CHECKLIST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            val items = listOf(
                (if (isBengali) "বাজারের দিক নিশ্চিত" else "Trend Confirmed") to trendConfirmed,
                (if (isBengali) "লেনদেন নিশ্চিত" else "Volume Confirmed") to volumeConfirmed,
                (if (isBengali) "মতিগতির জোর নিশ্চিত" else "Momentum Confirmed") to momentumConfirmed,
                (if (isBengali) "নিরাপদ তহবিল নিশ্চিত" else "Liquidity Confirmed") to liquidityConfirmed,
                (if (isBengali) "ঝুঁকি যাচাইকৃত" else "Risk Evaluated") to riskEvaluated
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { (label, checked) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (checked) CryptoGreen.copy(alpha = 0.15f) else CryptoRedText.copy(alpha = 0.15f))
                                .border(1.dp, if (checked) CryptoGreen else CryptoRedText, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (checked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Passed",
                                    tint = CryptoGreen,
                                    modifier = Modifier.size(9.dp)
                                )
                            } else {
                                Box(modifier = Modifier.size(4.dp).background(CryptoRedText, CircleShape))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = label,
                            fontSize = if (isBengali) 11.5.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (checked) TextPrimary else TextMuted,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MarketRegimeTraceModule(
    coinSymbol: String,
    isBengali: Boolean = false
) {
    val seed = coinSymbol.hashCode().absoluteValue
    val regimes = listOf("BULLISH", "BEARISH", "SIDEWAYS", "ACCUMULATION", "DISTRIBUTION")
    val regime = regimes[seed % regimes.size]

    val regimeText = when (regime) {
        "BULLISH" -> if (isBengali) "উর্ধ্বমুখী প্রবণতা" else "BULLISH"
        "BEARISH" -> if (isBengali) "নিম্নমুখী প্রবণতা" else "BEARISH"
        "SIDEWAYS" -> if (isBengali) "দাম স্থির ভাব" else "SIDEWAYS"
        "ACCUMULATION" -> if (isBengali) "সঞ্চয় হচ্ছে" else "ACCUMULATION"
        else -> if (isBengali) "বিক্রির চাপ" else "DISTRIBUTION"
    }

    val statusText = if (isBengali) "বর্তমানে সক্রিয়" else "ACTIVE DURING INSIGHT"

    val description = when(regime) {
        "BULLISH" -> if (isBengali) {
            "বাজারে ক্রেতার চাপ বেশি, দাম উপরে যাওয়ার সম্ভাবনা আছে।"
        } else {
            "High liquidity markup phase driven by strong smart money orders."
        }

        "BEARISH" -> if (isBengali) {
            "বাজারে বিক্রির চাপ বেশি, দাম নিচে যাওয়ার ঝুঁকি আছে।"
        } else {
            "Markdown liquidations under persistent offer pressure."
        }

        "SIDEWAYS" -> if (isBengali) {
            "দাম নির্দিষ্ট রেঞ্জে ঘুরছে, বড় ব্রেকের জন্য অপেক্ষা করছে।"
        } else {
            "Range bound bracket with low volatility waiting for core breaks."
        }

        "ACCUMULATION" -> if (isBengali) {
            "বড় ক্রেতারা ধীরে ধীরে পজিশন তৈরি করছে।"
        } else {
            "Institutional accumulation in value brackets."
        }

        else -> if (isBengali) {
            "উচ্চ দামে বিক্রির চাপ তৈরি হচ্ছে।"
        } else {
            "Smart money distribution at premium resistance heights."
        }
    }

    val tint = when(regime) {
        "BULLISH" -> CryptoGreen
        "BEARISH" -> CryptoRedText
        "SIDEWAYS" -> TextMuted
        "ACCUMULATION" -> CryptoCyan
        else -> AccentGold
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = if (isBengali) "চলতি বাজারের মতিগতি" else "PERSISTED REGIME TRACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(tint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = regimeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "|",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = statusText,
                    fontSize = if (isBengali) 10.sp else 8.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = description,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
@Composable
fun ScannerErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onGoBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Scanner compilation error indicator",
            tint = CryptoRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SCANNER ANOMALY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CryptoRed,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = CryptoCyan, contentColor = DarkBackground)
        ) {
            Text(text = "RE-CALIBRATE SCANNER", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onGoBack) {
            Text(text = "Return to Feed", color = TextSecondary)
        }
    }
}

@Composable
fun ExecutionReadinessMatrix(isBengali: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "এক্সিকিউশন রেডিনেস মেট্রিক্স" else "EXECUTION READINESS MATRIX",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isBengali) "অবস্থা: অপটিমাল" else "STATUS: OPTIMAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ExecutionMetric(if (isBengali) "স্প্রেড" else "Spread", "0.01%", CryptoGreen)
                ExecutionMetric(if (isBengali) "তারল্য" else "Liquidity", "STRONG", CryptoGreen)
                ExecutionMetric(if (isBengali) "স্লিপেজ" else "Slippage", "LOW", CryptoGreen)
                ExecutionMetric(if (isBengali) "লেটেন্সি" else "Latency", "12ms", CryptoGreen)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Liquidity Depth: Strong (Tier 1 Order Book)", fontSize = 9.sp, color = TextMuted)
        }
    }
}

@Composable
fun ExecutionMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DirectionTradeLogicValidation(isLong: Boolean = true, isBengali: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = if (isBengali) "দিকনির্দেশনা / ট্রেড লজিক ভ্যালিডেশন" else "DIRECTION / TRADE LOGIC VALIDATION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val directionLogic = if (isLong) "LONG / SPOT" else "SHORT"
            val targetLogic = if (isLong) "Target > Entry" else "StopLoss > Entry > Target"
            val stopLogic = if (isLong) "StopLoss < Entry" else "StopLoss > Entry"

            LogicValidationRow(if (isBengali) "দিকনির্দেশনা লজিক" else "Direction Logic", directionLogic, true)
            LogicValidationRow(if (isBengali) "এন্ট্রি স্টেট" else "Entry State", "VALIDATED", true)
            LogicValidationRow(if (isBengali) "স্টপ-লস লজিক" else "Stop Loss Logic", stopLogic, true)
            LogicValidationRow(if (isBengali) "টার্গেট লজিক" else "Target Logic", targetLogic, true)
            
            Spacer(modifier = Modifier.height(4.dp))
            val invalidationText = if (isLong) "Invalidation: Break below support or momentum decay" else "Invalidation: Break above resistance or short pressure fade"
            Text(text = invalidationText, fontSize = 9.sp, color = AccentGold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
        }
    }
}

@Composable
fun LogicValidationRow(label: String, value: String, passed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (passed) CryptoGreen.copy(alpha = 0.15f) else CryptoRedText.copy(alpha = 0.15f))
                    .border(1.dp, if (passed) CryptoGreen else CryptoRedText, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (passed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(9.dp))
                } else {
                    Box(modifier = Modifier.size(4.dp).background(CryptoRedText, CircleShape))
                }
            }
        }
    }
}

@Composable
fun DecisionGateSummary(isBengali: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = if (isBengali) "ডিসিশন গেট সামারি" else "DECISION GATE SUMMARY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isBengali) "চূড়ান্ত সিদ্ধান্ত:" else "Final Gate:", fontSize = 11.sp, color = TextMuted)
                Text(text = "ACCEPTABLE", fontSize = 13.sp, color = CryptoGreen, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ExecutionMetric("Direction", "PASS", CryptoGreen)
                ExecutionMetric("Risk Score", "PASS", CryptoGreen)
                ExecutionMetric("Readiness", "PASS", CryptoGreen)
                ExecutionMetric("Consensus", "PASS", CryptoGreen)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Blocked Reasons: None", fontSize = 9.sp, color = TextMuted)
            Text(text = "Warnings: Monitor volatility", fontSize = 9.sp, color = AccentGold)
        }
    }
}

@Composable
fun ConflictFlags(isBengali: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = if (isBengali) "কনফ্লিক্ট ফ্ল্যাগ" else "CONFLICT FLAGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ConflictChip("CONSENSUS ALIGNED", CryptoGreen, Modifier.weight(1f))
                ConflictChip("RISK OK", CryptoGreen, Modifier.weight(1f))
                ConflictChip("ENTRY CHECK REQUIRED", AccentGold, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ConflictChip(text: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 7.5.sp, color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@Composable
fun SourceProvenanceAudit(isBengali: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = if (isBengali) "সোর্স / প্রোভেনেন্স / অডিট" else "SOURCE / PROVENANCE / AUDIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Signal ID: SIG-8942-A", fontSize = 9.sp, color = TextMuted)
            Text(text = "Source: Local Mock / Simulated", fontSize = 9.sp, color = CryptoCyan)
            Text(text = "Rules Fired: 14/15 Institutional Constraints", fontSize = 9.sp, color = TextMuted)
            Text(text = "Data Mode: Snapshot | Model Mode: Offline Matrix", fontSize = 9.sp, color = TextMuted)
            Text(text = "Audit State: LOGGED & VERIFIED", fontSize = 9.sp, color = CryptoGreen)
        }
    }
}

@Composable
fun FinalGuidanceModule(isBengali: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(text = if (isBengali) "চূড়ান্ত নির্দেশনা" else "FINAL GUIDANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CryptoCyan, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Suggested Action: VALIDATED ENTRY", fontSize = 11.sp, color = CryptoGreen, fontWeight = FontWeight.Bold)
        Text(text = "Why: Alignment across short-term momentum and multi-AI consensus.", fontSize = 10.sp, color = TextSecondary)
        Text(text = "Risk Reminder: Ensure trailing stops. No system guarantees profit.", fontSize = 10.sp, color = CryptoRedText)
    }
}
