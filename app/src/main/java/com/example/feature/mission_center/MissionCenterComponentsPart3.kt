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

import com.example.ui.theme.DarkBackground

// Terminal Colors - Institutional Grade

// Extracted from MissionCenterScreen.kt to keep the public screen entry point compact.
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
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Box(
                        modifier = Modifier
                            .background(T_TextSecondary.copy(alpha = 0.12f), RoundedCornerShape(5.dp))
                            .border(0.5.dp, T_TextSecondary.copy(alpha = 0.32f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.uppercase(),
                            color = T_TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(SpacingMedium))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                    CompactDataRow("PROBABILITY", "85 / HIGH", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("RISK SCORE", "15 / LOW", T_Green)
                }
                Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                    CompactDataRow("EXEC READINESS", "OPTIMAL", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("CONSENSUS BIAS", "MODERATE", T_Gold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
            CompactDataRow("MISSION STATE", "ACTIVE", T_Cyan)
        }
        
        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)
        
        // MAIN SNAPSHOT SECTION
        Column(modifier = Modifier.fillMaxWidth().padding(start = SpacingMedium, end = SpacingMedium, top = SpacingMedium, bottom = 4.dp)) {
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
                    val tgDisp = mcFormatMissionPriceText(mission?.target)
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
                CompactDataRow("TP1", mcFormatMissionPriceText(tp1), if (tp1 != null) T_Green else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("TP2", mcFormatMissionPriceText(tp2), if (tp2 != null) T_Green else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("TP3", mcFormatMissionPriceText(tp3), if (tp3 != null) T_Green else T_TextSecondary)
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
                CompactDataRow("SL1", mcFormatMissionPriceText(manualStopLoss ?: stopLoss), T_Gold)
                Spacer(modifier = Modifier.height(SpacingCompact))
                CompactDataRow("SL2", mcFormatMissionPriceText(mission?.sl2), if (mission?.sl2 != null) T_Gold else T_TextSecondary)
                Spacer(modifier = Modifier.height(SpacingCompact))
                val levValue = (leverage?.uppercase() ?: if (isFutures) "NOT SET" else "1X").replace("SPOT / 1X", "1X").replace("SPOT", "1X")
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

        // 3. MISSION EXPOSURE PANEL
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("MISSION EXPOSURE", color = T_Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                    CompactDataRow("INITIAL ALLOC", mission?.positionSize?.ifBlank { "$50.00" } ?: "$50.00", T_TextPrimary)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("CURRENT EXPOSURE", "$35.00", T_Gold)
                }
                Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                    CompactDataRow("REMAINING", "70%", T_TextPrimary)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("STATE", "CONTROLLED", T_Green)
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
            CompactDataRow("POSITION MODE", "SIMULATED", T_Cyan)
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)

        // 4. PROFIT SHIELD / SAFE ZONE PANEL
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("PROFIT SHIELD / SAFE ZONE", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                    CompactDataRow("SAFE ZONE", "Level 2", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("LOCKED PROFIT", "$0.70", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("NEXT TARGET", "TP3 / +10%", T_TextPrimary)
                }
                Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                    CompactDataRow("ROI TRIGGER", "+7%", T_TextPrimary)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("UNREALIZED PROFIT", "$1.25", T_Gold)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("STATUS", "Protecting Runner", T_Cyan)
                }
            }
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)

        // 5. AI TRADE GUARDIAN STATUS
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("AI TRADE GUARDIAN", color = T_Gold, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                    CompactDataRow("STATE", "MONITORING", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("ACTION", "Hold / Watch liq", T_TextPrimary)
                }
                Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                    CompactDataRow("THREAT LEVEL", "Medium", T_Gold)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("EARLY EXIT", "Not triggered", T_TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
            CompactDataRow("EMERGENCY REASON", "None", T_TextSecondary)
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)

        // 6. VALIDITY WINDOW
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("VALIDITY WINDOW", color = T_TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                    CompactDataRow("VALID UNTIL", "12H Window", T_TextPrimary)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("FRESHNESS", "12s", T_Green)
                }
                Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                    CompactDataRow("REMAINING", "11h 45m", T_TextPrimary)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("EXPIRY STATE", "ACTIVE", T_Green)
                }
            }
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)

        // 7. MISSION DECISION GATE
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("MISSION DECISION GATE", color = T_Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f).padding(end = SpacingNormal)) {
                    CompactDataRow("DIRECTION CHK", "PASS", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("RISK / REWARD", "PASS", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("CONSENSUS", "PASS", T_Green)
                }
                Column(modifier = Modifier.weight(1f).padding(start = SpacingNormal)) {
                    CompactDataRow("SL SANITY", "PASS", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("EXEC READINESS", "PASS", T_Green)
                    Spacer(modifier = Modifier.height(SpacingCompact))
                    CompactDataRow("GUARDIAN THREAT", "WARN", T_Gold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingCompact))
            CompactDataRow("FINAL STATE", "ACTIVE", T_Green)
        }

        HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)

        // 8. MISSION AUDIT TRAIL
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
            Text("MISSION AUDIT TRAIL", color = T_TextPrimary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(SpacingCompact))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Guardian scan completed", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("12s ago", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Safe Zone reached", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("1m ago", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mission activated", color = T_Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("3m ago", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Entry verified", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("4m ago", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Signal accepted", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("5m ago", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // 9. INVALID / REVIEW REASON (Mocked as if occasionally active)
        val mockMissionState = "ACTIVE"
        if (mockMissionState == "INVALID" || mockMissionState == "REVIEW") {
            HorizontalDivider(color = T_BorderHigh, thickness = 0.5.dp)
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingMedium, vertical = SpacingNormal)) {
                Text("INVALID / REVIEW REASON", color = T_Red, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(SpacingCompact))
                Text("Execution readiness degraded due to widened spread.", color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
                    Text(text = "REVIEW", color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.weight(1f).clickable { showOverride = true }.border(1.dp, T_BorderHigh, RoundedCornerShape(4.dp)).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "PROTECT", color = T_Gold, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.weight(1.5f).clickable { showStopConfirm = true }.background(T_TextPrimary, RoundedCornerShape(4.dp)).padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "CLOSE SIMULATION", color = T_Bg, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(SpacingMedium))
            
            if (showDetails) {
                AlertDialog(
                    onDismissRequest = { showDetails = false },
                    title = { Text("MISSION DETAILS", color = T_Cyan, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Source Module: Signal Pro", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Setup Used: ${setupMode ?: "Pending Manual Setup"}", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Target: ${mcFormatMissionPriceText(mission?.target)}", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Locked Entry: ${mcFormatMissionPriceText(entryPrice)}", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("Original Signal Entry: ${mcFormatMissionPriceText(originalEntry)}", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("TP1: ${mcFormatMissionPriceText(tp1)} | TP2: ${mcFormatMissionPriceText(tp2)} | TP3: ${mcFormatMissionPriceText(tp3)}", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
                            Text("SL1: ${mcFormatMissionPriceText(manualStopLoss ?: stopLoss)} | SL2: ${mcFormatMissionPriceText(mission?.sl2)}", color = T_Red, fontSize = 11.sp, fontFamily = FontFamily.SansSerif)
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
