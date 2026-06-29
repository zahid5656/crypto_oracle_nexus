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
private val T_TextPrimary = TextPrimary
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
        contentPadding = PaddingValues(bottom = 132.dp)
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
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("ORACLE REFERENCE INDEX (REAL-TIME)")
                OracleReferenceIndexPanel()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("MARKET DASHBOARD (GLOBAL BINANCE SYNC)")
                RealTimeMarketPricePanel()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("ORACLE ANALYTICAL INSIGHTS")
                OracleAnalyticalInsightsPanel()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("MARKET FEED & INTELLIGENCE")
                MarketFeedIntelligencePanel()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("MARKET REGIME")
                MarketRegimePanel()
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("GLOBAL MARKET SNAPSHOT")
                GlobalMarketSnapshotPanel()
            }
        }

        if (activeTab == "MOVERS") {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("TIMEFRAME")
                TimeframeControlSurface(activeTimeframe) { activeTimeframe = it }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("TOP MOVERS / MARKET FEED")
                TopAssetFeedList(livePrices)
            }
        }

        if (activeTab == "ALERTS") {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("ALERTS & WATCHLIST")
                AlertsWatchlistPreviewPanel()
            }
        }

        if (activeTab == "SYSTEM") {
            item {
                Spacer(modifier = Modifier.height(8.dp))
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
                        text = "AI MARKET INTELLIGENCE COCKPIT",
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
                    text = "LOCAL SNAPSHOT : ACTIVE",
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
                    text = "AI CORE : LOCAL",
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
                text = "RISK-ON",
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
        Text("SOURCE: ORACLE QUANT ENGINE [LS]", color = T_TextSecondary, fontSize = 8.4.sp, fontFamily = FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.35f))
        Text("LOCAL MATRIX : ACTIVE", color = T_Green, fontSize = 8.4.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        Text("API COST: 0", color = T_TextSecondary, fontSize = 8.4.sp, fontFamily = FontFamily.SansSerif, maxLines = 1, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
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
        Text(label.uppercase(), color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(value.uppercase(), color = valueColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
        Text(label.uppercase(), color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
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
                Text("SIGNAL HEAT: ${asset.heat}", color = if(asset.heat > 80) T_Green else T_Gold, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("MOMENTUM: ${asset.momentum.uppercase()}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("PRESSURE: ${asset.pressure.uppercase()}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("PRIORITY: ${asset.priority.uppercase()}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("VOL: ${asset.volume.uppercase()}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text("RISK: ${asset.riskTag}", color = when(asset.riskTag){
                        "LOW" -> T_Green
                        "MEDIUM" -> T_Gold
                        "HIGH" -> T_Orange
                        else -> T_Red
                    }, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("READY: ${asset.readiness}", color = when(asset.readiness){
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
            text = label.uppercase(),
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
                Text("WATCHLIST: 8", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("BREAKOUT CANDIDATES: 3", color = T_Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("RISK ALERTS: 1", color = T_Orange, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("EXPIRED SIGNALS: 2", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
        StateBox("LOADING ORACLE FEED...", T_Gold)
        StateBox("NO MARKET DATA AVAILABLE", T_TextMuted)
        StateBox("DATA STALE — REFRESH REQUIRED", T_Orange)
        StateBox("USING LOCAL FALLBACK SNAPSHOT", T_Cyan)
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

private data class OracleInsightRow(
    val coin: String,
    val timeframe: String,
    val direction: String,
    val change: String,
    val target: String,
    val summary: String
)

@Composable
private fun CompactMarketHeader(left: String, middle: String, right: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(left, color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.65f))
        Text(middle, color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.15f))
        Text(right, color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.75f))
    }
    Divider(color = T_BorderHigh, thickness = 0.45.dp)
}

@Composable
private fun CompactMarketRow(left: String, middle: String, right: String, rightColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(left, color = T_TextPrimary, fontSize = 10.8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.65f))
        Text(middle, color = T_TextPrimary, fontSize = 11.4.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(1.15f))
        Text(right, color = rightColor, fontSize = 10.6.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(0.75f))
    }
}

@Composable
private fun CompactOracleInsight(item: OracleInsightRow) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(item.coin, color = T_TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(5.dp))
            Text("NEXT ${item.timeframe.removePrefix("NEXT ")}", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Spacer(modifier = Modifier.width(5.dp))
            Text(item.direction, color = if (item.direction == "LONG") T_Green else T_Red, fontSize = 8.2.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Text("[${item.change}]", color = if (item.direction == "LONG") T_Green else T_Red, fontSize = 8.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, maxLines = 1)
    }
    Spacer(modifier = Modifier.height(2.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("TARGET", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(item.target, color = T_TextPrimary, fontSize = 9.8.sp, fontFamily = FontFamily.Monospace, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(item.summary, color = T_TextSecondary, fontSize = 10.1.sp, fontFamily = FontFamily.SansSerif, lineHeight = 12.6.sp)
}

@Composable
fun OracleReferenceIndexPanel() {
    val rows = listOf(
        Triple("BTC", "62,777.64", "+7.12%"),
        Triple("ETH", "1,668.81", "+112.10%"),
        Triple("XRP", "1.1054", "-52.64%"),
        Triple("SOL", "69.6700", "+140.16%"),
        Triple("ADA", "0.1524", "+192.85%")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Bg)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("SOURCE: LOCAL SNAPSHOT [LS]", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text("Last update: 12s", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(6.dp))
        CompactMarketHeader("TICKER", "PRICE", "24H %")
        rows.forEach { (ticker, price, change) ->
            CompactMarketRow(
                left = ticker,
                middle = price,
                right = change,
                rightColor = if (change.startsWith("+")) T_Green else T_Red
            )
        }
    }
}

@Composable
fun RealTimeMarketPricePanel() {
    val rows = listOf(
        Triple("BTC", "62,774.82", "-2.03%"),
        Triple("ETH", "1,668.81", "-3.54%"),
        Triple("SOL", "69.6600", "-3.22%"),
        Triple("BNB", "578.26", "-2.41%"),
        Triple("XRP", "1.1054", "-2.02%"),
        Triple("DOGE", "0.0792", "-3.61%"),
        Triple("ADA", "0.1524", "-4.57%"),
        Triple("AVAX", "6.4320", "+1.95%"),
        Triple("LINK", "14.10", "+3.20%"),
        Triple("TON", "6.80", "+0.80%")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Bg)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Market data: Mock", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text("Feed status: Stable", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(6.dp))
        CompactMarketHeader("ASSET", "LAST", "CHG")
        rows.forEach { (asset, last, chg) ->
            CompactMarketRow(
                left = asset,
                middle = last,
                right = chg,
                rightColor = if (chg.startsWith("+")) T_Green else T_Red
            )
        }
    }
}

@Composable
fun OracleAnalyticalInsightsPanel() {
    val insights = listOf(
        OracleInsightRow("SOL", "NEXT 48 HOURS", "LONG", "+56.1234%", "0.5140", "A massive surge in Solana DEX trading volume paired with positive derivative funding rates indicates strong bullish momentum. Institutional accumulation at the support level has established a robust base, and technical indicators point to an imminent breakout attempt."),
        OracleInsightRow("XRP", "NEXT 24 HOURS", "SHORT", "+57.6624%", "0.4680", "Continued regulatory uncertainty and a sudden transfer of private capital from exchange wallets to major spot exchanges indicate imminent distribution pressure. XRP has rejected resistance and may revisit lower levels."),
        OracleInsightRow("ADA", "NEXT 7 DAYS", "LONG", "+237.2703%", "0.5140", "Cardano's network upgrade is successfully processing over 120,000 smart contracts, driving increased wallet activity and transaction throughput. Momentum has improved after an oversold reversal.")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Bg)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("SOURCE: ORACLE QUANT ENGINE [LS]", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text("Last update: 45s", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(6.dp))
        insights.forEachIndexed { index, item ->
            CompactOracleInsight(item)
            if (index != insights.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = T_BorderHigh, thickness = 0.45.dp)
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun MarketFeedIntelligencePanel() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
            Text("SOURCE: CURATED STATIC FEED [LS]", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text("Feed status: Stable", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
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
            val color = when(status) { "BULLISH" -> T_Green; "BEARISH" -> T_Red; else -> T_Cyan }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, T_BorderHigh, RoundedCornerShape(4.dp))
                    .background(T_Surface)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(source, color = T_TextPrimary, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(age, color = T_TextMuted, fontSize = 7.6.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }
                    Box(modifier = Modifier.background(color.copy(alpha=0.10f), RoundedCornerShape(2.dp)).border(0.5.dp, color, RoundedCornerShape(2.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                        Text(status, color = color, fontSize = 7.1.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(headline, color = T_TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, lineHeight = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(paragraph, color = T_TextSecondary, fontSize = 10.4.sp, fontFamily = FontFamily.SansSerif, lineHeight = 13.2.sp)
            }
            Spacer(modifier = Modifier.height(7.dp))
        }
    }
}

@Composable
fun FxOpenMarketIndexPanel() {
    val fxList = listOf(
        Triple("USD", "US Dollar", "118.42"),
        Triple("EUR", "Euro", "128.65"),
        Triple("GBP", "British Pound", "151.10"),
        Triple("SAR", "Saudi Riyal", "31.58"),
        Triple("AED", "UAE Dirham", "32.24")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Bg)
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Market data: Mock", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            Text("Last update: 12s", color = T_TextMuted, fontSize = 7.8.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
            Text("CCY", color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.55f))
            Text("CURRENCY", color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.4f))
            Text("RATE (BDT)", color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.95f))
        }
        Divider(color = T_BorderHigh, thickness = 0.45.dp)
        fxList.forEach { (code, name, rate) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(code, color = T_TextPrimary, fontSize = 10.8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.55f))
                Text(name, color = T_TextSecondary, fontSize = 9.4.sp, fontFamily = FontFamily.SansSerif, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1.4f))
                Text(rate, color = T_TextPrimary, fontSize = 10.8.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"), modifier = Modifier.weight(0.95f))
            }
        }
    }
}

