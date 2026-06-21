package com.example.feature.live_radar

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.example.core.radar.RadarAlert
import com.example.feature.signal_pro.StartTradeFlow
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveRadarScreen(
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
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
    ) {
        val intervals = listOf("1M", "5M", "15M", "30M", "45M", "1H")
        val displayWindow = intervals.getOrElse(shortTermInterval) { "15M" }

        // Sticky Live Radar control cluster: title, regime, scalp oracle header, and timeframe selector.
        stickyHeader(key = "live_radar_control_cluster") {
            LiveRadarStickyControlCluster(
                intervals = intervals,
                selectedIndex = shortTermInterval,
                isBengali = isBengali,
                marketRegime = marketRegime,
                onLanguageToggle = { viewModel.toggleLanguage() },
                onSelect = { idx -> viewModel.setShortTermTimeframe(idx) }
            )
        }

        // Short Term Signals Display List
        item(key = "short_term_signals_$displayWindow") {
            ShortTermOpportunisticSignalsSection(timeframe = displayWindow, isBengali = isBengali, viewModel = viewModel)
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
private fun LiveRadarStickyControlCluster(
    intervals: List<String>,
    selectedIndex: Int,
    isBengali: Boolean,
    marketRegime: String,
    onLanguageToggle: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(top = 2.dp, bottom = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIVE QUANT RADAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CryptoCyan,
                    letterSpacing = 2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isBengali) "মার্কেট ইন্টেলিজেন্স" else "Market Intelligence",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LiveRadarSoftWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onLanguageToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    contentColor = CryptoCyan
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.height(34.dp),
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isBengali) "English" else "বাংলা",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.85.dp, CryptoCyan.copy(alpha = 0.28f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(LiveRadarInstitutionalGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBengali) "অন-চেইন বাজার পরিস্থিতি" else "CURRENT MARKET REGIME",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = marketRegime,
                        fontSize = 13.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiveRadarInstitutionalGreen,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 1.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(14.dp))
                .border(0.85.dp, BorderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(
                text = if (isBengali) "সংক্ষিপ্ত সময়ের ওরাকল স্ক্যাল্পস" else "SHORT-TERM SCALP ORACLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LiveRadarSoftWhite,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                intervals.forEachIndexed { idx, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedIndex == idx) CryptoCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                0.85.dp,
                                if (selectedIndex == idx) CryptoCyan else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSelect(idx) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedIndex == idx) CryptoCyan else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
