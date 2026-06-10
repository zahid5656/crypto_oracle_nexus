package com.example.ui

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
import com.example.R
import com.example.model.NewsItem
import com.example.model.OracleAnalysisResponse
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.example.ui.theme.DarkBackground

// Terminal Colors - Institutional Grade Apple Ecosystem Style
private val T_Bg = DarkBackground
private val T_Surface = Color(0xFF111112)
private val T_Border = Color(0xFF1C1C1E)
private val T_BorderHigh = Color(0xFF2C2C2E)
private val T_TextPrimary = Color(0xFFFFFFFF)
private val T_TextSecondary = Color(0xFF8E8E93)
private val T_TextMuted = Color(0xFF636366)
private val T_Green = Color(0xFF34C759) 
private val T_Red = Color(0xFFFF3B30)   
private val T_Cyan = Color(0xFF32ADE6)  
private val T_Gold = Color(0xFFFFCC00)  

@Composable
fun HomeScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val newsFeed by viewModel.newsFeedData.collectAsState()
    val isBengali by viewModel.isBengali.collectAsState()
    val livePrices by viewModel.livePrices.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(T_Bg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            TerminalHeaderBlock(viewModel)
        }

        item {
            SectionHeader("ORACLE REFERENCE INDEX (REAL-TIME)")
            TopCoinsDenseTable(newsFeed, livePrices)
        }
        
        item {
            SectionHeader("MARKET DASHBOARD (GLOBAL BINANCE SYNC)")
            RealTimeDenseDashboard()
        }

        item {
            SectionHeader("ORACLE ANALYTICAL INSIGHTS")
        }
        items(newsFeed.deepInsights) { insight ->
            var isLocalBengali by remember { mutableStateOf(false) }
            DeepInsightTerminalBlock(
                insight = insight,
                isBengali = isLocalBengali,
                onToggleLanguage = { isLocalBengali = !isLocalBengali }
            )
        }

        item {
            SectionHeader("MARKET FEED & INTELLIGENCE")
        }
        items(newsFeed.newsList) { article ->
            var isLocalBengali by remember { mutableStateOf(false) }
            NewsTerminalBlock(article, isLocalBengali, onToggleLanguage = { isLocalBengali = !isLocalBengali })
        }

        item {
            SectionHeader("FX OPEN-MARKET INDEX")
            var isLocalBengali by remember { mutableStateOf(false) }
            LiveCurrencyTerminalBlock(
                isBengali = isLocalBengali,
                onToggleLanguage = { isLocalBengali = !isLocalBengali }
            )
        }
    }
}

@Composable
fun TerminalHeaderBlock(viewModel: CryptoViewModel) {
    val isLiveConnected by viewModel.isLiveConnected.collectAsState()
    val hasGeminiApiKey by viewModel.hasGeminiApiKey.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(horizontal = 12.dp, vertical = 10.dp)
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
                Text(
                    text = "NEXUS TERMINAL v3.1",
                    color = T_TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            TerminalClockWidget()
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                    text = if (isLiveConnected) "BINANCE WSS : ACTIVE" else "LOCAL INDEX : DEGRADED",
                    color = if (isLiveConnected) T_Green else T_Gold,
                    fontSize = 10.sp,
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
                    text = if (hasGeminiApiKey) "GEMINI PRO : ONLINE" else "SIMULATOR : ACTIVE",
                    color = if (hasGeminiApiKey) T_Cyan else T_Gold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
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
        fontWeight = FontWeight.Medium
    )
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
            color = T_TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun TopCoinsDenseTable(newsFeed: OracleAnalysisResponse, livePrices: Map<String, Double>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TICKER", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.3f))
            Text("PRICE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
            Text("24H %", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.3f), textAlign = TextAlign.End)
        }
        
        val symbols = listOf("BTC", "ETH", "XRP", "SOL", "ADA")
        symbols.forEach { sym ->
            val spot = newsFeed.spotSignals.find { it.coinSymbol == sym }
            val price = livePrices["${sym}USDT"] ?: spot?.currentPrice ?: 0.0
            val change = spot?.growthPotentialPct ?: 0.0
            DenseTickerRow(sym, price, change)
        }
    }
}

@Composable
fun DenseTickerRow(symbol: String, price: Double, changePct: Double) {
    val animatedPrice = animateFloatAsState(
        targetValue = price.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "PriceAnim"
    ).value

    var prevPrice by remember { mutableStateOf(price) }
    var priceFlashedUp by remember { mutableStateOf<Boolean?>(null) } 

    LaunchedEffect(price) {
        if (price != prevPrice) {
            priceFlashedUp = price > prevPrice
            delay(300)
            priceFlashedUp = null
            prevPrice = price
        }
    }

    val priceColor = when (priceFlashedUp) {
        true -> T_Green
        false -> T_Red
        null -> T_TextPrimary
    }

    val isPositive = changePct >= 0
    val changeColor = if (isPositive) T_Green else T_Red
    val formattedPrice = if (animatedPrice >= 100) String.format("%,.2f", animatedPrice) else String.format("%.4f", animatedPrice)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbol,
            color = T_TextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = formattedPrice,
            color = priceColor,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f),
            textAlign = TextAlign.End
        )
        Text(
            text = String.format("%+.2f%%", changePct),
            color = changeColor,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.3f),
            textAlign = TextAlign.End
        )
    }
}

data class MarketAsset(
    val symbol: String,
    val price: Double,
    val priceChangePercent: Double
)

@Composable
fun RealTimeDenseDashboard() {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
    ) {
        if (isLoading) {
            Text("CONNECTING TO BINANCE WSS...", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 12.dp))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ASSET", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.3f))
                Text("LAST", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                Text("CHG", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.3f), textAlign = TextAlign.End)
            }
            assets.forEach { asset ->
                DenseTickerRow(asset.symbol, asset.price, asset.priceChangePercent)
            }
        }
    }
}

@Composable
fun DeepInsightTerminalBlock(
    insight: com.example.model.DeepInsightItem,
    isBengali: Boolean,
    onToggleLanguage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleLanguage() }
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Bg)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = insight.coinSymbol,
                    color = T_TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) when (insight.timeframe) {
                        "Next 24 Hours" -> "আগামী ২৪ ঘন্টা"
                        "Next 48 Hours" -> "আগামী ৪৮ ঘন্টা"
                        "Next 7 Days" -> "আগামী ৭ দিন"
                        else -> insight.timeframe
                    }.uppercase() else insight.timeframe.uppercase(),
                    color = T_TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            val (dirLabel, dirColor) = if (insight.direction.uppercase() == "PUMP") Pair(if (isBengali) "পাম্প" else "LONG", T_Green) else Pair(if (isBengali) "ডাম্প" else "SHORT", T_Red)
            Text(
                text = "[$dirLabel +${insight.expectedChangePct}%]",
                color = dirColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isBengali) "লক্ষ্য" else "TARGET",
                color = T_TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format("%.4f", insight.targetPrice),
                color = T_TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (isBengali) insight.whyBengali else insight.whyEnglish,
            color = T_TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun NewsTerminalBlock(article: NewsItem, isBengali: Boolean, onToggleLanguage: () -> Unit) {
    val title = if (isBengali) (article.titleBengali ?: article.title) else article.title
    val summary = if (isBengali) (article.summaryBengali ?: article.summary) else article.summary
    val sentimentColor = when (article.sentiment.uppercase()) {
        "BULLISH" -> T_Green
        "BEARISH" -> T_Red
        else -> T_TextMuted
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleLanguage() }
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Bg)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val sourceText = if (isBengali) article.sourceBengali ?: article.source else article.source
            val timeAgoText = if (isBengali) article.timeAgoBengali ?: article.timeAgo else article.timeAgo
            Text(
                text = "${sourceText.uppercase()} • ${timeAgoText.uppercase()}",
                color = T_TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .background(sentimentColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                val sentimentText = when (article.sentiment.uppercase()) {
                    "BULLISH" -> if (isBengali) "তেজি" else "BULLISH"
                    "BEARISH" -> if (isBengali) "মন্দা" else "BEARISH"
                    else -> if (isBengali) "নিরপেক্ষ" else "NEUTRAL"
                }
                Text(
                    text = sentimentText.uppercase(),
                    color = sentimentColor,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = title,
            color = T_TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
            fontFamily = FontFamily.SansSerif
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = summary,
            color = T_TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun LiveCurrencyTerminalBlock(isBengali: Boolean, onToggleLanguage: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleLanguage() }
            .background(T_Bg)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("CCY", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.3f))
            Text("RATE (BDT)", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(0.7f), textAlign = TextAlign.End)
        }
        
        if (isBengali) {
            DenseCurrencyRow("USD", "মার্কিন ডলার", "১১৮.৪২")
            DenseCurrencyRow("EUR", "ইউরো", "১২৮.৬৫")
            DenseCurrencyRow("GBP", "ব্রিটিশ পাউন্ড", "১৫১.১০")
            DenseCurrencyRow("SAR", "সৌদি রিয়াল", "৩১.৫৮")
            DenseCurrencyRow("AED", "ইউএই দিরহাম", "৩২.২৪")
        } else {
            DenseCurrencyRow("USD", "US Dollar", "118.42")
            DenseCurrencyRow("EUR", "Euro", "128.65")
            DenseCurrencyRow("GBP", "British Pound", "151.10")
            DenseCurrencyRow("SAR", "Saudi Riyal", "31.58")
            DenseCurrencyRow("AED", "UAE Dirham", "32.24")
        }
    }
}

@Composable
fun DenseCurrencyRow(code: String, name: String, rate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(0.5f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = code,
                color = T_Gold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                color = T_TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
        Text(
            text = rate,
            color = T_TextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End
        )
    }
}
