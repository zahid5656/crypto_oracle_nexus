package com.example.feature.signal_pro

import androidx.compose.animation.*
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.ExperimentalFoundationApi
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpotItemCard(coin: SpotSignal, timeframeIndex: Int, viewModel: CryptoViewModel, livePrices: Map<String, Double>) {
    val isBengali by viewModel.isBengali.collectAsState()
    var isExpandedInternal by remember { mutableStateOf(false) }
    val expandedAsset = com.example.feature.signal_pro.LocalExpandedAsset.current
    val isExpanded = expandedAsset.value == coin.coinSymbol
    val spotAutoFitRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(180)
            spotAutoFitRequester.bringIntoView()
        }
    }
    
    val livePrice = livePrices["${coin.coinSymbol}USDT"] ?: coin.currentPrice

    val confidence = signalProSpotConfidence(coin, timeframeIndex)

    val priceDiffLabel = signalProSpotPriceDiffLabel(timeframeIndex)

    val scaleFactor = signalProSpotScaleFactor(timeframeIndex)
    val prevPrice = coin.currentPrice * (1.0 - (coin.growthPotentialPct * 0.1 * scaleFactor).coerceIn(0.01, 0.4))

    val growthPotential = signalProSpotGrowthPotential(coin, timeframeIndex)

    val projectedPrice = coin.currentPrice * (1.0 + growthPotential / 100.0)

    val targetLabel = signalProTargetLabel(timeframeIndex)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(spotAutoFitRequester)
            .border(1.dp, if (isExpanded) CryptoCyan else BorderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = !isExpanded) {
                expandedAsset.value = coin.coinSymbol
            }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedAsset.value = if (isExpanded) null else coin.coinSymbol },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .widthIn(min = 36.dp)
                            .background(CryptoCyan.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin.coinSymbol,
                            color = CryptoCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (coin.coinSymbol.length > 3) 10.sp else 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = coin.coinName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Spot Market Asset",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(CryptoCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confidence probability",
                        tint = CryptoCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$confidence% Probability",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CryptoCyan
                    )
                }
            }

            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

            // Spot coin info (prior ago, Current, Growth %)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: ENTRY (LOCKED)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "ENTRY (LOCKED)",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = formatPrice(coin.currentPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT PRICE",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = formatPrice(coin.currentPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "GROWTH POTENTIAL",
                        fontSize = 9.sp,
                        color = CryptoGreen,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = String.format("+%.2f%%", growthPotential),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGreen,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Target price prediction box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Projected upward potential trajectory",
                        tint = CryptoGreen,
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = (-4).dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = targetLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatPrice(projectedPrice),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoGreen,
                    maxLines = 1,
                    softWrap = false
                )
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to unfold deep institutional matrix ➔",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(360)) + fadeIn(animationSpec = tween(220)),
                exit = shrinkVertically(animationSpec = tween(260)) + fadeOut(animationSpec = tween(180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(10.dp))

                    val hours = signalProForecastHours(timeframeIndex)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Signal State: ACTIVE", fontSize = 9.sp, color = CryptoGreen)
                        Text(text = "Validity: ${hours}H window", fontSize = 9.sp, color = TextMuted)
                        Text(text = "Freshness: 12s", fontSize = 9.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    RealTimeCountdown(coin.coinSymbol, hours, isBengali)
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    RealTimeInvestmentTrackingModule(
                        entryPrice = coin.currentPrice,
                        projectedPrice = projectedPrice,
                        isLong = true,
                        currentPrice = livePrice
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    ExecutionReadinessMatrix(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    SignalQualitySystemBlock(
                        score = coin.oracleScore,
                        confidence = confidence,
                        probability = (confidence - 4).coerceIn(40, 99),
                        riskGrade = titanRiskScoreLabelFromPositiveScore(coin.oracleScore)
                    ,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    TradeChecklistBlock(
                        trendConfirmed = coin.trendStrength != "WEAK",
                        volumeConfirmed = coin.volumeStrength != "WEAK",
                        momentumConfirmed = coin.momentumStrength != "WEAK",
                        liquidityConfirmed = coin.liquidityStrength != "WEAK",
                        riskEvaluated = true
                    ,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    DirectionTradeLogicValidation(isLong = true, isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    MarketRegimeTraceModule(coin.coinSymbol,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    MultiAiConsensusModule(coin.coinSymbol, coin.oracleScore, true, isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    RiskManagementModule(
                        currentPrice = coin.currentPrice,
                        projectedPrice = projectedPrice,
                        priceChangePct = growthPotential,
                        invalidationPrice = coin.invalidationPrice,
                        isLong = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MultiTimeframeForecastModule(coin.currentPrice, true, growthPotential)

                    Spacer(modifier = Modifier.height(10.dp))

                    AiExplanationModule(coin.whyThisSignalEnglish, coin.whyThisSignalBengali, coin.coinSymbol, isBengali) { viewModel.toggleLanguage() }

                    Spacer(modifier = Modifier.height(10.dp))
                    DecisionGateSummary(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    ConflictFlags(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    SourceProvenanceAudit(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    FinalGuidanceModule(isBengali)

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val mission = remember(coin, projectedPrice, timeframeIndex) {
                        com.example.model.Mission(
                            coinSymbol = coin.coinSymbol,
                            type = "LONG",
                            marketType = "Spot",
                            signalTimeframe = signalProTimeframeLabel(timeframeIndex),
                            entryPrice = coin.currentPrice,
                            currentPrice = coin.currentPrice,
                            targets = formatPrice(projectedPrice),
                            stopLoss = formatPrice(coin.invalidationPrice),
                            confidence = confidence,
                            aiStatusEnglish = "Spot trade holding strong.\nNo signs of reversal.",
                            aiStatusBengali = "স্পট ট্রেড মজবুত রয়েছে।\nরিভার্সালের কোনো লক্ষণ নেই।"
                        )
                    }
                    StartTradeFlow(viewModel = viewModel, mission = mission, livePrice = livePrice)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tap here to collapse details ⤴",
                        fontSize = 12.sp,
                        color = CryptoCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedAsset.value = null }
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
@Composable
fun DecisionBriefBlock(
    title: String,
    value: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF08111C),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(0.75.dp, accentColor.copy(alpha = 0.46f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 11.5.sp,
            color = TextPrimary,
            lineHeight = 14.5.sp
        )
    }
}
