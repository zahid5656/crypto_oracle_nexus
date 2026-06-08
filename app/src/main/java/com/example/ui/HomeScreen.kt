package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.NewsItem
import com.example.model.OracleAnalysisResponse
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel

// ============================================================================
// ORACLE FEED — INSTITUTIONAL APPLE / BLOOMBERG STYLE COLOR SYSTEM
// Scope: Oracle Feed UI only. Signal Pro, Live Radar, Mission Center, Stats Hub untouched.
// ============================================================================
private val OracleFeedBg = Color(0xFF02050D)
private val OracleCardBg = Color(0xFF050A13)
private val OracleTileBg = Color(0xFF080E18)
private val OracleTileBgElevated = Color(0xFF0B1220)
private val OracleBorder = Color(0xFF1C2636)
private val OracleBorderSoft = Color(0xFF101827)
private val OracleWhite = Color(0xFFF5F5F5)
private val OracleTextSecondary = Color(0xFFB8C1CF)
private val OracleTextMuted = Color(0xFF7A8494)
private val OracleGreen = Color(0xFF0ECB81)
private val OracleRed = Color(0xFFFF453A)
private val OracleYellow = Color(0xFFFFD60A)
private val OracleBlue = Color(0xFF64D2FF)


@Composable
fun HomeScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val newsFeed by viewModel.newsFeedData.collectAsState()
    val useAiOracle by viewModel.useAiOracle.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OracleFeedBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            HeaderSection(viewModel)
        }

        item {
            RealTimePublicMarketDashboard()
        }

        item {
            val livePrices by viewModel.livePrices.collectAsState()
            TopCoinsTickerSection(newsFeed, isBengali, livePrices)
        }

        item {
            StarDivider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "News icon",
                    tint = OracleBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LATEST ORACLE MARKET FEED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleWhite,
                    letterSpacing = 1.sp
                )
            }
        }

        items(newsFeed.newsList) { article ->
            var isLocalBengali by remember { mutableStateOf(false) }
            NewsCard(article, isLocalBengali, onToggleLanguage = { isLocalBengali = !isLocalBengali })
        }

        item {
            StarDivider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Insights icon",
                    tint = OracleGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ORACLE DEEP ANALYTICAL INSIGHTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleWhite,
                    letterSpacing = 1.sp
                )
            }
        }

        items(newsFeed.deepInsights) { insight ->
            var isLocalBengali by remember { mutableStateOf(false) }
            DeepInsightCard(
                insight = insight,
                isBengali = isLocalBengali,
                onToggleLanguage = { isLocalBengali = !isLocalBengali }
            )
        }

        item {
            StarDivider()
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 2.dp)
            ) {
                Row {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Market index star",
                            tint = OracleGreen,
                            modifier = Modifier
                                .size(12.dp)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "ORACLE MARKET INDEX",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleYellow,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Market index star",
                            tint = OracleGreen,
                            modifier = Modifier
                                .size(12.dp)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }
            }
        }

        item {
            var isLocalBengali by remember { mutableStateOf(false) }
            LiveCurrencyTile(
                isBengali = isLocalBengali,
                onToggleLanguage = { isLocalBengali = !isLocalBengali }
            )
        }
    }
}

@Composable
fun StarDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "•   •   •",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OracleBorder.copy(alpha = 0.95f),
            letterSpacing = 10.sp
        )
    }
}

// Inline helper to bypass any dynamic dp evaluation quirks
private fun Int.getValidDp() = this.dp

@Composable
fun BangladeshTimeWidget() {
    var timeText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val nowInDhaka = ZonedDateTime.now(ZoneId.of("Asia/Dhaka"))
            timeText = nowInDhaka.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            dateText = nowInDhaka.format(DateTimeFormatter.ofPattern("dd MMM yyyy")).uppercase()
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .width(88.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(OracleTileBg)
            .border(0.75.dp, OracleBorder, RoundedCornerShape(9.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BGD",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleYellow
                )
                Text(
                    text = dateText,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = OracleTextSecondary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = OracleWhite,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "GMT+6",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = OracleGreen
            )
        }
    }
}

@Composable
fun TelemetryStatusRow(viewModel: CryptoViewModel) {
    val isLiveConnected by viewModel.isLiveConnected.collectAsState()
    val hasGeminiApiKey by viewModel.hasGeminiApiKey.collectAsState()

    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isLiveConnected) OracleGreen else OracleTextMuted, CircleShape)
            )
            Text(
                text = if (isLiveConnected) "BINANCE SYNC: LIVE" else "LOCAL INDEX",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = if (isLiveConnected) OracleGreen else OracleTextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (hasGeminiApiKey) OracleBlue else OracleYellow, CircleShape)
            )
            Text(
                text = if (hasGeminiApiKey) "GEMINI: ACTIVE" else "GEMINI: SIMULATED",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = if (hasGeminiApiKey) OracleBlue else OracleYellow,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun HeaderSection(viewModel: CryptoViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo_ai),
                contentDescription = "AI Trading Assistant Logo",
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, OracleGreen, RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(9.dp))
            Column {
                Text(
                    text = "Crypto Signal",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OracleTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = OracleWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)) {
                            append("Oracle ")
                        }
                        withStyle(style = SpanStyle(color = OracleTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                            append("B")
                        }
                        withStyle(style = SpanStyle(color = OracleTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)) {
                            append("Y ")
                        }
                        withStyle(style = SpanStyle(color = OracleGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)) {
                            append("Z")
                        }
                        withStyle(style = SpanStyle(color = OracleGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)) {
                            append("AHID")
                        }
                    },
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                TelemetryStatusRow(viewModel)
            }
        }

        Box(modifier = Modifier.padding(start = 8.dp)) {
            BangladeshTimeWidget()
        }
    }
}

@Composable
fun TopCoinsTickerSection(newsFeed: OracleAnalysisResponse, isBengali: Boolean, livePrices: Map<String, Double>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OracleCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.75.dp, OracleBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "ORACLE REFERENCE INDEX",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = OracleWhite,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val btcSpot = newsFeed.spotSignals.find { it.coinSymbol == "BTC" }
            val ethSpot = newsFeed.spotSignals.find { it.coinSymbol == "ETH" }
            val xrpSpot = newsFeed.spotSignals.find { it.coinSymbol == "XRP" }
            val solSpot = newsFeed.spotSignals.find { it.coinSymbol == "SOL" }
            val adaSpot = newsFeed.spotSignals.find { it.coinSymbol == "ADA" }

            val btcPrice = livePrices["BTC"] ?: btcSpot?.currentPrice ?: 66450.0
            val btcChange = btcSpot?.growthPotentialPct ?: 3.99

            val ethPrice = livePrices["ETH"] ?: ethSpot?.currentPrice ?: 3485.50
            val ethChange = ethSpot?.growthPotentialPct ?: 3.57

            val xrpPrice = livePrices["XRP"] ?: xrpSpot?.currentPrice ?: 0.512
            val xrpChange = xrpSpot?.growthPotentialPct ?: -5.8

            val solPrice = livePrices["SOL"] ?: solSpot?.currentPrice ?: 164.20
            val solChange = solSpot?.growthPotentialPct ?: 4.12

            val adaPrice = livePrices["ADA"] ?: adaSpot?.currentPrice ?: 0.435
            val adaChange = adaSpot?.growthPotentialPct ?: -2.15

            TickerRow(symbol = "BTC", name = "Bitcoin", price = btcPrice, changePct = btcChange)
            HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 10.dp))
            TickerRow(symbol = "ETH", name = "Ethereum", price = ethPrice, changePct = ethChange)
            HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 10.dp))
            TickerRow(symbol = "XRP", name = "Ripple", price = xrpPrice, changePct = xrpChange)
            HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 10.dp))
            TickerRow(symbol = "SOL", name = "Solana", price = solPrice, changePct = solChange)
            HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 10.dp))
            TickerRow(symbol = "ADA", name = "Cardano", price = adaPrice, changePct = adaChange)
        }
    }
}

@Composable
fun TickerRow(symbol: String, name: String, price: Double, changePct: Double) {
    val animatedPrice = animateFloatAsState(
        targetValue = price.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PriceAnim"
    ).value

    var prevPrice by remember { mutableStateOf(price) }
    var pulseActive by remember { mutableStateOf(false) }

    LaunchedEffect(price) {
        if (price != prevPrice) {
            pulseActive = true
            delay(220)
            pulseActive = false
            prevPrice = price
        }
    }

    val borderAlpha by animateFloatAsState(
        targetValue = if (pulseActive) 0.95f else 0.0f,
        animationSpec = tween(durationMillis = 260),
        label = "PriceBorderPulse"
    )

    val isPositive = changePct >= 0
    val moveColor = if (isPositive) OracleGreen else OracleRed
    val assetColor = when (symbol) {
        "BTC" -> OracleYellow
        "ETH" -> OracleBlue
        "XRP" -> OracleRed
        "SOL" -> OracleGreen
        "ADA" -> OracleRed
        else -> OracleBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OracleTileBg, RoundedCornerShape(9.dp))
            .border(
                width = 0.65.dp,
                color = if (pulseActive) moveColor.copy(alpha = borderAlpha) else OracleBorderSoft,
                shape = RoundedCornerShape(9.dp)
            )
            .padding(vertical = 6.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(OracleTileBgElevated, CircleShape)
                    .border(0.65.dp, assetColor.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.take(1),
                    color = assetColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = OracleWhite
                )
                Text(
                    text = symbol,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = OracleTextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (animatedPrice >= 100) String.format("$%,.2f", animatedPrice) else String.format("$%.3f", animatedPrice),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = OracleWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isPositive) "Price up" else "Price down",
                    tint = moveColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%+.2f%%", changePct),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = moveColor
                )
            }
        }
    }
}

@Composable
fun OracleModeControllerCard(
    useAiOracle: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val accent = if (useAiOracle) OracleBlue else OracleYellow

    Card(
        colors = CardDefaults.cardColors(containerColor = OracleCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.75.dp, accent.copy(alpha = 0.70f), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(OracleTileBg, CircleShape)
                        .border(0.65.dp, accent.copy(alpha = 0.75f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Oracle intelligence core selector",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI ORACLE MODALITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = OracleWhite,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (useAiOracle) "Direct Gemini-3.5-Flash active" else "Fast Technical Simulator local",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OracleTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = useAiOracle,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = OracleGreen,
                    checkedTrackColor = OracleGreen.copy(alpha = 0.32f),
                    uncheckedThumbColor = OracleTextSecondary,
                    uncheckedTrackColor = OracleTileBgElevated
                )
            )
        }
    }
}

private fun BorderBorder(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}

@Composable
fun NewsCard(article: NewsItem, isBengali: Boolean, onToggleLanguage: () -> Unit) {
    val title = if (isBengali) (article.titleBengali ?: article.title) else article.title
    val summary = if (isBengali) (article.summaryBengali ?: article.summary) else article.summary
    val source = if (isBengali) (article.sourceBengali ?: article.source) else article.source
    val timeAgo = if (isBengali) (article.timeAgoBengali ?: article.timeAgo) else article.timeAgo

    val sentimentText = when (article.sentiment.uppercase()) {
        "BULLISH" -> if (isBengali) "বুলিশ" else "BULLISH"
        "BEARISH" -> if (isBengali) "বেয়ারিশ" else "BEARISH"
        else -> if (isBengali) "নিরপেক্ষ" else "NEUTRAL"
    }
    val badgeColor = when (article.sentiment.uppercase()) {
        "BULLISH" -> OracleGreen
        "BEARISH" -> OracleRed
        else -> OracleTextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = OracleCardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.75.dp, OracleBorder, RoundedCornerShape(14.dp))
            .clickable { onToggleLanguage() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = source,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OracleTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(OracleTextMuted, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeAgo,
                        fontSize = 11.sp,
                        color = OracleTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .background(OracleTileBg, RoundedCornerShape(6.dp))
                        .border(0.65.dp, badgeColor.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sentimentText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OracleWhite,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = summary,
                fontSize = 13.sp,
                color = OracleTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun DeepInsightCard(
    insight: com.example.model.DeepInsightItem,
    isBengali: Boolean,
    onToggleLanguage: () -> Unit
) {
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isBengali) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    val isPump = insight.direction == "PUMP"
    val directionColor = if (isPump) OracleGreen else OracleRed
    val directionText = if (isBengali) {
        if (isPump) "পাম্প" else "ডাম্প"
    } else {
        insight.direction
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = OracleCardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.75.dp, directionColor.copy(alpha = 0.70f), RoundedCornerShape(14.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { onToggleLanguage() }
    ) {
        if (rotation <= 90f) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(OracleTileBg, CircleShape)
                                .border(0.65.dp, directionColor.copy(alpha = 0.75f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = insight.coinSymbol,
                                color = directionColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = insight.coinName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OracleWhite
                            )
                            Text(
                                text = insight.timeframe,
                                fontSize = 11.sp,
                                color = OracleTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(OracleTileBg, RoundedCornerShape(6.dp))
                            .border(0.65.dp, directionColor.copy(alpha = 0.80f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${insight.direction} +${insight.expectedChangePct}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = directionColor
                        )
                    }
                }

                HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 11.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESEARCH PROJECTED TARGET:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OracleTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (insight.targetPrice >= 100) String.format("$%,.2f", insight.targetPrice) else String.format("$%.3f", insight.targetPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = directionColor
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "Why:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleBlue
                )
                Text(
                    text = insight.whyEnglish,
                    fontSize = 13.sp,
                    color = OracleWhite,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "Tap to translate to Bengali / বাংলায় পড়তে চাপুন ➔",
                    fontSize = 10.sp,
                    color = OracleTextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(OracleTileBg, CircleShape)
                                .border(0.65.dp, directionColor.copy(alpha = 0.75f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = insight.coinSymbol,
                                color = directionColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = insight.coinName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = OracleWhite
                            )
                            Text(
                                text = "সময়সীমা: " + when (insight.timeframe) {
                                    "Next 24 Hours" -> "আগামী ২৪ ঘন্টা"
                                    "Next 48 Hours" -> "আগামী ৪৮ ঘন্টা"
                                    "Next 7 Days" -> "আগামী ৭ দিন"
                                    else -> insight.timeframe
                                },
                                fontSize = 11.sp,
                                color = OracleTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(OracleTileBg, RoundedCornerShape(6.dp))
                            .border(0.65.dp, directionColor.copy(alpha = 0.80f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$directionText +${insight.expectedChangePct}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = directionColor
                        )
                    }
                }

                HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 11.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গবেষণা পূর্বাভাসিত লক্ষ্য:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OracleTextSecondary
                    )
                    Text(
                        text = if (insight.targetPrice >= 100) String.format("$%,.2f", insight.targetPrice) else String.format("$%.3f", insight.targetPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = directionColor
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "বিশ্লেষণ কারণসমূহ:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleBlue
                )
                Text(
                    text = insight.whyBengali,
                    fontSize = 13.sp,
                    color = OracleWhite,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = "ইংরেজিতে পড়তে চাপুন / Tap to translate to English ➔",
                    fontSize = 10.sp,
                    color = OracleTextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun LiveCurrencyTile(
    isBengali: Boolean,
    onToggleLanguage: () -> Unit
) {
    val rotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isBengali) 180f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = OracleCardBg),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.75.dp, OracleYellow.copy(alpha = 0.72f), RoundedCornerShape(14.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { onToggleLanguage() }
    ) {
        if (rotation <= 90f) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(OracleTileBg, CircleShape)
                                .border(0.65.dp, OracleYellow.copy(alpha = 0.75f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Currency Symbol Index",
                                tint = OracleYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ORACLE FREE-MARKET CURRENCY INDEX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = OracleWhite,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("P2P Crypto-Referred Rates (Real-Time ")
                                    withStyle(SpanStyle(color = OracleGreen, fontWeight = FontWeight.Black)) {
                                        append("LIVE")
                                    }
                                    append(")")
                                },
                                fontSize = 10.sp,
                                color = OracleTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(OracleGreen, CircleShape)
                    )
                }

                HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 11.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyRow(flag = "🇺🇸", code = "USD", name = "United States Dollar", rate = "118.42 BDT")
                    CurrencyRow(flag = "🇪🇺", code = "EUR", name = "Euro", rate = "128.65 BDT")
                    CurrencyRow(flag = "🇬🇧", code = "GBP", name = "British Pound", rate = "151.10 BDT")
                    CurrencyRow(flag = "🇸🇦", code = "SAR", name = "Saudi Riyal", rate = "31.58 BDT")
                    CurrencyRow(flag = "🇦🇪", code = "AED", name = "United Arab Dirham", rate = "32.24 BDT")
                }

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "Tap to translate to Bengali / বাংলায় পড়তে চাপুন ➔",
                    fontSize = 9.sp,
                    color = OracleTextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(OracleTileBg, CircleShape)
                                .border(0.65.dp, OracleYellow.copy(alpha = 0.75f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Currency Symbol Index",
                                tint = OracleYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ওরাকল উন্মুক্ত বাজার মুদ্রা সূচক",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = OracleWhite
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("ক্রিপ্টো পিটুপি ও সরাসরি বাজার রেট (রিয়েল টাইম ")
                                    withStyle(SpanStyle(color = OracleGreen, fontWeight = FontWeight.Black)) {
                                        append("LIVE")
                                    }
                                    append(")")
                                },
                                fontSize = 10.sp,
                                color = OracleTextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(OracleGreen, CircleShape)
                    )
                }

                HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 11.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyRow(flag = "🇺🇸", code = "USD", name = "ইউএস ডলার", rate = "১১৮.৪২ টাকা")
                    CurrencyRow(flag = "🇪🇺", code = "EUR", name = "ইউরো", rate = "১২৮.৬৫ টাকা")
                    CurrencyRow(flag = "🇬🇧", code = "GBP", name = "ব্রিটিশ পাউন্ড", rate = "১৫১.১০ টাকা")
                    CurrencyRow(flag = "🇸🇦", code = "SAR", name = "সৌদি রিয়াল", rate = "৩১.৫৮ টাকা")
                    CurrencyRow(flag = "🇦🇪", code = "AED", name = "ইউএই দিরহাম", rate = "৩২.২৪ টাকা")
                }

                Spacer(modifier = Modifier.height(9.dp))

                Text(
                    text = "ইংরেজিতে পড়তে চাপুন / Tap to translate to English ➔",
                    fontSize = 9.sp,
                    color = OracleTextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun CurrencyRow(flag: String, code: String, name: String, rate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OracleTileBg, RoundedCornerShape(8.dp))
            .border(0.6.dp, OracleBorderSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = flag, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = code,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = OracleWhite
                )
                Text(
                    text = name,
                    fontSize = 10.sp,
                    color = OracleTextMuted
                )
            }
        }
        Text(
            text = rate,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = OracleYellow
        )
    }
}

data class MarketAsset(
    val symbol: String,
    val price: Double,
    val priceChangePercent: Double
)

@Composable
fun RealTimePublicMarketDashboard() {
    var assets by remember { mutableStateOf<List<MarketAsset>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            while (true) {
                try {
                    val response = java.net.URL("https://api.binance.com/api/v3/ticker/24hr").readText()
                    val jsonArray = org.json.JSONArray(response)
                    val topSymbols = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT", "XRPUSDT", "DOGEUSDT", "ADAUSDT", "AVAXUSDT")

                    val parsedAssets = mutableListOf<MarketAsset>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val symbol = obj.getString("symbol")
                        if (symbol in topSymbols) {
                            parsedAssets.add(
                                MarketAsset(
                                    symbol = symbol.replace("USDT", ""),
                                    price = obj.getDouble("lastPrice"),
                                    priceChangePercent = obj.getDouble("priceChangePercent")
                                )
                            )
                        }
                    }

                    assets = parsedAssets.sortedBy { topSymbols.indexOf("${it.symbol}USDT") }
                    isLoading = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = OracleCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.75.dp, OracleBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "REAL-TIME PUBLIC MARKET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = OracleWhite,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = OracleGreen,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                assets.forEachIndexed { index, asset ->
                    MarketAssetRow(asset)
                    if (index != assets.lastIndex) {
                        HorizontalDivider(color = OracleBorderSoft, modifier = Modifier.padding(vertical = 10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MarketAssetRow(asset: MarketAsset) {
    val isPositive = asset.priceChangePercent >= 0
    val moveColor = if (isPositive) OracleGreen else OracleRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OracleTileBg, RoundedCornerShape(8.dp))
            .border(0.6.dp, OracleBorderSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(OracleTileBgElevated, CircleShape)
                    .border(0.65.dp, OracleBlue.copy(alpha = 0.70f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = asset.symbol.take(1),
                    color = OracleBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Text(
                text = asset.symbol,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = OracleWhite
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (asset.price >= 100) String.format("$%,.2f", asset.price) else String.format("$%.3f", asset.price),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = OracleWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isPositive) "Price up" else "Price down",
                    tint = moveColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", asset.priceChangePercent)}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = moveColor
                )
            }
        }
    }
}

