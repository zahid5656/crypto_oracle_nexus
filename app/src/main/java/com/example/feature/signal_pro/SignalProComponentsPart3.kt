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
        "ট্রেন্ড, মতিগতি, লেনদেন, AI consensus এবং risk profile মিলিয়ে এই setup তৈরি হয়েছে।"
    } else {
        "This setup combines trend, momentum, volume, AI consensus, and risk profile signals."
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
        "এআই সিদ্ধান্তে সহায়তা করে; চূড়ান্ত ট্রেডিং সিদ্ধান্ত আপনার।"
    } else {
        "AI assists decision-making; the final trading decision is yours."
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
    var setupAllocation by remember(mission.id) { mutableStateOf("") }
    var setupRiskProfile by remember(mission.id) { mutableStateOf(if (highConfidence) "MODERATE" else "CONSERVATIVE") }
    var setupRemark by remember(mission.id) { mutableStateOf("AUTO-FILLED FROM SIGNAL PRO") }

    fun nullableSetupValue(value: String): String? = value.trim().takeIf { it.isNotBlank() }
    fun setupTargetsText(): String {
        return listOf(setupTp1, setupTp2, setupTp3, setupTarget)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" / ")
            .ifBlank { mission.targets }
    }

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
        setupMode = if (setupTarget.isNotBlank() || setupTp1.isNotBlank() || setupSl1.isNotBlank()) "RECOMMENDED SETUP" else mission.setupMode,
        setupStatus = if (setupTarget.isNotBlank() && setupSl1.isNotBlank()) "READY" else "INCOMPLETE SETUP",
        setupRiskReward = null,
        copilotMode = "ASSIST ONLY",
        autoCloseEnabled = false,
        autoCloseConditions = emptyList(),
        conditionValidity = "N/A",
        conditionInvalidReason = "Auto-close disabled by user."
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
                    .fillMaxHeight(0.92f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBengali) "AI সিদ্ধান্ত সংক্ষেপ" else "AI Decision Brief",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoCyan
                )

                Text(
                    text = if (isBengali) "দ্রুত সিদ্ধান্ত নেওয়ার জন্য সংক্ষিপ্ত সারাংশ" else "Compact signal summary for faster decision-making",
                    fontSize = 12.5.sp,
                    color = TextSecondary
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
                    fontSize = 11.5.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                showDecisionBrief = false
                                step = 2
                            },
                            border = BorderStroke(1.dp, CryptoCyan.copy(alpha = 0.72f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CryptoCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text(
                                text = if (isBengali) "সেটআপ সিগন্যাল" else "SIGNAL SETUP",
                                fontSize = 11.sp,
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text(
                                text = if (isBengali) "সিগন্যাল নিন" else "ACCEPT SIGNAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    TextButton(
                        onClick = { showDecisionBrief = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isBengali) "বন্ধ করুন" else "CLOSE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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
                    .fillMaxHeight(0.92f)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBengali) "সিগন্যাল সেটআপ" else "SIGNAL SETUP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CryptoCyan
                )
                Text(
                    text = if (isBengali) "বর্তমান সিগন্যাল থেকে Target, TP এবং SL অটো-ফিল করা হয়েছে।" else "Auto-filled from the current signal. Review before accepting.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                OutlinedTextField(value = setupTarget, onValueChange = { setupTarget = it }, label = { Text("TARGET") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupTp1, onValueChange = { setupTp1 = it }, label = { Text("TP1") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupTp2, onValueChange = { setupTp2 = it }, label = { Text("TP2") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupTp3, onValueChange = { setupTp3 = it }, label = { Text("TP3") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupSl1, onValueChange = { setupSl1 = it }, label = { Text("SL1 / STOP LOSS") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupSl2, onValueChange = { setupSl2 = it }, label = { Text("SL2") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupLeverage, onValueChange = { setupLeverage = it }, label = { Text("LEVERAGE") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupAllocation, onValueChange = { setupAllocation = it }, label = { Text("ALLOCATION") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupRiskProfile, onValueChange = { setupRiskProfile = it }, label = { Text("RISK PROFILE") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = setupRemark, onValueChange = { setupRemark = it }, label = { Text("REMARK") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { step = 0 },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBengali) "বন্ধ করুন" else "CLOSE", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { step = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = CryptoGreen, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text(if (isBengali) "সিগন্যাল নিন" else "ACCEPT SIGNAL", fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                        viewModel.sendLocalAlert("Mission Started", "AI intelligence system successfully started monitoring ${mission.coinSymbol}")
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

    var showMockupUi by remember { mutableStateOf(false) }

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
        
        TextButton(
            onClick = { showMockupUi = true },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp).height(24.dp)
        ) {
            Text(
                text = "VIEW SIGNAL PRO MOCKUP",
                color = CryptoCyan.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
