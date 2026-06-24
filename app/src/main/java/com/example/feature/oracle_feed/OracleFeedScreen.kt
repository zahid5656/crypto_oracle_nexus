package com.example.feature.oracle_feed

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NewsItem
import com.example.model.OracleAnalysisResponse
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.example.ui.theme.*

// Terminal Colors - Institutional Grade Apple Ecosystem Style
private val T_Bg = DarkBackground
private val T_Surface = Color(0xFF111112)
private val T_Border = Color(0xFF1C1C1E)
private val T_BorderHigh = Color(0xFF2C2C2E)
private val T_BorderMedium = Color(0xFF3A3A3C)
private val T_TextPrimary = Color(0xFFFFFFFF)
private val T_TextSecondary = Color(0xFF8E8E93)
private val T_TextMuted = Color(0xFF636366)
private val T_Green = CryptoGreen 
private val T_Red = CryptoRedText   
private val T_Cyan = CryptoCyan  
private val T_Gold = TitanGold  
private val T_Orange = TitanOrange

@Composable
fun OracleFeedScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val newsFeed by viewModel.newsFeedData.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()
    val livePrices by viewModel.livePrices.collectAsState()

    var activeTimeframe by remember { mutableStateOf("15M") }
    var activeTab by remember { mutableStateOf("TERMINAL") }
    var feedState by remember { mutableStateOf("LIVE") } // LIVE, CACHED, LOCAL, SYNCING

    val tabs = listOf("TERMINAL", "COCKPIT", "MOVERS", "ALERTS", "SYSTEM")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(T_Bg),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            OracleFeedHeaderBlock(viewModel, feedState)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { tab ->
                    val isActive = tab == activeTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isActive) T_Cyan.copy(alpha = 0.2f) else T_Surface)
                            .border(0.5.dp, if (isActive) T_Cyan else T_BorderMedium, RoundedCornerShape(4.dp))
                            .clickable { activeTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (isActive) T_Cyan else T_TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (activeTab == "TERMINAL") {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("ORACLE REFERENCE INDEX (REAL-TIME)")
                OracleReferenceIndexPanel()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("MARKET DASHBOARD (GLOBAL BINANCE SYNC)")
                RealTimeMarketPricePanel()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("ORACLE ANALYTICAL INSIGHTS")
                OracleAnalyticalInsightsPanel()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("MARKET FEED & INTELLIGENCE")
                MarketFeedIntelligencePanel()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("FX OPEN-MARKET INDEX")
                FxOpenMarketIndexPanel()
            }
        }

        if (activeTab == "COCKPIT") {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CockpitStatusRow(feedState)
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SourceProvenanceRow()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("MARKET REGIME")
                MarketRegimePanel()
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("GLOBAL MARKET SNAPSHOT")
                GlobalMarketSnapshotPanel()
            }
        }

        if (activeTab == "MOVERS") {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("TIMEFRAME")
                TimeframeControlSurface(activeTimeframe) { activeTimeframe = it }
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("TOP MOVERS / MARKET FEED")
                TopAssetFeedList(livePrices)
            }
        }

        if (activeTab == "ALERTS") {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("ALERTS & WATCHLIST")
                AlertsWatchlistPreviewPanel()
            }
        }

        if (activeTab == "SYSTEM") {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SectionHeader("SYSTEM STATES (UI TEST)")
                SystemStatesPreview()
            }
        }
    }
}

@Composable
fun OracleFeedHeaderBlock(viewModel: CryptoViewModel, feedState: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(T_TextPrimary, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SYS",
                        color = T_Bg,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "NEXUS TERMINAL v3.1",
                        color = T_TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI Market Intelligence Cockpit",
                        color = T_Cyan,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            TerminalClockWidget()
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "DATA STREAM",
                    color = T_TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "BINANCE WSS : ACTIVE",
                    color = T_Green,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "INTELLIGENCE CORE",
                    color = T_TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GEMINI PRO : ONLINE",
                    color = T_Cyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CockpitStatusRow(feedState: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp))
            .background(T_Surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "DATA STATE",
                color = T_TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = feedState,
                color = when (feedState) {
                    "LIVE" -> T_Green
                    "SYNCING" -> T_Gold
                    "CACHED" -> T_Cyan
                    else -> T_Red
                },
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "FRESHNESS",
                color = T_TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "12s",
                color = T_Green,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "GLOBAL MODE",
                color = T_TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Risk-On",
                color = T_Gold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TerminalClockWidget() {
    var timeText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = ZonedDateTime.now(ZoneId.of("UTC"))
            timeText = now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " UTC"
            delay(1000)
        }
    }

    Text(
        text = timeText,
        color = T_TextSecondary,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
    )
}

@Composable
fun SourceProvenanceRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Market: API", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("AI: Pending", color = T_Gold, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("Cache: Fresh", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text("Model: Active", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Surface)
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = T_Cyan,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun MarketRegimePanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp))
            .background(T_Surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RegimeItem("Regime", "Bullish Expansion", T_Green)
            RegimeItem("Trend Bias", "Positive", T_Green)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RegimeItem("Volatility", "Elevated", T_Orange)
            RegimeItem("Liquidity", "Healthy", T_Green)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RegimeItem("Risk Climate", "Normal", T_TextSecondary)
            RegimeItem("Session", "Active", T_Cyan)
        }
    }
}

@Composable
fun RegimeItem(label: String, value: String, valueColor: Color) {
    Column(modifier = Modifier.width(140.dp)) {
        Text(label, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GlobalMarketSnapshotPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SnapshotItem("BTC.D", "54.2%")
            SnapshotItem("ETH Strength", "Neutral")
            SnapshotItem("Trend", "Upward")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SnapshotItem("Stable Flow", "+$420M")
            SnapshotItem("Vol Pressure", "Rising")
            SnapshotItem("Breadth", "68% Pos")
        }
    }
}

@Composable
fun SnapshotItem(label: String, value: String) {
    Column(modifier = Modifier.width(100.dp)) {
        Text(label, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
    }
}

@Composable
fun TimeframeControlSurface(active: String, onSelect: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val timeframes = listOf("1M", "5M", "15M", "30M", "45M", "1H", "4H", "24H")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timeframes.forEach { tf ->
            val isActive = tf == active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isActive) T_Cyan.copy(alpha = 0.2f) else T_Surface)
                    .border(0.5.dp, if (isActive) T_Cyan else T_BorderMedium, RoundedCornerShape(4.dp))
                    .clickable { onSelect(tf) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tf,
                    color = if (isActive) T_Cyan else T_TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

data class MockAssetData(
    val rank: Int,
    val symbol: String,
    val price: Double,
    val change24h: Double,
    val volume: String,
    val heat: Int,
    val momentum: String,
    val pressure: String,
    val priority: String,
    val riskTag: String,
    val readiness: String
)

@Composable
fun TopAssetFeedList(livePrices: Map<String, Double>) {
    val mockedFeed = listOf(
        MockAssetData(1, "BTC", livePrices["BTCUSDT"] ?: 67420.50, 2.4, "High", 82, "Strong", "Buy Side", "Watch", "LOW", "OPTIMAL"),
        MockAssetData(2, "ETH", livePrices["ETHUSDT"] ?: 3540.20, 1.8, "High", 76, "Moderate", "Buy Side", "Normal", "MEDIUM", "ACCEPTABLE"),
        MockAssetData(3, "SOL", livePrices["SOLUSDT"] ?: 145.30, -0.5, "Medium", 65, "Neutral", "Balanced", "Normal", "MEDIUM", "ACCEPTABLE"),
        MockAssetData(4, "DOGE", livePrices["DOGEUSDT"] ?: 0.12, 12.5, "High", 95, "Extreme", "Heavy Buy", "Actionable", "HIGH", "DEGRADED"),
        MockAssetData(5, "XRP", livePrices["XRPUSDT"] ?: 0.52, -2.1, "Low", 45, "Weak", "Sell Side", "Ignore", "LOW", "POOR")
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        mockedFeed.forEach { asset ->
            AssetFeedCard(asset)
        }
    }
}

@Composable
fun AssetFeedCard(asset: MockAssetData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .border(0.5.dp, T_BorderMedium, RoundedCornerShape(6.dp))
            .background(T_Surface)
            .padding(12.dp)
    ) {
        // Top Row: Rank, Symbol, Sparkline, Price, Change
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${asset.rank}", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(6.dp))
                Text(asset.symbol, color = T_TextPrimary, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            // Sparkline Placeholder
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(60.dp)
                    .background(T_Bg)
                    .border(0.5.dp, T_BorderMedium)
            ) {
                Text("~~~", color = if (asset.change24h >= 0) T_Green else T_Red, fontSize = 8.sp, modifier = Modifier.align(Alignment.Center))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "\$${String.format("%,.2f", asset.price)}",
                    color = T_TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "${if(asset.change24h >= 0) "+" else ""}${asset.change24h}%",
                    color = if(asset.change24h >= 0) T_Green else T_Red,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Middle Row: Intelligence & Heat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Signal Heat: ${asset.heat}", color = if(asset.heat > 80) T_Green else T_Gold, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Momentum: ${asset.momentum}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("Pressure: ${asset.pressure}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("Priority: ${asset.priority}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Vol: ${asset.volume}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("Risk: ${asset.riskTag}", color = when(asset.riskTag){
                        "LOW" -> T_Green
                        "MEDIUM" -> T_Gold
                        "HIGH" -> T_Orange
                        else -> T_Red
                    }, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ready: ${asset.readiness}", color = when(asset.readiness){
                        "OPTIMAL" -> T_Green
                        "ACCEPTABLE" -> T_Cyan
                        "DEGRADED" -> T_Gold
                        else -> T_Red
                    }, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = T_BorderHigh, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bottom Row: Action Handoffs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HandoffAction("Signal Pro")
            HandoffAction("Live Radar")
            HandoffAction("Mission [+]")
        }
    }
}

@Composable
fun HandoffAction(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(T_BorderMedium)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = T_Cyan,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AlertsWatchlistPreviewPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).background(T_Surface).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Watchlist: 8", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Breakout Candidates: 3", color = T_Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Risk Alerts: 1", color = T_Orange, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Expired Signals: 2", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun SystemStatesPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StateBox("Loading Oracle Feed...", T_Gold)
        StateBox("No market data available", T_TextMuted)
        StateBox("Data stale — refresh required", T_Orange)
        StateBox("Using local fallback snapshot", T_Cyan)
    }
}

@Composable
fun StateBox(message: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OracleReferenceIndexPanel() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Text("Source: Simulated", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Last update: 12s", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        val indexList = listOf(
            Pair("BTC", "+2.4%"),
            Pair("ETH", "+1.8%"),
            Pair("XRP", "-2.1%"),
            Pair("SOL", "-0.5%"),
            Pair("ADA", "+1.1%")
        )
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TICKER", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("PRICE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("24H %", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
        }
        
        indexList.forEach { (name, change) ->
            val priceStr = when(name) {
                "BTC" -> "$67,420.50"; "ETH" -> "$3,540.20"; "XRP" -> "$0.52"; "SOL" -> "$145.30"; "ADA" -> "$0.45"
                else -> "$0.00"
            }
            Row(
                modifier = Modifier.fillMaxWidth().border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).background(T_Surface).padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(priceStr, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(change, color = if (change.startsWith("+")) T_Green else T_Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun RealTimeMarketPricePanel() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Text("Market data: Mock", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Feed status: Stable", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ASSET", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("LAST", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text("CHG", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
        }
        
        val top10 = listOf(
            Triple("BTC", "$67,420.50", "+2.4%"), Triple("ETH", "$3,540.20", "+1.8%"), Triple("SOL", "$145.30", "-0.5%"), Triple("BNB", "$590.10", "+0.2%"), Triple("XRP", "$0.52", "-2.1%"),
            Triple("DOGE", "$0.12", "+12.5%"), Triple("ADA", "$0.45", "+1.1%"), Triple("AVAX", "$35.20", "-1.4%"), Triple("LINK", "$14.10", "+3.2%"), Triple("TON", "$6.80", "+0.8%")
        )
        top10.forEach { (coin, price, chg) ->
            Row(
                modifier = Modifier.fillMaxWidth().border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).background(T_Surface).padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(coin, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(price, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text(chg, color = if (chg.startsWith("+")) T_Green else T_Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun OracleAnalyticalInsightsPanel() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).background(T_Surface).padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Text("Source: Cached Model", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Last update: 45s", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        val insights = listOf(
            Triple("SOL", "NEXT 48 HOURS", "LONG" to "Target: \$165.00. Strong bullish divergence observed on high timeframe indices. Liquidity inflows suggest continued momentum with moderate risk of short-term pullback."),
            Triple("XRP", "NEXT 24 HOURS", "SHORT" to "Target: \$0.48. Rejection at key resistance with declining volume. Orderbook shows heavy sell walls forming above current levels."),
            Triple("ADA", "NEXT 7 DAYS", "LONG" to "Target: \$0.55. Accumulation phase detected. On-chain metrics indicate growing institutional interest and network activity increase.")
        )
        
        insights.forEach { (coin, timeframe, details) ->
            val (direction, text) = details
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(coin, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("— $timeframe —", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(direction, color = if (direction == "LONG") T_Green else T_Red, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MarketFeedIntelligencePanel() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Text("Source: Simulated Feed", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Feed status: Stable", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        val articles = listOf(
            Triple("GAS TRACKER AI", "45M AGO", "NEUTRAL" to "ETH Gas Fees Stabilizing\nAverage gas fees have dropped below 15 gwei, stabilizing network transaction costs for smart contracts."),
            Triple("ORACLE FINANCIALS", "3H AGO", "BULLISH" to "Institutional Inflows Increase\nBitcoin ETFs show consecutive days of positive inflows, indicating strong institutional demand holding support levels."),
            Triple("LEGAL LEDGER", "2H AGO", "BEARISH" to "Regulatory Uncertainty in EU\nNew statements from European regulators suggest stricter compliance requirements for DeFi protocols coming next month.")
        )
        
        articles.forEach { (source, age, details) ->
            val (status, content) = details
            val lines = content.split("\n")
            val headline = lines[0]
            val paragraph = lines.getOrElse(1) { "" }
            
            Column(
                modifier = Modifier.fillMaxWidth().border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).background(T_Surface).padding(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(source, color = T_TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(age, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    val color = when(status) { "BULLISH" -> T_Green; "BEARISH" -> T_Red; else -> T_Cyan }
                    Box(modifier = Modifier.background(color.copy(alpha=0.1f), RoundedCornerShape(2.dp)).border(0.5.dp, color, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text(status, color = color, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(headline, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(paragraph, color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FxOpenMarketIndexPanel() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            Text("Market data: Mock", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Last update: 12s", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        
        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("CCY", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
            Text("CURRENCY", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            Text("RATE (BDT)", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        
        val fxList = listOf(
            Triple("USD", "US Dollar", "118.42"),
            Triple("EUR", "Euro", "128.65"),
            Triple("GBP", "British Pound", "151.10"),
            Triple("SAR", "Saudi Riyal", "31.58"),
            Triple("AED", "UAE Dirham", "32.24")
        )
        fxList.forEach { (code, name, rate) ->
            Row(
                modifier = Modifier.fillMaxWidth().border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).background(T_Surface).padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(code, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                Text(name, color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.5f))
                Text(rate, color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
