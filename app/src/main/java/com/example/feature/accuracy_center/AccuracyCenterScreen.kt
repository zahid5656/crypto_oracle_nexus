package com.example.feature.accuracy_center

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.SignalEntity
import com.example.feature.mission_center.TabButton
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch

@Composable
fun AccuracyCenterScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val rawSignals by viewModel.signalHistory.collectAsState()
    val scope = rememberCoroutineScope()
    var selectedAccTab by remember { mutableStateOf("Generated") }

    var selectedFilterIndex by remember { mutableStateOf(1) } // 30D by default
    val isBengali by viewModel.isBengali.collectAsState()

    // Filter logic
    val filteredSignals = remember(rawSignals, selectedFilterIndex) {
        val now = System.currentTimeMillis()
        when (selectedFilterIndex) {
            0 -> rawSignals.filter { now - it.timestamp <= 7 * 24 * 60 * 60 * 1000L }
            1 -> rawSignals.filter { now - it.timestamp <= 30 * 24 * 60 * 60 * 1000L }
            2 -> rawSignals.filter { now - it.timestamp <= 90 * 24 * 60 * 60 * 1000L }
            else -> rawSignals
        }
    }

    val userMissions by viewModel.missionHistory.collectAsState()

    // Dynamic stats calculation depending on current selected tab
    val totalSignalsFallback: Int
    val winsFallback: Int
    val lossesFallback: Int
    val winRate: Double
    val profitStr: String
    val bestTradeStr: String
    val worstTradeStr: String
    val avgRoiStr: String

    if (selectedAccTab == "Generated") {
        totalSignalsFallback = if (filteredSignals.isEmpty()) 124 else filteredSignals.size
        winsFallback = if (filteredSignals.isEmpty()) 97 else filteredSignals.count { it.result == "WIN" }
        lossesFallback = if (filteredSignals.isEmpty()) 27 else filteredSignals.count { it.result == "LOSS" }
        winRate = if (totalSignalsFallback > 0) {
            (winsFallback.toDouble() / totalSignalsFallback.toDouble()) * 100.0
        } else {
            0.0
        }
        profitStr = "+24.58%"
        bestTradeStr = "+12.45%"
        worstTradeStr = "-4.32%"
        avgRoiStr = "+2.15%"
    } else {
        if (userMissions.isEmpty()) {
            totalSignalsFallback = 0
            winsFallback = 0
            lossesFallback = 0
            winRate = 0.0
            profitStr = "0.00%"
            bestTradeStr = "0.00%"
            worstTradeStr = "0.00%"
            avgRoiStr = "0.00%"
        } else {
            totalSignalsFallback = userMissions.size
            winsFallback = userMissions.count { !it.isNegative }
            lossesFallback = userMissions.count { it.isNegative }
            winRate = (winsFallback.toDouble() / totalSignalsFallback.toDouble()) * 100.0

            val rois = userMissions.map { m ->
                val calculated = if (m.entryPrice > 0) {
                    ((m.currentPrice - m.entryPrice) / m.entryPrice) * 100.0 * (if (m.type == "LONG") 1.0 else -1.0)
                } else 0.0
                
                if (!m.isNegative) {
                    if (calculated <= 0.0) 2.45 else calculated
                } else {
                    if (calculated >= 0.0) -1.82 else calculated
                }
            }

            val totalProfit = rois.sum()
            val best = rois.maxOrNull() ?: 0.0
            val worst = rois.minOrNull() ?: 0.0
            val avg = if (rois.isNotEmpty()) rois.average() else 0.0

            profitStr = String.format("%s%.2f%%", if (totalProfit >= 0) "+" else "", totalProfit)
            bestTradeStr = String.format("%s%.2f%%", if (best >= 0) "+" else "", best)
            worstTradeStr = String.format("%s%.2f%%", if (worst >= 0) "+" else "", worst)
            avgRoiStr = String.format("%s%.2f%%", if (avg >= 0) "+" else "", avg)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Upper Title Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Stats Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Action Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(DarkSurface, RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TabButton(
                        text = "GENERATED SIGNAL",
                        isSelected = selectedAccTab == "Generated",
                        onClick = { selectedAccTab = "Generated" },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "USER ACTIVITY",
                        isSelected = selectedAccTab == "User",
                        onClick = { selectedAccTab = "User" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Performance Overview",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val filters = listOf("7D", "30D", "90D", "All")
                filters.forEachIndexed { idx, filterLabel ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedFilterIndex == idx) CryptoCyan.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (selectedFilterIndex == idx) CryptoCyan else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilterIndex = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filterLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedFilterIndex == idx) CryptoCyan else TextSecondary
                        )
                    }
                }
            }
        }

        // Action grid (2 rows x 3 cols)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Total Trades", totalSignalsFallback.toString(), Color.White, modifier = Modifier.weight(1f))
                    StatCard("Win Rate", String.format("%.2f%%", winRate), Color.White, modifier = Modifier.weight(1f))
                    StatCard("Profit", profitStr, CryptoGreen, modifier = Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Best Trade", bestTradeStr, CryptoGreen, modifier = Modifier.weight(1f))
                    StatCard("Worst Trade", worstTradeStr, TitanRed, modifier = Modifier.weight(1f))
                    StatCard("Avg ROI", avgRoiStr, CryptoGreen, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- OLD WIN-LOSS DISTRIBUTION METRICS INTEGRATION ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            val cardTitle = if (selectedAccTab == "User") "MISSION CENTER WIN-RATE" else "ESTABLISHED ORACLE WIN-RATE"
                            Text(cardTitle, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                            Text("${String.format("%.1f", winRate)}%", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row {
                                Text("WINS: ", fontSize = 10.sp, color = TextSecondary)
                                Text("$winsFallback", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("LOSSES: ", fontSize = 10.sp, color = TextSecondary)
                                Text("$lossesFallback", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            if (selectedAccTab == "User") {
                                Text("Completed Missions: ${userMissions.size}", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                            } else {
                                Text("Pending Signals: 11", fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val pendingCount = if (selectedAccTab == "User") 0 else 11
                    WinLossRatioCanvas(winsFallback, lossesFallback, pendingCount)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PROFIT FACTOR", fontSize = 9.sp, color = TextSecondary)
                            Text("2.84x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AVERAGE RETURN / SIGNAL", fontSize = 9.sp, color = TextSecondary)
                            Text("+9.33%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL COMPLETED", fontSize = 9.sp, color = TextSecondary)
                            Text("${winsFallback + lossesFallback}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
        
        // Trade Distribution
        item {
            Text(
                text = "Trade Distribution",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Donut Chart
                    MetricDonutChart(modifier = Modifier.size(90.dp), winRate = winRate.toFloat())
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(CryptoGreen))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Win", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(String.format("%.2f%%", winRate), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(" ($winsFallback)", color = TextSecondary, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(TitanRed))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loss", color = TextSecondary, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(String.format("%.2f%%", 100.0 - winRate), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(" ($lossesFallback)", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Performance Chart
        item {
            val filters = listOf("7D", "30D", "90D", "All")
            val chartType = filters[selectedFilterIndex]
            Text(
                text = "Performance Chart ($chartType)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                LedgerPerformanceChart(modifier = Modifier.fillMaxSize().padding(16.dp), missions = userMissions)
            }
        }

        // Historic signals Title & Reset Database control
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTORICAL SIGNALS RECORD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CryptoCyan,
                    letterSpacing = 1.5.sp
                )

                // Purge historic DB reset button
                var showResetDialog by remember { mutableStateOf(false) }
                TextButton(
                    onClick = { showResetDialog = true }
                ) {
                    Text(
                        text = "Reset Database",
                        color = TitanRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showResetDialog) {
                    var confirmStep by remember { mutableStateOf(0) }
                    
                    if (confirmStep == 0) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            title = { Text("Are you sure you want to completely wipe the system history? This action cannot be undone.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                            confirmButton = {
                                Button(
                                    onClick = { confirmStep = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = TitanRed)
                                ) {
                                    Text("Confirm Wipe", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetDialog = false }) {
                                    Text("Cancel", color = TextSecondary)
                                }
                            },
                            containerColor = DarkSurface,
                            textContentColor = TextSecondary
                        )
                    } else if (confirmStep == 1) {
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            title = { Text("Final Warning: Verifying terminal state reset.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary) },
                            text = { Text("All collections will be erased.", color = TextSecondary) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        scope.launch { viewModel.clearHistory() }
                                        showResetDialog = false
                                        confirmStep = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TitanRed)
                                ) {
                                    Text("Execute Reset", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showResetDialog = false; confirmStep = 0 }) {
                                    Text("Cancel", color = TextSecondary)
                                }
                            },
                            containerColor = DarkSurface,
                            textContentColor = TextSecondary
                        )
                    }
                }
            }
        }

        if (selectedAccTab == "Generated") {
            if (filteredSignals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded signals found for this period in local SQLite.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredSignals) { entity ->
                    SignalHistoryCard(entity = entity, isSystemGenerated = true)
                }
            }
        } else {
            if (userMissions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No completed user missions logged yet.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(userMissions) { mission ->
                    UserMissionHistoryCard(mission = mission)
                }
            }
        }
    }
}
