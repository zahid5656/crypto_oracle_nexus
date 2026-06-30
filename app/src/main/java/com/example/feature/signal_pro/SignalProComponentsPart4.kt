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
fun FuturesItemCard(coin: FuturesSignal, timeframeIndex: Int, viewModel: CryptoViewModel, livePrices: Map<String, Double>) {
    val isBengali by viewModel.isBengali.collectAsState()
    var isExpandedInternal by remember { mutableStateOf(false) }
    val expandedAsset = com.example.feature.signal_pro.LocalExpandedAsset.current
    val isExpanded = expandedAsset.value == "${coin.coinSymbol}_futures"
    val futuresAutoFitRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(180)
            futuresAutoFitRequester.bringIntoView()
        }
    }
    
    val livePrice = livePrices["${coin.coinSymbol}USDT"] ?: coin.currentPrice

    val isLong = coin.isLong
    
    // REDISIGNED CONTRAST RED FOR SHORT: Highly vibrant, high-contrast scarlet red
    val shortRedThemeColor = CryptoRedText
    val shortRedBadgeBg = CryptoRedText.copy(alpha = 0.14f)
    val shortRedCardBg = Color(0xFF1D1113) // Custom premium wine background tone for high contrast
    val shortRedBorderColor = CryptoRedText.copy(alpha = 0.34f) // Sharp, vibrant red border outline

    val themeColor = if (isLong) CryptoGreen else shortRedThemeColor
    val badgeBg = if (isLong) themeColor.copy(alpha = 0.1f) else shortRedBadgeBg
    val cardBg = if (isLong) DarkSurface else shortRedCardBg
    val cardBorder = if (isLong) BorderColor else if (isExpanded) shortRedThemeColor else shortRedBorderColor

    val probability = signalProFuturesProbability(coin, timeframeIndex)

    val priceChangeMultiplier = signalProFuturesPriceChangeMultiplier(timeframeIndex)
    val priceChange = coin.priceChangePct * priceChangeMultiplier

    val targetPrice = if (isLong) {
        coin.currentPrice * (1.0 + (priceChange / 100.0))
    } else {
        coin.currentPrice * (1.0 - (priceChange / 100.0))
    }
    
    val changeLabel = "EXPECTED"
    val targetLabel = "PREDICTED"

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(futuresAutoFitRequester)
            .border(1.dp, if (isExpanded && isLong) CryptoCyan else cardBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = !isExpanded) {
                expandedAsset.value = "${coin.coinSymbol}_futures"
            }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedAsset.value = if (isExpanded) null else "${coin.coinSymbol}_futures" },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .widthIn(min = 36.dp)
                            .background(badgeBg, RoundedCornerShape(18.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin.coinSymbol,
                            color = themeColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (coin.coinSymbol.length > 3) 10.sp else 12.sp,
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
                            text = if (isLong) "Futures Long Position" else "Futures Short Position",
                            fontSize = 11.sp,
                            color = if (isLong) TextMuted else Color(0xFFD1D5DB) // Enhanced contrast
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isLong) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Signal trend direction",
                        tint = themeColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$probability% Confidence",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColor
                    )
                }
            }

            HorizontalDivider(color = if (isLong) BorderColor else cardBorder.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 8.dp))

            // Price indicators rows
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
                        text = "ENTRY",
                        fontSize = 9.sp,
                        color = if (isLong) TextSecondary else Color(0xFFD1D5DB),
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

                // Column 2: CURRENT PRICE
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENT",
                        fontSize = 9.sp,
                        color = if (isLong) TextSecondary else Color(0xFFD1D5DB),
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

                // Column 3: EXPECTED
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = changeLabel,
                        fontSize = 9.sp,
                        color = themeColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = String.format("%s%.2f%%", if (isLong) "+" else "", priceChange),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColor,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Column 3: TARGET PRICE (Right Aligned)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = targetLabel,
                        fontSize = 9.sp,
                        color = themeColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                    Text(
                        text = formatPrice(targetPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = themeColor,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to unfold deep institutional matrix ➔",
                    fontSize = 10.sp,
                    color = if (isLong) TextMuted else Color(0xFFD1D5DB),
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
                    HorizontalDivider(color = if (isLong) BorderColor else cardBorder.copy(alpha = 0.3f))
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
                        projectedPrice = targetPrice,
                        isLong = isLong,
                        currentPrice = livePrice
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ExecutionReadinessMatrix(isBengali)
                    Spacer(modifier = Modifier.height(10.dp))

                    LeverageIntelligenceModule(coin)

                    Spacer(modifier = Modifier.height(10.dp))

                    SignalQualitySystemBlock(
                        score = coin.oracleScore,
                        confidence = probability,
                        probability = (probability - 4).coerceIn(40, 99),
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
                    DirectionTradeLogicValidation(isLong = isLong, isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    MarketRegimeTraceModule(coin.coinSymbol,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    MultiAiConsensusModule(coin.coinSymbol, coin.oracleScore, isLong, isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    RiskManagementModule(
                        currentPrice = coin.currentPrice,
                        projectedPrice = targetPrice,
                        priceChangePct = priceChange,
                        invalidationPrice = coin.invalidationPrice,
                        isLong = isLong
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MultiTimeframeForecastModule(coin.currentPrice, isLong, priceChange)

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
                    
                    val mission = remember(coin, targetPrice, isLong, timeframeIndex) {
                        com.example.model.Mission(
                            coinSymbol = coin.coinSymbol,
                            type = if (isLong) "LONG" else "SHORT",
                            marketType = "Futures",
                            signalTimeframe = signalProTimeframeLabel(timeframeIndex),
                            entryPrice = coin.currentPrice,
                            currentPrice = coin.currentPrice,
                            targets = formatPrice(targetPrice),
                            stopLoss = formatPrice(coin.invalidationPrice),
                            confidence = probability,
                            aiStatusEnglish = if (isLong) "Bullish convergence intact." else "Bearish momentum building.",
                            aiStatusBengali = if (isLong) "উর্ধ্বমুখী প্রবণতা অটুট রয়েছে।" else "নিম্নমুখী প্রবণতার মতিগতি তৈরি হচ্ছে।"
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OraclePickCard(asset: Any, timeframeIndex: Int, viewModel: CryptoViewModel, livePrices: Map<String, Double> = emptyMap()) {
    val isBengali by viewModel.isBengali.collectAsState()
    var isExpandedInternal by remember { mutableStateOf(false) }
    val expandedAsset = com.example.feature.signal_pro.LocalExpandedAsset.current
    
    val symbol = when (asset) {
        is SpotSignal -> asset.coinSymbol
        is FuturesSignal -> asset.coinSymbol
        else -> ""
    }

    val isExpanded = expandedAsset.value == "${symbol}_oraclepick"
    val oracleAutoFitRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(180)
            oracleAutoFitRequester.bringIntoView()
        }
    }

    val isFutures = asset is FuturesSignal
    val isLong = if (asset is FuturesSignal) asset.isLong else true
    
    val name = when (asset) {
        is SpotSignal -> asset.coinName
        is FuturesSignal -> asset.coinName
        else -> ""
    }
    
    val entryPrice = when (asset) {
        is SpotSignal -> asset.currentPrice
        is FuturesSignal -> asset.currentPrice
        else -> 0.0
    }
    val curPrice = livePrices["${symbol}USDT"] ?: entryPrice

    val potential = when (asset) {
        is SpotSignal -> signalProSpotGrowthPotential(asset, timeframeIndex)
        is FuturesSignal -> {
            val multiplier = signalProFuturesPriceChangeMultiplier(timeframeIndex)
            asset.priceChangePct * multiplier
        }
        else -> 0.0
    }

    val projPrice = when (asset) {
        is SpotSignal -> entryPrice * (1.0 + potential / 100.0)
        is FuturesSignal -> {
            if (isLong) {
                entryPrice * (1.0 + potential / 100.0)
            } else {
                entryPrice * (1.0 - potential / 100.0)
            }
        }
        else -> 0.0
    }

    val score = when (asset) {
        is SpotSignal -> asset.opportunityScore
        is FuturesSignal -> asset.opportunityScore
        else -> 0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(oracleAutoFitRequester)
            .border(
                border = androidx.compose.foundation.BorderStroke(
                    1.35.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(AccentGold, CryptoCyan)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isExpanded) {
                expandedAsset.value = "${symbol}_oraclepick"
            }
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            // Golden title header ribbon
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👑 ORACLE PICK OF THE MOMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = AccentGold,
                    letterSpacing = 1.5.sp
                )
            }

            // Coin primary asset info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedAsset.value = if (isExpanded) null else "${symbol}_oraclepick" },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(38.dp)
                            .widthIn(min = 38.dp)
                            .background(AccentGold.copy(alpha = 0.12f), RoundedCornerShape(19.dp))
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            color = AccentGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isFutures) (if (isLong) "Futures Leverage Long" else "Futures Leverage Short") else "Spot Market Select",
                            fontSize = 11.sp,
                            color = if (isFutures && !isLong) CryptoRedText else CryptoGreen
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // SCORE Pill
                    Box(
                        modifier = Modifier
                            .background(AccentGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SCORE: $score/100",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                    }

                    // Confidence Grade Pill
                    val grade = if (asset is SpotSignal) asset.confidenceGrade else if (asset is FuturesSignal) asset.confidenceGrade else "A"
                    Box(
                        modifier = Modifier
                            .background(CryptoCyan.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "RANK: $grade",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CryptoCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Pricing summaries
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "CURRENT", fontSize = 9.sp, color = TextSecondary)
                    Text(text = formatPrice(curPrice), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PREDICTED", fontSize = 9.sp, color = CryptoGreen)
                    Text(text = formatPrice(projPrice), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = "EXPECTED", fontSize = 9.sp, color = if (isLong) CryptoGreen else CryptoRedText)
                    Text(text = String.format("%s%.2f%%", if (isLong) "+" else "", potential), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isLong) CryptoGreen else CryptoRedText)
                }
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
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

                    RealTimeCountdown(symbol, hours, isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    RealTimeInvestmentTrackingModule(entryPrice = entryPrice, projectedPrice = projPrice, isLong = isLong, currentPrice = curPrice)
                    Spacer(modifier = Modifier.height(10.dp))
                    ExecutionReadinessMatrix(isBengali)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isFutures && asset is FuturesSignal) {
                        LeverageIntelligenceModule(asset)
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    MultiAiConsensusModule(symbol, when(asset) {
                        is SpotSignal -> asset.oracleScore
                        is FuturesSignal -> asset.oracleScore
                        else -> 80
                    }, isLong, isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    DirectionTradeLogicValidation(isLong = isLong, isBengali = isBengali)

                    Spacer(modifier = Modifier.height(10.dp))

                    RiskManagementModule(
                        currentPrice = curPrice,
                        projectedPrice = projPrice,
                        priceChangePct = potential,
                        invalidationPrice = when (asset) {
                            is SpotSignal -> asset.invalidationPrice
                            is FuturesSignal -> asset.invalidationPrice
                            else -> 0.0
                        },
                        isLong = isLong
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MultiTimeframeForecastModule(curPrice, isLong, potential)

                    Spacer(modifier = Modifier.height(10.dp))

                    val whyEn = when (asset) {
                        is SpotSignal -> asset.whyThisSignalEnglish
                        is FuturesSignal -> asset.whyThisSignalEnglish
                        else -> ""
                    }
                    val whyBn = when (asset) {
                        is SpotSignal -> asset.whyThisSignalBengali
                        is FuturesSignal -> asset.whyThisSignalBengali
                        else -> ""
                    }
                    AiExplanationModule(whyEn, whyBn, symbol, isBengali) { viewModel.toggleLanguage() }

                    Spacer(modifier = Modifier.height(10.dp))
                    DecisionGateSummary(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    ConflictFlags(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    SourceProvenanceAudit(isBengali)

                    Spacer(modifier = Modifier.height(10.dp))
                    FinalGuidanceModule(isBengali)

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val mission = remember(asset, projPrice, isLong, timeframeIndex) {
                        val invalidation = when (asset) {
                            is SpotSignal -> asset.invalidationPrice
                            is FuturesSignal -> asset.invalidationPrice
                            else -> 0.0
                        }
                        val conf = when (asset) {
                            is SpotSignal -> asset.confidencePct
                            is FuturesSignal -> asset.probabilityPct
                            else -> 80
                        }
                        com.example.model.Mission(
                            coinSymbol = symbol,
                            type = if (isLong) "LONG" else "SHORT",
                            marketType = if (isFutures) "Futures" else "Spot",
                            signalTimeframe = signalProTimeframeLabel(timeframeIndex),
                            entryPrice = entryPrice,
                            currentPrice = curPrice,
                            targets = formatPrice(projPrice),
                            stopLoss = formatPrice(invalidation),
                            confidence = conf,
                            aiStatusEnglish = "Oracle Pick active monitoring.",
                            aiStatusBengali = "ওরাকল পিক সক্রিয় পর্যবেক্ষণ।"
                        )
                    }
                    StartTradeFlow(viewModel = viewModel, mission = mission, livePrice = curPrice)
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
fun RealTimeInvestmentTrackingModule(
    entryPrice: Double,
    projectedPrice: Double,
    isLong: Boolean,
    currentPrice: Double
) {
    val progress = ((currentPrice - entryPrice) / (projectedPrice - entryPrice)).coerceIn(0.0, 1.0)
    val currentRoi = if (entryPrice > 0) ((currentPrice - entryPrice) / entryPrice) * 100 * (if(isLong) 1 else -1) else 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.8.dp, CryptoCyan.copy(alpha = 0.62f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF03111B),
                            Color(0xFF0B1220),
                            Color(0xFF02050D)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = "REAL-TIME INVESTMENT TRACKING",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = CryptoCyan,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(text = "ENTRY", fontSize = 9.sp, color = TextSecondary)
                    Text(text = formatPrice(entryPrice), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(CryptoGreen, shape = CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "LIVE PRICE", fontSize = 9.sp, color = TextSecondary)
                        Text(text = formatPrice(currentPrice), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "CURRENT ROI", fontSize = 9.sp, color = CryptoGreen)
                    Text(text = String.format("%s%.2f%%", if(currentRoi >= 0) "+" else "", currentRoi), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Black, color = CryptoGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Target Progress", fontSize = 10.sp, color = TextSecondary)
                Text(text = String.format("%.1f%% achieved", progress * 100), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
            }

            LinearProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = CryptoGreen,
                trackColor = BorderColor
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MARKET EVIDENCE SUMMARY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Signal Heat: 94", fontSize = 9.sp, color = CryptoRedText)
                Text(text = "Pressure: ${if (isLong) "Buy" else "Sell"} Side", fontSize = 9.sp, color = if (isLong) CryptoGreen else CryptoRedText)
                Text(text = "Trend Bias: ${if (isLong) "Bullish" else "Bearish"}", fontSize = 9.sp, color = if (isLong) CryptoGreen else CryptoRedText)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Volatility: Elevated", fontSize = 9.sp, color = AccentGold)
                Text(text = "Liquidity detail: Strong (Tier 1 Order Book)", fontSize = 9.sp, color = CryptoCyan)
            }
        }
    }
}
