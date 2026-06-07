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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding

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
                .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
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
    val isBengali by viewModel.isBengali.collectAsState()

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
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
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
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.wrapContentWidth()
            ) {
                IconButton(
                    onClick = { viewModel.runScanner() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CryptoCyan.copy(alpha = 0.18f),
                                    Color(0xFF050A13)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(1.dp, BorderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-run scanner",
                        tint = CryptoCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                SignalProLanguageSwitchButton(
                    isBengali = isBengali,
                    onClick = { viewModel.toggleLanguage() }
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
                .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
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
fun SignalProLanguageSwitchButton(
    isBengali: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkSurface,
            contentColor = CryptoCyan
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .height(36.dp)
            .width(82.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (isBengali) "English" else "বাংলা",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Clip
        )
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
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
                    RealTimeCountdown(coin.coinSymbol, hours, isBengali)
                    
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
                    ,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(16.dp))

                    TradeChecklistBlock(
                        trendConfirmed = coin.trendStrength != "WEAK",
                        volumeConfirmed = coin.volumeStrength != "WEAK",
                        momentumConfirmed = coin.momentumStrength != "WEAK",
                        liquidityConfirmed = coin.liquidityStrength != "WEAK",
                        riskEvaluated = true
                    ,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(16.dp))

                    MarketRegimeTraceModule(coin.coinSymbol,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiAiConsensusModule(coin.coinSymbol, coin.oracleScore, true, isBengali)

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
            .padding(horizontal = 11.dp, vertical = 9.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 13.5.sp,
            color = TextPrimary,
            lineHeight = 18.sp
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTradeFlow(
    viewModel: CryptoViewModel,
    mission: com.example.model.Mission,
    livePrice: Double = mission.entryPrice
) {
    var step by remember { mutableStateOf(0) }
    var showDecisionBrief by remember { mutableStateOf(false) }
    val isBengali by viewModel.isBengali.collectAsState()
    val decisionBriefSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val highConfidence = mission.confidence >= 85
    val isLong = mission.type.uppercase() == "LONG"

    val recommendationText = when {
        isBengali && highConfidence && isLong -> "উচ্চ আস্থা | এন্ট্রি যাচাই"
        isBengali && highConfidence && !isLong -> "উচ্চ আস্থা | শর্ট যাচাই"
        isBengali && !highConfidence -> "সতর্কভাবে যাচাই করুন"
        !isBengali && highConfidence -> "High Confidence | Verify Entry"
        else -> "Review Carefully | Verify Entry"
    }

    val verdictText = when {
        isBengali && highConfidence -> "সিগন্যাল শক্তিশালী, তবে এন্ট্রি যাচাই করে নিন।"
        isBengali -> "সিগন্যাল কার্যকর, তবে ঝুঁকি যাচাই করা জরুরি।"
        highConfidence -> "Signal is strong, but entry confirmation is still required."
        else -> "Signal is active, but risk review is required before action."
    }

    val whyText = if (isBengali) {
        "ট্রেন্ড, মতিগতি, লেনদেন, AI consensus এবং risk profile মিলিয়ে এই setup তৈরি হয়েছে।"
    } else {
        "This setup combines trend, momentum, volume, AI consensus, and risk profile signals."
    }

    val riskText = if (isBengali) {
        if (highConfidence) "রিস্ক কম থেকে মাঝারি। Stop loss এবং position size মেনে চলুন।"
        else "রিস্ক মাঝারি। দেরিতে entry নিলে signal quality কমতে পারে।"
    } else {
        if (highConfidence) "Risk is low to medium. Follow stop loss and position sizing."
        else "Risk is medium. Late entry may reduce signal quality."
    }

    val actionText = if (isBengali) {
        if (isLong) "এন্ট্রি price, stop loss এবং target মিলিয়ে তারপর Accept Signal করুন।"
        else "শর্ট এন্ট্রি, stop loss এবং target মিলিয়ে তারপর Accept Signal করুন।"
    } else {
        if (isLong) "Verify entry price, stop loss, and target before accepting the signal."
        else "Verify short entry, stop loss, and target before accepting the signal."
    }

    val disclaimerText = if (isBengali) {
        "এআই সিদ্ধান্তে সহায়তা করে; চূড়ান্ত ট্রেডিং সিদ্ধান্ত আপনার।"
    } else {
        "AI assists decision-making; the final trading decision is yours."
    }

    if (showDecisionBrief) {
        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            sheetState = decisionBriefSheetState,
            containerColor = Color(0xFF030712),
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBengali) "AI সিদ্ধান্ত সংক্ষেপ" else "AI Decision Brief",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoCyan
                )

                Text(
                    text = if (isBengali) "দ্রুত সিদ্ধান্ত নেওয়ার জন্য সংক্ষিপ্ত সারাংশ" else "Compact signal summary for faster decision-making",
                    fontSize = 12.5.sp,
                    color = TextSecondary
                )

                DecisionBriefBlock(
                    title = if (isBengali) "সিগন্যাল রায়" else "Signal Verdict",
                    value = verdictText,
                    accentColor = if (highConfidence) CryptoGreen else AccentGold
                )

                DecisionBriefBlock(
                    title = if (isBengali) "কেন গুরুত্বপূর্ণ" else "Why It Matters",
                    value = whyText,
                    accentColor = CryptoCyan
                )

                DecisionBriefBlock(
                    title = if (isBengali) "ঝুঁকির সতর্কতা" else "Risk Warning",
                    value = riskText,
                    accentColor = AccentGold
                )

                DecisionBriefBlock(
                    title = if (isBengali) "প্রস্তাবিত কাজ" else "Suggested Action",
                    value = actionText,
                    accentColor = CryptoGreen
                )

                Text(
                    text = disclaimerText,
                    fontSize = 11.5.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = { showDecisionBrief = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBengali) "বন্ধ করুন" else "Close",
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            showDecisionBrief = false
                            step = 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isBengali) "সিগন্যাল নিন" else "Accept Signal",
                            color = DarkBackground,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (step == 1) {
        val verifiedEntryLocked = remember { livePrice }

        AlertDialog(
            onDismissRequest = { step = 0 },
            title = {
                Text(
                    text = if (isBengali) "ট্রেড যাচাই করুন" else "Verify Trade Details",
                    color = CryptoCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBengali) "দিক: ${mission.type} (${mission.marketType})" else "Direction: ${mission.type} (${mission.marketType})",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) "লকড এন্ট্রি প্রাইস:" else "Locked Entry Price:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format("$%.4f", verifiedEntryLocked),
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBengali) {
                            "Accept করার পর এই entry personal mission হিসেবে track হবে।"
                        } else {
                            "Once accepted, this entry will activate personal mission tracking."
                        },
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startMission(
                            mission.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                entryPrice = verifiedEntryLocked,
                                startTime = System.currentTimeMillis()
                            )
                        )
                        viewModel.sendLocalAlert("Mission Started", "AI intelligence system successfully started monitoring ${mission.coinSymbol}")
                        step = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                ) {
                    Text(
                        text = if (isBengali) "মিশন চালু করুন" else "CONFIRM MISSION",
                        fontWeight = FontWeight.Black,
                        color = DarkBackground
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) {
                    Text(
                        text = if (isBengali) "বাতিল" else "Cancel",
                        color = TextSecondary
                    )
                }
            },
            containerColor = Color(0xFF030712),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "AcceptFlowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val recoSweepX by infiniteTransition.animateFloat(
        initialValue = -650f,
        targetValue = 650f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RecommendationSweepX"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF02050D),
                            Color(0xFF0B1220),
                            Color(0xFF02050D)
                        )
                    )
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            CryptoCyan.copy(alpha = 0.10f),
                            CryptoGreen.copy(alpha = 0.08f),
                            CryptoCyan.copy(alpha = 0.10f)
                        )
                    )
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            CryptoCyan.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.12f),
                            CryptoGreen.copy(alpha = 0.26f),
                            Color.Transparent
                        ),
                        startX = recoSweepX,
                        endX = recoSweepX + 520f
                    )
                )
                .border(0.8.dp, CryptoCyan.copy(alpha = 0.66f), RoundedCornerShape(10.dp))
                .clickable { showDecisionBrief = true }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = recommendationText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF4F8FF),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .scale(scale)
                .height(40.dp)
                .widthIn(min = 128.dp, max = 164.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CryptoCyan.copy(alpha = pulseAlpha),
                            CryptoGreen.copy(alpha = pulseAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = { step = 1 }
                )
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Trade",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "ACCEPT SIGNAL",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    softWrap = false
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
                    RealTimeCountdown(coin.coinSymbol, hours, isBengali)

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
                    ,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(16.dp))

                    TradeChecklistBlock(
                        trendConfirmed = coin.trendStrength != "WEAK",
                        volumeConfirmed = coin.volumeStrength != "WEAK",
                        momentumConfirmed = coin.momentumStrength != "WEAK",
                        liquidityConfirmed = coin.liquidityStrength != "WEAK",
                        riskEvaluated = true
                    ,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(16.dp))

                    MarketRegimeTraceModule(coin.coinSymbol,
                        isBengali = isBengali)

                    Spacer(modifier = Modifier.height(16.dp))

                    MultiAiConsensusModule(coin.coinSymbol, coin.oracleScore, isLong, isBengali)

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
                            aiStatusBengali = if (isLong) "দাম বাড়ছে কনভারজেন্স অটুট রয়েছে।" else "দাম কমছে মোমেন্টাম তৈরি হচ্ছে।"
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
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
                    RealTimeCountdown(symbol, hours, isBengali)

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
                    }, isLong, isBengali)

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
            .padding(horizontal = 12.dp, vertical = 10.dp)
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
            .height(58.dp)
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
            .padding(horizontal = 6.dp),
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
                color = CryptoGreen,
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
            .heightIn(min = 50.dp)
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

        Spacer(modifier = Modifier.height(3.dp))

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
        if (isLong) "দাম বাড়ছে" else "দাম কমছে"
    } else {
        if (isLong) "BULLISH" else "BEARISH"
    }

    val riskText = if (isBengali) {
        if (oracleScore >= 85) "কম" else "মাঝারি"
    } else {
        if (oracleScore >= 85) "LOW" else "MEDIUM"
    }

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
                .padding(14.dp)
        ) {
            Text(
                text = if (isBengali) "মাল্টি-এআই ঐকমত্য ইঞ্জিন" else "MULTI-AI CONSENSUS ENGINES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = CryptoCyan,
                letterSpacing = if (isBengali) 0.sp else 1.2.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AiScoreTile("Gemini Pro AI", geminiScore, Modifier.weight(1f))
                AiScoreTile("GPT-4o Quant", gptScore, Modifier.weight(1f))
                AiScoreTile("Claude Sentient", claudeScore, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConsensusMetricColumn(
                    label = if (isBengali) "ঐকমত্যের আস্থা" else "CONSENSUS CONFIDENCE",
                    value = "$consensusScore%",
                    valueColor = CryptoCyan,
                    modifier = Modifier.weight(1.25f)
                )

                ConsensusMetricColumn(
                    label = if (isBengali) "দিকনির্দেশ" else "DIRECTION",
                    value = directionText,
                    valueColor = CryptoGreen,
                    modifier = Modifier.weight(1.05f)
                )

                ConsensusMetricColumn(
                    label = if (isBengali) "রিস্ক প্রোফাইল" else "RISK PROFILE",
                    value = riskText,
                    valueColor = CryptoGreen,
                    modifier = Modifier.weight(1.0f)
                )
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier.fillMaxWidth().border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp))
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
            .border(0.75.dp, CryptoCyan.copy(alpha = 0.36f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = TextSecondary,
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
            color = AccentGold,
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

        Spacer(modifier = Modifier.height(8.dp))

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
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (rotation <= 90f) {
                    Column {
                        Text(
                            text = whyEnglish,
                            fontSize = 13.sp,
                            color = Color(0xFFF4F8FF),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "QUANTITATIVE HEATMAP SIGNALS",
                            fontSize = 9.sp,
                            color = CryptoCyan,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

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

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "পরিমাণগত হিটম্যাপ সিগন্যাল",
                            fontSize = 9.sp,
                            color = CryptoCyan,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

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
            .height(48.dp)
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
            .padding(horizontal = 5.dp, vertical = 6.dp),
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
            .background(Color(0xFF050812), RoundedCornerShape(8.dp))
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
fun QualityMetricColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.heightIn(min = 42.dp),
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
        "HIGH" -> if (isBengali) "তীব্র" else "HIGH"
        "EXTREME" -> if (isBengali) "খুব বেশি" else "EXTREME"
        else -> riskGrade
    }

    val riskColor = when (riskGrade.uppercase()) {
        "LOW" -> CryptoGreen
        "MEDIUM" -> AccentGold
        "HIGH", "EXTREME" -> Color(0xFFFF3F60)
        else -> AccentGold
    }

    val themeColor = when {
        score >= 82 -> CryptoCyan
        score >= 70 -> CryptoGreen
        score >= 55 -> AccentGold
        else -> Color(0xFFFF3F60)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF050A13)),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.95.dp, CryptoCyan.copy(alpha = 0.62f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "সিগন্যাল মান যাচাই সূচক" else "SIGNAL QUALITY ENGINE INDEX",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QualityMetricColumn(
                    label = if (isBengali) "আস্থা" else "CONFIDENCE",
                    value = "$confidence%",
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                QualityMetricColumn(
                    label = if (isBengali) "সম্ভাবনা" else "PROBABILITY",
                    value = "$probability%",
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                QualityMetricColumn(
                    label = if (isBengali) "রিস্ক" else "RISK SCORE",
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
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "ইনস্টিটিউশনাল নিশ্চিতকরণ তালিকা" else "INSTITUTIONAL CONFIRMATION CHECKLIST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

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
        "BULLISH" -> if (isBengali) "দাম বাড়ার ভাব" else "BULLISH"
        "BEARISH" -> if (isBengali) "মন্দা ভাব" else "BEARISH"
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
        "BEARISH" -> Color(0xFFFF3F60)
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
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (isBengali) "চলতি বাজারের মতিগতি" else "PERSISTED REGIME TRACE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = if (isBengali) 0.sp else 1.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

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
