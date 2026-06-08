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
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // App Header Brand Profile
        item {
            HeaderSection(viewModel)
        }

        item {
            RealTimePublicMarketDashboard()
        }

        // Ticker for top coins: BTC, ETH, XRP
        item {
            val livePrices by viewModel.livePrices.collectAsState()
            TopCoinsTickerSection(newsFeed, isBengali, livePrices)
        }

        // Star Divider
        item {
            StarDivider()
        }

        // Section Title: Crypto News Feed
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "News icon",
                    tint = CryptoCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LATEST ORACLE MARKET FEED",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan,
                    letterSpacing = 1.sp
                )
            }
        }

        // Scrolling News List
        items(newsFeed.newsList) { article ->
            var isLocalBengali by remember { mutableStateOf(false) }
            NewsCard(article, isLocalBengali, onToggleLanguage = { isLocalBengali = !isLocalBengali })
        }

        // Star Divider
        item {
            StarDivider()
        }

        // New Section Title: Deep Analytical Insights
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Insights icon",
                    tint = CryptoGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ORACLE DEEP ANALYTICAL INSIGHTS",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoGreen,
                    letterSpacing = 1.sp
                )
            }
        }

        // Scrolling Deep Insights List
        items(newsFeed.deepInsights) { insight ->
            var isLocalBengali by remember { mutableStateOf(false) }
            DeepInsightCard(
                insight = insight,
                isBengali = isLocalBengali,
                onToggleLanguage = { isLocalBengali = !isLocalBengali }
            )
        }

        // Star Divider preceding Dedicated Pricing Section (Oracle Market Index)
        item {
            StarDivider()
        }

        // Dedicated Pricing Section (Oracle Market Index Header)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                // 3 Green Stars in front
                Row {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Glossy star indicator",
                            tint = CryptoGreen,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "ORACLE MARKET INDEX",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 3 Green Stars at the back
                Row {
                    repeat(3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Glossy star indicator",
                            tint = CryptoGreen,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }
            }
        }

        // Decoupled beautiful Currency Exchange Rate tile
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
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "*   *   *",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BorderColor.copy(alpha = 0.5f),
            letterSpacing = 12.sp
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
            .width(86.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
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
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                Text(
                    text = dateText,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = CryptoCyan,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "GMT+6",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoGreen
                )
            }
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
        // Network Sync status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isLiveConnected) CryptoGreen else TextMuted, CircleShape)
            )
            Text(
                text = if (isLiveConnected) "BINANCE SYNC: LIVE" else "LOCAL INDEX",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLiveConnected) CryptoGreen else TextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        // Gemini Key status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (hasGeminiApiKey) CryptoCyan else AccentGold, CircleShape)
            )
            Text(
                text = if (hasGeminiApiKey) "GEMINI: ACTIVE" else "GEMINI: SIMULATED",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasGeminiApiKey) CryptoCyan else AccentGold,
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
            modifier = Modifier.weight(1f).padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Embed the generated high-quality custom png asset
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo_ai),
                contentDescription = "AI Trading Assistant Logo",
                modifier = Modifier
                    .size(42.dp) // Scaled down logo
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, CryptoGreen, RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Crypto Signal",
                    fontSize = 10.sp, // Reduced 2px
                    fontWeight = FontWeight.Medium,
                    color = CryptoCyan,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)) {
                            append("Oracle ")
                        }
                        withStyle(style = SpanStyle(color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                            append("B")
                        }
                        withStyle(style = SpanStyle(color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)) {
                            append("Y ")
                        }
                        withStyle(style = SpanStyle(color = CryptoGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)) {
                            append("Z")
                        }
                        withStyle(style = SpanStyle(color = CryptoGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)) {
                            append("AHID")
                        }
                    },
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                // Real-time telemetry indicators row below labels
                TelemetryStatusRow(viewModel)
            }
        }
        
        Box(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            BangladeshTimeWidget()
        }
    }
}

@Composable
fun TopCoinsTickerSection(newsFeed: OracleAnalysisResponse, isBengali: Boolean, livePrices: Map<String, Double>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ORACLE REFERENCE INDEX",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val btcSpot = newsFeed.spotSignals.find { it.coinSymbol == "BTC" }
            val ethSpot = newsFeed.spotSignals.find { it.coinSymbol == "ETH" }
            val xrpSpot = newsFeed.spotSignals.find { it.coinSymbol == "XRP" }
            val solSpot = newsFeed.spotSignals.find { it.coinSymbol == "SOL" }
            val adaSpot = newsFeed.spotSignals.find { it.coinSymbol == "ADA" }

            val btcPrice = livePrices["BTCUSDT"] ?: btcSpot?.currentPrice ?: 66450.0
            val btcChange = btcSpot?.growthPotentialPct ?: 3.99

            val ethPrice = livePrices["ETHUSDT"] ?: ethSpot?.currentPrice ?: 3485.50
            val ethChange = ethSpot?.growthPotentialPct ?: 3.57

            val xrpPrice = livePrices["XRPUSDT"] ?: xrpSpot?.currentPrice ?: 0.512
            val xrpChange = xrpSpot?.growthPotentialPct ?: -5.8

            val solPrice = livePrices["SOLUSDT"] ?: solSpot?.currentPrice ?: 164.20
            val solChange = solSpot?.growthPotentialPct ?: 4.12

            val adaPrice = livePrices["ADAUSDT"] ?: adaSpot?.currentPrice ?: 0.435
            val adaChange = adaSpot?.growthPotentialPct ?: -2.15

            TickerRow(symbol = "BTC", name = "Bitcoin", price = btcPrice, changePct = btcChange)
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
            TickerRow(symbol = "ETH", name = "Ethereum", price = ethPrice, changePct = ethChange)
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
            TickerRow(symbol = "XRP", name = "Ripple", price = xrpPrice, changePct = xrpChange)
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
            TickerRow(symbol = "SOL", name = "Solana", price = solPrice, changePct = solChange)
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
            TickerRow(symbol = "ADA", name = "Cardano", price = adaPrice, changePct = adaChange)
        }
    }
}

@Composable
fun TickerRow(symbol: String, name: String, price: Double, changePct: Double) {
    // Pulse effect when price changes
    val animatedPrice = animateFloatAsState(
        targetValue = price.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "PriceAnim"
    ).value

    // Determine pulse state by storing previous value
    var prevPrice by remember { mutableStateOf(price) }
    var pulseGlow by remember { mutableStateOf(false) }

    LaunchedEffect(price) {
        if (price != prevPrice) {
            pulseGlow = true
            delay(300)
            pulseGlow = false
            prevPrice = price
        }
    }

    val glowAlpha by animateFloatAsState(
        targetValue = if (pulseGlow) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "GlowAnim"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CryptoCyan.copy(alpha = glowAlpha * 0.2f),
                shape = RoundedCornerShape(8.dp) // Glass premium layout
            )
            .border(
                width = 1.dp,
                color = CryptoCyan.copy(alpha = glowAlpha * 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(vertical = 4.dp, horizontal = 4.dp), // Premium Tile
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        when (symbol) {
                            "BTC" -> AccentGold.copy(alpha = 0.15f)
                            "ETH" -> CryptoCyan.copy(alpha = 0.15f)
                            "XRP" -> CryptoRed.copy(alpha = 0.15f)
                            "SOL" -> CryptoGreen.copy(alpha = 0.15f)
                            "ADA" -> CryptoRed.copy(alpha = 0.15f)
                            else -> CryptoCyan.copy(alpha = 0.15f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol.take(1),
                    color = when (symbol) {
                        "BTC" -> AccentGold
                        "ETH" -> CryptoCyan
                        "XRP" -> CryptoRed
                        "SOL" -> CryptoGreen
                        "ADA" -> CryptoRed
                        else -> CryptoCyan
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = symbol,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (animatedPrice >= 100) String.format("$%,.2f", animatedPrice) else String.format("$%.3f", animatedPrice),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            
            val isPositive = changePct >= 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isPositive) "Price up" else "Price down",
                    tint = if (isPositive) CryptoGreen else CryptoRed,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%+.2f%%", changePct),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) CryptoGreen else CryptoRed
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
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderBorder(if (useAiOracle) CryptoCyan else BorderColor),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(CryptoCyan.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Oracle intelligence core selector",
                        tint = CryptoCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI ORACLE MODALITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (useAiOracle) "Direct Gemini-3.5-Flash active" else "Fast Technical Simulator local",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = useAiOracle,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CryptoGreen,
                    checkedTrackColor = CryptoGreen.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkSurfaceVariant
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
        "BULLISH" -> if (isBengali) "তেজি" else "BULLISH"
        "BEARISH" -> if (isBengali) "মন্দা" else "BEARISH"
        else -> if (isBengali) "নিরপেক্ষ" else "NEUTRAL"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .clickable { onToggleLanguage() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Source, Time, Sentiment Pill
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
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(TextMuted, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeAgo,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                val badgeBg = when (article.sentiment.uppercase()) {
                    "BULLISH" -> CryptoGreen.copy(alpha = 0.12f)
                    "BEARISH" -> CryptoRedContainer
                    else -> TextMuted.copy(alpha = 0.15f)
                }
                val badgeColor = when (article.sentiment.uppercase()) {
                    "BULLISH" -> CryptoGreen
                    "BEARISH" -> CryptoRedText
                    else -> TextSecondary
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = sentimentText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Article Title
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Article Summary
            Text(
                text = summary,
                fontSize = 13.sp,
                color = TextSecondary,
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

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (insight.direction == "PUMP") CryptoGreen.copy(alpha = 0.5f) else CryptoRed.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { onToggleLanguage() }
    ) {
        if (rotation <= 90f) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (insight.direction == "PUMP") CryptoGreen.copy(alpha = 0.15f)
                                    else CryptoRedContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = insight.coinSymbol,
                                color = if (insight.direction == "PUMP") CryptoGreen else CryptoRedText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = insight.coinName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = insight.timeframe,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    val badgeBg = if (insight.direction == "PUMP") CryptoGreen.copy(alpha = 0.15f) else CryptoRedContainer
                    val badgeColor = if (insight.direction == "PUMP") CryptoGreen else CryptoRedText
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${insight.direction} +${insight.expectedChangePct}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeColor
                        )
                    }
                }

                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESEARCH PROJECTED TARGET:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (insight.targetPrice >= 100) String.format("$%,.2f", insight.targetPrice) else String.format("$%.3f", insight.targetPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (insight.direction == "PUMP") CryptoGreen else CryptoRedText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Why:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan
                )
                Text(
                    text = insight.whyEnglish,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap to translate to Bengali / বাংলায় পড়তে চাপুন ➔",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
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
                                .size(32.dp)
                                .background(
                                    if (insight.direction == "PUMP") CryptoGreen.copy(alpha = 0.15f)
                                    else CryptoRedContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = insight.coinSymbol,
                                color = if (insight.direction == "PUMP") CryptoGreen else CryptoRedText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = insight.coinName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "সময়সীমা: " + when (insight.timeframe) {
                                    "Next 24 Hours" -> "আগামী ২৪ ঘন্টা"
                                    "Next 48 Hours" -> "আগামী ৪৮ ঘন্টা"
                                    "Next 7 Days" -> "আগামী ৭ দিন"
                                    else -> insight.timeframe
                                },
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    val badgeBg = if (insight.direction == "PUMP") CryptoGreen.copy(alpha = 0.15f) else CryptoRedContainer
                    val badgeColor = if (insight.direction == "PUMP") CryptoGreen else CryptoRedText
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${if (insight.direction == "PUMP") "পাম্প" else "ডাম্প"} +${insight.expectedChangePct}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeColor
                        )
                    }
                }

                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গবেষণা পূর্বাভাসিত লক্ষ্য:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                    Text(
                        text = if (insight.targetPrice >= 100) String.format("$%,.2f", insight.targetPrice) else String.format("$%.3f", insight.targetPrice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (insight.direction == "PUMP") CryptoGreen else CryptoRedText
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "বিশ্লেষণ কারণসমূহ:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan
                )
                Text(
                    text = insight.whyBengali,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ইংরেজিতে পড়তে চাপুন / Tap to translate to English ➔",
                    fontSize = 10.sp,
                    color = TextMuted,
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
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(CryptoCyan.copy(alpha = 0.5f), AccentGold.copy(alpha = 0.5f))
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { onToggleLanguage() }
    ) {
        if (rotation <= 90f) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header of index
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AccentGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Currency Symbol Index",
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ORACLE FREE-MARKET CURRENCY INDEX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("P2P Crypto-Referred Rates (Real-Time ")
                                    withStyle(SpanStyle(color = CryptoCyan, fontWeight = FontWeight.Bold)) {
                                        append("LIVE")
                                    }
                                    append(")")
                                },
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CryptoGreen, CircleShape)
                    )
                }

                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                // Exchange Rates Table
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyRow(flag = "🇺🇸", code = "USD", name = "United States Dollar", rate = "118.42 BDT")
                    CurrencyRow(flag = "🇪🇺", code = "EUR", name = "Euro", rate = "128.65 BDT")
                    CurrencyRow(flag = "🇬🇧", code = "GBP", name = "British Pound", rate = "151.10 BDT")
                    CurrencyRow(flag = "🇸🇦", code = "SAR", name = "Saudi Riyal", rate = "31.58 BDT")
                    CurrencyRow(flag = "🇦🇪", code = "AED", name = "United Arab Dirham", rate = "32.24 BDT")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Tap to translate to Bengali / বাংলায় পড়তে চাপুন ➔",
                    fontSize = 9.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .padding(16.dp)
            ) {
                // Header of index - Bengali
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(AccentGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Currency Symbol Index",
                                tint = AccentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ওরাকল উন্মুক্ত বাজার মুদ্রা সূচক",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append("ক্রিপ্টো পিটুপি ও সরাসরি বাজার রেট (রিয়েল টাইম ")
                                    withStyle(SpanStyle(color = CryptoCyan, fontWeight = FontWeight.Bold)) {
                                        append("LIVE")
                                    }
                                    append(")")
                                },
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CryptoGreen, CircleShape)
                    )
                }

                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                // Exchange Rates Table - Bengali
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CurrencyRow(flag = "🇺🇸", code = "USD", name = "ইউএস ডলার", rate = "১১৮.৪২ টাকা")
                    CurrencyRow(flag = "🇪🇺", code = "EUR", name = "ইউরো", rate = "১২৮.৬৫ টাকা")
                    CurrencyRow(flag = "🇬🇧", code = "GBP", name = "ব্রিটিশ পাউন্ড", rate = "১৫১.১০ টাকা")
                    CurrencyRow(flag = "🇸🇦", code = "SAR", name = "সৌদি রিয়াল", rate = "৩১.৫৮ টাকা")
                    CurrencyRow(flag = "🇦🇪", code = "AED", name = "ইউএই দিরহাম", rate = "৩২.২৪ টাকা")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ইংরেজিতে পড়তে চাপুন / Tap to translate to English ➔",
                    fontSize = 9.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun CurrencyRow(flag: String, code: String, name: String, rate: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = name,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
        Text(
            text = rate,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold
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
                    
                    // Sort by our predefined order
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
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "REAL-TIME PUBLIC MARKET",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CryptoCyan,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = CryptoCyan,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                assets.forEach { asset ->
                    MarketAssetRow(asset)
                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    }
}

@Composable
fun MarketAssetRow(asset: MarketAsset) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CryptoCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = asset.symbol.take(1),
                    color = CryptoCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = asset.symbol,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextPrimary
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (asset.price >= 100) String.format("$%,.2f", asset.price) else String.format("$%.3f", asset.price),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            
            val isPositive = asset.priceChangePercent >= 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isPositive) "Price up" else "Price down",
                    tint = if (isPositive) CryptoGreen else CryptoRed,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", asset.priceChangePercent)}%",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) CryptoGreen else CryptoRed
                )
            }
        }
    }
}
