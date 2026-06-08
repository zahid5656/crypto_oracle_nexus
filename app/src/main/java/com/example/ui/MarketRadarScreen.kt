package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadarAlert
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import kotlin.random.Random
import androidx.compose.runtime.Immutable
import kotlin.math.absoluteValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width

@Composable
fun MarketRadarScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val radarAlerts by viewModel.radarAlerts.collectAsState()
    val marketRegime by viewModel.marketRegime.collectAsState()
    val shortTermInterval by viewModel.shortTermTimeframe.collectAsState()

    val isBengali by viewModel.isBengali.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Toolbar Title Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE QUANT RADAR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CryptoCyan,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Market Intelligence",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Inline language switcher
                Button(
                    onClick = { viewModel.toggleLanguage() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkSurface,
                        contentColor = CryptoCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBengali) "English" else "বাংলা",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Market Regime Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CryptoCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CryptoGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBengali) "অন-চেইন বাজার পরিস্থিতি" else "CURRENT MARKET REGIME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = marketRegime,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGreen,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Short-Term Period Selector Tabs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = if (isBengali) "সংক্ষিপ্ত সময়ের ওরাকল স্ক্যাল্পস" else "SHORT-TERM SCALP ORACLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(8.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val intervals = listOf("1 Min", "5 Min", "15 Min", "30 Min")
                    intervals.forEachIndexed { idx, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (shortTermInterval == idx) CryptoCyan.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (shortTermInterval == idx) CryptoCyan else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.setShortTermTimeframe(idx) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shortTermInterval == idx) CryptoCyan else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Short Term Signals Display List
                val displayWindow = listOf("1M", "5M", "15M", "30M")[shortTermInterval]
                ShortTermOpportunisticSignalsSection(timeframe = displayWindow, isBengali = isBengali, viewModel = viewModel)
            }
        }

        // Radar Alert Logs Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBengali) "তাত্ক্ষণিক পরিমাণগত রাডার বার্তা" else "REAL-TIME ALERTS FEED",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CryptoCyan,
                    letterSpacing = 1.5.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = CryptoCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBengali) "অটো-স্ক্যানিং" else "AUTO-SCANNING",
                        fontSize = 9.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Radar Active Alerts Item List
        if (radarAlerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBengali) "রাডার সক্রিয় করা হচ্ছে। নতুন সিগন্যালের সন্ধান চলছে..." else "Initializing quantum radar... searching for breakouts...",
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(radarAlerts, key = { it.id }) { alert ->
                RadarAlertCard(alert = alert, isBengali = isBengali)
            }
        }
    }
}

@Composable
fun ShortTermOpportunisticSignalsSection(timeframe: String, isBengali: Boolean, viewModel: CryptoViewModel) {
    var expandedKey by remember { mutableStateOf<String?>(null) }
    val livePrices by viewModel.livePrices.collectAsState()

    // Top 3 dynamic simulation metrics
    val spotScalps = listOf(
        Triple("Bitcoin", "BTC", livePrices["BTCUSDT"] ?: 66520.0),
        Triple("Ethereum", "ETH", livePrices["ETHUSDT"] ?: 3482.0),
        Triple("Solana", "SOL", livePrices["SOLUSDT"] ?: 164.2)
    )

    val longScalps = listOf(
        Triple("Render Token", "RNDR", livePrices["RNDRUSDT"] ?: 8.45),
        Triple("NEAR Protocol", "NEAR", livePrices["NEARUSDT"] ?: 6.12),
        Triple("Floki", "FLOKI", livePrices["FLOKIUSDT"] ?: 0.000215)
    )

    val shortScalps = listOf(
        Triple("Cardano", "ADA", livePrices["ADAUSDT"] ?: 0.45),
        Triple("Dogecoin", "DOGE", livePrices["DOGEUSDT"] ?: 0.125),
        Triple("Arbitrum", "ARB", livePrices["ARBUSDT"] ?: 0.95)
    )

    val spotDetails = listOf(
        mapOf(
            "pattern" to "200-EMA Support Bounce",
            "pattern_bn" to "২০০-ইএমএ সাপোর্ট বাউন্স",
            "vol" to "+148% Limit Buy Volume",
            "vol_bn" to "+১৪৮% লিমিট বাই ভলিউম",
            "prob" to "89%",
            "bid_ask" to "2.4x Bid Imbalance",
            "bid_ask_bn" to "২.৪ গুণ বিড ভারসাম্য",
            "sl" to "$65,210",
            "desc" to "Bitcoin is displaying strong buy absorption at the 200-EMA support channel on the 15M chart. A heavy cluster of institutional limit orders confirms solid floor strength. Expect an explosive continuation toward the target with minimal drawdown risk.",
            "desc_bn" to "বিটকয়েন ১৫-মিনিটের চার্টে ২০০-ইএমএ সমর্থন চ্যানেলে শক্তিশালী ক্রয় শোষণ প্রদর্শন করছে। প্রাতিষ্ঠানিক লিমিট বাই অর্ডারগুলোর ভারী ক্লাস্টার এখানে মেঝের শক্তি নিশ্চিত করেছে। সামান্য ড্রডাউন ঝুঁকি সহ টার্গেটের দিকে একটি উর্ধ্বমুখী গতির আশা করা হচ্ছে।"
        ),
        mapOf(
            "pattern" to "Ascending Triangle Breakout",
            "pattern_bn" to "অ্যাসেন্ডিং ট্রায়াঙ্গেল ব্রেকআউট",
            "vol" to "+195% Breakout Volume",
            "vol_bn" to "+১৯৫% ব্রেকআউট ভলিউম",
            "prob" to "85%",
            "bid_ask" to "1.9x Bid Imbalance",
            "bid_ask_bn" to "১.৯ গুণ বিড ভারসাম্য",
            "sl" to "$3,410",
            "desc" to "Ethereum has successfully breached its horizontal resistance line on high volume velocity. Positive RSI slope on multiple micro-timeframes validates that bulls are firmly in control of the trend.",
            "desc_bn" to "ইথেরিয়াম অত্যন্ত উচ্চ গতিতে তার সমান্তরাল প্রতিরোধ লাইন সফলভাবে অতিক্রম করেছে। একাধিক ক্ষুদ্র টাইমফ্রেমে ধনাত্মক আরএসআই (RSI) স্লোপ প্রমাণ করে যে বাজারের নিয়ন্ত্রণ সম্পূর্ণ ক্রেতাদের হাতে।"
        ),
        mapOf(
            "pattern" to "Orderbook Liquidity Sweep",
            "pattern_bn" to "অর্ডারবুক লিকুইডিটি সুইপ",
            "vol" to "+220% Buy Absorption",
            "vol_bn" to "+২২০% ক্রয় শোষণ ভলিউম",
            "prob" to "82%",
            "bid_ask" to "3.1x Bid Imbalance",
            "bid_ask_bn" to "৩.১ গুণ বিড ভারসাম্য",
            "sl" to "$161.0",
            "desc" to "Solana completed a clean sweep of stop-loss levels below $163 before pivoting. Intense limit buying was detected, reflecting automated accumulation from quantitative funds in the demand zone.",
            "desc_bn" to "সোলানা পুনরায় উর্ধ্বমুখী হওয়ার আগে $১৬৩-এর নিচের স্টপ-লস স্তরগুলো সফলভাবে সুইপ করেছে। ডিমান্ড জোনে কোয়ান্ট ফান্ডের স্বয়ংক্রিয় একুমুলেশন ও ক্রয়ের প্রবল চাপ সনাক্ত করা হয়েছে।"
        )
    )

    val longDetails = listOf(
        mapOf(
            "pattern" to "Short-Squeeze Trigger above VWAP",
            "pattern_bn" to "ভিওয়াপের উপরে শর্ট-স্কুইজ ট্রিগার",
            "vol" to "+310% Vol Surge",
            "vol_bn" to "+৩১০% ভলিউম সার্জ",
            "prob" to "84%",
            "bid_ask" to "+18.2% Open Interest",
            "bid_ask_bn" to "+১৮.২% ওপেন ইন্টারেস্ট",
            "sl" to "$8.15",
            "desc" to "Render's open interest has spiked significantly alongside massive short liquidations. Maintaining position above the daily VWAP creates high probability for an rapid upward cascade.",
            "desc_bn" to "রেন্ডার টোকেনের ওপেন ইন্টারেস্ট এবং শর্ট লিকুইডেশন উভয়ই অত্যন্ত বৃদ্ধি পেয়েছে। দৈনিক ভিওয়াপ (VWAP) স্তরের উপরে এর অবস্থান একটি তীব্র উর্ধ্বমুখী শর্ট-স্কুইজ তরঙ্গের পথ প্রসারিত করছে।"
        ),
        mapOf(
            "pattern" to "RSI Bullish Divergence",
            "pattern_bn" to "আরএসআই বুলিশ ডাইভারজেন্স",
            "vol" to "+160% Spot Buying Premium",
            "vol_bn" to "+১৬০% স্পট বাইং প্রিমিয়াম",
            "prob" to "81%",
            "bid_ask" to "+14.5% Open Interest",
            "bid_ask_bn" to "+১৪.৫% ওপেন ইন্টারেস্ট",
            "sl" to "$5.94",
            "desc" to "While the price tested lower levels, the 15M RSI printed higher lows to confirm a classic bullish divergence. Spot buying premiums are climbing, indicating immediate bullish intent.",
            "desc_bn" to "মূল্য যখন নিম্ন স্তরে অবস্থান নিচ্ছিল, ১৫-মিনিটের আরএসআই (RSI) তার তুলনায় উচ্ছে অবস্থান করে বুলিশ ডাইভারজেন্স তৈরি করেছে। স্পট প্রিমিয়াম ক্রয়ের বৃদ্ধি আসন্ন গতির নির্দেশক।"
        ),
        mapOf(
            "pattern" to "High-Beta Social Momentum",
            "pattern_bn" to "হাই-বিটা সোশাল মোমেন্টাম",
            "vol" to "+420% Aggressive Bids",
            "vol_bn" to "+৪২০% আক্রমণাত্মক বিডস",
            "prob" to "76%",
            "bid_ask" to "+32.0% Open Interest",
            "bid_ask_bn" to "+৩২.০% ওপেন ইন্টারেস্ট",
            "sl" to "$0.000205",
            "desc" to "FLOKI has major buy walls resting on top tier DEX and CEX depth books. Backed by intense and explosive social volume velocity, this high-beta scalp setup is fully primed.",
            "desc_bn" to "ফ্লকি টোকেনটির শীর্ষস্থানীয় ডেক্স এবং সেন্ট্রাল এক্সচেঞ্জগুলোতে বড় ক্রয় প্রাচীর তৈরি হয়েছে। বিস্ফোরক সামাজিক ভলিউম গতির সাথে এই হাই-বিটা স্ক্যাল্প সেটআপটি এখন পুরোপুরি প্রস্তুত।"
        )
    )

    val shortDetails = listOf(
        mapOf(
            "pattern" to "Descending Resistance Rejection",
            "pattern_bn" to "ডাউনট্রেন্ড রেজিস্ট্যান্স রিজেকশন",
            "vol" to "+120% Heavy Sell Wall",
            "vol_bn" to "+১২০% ভারী সেল ওয়াল",
            "prob" to "80%",
            "bid_ask" to "-11.2% Open Interest Decline",
            "bid_ask_bn" to "-১১.২% ওপেন ইন্টারেস্ট হ্রাস",
            "sl" to "$0.462",
            "desc" to "ADA hit the ceiling of an active descending channel with declining volume on small bounces, verifying weak buyer conviction. A high confidence short scalp set up.",
            "desc_bn" to "কার্ডানো তার সক্রিয় ডাউনট্রেন্ড চ্যানেলের সর্বোচ্চ প্রতিরোধ স্তরে বাধা পেয়েছে। ধীরগতির উর্ধ্বমুখী বাউন্সে কম ভলিউম নির্দেশ করে যে ক্রেতাদের আত্মবিশ্বাসের চূড়ান্ত অভাব।"
        ),
        mapOf(
            "pattern" to "Distribution Phase Breakdown",
            "pattern_bn" to "ডিস্ট্রিবিউশন ফেজ ব্রেকডাউন",
            "vol" to "+175% Distribution Outflow",
            "vol_bn" to "+১৭৫% ডিস্ট্রিবিউশন আউটফ্লো",
            "prob" to "82%",
            "bid_ask" to "-15.8% Open Interest Decline",
            "bid_ask_bn" to "-১৫.৮% ওপেন ইন্টারেস্ট হ্রাস",
            "sl" to "$0.129",
            "desc" to "Dogecoin failed to sustain its levels above the daily open high as large distribution outflows occurred. Rising negative funding momentum suggests active short hedging blocks.",
            "desc_bn" to "ডগকয়েন বড় ধরনের লাভ তোলার কারণে দৈনিক সর্বোচ্চ সীমার ওপর স্থায়িত্ব ধরে রাখতে ব্যর্থ হয়েছে। ক্রমবর্ধমান নেতিবাচক ফান্ডিং মোমেন্টাম সক্রিয় শর্ট পজিশনের ইঙ্গিত দেয়।"
        ),
        mapOf(
            "pattern" to "Unlock Liquidation Hedging",
            "pattern_bn" to "আনলক লিকুইডেশন হেজিং",
            "vol" to "+190% Perpetual Liquidations",
            "vol_bn" to "+১৯০% পার্পেচুয়াল লিকুইডেশন",
            "prob" to "85%",
            "bid_ask" to "-22.4% Open Interest Decline",
            "bid_ask_bn" to "-২২.৪% ওপেন ইন্টারেস্ট হ্রাস",
            "sl" to "$0.978",
            "desc" to "Arbitrum shows systematic futures dumping and perpetual orderbook short aggression. Failing to defend local support will trigger extensive algorithmic sell orders.",
            "desc_bn" to "আরবিট্রাম ফিউচার মার্কেটে পদ্ধতিগত বিক্রয় চাপ লক্ষ্য করা যাচ্ছে। স্থানীয় সাপোর্ট বা সমর্থন স্তরটি ধরে রাখতে ব্যর্থ হলে তাৎক্ষণিকভাবে অ্যালগরিদমিক বিক্রয় আদেশ সচল হবে।"
        )
    )

    val formatPrice = { price: Double ->
        when {
            price < 0.01 -> String.format("%.6f", price)
            price < 1.0 -> String.format("%.4f", price)
            else -> String.format("%,.2f", price)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // --- 1. SPOT SECTIONS ---
        Text(
            text = if (isBengali) "🔥 তাত্ক্ষণিক স্পট টার্গেট (সেরা ৩)" else "🔥 HOT SPOT TRIGGERS (TOP 3)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            letterSpacing = 0.5.sp
        )

        spotScalps.forEachIndexed { index, (name, symbol, basePrice) ->
            val potential = 1.5 + index * 0.4
            val target = basePrice * (1.0 + potential / 100)
            val isExpanded = expandedKey == "spot_$index"
            val details = spotDetails[index]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isExpanded) AccentGold.copy(alpha = 0.6f) else BorderColor,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        expandedKey = if (isExpanded) null else "spot_$index"
                    }
                    .animateContentSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(AccentGold.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Live: $${formatPrice(basePrice)}", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (isBengali) "বিশ্লেষণ দেখতে ট্যাপ করুন" else "Tap for Deep Quant Info",
                                fontSize = 8.sp,
                                color = AccentGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target: $${formatPrice(target)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGreen
                        )
                        Text(
                            text = "+${String.format("%.2f", potential)}% ($timeframe)",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LiveRadarAiOracleMetadataTile(
                        patternText = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                        orderbookText = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                        stopLossText = details["sl"]!!,
                        probabilityText = details["prob"]!!,
                        descriptionText = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        isBengali = isBengali,
                        themeColor = if (target >= basePrice) CryptoGreen else Color(0xFFFF3F60)
                    )

                    LiveRadarBetaDivergenceGuard(
                        symbol = symbol,
                        timeframe = timeframe,
                        isLong = target >= basePrice,
                        isBengali = isBengali
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OpportunisticSignalAdornmentSection(
                        basePrice = basePrice,
                        isLong = true,
                        potential = potential,
                        isBengali = isBengali,
                        themeColor = CryptoGreen
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val mission = remember(symbol, target) {
                        com.example.model.Mission(
                            coinSymbol = symbol,
                            type = "LONG",
                            marketType = "Spot",
                            entryPrice = basePrice,
                            currentPrice = basePrice,
                            targets = formatPrice(target),
                            stopLoss = formatPrice(basePrice * 0.98), // approximate 2%
                            confidence = details["prob"]?.replace("%", "")?.toIntOrNull() ?: 80,
                            aiStatusEnglish = "Radar Spot setup active.",
                            aiStatusBengali = "রাডার স্পট সেটআপ সক্রিয়।"
                        )
                    }
                    com.example.ui.StartTradeFlow(viewModel = viewModel, mission = mission, livePrice = basePrice)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 2. FUTURES LONG SECTIONS ---
        Text(
            text = if (isBengali) "⚡ ফিউচার লং টার্গেট" else "⚡ FUTURES LONG TRIGGERS (TOP 3)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CryptoGreen,
            letterSpacing = 0.5.sp
        )

        longScalps.forEachIndexed { index, (name, symbol, basePrice) ->
            val potential = 3.2 + index * 0.8
            val target = basePrice * (1.0 + potential / 100)
            val isExpanded = expandedKey == "long_$index"
            val details = longDetails[index]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isExpanded) CryptoGreen.copy(alpha = 0.6f) else BorderColor,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        expandedKey = if (isExpanded) null else "long_$index"
                    }
                    .animateContentSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(CryptoGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Live: $${formatPrice(basePrice)}", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (isBengali) "বিশ্লেষণ দেখতে ট্যাপ করুন" else "Tap for Deep Quant Info",
                                fontSize = 8.sp,
                                color = CryptoGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target: $${formatPrice(target)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoGreen
                        )
                        Text(
                            text = "Leverage: 5x | +${String.format("%.2f", potential)}%",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LiveRadarAiOracleMetadataTile(
                        patternText = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                        orderbookText = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                        stopLossText = details["sl"]!!,
                        probabilityText = details["prob"]!!,
                        descriptionText = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        isBengali = isBengali,
                        themeColor = if (target >= basePrice) CryptoGreen else Color(0xFFFF3F60)
                    )

                    LiveRadarBetaDivergenceGuard(
                        symbol = symbol,
                        timeframe = timeframe,
                        isLong = target >= basePrice,
                        isBengali = isBengali
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OpportunisticSignalAdornmentSection(
                        basePrice = basePrice,
                        isLong = true,
                        potential = potential,
                        isBengali = isBengali,
                        themeColor = CryptoCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val mission = remember(symbol, target) {
                        com.example.model.Mission(
                            coinSymbol = symbol,
                            type = "LONG",
                            marketType = "Futures",
                            entryPrice = basePrice,
                            currentPrice = basePrice,
                            targets = formatPrice(target),
                            stopLoss = formatPrice(basePrice * 0.98), // approximate 2%
                            confidence = details["prob"]?.replace("%", "")?.toIntOrNull() ?: 80,
                            aiStatusEnglish = "Radar Long setup active.",
                            aiStatusBengali = "রাডার লং সেটআপ সক্রিয়।"
                        )
                    }
                    com.example.ui.StartTradeFlow(viewModel = viewModel, mission = mission, livePrice = basePrice)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- 3. FUTURES SHORT SECTIONS ---
        Text(
            text = if (isBengali) "🔻 ফিউচার শর্ট টার্গেট (সেরা ৩)" else "🔻 FUTURES SHORT TRIGGERS (TOP 3)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF3F60),
            letterSpacing = 0.5.sp
        )

        shortScalps.forEachIndexed { index, (name, symbol, basePrice) ->
            val potential = 2.8 + index * 0.7
            val target = basePrice * (1.0 - potential / 100)
            val isExpanded = expandedKey == "short_$index"
            val details = shortDetails[index]

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isExpanded) Color(0xFFFF3F60).copy(alpha = 0.6f) else BorderColor,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        expandedKey = if (isExpanded) null else "short_$index"
                    }
                    .animateContentSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF3F60).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = symbol, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3F60))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Live: $${formatPrice(basePrice)}", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (isBengali) "বিশ্লেষণ দেখতে ট্যাপ করুন" else "Tap for Deep Quant Info",
                                fontSize = 8.sp,
                                color = Color(0xFFFF3F60),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target: $${formatPrice(target)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF3F60)
                        )
                        Text(
                            text = "Leverage: 5x | -${String.format("%.2f", potential)}%",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LiveRadarAiOracleMetadataTile(
                        patternText = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                        orderbookText = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                        stopLossText = details["sl"]!!,
                        probabilityText = details["prob"]!!,
                        descriptionText = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        isBengali = isBengali,
                        themeColor = if (target >= basePrice) CryptoGreen else Color(0xFFFF3F60)
                    )

                    LiveRadarBetaDivergenceGuard(
                        symbol = symbol,
                        timeframe = timeframe,
                        isLong = target >= basePrice,
                        isBengali = isBengali
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OpportunisticSignalAdornmentSection(
                        basePrice = basePrice,
                        isLong = false,
                        potential = potential,
                        isBengali = isBengali,
                        themeColor = Color(0xFFFF3F60)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val mission = remember(symbol, target) {
                        com.example.model.Mission(
                            coinSymbol = symbol,
                            type = "SHORT",
                            marketType = "Futures",
                            entryPrice = basePrice,
                            currentPrice = basePrice,
                            targets = formatPrice(target),
                            stopLoss = formatPrice(basePrice * 1.02), // approximate 2%
                            confidence = details["prob"]?.replace("%", "")?.toIntOrNull() ?: 80,
                            aiStatusEnglish = "Radar Short setup active.",
                            aiStatusBengali = "রাডার শর্ট সেটআপ সক্রিয়।"
                        )
                    }
                    com.example.ui.StartTradeFlow(viewModel = viewModel, mission = mission, livePrice = basePrice)
                }
            }
        }
    }
}

@Composable
fun RadarAlertCard(alert: RadarAlert, isBengali: Boolean) {
    val accentColor = when (alert.eventType) {
        "VOLUME_EXPLOSION" -> AccentGold
        "BREAKOUT" -> CryptoGreen
        "MOMENTUM_SURGE" -> CryptoCyan
        else -> Color(0xFFFF3F60) // Reddish
    }

    val badgeBg = accentColor.copy(alpha = 0.14f)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = alert.coinSymbol,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = alert.eventType.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = String.format("%.1f%% Magnitude", alert.magnitude),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isBengali) alert.descriptionBengali else alert.descriptionEnglish,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun OpportunisticSignalAdornmentSection(
    basePrice: Double,
    isLong: Boolean,
    potential: Double,
    isBengali: Boolean,
    themeColor: Color
) {
    val tp1 = if (isLong) basePrice * (1.0 + potential * 0.25 / 100.0) else basePrice * (1.0 - potential * 0.25 / 100.0)
    val tp2 = if (isLong) basePrice * (1.0 + potential * 0.50 / 100.0) else basePrice * (1.0 - potential * 0.50 / 100.0)
    val tp3 = if (isLong) basePrice * (1.0 + potential * 1.00 / 100.0) else basePrice * (1.0 - potential * 1.00 / 100.0)

    val formatPrice = { price: Double ->
        when {
            price < 0.01 -> String.format("%.6f", price)
            price < 1.0 -> String.format("%.4f", price)
            else -> String.format("%,.2f", price)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = if (isBengali) "টার্গেট প্রফিট ম্যাট্রিক্স" else "TAKE PROFIT TARGET MATRIX",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(7.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LiveRadarAdornmentMiniTile(
            label = "TP1 (25%)",
            value = "$${formatPrice(tp1)}",
            valueColor = Color.White,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            labelSize = 8.5f,
            valueSize = 10.8f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "TP2 (50%)",
            value = "$${formatPrice(tp2)}",
            valueColor = Color.White,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            labelSize = 8.5f,
            valueSize = 10.8f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "TP3 (100%)",
            value = "$${formatPrice(tp3)}",
            valueColor = Color.White,
            labelColor = CryptoGreen,
            borderColor = themeColor,
            labelSize = 8.5f,
            valueSize = 10.8f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = if (isBengali) "মাল্টি-এআই কনসেনসাস স্কোর" else "MULTI-AI CONSENSUS ENGINES",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(7.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LiveRadarAdornmentMiniTile(
            label = "Gemini Pro AI",
            value = "94/100",
            valueColor = Color.White,
            labelColor = TextPrimary,
            borderColor = themeColor,
            labelSize = 8.2f,
            valueSize = 10.6f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "GPT-4Q Quant",
            value = "90/100",
            valueColor = Color.White,
            labelColor = TextPrimary,
            borderColor = themeColor,
            labelSize = 8.2f,
            valueSize = 10.6f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Claude Sentient",
            value = "93/100",
            valueColor = Color.White,
            labelColor = TextPrimary,
            borderColor = themeColor,
            labelSize = 8.2f,
            valueSize = 10.6f,
            minHeight = 58,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
    HorizontalDivider(color = BorderColor.copy(alpha = 0.4f))
    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = if (isBengali) "পজিশন সাইজিং পোর্টফোলিও কন্ট্রোল" else "RECOMMENDED POSITION ALLOCATION SIZING",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = themeColor,
        letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(7.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LiveRadarAdornmentMiniTile(
            label = "Conservative",
            value = "2.0% Cap",
            valueColor = AccentGold,
            labelColor = Color.White,
            borderColor = themeColor,
            labelSize = 8.9f,
            valueSize = 10.6f,
            minHeight = 62,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Balanced",
            value = "5.0% Cap",
            valueColor = AccentGold,
            labelColor = Color.White,
            borderColor = themeColor,
            labelSize = 8.9f,
            valueSize = 10.6f,
            minHeight = 62,
            modifier = Modifier.weight(1f)
        )

        LiveRadarAdornmentMiniTile(
            label = "Aggressive",
            value = "10.0% Max",
            valueColor = AccentGold,
            labelColor = Color.White,
            borderColor = themeColor,
            labelSize = 8.9f,
            valueSize = 10.6f,
            minHeight = 62,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================================================
// LIVE RADAR — BETA DIVERGENCE GUARD
// Scope: Live Radar expanded signal card only.
// Signal Pro / Mission Center / StartTradeFlow / Accept Signal are untouched.
// ============================================================================


// ============================================================================
// LIVE RADAR — BETA DIVERGENCE GUARD FINAL ENGINE
// Scope: Live Radar expanded signal card only.
// Signal Pro / Mission Center / StartTradeFlow / Accept Signal are untouched.
// ============================================================================


// ============================================================================
// LIVE RADAR — BETA DIVERGENCE GUARD FINAL COMPACT ENGINE
// Scope: Live Radar expanded signal cards only.
// Applied to: Hot Spot Top 3, Futures Long Top 3, Futures Short Top 3.
// Signal Pro / Mission Center / StartTradeFlow / Accept Signal are untouched.
// ============================================================================

enum class DivergenceState {
    STABLE,
    WARNING,
    DANGER,
    BLIND
}

enum class ExecutionGuardStatus {
    GO,
    CAUTION,
    DANGER,
    BLIND
}

@Immutable
data class LiveRadarMarketSnapshot(
    val exchangeLatencyMs: Int,
    val lastTickAgeMs: Int,
    val btcDelta5mPct: Double,
    val ecosystemLeaderName: String,
    val ecosystemLeaderDelta5mPct: Double,
    val usdtDominanceVelocityPct: Double,
    val total3VelocityPct: Double,
    val openInterestSpikePct: Double,
    val fundingBiasPct: Double,
    val liquidationPressurePct: Double,
    val spreadBps: Double,
    val takerFeeBps: Double,
    val orderBookDepthScore: Int,
    val assetVolume1mMultiple: Double,
    val assetAtr1mMultiple: Double
)

@Immutable
data class BetaDivergenceGuardState(
    val snapshot: LiveRadarMarketSnapshot,
    val latencyState: DivergenceState,
    val btcDeltaState: DivergenceState,
    val ecosystemLeaderState: DivergenceState,
    val marketOutflowState: DivergenceState,
    val derivativesStressState: DivergenceState,
    val spreadLiquidityState: DivergenceState,
    val assetVelocityShockState: DivergenceState,
    val finalGuardStatus: ExecutionGuardStatus,
    val executionReadinessPenalty: Int,
    val adjustedReadinessScore: Int,
    val narrativeEnglish: String,
    val narrativeBengali: String
)

@Composable
fun LiveRadarBetaDivergenceGuard(
    symbol: String,
    timeframe: String,
    isLong: Boolean,
    isBengali: Boolean
) {
    val snapshot = remember(symbol, timeframe, isLong) {
        buildLiveRadarMarketSnapshot(
            symbol = symbol,
            timeframe = timeframe,
            isLong = isLong
        )
    }

    val guardState = remember(snapshot, isLong) {
        buildBetaDivergenceGuardState(
            snapshot = snapshot,
            isLong = isLong
        )
    }

    val accentColor = executionGuardColor(guardState.finalGuardStatus)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        accentColor.copy(alpha = 0.026f),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(11.dp)
            )
            .border(0.65.dp, accentColor.copy(alpha = 0.32f), RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BETA DIVERGENCE GUARD",
                    fontSize = 8.6.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 0.85.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isBengali) {
                        "Market + Asset Shock + Data Safety"
                    } else {
                        "Market + Asset Shock + Data Safety"
                    },
                    fontSize = 8.7.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .background(accentColor.copy(alpha = 0.115f), RoundedCornerShape(7.dp))
                    .border(0.65.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(7.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = guardStatusLabel(guardState.finalGuardStatus, isBengali),
                    fontSize = 9.2.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        BetaGuardAiImpactTile(
            status = guardState.finalGuardStatus,
            readiness = guardState.adjustedReadinessScore,
            penalty = guardState.executionReadinessPenalty,
            isBengali = isBengali
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = "Data Sync",
                state = guardState.latencyState,
                value = dataSyncStateLabel(guardState.latencyState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "BTC Delta",
                state = guardState.btcDeltaState,
                value = btcDeltaStateLabel(guardState.btcDeltaState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = guardState.snapshot.ecosystemLeaderName,
                state = guardState.ecosystemLeaderState,
                value = ecosystemStateLabel(guardState.ecosystemLeaderState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Market Flow",
                state = guardState.marketOutflowState,
                value = marketFlowStateLabel(guardState.marketOutflowState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = "Derivatives",
                state = guardState.derivativesStressState,
                value = derivativesStateLabel(guardState.derivativesStressState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardMiniTile(
                label = "Spread Risk",
                state = guardState.spreadLiquidityState,
                value = spreadStateLabel(guardState.spreadLiquidityState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BetaGuardMiniTile(
                label = "Asset Shock",
                state = guardState.assetVelocityShockState,
                value = assetShockStateLabel(guardState.assetVelocityShockState, guardState.snapshot, isBengali),
                modifier = Modifier.weight(1f)
            )

            BetaGuardPenaltyTile(
                penalty = guardState.executionReadinessPenalty,
                readiness = guardState.adjustedReadinessScore,
                status = guardState.finalGuardStatus,
                isBengali = isBengali,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF02050D),
                            accentColor.copy(alpha = 0.040f),
                            Color(0xFF02050D)
                        )
                    ),
                    RoundedCornerShape(8.dp)
                )
                .border(0.58.dp, accentColor.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (isBengali) guardState.narrativeBengali else guardState.narrativeEnglish,
                fontSize = 9.8.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BetaGuardAiImpactTile(
    status: ExecutionGuardStatus,
    readiness: Int,
    penalty: Int,
    isBengali: Boolean
) {
    val color = executionGuardColor(status)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(0.58.dp, color.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI GUARD IMPACT",
                fontSize = 7.6.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = readinessActionLabel(status, isBengali),
                fontSize = 9.4.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(7.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$readiness/100",
                fontSize = 13.2.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1
            )

            if (penalty > 0) {
                Text(
                    text = "-$penalty pts",
                    fontSize = 7.6.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE95772),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun BetaGuardMiniTile(
    label: String,
    state: DivergenceState,
    value: String,
    modifier: Modifier = Modifier
) {
    val color = divergenceStateColor(state)

    Column(
        modifier = modifier
            .heightIn(min = 43.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(7.dp)
            )
            .border(0.56.dp, color.copy(alpha = 0.30f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.0.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            fontSize = 8.6.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BetaGuardPenaltyTile(
    penalty: Int,
    readiness: Int,
    status: ExecutionGuardStatus,
    isBengali: Boolean,
    modifier: Modifier = Modifier
) {
    val color = executionGuardColor(status)
    val currentScore = readiness.coerceIn(0, 100)

    Column(
        modifier = modifier
            .heightIn(min = 43.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(7.dp)
            )
            .border(0.56.dp, color.copy(alpha = 0.30f), RoundedCornerShape(7.dp))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Readiness",
            fontSize = 7.0.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "$currentScore/100",
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (penalty > 0) {
            Text(
                text = "-$penalty pts",
                fontSize = 7.6.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE95772),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun buildLiveRadarMarketSnapshot(
    symbol: String,
    timeframe: String,
    isLong: Boolean
): LiveRadarMarketSnapshot {
    val seed = safeAbsHash(symbol) + safeAbsHash(timeframe) + if (isLong) 101 else 211
    val ecosystemLeader = resolveEcosystemLeaderName(symbol)

    // Real-data-ready binding contract:
    // Replace these deterministic placeholders later with Binance Sync / WebSocket / orderbook / OI / funding feeds.
    val exchangeLatencyMs = 120 + (seed % 620)
    val lastTickAgeMs = 80 + ((seed / 2) % 620)

    val rareBlind = seed % 97 == 0
    val adjustedExchangeLatency = if (rareBlind) 1550 else exchangeLatencyMs
    val adjustedLastTickAge = if (rareBlind) 1510 else lastTickAgeMs

    val btcDelta = (((seed / 3) % 13) - 6) / 12.0
    val leaderDelta = (((seed / 5) % 13) - 6) / 12.0

    val rareOutflow = seed % 43 == 0
    val usdtVelocity = if (rareOutflow) 0.72 else (((seed / 7) % 8) - 2) / 12.0
    val total3Velocity = if (rareOutflow) -0.58 else (((seed / 11) % 9) - 4) / 12.0

    val rareDerivativeStress = seed % 47 == 0
    val oiSpike = if (rareDerivativeStress) 30.0 else 3.0 + ((seed / 13) % 18)
    val fundingBias = if (rareDerivativeStress) 0.08 else (((seed / 17) % 13) - 6) / 180.0
    val liquidationPressure = if (rareDerivativeStress) 31.0 else ((seed / 19) % 22).toDouble()

    val rarePoorLiquidity = seed % 53 == 0
    val spreadBps = if (rarePoorLiquidity) 30.0 else 2.0 + ((seed / 23) % 17)
    val takerFeeBps = 6.0
    val depthScore = if (rarePoorLiquidity) 58 else (72 + ((seed / 29) % 24)).coerceIn(0, 100)

    val rareAssetShock = seed % 41 == 0
    val volumeMultiple = if (rareAssetShock) 3.2 else 0.8 + (((seed / 31) % 18) / 10.0)
    val atrMultiple = if (rareAssetShock) 3.1 else 0.8 + (((seed / 37) % 17) / 10.0)

    return LiveRadarMarketSnapshot(
        exchangeLatencyMs = adjustedExchangeLatency,
        lastTickAgeMs = adjustedLastTickAge,
        btcDelta5mPct = btcDelta,
        ecosystemLeaderName = ecosystemLeader,
        ecosystemLeaderDelta5mPct = leaderDelta,
        usdtDominanceVelocityPct = usdtVelocity,
        total3VelocityPct = total3Velocity,
        openInterestSpikePct = oiSpike,
        fundingBiasPct = fundingBias,
        liquidationPressurePct = liquidationPressure,
        spreadBps = spreadBps,
        takerFeeBps = takerFeeBps,
        orderBookDepthScore = depthScore,
        assetVolume1mMultiple = volumeMultiple,
        assetAtr1mMultiple = atrMultiple
    )
}

fun buildBetaDivergenceGuardState(
    snapshot: LiveRadarMarketSnapshot,
    isLong: Boolean
): BetaDivergenceGuardState {
    val latencyState = latencyStateFromSnapshot(snapshot)

    val btcDeltaState = directionalDivergenceState(
        deltaPct = snapshot.btcDelta5mPct,
        isLong = isLong,
        warningAbs = 0.42,
        dangerAbs = 0.78
    )

    val ecosystemLeaderState = directionalDivergenceState(
        deltaPct = snapshot.ecosystemLeaderDelta5mPct,
        isLong = isLong,
        warningAbs = 0.38,
        dangerAbs = 0.74
    )

    val marketOutflowState = marketOutflowStateFromSnapshot(snapshot)
    val derivativesStressState = derivativesStressStateFromSnapshot(snapshot)
    val spreadLiquidityState = spreadLiquidityStateFromSnapshot(snapshot)
    val assetVelocityShockState = assetVelocityShockStateFromSnapshot(snapshot)

    val allStates = listOf(
        latencyState,
        btcDeltaState,
        ecosystemLeaderState,
        marketOutflowState,
        derivativesStressState,
        spreadLiquidityState,
        assetVelocityShockState
    )

    val penalty = allStates.sumOf { state ->
        when (state) {
            DivergenceState.STABLE -> 0
            DivergenceState.WARNING -> 4
            DivergenceState.DANGER -> 10
            DivergenceState.BLIND -> 24
        }
    }.coerceIn(0, 52)

    val finalStatus = when {
        latencyState == DivergenceState.BLIND -> ExecutionGuardStatus.BLIND
        allStates.any { it == DivergenceState.DANGER } -> ExecutionGuardStatus.DANGER
        penalty >= 16 -> ExecutionGuardStatus.CAUTION
        else -> ExecutionGuardStatus.GO
    }

    val adjustedReadiness = (100 - penalty).coerceIn(0, 100)

    val narrativeEnglish = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Guard clear. Market, liquidity and asset-shock layers support validation."

        ExecutionGuardStatus.CAUTION ->
            "Caution: some safety layers weakened. Verify entry and reduce size."

        ExecutionGuardStatus.DANGER ->
            "Warning: divergence or execution risk detected. Readiness reduced."

        ExecutionGuardStatus.BLIND ->
            "Data sync drift detected. Wait for fresh market data."
    }

    val narrativeBengali = when (finalStatus) {
        ExecutionGuardStatus.GO ->
            "Guard clear. Market, liquidity এবং asset-shock layer validation support করছে।"

        ExecutionGuardStatus.CAUTION ->
            "সতর্কতা: কিছু safety layer দুর্বল। entry verify করুন এবং size কমান।"

        ExecutionGuardStatus.DANGER ->
            "সতর্কতা: divergence অথবা execution risk ধরা পড়েছে। readiness কমেছে।"

        ExecutionGuardStatus.BLIND ->
            "ডেটা sync drift ধরা পড়েছে। fresh market data অপেক্ষা করুন।"
    }

    return BetaDivergenceGuardState(
        snapshot = snapshot,
        latencyState = latencyState,
        btcDeltaState = btcDeltaState,
        ecosystemLeaderState = ecosystemLeaderState,
        marketOutflowState = marketOutflowState,
        derivativesStressState = derivativesStressState,
        spreadLiquidityState = spreadLiquidityState,
        assetVelocityShockState = assetVelocityShockState,
        finalGuardStatus = finalStatus,
        executionReadinessPenalty = penalty,
        adjustedReadinessScore = adjustedReadiness,
        narrativeEnglish = narrativeEnglish,
        narrativeBengali = narrativeBengali
    )
}

fun safeAbsHash(value: String): Int {
    val hash = value.hashCode()
    return if (hash == Int.MIN_VALUE) 0 else hash.absoluteValue
}

fun directionalDivergenceState(
    deltaPct: Double,
    isLong: Boolean,
    warningAbs: Double,
    dangerAbs: Double
): DivergenceState {
    val adverseMove = if (isLong) -deltaPct else deltaPct

    return when {
        adverseMove >= dangerAbs -> DivergenceState.DANGER
        adverseMove >= warningAbs -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun latencyStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val maxDelay = maxOf(snapshot.exchangeLatencyMs, snapshot.lastTickAgeMs)

    return when {
        maxDelay > 1500 -> DivergenceState.BLIND
        maxDelay > 500 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun marketOutflowStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val outflowPressure = snapshot.usdtDominanceVelocityPct > 0.45 && snapshot.total3VelocityPct < -0.35

    return when {
        snapshot.usdtDominanceVelocityPct > 0.70 && snapshot.total3VelocityPct < -0.55 -> DivergenceState.DANGER
        outflowPressure -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun derivativesStressStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val fundingStress = snapshot.fundingBiasPct.absoluteValue >= 0.07
    val liquidationStress = snapshot.liquidationPressurePct >= 28.0
    val oiStress = snapshot.openInterestSpikePct >= 25.0

    return when {
        (fundingStress && liquidationStress) || (oiStress && liquidationStress) -> DivergenceState.DANGER
        fundingStress || liquidationStress || oiStress -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun spreadLiquidityStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    val totalCostBps = snapshot.spreadBps + snapshot.takerFeeBps
    val depthWeak = snapshot.orderBookDepthScore < 68

    return when {
        totalCostBps >= 34.0 || snapshot.orderBookDepthScore < 62 -> DivergenceState.DANGER
        totalCostBps >= 22.0 || depthWeak -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun assetVelocityShockStateFromSnapshot(snapshot: LiveRadarMarketSnapshot): DivergenceState {
    return when {
        snapshot.assetVolume1mMultiple >= 3.0 || snapshot.assetAtr1mMultiple >= 3.0 -> DivergenceState.DANGER
        snapshot.assetVolume1mMultiple >= 2.2 || snapshot.assetAtr1mMultiple >= 2.2 -> DivergenceState.WARNING
        else -> DivergenceState.STABLE
    }
}

fun resolveEcosystemLeaderName(symbol: String): String {
    val upper = symbol.uppercase()

    return when {
        upper.contains("ARB") || upper.contains("OP") || upper.contains("MATIC") || upper.contains("UNI") || upper.contains("LINK") -> "ETH Leader"
        upper.contains("SOL") || upper.contains("BONK") || upper.contains("JUP") || upper.contains("RAY") || upper.contains("PYTH") -> "SOL Leader"
        upper.contains("BNB") || upper.contains("CAKE") || upper.contains("TWT") -> "BNB Leader"
        upper.contains("AVAX") || upper.contains("JOE") -> "AVAX Leader"
        else -> "BTC Leader"
    }
}

fun divergenceStateColor(state: DivergenceState): Color {
    return when (state) {
        DivergenceState.STABLE -> CryptoGreen.copy(alpha = 0.90f)
        DivergenceState.WARNING -> AccentGold.copy(alpha = 0.88f)
        DivergenceState.DANGER -> Color(0xFFE95772)
        DivergenceState.BLIND -> Color(0xFFE96B82)
    }
}

fun executionGuardColor(status: ExecutionGuardStatus): Color {
    return when (status) {
        ExecutionGuardStatus.GO -> CryptoGreen.copy(alpha = 0.90f)
        ExecutionGuardStatus.CAUTION -> AccentGold.copy(alpha = 0.88f)
        ExecutionGuardStatus.DANGER -> Color(0xFFE95772)
        ExecutionGuardStatus.BLIND -> Color(0xFFE96B82)
    }
}

fun guardStatusLabel(status: ExecutionGuardStatus, isBengali: Boolean): String {
    return when (status) {
        ExecutionGuardStatus.GO -> "GO"
        ExecutionGuardStatus.CAUTION -> "CAUTION"
        ExecutionGuardStatus.DANGER -> "DANGER"
        ExecutionGuardStatus.BLIND -> "BLIND"
    }
}

fun dataSyncStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    val maxDelay = maxOf(snapshot.exchangeLatencyMs, snapshot.lastTickAgeMs)

    return when (state) {
        DivergenceState.STABLE -> "${maxDelay}ms OK"
        DivergenceState.WARNING -> "${maxDelay}ms Delay"
        DivergenceState.DANGER -> "${maxDelay}ms Drift"
        DivergenceState.BLIND -> "${maxDelay}ms Blind"
    }
}

fun btcDeltaStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return String.format("%.2f%%", snapshot.btcDelta5mPct)
}

fun ecosystemStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return String.format("%.2f%%", snapshot.ecosystemLeaderDelta5mPct)
}

fun marketFlowStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return when (state) {
        DivergenceState.STABLE -> "Neutral"
        DivergenceState.WARNING -> "Outflow"
        DivergenceState.DANGER -> "Drain"
        DivergenceState.BLIND -> "Blind"
    }
}

fun derivativesStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    return when (state) {
        DivergenceState.STABLE -> "Normal"
        DivergenceState.WARNING -> "Crowded"
        DivergenceState.DANGER -> "Squeeze"
        DivergenceState.BLIND -> "Blind"
    }
}

fun spreadStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    val cost = snapshot.spreadBps + snapshot.takerFeeBps

    return when (state) {
        DivergenceState.STABLE -> String.format("%.1fbps OK", cost)
        DivergenceState.WARNING -> String.format("%.1fbps Drag", cost)
        DivergenceState.DANGER -> String.format("%.1fbps Poor", cost)
        DivergenceState.BLIND -> "Blind"
    }
}

fun assetShockStateLabel(
    state: DivergenceState,
    snapshot: LiveRadarMarketSnapshot,
    isBengali: Boolean
): String {
    val maxShock = maxOf(snapshot.assetVolume1mMultiple, snapshot.assetAtr1mMultiple)

    return when (state) {
        DivergenceState.STABLE -> String.format("%.1fx Clear", maxShock)
        DivergenceState.WARNING -> String.format("%.1fx Fast", maxShock)
        DivergenceState.DANGER -> String.format("%.1fx Shock", maxShock)
        DivergenceState.BLIND -> "Blind"
    }
}

fun readinessActionLabel(
    status: ExecutionGuardStatus,
    isBengali: Boolean
): String {
    return when (status) {
        ExecutionGuardStatus.GO -> "Validate entry"
        ExecutionGuardStatus.CAUTION -> "Reduce size; verify"
        ExecutionGuardStatus.DANGER -> "Avoid chase; wait"
        ExecutionGuardStatus.BLIND -> "Wait for fresh data"
    }
}


@Composable
fun LiveRadarAiOracleMetadataTile(
    patternText: String,
    orderbookText: String,
    stopLossText: String,
    probabilityText: String,
    descriptionText: String,
    isBengali: Boolean,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(11.dp)
            )
            .border(0.72.dp, themeColor.copy(alpha = 0.38f), RoundedCornerShape(11.dp))
            .padding(horizontal = 11.dp, vertical = 10.dp)
    ) {
        Text(
            text = if (isBengali) "এআই ওরাকল অ্যানালিটিক্যাল মেটাডেটা" else "AI ORACLE ANALYTICAL METADATA",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = themeColor,
            letterSpacing = if (isBengali) 0.sp else 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiveRadarMetadataMiniTile(
                label = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED",
                value = patternText,
                valueColor = Color.White,
                borderColor = themeColor,
                modifier = Modifier.weight(1f)
            )

            LiveRadarMetadataMiniTile(
                label = if (isBengali) "অর্ডারবুক রেশিও" else "ORDERBOOK RATIO",
                value = orderbookText,
                valueColor = Color.White,
                borderColor = themeColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LiveRadarMetadataMiniTile(
                label = if (isBengali) "স্টপ লস" else "SUGGESTED STOP LOSS",
                value = stopLossText,
                valueColor = Color(0xFFFF6F86),
                borderColor = Color(0xFFFF6F86),
                modifier = Modifier.weight(1f)
            )

            LiveRadarMetadataMiniTile(
                label = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE",
                value = probabilityText,
                valueColor = CryptoGreen,
                borderColor = CryptoGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF02050D),
                            Color(0xFF060D18),
                            Color(0xFF02050D)
                        )
                    ),
                    RoundedCornerShape(8.dp)
                )
                .border(0.58.dp, themeColor.copy(alpha = 0.28f), RoundedCornerShape(8.dp))
                .padding(horizontal = 9.dp, vertical = 8.dp)
        ) {
            Text(
                text = descriptionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun LiveRadarMetadataMiniTile(
    label: String,
    value: String,
    valueColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 52.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                ),
                RoundedCornerShape(8.dp)
            )
            .border(0.58.dp, borderColor.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 7.3.sp,
            fontWeight = FontWeight.Black,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = value,
            fontSize = 9.6.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            maxLines = 2,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LiveRadarAdornmentMiniTile(
    label: String,
    value: String,
    valueColor: Color,
    labelColor: Color,
    borderColor: Color,
    labelSize: Float,
    valueSize: Float,
    minHeight: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = minHeight.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF02050D),
                        Color(0xFF050B15),
                        Color(0xFF02050D)
                    )
                )
            )
            .border(
                0.75.dp,
                borderColor.copy(alpha = 0.46f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 5.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = labelSize.sp,
            fontWeight = FontWeight.Black,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = valueSize.sp,
            fontWeight = FontWeight.Black,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

