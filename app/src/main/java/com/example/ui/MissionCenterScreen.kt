package com.example.ui

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Mission
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
                    Column {
                        Text(
                            text = if (isBengali) "মিশন সেন্টার" else "Mission Center",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "AI Trade Guardian",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CryptoCyan,
                            letterSpacing = 0.8.sp
                        )
                    }
                },
                actions = {
                    LanguageToggle(isBengali) { viewModel.toggleLanguage() }
                    IconButton(onClick = { viewModel.sendLocalAlert("Mission Center", "AI Trade Guardian is monitoring active missions.") }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
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
            .heightIn(min = 44.dp)
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
            .heightIn(min = 36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AccentGold.copy(alpha = 0.15f))
            .border(1.dp, AccentGold, RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isBengali) "বাংলা" else "EN",
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBengali) "কোন সক্রিয় মিশন নেই" else "No Active Missions",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBengali) "Signal Pro বা Live Radar থেকে একটি সিগনাল Start Trade করলে AI monitoring শুরু হবে।" else "Start a signal from Signal Pro or Live Radar to begin AI monitoring.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GuardianOverviewCard(missions, isBengali)
            }
            items(missions, key = { it.id }) { mission ->
                MissionCard(
                    mission = mission,
                    isBengali = isBengali,
                    isHistory = false,
                    onStop = { isNegative -> viewModel.stopMission(mission.id, isNegative) }
                )
            }
        }
    }
}

@Composable
private fun GuardianOverviewCard(missions: List<Mission>, isBengali: Boolean) {
    val averageHealth = missions.map { it.tradeHealthScore }.average().takeIf { !it.isNaN() } ?: 0.0
    val activeRisk = missions.count { it.riskLevel == "HIGH" || it.riskLevel == "ELEVATED" }
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CryptoCyan.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(9.dp).background(CryptoGreen, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBengali) "AI Trade Guardian সক্রিয়" else "AI Trade Guardian Active",
                    color = CryptoCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.7.sp
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GuardianMetricTile("MISSIONS", missions.size.toString(), Modifier.weight(1f))
                GuardianMetricTile("HEALTH", "${averageHealth.toInt()}/100", Modifier.weight(1f))
                GuardianMetricTile("RISK", activeRisk.toString(), Modifier.weight(1f))
            }
            Text(
                text = if (isBengali) "সিস্টেম লাইভ মূল্য, ROI, রিস্ক এবং মোমেন্টাম পর্যবেক্ষণ করছে।" else "System is monitoring live price, ROI, risk, and momentum for active missions.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun GuardianMetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun MissionCard(
    mission: Mission,
    isBengali: Boolean,
    isHistory: Boolean,
    onStop: (Boolean?) -> Unit
) {
    var showStopDialog by remember { mutableStateOf(false) }
    val roiColor = if (mission.roiPct < 0.0) CryptoRedText else CryptoGreen
    val healthColor = when {
        mission.tradeHealthScore >= 80 -> CryptoGreen
        mission.tradeHealthScore >= 60 -> AccentGold
        else -> CryptoRedText
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (mission.riskLevel == "HIGH") CryptoRedText.copy(alpha = 0.55f) else BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Coin", tint = AccentGold, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = mission.coinSymbol, fontSize = 20.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        Text(text = "${mission.type} • ${mission.marketType}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (mission.type.equals("SHORT", true)) CryptoRedText else CryptoGreen)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(healthColor.copy(alpha = 0.14f))
                        .border(1.dp, healthColor.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(text = "${mission.tradeHealthScore.coerceIn(0, 100)}/100", color = healthColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataTile(if (isBengali) "সিগন্যাল এন্ট্রি" else "Signal Entry", priceText(mission.signalEntryForValidation), Modifier.weight(1f))
                DataTile(if (isBengali) "লকড এন্ট্রি" else "User Entry", priceText(mission.userLockedEntry), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataTile(if (isBengali) "লাইভ প্রাইস" else "Live Price", priceText(mission.currentPrice), Modifier.weight(1f))
                DataTile("ROI", mission.formattedRoi(), Modifier.weight(1f), valueColor = roiColor)
            }

            LinearProgressIndicator(
                progress = mission.tp1Probability / 100f,
                color = healthColor,
                trackColor = BorderColor,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp))
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (isBengali) "TP1 সম্ভাবনা" else "TP1 Probability", color = TextSecondary, fontSize = 11.sp)
                Text(text = "${mission.tp1Probability}%", color = healthColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            GuardianBrief(mission, isBengali)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DataTile(if (isBengali) "রিস্ক" else "Risk", mission.riskLevel, Modifier.weight(1f), valueColor = healthColor)
                DataTile(if (isBengali) "ট্রেন্ড" else "Trend", mission.currentTrend, Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant, RoundedCornerShape(14.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(text = if (isBengali) "টার্গেট" else "Targets", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = mission.targets, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (isBengali) "স্টপ লস" else "Stop Loss", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(text = mission.stopLoss, color = CryptoRedText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            if (!isHistory) {
                Button(
                    onClick = { showStopDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoRedContainer),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = if (isBengali) "Stop Mission" else "Stop Mission", color = CryptoRedText, fontWeight = FontWeight.Black)
                }
            } else {
                Text(
                    text = mission.finalAnalysisEnglish ?: mission.aiStatusEnglish,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }

    if (showStopDialog) {
        StopMissionDialog(
            isBengali = isBengali,
            onDismiss = { showStopDialog = false },
            onConfirm = { isNegative ->
                onStop(isNegative)
                showStopDialog = false
            }
        )
    }
}

@Composable
private fun DataTile(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = TextPrimary) {
    Column(
        modifier = modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(text = label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = value, color = valueColor, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun GuardianBrief(mission: Mission, isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CryptoCyan.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(1.dp, CryptoCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CryptoCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "AI Guardian Brief", color = CryptoCyan, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Text(
            text = if (isBengali) mission.guardianRecommendationBengali else mission.guardianRecommendationEnglish,
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "TP1 ${mission.tp1Probability}%", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "TP2 ${mission.tp2Probability}%", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "TP3 ${mission.tp3Probability}%", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StopMissionDialog(
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean?) -> Unit
) {
    data class Reason(val label: String, val override: Boolean?)
    val reasons = listOf(
        Reason("Take Profit", false),
        Reason("Stop Loss", true),
        Reason("Manual Exit", null),
        Reason("Risk Exit", true),
        Reason("Other", null)
    )
    var selected by remember { mutableStateOf(reasons.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (isBengali) "Stop Mission" else "Stop Mission", color = TextPrimary, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = if (isBengali) "মিশন বন্ধ করার কারণ নির্বাচন করুন।" else "Select the reason for closing this mission.", color = TextSecondary)
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selected = reason }
                            .background(if (selected == reason) AccentGold.copy(alpha = 0.14f) else Color.Transparent)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected == reason, onClick = { selected = reason })
                        Text(text = reason.label, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected.override) }, colors = ButtonDefaults.buttonColors(containerColor = CryptoRedContainer)) {
                Text("Close Mission", color = CryptoRedText, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } },
        containerColor = DarkSurface
    )
}

@Composable
private fun MissionHistoryList(viewModel: CryptoViewModel, isBengali: Boolean) {
    val history by viewModel.missionHistory.collectAsState()

    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (isBengali) "কোন মিশন ইতিহাস নেই" else "No Mission History Yet",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(history, key = { it.id }) { mission ->
                MissionCard(
                    mission = mission,
                    isBengali = isBengali,
                    isHistory = true,
                    onStop = {}
                )
            }
        }
    }
}

private fun priceText(value: Double): String = if (value >= 100) {
    "$${String.format("%,.2f", value)}"
} else {
    "$${String.format("%.4f", value)}"
}
