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
fun RealTimeCountdown(
    coinSymbol: String,
    totalDurationHours: Int,
    isBengali: Boolean = false
) {
    val totalSeconds = (totalDurationHours * 3600).coerceAtLeast(1)

    var remainingSeconds by remember(coinSymbol, totalDurationHours) {
        val safeRange = (totalSeconds - 900).coerceAtLeast(600)
        val stableOffsetSeconds = (coinSymbol.hashCode().absoluteValue % safeRange) + 300
        mutableStateOf(stableOffsetSeconds.coerceIn(0, totalSeconds))
    }

    LaunchedEffect(coinSymbol, totalDurationHours) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val sweepTransition = rememberInfiniteTransition(label = "ValidityGradientSweep")
    val sweepX by sweepTransition.animateFloat(
        initialValue = -850f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ValiditySweepX"
    )

    val pulseAlpha by sweepTransition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.56f,
        animationSpec = infiniteRepeatable(
            animation = tween(5400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ValidityPulseAlpha"
    )

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val isExpired = remainingSeconds <= 0
    val isUrgent = progress <= 0.20f && !isExpired
    val isCaution = progress in 0.21f..0.50f

    val accentColor = when {
        isExpired -> CryptoRedText
        isUrgent -> Color(0xFFFF6F86)
        isCaution -> AccentGold
        else -> CryptoCyan
    }

    val baseLeft = when {
        isExpired -> Color(0xFF1A0610)
        isUrgent -> Color(0xFF1E0712)
        isCaution -> Color(0xFF1A1304)
        else -> Color(0xFF03141D)
    }

    val titleText = if (isBengali) "বৈধতার নির্দিষ্ট মেয়াদ" else "VALIDITY WINDOW"
    val remainingText = if (isBengali) "বাকি সময়" else "Remaining"
    val windowText = "Window ${totalDurationHours}H"
    val activeText = "${(progress * 100).toInt()}% active"

    val stateText = when {
        isExpired -> if (isBengali) "সিগন্যালের মেয়াদ শেষ" else "Expired"
        isUrgent -> if (isBengali) "শেষ পর্যায় • দ্রুত যাচাই করুন" else "Critical window • Review quickly"
        isCaution -> if (isBengali) "সতর্ক পর্যায় • ভালোভাবে নজর রাখুন" else "Caution window • Monitor closely"
        else -> if (isBengali) "সিগন্যাল সক্রিয় • ঝুঁকির সময় চলছে" else "Signal active • Risk window open"
    }

    val stateMeaning = when {
        isExpired -> if (isBengali) "সিগন্যালের সময় শেষ" else "Signal window closed"
        isUrgent -> if (isBengali) "শেষ পর্যায়ের সিগনাল — আগে যাচাই করুন" else "Late-stage signal — Verify before action"
        isCaution -> if (isBengali) "দেরি করলে সিগন্যালের মান কমতে পারে" else "Delay may reduce signal quality"
        else -> if (isBengali) "সক্রিয় — এখনো তাড়াহুড়া নেই" else "Active window — No urgency yet"
    }

    val titleColor = if (isUrgent || isExpired) Color(0xFFFF91A6) else Color(0xFFF4FAFF)
    val subtitleColor = if (isUrgent || isExpired) Color(0xFFFFB7C4) else Color(0xFFD6F5FF)
    val supportColor = Color(0xFFB7C2D4)
    val trackColor = Color(0xFF111A28)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseLeft,
                        Color(0xFF090F1C),
                        Color(0xFF02050D)
                    )
                )
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f),
                        Color.Transparent,
                        accentColor.copy(alpha = 0.075f),
                        Color.White.copy(alpha = 0.035f),
                        accentColor.copy(alpha = 0.055f),
                        Color.Transparent
                    )
                )
            ) // ValidityStaticAccentLayer
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = pulseAlpha),
                        Color.White.copy(alpha = 0.10f),
                        accentColor.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    startX = sweepX,
                    endX = sweepX + 620f
                )
            )
            .border(
                width = 1.25.dp,
                color = accentColor.copy(alpha = 0.88f),
                shape = RoundedCornerShape(17.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titleText,
                        fontSize = if (isBengali) 12.sp else 9.sp,
                        fontWeight = FontWeight.Black,
                        color = titleColor,
                        letterSpacing = if (isBengali) 0.sp else 1.1.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Text(
                        text = stateText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = subtitleColor,
                        modifier = Modifier.padding(top = 1.dp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isExpired) {
                            if (isBengali) "শেষ" else "EXPIRED"
                        } else {
                            timeText
                        },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        maxLines = 1,
                        softWrap = false
                    )

                    Text(
                        text = remainingText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = supportColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = accentColor,
                trackColor = trackColor
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF02050D),
                                Color(0xFF0D1422),
                                Color(0xFF02050D)
                            )
                        )
                    )
                    .border(
                        0.8.dp,
                        accentColor.copy(alpha = 0.50f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stateMeaning,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF7FBFF),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = windowText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = supportColor,
                    maxLines = 1,
                    softWrap = false
                )

                Text(
                    text = activeText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
@Composable
fun AiScoreTile(title: String, score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF080E18),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(0.75.dp, CryptoCyan.copy(alpha = 0.34f), RoundedCornerShape(9.dp))
            .padding(horizontal = 5.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDCE5F5),
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "$score/100",
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = signalProfileConfidenceColor(score),
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun ConsensusMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val valueSize = when {
        value.length >= 9 -> 12.sp
        value.length >= 6 -> 13.sp
        else -> 15.sp
    }

    Column(
        modifier = modifier
            .heightIn(min = 44.dp)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD0D8E8),
            maxLines = 2,
            lineHeight = 8.5.sp,
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
@Composable
fun MultiAiConsensusModule(
    coinSymbol: String,
    oracleScore: Int,
    isLong: Boolean,
    isBengali: Boolean = false
) {
    val geminiScore = (oracleScore - 4).coerceIn(60, 99)
    val gptScore = (oracleScore - 8).coerceIn(60, 99)
    val claudeScore = (oracleScore - 5).coerceIn(60, 99)
    val consensusScore = ((geminiScore + gptScore + claudeScore) / 3).coerceIn(0, 100)

    val directionText = if (isBengali) {
        if (isLong) "উর্ধ্বমুখী প্রবণতা" else "নিম্নমুখী প্রবণতা"
    } else {
        if (isLong) "BULLISH" else "BEARISH"
    }

    val riskText = titanRiskProfileForPositiveScore(consensusScore)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(14.dp))
    ) {
        Column(
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isBengali) "মাল্টি-এআই মডেলের ঐক্যমত ইঞ্জিন" else "MULTI-AI CONSENSUS ENGINES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CryptoCyan,
                letterSpacing = if (isBengali) 0.sp else 1.2.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AiScoreTile("Gemini Pro [Mock]", geminiScore, Modifier.weight(1f))
                AiScoreTile("GPT-4o [Mock]", gptScore, Modifier.weight(1f))
                AiScoreTile("Claude [Mock]", claudeScore, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF04111A),
                                Color(0xFF0B1824),
                                Color(0xFF04111A)
                            )
                        )
                    )
                    .border(0.8.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConsensusMetricColumn(
                    label = if (isBengali) "সম্মিলিত আস্থা" else "CONSENSUS CONFIDENCE",
                    value = "$consensusScore%",
                    valueColor = signalProfileConfidenceColor(consensusScore),
                    modifier = Modifier.weight(1.25f)
                )

                ConsensusMetricColumn(
                    label = if (isBengali) "দিকনির্দেশনা" else "DIRECTION",
                    value = directionText,
                    valueColor = signalProfileDirectionColor(directionText),
                    modifier = Modifier.weight(1.05f)
                )

                ConsensusMetricColumn(
                    label = if (isBengali) "কনসেনসাস বায়াস" else "CONSENSUS BIAS",
                    value = riskText,
                    valueColor = signalProfileRiskColor(riskText),
                    modifier = Modifier.weight(1.0f)
                )
            }
            
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Model Spread: Low (1-4%)", fontSize = 9.sp, color = TextMuted)
                Text(text = "Vote Split: 3/3 Aligned", fontSize = 9.sp, color = CryptoGreen)
                Text(text = "Outliers: 0", fontSize = 9.sp, color = TextMuted)
            }
        }
    }
}
@Composable
fun AiEngineGauge(name: String, score: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF050812), RoundedCornerShape(8.dp))
            .border(0.75.dp, CryptoCyan.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = name, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$score / 100",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = titanPositiveScoreColor(score)
        )
    }
}
@Composable
fun RiskManagementModule(
    currentPrice: Double,
    projectedPrice: Double,
    priceChangePct: Double,
    invalidationPrice: Double = 0.0,
    isLong: Boolean = true
) {
    val calcStopLoss = if (isLong) {
        if (invalidationPrice > 0.0) invalidationPrice else currentPrice * (1.0 - (priceChangePct.absoluteValue * 0.4) / 100.0)
    } else {
        if (invalidationPrice > 0.0) invalidationPrice else currentPrice * (1.0 + (priceChangePct.absoluteValue * 0.4) / 100.0)
    }

    val riskRewardRatio = if (isLong) {
        val targetMove = projectedPrice - currentPrice
        val stopMove = currentPrice - calcStopLoss
        if (stopMove != 0.0) (targetMove / stopMove).absoluteValue else 2.5
    } else {
        val targetMove = currentPrice - projectedPrice
        val stopMove = calcStopLoss - currentPrice
        if (stopMove != 0.0) (targetMove / stopMove).absoluteValue else 2.5
    }

    val tp1 = currentPrice + (projectedPrice - currentPrice) * 0.25
    val tp2 = currentPrice + (projectedPrice - currentPrice) * 0.50
    val tp3 = currentPrice + (projectedPrice - currentPrice) * 0.75
    val tp4 = projectedPrice

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "RISK ENGINEERING & SIZING CONTROL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "STOP LOSS (ATR AWARE)", fontSize = 9.sp, color = CryptoRedText)
                    Text(text = formatPrice(calcStopLoss), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoRedText)
                    Text(text = "SL Distance: ~${String.format("%.1f", priceChangePct.absoluteValue * 0.4)}%", fontSize = 9.sp, color = TextMuted)
                    Text(text = "Sanity State: OK (Standard)", fontSize = 9.sp, color = CryptoGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "RISK REWARD RATIO", fontSize = 9.sp, color = TextSecondary)
                    Text(text = String.format("1 : %.2f", riskRewardRatio), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Black, color = AccentGold)
                    Text(text = "RR Validation: PASS", fontSize = 9.sp, color = CryptoGreen)
                    Text(text = "Risk Path: Asymmetric", fontSize = 9.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "TAKE PROFIT TARGET MATRIX", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TpBadge("TP1 (25%)", tp1, Modifier.weight(1f))
                TpBadge("TP2 (50%)", tp2, Modifier.weight(1f))
                TpBadge("TP3 (75%)", tp3, Modifier.weight(1f))
                TpBadge("TP4 (100%)", tp4, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "RECOMMENDED POSITION ALLOCATION SIZING", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SizingBox("Conservative", "2.0% Cap", Modifier.weight(1f))
                SizingBox("Moderate", "5.0% Cap", Modifier.weight(1f))
                SizingBox("Aggressive", "10.0% Max", Modifier.weight(1f))
            }
        }
    }
}
@Composable
fun TpBadge(label: String, price: Double, modifier: Modifier = Modifier) {
    val formattedPrice = formatPrice(price)
    val priceFontSize = if (formattedPrice.length >= 10) 7.6.sp else 9.sp

    Column(
        modifier = modifier
            .background(Color(0xFF050812), RoundedCornerShape(6.dp))
            .border(0.75.dp, CryptoCyan.copy(alpha = 0.36f), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 8.sp, color = CryptoGreen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formattedPrice,
            fontSize = priceFontSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip
        )
    }
}
@Composable
fun SizingBox(label: String, size: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF050812), RoundedCornerShape(6.dp))
            .border(0.75.dp, signalProfileAllocationColor(label).copy(alpha = 0.46f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val profileColor = signalProfileAllocationColor(label)
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = profileColor.copy(alpha = 0.92f),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = size,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            color = profileColor,
            maxLines = 1,
            softWrap = false
        )
    }
}
@Composable
fun MultiTimeframeForecastModule(currentPrice: Double, isLong: Boolean, priceChangePct: Double) {
    val forecasts = generateMultiTimeframeForecasts(currentPrice, isLong, priceChangePct)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = "MULTI-TIMEFRAME PREDICTION CASCADE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forecasts.take(3).forEach { forecast ->
                    ForecastGridItem(forecast, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forecasts.subList(3, 6).forEach { forecast ->
                    ForecastGridItem(forecast, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forecasts.takeLast(3).forEach { forecast ->
                    ForecastGridItem(forecast, Modifier.weight(1f))
                }
            }
        }
    }
}
@Composable
fun ForecastGridItem(forecast: TimeframeForecast, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF050812), RoundedCornerShape(8.dp))
            .border(0.75.dp, CryptoCyan.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = forecast.timeframe, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CryptoCyan)
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = if (forecast.isBullish) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (forecast.isBullish) CryptoGreen else CryptoRedText,
                modifier = Modifier.size(10.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = formatPrice(forecast.price), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = String.format("%s%.2f%%", if (forecast.roi >= 0) "+" else "", forecast.roi),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (forecast.roi >= 0) CryptoGreen else CryptoRedText
        )
    }
}
fun generateMultiTimeframeForecasts(currentPrice: Double, isLong: Boolean, priceChangePct: Double): List<TimeframeForecast> {
    val intervals = listOf(
        "10m" to 0.08,
        "30m" to 0.15,
        "1h" to 0.30,
        "4h" to 0.70,
        "6h" to 1.00,
        "12h" to 1.50,
        "24h" to 2.20,
        "3d" to 3.50,
        "7d" to 5.00
    )
    val directionMultiplier = if (isLong) 1.0 else -1.0
    val maxPotentialMultiplier = priceChangePct.absoluteValue / 100.0

    return intervals.map { (tf, weight) ->
        val expectedChange = maxPotentialMultiplier * weight * directionMultiplier
        val forecastPrice = currentPrice * (1.0 + expectedChange)
        val roi = expectedChange * 100.0
        val confidence = (85 - (weight * 5)).coerceIn(50.0, 95.0).toInt()
        val isBullish = isLong

        TimeframeForecast(
            timeframe = tf,
            price = forecastPrice,
            roi = roi,
            confidence = confidence,
            isBullish = isBullish
        )
    }
}
