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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType

private fun spLeverageDigitsOnly(value: String?): String = value.orEmpty().filter { it.isDigit() }.take(3)

private fun spLeverageDisplayValue(value: String?): String {
    val digits = spLeverageDigitsOnly(value)
    return if (digits.isBlank()) "" else "${digits}X"
}

private fun spAllocationDigitsOnly(value: String?): String {
    val raw = value.orEmpty().replace(",", ".")
    val cleaned = StringBuilder()
    var decimalUsed = false
    raw.forEach { ch ->
        when {
            ch.isDigit() -> cleaned.append(ch)
            ch == '.' && !decimalUsed -> {
                cleaned.append(ch)
                decimalUsed = true
            }
        }
    }
    return cleaned.toString().take(5)
}

private fun spAllocationDisplayValue(value: String?, fallback: String = "2.0% Cap"): String {
    val source = value.orEmpty().ifBlank { fallback }
    val numeric = spAllocationDigitsOnly(source).trimEnd('.')
    if (numeric.isBlank()) return ""
    val suffix = if (source.contains("MAX", ignoreCase = true) || numeric.toDoubleOrNull()?.let { it >= 10.0 } == true) "Max" else "Cap"
    return "$numeric% $suffix"
}

private fun spRecommendedAllocation(highConfidence: Boolean): String = if (highConfidence) "5.0% Cap" else "2.0% Cap"

private fun spAllocationForConsensusBias(bias: String): String {
    return when (bias.uppercase()) {
        "CONSERVATIVE" -> "2.0% Cap"
        "AGGRESSIVE" -> "10.0% Max"
        else -> "5.0% Cap"
    }
}

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
    // Setup Signal is now rendered as a locked dialog instead of a draggable bottom sheet.
    // This avoids sheet-state deadlock/freezing after rapid policy toggles and a quick VERIFY/ACCEPT tap.
    var fastSetupMutationAllowedAt by remember { mutableStateOf(0L) }
    var missionConfirmLocked by remember { mutableStateOf(false) }
    fun guardedSetupMutation(action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now >= fastSetupMutationAllowedAt) {
            fastSetupMutationAllowedAt = now + 140L
            action()
        }
    }

    // Stable UI session key for per-signal user choices.
    // Do not include volatile values such as mission.id, generated timestamps,
    // current/entry price, targets, or stop-loss here. Those values may be rebuilt
    // by the parent signal card during recomposition and previously forced the
    // sheet back to RECOMMENDED / default toggles after the user selected SETUP-1,
    // SETUP-2, Auto-Trading, Copilot Policy, or condition checks.
    val tradeFlowStateKey = remember(
        mission.coinSymbol,
        mission.type,
        mission.marketType,
        mission.signalTimeframe,
        mission.confidence
    ) {
        listOf(
            mission.coinSymbol.trim().uppercase(),
            mission.type.trim().uppercase(),
            mission.marketType.trim().uppercase(),
            mission.signalTimeframe.trim().uppercase(),
            mission.confidence.toString()
        ).joinToString("|")
    }

    LaunchedEffect(step, tradeFlowStateKey) {
        if (step != 1) {
            missionConfirmLocked = false
        }
    }

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
    val isFuturesMarket = mission.marketType.contains("Futures", ignoreCase = true)
    val setupSourceLabel = remember(tradeFlowStateKey, mission.aiStatusEnglish, mission.aiStatusBengali) {
        if (mission.aiStatusEnglish.contains("Radar", ignoreCase = true) || mission.aiStatusBengali.contains("রাডার")) "LIVE RADAR" else "SIGNAL PRO"
    }
    val setupDirectionCode = mission.type.trim().uppercase().ifBlank { "LONG" }
    val setupMarketCode = if (isFuturesMarket) "F" else "S"
    val setupInstrumentContext = remember(tradeFlowStateKey, isFuturesMarket) {
        val symbol = mission.coinSymbol.trim().uppercase().ifBlank { "ASSET" }
        val timeframe = mission.signalTimeframe.trim().uppercase().ifBlank { "TF" }
        "$symbol | $setupMarketCode-$setupDirectionCode | $timeframe"
    }
    fun recommendedLeverageForMarket(): String = if (isFuturesMarket) "2X" else "1X"
    val defaultLeverage = recommendedLeverageForMarket()

    var setupTarget by rememberSaveable(tradeFlowStateKey) { mutableStateOf(defaultTarget) }
    var setupTp1 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(defaultTp1) }
    var setupTp2 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(defaultTp2) }
    var setupTp3 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(defaultTp3) }
    var setupSl1 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(defaultStopLoss) }
    var setupSl2 by rememberSaveable(tradeFlowStateKey) { mutableStateOf("") }
    var setupLeverage by rememberSaveable(tradeFlowStateKey) { mutableStateOf(defaultLeverage) }
    var setupAllocation by rememberSaveable(tradeFlowStateKey) { mutableStateOf(spRecommendedAllocation(highConfidence)) }
    var setupRiskProfile by rememberSaveable(tradeFlowStateKey) { mutableStateOf(if (highConfidence) "MODERATE" else "CONSERVATIVE") }
    var setupRemark by rememberSaveable(tradeFlowStateKey) { mutableStateOf("AUTO-FILLED FROM SIGNAL PRO") }
    var selectedSetupPreset by rememberSaveable(tradeFlowStateKey) { mutableStateOf("RECOMMENDED") }
    var setupEditedManually by rememberSaveable(tradeFlowStateKey) { mutableStateOf(false) }
    var setupCopilotPolicy by rememberSaveable(tradeFlowStateKey) { mutableStateOf("ASSIST ONLY") }
    var autoCloseTarget by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var autoCloseTp1 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var autoCloseTp2 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(false) }
    var autoCloseTp3 by rememberSaveable(tradeFlowStateKey) { mutableStateOf(false) }
    var autoCloseStopLoss by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var acceptAutoTradingEnabled by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var copilotRiskExtreme by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var copilotExecutionPoor by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var copilotLiquidityGuard by rememberSaveable(tradeFlowStateKey) { mutableStateOf(true) }
    var copilotRegimeFlip by rememberSaveable(tradeFlowStateKey) { mutableStateOf(false) }
    var copilotVolatilitySpike by rememberSaveable(tradeFlowStateKey) { mutableStateOf(false) }
    var acceptMarginMode by rememberSaveable(tradeFlowStateKey) { mutableStateOf(if (isFuturesMarket) "ISOLATED" else "SPOT WALLET") }

    fun nullableSetupValue(value: String): String? = value.trim().takeIf { it.isNotBlank() }
    fun setupTargetsText(): String {
        return listOf(setupTp1, setupTp2, setupTp3, setupTarget)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" / ")
            .ifBlank { mission.targets }
    }

    fun applyConsensusBias(bias: String) {
        setupRiskProfile = bias.uppercase()
        setupAllocation = spAllocationForConsensusBias(bias)
    }

    fun applySetupPreset(preset: String) {
        selectedSetupPreset = preset
        setupEditedManually = false
        when (preset) {
            "SETUP-1" -> {
                applyConsensusBias("MODERATE")
                setupCopilotPolicy = "EXECUTION"
                acceptAutoTradingEnabled = true
                autoCloseTarget = true
                autoCloseTp1 = true
                autoCloseTp2 = true
                autoCloseTp3 = false
                autoCloseStopLoss = true
                if (isFuturesMarket) {
                    setupLeverage = "5X"
                    acceptMarginMode = "ISOLATED"
                } else {
                    setupLeverage = "1X"
                }
                copilotRiskExtreme = true
                copilotExecutionPoor = true
                copilotLiquidityGuard = true
                copilotRegimeFlip = false
                copilotVolatilitySpike = false
                setupRemark = "SETUP-1 | $setupSourceLabel AUTO-PICKED | MODERATE+AUTO-TRADE+COPILOT EXECUTION"
            }
            "SETUP-2" -> {
                applyConsensusBias("AGGRESSIVE")
                setupCopilotPolicy = "EXECUTION"
                acceptAutoTradingEnabled = true
                autoCloseTarget = true
                autoCloseTp1 = true
                autoCloseTp2 = true
                autoCloseTp3 = true
                autoCloseStopLoss = true
                if (isFuturesMarket) {
                    setupLeverage = "3X"
                    acceptMarginMode = "ISOLATED"
                } else {
                    setupLeverage = "1X"
                }
                copilotRiskExtreme = true
                copilotExecutionPoor = true
                copilotLiquidityGuard = true
                copilotRegimeFlip = true
                copilotVolatilitySpike = true
                setupRemark = "SETUP-2 | $setupSourceLabel AUTO-PICKED | AGGRESSIVE+AUTO-TRADE+COPILOT EXECUTION"
            }
            else -> {
                applyConsensusBias(if (highConfidence) "MODERATE" else "CONSERVATIVE")
                setupCopilotPolicy = if (highConfidence) "EXECUTION" else "ASSIST ONLY"
                acceptAutoTradingEnabled = true
                autoCloseTarget = true
                autoCloseTp1 = true
                autoCloseTp2 = false
                autoCloseTp3 = false
                autoCloseStopLoss = true
                setupLeverage = recommendedLeverageForMarket()
                acceptMarginMode = if (isFuturesMarket) "ISOLATED" else "SPOT WALLET"
                copilotRiskExtreme = true
                copilotExecutionPoor = true
                copilotLiquidityGuard = true
                copilotRegimeFlip = false
                copilotVolatilitySpike = false
                setupRemark = "RECOMMENDED | $setupSourceLabel AUTO-SETUP"
            }
        }
    }

    fun copilotProtectionConditions(): List<String> {
        if (setupCopilotPolicy != "EXECUTION") return emptyList()
        return listOfNotNull(
            "RISK EXTREME".takeIf { copilotRiskExtreme },
            "EXECUTION POOR".takeIf { copilotExecutionPoor },
            "LIQUIDITY GUARD".takeIf { copilotLiquidityGuard },
            "REGIME FLIP".takeIf { copilotRegimeFlip },
            "VOLATILITY SPIKE".takeIf { copilotVolatilitySpike }
        )
    }

    fun setupRemarkWithContext(): String {
        val setupName = when (selectedSetupPreset) {
            "SETUP-1" -> "SETUP-1"
            "SETUP-2" -> "SETUP-2"
            else -> "RECOMMENDED"
        }
        val sourceMode = when {
            selectedSetupPreset == "RECOMMENDED" && !setupEditedManually -> "$setupSourceLabel AUTO-SETUP"
            setupEditedManually -> "$setupSourceLabel MANUAL"
            else -> "$setupSourceLabel AUTO-PICKED"
        }
        val autoTradeActive = acceptAutoTradingEnabled && (autoCloseTarget || autoCloseTp1 || autoCloseTp2 || autoCloseTp3 || autoCloseStopLoss)
        val dynamicTokens = listOfNotNull(
            setupRiskProfile.uppercase(),
            "AUTO-TRADE".takeIf { autoTradeActive },
            if (setupCopilotPolicy == "EXECUTION") "COPILOT EXECUTION" else "COPILOT ASSIST ONLY"
        ).joinToString("+")
        val includeDynamicTokens = selectedSetupPreset != "RECOMMENDED" || setupEditedManually
        return listOfNotNull(
            setupName,
            sourceMode,
            dynamicTokens.takeIf { includeDynamicTokens && it.isNotBlank() }
        ).joinToString(" | ")
    }

    val selectedSetupMode = when (selectedSetupPreset) {
        "SETUP-1" -> "SETUP-1"
        "SETUP-2" -> "SETUP-2"
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
        leverage = nullableSetupValue(spLeverageDisplayValue(setupLeverage)),
        positionSize = nullableSetupValue(spAllocationDisplayValue(setupAllocation, spRecommendedAllocation(highConfidence))),
        riskProfile = nullableSetupValue(setupRiskProfile),
        setupRemark = nullableSetupValue(setupRemarkWithContext()),
        setupMode = selectedSetupMode,
        setupStatus = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "READY" else "INCOMPLETE SETUP",
        setupRiskReward = null,
        copilotMode = setupCopilotPolicy,
        executionMode = setupCopilotPolicy,
        autoCloseEnabled = acceptAutoTradingEnabled && autoCloseConditions.isNotEmpty(),
        autoCloseConditions = if (acceptAutoTradingEnabled) autoCloseConditions else emptyList(),
        conditionValidity = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "VALID" else "INVALID",
        conditionInvalidReason = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "Ready for supervised mission handoff." else "Target and SL1 are required before mission activation."
    )

    fun startAcceptedMission(entryPrice: Double = livePrice) {
        viewModel.startMission(
            syncedMission.copy(
                id = java.util.UUID.randomUUID().toString(),
                entryPrice = entryPrice,
                originalSignalEntry = if (mission.originalSignalEntry > 0.0) mission.originalSignalEntry else mission.entryPrice,
                currentPrice = livePrice,
                startTime = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
        viewModel.sendLocalAlert("Mission Started", "TITAN AI intelligence system successfully started monitoring ${mission.coinSymbol}")
        showDecisionBrief = false
        missionConfirmLocked = false
        step = 0
    }
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

                Spacer(modifier = Modifier.height(4.dp))

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
                            step = 2
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
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* Locked setup cockpit: only the visible CLOSE button dismisses it. */ },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(Color(0xFF030712))
                    .border(0.9.dp, CryptoCyan.copy(alpha = 0.30f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    SetupPresetChip("RECOMMENDED", selectedSetupPreset == "RECOMMENDED", CryptoGreen, Modifier.weight(1f)) { guardedSetupMutation { applySetupPreset("RECOMMENDED") } }
                    SetupPresetChip("SETUP-1", selectedSetupPreset == "SETUP-1", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { applySetupPreset("SETUP-1") } }
                    SetupPresetChip("SETUP-2", selectedSetupPreset == "SETUP-2", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { applySetupPreset("SETUP-2") } }
                }

                val recommendedCoreLocked = selectedSetupPreset == "RECOMMENDED"
                val coreEditEnabled = !recommendedCoreLocked

                SetupCompactPanel(title = "PRICE / EXIT MATRIX", context = setupInstrumentContext) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("TARGET", setupTarget, { setupEditedManually = true; setupTarget = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                        CompactSetupTextField("TP1", setupTp1, { setupEditedManually = true; setupTp1 = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("TP2", setupTp2, { setupEditedManually = true; setupTp2 = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                        CompactSetupTextField("TP3", setupTp3, { setupEditedManually = true; setupTp3 = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactSetupTextField("SL1 / STOP LOSS", setupSl1, { setupEditedManually = true; setupSl1 = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                        CompactSetupTextField("SL2", setupSl2, { setupEditedManually = true; setupSl2 = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                    }
                }

                SetupCompactPanel(title = "MISSION INTELLIGENCE") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CompactLeverageTextField("LEVERAGE", setupLeverage, { setupEditedManually = true; setupLeverage = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                        CompactAllocationTextField("ALLOCATION", setupAllocation, { setupEditedManually = true; setupAllocation = it }, Modifier.weight(1f), enabled = coreEditEnabled)
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text("CONSENSUS BIAS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SetupPresetChip("CONSERVATIVE", setupRiskProfile == "CONSERVATIVE", CryptoCyan, Modifier.weight(1f), enabled = coreEditEnabled) { setupEditedManually = true; applyConsensusBias("CONSERVATIVE") }
                        SetupPresetChip("MODERATE", setupRiskProfile == "MODERATE", CryptoGreen, Modifier.weight(1f), enabled = coreEditEnabled) { setupEditedManually = true; applyConsensusBias("MODERATE") }
                        SetupPresetChip("AGGRESSIVE", setupRiskProfile == "AGGRESSIVE", AccentGold, Modifier.weight(1f), enabled = coreEditEnabled) { setupEditedManually = true; applyConsensusBias("AGGRESSIVE") }
                    }
                    if (recommendedCoreLocked) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Text("RECOMMENDED core values are locked here. Use Mission Center Override to create CUSTOM-1.", color = TextMuted, fontSize = 8.4.sp, lineHeight = 10.2.sp)
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text("REMARK", color = TextMuted, fontSize = 8.6.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Text(
                        text = setupRemarkWithContext(),
                        color = CryptoCyan,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF050A13))
                            .border(0.55.dp, CryptoCyan.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                SetupCompactPanel(title = "AUTOMATION / COPILOT MODE") {
                    Text("Only these controls are changeable in RECOMMENDED mode.", color = TextSecondary, fontSize = 8.8.sp, lineHeight = 10.5.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text("AUTO-TRADING", color = TextMuted, fontSize = 8.8.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SetupPresetChip("AUTO-TRADING OFF", !acceptAutoTradingEnabled, TextMuted, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; acceptAutoTradingEnabled = false } }
                        SetupPresetChip("AUTO-TRADING ON", acceptAutoTradingEnabled, CryptoGreen, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; acceptAutoTradingEnabled = true } }
                    }
                    Spacer(modifier = Modifier.height(7.dp))
                    Text("TITAN AI COPILOT POLICY", color = TextMuted, fontSize = 8.8.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SetupPresetChip("ASSIST ONLY", setupCopilotPolicy == "ASSIST ONLY", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; setupCopilotPolicy = "ASSIST ONLY" } }
                        SetupPresetChip("COPILOT EXECUTION", setupCopilotPolicy == "EXECUTION", AccentGold, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; setupCopilotPolicy = "EXECUTION" } }
                    }
                }

                DualConditionControlTile(
                    autoCloseTarget = autoCloseTarget,
                    autoCloseTp1 = autoCloseTp1,
                    autoCloseTp2 = autoCloseTp2,
                    autoCloseTp3 = autoCloseTp3,
                    autoCloseStopLoss = autoCloseStopLoss,
                    acceptAutoTradingEnabled = acceptAutoTradingEnabled,
                    copilotEnabled = setupCopilotPolicy == "EXECUTION",
                    copilotRiskExtreme = copilotRiskExtreme,
                    copilotExecutionPoor = copilotExecutionPoor,
                    copilotLiquidityGuard = copilotLiquidityGuard,
                    copilotRegimeFlip = copilotRegimeFlip,
                    copilotVolatilitySpike = copilotVolatilitySpike,
                    onAutoTargetChange = { guardedSetupMutation { setupEditedManually = true; autoCloseTarget = it } },
                    onAutoTp1Change = { guardedSetupMutation { setupEditedManually = true; autoCloseTp1 = it } },
                    onAutoTp2Change = { guardedSetupMutation { setupEditedManually = true; autoCloseTp2 = it } },
                    onAutoTp3Change = { guardedSetupMutation { setupEditedManually = true; autoCloseTp3 = it } },
                    onAutoStopLossChange = { guardedSetupMutation { setupEditedManually = true; autoCloseStopLoss = it } },
                    onCopilotRiskExtremeChange = { guardedSetupMutation { setupEditedManually = true; copilotRiskExtreme = it } },
                    onCopilotExecutionPoorChange = { guardedSetupMutation { setupEditedManually = true; copilotExecutionPoor = it } },
                    onCopilotLiquidityGuardChange = { guardedSetupMutation { setupEditedManually = true; copilotLiquidityGuard = it } },
                    onCopilotRegimeFlipChange = { guardedSetupMutation { setupEditedManually = true; copilotRegimeFlip = it } },
                    onCopilotVolatilitySpikeChange = { guardedSetupMutation { setupEditedManually = true; copilotVolatilitySpike = it } }
                )

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
                    Box(
                        modifier = Modifier
                            .weight(0.85f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF07101A))
                            .border(0.8.dp, TextSecondary.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .clickable { step = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isBengali) "বন্ধ" else "CLOSE", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.35f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(CryptoCyan.copy(alpha = 0.85f), CryptoGreen)
                                )
                            )
                            .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                            .clickable { guardedSetupMutation { missionConfirmLocked = false; step = 1 } },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Accept Signal",
                                tint = DarkBackground,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBengali) "সিগন্যাল নিন" else "ACCEPT SIGNAL", color = DarkBackground, fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    if (step == 1) {
        val verifiedEntryLocked = remember { livePrice }
        val confirmScroll = rememberScrollState()
        val autoTradingDisplay = if (acceptAutoTradingEnabled && autoCloseConditions.isNotEmpty()) {
            autoCloseConditions.joinToString(" / ")
        } else {
            "INACTIVE"
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { step = 0 },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 680.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF07101D),
                                Color(0xFF11152A),
                                Color(0xFF050914)
                            )
                        )
                    )
                    .border(0.9.dp, CryptoCyan.copy(alpha = 0.36f), RoundedCornerShape(28.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "মিশন অ্যাক্টিভেশন" else "MISSION ACTIVATION",
                            color = CryptoCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Text(
                            text = if (isBengali) "Confirm করার আগে setup, policy এবং risk controls যাচাই করুন।" else "Review setup, policy, and risk controls before adding this to Mission Center.",
                            color = TextSecondary,
                            fontSize = 9.5.sp,
                            lineHeight = 12.sp
                        )
                    }
                    SetupStatusBadge("READY", CryptoGreen)
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(confirmScroll),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SetupCompactPanel(title = "ONE-TAP QUICK SELECTION", context = setupInstrumentContext) {
                        Text(
                            text = "RECOMMENDED is the default setup. Price, TP, SL, leverage, and allocation remain display-only here; use SIGNAL SETUP for full editing.",
                            color = TextSecondary,
                            fontSize = 8.6.sp,
                            lineHeight = 10.4.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SetupPresetChip("RECOMMENDED", selectedSetupPreset == "RECOMMENDED", CryptoGreen, Modifier.weight(1f)) { guardedSetupMutation { applySetupPreset("RECOMMENDED") } }
                            SetupPresetChip("SETUP-1", selectedSetupPreset == "SETUP-1", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { applySetupPreset("SETUP-1") } }
                            SetupPresetChip("SETUP-2", selectedSetupPreset == "SETUP-2", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { applySetupPreset("SETUP-2") } }
                        }
                    }

                    SetupCompactPanel(title = if (isFuturesMarket) "FUTURES EXECUTION CONTEXT" else "SPOT EXECUTION CONTEXT") {
                        if (isFuturesMarket) {
                            SetupSummaryLine("Leverage", setupLeverage.ifBlank { recommendedLeverageForMarket() }, AccentGold)
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SetupPresetChip("CROSS", acceptMarginMode == "CROSS", AccentGold, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; acceptMarginMode = "CROSS" } }
                                SetupPresetChip("ISOLATED", acceptMarginMode == "ISOLATED", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; acceptMarginMode = "ISOLATED" } }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            SetupSummaryLine("Liquidity Guard", "ACTIVE", CryptoGreen)
                        } else {
                            SetupSummaryLine("Execution Mode", "SPOT WALLET", CryptoCyan)
                            SetupSummaryLine("Margin", "NOT APPLICABLE", TextMuted)
                            SetupSummaryLine("Liquidity Guard", "ACTIVE", CryptoGreen)
                        }
                    }

                    SetupCompactPanel(title = "MISSION HANDOFF SUMMARY") {
                        SetupSummaryLine("Destination", "MISSION CENTER", CryptoCyan)
                        SetupSummaryLine("Direction / Market", "${mission.type.uppercase()} / ${mission.marketType.uppercase()}", if (mission.type.equals("SHORT", true)) CryptoRedText else CryptoGreen)
                        SetupSummaryLine("Mode", if (isFuturesMarket) "$acceptMarginMode / ${setupLeverage.ifBlank { recommendedLeverageForMarket() }}" else "SPOT / NO MARGIN", if (isFuturesMarket) AccentGold else CryptoCyan)
                        if (isFuturesMarket) {
                            SetupSummaryLine("Leverage", setupLeverage.ifBlank { recommendedLeverageForMarket() }, AccentGold)
                        }
                        SetupSummaryLine("Locked Entry", String.format("$%.4f", verifiedEntryLocked), TextPrimary)
                        SetupSummaryLine("Target", setupTarget.ifBlank { mission.target ?: mission.targets }, CryptoGreen)
                        SetupSummaryLine("Stop Loss", setupSl1.ifBlank { mission.stopLoss }, CryptoRedText)
                        SetupSummaryLine("Allocation", spAllocationDisplayValue(setupAllocation, spRecommendedAllocation(highConfidence)), CryptoCyan)
                        SetupSummaryLine("Consensus Bias", setupRiskProfile, signalProfileRiskColor(setupRiskProfile))
                        SetupSummaryLine("Remark", setupRemarkWithContext(), CryptoCyan)
                    }

                    SetupCompactPanel(title = "EXIT / PROTECTION MAP") {
                        SetupSummaryLine("TP1", setupTp1.ifBlank { "NOT SET" }, if (setupTp1.isBlank()) TextMuted else CryptoGreen)
                        SetupSummaryLine("TP2", setupTp2.ifBlank { "NOT SET" }, if (setupTp2.isBlank()) TextMuted else CryptoGreen)
                        SetupSummaryLine("TP3", setupTp3.ifBlank { "NOT SET" }, if (setupTp3.isBlank()) TextMuted else CryptoGreen)
                        SetupSummaryLine("SL1", setupSl1.ifBlank { "NOT SET" }, if (setupSl1.isBlank()) TextMuted else CryptoRedText)
                        SetupSummaryLine("Auto-Trading Rules", autoTradingDisplay, if (acceptAutoTradingEnabled) CryptoGreen else TextMuted)
                    }

                    SetupCompactPanel(title = "TITAN AI CONTROL POLICY") {
                        SetupSummaryLine("Setup Used", selectedSetupMode, CryptoCyan)
                        SetupSummaryLine("TITAN AI Copilot", if (setupCopilotPolicy == "EXECUTION") "COPILOT EXECUTION" else "ASSIST ONLY", if (setupCopilotPolicy == "EXECUTION") AccentGold else CryptoCyan)
                        SetupSummaryLine("Real Market Order", "NO", CryptoRedText)
                        SetupSummaryLine("TITAN AI Auto Pilot", "NOT USED IN THIS ACCEPT FLOW", TextMuted)
                        SetupSummaryLine("Mission Status", "SUPERVISED TRACKING", CryptoGreen)
                        Text(
                            text = "Confirming will add this signal to Mission Center for monitored supervision. Final trading authority remains with the user.",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            lineHeight = 11.sp
                        )
                    }

                    SetupCompactPanel(title = "AUTOMATION / COPILOT QUICK CONTROLS") {
                        Text("Only quick controls and condition checks are switchable here. Core setup values are display-only.", color = TextSecondary, fontSize = 8.6.sp, lineHeight = 10.4.sp)
                        Spacer(modifier = Modifier.height(5.dp))
                        Text("AUTO-TRADING", color = TextMuted, fontSize = 8.8.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SetupPresetChip("AUTO-TRADING OFF", !acceptAutoTradingEnabled, TextMuted, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; acceptAutoTradingEnabled = false } }
                            SetupPresetChip("AUTO-TRADING ON", acceptAutoTradingEnabled, CryptoGreen, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; acceptAutoTradingEnabled = true } }
                        }
                        Spacer(modifier = Modifier.height(7.dp))
                        Text("TITAN AI COPILOT POLICY", color = TextMuted, fontSize = 8.8.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SetupPresetChip("ASSIST ONLY", setupCopilotPolicy == "ASSIST ONLY", CryptoCyan, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; setupCopilotPolicy = "ASSIST ONLY" } }
                            SetupPresetChip("COPILOT EXECUTION", setupCopilotPolicy == "EXECUTION", AccentGold, Modifier.weight(1f)) { guardedSetupMutation { setupEditedManually = true; setupCopilotPolicy = "EXECUTION" } }
                        }
                    }

                    DualConditionControlTile(
                        autoCloseTarget = autoCloseTarget,
                        autoCloseTp1 = autoCloseTp1,
                        autoCloseTp2 = autoCloseTp2,
                        autoCloseTp3 = autoCloseTp3,
                        autoCloseStopLoss = autoCloseStopLoss,
                        acceptAutoTradingEnabled = acceptAutoTradingEnabled,
                        copilotEnabled = setupCopilotPolicy == "EXECUTION",
                        copilotRiskExtreme = copilotRiskExtreme,
                        copilotExecutionPoor = copilotExecutionPoor,
                        copilotLiquidityGuard = copilotLiquidityGuard,
                        copilotRegimeFlip = copilotRegimeFlip,
                        copilotVolatilitySpike = copilotVolatilitySpike,
                        onAutoTargetChange = { guardedSetupMutation { setupEditedManually = true; autoCloseTarget = it } },
                        onAutoTp1Change = { guardedSetupMutation { setupEditedManually = true; autoCloseTp1 = it } },
                        onAutoTp2Change = { guardedSetupMutation { setupEditedManually = true; autoCloseTp2 = it } },
                        onAutoTp3Change = { guardedSetupMutation { setupEditedManually = true; autoCloseTp3 = it } },
                        onAutoStopLossChange = { guardedSetupMutation { setupEditedManually = true; autoCloseStopLoss = it } },
                        onCopilotRiskExtremeChange = { guardedSetupMutation { setupEditedManually = true; copilotRiskExtreme = it } },
                        onCopilotExecutionPoorChange = { guardedSetupMutation { setupEditedManually = true; copilotExecutionPoor = it } },
                        onCopilotLiquidityGuardChange = { guardedSetupMutation { setupEditedManually = true; copilotLiquidityGuard = it } },
                        onCopilotRegimeFlipChange = { guardedSetupMutation { setupEditedManually = true; copilotRegimeFlip = it } },
                        onCopilotVolatilitySpikeChange = { guardedSetupMutation { setupEditedManually = true; copilotVolatilitySpike = it } }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF07101A))
                            .border(0.8.dp, TextSecondary.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                            .clickable { step = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isBengali) "বাতিল" else "CANCEL", color = TextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1.35f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(CryptoCyan.copy(alpha = 0.85f), CryptoGreen)
                                )
                            )
                            .border(0.8.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                            .clickable(enabled = !missionConfirmLocked) {
                                if (missionConfirmLocked) return@clickable
                                missionConfirmLocked = true
                                startAcceptedMission(verifiedEntryLocked)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Confirm Mission",
                                tint = DarkBackground,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "মিশন কনফার্ম" else "CONFIRM MISSION",
                                fontWeight = FontWeight.Black,
                                color = DarkBackground,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
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
            SignalProMockupScreen(
                viewModel = viewModel,
                mission = syncedMission,
                livePrice = livePrice,
                onBackOverride = { showMockupUi = false },
                onSignalSetup = {
                    showMockupUi = false
                    step = 2
                },
                onAcceptSignal = {
                    showMockupUi = false
                    if (step == 0) step = 1
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PremiumTitanInsightButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showMockupUi = true }
        )

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
                    .clickable { if (step == 0 && !showDecisionBrief) showDecisionBrief = true }
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
                        onClick = { if (step == 0) step = 1 }
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
    context: String? = null,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = CryptoCyan,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (!context.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context,
                    color = TextMuted,
                    fontSize = 7.4.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.82f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}


@Composable
private fun CompactLeverageTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }
    val displayValue = if (focused) spLeverageDigitsOnly(value) else spLeverageDisplayValue(value)
    OutlinedTextField(
        value = displayValue,
        onValueChange = { if (enabled) onValueChange(spLeverageDisplayValue(it)) },
        label = { Text(label, fontSize = 8.5.sp, maxLines = 1) },
        modifier = modifier
            .height(52.dp)
            .onFocusChanged { focused = it.isFocused },
        singleLine = true,
        readOnly = !enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextPrimary,
            disabledBorderColor = TextMuted.copy(alpha = 0.24f),
            disabledLabelColor = TextMuted.copy(alpha = 0.62f)
        )
    )
}

@Composable
private fun CompactAllocationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }
    val displayValue = if (focused) spAllocationDigitsOnly(value) else spAllocationDisplayValue(value)
    OutlinedTextField(
        value = displayValue,
        onValueChange = { if (enabled) onValueChange(spAllocationDigitsOnly(it)) },
        label = { Text(label, fontSize = 8.5.sp, maxLines = 1) },
        modifier = modifier
            .height(52.dp)
            .onFocusChanged { focused = it.isFocused },
        singleLine = true,
        readOnly = !enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextPrimary,
            disabledBorderColor = TextMuted.copy(alpha = 0.24f),
            disabledLabelColor = TextMuted.copy(alpha = 0.62f)
        )
    )
}

@Composable
private fun CompactSetupTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (enabled) onValueChange(it) },
        label = { Text(label, fontSize = 8.5.sp, maxLines = 1) },
        modifier = modifier.height(52.dp),
        singleLine = true,
        readOnly = !enabled,
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val borderColor = when {
        selected -> accent
        enabled -> TextMuted.copy(alpha = 0.34f)
        else -> TextMuted.copy(alpha = 0.18f)
    }
    val textColor = when {
        selected -> accent
        enabled -> TextSecondary
        else -> TextMuted.copy(alpha = 0.62f)
    }
    Box(
        modifier = modifier
            .height(31.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accent.copy(alpha = if (enabled) 0.22f else 0.10f) else Color(0xFF060B14))
            .border(if (selected) 1.dp else 0.7.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = if (label.length > 9) 7.4.sp else 8.5.sp,
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
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            modifier = Modifier.size(24.dp),
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = CryptoGreen,
                uncheckedColor = TextMuted,
                checkmarkColor = DarkBackground,
                disabledCheckedColor = CryptoGreen.copy(alpha = 0.28f),
                disabledUncheckedColor = TextMuted.copy(alpha = 0.28f)
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = if (enabled) TextPrimary else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}


@Composable
private fun DualConditionControlTile(
    autoCloseTarget: Boolean,
    autoCloseTp1: Boolean,
    autoCloseTp2: Boolean,
    autoCloseTp3: Boolean,
    autoCloseStopLoss: Boolean,
    acceptAutoTradingEnabled: Boolean,
    copilotEnabled: Boolean,
    copilotRiskExtreme: Boolean,
    copilotExecutionPoor: Boolean,
    copilotLiquidityGuard: Boolean,
    copilotRegimeFlip: Boolean,
    copilotVolatilitySpike: Boolean,
    onAutoTargetChange: (Boolean) -> Unit,
    onAutoTp1Change: (Boolean) -> Unit,
    onAutoTp2Change: (Boolean) -> Unit,
    onAutoTp3Change: (Boolean) -> Unit,
    onAutoStopLossChange: (Boolean) -> Unit,
    onCopilotRiskExtremeChange: (Boolean) -> Unit,
    onCopilotExecutionPoorChange: (Boolean) -> Unit,
    onCopilotLiquidityGuardChange: (Boolean) -> Unit,
    onCopilotRegimeFlipChange: (Boolean) -> Unit,
    onCopilotVolatilitySpikeChange: (Boolean) -> Unit
) {
    val autoCount = listOf(autoCloseTarget, autoCloseTp1, autoCloseTp2, autoCloseTp3, autoCloseStopLoss).count { it }
    val copilotCount = if (copilotEnabled) listOf(copilotRiskExtreme, copilotExecutionPoor, copilotLiquidityGuard, copilotRegimeFlip, copilotVolatilitySpike).count { it } else 0
    SetupCompactPanel(title = "MISSION CONDITION CONTROL") {
        Text(
            text = "Auto-Trading follows user rules. Copilot Execution supervises protection. Mission Center later arbitrates duplicate or conflicting actions.",
            color = TextSecondary,
            fontSize = 8.4.sp,
            lineHeight = 10.2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("AUTO-TRADING CONDITIONS", color = CryptoCyan, fontSize = 8.2.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text("Rule-based auto-close preview", color = TextMuted, fontSize = 7.8.sp, maxLines = 1)
                CompactSetupToggleRow("TARGET", autoCloseTarget, onAutoTargetChange, enabled = acceptAutoTradingEnabled)
                CompactSetupToggleRow("TP1", autoCloseTp1, onAutoTp1Change, enabled = acceptAutoTradingEnabled)
                CompactSetupToggleRow("TP2", autoCloseTp2, onAutoTp2Change, enabled = acceptAutoTradingEnabled)
                CompactSetupToggleRow("TP3", autoCloseTp3, onAutoTp3Change, enabled = acceptAutoTradingEnabled)
                CompactSetupToggleRow("STOP LOSS", autoCloseStopLoss, onAutoStopLossChange, enabled = acceptAutoTradingEnabled)
                SetupStatusBadge(if (!acceptAutoTradingEnabled) "AUTO OFF" else "AUTO: $autoCount RULES", if (!acceptAutoTradingEnabled) TextMuted else CryptoGreen)
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(154.dp)
                    .background(CryptoCyan.copy(alpha = 0.36f))
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("EXECUTION CONDITIONS", color = if (copilotEnabled) AccentGold else TextMuted, fontSize = 8.2.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(if (copilotEnabled) "AI protection triggers" else "Disabled in Assist Only", color = TextMuted, fontSize = 7.8.sp, maxLines = 1)
                CompactSetupToggleRow("RISK EXTREME", copilotRiskExtreme, onCopilotRiskExtremeChange, enabled = copilotEnabled)
                CompactSetupToggleRow("EXECUTION POOR", copilotExecutionPoor, onCopilotExecutionPoorChange, enabled = copilotEnabled)
                CompactSetupToggleRow("LIQUIDITY GUARD", copilotLiquidityGuard, onCopilotLiquidityGuardChange, enabled = copilotEnabled)
                CompactSetupToggleRow("REGIME FLIP", copilotRegimeFlip, onCopilotRegimeFlipChange, enabled = copilotEnabled)
                CompactSetupToggleRow("VOLATILITY SPIKE", copilotVolatilitySpike, onCopilotVolatilitySpikeChange, enabled = copilotEnabled)
                SetupStatusBadge(if (copilotEnabled) "COPILOT: $copilotCount RULES" else "ASSIST ONLY", if (copilotEnabled) AccentGold else TextMuted)
            }
        }
    }
}

@Composable
private fun SetupSummaryLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 9.5.sp, maxLines = 1, modifier = Modifier.weight(0.72f))
        Text(
            value,
            color = valueColor,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.28f)
        )
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
