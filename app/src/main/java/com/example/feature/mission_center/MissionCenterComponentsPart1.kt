package com.example.feature.mission_center

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

import com.example.ui.theme.*

// Terminal Colors - Institutional Grade

// Extracted from MissionCenterScreen.kt to keep the public screen entry point compact.
internal val T_Bg = DarkBackground
internal val T_Surface = Color(0xFF111112)
internal val T_Border = Color(0xFF1C1C1E)
internal val T_BorderHigh = Color(0xFF2C2C2E)
internal val T_TextPrimary = Color(0xFFFFFFFF)
internal val T_TextSecondary = Color(0xFF8E8E93)
internal val T_TextMuted = Color(0xFF636366)
internal val T_Green = CryptoGreen
internal val T_Red = CryptoRedText
internal val T_Cyan = CryptoCyan
internal val T_Gold = TitanGold
internal val mcTimeframeDurationMinutes = mapOf(
    "1M" to 1,
    "5M" to 5,
    "15M" to 15,
    "30M" to 30,
    "45M" to 45,
    "1H" to 60,
    "2H" to 120,
    "4H" to 240,
    "6H" to 360,
    "12H" to 720,
    "24H" to 1440,
    "3D" to 4320,
    "7D" to 10080
)
internal fun mcTimeframeRank(timeframe: String?): Int {
    val normalized = timeframe.orEmpty().trim().uppercase()
    return mcTimeframeDurationMinutes[normalized] ?: Int.MAX_VALUE
}
internal fun mcMissionDisplaySortRank(mission: com.example.model.Mission, originalIndex: Int): Triple<Int, Int, Int> {
    val criticalBucket = if (mcMissionRiskState(mission) == "CRITICAL") 0 else 1
    val timeframeRank = mcTimeframeRank(mission.signalTimeframe)
    val warningBucket = when (mcMissionRiskState(mission)) {
        "WARNING" -> 0
        "ACTIVE" -> 1
        else -> 2
    }
    return Triple(criticalBucket, timeframeRank, warningBucket * 10_000 + originalIndex)
}
internal fun mcMissionDirectionMultiplier(mission: com.example.model.Mission): Double {
    val isLong = mission.type.equals("LONG", ignoreCase = true) || mission.type.equals("BUY", ignoreCase = true)
    return if (isLong) 1.0 else -1.0
}
internal fun mcMissionRoiPct(mission: com.example.model.Mission): Double {
    if (mission.entryPrice <= 0.0) return 0.0
    val rawPct = ((mission.currentPrice - mission.entryPrice) / mission.entryPrice) * 100.0
    return rawPct * mcMissionDirectionMultiplier(mission)
}
internal fun mcMissionPnlUsd(mission: com.example.model.Mission): Double {
    if (mission.entryPrice <= 0.0) return 0.0
    val notional = mission.positionSize
        ?.replace("$", "")
        ?.replace(",", "")
        ?.trim()
        ?.toDoubleOrNull()
    val quantity = if (notional != null && notional > 0.0) notional / mission.entryPrice else 1.0
    return (mission.currentPrice - mission.entryPrice) * quantity * mcMissionDirectionMultiplier(mission)
}
internal fun mcMissionRiskState(mission: com.example.model.Mission): String {
    val roi = mcMissionRoiPct(mission)
    return when {
        roi <= -5.0 -> "CRITICAL"
        roi < 0.0 -> "WARNING"
        else -> "ACTIVE"
    }
}
internal fun mcFormatUsd(value: Double): String {
    return "$" + String.format(java.util.Locale.US, "%,.2f", value)
}
internal fun mcFormatMissionPriceText(value: String?): String {
    val clean = value
        ?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "")
        ?.replace("$", "")
        ?.replace(",", "")
        ?.trim()
        .orEmpty()
    if (clean.isBlank() || clean.equals("N/A", ignoreCase = true) || clean.equals("NOT SET", ignoreCase = true)) return "NOT SET"
    val numeric = clean.toDoubleOrNull() ?: return value.orEmpty().ifBlank { "NOT SET" }
    return mcFormatUsd(numeric)
}
internal fun mcDisplayLeverage(value: String?, isFutures: Boolean): String {
    val normalized = value.orEmpty().trim().uppercase()
    return when {
        normalized.isBlank() && isFutures -> "NOT SET"
        normalized.isBlank() -> "1X"
        normalized == "SPOT" -> "1X"
        normalized == "SPOT / 1X" -> "1X"
        normalized == "SPOT/1X" -> "1X"
        else -> normalized
    }
}
internal fun mcSetupModeColor(value: String): Color {
    val normalized = value.uppercase()
    return when {
        normalized.contains("RECOMMENDED") -> T_Green
        normalized.contains("CUSTOM") || normalized.contains("OVERRIDDEN") -> T_Gold
        normalized.contains("PENDING") || normalized.contains("INCOMPLETE") -> T_TextSecondary
        else -> T_Cyan
    }
}
internal fun mcRiskProfileColor(value: String?): Color {
    val normalized = value?.uppercase().orEmpty()
    return when {
        normalized.contains("CONSERVATIVE") || normalized.contains("SAFE") || normalized.contains("DEFAULT") -> T_Cyan
        normalized.contains("MODERATE") || normalized.contains("BALANCED") -> T_Green
        normalized.contains("AGGRESSIVE") || normalized.contains("CUSTOM") -> T_Gold
        normalized.contains("LOW") || normalized.contains("MEDIUM") || normalized.contains("HIGH") || normalized.contains("EXTREME") || normalized.contains("CRITICAL") || normalized.contains("INVALID") -> titanRiskScoreColorFromLabel(normalized)
        else -> T_TextPrimary
    }
}
internal fun mcSignedUsd(value: Double): String {
    val sign = if (value < 0.0) "-" else "+"
    return sign + "$" + String.format(java.util.Locale.US, "%,.2f", kotlin.math.abs(value))
}
internal fun mcSignedPct(value: Double): String {
    val sign = if (value < 0.0) "-" else "+"
    return sign + String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(value)) + "%"
}
internal fun mcCleanPriceText(value: String?): String {
    return value
        ?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "")
        ?.trim()
        .orEmpty()
}
internal fun mcFormatDisplayPrice(value: String?): String {
    val cleaned = mcCleanPriceText(value)
    if (cleaned.isBlank() || cleaned.equals("NOT SET", ignoreCase = true) || cleaned.equals("N/A", ignoreCase = true)) return if (cleaned.isBlank()) "NOT SET" else cleaned.uppercase()
    val numeric = cleaned.replace("$", "").replace(",", "").toDoubleOrNull()
    return if (numeric != null) mcFormatUsd(numeric) else cleaned
}
internal fun mcValidateAutoTrading(
    enabled: Boolean,
    conditions: List<String>,
    entryPrice: Double,
    target: String?,
    stopLoss: String?,
    isLong: Boolean
): Pair<String, String?> {
    if (!enabled) return "INACTIVE" to "Auto-trading disabled by user."
    if (conditions.isEmpty()) return "INVALID" to "Auto-trading active but no condition was provided."
    val targetValue = mcCleanPriceText(target).replace("$", "").replace(",", "").toDoubleOrNull()
    val stopValue = mcCleanPriceText(stopLoss).replace("$", "").replace(",", "").toDoubleOrNull()
    if (entryPrice <= 0.0) return "INVALID" to "Entry price is missing or invalid."
    if (targetValue == null || targetValue <= 0.0) return "INVALID" to "Target price is missing or invalid."
    if (stopValue == null || stopValue <= 0.0) return "INVALID" to "Stop loss is missing or invalid."
    if (isLong && targetValue <= entryPrice) return "INVALID" to "LONG target must be above entry."
    if (isLong && stopValue >= entryPrice) return "INVALID" to "LONG stop loss must be below entry."
    if (!isLong && targetValue >= entryPrice) return "INVALID" to "SHORT target must be below entry."
    if (!isLong && stopValue <= entryPrice) return "INVALID" to "SHORT stop loss must be above entry."
    val allowed = listOf("TP", "TARGET", "STOP", "SL", "ROI", "PNL", "PRICE", "TRAIL", "TIME", "BREAK", "AUTO")
    val bad = conditions.map { it.uppercase() }.firstOrNull { c -> allowed.none { c.contains(it) } }
    if (bad != null) return "INVALID" to "Unsupported auto-trading condition: $bad"
    return "VALID" to null
}
@Composable
fun MissionTerminalHeaderBlock(viewModel: CryptoViewModel) {
    val missions by viewModel.activeMissions.collectAsState()
    val headerWarnings = missions.count { mcMissionRiskState(it) == "WARNING" }
    val headerCriticals = missions.count { mcMissionRiskState(it) == "CRITICAL" }
    val guardianStatus = when {
        missions.isEmpty() -> "STANDBY"
        headerCriticals > 0 -> "CRITICAL"
        headerWarnings > 0 -> "WARNING"
        else -> "ACTIVE"
    }
    val guardianStatusColor = when (guardianStatus) {
        "ACTIVE" -> T_Green
        "WARNING" -> T_Gold
        "CRITICAL" -> T_Red
        else -> T_TextSecondary
    }
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
                    text = "AI COPILOT TRADE GUARDIAN",
                    color = T_TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = guardianStatus,
                    color = guardianStatusColor,
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
internal fun McTerminalClockWidget() {
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
internal fun McStatusBox(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
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
fun PremiumCopilotActivationBanner(isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(T_Surface, RoundedCornerShape(8.dp))
            .border(1.dp, T_BorderHigh, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(T_Cyan, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isBengali) "এআই ট্রেড গাইডেন্স সক্রিয়" else "AI TRADE GUARDIAN ACTIVE",
                color = T_TextPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isBengali) "এআই ট্রেড গাইডেন্স সতর্কতা-শুধুমাত্র মোডে মিশন মনিটর করছে।" else "AI Trade Guardian is monitoring this mission in Alert-Only mode.",
            color = T_TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            McStatusChip(if (isBengali) "সতর্কতা-শুধুমাত্র" else "ALERT-ONLY", T_Cyan)
            McStatusChip("MANUAL APPROVAL REQ", T_Gold)
            McStatusChip("AUTO-EXECUTE: OFF", T_TextMuted)
        }
    }
}
@Composable
fun McStatusChip(text: String, color: Color) {
    Box(modifier = Modifier.background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 3.dp)) {
        Text(text, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun MissionSetupSummary(isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(T_Surface, RoundedCornerShape(8.dp))
            .border(1.dp, T_BorderHigh, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (isBengali) "মিশন সেটআপ সারাংশ" else "MISSION SETUP SUMMARY",
            color = T_TextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                SetupRow("ENTRY", "LOCKED", T_Green)
                Spacer(modifier = Modifier.height(8.dp))
                SetupRow("SIGNAL", "PRESERVED", T_TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                SetupRow("SOURCE", "SIGNAL PRO", T_TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                SetupRow("RISK PROFILE", "MODERATE", T_Green)
                Spacer(modifier = Modifier.height(8.dp))
                SetupRow("COPILOT MODE", "ALERT-ONLY", T_Cyan)
                Spacer(modifier = Modifier.height(8.dp))
                SetupRow("EXECUTION", "DISABLED", T_TextMuted)
            }
        }
    }
}
@Composable
fun SetupRow(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = valueColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun HeaderSummaryDashboard(viewModel: CryptoViewModel, isBengali: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(T_Surface, RoundedCornerShape(4.dp))
            .border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text("MISSION CENTER", color = T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text("Active signal supervision cockpit", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ACTIVE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text("2", color = T_Cyan, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PENDING", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text("1", color = T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PROTECTED", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text("1", color = T_Gold, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("INVALID", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text("0", color = T_Red, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("COMPLETED", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text("0", color = T_Green, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}
@Composable
fun SummaryStatBox(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = valueColor, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun EscalationPolicyCard(viewModel: CryptoViewModel, isBengali: Boolean) {
    val missions by viewModel.activeMissions.collectAsState()
    val warningCount = missions.count { mcMissionRiskState(it) == "WARNING" }
    val criticalCount = missions.count { mcMissionRiskState(it) == "CRITICAL" }
    val executionState = when {
        missions.isEmpty() -> "STANDBY"
        criticalCount > 0 -> "CRITICAL"
        warningCount > 0 -> "WARNING"
        else -> "MATCH"
    }
    val executionColor = when (executionState) {
        "MATCH" -> T_Green
        "WARNING" -> T_Gold
        "CRITICAL" -> T_Red
        else -> T_TextSecondary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(T_Surface, RoundedCornerShape(4.dp))
            .border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "AI COPILOT EXECUTION STATUS",
                color = T_TextMuted,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                executionState,
                color = executionColor,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            McExecutionStateChip("MATCH", T_Green, executionState == "MATCH", Modifier.weight(1f))
            McExecutionStateChip("WARNING", T_Gold, executionState == "WARNING", Modifier.weight(1f))
            McExecutionStateChip("CRITICAL", T_Red, executionState == "CRITICAL", Modifier.weight(1f))
        }
    }
}
@Composable
internal fun McExecutionStateChip(label: String, color: Color, isActive: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = if (isActive) 0.18f else 0.045f), RoundedCornerShape(2.dp))
            .border(0.5.dp, if (isActive) color else T_BorderHigh, RoundedCornerShape(2.dp))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isActive) color else T_TextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun PolicyRow(label: String, desc: String, labelColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Text(text = label, color = labelColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
        Text(text = desc, color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.SansSerif)
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
