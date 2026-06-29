package com.example.feature.signal_pro

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.graphics.Brush
import com.example.model.FuturesSignal
import com.example.model.OracleAnalysisResponse
import com.example.model.SpotSignal
import com.example.ui.theme.*
import com.example.viewmodel.AnalysisState
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.shadow

// Extracted from SignalProScreen.kt to keep the public screen entry point compact.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartTradeFlow(
    viewModel: CryptoViewModel,
    mission: com.example.model.Mission,
    livePrice: Double = mission.entryPrice
) {
    var step by remember { mutableStateOf(0) }
    var showDecisionBrief by remember { mutableStateOf(false) }
    var showMockupUi by remember { mutableStateOf(false) }
    val isBengali by viewModel.isBengali.collectAsState()
    val decisionBriefSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val setupSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val highConfidence = mission.confidence >= 85
    val isLong = mission.type.uppercase() == "LONG"

    val recommendationText = when {
        isBengali && highConfidence && isLong -> "উচ্চ আস্থা | এন্ট্রি যাচাই"
        isBengali && highConfidence && !isLong -> "উচ্চ আস্থা | শর্ট যাচাই"
        isBengali && !highConfidence -> "সতর্কভাবে যাচাই করুন"
        !isBengali && highConfidence -> "HIGH CONFIDENCE | VERIFY ENTRY |"
        else -> "REVIEW CAREFULLY | VERIFY ENTRY |"
    }

    val verdictText = when {
        isBengali && highConfidence -> "সিগন্যাল শক্তিশালী, তবে এন্ট্রি যাচাই করে নিন।"
        isBengali -> "সিগন্যাল কার্যকর, তবে ঝুঁকি যাচাই করা জরুরি।"
        highConfidence -> "Signal is strong, but entry confirmation is still required."
        else -> "Signal is active, but risk review is required before action."
    }

    val whyText = if (isBengali) {
        "ট্রেন্ড, মতিগতি, লেনদেন, TITAN AI consensus এবং risk score মিলিয়ে setup তৈরি হয়েছে।"
    } else {
        "Trend, momentum, volume, TITAN AI consensus, and risk score align for this setup."
    }

    val riskText = if (isBengali) {
        if (highConfidence) "রিস্ক কম থেকে মাঝারি। Stop loss এবং position size মেনে চলুন।"
        else "রিস্ক মাঝারি। দেরিতে entry নিলে signal quality কমতে পারে।"
    } else {
        if (highConfidence) "Risk is low to medium. Follow stop loss and position sizing."
        else "Risk is medium. Late entry may reduce signal quality."
    }

    val actionText = if (isBengali) {
        if (isLong) "এন্ট্রি price, stop loss এবং target মিলিয়ে তারপর Accept Signal করুন।"
        else "শর্ট এন্ট্রি, stop loss এবং target মিলিয়ে তারপর Accept Signal করুন।"
    } else {
        if (isLong) "Verify entry price, stop loss, and target before accepting the signal."
        else "Verify short entry, stop loss, and target before accepting the signal."
    }

    val disclaimerText = if (isBengali) {
        "TITAN AI সিদ্ধান্তে সহায়তা করে; চূড়ান্ত ট্রেডিং সিদ্ধান্ত আপনার।"
    } else {
        "TITAN AI assists decision-making; the final trading decision is yours."
    }

    val parsedSignalTargets = remember(mission.targets) {
        mission.targets
            .split("/")
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals("N/A", ignoreCase = true) && !it.equals("NOT SET", ignoreCase = true) }
    }
    val defaultTp1 = parsedSignalTargets.getOrNull(0).orEmpty()
    val defaultTp2 = parsedSignalTargets.getOrNull(1).orEmpty()
    val defaultTp3 = parsedSignalTargets.getOrNull(2).orEmpty()
    val defaultTarget = parsedSignalTargets.lastOrNull().orEmpty()
    val defaultStopLoss = mission.stopLoss.takeIf { it.isNotBlank() && !it.equals("N/A", ignoreCase = true) && !it.equals("NOT SET", ignoreCase = true) }.orEmpty()
    val defaultLeverage = if (mission.marketType.contains("Futures", ignoreCase = true)) "NOT SET" else "1X"

    var setupTarget by remember(mission.id, mission.targets, mission.stopLoss) { mutableStateOf(defaultTarget) }
    var setupTp1 by remember(mission.id, mission.targets, mission.stopLoss) { mutableStateOf(defaultTp1) }
    var setupTp2 by remember(mission.id, mission.targets, mission.stopLoss) { mutableStateOf(defaultTp2) }
    var setupTp3 by remember(mission.id, mission.targets, mission.stopLoss) { mutableStateOf(defaultTp3) }
    var setupSl1 by remember(mission.id, mission.targets, mission.stopLoss) { mutableStateOf(defaultStopLoss) }
    var setupSl2 by remember(mission.id, mission.targets, mission.stopLoss) { mutableStateOf("") }
    var setupLeverage by remember(mission.id, mission.marketType) { mutableStateOf(defaultLeverage) }
    var setupAllocation by remember(mission.id) { mutableStateOf(if (highConfidence) "5.0% Cap" else "2.0% Cap") }
    var setupRiskProfile by remember(mission.id) { mutableStateOf(if (highConfidence) "MODERATE" else "CONSERVATIVE") }
    var setupRemark by remember(mission.id) { mutableStateOf("AUTO-FILLED FROM SIGNAL PRO") }
    var selectedSetupPreset by remember(mission.id) { mutableStateOf("REC") }
    var setupCopilotPolicy by remember(mission.id) { mutableStateOf("ASSIST ONLY") }
    var autoCloseTarget by remember(mission.id) { mutableStateOf(true) }
    var autoCloseTp1 by remember(mission.id) { mutableStateOf(true) }
    var autoCloseTp2 by remember(mission.id) { mutableStateOf(false) }
    var autoCloseTp3 by remember(mission.id) { mutableStateOf(false) }
    var autoCloseStopLoss by remember(mission.id) { mutableStateOf(true) }

    fun nullableSetupValue(value: String): String? = value.trim().takeIf { it.isNotBlank() }
    fun setupTargetsText(): String {
        return listOf(setupTp1, setupTp2, setupTp3, setupTarget)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" / ")
            .ifBlank { mission.targets }
    }

    val selectedSetupMode = when (selectedSetupPreset) {
        "SETUP-1" -> "CUSTOM SETUP-1"
        "SETUP-2" -> "CUSTOM SETUP-2"
        else -> "RECOMMENDED SETUP"
    }
    val autoCloseConditions = listOfNotNull(
        "TARGET".takeIf { autoCloseTarget },
        "TP1".takeIf { autoCloseTp1 },
        "TP2".takeIf { autoCloseTp2 },
        "TP3".takeIf { autoCloseTp3 },
        "STOP LOSS".takeIf { autoCloseStopLoss }
    )

    val syncedMission = mission.copy(
        targets = setupTargetsText(),
        stopLoss = setupSl1.ifBlank { mission.stopLoss },
        target = nullableSetupValue(setupTarget),
        tp1 = nullableSetupValue(setupTp1),
        tp2 = nullableSetupValue(setupTp2),
        tp3 = nullableSetupValue(setupTp3),
        manualStopLoss = nullableSetupValue(setupSl1),
        sl2 = nullableSetupValue(setupSl2),
        leverage = nullableSetupValue(setupLeverage),
        positionSize = nullableSetupValue(setupAllocation),
        riskProfile = nullableSetupValue(setupRiskProfile),
        setupRemark = nullableSetupValue(setupRemark),
        setupMode = selectedSetupMode,
        setupStatus = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "READY" else "INCOMPLETE SETUP",
        setupRiskReward = null,
        copilotMode = setupCopilotPolicy,
        executionMode = setupCopilotPolicy,
        autoCloseEnabled = autoCloseConditions.isNotEmpty(),
        autoCloseConditions = autoCloseConditions,
        conditionValidity = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "VALID" else "INVALID",
        conditionInvalidReason = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "Ready for supervised mission handoff." else "Target and SL1 are required before mission activation."
    )

    if (showDecisionBrief) {
        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            sheetState = decisionBriefSheetState,
            containerColor = Color(0xFF030712),
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.86f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TITAN AI ORACLE DECISION BRIEF",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoCyan,
                    maxLines = 1
                )

                Text(
                    text = if (isBengali) "দ্রুত সিদ্ধান্তের জন্য কমপ্যাক্ট সারাংশ" else "Compact signal summary for fast decision-making",
                    fontSize = 10.5.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )

                DecisionBriefBlock(
                    title = if (isBengali) "সিগন্যাল রায়" else "Signal Verdict",
                    value = verdictText,
                    accentColor = if (highConfidence) CryptoGreen else AccentGold
                )

                DecisionBriefBlock(
                    title = if (isBengali) "কেন গুরুত্বপূর্ণ" else "Why It Matters",
                    value = whyText,
                    accentColor = CryptoCyan
                )

                DecisionBriefBlock(
                    title = if (isBengali) "ঝুঁকির সতর্কতা" else "Risk Warning",
                    value = riskText,
                    accentColor = AccentGold
                )

                DecisionBriefBlock(
                    title = if (isBengali) "প্রস্তাবিত কাজ" else "Suggested Action",
                    value = actionText,
                    accentColor = CryptoGreen
                )

                Text(
                    text = disclaimerText,
                    fontSize = 10.sp,
                    color = TextMuted,
                    lineHeight = 13.sp
                )

                PremiumTitanInsightButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showMockupUi = true }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            showDecisionBrief = false
                            step = 2
                        },
                        border = BorderStroke(1.dp, CryptoCyan.copy(alpha = 0.72f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = if (isBengali) "সেটআপ" else "SIGNAL SETUP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = {
                            showDecisionBrief = false
                            step = 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = DarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Text(
                            text = if (isBengali) "সিগন্যাল নিন" else "ACCEPT SIGNAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                OutlinedButton(
                    onClick = { showDecisionBrief = false },
                    border = BorderStroke(0.8.dp, TextSecondary.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(34.dp)
                        .widthIn(min = 104.dp)
                ) {
                    Text(
                        text = if (isBengali) "বন্ধ" else "CLOSE",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (step == 2) {
        ModalBottomSheet(
            onDismissRequest = { step = 0 },
            sheetState = setupSheetState,
            containerColor = Color(0xFF030712),
            contentColor = TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "সিগন্যাল সেটআপ" else "SIGNAL SETUP",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = CryptoCyan,
                            maxLines = 1
                        )
                        Text(
                            text = if (isBengali) "মিশনের জন্য অটো-পিকড কনফিগারেশন" else "Auto-picked mission configuration",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                    SetupStatusBadge(
                        label = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "READY" else "REVIEW",
                        color = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) CryptoGreen else AccentGold
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SetupPresetChip("REC", selectedSetupPreset == "REC", AccentGold, Modifier.weight(1f)) { selectedSetupPreset = "REC" }
                    SetupPresetChip("SETUP-1", selectedSetupPreset == "SETUP-1", CryptoCyan, Modifier.weight(1f)) { selectedSetupPreset = "SETUP-1" }
                    SetupPresetChip("SETUP-2", selectedSetupPreset == "SETUP-2", CryptoCyan, Modifier.weight(1f)) { selectedSetupPreset = "SETUP-2" }
                }

                SetupCompactPanel(title = "PRICE / EXIT MATRIX") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("TARGET", setupTarget, { setupTarget = it }, Modifier.weight(1f))
                        CompactSetupTextField("TP1", setupTp1, { setupTp1 = it }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("TP2", setupTp2, { setupTp2 = it }, Modifier.weight(1f))
                        CompactSetupTextField("TP3", setupTp3, { setupTp3 = it }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("SL1 / STOP LOSS", setupSl1, { setupSl1 = it }, Modifier.weight(1f))
                        CompactSetupTextField("SL2", setupSl2, { setupSl2 = it }, Modifier.weight(1f))
                    }
                }

                SetupCompactPanel(title = "MISSION INTELLIGENCE") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("LEVERAGE", setupLeverage, { setupLeverage = it }, Modifier.weight(1f))
                        CompactSetupTextField("ALLOCATION", setupAllocation, { setupAllocation = it }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text("CONSENSUS BIAS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SetupPresetChip("CONSERVATIVE", setupRiskProfile == "CONSERVATIVE", CryptoCyan, Modifier.weight(1f)) { setupRiskProfile = "CONSERVATIVE" }
                        SetupPresetChip("MODERATE", setupRiskProfile == "MODERATE", CryptoGreen, Modifier.weight(1f)) { setupRiskProfile = "MODERATE" }
                        SetupPresetChip("AGGRESSIVE", setupRiskProfile == "AGGRESSIVE", AccentGold, Modifier.weight(1f)) { setupRiskProfile = "AGGRESSIVE" }
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        SetupPresetChip("ASSIST ONLY", setupCopilotPolicy == "ASSIST ONLY", CryptoCyan, Modifier.weight(1f)) { setupCopilotPolicy = "ASSIST ONLY" }
                        SetupPresetChip("EXECUTION", setupCopilotPolicy == "EXECUTION", AccentGold, Modifier.weight(1f)) { setupCopilotPolicy = "EXECUTION" }
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    CompactSetupTextField("REMARK", setupRemark, { setupRemark = it }, Modifier.fillMaxWidth())
                }

                SetupCompactPanel(title = "AUTO-CLOSE TRADING SETUP") {
                    Text(
                        text = "Select local mission conditions for supervised auto-close preview.",
                        color = TextSecondary,
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            CompactSetupToggleRow("TARGET", autoCloseTarget) { autoCloseTarget = it }
                            CompactSetupToggleRow("TP1", autoCloseTp1) { autoCloseTp1 = it }
                            CompactSetupToggleRow("TP2", autoCloseTp2) { autoCloseTp2 = it }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            CompactSetupToggleRow("TP3", autoCloseTp3) { autoCloseTp3 = it }
                            CompactSetupToggleRow("STOP LOSS", autoCloseStopLoss) { autoCloseStopLoss = it }
                            SetupStatusBadge(if (autoCloseConditions.isEmpty()) "AUTO: OFF" else "AUTO: ${autoCloseConditions.size} RULES", if (autoCloseConditions.isEmpty()) TextMuted else CryptoGreen)
                        }
                    }
                }

                SetupCompactPanel(title = "DEFAULT SELECTIONS") {
                    SetupSummaryLine("Setup Used", selectedSetupMode, CryptoCyan)
                    SetupSummaryLine("TITAN AI Copilot Policy", setupCopilotPolicy, if (setupCopilotPolicy == "EXECUTION") AccentGold else CryptoCyan)
                    SetupSummaryLine("Consensus Bias", setupRiskProfile, signalProfileRiskColor(setupRiskProfile))
                    SetupSummaryLine("Setup Status", syncedMission.setupStatus ?: "REVIEW", if (syncedMission.setupStatus == "READY") CryptoGreen else AccentGold)
                    SetupSummaryLine("Validity", syncedMission.conditionValidity ?: "N/A", if (syncedMission.conditionValidity == "VALID") CryptoGreen else CryptoRedText)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { step = 0 },
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(if (isBengali) "বন্ধ" else "CLOSE", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { step = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(if (isBengali) "সিগন্যাল নিন" else "ACCEPT SIGNAL", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (step == 1) {
        val verifiedEntryLocked = remember { livePrice }

        AlertDialog(
            onDismissRequest = { step = 0 },
            title = {
                Text(
                    text = if (isBengali) "ট্রেড যাচাই করুন" else "Verify Trade Details",
                    color = CryptoCyan,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBengali) "দিক: ${mission.type} (${mission.marketType})" else "Direction: ${mission.type} (${mission.marketType})",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) "লকড এন্ট্রি প্রাইস:" else "Locked Entry Price:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format("$%.4f", verifiedEntryLocked),
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBengali) {
                            "Accept করার পর এই entry personal mission হিসেবে track হবে।"
                        } else {
                            "Once accepted, this entry will activate personal mission tracking."
                        },
                        color = AccentGold,
                        fontSize = 10.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startMission(
                            syncedMission.copy(
                                id = java.util.UUID.randomUUID().toString(),
                                entryPrice = verifiedEntryLocked,
                                originalSignalEntry = if (mission.originalSignalEntry > 0.0) mission.originalSignalEntry else mission.entryPrice,
                                currentPrice = livePrice,
                                startTime = System.currentTimeMillis(),
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                        viewModel.sendLocalAlert("Mission Started", "TITAN AI intelligence system successfully started monitoring ${mission.coinSymbol}")
                        step = 0
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen)
                ) {
                    Text(
                        text = if (isBengali) "মিশন চালু করুন" else "CONFIRM MISSION",
                        fontWeight = FontWeight.Black,
                        color = DarkBackground
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) {
                    Text(
                        text = if (isBengali) "বাতিল" else "Cancel",
                        color = TextSecondary
                    )
                }
            },
            containerColor = Color(0xFF030712),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ButtonScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "AcceptFlowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.84f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val recoSweepX by infiniteTransition.animateFloat(
        initialValue = -650f,
        targetValue = 650f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RecommendationSweepX"
    )

    if (showMockupUi) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showMockupUi = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            SignalProMockupScreen(viewModel = viewModel)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF02050D),
                                Color(0xFF0B1220),
                                Color(0xFF02050D)
                            )
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                CryptoCyan.copy(alpha = 0.10f),
                                CryptoGreen.copy(alpha = 0.08f),
                                CryptoCyan.copy(alpha = 0.10f)
                            )
                        )
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CryptoCyan.copy(alpha = 0.42f),
                                Color.White.copy(alpha = 0.12f),
                                CryptoGreen.copy(alpha = 0.26f),
                                Color.Transparent
                            ),
                            startX = recoSweepX,
                            endX = recoSweepX + 520f
                        )
                    )
                    .border(0.8.dp, CryptoCyan.copy(alpha = 0.66f), RoundedCornerShape(10.dp))
                    .clickable { showDecisionBrief = true }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (highConfidence) "HIGH CONFIDENCE" else "REVIEW CAREFULLY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF4F8FF),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "| VERIFY ENTRY |",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF4F8FF),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Box(
                modifier = Modifier
                    .scale(scale)
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                CryptoCyan.copy(alpha = pulseAlpha),
                                CryptoGreen.copy(alpha = pulseAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(0.8.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = { step = 1 }
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Trade",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "ACCEPT SIGNAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color.White,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}


@Composable
private fun PremiumTitanInsightButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "TitanInsightGlass")
    val sweepX by transition.animateFloat(
        initialValue = -420f,
        targetValue = 620f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TitanInsightSweep"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.58f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TitanInsightGlow"
    )

    Box(
        modifier = modifier
            .height(42.dp)
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF071827),
                        CryptoCyan.copy(alpha = 0.62f),
                        Color(0xFF0A58FF).copy(alpha = 0.76f),
                        Color(0xFF071827)
                    )
                )
            )
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.34f),
                        CryptoGreen.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    startX = sweepX,
                    endX = sweepX + 240f
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.34f), RoundedCornerShape(14.dp))
            .border(1.4.dp, CryptoCyan.copy(alpha = glowAlpha), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(
                text = "TITAN ∆ INSIGHT",
                color = Color.White,
                fontSize = 11.2.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.45.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.86f))
            )
        }
    }
}


@Composable
private fun SetupCompactPanel(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF07101C).copy(alpha = 0.78f))
            .border(0.65.dp, CryptoCyan.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            color = CryptoCyan,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun CompactSetupTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 8.5.sp, maxLines = 1) },
        modifier = modifier.height(52.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            fontFeatureSettings = "tnum"
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CryptoCyan.copy(alpha = 0.78f),
            unfocusedBorderColor = TextMuted.copy(alpha = 0.40f),
            focusedLabelColor = CryptoCyan,
            unfocusedLabelColor = TextMuted,
            cursorColor = CryptoCyan,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
private fun SetupPresetChip(
    label: String,
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(31.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accent.copy(alpha = 0.22f) else Color(0xFF060B14))
            .border(if (selected) 1.dp else 0.7.dp, if (selected) accent else TextMuted.copy(alpha = 0.34f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) accent else TextSecondary,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CompactSetupToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = CryptoGreen,
                uncheckedColor = TextMuted,
                checkmarkColor = DarkBackground
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun SetupSummaryLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 9.5.sp, maxLines = 1)
        Text(value, color = valueColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun SetupStatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.7.dp, color.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 8.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
fun FuturesTradingList(signals: List<FuturesSignal>, timeframeIndex: Int, viewModel: CryptoViewModel) {
    val oraclePick = signals.maxByOrNull { it.opportunityScore }
    val livePrices by viewModel.livePrices.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp, top = 4.dp)
    ) {
        if (oraclePick != null) {
            item {
                OraclePickCard(asset = oraclePick, timeframeIndex = timeframeIndex, viewModel = viewModel, livePrices = livePrices)
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ALL SCANNED FUTURES ASSETS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
        items(signals) { coin ->
            FuturesItemCard(coin, timeframeIndex = timeframeIndex, viewModel = viewModel, livePrices = livePrices)
        }
    }
}
