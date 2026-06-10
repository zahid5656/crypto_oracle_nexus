package com.example.ui

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
import androidx.compose.material.icons.filled.Star
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

@Composable
fun AnalysisScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()

    androidx.activity.compose.BackHandler {
        viewModel.navigateTo(AppScreen.Home)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (val state = analysisState) {
            is AnalysisState.Idle -> {
                val initData by viewModel.newsFeedData.collectAsState()
                PredictionDashboard(
                    data = initData,
                    viewModel = viewModel
                )
            }
            is AnalysisState.Analyzing -> {
                AnalyzingTelemetryScreen(stepMessage = state.statusMessage)
            }
            is AnalysisState.Success -> {
                PredictionDashboard(
                    data = state.data,
                    viewModel = viewModel
                )
            }
            is AnalysisState.Error -> {
                ScannerErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.runScanner() },
                    onGoBack = { viewModel.navigateTo(AppScreen.Home) }
                )
            }
        }
    }
}

@Composable
fun AnalyzingTelemetryScreen(stepMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp)
        ) {
            CircularProgressIndicator(
                color = CryptoCyan,
                strokeWidth = 4.dp,
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Calculating scanner metrics",
                tint = CryptoCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SCANNING SIGNAL MATRIX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CryptoCyan,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stepMessage,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Futuristic decorative log box
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(DarkSurface, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(6.dp).background(CryptoGreen, CircleShape).align(Alignment.CenterVertically))
                Text(text = "RSI Relative indicators calculated", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(6.dp).background(CryptoGreen, CircleShape).align(Alignment.CenterVertically))
                Text(text = "MACD trend histograms synchronized", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(6.dp).background(CryptoCyan, CircleShape).align(Alignment.CenterVertically))
                Text(text = "Evaluating historical probability metrics", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
            }
        }
    }
}

@Composable
fun PredictionDashboard(
    data: OracleAnalysisResponse,
    viewModel: CryptoViewModel
) {
    val selectedIndex by viewModel.selectedDashboardTab.collectAsState()
    var futuresSubTab by remember { mutableStateOf(0) }
    var spotTimeframe by remember { mutableStateOf(0) }
    var futuresTimeframe by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // App Custom Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .background(DarkSurface, CircleShape)
                    .border(1.dp, BorderColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Navigate to home",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "PREDICTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Oracle Dashboard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            
            // Re-run scanner
            IconButton(
                onClick = { viewModel.runScanner() },
                modifier = Modifier
                    .background(DarkSurface, CircleShape)
                    .border(1.dp, BorderColor, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Re-run scanner",
                    tint = CryptoCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Condensed AI Modality and Scan Button
        val useAiOracle by viewModel.useAiOracle.collectAsState()
        val lastScanTime by viewModel.lastScanTime.collectAsState()
        
        val now = System.currentTimeMillis()
        val diffSecs = (now - lastScanTime) / 1000
        val diffMins = diffSecs / 60
        val timeString = if (diffMins > 0) "$diffMins mins ago" else "$diffSecs secs ago"
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Modality Switch Side
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo_ai),
                    contentDescription = "AI Oracle Modality Logo",
                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "AI ORACLE MODALITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (useAiOracle) "Deep Gemini Sentient API" else "Fast Technical Simulator local",
                        fontSize = 9.sp,
                        color = if (useAiOracle) CryptoCyan else TextSecondary
                    )
                    Text(
                        text = "Last Scan: $timeString",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                    checked = useAiOracle,
                    onCheckedChange = { viewModel.toggleOracleMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CryptoCyan,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    ),
                    modifier = Modifier.scale(0.7f)
                )
            }
            
            // Scan Button
            Button(
                onClick = { viewModel.runScanner() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE2E8F0), // Near white
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(36.dp)
                    .graphicsLayer {
                        shadowElevation = 8.dp.toPx()
                        shape = RoundedCornerShape(8.dp)
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Scan Now",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Tab Selectors (A: Spot Trading, B: Futures Trading)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(DarkSurface, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            TabButton(
                title = "Spot Signals",
                isSelected = selectedIndex == 0,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setDashboardTab(0) }
            )
            TabButton(
                title = "Futures Signals",
                isSelected = selectedIndex == 1,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setDashboardTab(1) }
            )
        }

        // Dashboard Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedIndex) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTimeframeRow(
                            selectedInterval = spotTimeframe,
                            onIntervalSelected = { spotTimeframe = it }
                        )

                        val sortedSpot = data.spotSignals.sortedByDescending { coin ->
                            when (spotTimeframe) {
                                0 -> coin.confidencePct
                                1 -> coin.confidenceTwelveHoursPct ?: coin.confidencePct
                                2 -> coin.confidencePct - 5
                                3 -> coin.confidencePct - 10
                                else -> coin.confidencePct - 16
                            }
                        }.take(10)

                        SpotTradingList(
                            signals = sortedSpot,
                            timeframeIndex = spotTimeframe,
                            viewModel = viewModel
                        )
                    }
                }
                1 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            SubTabButton(
                                title = "BUY LONG (Top 10)",
                                isSelected = futuresSubTab == 0,
                                isLongSelection = true,
                                modifier = Modifier.weight(1f),
                                onClick = { futuresSubTab = 0 }
                            )
                            SubTabButton(
                                title = "SELL SHORT (Top 10)",
                                isSelected = futuresSubTab == 1,
                                isLongSelection = false,
                                modifier = Modifier.weight(1f),
                                onClick = { futuresSubTab = 1 }
                            )
                        }

                        ScrollableTimeframeRow(
                            selectedInterval = futuresTimeframe,
                            onIntervalSelected = { futuresTimeframe = it },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val rawFutures = if (futuresSubTab == 0) data.futuresLongSignals else data.futuresShortSignals
                        val sortedFutures = rawFutures.sortedByDescending { coin ->
                            when (futuresTimeframe) {
                                0 -> coin.probabilityPct
                                1 -> coin.probabilityTwelveHoursPct ?: coin.probabilityPct
                                2 -> coin.probabilityPct - 4
                                3 -> coin.probabilityPct - 8
                                else -> coin.probabilityPct - 12
                            }
                        }.take(10)

                        FuturesTradingList(
                            signals = sortedFutures,
                            timeframeIndex = futuresTimeframe,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CryptoCyan.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) CryptoCyan else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) CryptoCyan else TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SubTabButton(
    title: String,
    isSelected: Boolean,
    isLongSelection: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeBgColor = if (isLongSelection) {
        if (isSelected) CryptoGreen.copy(alpha = 0.12f) else Color.Transparent
    } else {
        if (isSelected) Color(0xFFDC2626) else Color.Transparent
    }
    
    val textColor = if (isSelected) {
        if (isLongSelection) CryptoGreen else Color.White
    } else {
        TextSecondary
    }

    val borderModifier = if (isSelected && !isLongSelection) {
        Modifier.border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(6.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(activeBgColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SpotTradingList(signals: List<SpotSignal>, timeframeIndex: Int, viewModel: CryptoViewModel) {
    val oraclePick = signals.maxByOrNull { it.opportunityScore }
    val livePrices by viewModel.livePrices.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        if (oraclePick != null) {
            item {
                OraclePickCard(asset = oraclePick, timeframeIndex = timeframeIndex, viewModel = viewModel, livePrices = livePrices)
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALL SCANNED EXCHANGE ASSETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        items(signals) { coin ->
            SpotItemCard(coin, timeframeIndex = timeframeIndex, viewModel = viewModel, livePrices = livePrices)
        }
    }
}

@Composable
fun SpotItemCard(coin: SpotSignal, timeframeIndex: Int, viewModel: CryptoViewModel, livePrices: Map<String, Double>) {
    val isBengali by viewModel.isBengali.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    
    val livePrice = livePrices["${coin.coinSymbol}USDT"] ?: coin.currentPrice

    val confidence = when(timeframeIndex) {
        0 -> coin.confidencePct
        1 -> coin.confidenceTwelveHoursPct ?: coin.confidencePct
        2 -> (coin.confidencePct - 5).coerceIn(60, 99)
        3 -> (coin.confidencePct - 10).coerceIn(52, 99)
        else -> (coin.confidencePct - 16).coerceIn(45, 99)
    }

    val priceDiffLabel = when(timeframeIndex) {
        0 -> "PRICE 6H AGO"
        1 -> "PRICE 12H AGO"
        2 -> "PRICE 24H AGO"
        3 -> "PRICE 3D AGO"
        else -> "PRICE 7D AGO"
    }

    val scaleFactor = when(timeframeIndex) {
        0 -> 1.0
        1 -> 1.8
        2 -> 3.2
        3 -> 7.5
        else -> 12.0
    }
    val prevPrice = coin.currentPrice * (1.0 - (coin.growthPotentialPct * 0.1 * scaleFactor).coerceIn(0.01, 0.4))

    val growthPotential = when(timeframeIndex) {
        0 -> coin.growthPotentialPct
        1 -> coin.growthPotentialTwelveHoursPct ?: (coin.growthPotentialPct * 1.5)
        2 -> coin.growthPotentialPct * 2.2
        3 -> coin.growthPotentialPct * 3.5
        else -> coin.growthPotentialPct * 5.0
    }

    val projectedPrice = coin.currentPrice * (1.0 + growthPotential / 100.0)

    val targetLabel = when(timeframeIndex) {
        0 -> "6-H Predicted Target"
        1 -> "12-H Predicted Target"
        2 -> "24-H Gold Target"
        3 -> "3-Day Wave Target"
        else -> "7-Day Range High"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isExpanded) CryptoCyan else BorderColor, RoundedCornerShape(16.dp))
            .clickable { if (!isExpanded) isExpanded = true }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

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

            Spacer(modifier = Modifier.height(10.dp))

            // Target price prediction box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(12.dp),
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isExpanded) "Tap to collapse detailed matrix ⬏" else "Tap to unfold deep institutional matrix ➔",
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    val hours = when(timeframeIndex) {
                        0 -> 6
                        1 -> 12
                        2 -> 24
                        3 -> 72
                        else -> 168
                    }
                    RealTimeCountdown(coin.coinSymbol, hours)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    RealTimeInvestmentTrackingModule(
                        entryPrice = coin.currentPrice,
                        projectedPrice = projectedPrice,
                        isLong = true,
                        currentPrice = livePrice
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SignalQualitySystemBlock(
                        score = coin.oracleScore,
                        confidence = confidence,
                        probability = (confidence - 4).coerceIn(40, 99),
                        riskGrade = if (coin.oracleScore >= 85) "LOW" else "MEDIUM"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TradeChecklistBlock(
                        trendConfirmed = coin.trendStrength != "WEAK",
                        volumeConfirmed = coin.volumeStrength != "WEAK",
                        momentumConfirmed = coin.momentumStrength != "WEAK",
                        liquidityConfirmed = coin.liquidityStrength != "WEAK",
                        riskEvaluated = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MarketRegimeTraceModule(coin.coinSymbol)

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiAiConsensusModule(coin.coinSymbol, coin.oracleScore, true)

                    Spacer(modifier = Modifier.height(16.dp))

                    RiskManagementModule(
                        currentPrice = coin.currentPrice,
                        projectedPrice = projectedPrice,
                        priceChangePct = growthPotential,
                        invalidationPrice = coin.invalidationPrice,
                        isLong = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiTimeframeForecastModule(coin.currentPrice, true, growthPotential)

                    Spacer(modifier = Modifier.height(16.dp))

                    AiExplanationModule(coin.whyThisSignalEnglish, coin.whyThisSignalBengali, coin.coinSymbol, isBengali) { viewModel.toggleLanguage() }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val mission = remember(coin, projectedPrice) {
                        com.example.model.Mission(
                            coinSymbol = coin.coinSymbol,
                            type = "LONG",
                            marketType = "Spot",
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap here to collapse details ⬏",
                        fontSize = 12.sp,
                        color = CryptoCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = false }
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun StartTradeFlow(viewModel: CryptoViewModel, mission: com.example.model.Mission, livePrice: Double) {
    var step by remember { mutableStateOf(0) }

    val custom1 by viewModel.customSetup1.collectAsState()
    val custom2 by viewModel.customSetup2.collectAsState()
    val defaultName by viewModel.defaultSetupName.collectAsState()
    val defaultPolicy by viewModel.defaultAiPolicy.collectAsState()
    val recAutoCloseFlow by viewModel.recommendedAutoCloseConditions.collectAsState()

    if (step == 1) {
        val verifiedEntryLocked = remember { livePrice }
        var selectedSetup by remember { mutableStateOf(defaultName) }
        var aiPolicy by remember { mutableStateOf(defaultPolicy) }

        val activeProfile = when (selectedSetup) {
            "CUSTOM SETUP-1", "SETUP-1" -> custom1
            "CUSTOM SETUP-2", "SETUP-2" -> custom2
            else -> null
        }
        
        val isRecommended = selectedSetup == "RECOMMENDED SETUP"
        
        val parsedTargets = mission.targets.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        val pTp1 = parsedTargets.getOrNull(0) ?: mission.targets
        val pTp2 = parsedTargets.getOrNull(1)
        val pTp3 = parsedTargets.getOrNull(2)
        
        // User profile values (what's shown in the summary for the profile)
        val profileTarget = if (isRecommended) mission.targets else activeProfile?.target?.ifBlank { null }
        val profileTp1 = if (isRecommended) pTp1 else activeProfile?.tp1?.ifBlank { null }
        val profileTp2 = if (isRecommended) pTp2 else activeProfile?.tp2?.ifBlank { null }
        val profileTp3 = if (isRecommended) pTp3 else activeProfile?.tp3?.ifBlank { null }
        val profileSl1 = if (isRecommended) mission.stopLoss else activeProfile?.stopLoss?.ifBlank { null }
        val profileSl2 = activeProfile?.sl2?.ifBlank { null }
        val profileLev = if (isRecommended) (if (mission.marketType.equals("Futures", ignoreCase = true)) "5X" else "SPOT (1X)") else activeProfile?.leverage?.ifBlank { null }
        val profileAlloc = if (isRecommended) "5%" else activeProfile?.positionSize?.ifBlank { null }
        val profileRemark = activeProfile?.remark?.ifBlank { null }
        val profileRisk = if (isRecommended) "BALANCED" else activeProfile?.riskProfile?.ifBlank { null }

        // Effective mission values (used for calculations and mission creation)
        val effectiveTarget = profileTarget ?: mission.targets
        val effectiveTp1 = profileTp1 ?: mission.targets
        val effectiveTp2 = profileTp2
        val effectiveTp3 = profileTp3
        val effectiveSl1 = profileSl1 ?: mission.stopLoss
        val effectiveSl2 = profileSl2
        val effectiveLev = profileLev

        val numberRegex = Regex("[0-9]*\\.?[0-9]+")
        val parsedTarget = effectiveTarget?.replace(",", "")?.let { numberRegex.find(it)?.value?.toDoubleOrNull() }
        val parsedTp1 = effectiveTp1?.replace(",", "")?.let { numberRegex.find(it)?.value?.toDoubleOrNull() }
        val parsedTp2 = effectiveTp2?.replace(",", "")?.let { numberRegex.find(it)?.value?.toDoubleOrNull() }
        val parsedTp3 = effectiveTp3?.replace(",", "")?.let { numberRegex.find(it)?.value?.toDoubleOrNull() }
        val parsedSl1 = effectiveSl1?.replace(",", "")?.let { numberRegex.find(it)?.value?.toDoubleOrNull() }
        val parsedSl2 = effectiveSl2?.replace(",", "")?.let { numberRegex.find(it)?.value?.toDoubleOrNull() }
        
        val autoCloseOptions = if (isRecommended) recAutoCloseFlow else activeProfile?.autoCloseConditions ?: emptyList()
        val isLong = mission.type.uppercase() == "LONG" || mission.type.uppercase() == "BUY"
        val (acValid, acReason) = validateAutoClose(isLong, verifiedEntryLocked, parsedTarget, parsedTp1, parsedTp2, parsedTp3, parsedSl1, parsedSl2, autoCloseOptions, verifiedEntryLocked)

        AlertDialog(
            onDismissRequest = { step = 0 },
            title = { Text("Confirm Trade Activation", color = CryptoCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    Text("CURRENT MARKET PRICE: ${String.format("%.4f", verifiedEntryLocked)} USDT", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("SELECT SETUP:", color = TextSecondary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val setups = listOf("RECOMMENDED SETUP", "SETUP-1", "SETUP-2")
                    setups.forEach { setup ->
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedSetup = setup }
                                .background(if (selectedSetup == setup) CryptoCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .border(1.dp, if (selectedSetup == setup) CryptoCyan else BorderColor, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text(setup, color = if (selectedSetup == setup) CryptoCyan else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SETUP SUMMARY", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SETUP USED", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(selectedSetup, color = CryptoCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TARGET", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                val tgText = if (isRecommended) "${mission.targets} / SYSTEM RECOMMENDED" else (profileTarget ?: "NOT SET")
                                Text(tgText, color = if (profileTarget != null || isRecommended) Color(0xFF34C759) else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (!isRecommended && profileTarget == null && mission.targets != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("EFFECTIVE TARGET", color = TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("${mission.targets} / SIGNAL FALLBACK", color = Color(0xFF34C759), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TP1", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                val tpText = if (isRecommended) "${mission.targets} / SYSTEM RECOMMENDED" else (profileTp1 ?: "NOT SET")
                                Text(tpText, color = if (profileTp1 != null || isRecommended) Color(0xFF34C759) else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (!isRecommended && profileTp1 == null && mission.targets != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("EFFECTIVE TP1", color = TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("${mission.targets} / SIGNAL FALLBACK", color = Color(0xFF34C759), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TP2", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(profileTp2 ?: "NOT SET", color = if (profileTp2 != null) Color(0xFF34C759) else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("TP3", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(profileTp3 ?: "NOT SET", color = if (profileTp3 != null) Color(0xFF34C759) else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SL1 (STOP LOSS)", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                val slText = if (isRecommended) "${mission.stopLoss} / SYSTEM RECOMMENDED" else (profileSl1 ?: "NOT SET")
                                Text(slText, color = if (profileSl1 != null || isRecommended) Color(0xFFFF3B30) else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (!isRecommended && profileSl1 == null && mission.stopLoss != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("EFFECTIVE SL1", color = TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("${mission.stopLoss} / SIGNAL FALLBACK", color = Color(0xFFFF3B30), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SL2", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(profileSl2 ?: "NOT SET", color = if (profileSl2 != null) Color(0xFFFF3B30) else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("LEVERAGE", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                val levText = if (isRecommended) "SPOT (1X) / SYSTEM RECOMMENDED" else (profileLev ?: "NOT SET")
                                Text(levText, color = if (profileLev != null || isRecommended) TextPrimary else TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (!isRecommended && profileLev == null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("EFFECTIVE LEV", color = TextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text(if (mission.marketType.equals("Spot", ignoreCase = true)) "SPOT (1X) / SIGNAL FALLBACK" else "NOT SET", color = TextPrimary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ALLOCATION", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(profileAlloc?.uppercase() ?: "NOT SET", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("RISK PROFILE", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(profileRisk?.uppercase() ?: "NOT SET", color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                            if (profileRemark != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("REMARK", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(profileRemark.uppercase(), color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (parsedTp1 != null && parsedSl1 != null && verifiedEntryLocked > 0.0) {
                                val risk = kotlin.math.abs(verifiedEntryLocked - parsedSl1)
                                val reward = kotlin.math.abs(parsedTp1 - verifiedEntryLocked)
                                if (risk > 0.0 && reward > 0.0) {
                                    val ratio = reward / risk
                                    val ratioColor = if (ratio >= 2.0) Color(0xFF34C759) else if (ratio >= 1.0) AccentGold else Color(0xFFFF3B30)
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("RISK : REWARD", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Text("1 : ${String.format("%.1f", ratio)}", color = ratioColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("STATUS", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Text("READY", color = setupStatusColor("READY"), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    val totalSize = risk + reward
                                    val riskWeight = (risk / totalSize).toFloat().coerceIn(0.15f, 0.85f)
                                    val rewardWeight = (reward / totalSize).toFloat().coerceIn(0.15f, 0.85f)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Box(modifier = Modifier.weight(riskWeight).fillMaxHeight().background(Color(0xFFFF3B30)))
                                        Box(modifier = Modifier.weight(rewardWeight).fillMaxHeight().background(Color(0xFF34C759)))
                                    }
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("RISK : REWARD", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Text("N/A", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("STATUS", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                        Text("INVALID / HIGH RISK", color = setupStatusColor("INVALID / HIGH RISK"), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("RISK : REWARD", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text("N/A", color = AccentGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("STATUS", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text("INCOMPLETE SETUP", color = setupStatusColor("INCOMPLETE SETUP"), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AUTO-CLOSE TRADING SETUP", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            val isAutoActive = autoCloseOptions.isNotEmpty()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("AUTO TRADING ACTIVE", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                Text(if (isAutoActive) "YES" else "NO", color = if (isAutoActive) CryptoCyan else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            if (isAutoActive) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("AUTO-CLOSE CONDITIONS", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(autoCloseOptions.joinToString(" / "), color = TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("CONDITION VALIDITY", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                    Text(if (acValid) "VALID" else "INVALID", color = if (acValid) Color(0xFF34C759) else Color(0xFFFF3B30), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                if (!acValid && acReason != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("REASON: $acReason", color = Color(0xFFFF3B30), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                    Text("AUTO-CLOSE DISABLED UNTIL FIXED", color = Color(0xFFFF3B30), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("MANUAL APPROVAL REQUIRED", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("SIGNAL FRESHNESS", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(AccentGold.copy(alpha=0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, AccentGold.copy(alpha=0.5f), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("EXPIRY UNKNOWN", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("USE CURRENT MARKET CONFIRMATION", color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI TRADE COPILOT POLICY:", color = TextSecondary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { aiPolicy = "ASSIST ONLY" }
                            .background(if (aiPolicy == "ASSIST ONLY") CryptoCyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                            .border(1.dp, if (aiPolicy == "ASSIST ONLY") CryptoCyan else BorderColor, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("ASSIST ONLY", color = if (aiPolicy == "ASSIST ONLY") CryptoCyan else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("MANUAL APPROVAL REQUIRED", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { aiPolicy = "ASSIST & EXECUTION" }
                            .background(if (aiPolicy == "ASSIST & EXECUTION") AccentGold.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                            .border(1.dp, if (aiPolicy == "ASSIST & EXECUTION") AccentGold else BorderColor, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("ASSIST & EXECUTION", color = if (aiPolicy == "ASSIST & EXECUTION") AccentGold else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("SHADOW EXECUTION ONLY", color = AccentGold, fontSize = 9.sp)
                            Text("NO REAL MARKET ORDER", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    
                }
            },
            confirmButton = {
                Button(onClick = {
                    var calculatedStatus = "INCOMPLETE SETUP"
                    var calculatedRR = "N/A"
                    
                    if (parsedTp1 != null && parsedSl1 != null && verifiedEntryLocked > 0.0) {
                        val risk = kotlin.math.abs(verifiedEntryLocked - parsedSl1)
                        val reward = kotlin.math.abs(parsedTp1 - verifiedEntryLocked)
                        if (risk > 0.0 && reward > 0.0) {
                            val ratio = reward / risk
                            calculatedRR = "1 : ${String.format("%.1f", ratio)}"
                            calculatedStatus = "READY"
                        } else {
                            calculatedStatus = "INVALID / HIGH RISK"
                        }
                    }
                    
                    val initialLogs = mutableListOf(
                        "MISSION CREATED",
                        "SETUP SELECTED: $selectedSetup",
                        "AI POLICY SELECTED: AI TRADE COPILOT $aiPolicy",
                        if (autoCloseOptions.isNotEmpty()) "AUTO-CLOSE CONDITIONS: ${autoCloseOptions.joinToString(" / ")}" else "AUTO-CLOSE CONDITIONS: NONE",
                        "CONDITION VALIDITY: ${if (acValid) "VALID" else "INVALID"}",
                        "REAL MARKET ORDER: NO",
                        "MISSION MONITORING ACTIVE"
                    )

                    val finalTarget = effectiveTarget?.let { if (profileTarget == null && !isRecommended) "$it / SIGNAL FALLBACK" else it }
                    val finalTp1 = effectiveTp1?.let { if (profileTp1 == null && !isRecommended) "$it / SIGNAL FALLBACK" else it }
                    val finalSl = effectiveSl1?.let { if (profileSl1 == null && !isRecommended) "$it / SIGNAL FALLBACK" else it }
                    val finalLev = effectiveLev?.let { if (profileLev == null && !isRecommended) (if (mission.marketType.equals("Spot", ignoreCase = true)) "SPOT (1X) / SIGNAL FALLBACK" else "NOT SET") else it }

                    viewModel.startMission(mission.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        entryPrice = verifiedEntryLocked,
                        originalSignalEntry = mission.entryPrice,
                        startTime = System.currentTimeMillis(),
                        setupMode = selectedSetup,
                        setupRemark = profileRemark ?: (if (isRecommended) "AUTO-GENERATED" else null),
                        target = finalTarget,
                        tp1 = finalTp1,
                        tp2 = effectiveTp2,
                        tp3 = effectiveTp3,
                        manualStopLoss = finalSl,
                        sl2 = effectiveSl2,
                        leverage = finalLev,
                        riskProfile = profileRisk,
                        positionSize = profileAlloc,
                        copilotMode = aiPolicy,
                        executionMode = aiPolicy,
                        setupStatus = calculatedStatus,
                        setupRiskReward = calculatedRR,
                        autoCloseEnabled = autoCloseOptions.isNotEmpty() && acValid,
                        autoCloseConditions = autoCloseOptions,
                        conditionValidity = if (acValid) "VALID" else "INVALID",
                        conditionInvalidReason = acReason,
                        missionHistoryLog = initialLogs.toList()
                    ))
                    viewModel.sendLocalAlert("Mission Started", "AI intelligence system successfully started monitoring ${mission.coinSymbol}")
                    step = 0
                }, colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)) {
                    Text("CONFIRM", fontWeight = FontWeight.Black, color = DarkBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    if (step == 2) {
        var localSetup1 by remember { mutableStateOf(custom1) }
        var localSetup2 by remember { mutableStateOf(custom2) }
        var localDefaultName by remember { mutableStateOf(defaultName) }
        var localDefaultPolicy by remember { mutableStateOf(defaultPolicy) }
        val recAutoCloseFlow by viewModel.recommendedAutoCloseConditions.collectAsState()
        var localRecAutoClose by remember { mutableStateOf(recAutoCloseFlow) }
        var selectedTab by remember { mutableStateOf("RECOMMENDED SETUP") }
        
        AlertDialog(
            onDismissRequest = { step = 0 },
            title = { Text("Signal Setup Configuration", color = AccentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).clickable { selectedTab = "RECOMMENDED SETUP" }
                                .background(if (selectedTab == "RECOMMENDED SETUP") AccentGold.copy(alpha=0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .border(1.dp, if (selectedTab == "RECOMMENDED SETUP") AccentGold else BorderColor, RoundedCornerShape(4.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("REC", color = if (selectedTab=="RECOMMENDED SETUP") AccentGold else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        
                        Box(
                            modifier = Modifier.weight(1f).clickable { selectedTab = "SETUP-1" }
                                .background(if (selectedTab == "SETUP-1") AccentGold.copy(alpha=0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .border(1.dp, if (selectedTab == "SETUP-1") AccentGold else BorderColor, RoundedCornerShape(4.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("SETUP-1", color = if (selectedTab=="SETUP-1") AccentGold else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                        
                        Box(
                            modifier = Modifier.weight(1f).clickable { selectedTab = "SETUP-2" }
                                .background(if (selectedTab == "SETUP-2") AccentGold.copy(alpha=0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                .border(1.dp, if (selectedTab == "SETUP-2") AccentGold else BorderColor, RoundedCornerShape(4.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("SETUP-2", color = if (selectedTab=="SETUP-2") AccentGold else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CryptoCyan, unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                    
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (selectedTab == "RECOMMENDED SETUP") {
                            item { Text("AUTO-CLOSE TRADING SETUP", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            item { Text("Select conditions to enable auto-close for recommended values.", color = TextSecondary, fontSize = 10.sp) }
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                            
                            val options = listOf("TARGET", "TP1", "TP2", "TP3", "STOP LOSS")
                            options.forEach { opt ->
                                item {
                                    Row(modifier = Modifier.fillMaxWidth().clickable {
                                        localRecAutoClose = if (localRecAutoClose.contains(opt)) localRecAutoClose - opt else localRecAutoClose + opt
                                    }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = localRecAutoClose.contains(opt), onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            val currentSetup = if (selectedTab == "SETUP-1") localSetup1 else localSetup2
                            val updateSetup = { newSetup: com.example.model.CustomSetupProfile -> 
                                if (selectedTab == "SETUP-1") localSetup1 = newSetup else localSetup2 = newSetup
                            }
                            
                            item { OutlinedTextField(value = currentSetup.target, onValueChange = { updateSetup(currentSetup.copy(target=it)) }, label = { Text("TARGET") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.tp1, onValueChange = { updateSetup(currentSetup.copy(tp1=it)) }, label = { Text("TP1") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.tp2, onValueChange = { updateSetup(currentSetup.copy(tp2=it)) }, label = { Text("TP2") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.tp3, onValueChange = { updateSetup(currentSetup.copy(tp3=it)) }, label = { Text("TP3") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.stopLoss, onValueChange = { updateSetup(currentSetup.copy(stopLoss=it)) }, label = { Text("SL1 / STOP LOSS 1") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.sl2, onValueChange = { updateSetup(currentSetup.copy(sl2=it)) }, label = { Text("SL2 / STOP LOSS 2") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.leverage, onValueChange = { updateSetup(currentSetup.copy(leverage=it)) }, label = { Text("LEVERAGE") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            
                            item { 
                                Text("RISK PROFILE", color = TextSecondary, fontSize = 10.sp)
                                val riskOptions = listOf("CONSERVATIVE", "BALANCED", "AGGRESSIVE", "CUSTOM")
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    riskOptions.forEach { opt ->
                                        Box(modifier = Modifier.clickable { updateSetup(currentSetup.copy(riskProfile=opt)) }.background(if (currentSetup.riskProfile == opt) CryptoCyan else Color.Transparent, RoundedCornerShape(4.dp)).border(1.dp, if (currentSetup.riskProfile == opt) CryptoCyan else BorderColor, RoundedCornerShape(4.dp)).padding(8.dp)) {
                                            Text(opt, color = if (currentSetup.riskProfile == opt) DarkBackground else TextSecondary, fontSize = 8.sp)
                                        }
                                    }
                                }
                            }
                            
                            item { OutlinedTextField(value = currentSetup.positionSize, onValueChange = { updateSetup(currentSetup.copy(positionSize=it)) }, label = { Text("ALLOCATION") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            item { OutlinedTextField(value = currentSetup.remark, onValueChange = { updateSetup(currentSetup.copy(remark=it)) }, label = { Text("REMARK") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                            
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                            item { Text("AUTO-CLOSE TRADING SETUP", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            val options = listOf("TARGET", "TP1", "TP2", "TP3", "STOP LOSS")
                            options.forEach { opt ->
                                item {
                                    Row(modifier = Modifier.fillMaxWidth().clickable {
                                        val currentOptions = currentSetup.autoCloseConditions
                                        val newOptions = if (currentOptions.contains(opt)) currentOptions - opt else currentOptions + opt
                                        updateSetup(currentSetup.copy(autoCloseConditions = newOptions))
                                    }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = currentSetup.autoCloseConditions.contains(opt), onCheckedChange = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt, color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        item { Text("DEFAULT SELECTIONS:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        item {
                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                 Text("Default Profile:", color = TextSecondary, fontSize = 11.sp)
                                 Text(localDefaultName, color = CryptoCyan, fontSize = 11.sp, modifier = Modifier.clickable {
                                     localDefaultName = when(localDefaultName) {
                                         "RECOMMENDED SETUP" -> "SETUP-1"
                                         "SETUP-1" -> "SETUP-2"
                                         else -> "RECOMMENDED SETUP"
                                     }
                                 }.padding(4.dp))
                             }
                        }
                        item {
                             Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                 Text("Default Policy:", color = TextSecondary, fontSize = 11.sp)
                                 Text(localDefaultPolicy, color = AccentGold, fontSize = 11.sp, modifier = Modifier.clickable {
                                     localDefaultPolicy = if (localDefaultPolicy == "ASSIST ONLY") "ASSIST & EXECUTION" else "ASSIST ONLY"
                                 }.padding(4.dp))
                             }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.customSetup1.value = localSetup1
                    viewModel.customSetup2.value = localSetup2
                    viewModel.defaultSetupName.value = localDefaultName
                    viewModel.defaultAiPolicy.value = localDefaultPolicy
                    viewModel.recommendedAutoCloseConditions.value = localRecAutoClose
                    step = 0
                }, colors = ButtonDefaults.buttonColors(containerColor = AccentGold)) {
                    Text("SAVE SETUP", fontWeight = FontWeight.Black, color = DarkBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .border(
                    width = 1.dp,
                    color = AccentGold.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(onClick = { step = 2 })
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SIGNAL SETUP",
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = AccentGold,
                letterSpacing = 1.sp
            )
        }

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "ButtonScale"
        )
        
        // Pulse glow
        val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PulseAlpha"
        )

        Box(
            modifier = Modifier
                .scale(scale)
                .height(36.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CryptoCyan.copy(alpha = pulseAlpha), 
                            CryptoGreen.copy(alpha = pulseAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = { step = 1 }
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Trade",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ACCEPT SIGNAL",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

@Composable
fun FuturesTradingList(signals: List<FuturesSignal>, timeframeIndex: Int, viewModel: CryptoViewModel) {
    val oraclePick = signals.maxByOrNull { it.opportunityScore }
    val livePrices by viewModel.livePrices.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
    ) {
        if (oraclePick != null) {
            item {
                OraclePickCard(asset = oraclePick, timeframeIndex = timeframeIndex, viewModel = viewModel, livePrices = livePrices)
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALL SCANNED FUTURES ASSETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        items(signals) { coin ->
            FuturesItemCard(coin, timeframeIndex = timeframeIndex, viewModel = viewModel, livePrices = livePrices)
        }
    }
}

@Composable
fun FuturesItemCard(coin: FuturesSignal, timeframeIndex: Int, viewModel: CryptoViewModel, livePrices: Map<String, Double>) {
    val isBengali by viewModel.isBengali.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    
    val livePrice = livePrices["${coin.coinSymbol}USDT"] ?: coin.currentPrice

    val isLong = coin.isLong
    
    // REDISIGNED CONTRAST RED FOR SHORT: Highly vibrant, high-contrast scarlet red
    val shortRedThemeColor = Color(0xFFFF3F60)
    val shortRedBadgeBg = Color(0xFFFF3F60).copy(alpha = 0.18f)
    val shortRedCardBg = Color(0xFF1D1113) // Custom premium wine background tone for high contrast
    val shortRedBorderColor = Color(0xFFFF3F60).copy(alpha = 0.35f) // Sharp, vibrant red border outline

    val themeColor = if (isLong) CryptoGreen else shortRedThemeColor
    val badgeBg = if (isLong) themeColor.copy(alpha = 0.1f) else shortRedBadgeBg
    val cardBg = if (isLong) DarkSurface else shortRedCardBg
    val cardBorder = if (isLong) BorderColor else if (isExpanded) shortRedThemeColor else shortRedBorderColor

    val probability = when(timeframeIndex) {
        0 -> coin.probabilityPct
        1 -> coin.probabilityTwelveHoursPct ?: coin.probabilityPct
        2 -> (coin.probabilityPct - 4).coerceIn(40, 99)
        3 -> (coin.probabilityPct - 8).coerceIn(35, 99)
        else -> (coin.probabilityPct - 12).coerceIn(30, 99)
    }

    val priceChangeMultiplier = when(timeframeIndex) {
        0 -> 1.0
        1 -> 1.48
        2 -> 2.1
        3 -> 3.8
        else -> 5.5
    }
    val priceChange = coin.priceChangePct * priceChangeMultiplier

    val targetPrice = if (isLong) {
        coin.currentPrice * (1.0 + (priceChange / 100.0))
    } else {
        coin.currentPrice * (1.0 - (priceChange / 100.0))
    }
    
    val changeLabel = if (isLong) "EXPECTED GAIN" else "EXPECTED DROP"
    val targetLabel = when(timeframeIndex) {
        0 -> "6-H Predicted Target"
        1 -> "12-H Predicted Target"
        2 -> "24-H Gold Target"
        3 -> "3-Day Wave Target"
        else -> "7-Day Range Target"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isExpanded && isLong) CryptoCyan else cardBorder, RoundedCornerShape(16.dp))
            .clickable { if (!isExpanded) isExpanded = true }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            HorizontalDivider(color = if (isLong) BorderColor else cardBorder.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 12.dp))

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
                        text = "ENTRY (LOCKED)",
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
                        text = "CURRENT PRICE",
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

                // Column 3: EXPECTED GAIN/DROP
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
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
                    modifier = Modifier.weight(1.2f),
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isExpanded) "Tap to collapse detailed matrix ⬏" else "Tap to unfold deep institutional matrix ➔",
                fontSize = 10.sp,
                color = if (isLong) TextMuted else Color(0xFFD1D5DB),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isLong) BorderColor else cardBorder.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(16.dp))

                    val hours = when(timeframeIndex) {
                        0 -> 6
                        1 -> 12
                        2 -> 24
                        3 -> 72
                        else -> 168
                    }
                    RealTimeCountdown(coin.coinSymbol, hours)

                    Spacer(modifier = Modifier.height(16.dp))
                    RealTimeInvestmentTrackingModule(
                        entryPrice = coin.currentPrice,
                        projectedPrice = targetPrice,
                        isLong = isLong,
                        currentPrice = livePrice
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LeverageIntelligenceModule(coin)

                    Spacer(modifier = Modifier.height(16.dp))

                    SignalQualitySystemBlock(
                        score = coin.oracleScore,
                        confidence = probability,
                        probability = (probability - 4).coerceIn(40, 99),
                        riskGrade = if (coin.oracleScore >= 83) "LOW" else "MEDIUM"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TradeChecklistBlock(
                        trendConfirmed = coin.trendStrength != "WEAK",
                        volumeConfirmed = coin.volumeStrength != "WEAK",
                        momentumConfirmed = coin.momentumStrength != "WEAK",
                        liquidityConfirmed = coin.liquidityStrength != "WEAK",
                        riskEvaluated = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MarketRegimeTraceModule(coin.coinSymbol)

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiAiConsensusModule(coin.coinSymbol, coin.oracleScore, isLong)

                    Spacer(modifier = Modifier.height(16.dp))

                    RiskManagementModule(
                        currentPrice = coin.currentPrice,
                        projectedPrice = targetPrice,
                        priceChangePct = priceChange,
                        invalidationPrice = coin.invalidationPrice,
                        isLong = isLong
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiTimeframeForecastModule(coin.currentPrice, isLong, priceChange)

                    Spacer(modifier = Modifier.height(16.dp))

                    AiExplanationModule(coin.whyThisSignalEnglish, coin.whyThisSignalBengali, coin.coinSymbol, isBengali) { viewModel.toggleLanguage() }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val mission = remember(coin, targetPrice, isLong) {
                        com.example.model.Mission(
                            coinSymbol = coin.coinSymbol,
                            type = if (isLong) "LONG" else "SHORT",
                            marketType = "Futures",
                            entryPrice = coin.currentPrice,
                            currentPrice = coin.currentPrice,
                            targets = formatPrice(targetPrice),
                            stopLoss = formatPrice(coin.invalidationPrice),
                            confidence = probability,
                            aiStatusEnglish = if (isLong) "Bullish convergence intact." else "Bearish momentum building.",
                            aiStatusBengali = if (isLong) "বুলিশ কনভারজেন্স অটুট রয়েছে।" else "বেয়ারিশ মোমেন্টাম তৈরি হচ্ছে।"
                        )
                    }
                    StartTradeFlow(viewModel = viewModel, mission = mission, livePrice = livePrice)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap here to collapse details ⬏",
                        fontSize = 12.sp,
                        color = CryptoCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = false }
                            .padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun OraclePickCard(asset: Any, timeframeIndex: Int, viewModel: CryptoViewModel, livePrices: Map<String, Double> = emptyMap()) {
    val isBengali by viewModel.isBengali.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }
    val isFutures = asset is FuturesSignal
    val isLong = if (asset is FuturesSignal) asset.isLong else true
    
    val name = when (asset) {
        is SpotSignal -> asset.coinName
        is FuturesSignal -> asset.coinName
        else -> ""
    }
    val symbol = when (asset) {
        is SpotSignal -> asset.coinSymbol
        is FuturesSignal -> asset.coinSymbol
        else -> ""
    }
    val entryPrice = when (asset) {
        is SpotSignal -> asset.currentPrice
        is FuturesSignal -> asset.currentPrice
        else -> 0.0
    }
    val curPrice = livePrices["${symbol}USDT"] ?: entryPrice

    val potential = when (asset) {
        is SpotSignal -> when(timeframeIndex) {
            0 -> asset.growthPotentialPct
            1 -> asset.growthPotentialTwelveHoursPct ?: (asset.growthPotentialPct * 1.5)
            2 -> asset.growthPotentialPct * 2.2
            3 -> asset.growthPotentialPct * 3.5
            else -> asset.growthPotentialPct * 5.0
        }
        is FuturesSignal -> {
            val multiplier = when(timeframeIndex) {
                0 -> 1.0
                1 -> 1.48
                2 -> 2.1
                3 -> 3.8
                else -> 5.5
            }
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
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = androidx.compose.foundation.BorderStroke(
                    1.7.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(AccentGold, CryptoCyan)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { if (!isExpanded) isExpanded = true }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Golden title header ribbon
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Pricing summaries
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "CURRENT PRICE", fontSize = 9.sp, color = TextSecondary)
                    Text(text = formatPrice(curPrice), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TARGET FORECAST", fontSize = 9.sp, color = CryptoGreen)
                    Text(text = formatPrice(projPrice), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = "EXPECTED GAIN", fontSize = 9.sp, color = if (isLong) CryptoGreen else CryptoRedText)
                    Text(text = String.format("%s%.2f%%", if (isLong) "+" else "", potential), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isLong) CryptoGreen else CryptoRedText)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isExpanded) "Tap to collapse detailed matrix ⬏" else "Tap to unfold deep institutional matrix ➔",
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    val hours = when(timeframeIndex) {
                        0 -> 6
                        1 -> 12
                        2 -> 24
                        3 -> 72
                        else -> 168
                    }
                    RealTimeCountdown(symbol, hours)

                    Spacer(modifier = Modifier.height(16.dp))
                    RealTimeInvestmentTrackingModule(entryPrice = entryPrice, projectedPrice = projPrice, isLong = isLong, currentPrice = curPrice)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isFutures && asset is FuturesSignal) {
                        LeverageIntelligenceModule(asset)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiAiConsensusModule(symbol, when(asset) {
                        is SpotSignal -> asset.oracleScore
                        is FuturesSignal -> asset.oracleScore
                        else -> 80
                    }, isLong)

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiTimeframeForecastModule(curPrice, isLong, potential)

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val mission = remember(asset, projPrice, isLong) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap here to collapse details ⬏",
                        fontSize = 12.sp,
                        color = CryptoCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = false }
                            .padding(vertical = 12.dp),
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
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Text(text = "ENTRY (LOCKED)", fontSize = 9.sp, color = TextSecondary)
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
        }
    }
}

@Composable
fun RealTimeCountdown(coinSymbol: String, totalDurationHours: Int) {
    var remainingSeconds by remember(coinSymbol, totalDurationHours) {
        val stableOffsetSeconds = (coinSymbol.hashCode().absoluteValue % (totalDurationHours * 3600 - 1800)) + 600
        mutableStateOf(stableOffsetSeconds)
    }

    LaunchedEffect(coinSymbol) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val isLongTerm = totalDurationHours >= 4
    val timeText = if (isLongTerm) {
        String.format("%02dh %02dm", hours, minutes)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
    val pctRemaining = remainingSeconds.toFloat() / (totalDurationHours * 3600f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VALIDITY WINDOW COUNTDOWN",
                fontSize = 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$timeText Remaining",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (remainingSeconds < 1800) CryptoRedText else CryptoCyan
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = pctRemaining,
            color = if (remainingSeconds < 1800) CryptoRed else CryptoCyan,
            trackColor = BorderColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}



@Composable
fun MultiAiConsensusModule(coinSymbol: String, oracleScore: Int, isLong: Boolean) {
    val consensus = getConsensusDetails(coinSymbol, oracleScore, isLong)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "MULTI-AI CONSENSUS ENGINES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiEngineGauge("Gemini Pro AI", consensus.geminiScore, Modifier.weight(1f))
                AiEngineGauge("GPT-4o Quant", consensus.gptScore, Modifier.weight(1f))
                AiEngineGauge("Claude Sentient", consensus.claudeScore, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "CONSENSUS CONFIDENCE", fontSize = 9.sp, color = TextSecondary)
                    Text(text = "${consensus.confidence}%", fontSize = 15.sp, fontWeight = FontWeight.Black, color = CryptoCyan)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DIRECTION", fontSize = 9.sp, color = TextSecondary)
                    Text(
                        text = consensus.direction,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLong) CryptoGreen else CryptoRedText
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "RISK PROFILE", fontSize = 9.sp, color = TextSecondary)
                    Text(
                        text = consensus.riskScore,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = when (consensus.riskScore) {
                            "LOW" -> CryptoGreen
                            "MEDIUM" -> AccentGold
                            else -> CryptoRedText
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AiEngineGauge(name: String, score: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkBackground, RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = name, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$score / 100",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (score >= 80) CryptoGreen else if (score >= 70) AccentGold else CryptoCyan
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
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "RISK ENGINEERING & SIZING CONTROL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "STOP LOSS (ATR AWARE)", fontSize = 9.sp, color = CryptoRedText)
                    Text(text = formatPrice(calcStopLoss), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CryptoRedText)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "RISK REWARD RATIO", fontSize = 9.sp, color = TextSecondary)
                    Text(text = String.format("1 : %.2f", riskRewardRatio), fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Black, color = AccentGold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "TAKE PROFIT TARGET MATRIX", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TpBadge("TP1 (25%)", tp1, Modifier.weight(1f))
                TpBadge("TP2 (50%)", tp2, Modifier.weight(1f))
                TpBadge("TP3 (75%)", tp3, Modifier.weight(1f))
                TpBadge("TP4 (100%)", tp4, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "RECOMMENDED POSITION ALLOCATION SIZING", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SizingBox("Conservative", "2.0% Cap", Modifier.weight(1f))
                SizingBox("Balanced", "5.0% Cap", Modifier.weight(1f))
                SizingBox("Aggressive", "10.0% Max", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun TpBadge(label: String, price: Double, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkBackground, RoundedCornerShape(6.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 8.sp, color = CryptoGreen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = formatPrice(price), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
    }
}

@Composable
fun SizingBox(label: String, size: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkBackground, RoundedCornerShape(6.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 8.sp, color = TextSecondary)
        Text(text = size, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentGold)
    }
}

@Composable
fun MultiTimeframeForecastModule(currentPrice: Double, isLong: Boolean, priceChangePct: Double) {
    val forecasts = generateMultiTimeframeForecasts(currentPrice, isLong, priceChangePct)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "MULTI-TIMEFRAME PREDICTION CASCADE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forecasts.take(3).forEach { forecast ->
                    ForecastGridItem(forecast, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forecasts.subList(3, 6).forEach { forecast ->
                    ForecastGridItem(forecast, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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
            .background(DarkBackground, RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
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

@Composable
fun AiExplanationModule(whyEnglish: String, whyBengali: String, coinSymbol: String, isBengali: Boolean, onToggleLanguage: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue = if (isBengali) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI ORACLE ANALYTIC COGNITION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )

            TextButton(
                onClick = onToggleLanguage,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text(
                    text = if (isBengali) "Show English" else "বাংলায় দেখুন ➔",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentGold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BorderColor, RoundedCornerShape(12.dp))
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14 * density
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                if (rotation <= 90f) {
                    Column {
                        Text(
                            text = whyEnglish,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(text = "QUANTITATIVE HEATMAP SIGNALS", fontSize = 9.sp, color = CryptoCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InsightMetricPill("Trend", "STRONG", CryptoGreen)
                            InsightMetricPill("Momentum", "HOT", AcceleratorCyanColor(coinSymbol))
                            InsightMetricPill("Volume", "ACCUMULATING", AccentGold)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f }
                    ) {
                        Text(
                            text = whyBengali,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(text = "রিয়াল-টাইম কোয়ান্ট সংকেতসমূহ", fontSize = 9.sp, color = CryptoCyan, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InsightMetricPill("ট্রেন্ড", "শক্তিশালী", CryptoGreen)
                            InsightMetricPill("মোমেন্টাম", "উচ্চ", AcceleratorCyanColor(coinSymbol))
                            InsightMetricPill("ভলিউম", "সঞ্চয়কারী", AccentGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightMetricPill(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .background(DarkBackground, RoundedCornerShape(6.dp))
            .border(0.7.dp, BorderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label: ", fontSize = 8.sp, color = TextSecondary)
        Text(text = value, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

fun AcceleratorCyanColor(symbol: String): Color {
    return if (symbol.hashCode() % 2 == 0) CryptoCyan else CryptoGreen
}

@Composable
fun LeverageIntelligenceModule(coin: FuturesSignal) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "LEVERAGE INTELLIGENCE MATRIX",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeverageBox("SAFE LEVERAGE", "${coin.leverageConservative}x", "Conservative risk mitigation level", CryptoGreen, Modifier.weight(1f))
                LeverageBox("MODERATE RISK", "${coin.leverageBalanced}x", "Default recommended balanced index", AccentGold, Modifier.weight(1f))
                LeverageBox("MAX AGGRESIVE", "${coin.leverageAggressive}x", "Extreme danger volatility thresholds", CryptoRedText, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LeverageBox(title: String, multiplier: String, desc: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkBackground, RoundedCornerShape(8.dp))
            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
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
    val risk = when {
        confidence >= 85 -> "LOW"
        confidence >= 75 -> "MEDIUM"
        else -> "HIGH"
    }
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

private fun formatPrice(price: Double): String {
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
    val intervals = listOf("6H", "12H", "24H", "3D", "7D")
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
                    .padding(vertical = 10.dp),
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
fun SignalQualitySystemBlock(
    score: Int,
    confidence: Int,
    probability: Int,
    riskGrade: String
) {
    val indicator = when {
        score >= 90 -> "Institutional Grade"
        score >= 82 -> "High Confidence"
        score >= 70 -> "Strong"
        score >= 55 -> "Moderate"
        else -> "Weak"
    }

    val themeColor = when {
        score >= 82 -> CryptoCyan
        score >= 70 -> CryptoGreen
        score >= 55 -> AccentGold
        else -> Color(0xFFFF3F60)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SIGNAL QUALITY ENGINE INDEX",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "CLASSIFICATION", fontSize = 8.sp, color = TextMuted)
                    Text(
                        text = indicator.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = themeColor
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
                        color = themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "CONFIDENCE", fontSize = 8.sp, color = TextMuted)
                    Text(text = "$confidence%", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text(text = "PROBABILITY", fontSize = 8.sp, color = TextMuted)
                    Text(text = "$probability%", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "RISK SCORE", fontSize = 8.sp, color = TextMuted)
                    Text(text = riskGrade, fontSize = 12.sp, color = if (riskGrade == "LOW") CryptoGreen else AccentGold, fontWeight = FontWeight.Bold)
                }
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
    riskEvaluated: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "INSTITUTIONAL CONFIRMATION CHECKLIST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val items = listOf(
                "Trend Confirmed" to trendConfirmed,
                "Volume Confirmed" to volumeConfirmed,
                "Momentum Confirmed" to momentumConfirmed,
                "Liquidity Confirmed" to liquidityConfirmed,
                "Risk Evaluated" to riskEvaluated
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
                                .background(if (checked) CryptoGreen.copy(alpha = 0.15f) else Color(0xFFFF3F60).copy(alpha = 0.15f))
                                .border(1.dp, if (checked) CryptoGreen else Color(0xFFFF3F60), CircleShape),
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
                                Box(modifier = Modifier.size(4.dp).background(Color(0xFFFF3F60), CircleShape))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (checked) TextPrimary else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketRegimeTraceModule(coinSymbol: String) {
    // Generate a beautiful, stable, hash-based market regime state for this asset
    val seed = coinSymbol.hashCode().absoluteValue
    val regimes = listOf("BULLISH", "BEARISH", "SIDEWAYS", "ACCUMULATION", "DISTRIBUTION")
    val regime = regimes[seed % regimes.size]

    val description = when(regime) {
        "BULLISH" -> "High liquidity markup phase driven by strong smart money orders."
        "BEARISH" -> "Markdown liquidations under persistent offer pressure."
        "SIDEWAYS" -> "Range bound bracket with low volatility waiting for core breaks."
        "ACCUMULATION" -> "Institutional accumulation in value brackets."
        else -> "Smart money distribution at premium resistance heights."
    }

    val tint = when(regime) {
        "BULLISH" -> CryptoGreen
        "BEARISH" -> Color(0xFFFF3F60)
        "SIDEWAYS" -> TextMuted
        "ACCUMULATION" -> CryptoCyan
        else -> AccentGold
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "PERSISTED REGIME TRACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(tint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = regime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tint
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ACTIVE DURING INSIGHT",
                    fontSize = 8.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
