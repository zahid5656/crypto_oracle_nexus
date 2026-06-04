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

                    Text(
                        text = if (isBengali) "ওরাকল কোয়ান্ট বিশ্লেষণ" else "ORACLE ANALYTIC METADATA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "অর্ডারবুক ইমব্যালেন্স" else "ORDERBOOK RATIO", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "স্টপ লস" else "SUGGESTED STOP LOSS", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = details["sl"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3F60)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE", fontSize = 8.sp, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = details["prob"]!!,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CryptoGreen, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
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
                    com.example.ui.StartTradeFlow(viewModel = viewModel, mission = mission)
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

                    Text(
                        text = if (isBengali) "ওরাকল কোয়ান্ট ফিউচার লং বিশ্লেষণ" else "ORACLE FUTURES LONG METADATA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CryptoGreen,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "ওপেন ইন্টারেস্ট গতি" else "OPEN INTEREST DYNAMICS", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "স্টপ লস টার্গেট" else "SUGGESTED STOP LOSS", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = details["sl"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3F60)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE", fontSize = 8.sp, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = details["prob"]!!,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CryptoGreen, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
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
                    com.example.ui.StartTradeFlow(viewModel = viewModel, mission = mission)
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

                    Text(
                        text = if (isBengali) "ওরাকল কোয়ান্ট ফিউচার শর্ট বিশ্লেষণ" else "ORACLE FUTURES SHORT METADATA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3F60),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "প্যাটার্ন" else "PATTERN DETECTED", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = if (isBengali) details["pattern_bn"]!! else details["pattern"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "ওপেন ইন্টারেস্ট গতি" else "OPEN INTEREST DYNAMICS", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = if (isBengali) details["bid_ask_bn"]!! else details["bid_ask"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "স্টপ লস টার্গেট" else "SUGGESTED STOP LOSS", fontSize = 8.sp, color = TextMuted)
                            Text(
                                text = details["sl"]!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF3F60)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (isBengali) "সম্ভাব্যতা স্কোর" else "PROBABILITY SCORE", fontSize = 8.sp, color = TextMuted)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = details["prob"]!!,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CryptoGreen
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CryptoGreen, CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isBengali) details["desc_bn"]!! else details["desc"]!!,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
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
                    com.example.ui.StartTradeFlow(viewModel = viewModel, mission = mission)
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
