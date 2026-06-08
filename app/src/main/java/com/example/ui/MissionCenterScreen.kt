package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

// Terminal Colors - Institutional Grade
private val T_Bg = Color(0xFF000000)
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
fun MissionCenterScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val isBengali by viewModel.isBengali.collectAsState()
    var selectedTab by remember { mutableStateOf("Running") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(T_Bg),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            MissionTerminalHeaderBlock(viewModel)
        }

        item {
            McSectionHeader("SYSTEM HEALTH & COPILOT MODE")
            MissionStatusDashboard(isBengali)
        }

        item {
            McSectionHeader("ACTIVE TRADE GUARDIAN & RISK TIMELINE")
            RiskTimelinePreview(isBengali)
        }

        item {
            McSectionHeader("MISSION LOG")
            MissionTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isBengali = isBengali
            )
        }

        if (selectedTab == "Running") {
            item {
                RunningMissionsContent(viewModel, isBengali)
            }
        } else {
            item {
                HistoryMissionsContent(viewModel, isBengali)
            }
        }
    }
}

@Composable
fun MissionTerminalHeaderBlock(viewModel: CryptoViewModel) {
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
                    text = "MISSION CONTROL v1.0",
                    color = T_TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            McTerminalClockWidget()
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AI TRADE GUARDIAN",
                    color = T_TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ACTIVE",
                    color = T_Green,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "LANGUAGE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                val isBengali by viewModel.isBengali.collectAsState()
                Text(
                    text = if (isBengali) "BENGALI" else "ENGLISH",
                    color = T_Cyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.toggleLanguage() }
                )
            }
        }
    }
}

@Composable
private fun McTerminalClockWidget() {
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
fun McSectionHeader(title: String) {
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
fun MissionStatusDashboard(isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            McStatusBox(
                title = if (isBengali) "মিশন স্বাস্থ্য" else "MISSION HEALTH",
                value = if (isBengali) "স্থিতিশীল" else "STABLE",
                valueColor = T_Green,
                modifier = Modifier.weight(1f)
            )
            McStatusBox(
                title = if (isBengali) "মোড" else "COPILOT MODE",
                value = if (isBengali) "সতর্কতা-শুধুমাত্র" else "ALERT-ONLY",
                valueColor = T_Gold,
                modifier = Modifier.weight(1f)
            )
            McStatusBox(
                title = if (isBengali) "অটো-এক্সিকিউট" else "AUTO-EXECUTE",
                value = if (isBengali) "নিষ্ক্রিয়" else "OFF",
                valueColor = T_TextMuted,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isBengali) "নোট: ম্যানুয়াল কন্ট্রোল সক্রিয়। এআই শুধুমাত্র পরামর্শ দেয়।" else "NOTE: MANUAL CONTROL ACTIVE. AI PROVIDES DECISION SUPPORT ONLY.",
            color = T_TextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun McStatusBox(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(0.5.dp, T_BorderHigh, RoundedCornerShape(4.dp))
            .background(T_Surface)
            .padding(8.dp)
    ) {
        Text(text = title, color = T_TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = valueColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RiskTimelinePreview(isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, T_Green.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .background(T_Green.copy(alpha = 0.1f))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(T_Green, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "কোনো ঝুঁকি সনাক্ত করা হয়নি। মার্কেট রেসিস্ট্যান্স স্থিতিশীল।" else "NO CRITICAL ESCALATIONS DETECTED. MARKET SUPPORT STABLE.",
                    color = T_Green,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MissionTabs(selectedTab: String, onTabSelected: (String) -> Unit, isBengali: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(T_Bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        TerminalTab(
            text = if (isBengali) "চলমান মিশন" else "ACTIVE MISSIONS",
            isSelected = selectedTab == "Running",
            onClick = { onTabSelected("Running") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        TerminalTab(
            text = if (isBengali) "ইতিহাস" else "MISSION HISTORY",
            isSelected = selectedTab == "History",
            onClick = { onTabSelected("History") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TerminalTab(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) T_Surface else T_Bg
    val borderColor = if (isSelected) T_TextPrimary else T_BorderHigh
    val textColor = if (isSelected) T_TextPrimary else T_TextMuted

    Box(
        modifier = modifier
            .clickable { onClick() }
            .background(bgColor)
            .border(0.5.dp, borderColor, RectangleShape)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun RunningMissionsContent(viewModel: CryptoViewModel, isBengali: Boolean) {
    val missions by viewModel.activeMissions.collectAsState()
    
    if (missions.isEmpty()) {
        EmptyMissionTerminal(isBengali)
    } else {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            missions.forEach { m ->
                val diff = m.currentPrice - m.entryPrice
                val diffPct = (diff / m.entryPrice) * 100.0 * (if (m.type == "LONG") 1.0 else -1.0)
                val isLoss = diffPct < 0
                val sign = if (isLoss) "" else "+"
                
                val elapsedMs = System.currentTimeMillis() - m.startTime
                val h = elapsedMs / (1000 * 60 * 60)
                val min = (elapsedMs / (1000 * 60)) % 60
                
                MissionTerminalCard(
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
fun HistoryMissionsContent(viewModel: CryptoViewModel, isBengali: Boolean) {
    val history by viewModel.missionHistory.collectAsState()
    
    if (history.isEmpty()) {
        EmptyMissionTerminal(isBengali)
    } else {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            history.forEach { m ->
                val diff = m.currentPrice - m.entryPrice
                val diffPct = (diff / m.entryPrice) * 100.0 * (if (m.type == "LONG") 1.0 else -1.0)
                val isLoss = diffPct < 0
                val sign = if (isLoss) "" else "+"
                
                val elapsedMs = System.currentTimeMillis() - m.startTime
                val h = elapsedMs / (1000 * 60 * 60)
                val min = (elapsedMs / (1000 * 60)) % 60
                
                MissionTerminalCard(
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
                    aiStatus = (if (isBengali) "মিশন সম্পন্ন হয়েছে" else "MISSION COMPLETED") + " - ${if (isLoss) "LOSS" else "PROFIT"}",
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

@Composable
fun EmptyMissionTerminal(isBengali: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Surface)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isBengali) "কোনো সক্রিয় মিশন নেই" else "NO ACTIVE MISSIONS",
                color = T_TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBengali) "প্রতীক্ষিত কমান্ড" else "AWAITING COMMAND...",
                color = T_TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun MissionTerminalCard(
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
    val roiColor = if (isNegative) T_Red else T_Green
    val typeColor = if (type.uppercase() == "LONG") T_Green else T_Red
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, T_BorderHigh, RectangleShape)
            .background(T_Surface)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = coinSymbol.uppercase(),
                    color = T_TextPrimary,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = type.uppercase(),
                        color = typeColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = marketType.uppercase(),
                color = T_TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        
        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)
        
        // Data Grid
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TerminalDataField(if (isBengali) "লকড এন্ট্রি" else "LOCKED ENTRY", entryPrice, T_TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                TerminalDataField("ROI", roi, roiColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                TerminalDataField(if (isBengali) "বর্তমান মূল্য" else "LIVE PRICE", currentPrice, T_TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                TerminalDataField(if (isBengali) "অতিবাহিত সময়" else "TIME IN TRADE", timeElapsed, T_TextSecondary)
            }
        }
        
        // Progress Bars & Risk
        val entryVal = entryPrice.replace("$", "").toDoubleOrNull() ?: 0.0
        val currentVal = currentPrice.replace("$", "").toDoubleOrNull() ?: 0.0
        val targetVals = targets.split("/").mapNotNull { it.replace("$", "").trim().toDoubleOrNull() }
        val firstTarget = targetVals.firstOrNull() ?: (entryVal * (if (type == "LONG") 1.05 else 0.95))
        
        val totalDistance = (firstTarget - entryVal).absoluteValue
        val currentDistance = (currentVal - entryVal).absoluteValue
        val pctToTarget = if (totalDistance != 0.0) (currentDistance / totalDistance).coerceIn(0.0, 1.0).toFloat() else 0f
        
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (isBengali) "লক্ষ্য অগ্রগতি" else "TARGET PROGRESS", fontSize = 9.sp, color = T_TextMuted, fontFamily = FontFamily.Monospace)
                Text(text = String.format("%.1f%%", pctToTarget * 100f), fontSize = 9.sp, color = roiColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { pctToTarget },
                color = roiColor,
                trackColor = T_BorderHigh,
                modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp))
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TerminalDataField(if (isBengali) "টার্গেট স্তর" else "TARGETS", targets, T_TextPrimary)
                TerminalDataField(if (isBengali) "স্টপ লস" else "STOP LOSS", stopLoss, T_Gold, alignEnd = true)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // AI Guidance Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(T_Bg)
                .border(0.5.dp, T_BorderHigh, RectangleShape)
                .padding(12.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = if (isBengali) "এআই ট্রেড গাইডেন্স" else "AI TRADE GUIDANCE", color = T_Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(text = "CONFIDENCE: $confidence%", color = T_TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = aiStatus.uppercase(), color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif, lineHeight = 16.sp)
            }
        }
        
        if (!isHistory) {
            var showStopConfirm by remember { mutableStateOf(false) }

            if (showStopConfirm) {
                AlertDialog(
                    onDismissRequest = { showStopConfirm = false },
                    title = { Text(if (isBengali) "মিশন বন্ধ করুন" else "CLOSE MISSION", color = T_TextPrimary, fontFamily = FontFamily.Monospace) },
                    text = { Text(if (isBengali) "বন্ধ করার কারণ নির্বাচন করুন:" else "SELECT CLOSURE REASON:", color = T_TextSecondary, fontFamily = FontFamily.Monospace) },
                    confirmButton = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showStopConfirm = false; onStop(false) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = T_Green),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("TAKE PROFIT", color = T_Bg, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Button(
                                onClick = { showStopConfirm = false; onStop(true) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = T_Red),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("STOP LOSS", color = T_Bg, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Button(
                                onClick = { showStopConfirm = false; onStop(null) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = T_Surface),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, T_BorderHigh)
                            ) {
                                Text("MANUAL EXIT", color = T_TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showStopConfirm = false }, modifier = Modifier.fillMaxWidth()) {
                            Text("CANCEL", color = T_TextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    },
                    containerColor = T_Bg,
                    titleContentColor = T_TextPrimary,
                    textContentColor = T_TextSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStopConfirm = true }
                        .background(T_Surface)
                        .border(1.dp, T_BorderHigh)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isBengali) "মিশন বাতিল" else "ABORT MISSION", color = T_Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TerminalDataField(label: String, value: String, valueColor: Color, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(text = label, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = valueColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

// Reinstating original TabButton format required by other files (AccuracyCenterScreen)
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
            .background(if (isSelected) T_Gold.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) T_Gold else T_TextMuted
        )
    }
}
