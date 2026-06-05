package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionCenterScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val isBengali by viewModel.isBengali.collectAsState()
    var selectedTab by remember { mutableStateOf("Running") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isBengali) "মিশন সেন্টার" else "Mission Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                actions = {
                    LanguageToggle(isBengali) { viewModel.toggleLanguage() }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .background(DarkSurface, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TabButton(
                    text = if (isBengali) "চলমান" else "Running",
                    isSelected = selectedTab == "Running",
                    onClick = { selectedTab = "Running" },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = if (isBengali) "ইতিহাস" else "History",
                    isSelected = selectedTab == "History",
                    onClick = { selectedTab = "History" },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedTab == "Running") {
                RunningMissionsList(viewModel, isBengali)
            } else {
                MissionHistoryList(viewModel, isBengali)
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) AccentGold.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) AccentGold else TextMuted
        )
    }
}

@Composable
private fun LanguageToggle(isBengali: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AccentGold.copy(alpha = 0.15f))
            .border(1.dp, AccentGold, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isBengali) "GLOBAL: বাংলা" else "GLOBAL: EN",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AccentGold
        )
    }
}

@Composable
private fun RunningMissionsList(viewModel: CryptoViewModel, isBengali: Boolean) {
    val missions by viewModel.activeMissions.collectAsState()
    
    if (missions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (isBengali) "কোন সক্রিয় মিশন নেই" else "No Active Missions",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(missions.size) { i ->
                val m = missions[i]
                
                // Simulate ROI / elapsed time format for now
                val diff = m.currentPrice - m.entryPrice
                val diffPct = (diff / m.entryPrice) * 100.0 * (if (m.type == "LONG") 1.0 else -1.0)
                val isLoss = diffPct < 0
                val sign = if (isLoss) "" else "+"
                
                val elapsedMs = System.currentTimeMillis() - m.startTime
                val h = elapsedMs / (1000 * 60 * 60)
                val min = (elapsedMs / (1000 * 60)) % 60
                
                MissionCard(
                    missionId = m.id,
                    coinSymbol = m.coinSymbol,
                    type = m.type,
                    marketType = m.marketType,
                    originalEntry = "$${String.format("%.4f", m.originalSignalEntry)}",
                    entryPrice = "$${String.format("%.4f", m.entryPrice)}",
                    currentPrice = "$${String.format("%.4f", m.currentPrice)}",
                    roi = "$sign${String.format("%.2f", diffPct)}%",
                    timeElapsed = String.format("%02dh %02dm", h, min),
                    targets = m.targets,
                    stopLoss = m.stopLoss,
                    aiStatus = if (isBengali) m.aiStatusBengali else m.aiStatusEnglish,
                    confidence = m.confidence,
                    isNegative = isLoss,
                    isBengali = isBengali,
                    isHistory = false,
                    onStop = { overrideValue -> viewModel.stopMission(m.id, overrideValue) }
                )
            }
        }
    }
}

@Composable
private fun MissionCard(
    missionId: String,
    coinSymbol: String,
    type: String,
    marketType: String,
    originalEntry: String,
    entryPrice: String,
    currentPrice: String,
    roi: String,
    timeElapsed: String,
    targets: String,
    stopLoss: String,
    aiStatus: String,
    confidence: Int,
    isNegative: Boolean,
    isBengali: Boolean,
    isHistory: Boolean,
    onStop: (Boolean?) -> Unit
) {
    val roiColor = if (isNegative) Color(0xFFFF3F60) else CryptoGreen
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Coin",
                        tint = AccentGold,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = coinSymbol, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CryptoGreen)
                    }
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = marketType, fontSize = 12.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pricing & ROI
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = if (isBengali) "সিগন্যাল এন্ট্রি" else "Signal Entry", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = originalEntry, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = if (isBengali) "লকড এন্ট্রি" else "Locked Entry", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = entryPrice, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(text = "ROI", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = roi, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = roiColor)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Spacer(modifier = Modifier.height(28.dp)) // Aligns next block

                    Text(text = if (isBengali) "বর্তমান মূল্য" else "Live Price", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = currentPrice, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(text = if (isBengali) "অতিবাহিত সময়" else "Time Elapsed", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = timeElapsed, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar to Target
            val entryVal = entryPrice.replace("$", "").toDoubleOrNull() ?: 0.0
            val currentVal = currentPrice.replace("$", "").toDoubleOrNull() ?: 0.0
            val targetVals = targets.split("/").mapNotNull { it.replace("$", "").trim().toDoubleOrNull() }
            val firstTarget = targetVals.firstOrNull() ?: (entryVal * (if (type == "LONG") 1.05 else 0.95))
            
            val totalDistance = (firstTarget - entryVal).absoluteValue
            val currentDistance = (currentVal - entryVal).absoluteValue
            val pctToTarget = if (totalDistance != 0.0) (currentDistance / totalDistance).coerceIn(0.0, 1.0).toFloat() else 0f

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isBengali) "টার্গেট অগ্রগতি" else "Target Progress", fontSize = 10.sp, color = TextSecondary)
                Text(text = String.format("%.1f%% achieved", pctToTarget * 100f), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = roiColor)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = pctToTarget,
                color = roiColor,
                trackColor = BorderColor,
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = BorderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Targets & Stops
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (isBengali) "টার্গেট স্তর" else "Targets", fontSize = 12.sp, color = TextMuted)
                Text(text = targets, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (isBengali) "স্টপ লস" else "Stop Loss", fontSize = 12.sp, color = TextMuted)
                Text(text = stopLoss, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3F60))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // AI Mission Brief
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(8.dp))
                    .border(1.dp, CryptoGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = if (isBengali) "এআই মিশন ব্রিফ" else "AI Mission Brief", fontSize = 13.sp, color = CryptoCyan, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = if (isBengali) "এআই আত্মবিশ্বাস" else "AI Confidence", fontSize = 10.sp, color = TextMuted)
                            Text(text = "$confidence%", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (isBengali) "প্রবণতা" else "Trend", fontSize = 10.sp, color = TextMuted)
                            Text(text = if (type == "LONG") "BULLISH" else "BEARISH", fontSize = 12.sp, color = CryptoGreen, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = if (isBengali) "ঝুঁকি স্তর" else "Risk Level", fontSize = 10.sp, color = TextMuted)
                            Text(text = "MODERATE", fontSize = 12.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = if (isBengali) "সুপারিশ:" else "Recommendation:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = aiStatus, fontSize = 13.sp, color = CryptoGreen, lineHeight = 18.sp)
                }
            }

            if (!isHistory) {
                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                var showStopConfirm by remember { mutableStateOf(false) }

                if (showStopConfirm) {
                    AlertDialog(
                        onDismissRequest = { showStopConfirm = false },
                        title = { Text(if (isBengali) "মিশন বন্ধ করার কারণ" else "Select Exit Reason", color = TextPrimary) },
                        text = { Text(if (isBengali) "অনুগ্রহ করে এই মিশনটি বন্ধ করার একটি কারণ নির্বাচন করুন।" else "Please select the reason for closing this mission:", color = TextSecondary) },
                        confirmButton = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { showStopConfirm = false; onStop(false) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                                ) {
                                    Text("Take Profit", color = DarkBackground, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showStopConfirm = false; onStop(true) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3F60))
                                ) {
                                    Text("Stop Loss", color = DarkBackground, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showStopConfirm = false; onStop(null) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                                ) {
                                    Text("Manual Exit", color = DarkBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStopConfirm = false }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (isBengali) "বাতিল" else "Cancel", color = TextSecondary)
                            }
                        },
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary,
                        textContentColor = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: Detailed view */ },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Text(if (isBengali) "বিস্তারিত দেখুন" else "View Details")
                    }
                    Button(
                        onClick = { showStopConfirm = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3F60))
                    ) {
                        Text(if (isBengali) "মিশন বন্ধ করুন" else "Stop Mission", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionHistoryList(viewModel: CryptoViewModel, isBengali: Boolean) {
    val history by viewModel.missionHistory.collectAsState()
    
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (isBengali) "কোন ইতিহাস নেই" else "No Mission History Found",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(history.size) { i ->
                val m = history[i]
                
                val diff = m.currentPrice - m.entryPrice
                val diffPct = (diff / m.entryPrice) * 100.0 * (if (m.type == "LONG") 1.0 else -1.0)
                val isLoss = diffPct < 0
                val sign = if (isLoss) "" else "+"
                
                val elapsedMs = System.currentTimeMillis() - m.startTime
                val h = elapsedMs / (1000 * 60 * 60)
                val min = (elapsedMs / (1000 * 60)) % 60
                
                MissionCard(
                    missionId = m.id,
                    coinSymbol = m.coinSymbol,
                    type = m.type,
                    marketType = m.marketType,
                    originalEntry = "$${String.format("%.4f", m.originalSignalEntry)}",
                    entryPrice = "$${String.format("%.4f", m.entryPrice)}",
                    currentPrice = "$${String.format("%.4f", m.currentPrice)}",
                    roi = "$sign${String.format("%.2f", diffPct)}%",
                    timeElapsed = String.format("%02dh %02dm", h, min),
                    targets = m.targets,
                    stopLoss = m.stopLoss,
                    aiStatus = (if (isBengali) "মিশন সম্পন্ন হয়েছে" else "Mission Completed") + " - ${if (isLoss) "Loss" else "Profit"}",
                    confidence = m.confidence,
                    isNegative = isLoss,
                    isBengali = isBengali,
                    isHistory = true,
                    onStop = {} // No-op in history
                )
            }
        }
    }
}
