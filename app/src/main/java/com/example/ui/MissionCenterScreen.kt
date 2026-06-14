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

import com.example.ui.theme.DarkBackground

// Terminal Colors - Institutional Grade
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


private fun mcMissionDirectionMultiplier(mission: com.example.model.Mission): Double {
    val isLong = mission.type.equals("LONG", ignoreCase = true) || mission.type.equals("BUY", ignoreCase = true)
    return if (isLong) 1.0 else -1.0
}

private fun mcMissionRoiPct(mission: com.example.model.Mission): Double {
    if (mission.entryPrice <= 0.0) return 0.0
    val rawPct = ((mission.currentPrice - mission.entryPrice) / mission.entryPrice) * 100.0
    return rawPct * mcMissionDirectionMultiplier(mission)
}

private fun mcMissionPnlUsd(mission: com.example.model.Mission): Double {
    if (mission.entryPrice <= 0.0) return 0.0
    val notional = mission.positionSize
        ?.replace("$", "")
        ?.replace(",", "")
        ?.trim()
        ?.toDoubleOrNull()
    val quantity = if (notional != null && notional > 0.0) notional / mission.entryPrice else 1.0
    return (mission.currentPrice - mission.entryPrice) * quantity * mcMissionDirectionMultiplier(mission)
}

private fun mcMissionRiskState(mission: com.example.model.Mission): String {
    val roi = mcMissionRoiPct(mission)
    return when {
        roi <= -5.0 -> "CRITICAL"
        roi < 0.0 -> "WARNING"
        else -> "ACTIVE"
    }
}

private fun mcFormatUsd(value: Double): String {
    return "$" + String.format(java.util.Locale.US, "%,.2f", value)
}

private fun mcFormatMissionPriceText(value: String?): String {
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

private fun mcDisplayLeverage(value: String?, isFutures: Boolean): String {
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

private fun mcSetupModeColor(value: String): Color {
    val normalized = value.uppercase()
    return when {
        normalized.contains("RECOMMENDED") -> T_Green
        normalized.contains("CUSTOM") || normalized.contains("OVERRIDDEN") -> T_Gold
        normalized.contains("PENDING") || normalized.contains("INCOMPLETE") -> T_TextSecondary
        else -> T_Cyan
    }
}


private fun mcRiskProfileColor(value: String?): Color {
    val normalized = value?.uppercase().orEmpty()
    return when {
        normalized.contains("CONSERVATIVE") || normalized.contains("SAFE") || normalized.contains("LOW") -> T_Cyan
        normalized.contains("BALANCED") || normalized.contains("MEDIUM") || normalized.contains("MODERATE") -> T_Green
        normalized.contains("AGGRESSIVE") || normalized.contains("ELEVATED") || normalized.contains("CUSTOM") -> T_Gold
        normalized.contains("HIGH") || normalized.contains("CRITICAL") || normalized.contains("EXTREME") || normalized.contains("INVALID") -> T_Red
        normalized.contains("DEFAULT") -> T_Cyan
        else -> T_TextPrimary
    }
}

private fun mcSignedUsd(value: Double): String {
    val sign = if (value < 0.0) "-" else "+"
    return sign + "$" + String.format(java.util.Locale.US, "%,.2f", kotlin.math.abs(value))
}

private fun mcSignedPct(value: Double): String {
    val sign = if (value < 0.0) "-" else "+"
    return sign + String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(value)) + "%"
}

private fun mcCleanPriceText(value: String?): String {
    return value
        ?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "")
        ?.trim()
        .orEmpty()
}

private fun mcFormatDisplayPrice(value: String?): String {
    val cleaned = mcCleanPriceText(value)
    if (cleaned.isBlank() || cleaned.equals("NOT SET", ignoreCase = true) || cleaned.equals("N/A", ignoreCase = true)) return if (cleaned.isBlank()) "NOT SET" else cleaned.uppercase()
    val numeric = cleaned.replace("$", "").replace(",", "").toDoubleOrNull()
    return if (numeric != null) mcFormatUsd(numeric) else cleaned
}

private fun mcValidateAutoTrading(
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
            Spacer(modifier = Modifier.height(10.dp))
            HeaderSummaryDashboard(viewModel, isBengali)
            Spacer(modifier = Modifier.height(8.dp))
            EscalationPolicyCard(viewModel, isBengali)
            Spacer(modifier = Modifier.height(10.dp))
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
                SetupRow("RISK PROFILE", "STANDARD", T_Gold)
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
    val missions by viewModel.activeMissions.collectAsState()
    val totalActive = missions.size
    
    var aggregatePct = 0.0
    var activeWarnings = 0
    var activeCriticals = 0
    var aggregateDollr = 0.0
    var allHavePositionSize = true
    
    var spotCount = 0
    var futuresCount = 0
    
    if (missions.isNotEmpty()) {
        missions.forEach { m ->
            val diff = m.currentPrice - m.entryPrice
            val isLong = m.type.uppercase() == "LONG" || m.type.uppercase() == "BUY"
            val typeMult = if (isLong) 1.0 else -1.0
            val diffPct = (diff / m.entryPrice) * 100.0 * typeMult
            aggregatePct += diffPct
            
            if (m.marketType.uppercase() == "FUTURES") futuresCount++ else spotCount++

            when (mcMissionRiskState(m)) {
                "CRITICAL" -> activeCriticals++
                "WARNING" -> activeWarnings++
            }

            aggregateDollr += mcMissionPnlUsd(m)
        }
        aggregatePct /= missions.size
    }

    val pnlColor = if (aggregatePct > 0) T_Green else if (aggregatePct < 0) T_Red else T_TextSecondary
    val pnlSign = if (aggregatePct > 0) "+" else ""
    val dollrSign = if (aggregateDollr > 0) "+" else ""
    val dollrString = mcSignedUsd(aggregateDollr)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(T_Surface, RoundedCornerShape(4.dp))
            .border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text("ACTIVE MISSION SUMMARY", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text("ACTIVE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(totalActive.toString(), color = T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SPOT", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(spotCount.toString(), color = T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("FUTURES", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(futuresCount.toString(), color = T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text("AGGREGATE PnL", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                val pnlUsdText = if (missions.isEmpty()) "+$0.00" else dollrString
                val pnlRoiText = if (missions.isEmpty()) "(+0.00%)" else "(${mcSignedPct(aggregatePct)})"
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(pnlUsdText, color = pnlColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(pnlRoiText, color = pnlColor.copy(alpha = 0.86f), fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WARNING", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(activeWarnings.toString(), color = if (activeWarnings > 0) T_Gold else T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("CRITICAL", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(activeCriticals.toString(), color = if (activeCriticals > 0) T_Red else T_TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
private fun McExecutionStateChip(label: String, color: Color, isActive: Boolean, modifier: Modifier = Modifier) {
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
                    setupMode = m.setupMode,
                    tp1 = m.tp1,
                    tp2 = m.tp2,
                    tp3 = m.tp3,
                    manualStopLoss = m.manualStopLoss,
                    leverage = m.leverage,
                    riskProfile = m.riskProfile,
                    aiStatus = if (isBengali) m.aiStatusBengali else m.aiStatusEnglish,
                    confidence = m.confidence,
                    isNegative = isLoss,
                    isBengali = isBengali,
                    isHistory = false,
                    onStop = { overrideValue -> viewModel.stopMission(m.id, overrideValue) },
                    mission = m,
                    viewModel = viewModel
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
                    setupMode = m.setupMode,
                    tp1 = m.tp1,
                    tp2 = m.tp2,
                    tp3 = m.tp3,
                    manualStopLoss = m.manualStopLoss,
                    leverage = m.leverage,
                    riskProfile = m.riskProfile,
                    aiStatus = (if (isBengali) "মিশন সম্পন্ন হয়েছে" else "MISSION COMPLETED") + " - ${if (isLoss) "LOSS" else "PROFIT"}",
                    confidence = m.confidence,
                    isNegative = isLoss,
                    isBengali = isBengali,
                    isHistory = true,
                    onStop = {}, // No-op in history
                    mission = m,
                    viewModel = viewModel
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
fun CompactDataRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = valueColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
    setupMode: String?,
    tp1: String?,
    tp2: String?,
    tp3: String?,
    manualStopLoss: String?,
    leverage: String?,
    riskProfile: String?,
    aiStatus: String,
    confidence: Int,
    isNegative: Boolean,
    isBengali: Boolean,
    isHistory: Boolean,
    onStop: (Boolean?) -> Unit,
    mission: com.example.model.Mission? = null,
    viewModel: com.example.viewmodel.CryptoViewModel? = null
) {
    val roiColor = if (isNegative) T_Red else T_Green
    val typeColor = if (type.uppercase() == "LONG") T_Green else T_Red
    val isFutures = marketType.uppercase() == "FUTURES"
    val isLong = type.uppercase() == "LONG" || type.uppercase() == "BUY"
    
    val currentVal = currentPrice.replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0
    val entryVal = entryPrice.replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0
    val targetVal = mission?.target?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "")?.replace("$", "")?.replace(",", "")?.toDoubleOrNull()
    
    val totalDistance = if (targetVal != null) kotlin.math.abs(targetVal - entryVal) else 0.0
    val currentDistance = if (isLong) {
        if (currentVal > entryVal) currentVal - entryVal else 0.0
    } else {
        if (currentVal < entryVal) entryVal - currentVal else 0.0
    }
    val pctToTarget = if (totalDistance > 0.0) ((currentDistance / totalDistance) * 100).toInt().coerceIn(0, 100) else null
    val targetProgressText = if (pctToTarget != null) "($pctToTarget%)" else "(N/A)"
    
    // Status text logic based on current price versus targets
    var statusText = "PENDING"
    if (targetVal != null && entryVal != 0.0) {
        val tp1Val = tp1?.replace("$", "")?.replace(",", "")?.toDoubleOrNull()
        val tp2Val = tp2?.replace("$", "")?.replace(",", "")?.toDoubleOrNull()
        val tp3Val = tp3?.replace("$", "")?.replace(",", "")?.toDoubleOrNull()
        val slVal = (manualStopLoss ?: stopLoss).replace("$", "")?.replace(",", "")?.toDoubleOrNull()
        
        if (isLong) {
            if (tp3Val != null && currentVal >= tp3Val) statusText = "TP3"
            else if (tp2Val != null && currentVal >= tp2Val) statusText = "TP2"
            else if (tp1Val != null && currentVal >= tp1Val) statusText = "TP1"
            else if (slVal != null && currentVal <= slVal) statusText = "STOP LOSS"
        } else {
            if (tp3Val != null && currentVal <= tp3Val) statusText = "TP3"
            else if (tp2Val != null && currentVal <= tp2Val) statusText = "TP2"
            else if (tp1Val != null && currentVal <= tp1Val) statusText = "TP1"
            else if (slVal != null && currentVal >= slVal) statusText = "STOP LOSS"
        }
    }
    
    // Calculate PnL if possible
    val pnlVal = mission?.let { mcMissionPnlUsd(it) } ?: if (entryVal != 0.0) {
        (currentVal - entryVal) * (if (isLong) 1.0 else -1.0)
    } else 0.0
    val pnlText = mcSignedUsd(pnlVal)
    val pnlTextColor = if (pnlVal > 0.0) T_Green else if (pnlVal < 0.0) T_Red else T_TextSecondary

    val SpacingCompact = 4.dp
    val SpacingNormal = 8.dp
    val SpacingMedium = 12.dp
    val SpacingLarge = 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(T_Surface)
            .border(0.5.dp, T_BorderHigh, RoundedCornerShape(8.dp))
    ) {
        // ACTIVE MISSION CARD HEADER
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = coinSymbol.uppercase(),
                    color = T_TextPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(if (isLong) T_Green else T_Red, RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = type.uppercase(),
                        color = T_Bg,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = marketType.uppercase(),
                    color = T_TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                val tf = mission?.signalTimeframe?.ifBlank { "N/A" } ?: "N/A"
                Text(
                    text = tf.uppercase(),
                    color = T_TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)
        
        // MAIN SNAPSHOT SECTION
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingMedium)) {
            val entryClean = mcFormatUsd(entryVal)
            val liveClean = mcFormatUsd(currentVal)
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("LIVE PRICE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    Text(liveClean, color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TARGET", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    val tgDisp = mission?.target?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "") ?: "N/A"
                    Text(tgDisp, color = T_Green, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("STATUS", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    val statusColor = when (statusText.uppercase()) {
                        "TP1", "TP2", "TP3", "TARGET HIT" -> T_Green
                        "STOP LOSS", "SL", "SL1", "SL2" -> T_Red
                        "CLOSED" -> T_TextSecondary
                        "INVALID" -> T_Red
                        "WARNING" -> T_Gold
                        "FALL BACK" -> T_Cyan
                        else -> T_TextPrimary
                    }
                    Text(statusText, color = statusColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingNormal))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("PnL", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    Text(pnlText, color = pnlTextColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ROI", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    Text(roi, color = roiColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("TRADING TIME", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    Text(timeElapsed, color = T_TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingCompact))
        if (isFutures) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingMedium)
                    .background(T_Bg, RoundedCornerShape(4.dp))
                    .border(0.5.dp, T_BorderHigh, RoundedCornerShape(4.dp))
                    .padding(horizontal = SpacingNormal, vertical = SpacingNormal)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LIQ RISK", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(SpacingCompact))
                        Text("N/A", color = T_TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MARGIN RISK", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(SpacingCompact))
                        Text("UNKNOWN", color = T_TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("LEVERAGE", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(SpacingCompact))
                        Text(leverage?.uppercase() ?: "NOT SET", color = T_TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
        }
        
        // TARGET PROGRESS
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = 4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TARGET PROGRESS", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(targetProgressText, color = T_TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(T_BorderHigh.copy(alpha = 0.65f))
            ) {
                val progressFraction = ((pctToTarget ?: 0).toFloat() / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .background(if (progressFraction >= 1f) T_Green else T_Cyan)
                )
            }
        }
        
        // INFO GRID
        val sMode = setupMode?.uppercase() ?: "PENDING MANUAL SETUP"
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT COLUMN
            Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                CompactDataRow("TP1", tp1?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "") ?: "NOT SET", if (tp1 != null) T_Green else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("TP2", tp2?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "") ?: "NOT SET", if (tp2 != null) T_Green else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("TP3", tp3?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "") ?: "NOT SET", if (tp3 != null) T_Green else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("ENTRY", mcFormatUsd(entryVal), T_TextPrimary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                val setupDisplay = sMode.replace("OVERRIDDEN", "").replace("CUSTOM", "").replace("(", "").replace(")", "").replace("RECOMMENDED SETUP", "RECOMMENDED").trim()
                val setupColor = if (setupDisplay.contains("RECOMMENDED", ignoreCase = true)) T_Green else T_Cyan
                CompactDataRow("SETUP", setupDisplay, setupColor)
                Spacer(modifier = Modifier.height(SpacingCompact))
                val overrideCount = if (sMode.contains("OVERRIDDEN") || sMode.contains("CUSTOM")) "1" else "0" // Simulated override count
                CompactDataRow("OVERRIDE", overrideCount, T_TextPrimary)
            }
            // RIGHT COLUMN
            Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                CompactDataRow("SL1", (manualStopLoss ?: stopLoss).replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), ""), T_Gold)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("SL2", mission?.sl2?.replace("(?i)\\s*/\\s*SIGNAL FALLBACK".toRegex(), "") ?: "NOT SET", if (mission?.sl2 != null) T_Gold else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                val levValue = mcDisplayLeverage(leverage, isFutures)
                CompactDataRow("LEVERAGE", levValue, T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("ALLOCATION", mission?.positionSize?.uppercase() ?: "NOT SET", T_TextPrimary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                val profUpper = mission?.riskProfile?.uppercase() ?: "NOT SET"
                val profColor = mcRiskProfileColor(profUpper)
                CompactDataRow("RISK PROFILE", profUpper, profColor)
                Spacer(modifier = Modifier.height(SpacingCompact))
                val isAutoActive = mission?.autoCloseEnabled == true
                val isConditionValid = mission?.conditionValidity == "VALID"
                val autoTradingStatus = when {
                    isAutoActive && isConditionValid -> "ACTIVE"
                    isAutoActive && !isConditionValid -> "INVALID"
                    else -> "INACTIVE"
                }
                val autoColor = when (autoTradingStatus) {
                    "ACTIVE" -> T_Cyan
                    "INVALID" -> T_Red
                    else -> T_TextSecondary
                }
                CompactDataRow("AUTO-TRADING", autoTradingStatus, autoColor)
            }
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)

        // AUTO-TRADING & AI POLICY ROW
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            val policyText = if (mission?.copilotMode?.contains("EXECUTION") == true) "ASSIST & EXECUTION" else "ASSIST ONLY"
            CompactDataRow("AI COPILOT POLICY", policyText, if (policyText == "ASSIST ONLY") T_Cyan else T_Gold)
            
            if (policyText == "ASSIST & EXECUTION") {
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("MODE", "SHADOW ONLY", T_Gold)
            }
            
            Spacer(modifier = Modifier.height(SpacingCompact))
            CompactDataRow("REAL ORDER", "NO", T_TextMuted)
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)


        // AI EXECUTION COMPACT GUIDANCE
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("AI COPILOT EXECUTION STATUS", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SpacingCompact)) {
                // MATCH | WARNING | CRITICAL layout
                val statusTitleColor = if (confidence >= 80) T_Green else if (confidence >= 60) T_Gold else T_Red
                Box(modifier = Modifier.weight(1f).background(if (confidence >= 80) T_Green.copy(alpha = 0.1f) else T_Surface, RoundedCornerShape(2.dp)).border(0.5.dp, if (confidence >= 80) T_Green else T_BorderHigh, RoundedCornerShape(2.dp)).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("MATCH", color = if (confidence >= 80) T_Green else T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).background(if (confidence in 60..79) T_Gold.copy(alpha = 0.1f) else T_Surface, RoundedCornerShape(2.dp)).border(0.5.dp, if (confidence in 60..79) T_Gold else T_BorderHigh, RoundedCornerShape(2.dp)).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("WARNING", color = if (confidence in 60..79) T_Gold else T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).background(if (confidence < 60) T_Red.copy(alpha = 0.1f) else T_Surface, RoundedCornerShape(2.dp)).border(0.5.dp, if (confidence < 60) T_Red else T_BorderHigh, RoundedCornerShape(2.dp)).padding(4.dp), contentAlignment = Alignment.Center) {
                    Text("CRITICAL", color = if (confidence < 60) T_Red else T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingNormal))
            
            val actRec = "HOLD"
            val actConf = "N/A"
            val actRegime = "ACCUMULATING"
            val actDir = "NEUTRAL"
            
            val actRecColor = when (actRec) {
                "HOLD", "WAIT FOR CONFIRMATION" -> T_Cyan
                "WATCH", "TIGHTEN SL" -> T_Gold
                "CLOSE" -> T_Red
                "TAKE PARTIAL PROFIT" -> T_Green
                else -> T_Cyan
            }
            
            val actConfColor = T_TextSecondary // hardcoded for N/A as per rule
            
            val actRegimeColor = when (actRegime) {
                "ACCUMULATING" -> T_Cyan
                "DISTRIBUTING" -> T_Gold
                "TRENDING" -> T_Green
                "CHOPPY", "SIDEWAY" -> T_Gold
                else -> T_TextSecondary
            }
            
            val actDirColor = when (actDir) {
                "BULLISH" -> T_Green
                "BEARISH" -> T_Red
                "NEUTRAL" -> T_Cyan
                "MIXED" -> T_Gold
                else -> T_TextSecondary
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = T_BorderHigh, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ACTION RECOMMENDATION: ", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(actRec, color = actRecColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CONFIDENCE: ", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(actConf, color = actConfColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(SpacingCompact))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("PERSISTED REGIME: ", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(actRegime, color = actRegimeColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("DIRECTION: ", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(actDir, color = actDirColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(SpacingNormal))
                Text("HOLD. WAITING FOR UPDATED MISSION CONTEXT.", color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(SpacingCompact))
                Text("REASON: SYSTEM INITIALIZATION / WAITING FOR MISSION DATA.", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
        
        Spacer(modifier = Modifier.height(SpacingMedium))
        
        if (!isHistory) {
            var showStopConfirm by remember { mutableStateOf(false) }

            if (showStopConfirm) {
                AlertDialog(
                    onDismissRequest = { showStopConfirm = false },
                    title = { Text(if (isBengali) "মিশন বন্ধ করুন" else "CLOSE MISSION", color = T_TextPrimary, fontFamily = FontFamily.Monospace) },
                    text = {
                        Column {
                            Text(if (isBengali) "বন্ধ করার কারণ নির্বাচন করুন:" else "SELECT CLOSURE REASON:", color = T_TextSecondary, fontFamily = FontFamily.Monospace)
                        }
                    },
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
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { showStopConfirm = false }, modifier = Modifier.fillMaxWidth()) {
                                Text("CANCEL", color = T_TextSecondary, fontFamily = FontFamily.Monospace)
                            }
                        }
                    },
                    dismissButton = {},
                    containerColor = T_Bg,
                    titleContentColor = T_TextPrimary,
                    textContentColor = T_TextSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            var showOverride by remember { mutableStateOf(false) }
            var showDetails by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium),
                horizontalArrangement = Arrangement.spacedBy(SpacingNormal)
            ) {
                Box(
                    modifier = Modifier.weight(1f).clickable { showDetails = true }.border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp)).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "DETAILS", color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.weight(1f).clickable { showOverride = true }.border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp)).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "OVERRIDE", color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.weight(1f).clickable { showStopConfirm = true }.background(T_TextPrimary, RoundedCornerShape(4.dp)).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "CLOSE", color = T_Bg, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStopConfirm = true }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "ABORT MISSION", color = T_Red.copy(alpha = 0.8f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
            
            if (showDetails) {
                AlertDialog(
                    onDismissRequest = { showDetails = false },
                    title = { Text("MISSION DETAILS", color = T_Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Source Module: Signal Pro", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Setup Used: ${setupMode ?: "Pending Manual Setup"}", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Target: ${mission?.target ?: "Not Set"}", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Locked Entry: $entryPrice", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Original Signal Entry: $originalEntry", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("TP1: ${tp1 ?: "Not Set"} | TP2: ${tp2 ?: "Not Set"} | TP3: ${tp3 ?: "Not Set"}", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("SL1: ${manualStopLoss ?: stopLoss} | SL2: ${mission?.sl2 ?: "Not Set"}", color = T_Red, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Leverage: ${leverage ?: if (marketType.equals("Spot", ignoreCase = true)) "1X" else "Not Set"}", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Allocation: ${mission?.positionSize ?: "Not Set"}", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Risk Profile: ${mission?.riskProfile ?: "Not Set"}", color = T_Gold, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Remark: ${mission?.setupRemark ?: "None"}", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Time in Trade: $timeElapsed", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val isAutoActive = mission?.autoCloseEnabled == true
                            Text("Auto-Trading: ${if (isAutoActive) mission?.autoCloseConditions?.joinToString(" / ") else "Inactive"}", color = if (isAutoActive) T_Cyan else T_TextMuted, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            if (isAutoActive) {
                                val conditionColor = if (mission?.conditionValidity == "VALID") T_Green else T_Red
                                Text("Condition Validity: ${mission?.conditionValidity ?: "N/A"}", color = conditionColor, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                                if (mission?.conditionValidity == "INVALID") {
                                    Text("Reason: ${mission?.conditionInvalidReason ?: "Unknown"}", color = T_Red, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val policyText = if (mission?.copilotMode?.contains("EXECUTION") == true) "ASSIST & EXECUTION" else "ASSIST ONLY"
                            Text("AI Copilot Policy: $policyText", color = T_Cyan, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            if (mission?.copilotMode?.contains("Shadow") == true || mission?.copilotMode?.contains("EXECUTION") == true) {
                                Text("Mode: Shadow Only", color = T_Gold, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            }
                            Text("Real Market Order: No", color = T_TextMuted, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("MISSION EVENT LOG", color = T_Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val logs = mission?.missionHistoryLog ?: emptyList()
                            if (logs.isEmpty()) {
                                Text("NO MISSION EVENTS RECORDED", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 150.dp)
                                        .background(T_Surface, RoundedCornerShape(4.dp))
                                        .border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        logs.takeLast(20).forEach { logMsg ->
                                            Text("> ${logMsg.uppercase()}", color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDetails = false }) {
                            Text("CLOSE", color = T_TextPrimary, fontFamily = FontFamily.Monospace)
                        }
                    },
                    containerColor = T_Bg,
                    titleContentColor = T_TextPrimary,
                    textContentColor = T_TextSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (showOverride && mission != null && viewModel != null) {
                var selectedSetup by remember { mutableStateOf(mission.setupMode ?: "RECOMMENDED SETUP") }
                var overrideTp1 by remember { mutableStateOf(mission.tp1?.replace(" / SIGNAL FALLBACK", "") ?: "") }
                var overrideTp2 by remember { mutableStateOf(mission.tp2 ?: "") }
                var overrideTp3 by remember { mutableStateOf(mission.tp3 ?: "") }
                var overrideSl by remember { mutableStateOf(mission.manualStopLoss?.replace(" / SIGNAL FALLBACK", "") ?: "") }
                var overrideLev by remember { mutableStateOf(mission.leverage?.replace(" / SIGNAL FALLBACK", "") ?: "") }
                var overrideRisk by remember { mutableStateOf(mission.riskProfile ?: "") }
                var overrideAlloc by remember { mutableStateOf(mission.positionSize ?: "") }
                var overrideRemark by remember { mutableStateOf(mission.setupRemark ?: "") }
                var aiPolicy by remember { mutableStateOf(mission.copilotMode?.takeIf { it.contains("EXECUTION") }?.let { "ASSIST & EXECUTION" } ?: "ASSIST ONLY") }
                var overrideAutoTrading by remember { mutableStateOf(mission.autoCloseEnabled) }
                var overrideAutoConditions by remember { mutableStateOf(mission.autoCloseConditions.joinToString(" / ")) }


                val custom1 by viewModel.customSetup1.collectAsState()
                val custom2 by viewModel.customSetup2.collectAsState()

                AlertDialog(
                    onDismissRequest = { showOverride = false },
                    title = { Text("OVERRIDE SETUP", color = T_Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val setups = listOf("RECOMMENDED SETUP", "CUSTOM SETUP-1", "CUSTOM SETUP-2")
                                setups.forEach { setup ->
                                    Box(
                                        modifier = Modifier.weight(1f).clickable { 
                                            selectedSetup = setup
                                            val profile = when (setup) {
                                                "CUSTOM SETUP-1" -> custom1
                                                "CUSTOM SETUP-2" -> custom2
                                                else -> null
                                            }
                                            if (profile != null) {
                                                overrideTp1 = profile.tp1
                                                overrideTp2 = profile.tp2
                                                overrideTp3 = profile.tp3
                                                overrideSl = profile.stopLoss
                                                overrideLev = profile.leverage
                                                overrideRisk = profile.riskProfile
                                                overrideAlloc = profile.positionSize
                                                overrideRemark = profile.remark
                                            } else {
                                                overrideTp1 = mission.targets
                                                overrideSl = mission.stopLoss
                                                overrideLev = ""
                                                overrideRisk = ""
                                                overrideAlloc = ""
                                                overrideRemark = ""
                                            }
                                        }
                                        .background(if (selectedSetup == setup) T_Gold.copy(alpha=0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (selectedSetup == setup) T_Gold else T_BorderHigh, RoundedCornerShape(4.dp))
                                        .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) { Text(setup.replace("SETUP", "").trim(), color = if (selectedSetup == setup) T_Gold else T_TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val textFieldColors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = T_Cyan, unfocusedBorderColor = T_BorderHigh,
                                focusedTextColor = T_TextPrimary, unfocusedTextColor = T_TextPrimary
                            )
                            
                            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { androidx.compose.material3.OutlinedTextField(value = overrideTp1, onValueChange = { overrideTp1 = it }, label = { Text("TP1", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideTp2, onValueChange = { overrideTp2 = it }, label = { Text("TP2", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideTp3, onValueChange = { overrideTp3 = it }, label = { Text("TP3", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideSl, onValueChange = { overrideSl = it }, label = { Text("Stop Loss", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideLev, onValueChange = { overrideLev = it }, label = { Text("Leverage", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideRisk, onValueChange = { overrideRisk = it }, label = { Text("Risk Profile", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideAlloc, onValueChange = { overrideAlloc = it }, label = { Text("Allocation", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item { androidx.compose.material3.OutlinedTextField(value = overrideRemark, onValueChange = { overrideRemark = it }, label = { Text("Remark", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = textFieldColors) }
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("AUTO-TRADING", color = T_Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier.weight(1f).clickable { overrideAutoTrading = false }
                                                .background(if (!overrideAutoTrading) T_TextSecondary.copy(alpha = 0.16f) else Color.Transparent, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (!overrideAutoTrading) T_TextSecondary else T_BorderHigh, RoundedCornerShape(4.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) { Text("INACTIVE", color = if (!overrideAutoTrading) T_TextSecondary else T_TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                        Box(
                                            modifier = Modifier.weight(1f).clickable { overrideAutoTrading = true }
                                                .background(if (overrideAutoTrading) T_Cyan.copy(alpha = 0.18f) else Color.Transparent, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (overrideAutoTrading) T_Cyan else T_BorderHigh, RoundedCornerShape(4.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) { Text("ACTIVE", color = if (overrideAutoTrading) T_Cyan else T_TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                                    }
                                }
                                if (overrideAutoTrading) {
                                    item { androidx.compose.material3.OutlinedTextField(value = overrideAutoConditions, onValueChange = { overrideAutoConditions = it }, label = { Text("Auto-Trading Conditions", fontSize = 10.sp) }, modifier = Modifier.fillMaxWidth(), minLines = 2, colors = textFieldColors) }
                                }
                                item { 
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("AI Copilot Policy", color = T_TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier.weight(1f).clickable { aiPolicy = "ASSIST ONLY" }
                                                .background(if (aiPolicy == "ASSIST ONLY") T_Cyan.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (aiPolicy == "ASSIST ONLY") T_Cyan else T_BorderHigh, RoundedCornerShape(4.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("ASSIST ONLY", color = if (aiPolicy == "ASSIST ONLY") T_Cyan else T_TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier.weight(1f).clickable { aiPolicy = "ASSIST & EXECUTION" }
                                                .background(if (aiPolicy == "ASSIST & EXECUTION") T_Gold.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(4.dp))
                                                .border(1.dp, if (aiPolicy == "ASSIST & EXECUTION") T_Gold else T_BorderHigh, RoundedCornerShape(4.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("EXECUTION", color = if (aiPolicy == "ASSIST & EXECUTION") T_Gold else T_TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (aiPolicy == "ASSIST & EXECUTION") {
                                    item {
                                        Text("SHADOW EXECUTION. NO REAL MARKET ORDER.", color = T_Gold, fontSize = 10.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val newLogs = mutableListOf<String>()
                            newLogs.add("OVERRIDE APPLIED")
                            newLogs.add("SETUP UPDATED: $selectedSetup")
                            if (mission.copilotMode != aiPolicy) {
                                newLogs.add("AI COPILOT POLICY UPDATED: $aiPolicy")
                                if (aiPolicy.contains("EXECUTION")) {
                                    newLogs.add("SHADOW EXECUTION ENABLED")
                                } else {
                                    newLogs.add("MANUAL APPROVAL REQUIRED")
                                }
                            }
                            
                            val overrideConditions = overrideAutoConditions
                                .split("\n", ";", "/")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .distinct()
                            val autoValidation = mcValidateAutoTrading(
                                enabled = overrideAutoTrading,
                                conditions = overrideConditions,
                                entryPrice = mission.entryPrice,
                                target = overrideTp3.ifBlank { overrideTp2.ifBlank { overrideTp1.ifBlank { mission.target } } },
                                stopLoss = overrideSl.ifBlank { mission.manualStopLoss ?: mission.stopLoss },
                                isLong = isLong
                            )
                            if (overrideAutoTrading) {
                                newLogs.add("AUTO-TRADING OVERRIDE: ${autoValidation.first}")
                                autoValidation.second?.let { newLogs.add("AUTO-TRADING VALIDATION: $it") }
                            }
                            val updatedLog = (mission.missionHistoryLog + newLogs).takeLast(20)

                            val updatedMission = mission.copy(
                                setupMode = "Overridden ($selectedSetup)",
                                setupRemark = overrideRemark.ifBlank { null },
                                tp1 = overrideTp1.ifBlank { null },
                                tp2 = overrideTp2.ifBlank { null },
                                tp3 = overrideTp3.ifBlank { null },
                                manualStopLoss = overrideSl.ifBlank { null },
                                leverage = overrideLev.ifBlank { null },
                                riskProfile = overrideRisk.ifBlank { null },
                                positionSize = overrideAlloc.ifBlank { null },
                                copilotMode = aiPolicy,
                                autoCloseEnabled = overrideAutoTrading,
                                autoCloseConditions = overrideConditions,
                                conditionValidity = autoValidation.first,
                                conditionInvalidReason = autoValidation.second,
                                missionHistoryLog = updatedLog
                            )
                            viewModel.updateMission(updatedMission)
                            showOverride = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = T_Gold)) {
                            Text("Apply Override", color = T_Bg, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOverride = false }) {
                            Text("Cancel", color = T_TextSecondary)
                        }
                    },
                    containerColor = T_Bg,
                    titleContentColor = T_TextPrimary,
                    textContentColor = T_TextSecondary,
                    shape = RoundedCornerShape(8.dp)
                )
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
fun McTabButton(
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
            .padding(vertical = 8.dp),
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
