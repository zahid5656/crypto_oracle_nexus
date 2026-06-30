package com.example.feature.signal_pro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.model.Mission
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel
import kotlin.math.abs

private val T_Surface = Color(0xFF111112)
private val T_Bg = DarkBackground
private val T_BorderMedium = Color(0xFF3A3A3C)
private val T_TextPrimary = TextPrimary
private val T_TextSecondary = TextSecondary
private val T_TextMuted = TextMuted

private val T_Green = CryptoGreen
private val T_Red = CryptoRedText
private val T_Cyan = CryptoCyan
private val T_Gold = TitanGold
private val T_Orange = TitanOrange

data class SignalInsightSnapshot(
    val symbol: String = "BTC",
    val marketMode: String = "FUTURES LONG",
    val direction: String = "LONG",
    val marketType: String = "Futures",
    val timeframe: String = "24H",
    val context: String = "BTC | F-LONG | 24H",
    val entry: String = "\$63,920.00",
    val current: String = "\$63,994.00",
    val expected: String = "\$65,400.00",
    val predicted: String = "\$66,200.00",
    val target: String = "\$66,500.00",
    val stopLoss: String = "\$62,780.00",
    val allocation: String = "5.0% Cap",
    val confidence: Int = 82,
    val cqiScore: Int = 82,
    val classification: String = "MODERATE CONFIDENCE",
    val signalState: String = "ACTIVE",
    val validity: String = "VALIDATED",
    val freshness: String = "LIVE",
    val riskScore: Int = 24,
    val riskLabel: String = "MEDIUM",
    val consensusBias: String = "MODERATE",
    val voteSplit: String = "4-0-1 (1 Outlier)",
    val riskReward: String = "2.4R",
    val slDistance: String = "N/A",
    val directionLogic: String = "VALID",
    val decisionGate: String = "REVIEW",
    val remark: String = "RECOMMENDED | SIGNAL PRO AUTO-SETUP",
    val source: String = "SIGNAL PRO"
)

private fun insightFormatMoney(value: Double): String {
    if (value <= 0.0 || value.isNaN() || value.isInfinite()) return "N/A"
    return when {
        value < 0.01 -> "$" + String.format("%.6f", value)
        value < 1.0 -> "$" + String.format("%.4f", value)
        else -> "$" + String.format("%,.2f", value)
    }
}

private fun insightCleanDisplayPrice(value: String?): String {
    val cleaned = value.orEmpty().trim()
    return cleaned.takeIf { it.isNotBlank() && !it.equals("N/A", true) && !it.equals("NOT SET", true) } ?: "N/A"
}

private fun insightPriceToDouble(value: String?): Double? {
    return value.orEmpty().replace(",", "").filter { it.isDigit() || it == '.' || it == '-' }.toDoubleOrNull()
}

private fun insightTargetsFromMission(mission: Mission?): List<String> {
    if (mission == null) return listOf("\$66,500.00")
    return listOf(mission.tp1, mission.tp2, mission.tp3, mission.target, mission.targets)
        .flatMap { item -> item.orEmpty().split("/") }
        .map { it.trim() }
        .filter { it.isNotBlank() && !it.equals("N/A", true) && !it.equals("NOT SET", true) }
        .distinct()
}

private fun insightClassForConfidence(confidence: Int): String = when {
    confidence >= 90 -> "INSTITUTIONAL GRADE"
    confidence >= 85 -> "HIGH CONFIDENCE"
    confidence >= 75 -> "MODERATE CONFIDENCE"
    confidence >= 65 -> "CAUTION"
    else -> "LOW CONFIDENCE"
}

private fun insightRiskLabel(score: Int): String = when {
    score <= 15 -> "LOW"
    score <= 25 -> "MEDIUM"
    score <= 35 -> "HIGH"
    else -> "EXTREME"
}

private fun insightBiasForMission(mission: Mission?, confidence: Int): String {
    return mission?.riskProfile?.uppercase()?.takeIf { it.isNotBlank() } ?: when {
        confidence >= 88 -> "AGGRESSIVE"
        confidence >= 75 -> "MODERATE"
        else -> "CONSERVATIVE"
    }
}

private fun insightAllocationForBias(bias: String): String = when (bias.uppercase()) {
    "AGGRESSIVE" -> "10.0% Max"
    "CONSERVATIVE" -> "2.0% Cap"
    else -> "5.0% Cap"
}

private fun insightBuildSnapshot(mission: Mission?, livePrice: Double?): SignalInsightSnapshot {
    if (mission == null) return SignalInsightSnapshot()

    val symbol = mission.coinSymbol.trim().uppercase().ifBlank { "ASSET" }
    val direction = mission.type.trim().uppercase().ifBlank { "LONG" }
    val isFutures = mission.marketType.contains("Futures", true)
    val marketType = if (isFutures) "Futures" else "Spot"
    val marketCode = if (isFutures) "F" else "S"
    val timeframe = mission.signalTimeframe.trim().uppercase().ifBlank { "TF" }
    val context = "$symbol | $marketCode-$direction | $timeframe"
    val confidence = mission.confidence.coerceIn(0, 100)
    val targetList = insightTargetsFromMission(mission)
    val target = insightCleanDisplayPrice(targetList.lastOrNull())
    val expected = insightCleanDisplayPrice(targetList.firstOrNull() ?: target)
    val stopLoss = insightCleanDisplayPrice(mission.manualStopLoss ?: mission.stopLoss)
    val currentDouble = livePrice ?: mission.currentPrice.takeIf { it > 0.0 } ?: mission.entryPrice
    val entryDouble = mission.entryPrice
    val riskRaw = (100 - confidence).coerceIn(0, 100)
    val riskScore = riskRaw.coerceAtLeast(12)
    val bias = insightBiasForMission(mission, confidence)
    val allocation = mission.positionSize?.takeIf { it.isNotBlank() } ?: insightAllocationForBias(bias)
    val targetDouble = insightPriceToDouble(target)
    val stopDouble = insightPriceToDouble(stopLoss)
    val riskReward = if (targetDouble != null && stopDouble != null && entryDouble > 0.0 && abs(entryDouble - stopDouble) > 0.0) {
        String.format("%.2fR", abs(targetDouble - entryDouble) / abs(entryDouble - stopDouble))
    } else {
        "N/A"
    }
    val slDistance = if (stopDouble != null && entryDouble > 0.0) {
        String.format("%.2f%%", abs(entryDouble - stopDouble) / entryDouble * 100.0)
    } else {
        "N/A"
    }
    val signalState = mission.setupStatus?.uppercase()?.takeIf { it.isNotBlank() } ?: "ACTIVE"
    val validity = mission.conditionValidity?.uppercase()?.takeIf { it.isNotBlank() } ?: "VALIDATED"
    val source = if (mission.aiStatusEnglish.contains("Radar", true) || mission.aiStatusBengali.contains("রাডার")) "LIVE RADAR" else "SIGNAL PRO"
    val remark = mission.setupRemark?.takeIf { it.isNotBlank() } ?: "RECOMMENDED | $source AUTO-SETUP"

    return SignalInsightSnapshot(
        symbol = symbol,
        marketMode = "${marketType.uppercase()} $direction",
        direction = direction,
        marketType = marketType,
        timeframe = timeframe,
        context = context,
        entry = insightFormatMoney(entryDouble),
        current = insightFormatMoney(currentDouble),
        expected = expected,
        predicted = target,
        target = target,
        stopLoss = stopLoss,
        allocation = allocation,
        confidence = confidence,
        cqiScore = confidence,
        classification = insightClassForConfidence(confidence),
        signalState = signalState,
        validity = validity,
        freshness = if (livePrice != null) "LIVE" else "SIGNAL",
        riskScore = riskScore,
        riskLabel = insightRiskLabel(riskScore),
        consensusBias = bias,
        voteSplit = if (confidence >= 85) "4-1-0" else "3-1-1",
        riskReward = riskReward,
        slDistance = slDistance,
        directionLogic = if (validity == "VALID" || validity == "VALIDATED") "VALID" else "REVIEW",
        decisionGate = if (validity == "VALID" || confidence >= 85) "READY" else "REVIEW",
        remark = remark,
        source = source
    )
}

@Composable
fun SignalProMockupScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier,
    mission: Mission? = null,
    livePrice: Double? = null,
    onBackOverride: (() -> Unit)? = null,
    onSignalSetup: (() -> Unit)? = null,
    onAcceptSignal: (() -> Unit)? = null
) {
    val snapshot = remember(mission, livePrice) { insightBuildSnapshot(mission, livePrice) }
    val closeInsight = onBackOverride ?: { viewModel.navigateTo(AppScreen.Home) }
    androidx.activity.compose.BackHandler {
        closeInsight()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(T_Bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp),
            contentPadding = PaddingValues(bottom = 72.dp, top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { SignalProHeader(snapshot = snapshot, onBack = closeInsight) }
            item { PriceMatrixBlock(snapshot) }
            item { CQISurface(snapshot) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.weight(1f)) { RiskScoreSurface(snapshot) }
                    Box(modifier = Modifier.weight(1f)) { ExecutionReadinessSurface(snapshot) }
                }
            }
            item { ConsensusSummarySurface(snapshot) }
            item { MultiAIConsensusSurface() }
            item { DirectionValidationSurface(snapshot) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.weight(1f)) { RRValidationSurface(snapshot) }
                    Box(modifier = Modifier.weight(1f)) { SLSanitySurface(snapshot) }
                }
            }
            item { TPMatrixSurface(snapshot) }
            item { PositionAllocationSurface(snapshot) }
            item { DecisionGateSurface(snapshot) }
            item { ConflictFlagSurface() }
            item { AuditRow(snapshot) }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButtonsSurface(onBack = closeInsight, snapshot = snapshot, onSignalSetup = onSignalSetup, onAcceptSignal = onAcceptSignal)
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun SignalProHeader(snapshot: SignalInsightSnapshot, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = T_TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("TITAN ORACLE SIGNAL INSIGHT", color = T_TextPrimary, fontSize = 15.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Validation Cockpit", color = T_Cyan, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Symbol: ${snapshot.symbol}", color = T_TextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Mode: ${snapshot.marketMode}", color = if (snapshot.direction == "SHORT") T_Red else T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Signal State: ${snapshot.signalState}", color = if (snapshot.signalState.contains("READY") || snapshot.signalState.contains("ACTIVE")) T_Green else T_Gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("Validity: ${snapshot.validity}", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Timeframe: ${snapshot.timeframe}", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Freshness: ${snapshot.freshness}", color = T_Green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun PriceMatrixBlock(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("PRICE MATRIX") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PriceItem("Entry", snapshot.entry, T_Cyan)
            PriceItem("Current", snapshot.current, T_TextPrimary)
            PriceItem("Expected", snapshot.expected, T_Gold)
        }
        Spacer(modifier = Modifier.height(7.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PriceItem("Predicted", snapshot.predicted, T_Green)
            PriceItem("TP", snapshot.target, T_Green)
            PriceItem("SL", snapshot.stopLoss, T_Red)
        }
    }
}

@Composable
fun PriceItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"))
    }
}

@Composable
fun CQISurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("SIGNAL QUALITY ENGINE (CQI)") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("CQI Score", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(snapshot.cqiScore.toString(), color = if (snapshot.cqiScore >= 75) T_Green else T_Gold, fontSize = 21.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Classification", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(snapshot.classification, color = if (snapshot.cqiScore >= 75) T_Green else T_Gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Probability: ${snapshot.confidence}%", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text("Status: ${snapshot.validity}", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun RiskScoreSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("RISK SCORE / ঝুঁকির পরিমান") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(snapshot.riskScore.toString(), color = if (snapshot.riskScore <= 15) T_Green else if (snapshot.riskScore <= 25) T_Gold else T_Orange, fontSize = 21.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(snapshot.riskLabel, color = if (snapshot.riskScore <= 15) T_Green else if (snapshot.riskScore <= 25) T_Gold else T_Orange, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExecutionReadinessSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("EXECUTION READINESS") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("ACCEPTABLE", color = T_Cyan, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Spread: 0.04% | Liq: High", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text("Slip: Low | Source: ${snapshot.source}", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun ConsensusSummarySurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("CONSENSUS SUMMARY") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Consensus Confidence", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("${snapshot.classification} (${snapshot.confidence}%)", color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text("Consensus Bias", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(snapshot.consensusBias, color = if (snapshot.consensusBias == "AGGRESSIVE") T_Gold else T_Cyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Direction", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(if (snapshot.direction == "SHORT") "BEARISH" else "BULLISH", color = if (snapshot.direction == "SHORT") T_Red else T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(5.dp))
                Text("Vote Split", color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text(snapshot.voteSplit, color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("Model Spread: 14%", color = T_TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun MultiAIConsensusSurface() {
    SurfaceBlock("MULTI-AI CONSENSUS ENGINES") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            EngineCard("Oracle Quant Engine", "91", "BULLISH", "ACTIVE", T_Green)
            EngineCard("Deterministic Rules Core", "88", "BULLISH", "ACTIVE", T_Green)
            EngineCard("Sentiment Engine", "76", "NEUTRAL", "WATCH", T_Gold)
            EngineCard("Market Structure Engine", "82", "BULLISH", "ACTIVE", T_Green)
            EngineCard("Risk Guard Engine", "85", "SAFE", "VALIDATED", T_Cyan)
        }
    }
}

@Composable
fun EngineCard(name: String, score: String, vote: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().background(T_Surface).border(0.5.dp, T_BorderMedium, RoundedCornerShape(4.dp)).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, color = T_TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Status: $status", color = T_TextSecondary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(score, color = color, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(vote, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DirectionValidationSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("DIRECTION / TRADE LOGIC VALIDATION") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Direction Logic", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(snapshot.direction, color = if (snapshot.direction == "SHORT") T_Red else T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Entry State", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text(snapshot.directionLogic, color = if (snapshot.directionLogic == "VALID") T_Green else T_Gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(if (snapshot.direction == "SHORT") "StopLoss > Entry : ${snapshot.directionLogic}" else "StopLoss < Entry : ${snapshot.directionLogic}", color = if (snapshot.directionLogic == "VALID") T_Green else T_Gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(if (snapshot.direction == "SHORT") "Target < Entry : ${snapshot.directionLogic}" else "Target > Entry : ${snapshot.directionLogic}", color = if (snapshot.directionLogic == "VALID") T_Green else T_Gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun RRValidationSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("RISK / REWARD") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("RR: ${snapshot.riskReward}", color = T_Cyan, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(snapshot.directionLogic, color = if (snapshot.directionLogic == "VALID") T_Green else T_Gold, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("Risk Path: Standard", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun SLSanitySurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("STOP-LOSS SANITY") {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(snapshot.slDistance, color = T_Green, fontSize = 16.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("State: Standard", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun TPMatrixSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("TAKE PROFIT MATRIX") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TPItem("TP1", "+3%")
            TPItem("TP2", "+7%")
            TPItem("TP3", "+10%")
            TPItem("Target", snapshot.target)
        }
    }
}

@Composable
fun TPItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = T_Green, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PositionAllocationSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("POSITION ALLOCATION") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Posture", color = T_TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text(snapshot.consensusBias.lowercase().replaceFirstChar { it.uppercase() }, color = T_Cyan, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(snapshot.allocation, color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun DecisionGateSurface(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("DECISION GATE SUMMARY") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Decision Gate:", color = T_TextPrimary, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(snapshot.decisionGate, color = if (snapshot.decisionGate == "READY") T_Green else T_Gold, fontSize = 12.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text("Blocked: None", color = T_TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text("Warnings: Spread elevated", color = T_Orange, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Signal Dir: PASS", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("Risk Score: PASS", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Readiness: WARN", color = T_Gold, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Text("Consensus: PASS", color = T_Green, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun ConflictFlagSurface() {
    SurfaceBlock("CONFLICT FLAG") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChipLabel("REVIEW LIQUIDITY", T_Orange)
            ChipLabel("CONSENSUS ALIGNED", T_Green)
        }
    }
}

@Composable
fun AuditRow(snapshot: SignalInsightSnapshot) {
    SurfaceBlock("SOURCE / PROVENANCE / AUDIT") {
        Column {
            Text("Signal ID: SIG-${snapshot.symbol}-${snapshot.timeframe}", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Source: ${snapshot.source}", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Rules Fired: 8", color = T_TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("Audit: Pending", color = T_TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

@Composable
private fun SignalInsightSetupPanel(
    title: String,
    context: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF07101C).copy(alpha = 0.78f))
            .border(0.65.dp, T_Cyan.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 7.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = T_Cyan, fontSize = 9.5.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Text(context, color = T_TextMuted, fontSize = 7.4.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.82f))
        }
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun SignalInsightPresetChip(label: String, selected: Boolean, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) accent.copy(alpha = 0.18f) else Color(0xFF050A13))
            .border(0.65.dp, if (selected) accent else T_BorderMedium, RoundedCornerShape(8.dp))
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) accent else T_TextSecondary, fontSize = 8.2.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButtonsSurface(
    onBack: () -> Unit,
    snapshot: SignalInsightSnapshot = SignalInsightSnapshot(),
    onSignalSetup: (() -> Unit)? = null,
    onAcceptSignal: (() -> Unit)? = null
) {
    var step by remember { mutableStateOf(0) }
    var showDecisionBrief by remember { mutableStateOf(false) }
    
    val decisionBriefSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var fastSetupMutationAllowedAt by remember { mutableStateOf(0L) }
    fun guardedSetupMutation(action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now >= fastSetupMutationAllowedAt) {
            fastSetupMutationAllowedAt = now + 140L
            action()
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

    if (showDecisionBrief) {
        ModalBottomSheet(
            onDismissRequest = { showDecisionBrief = false },
            sheetState = decisionBriefSheetState,
            containerColor = Color(0xFF030712),
            contentColor = T_TextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("TITAN AI ORACLE DECISION BRIEF", fontSize = 17.sp, fontWeight = FontWeight.Black, color = T_Cyan)
                Text("Compact signal summary for fast decision-making", fontSize = 10.5.sp, color = T_TextSecondary)
                
                SurfaceBlock("Signal Verdict") { Text("Signal is strong, but entry confirmation is still required.", color = T_Green, fontSize = 11.sp) }
                SurfaceBlock("Why It Matters") { Text("Trend, momentum, volume, TITAN AI consensus, and risk score align for this setup.", color = T_Cyan, fontSize = 11.sp) }
                SurfaceBlock("Risk Warning") { Text("Risk is low to medium. Follow stop loss and position sizing.", color = T_Gold, fontSize = 11.sp) }
                SurfaceBlock("Suggested Action") { Text("Verify entry price, stop loss, and target before accepting the signal.", color = T_Green, fontSize = 11.sp) }
                
                Text("TITAN AI assists decision-making; the final trading decision is yours.", fontSize = 11.5.sp, color = T_TextMuted)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    T_Cyan.copy(alpha = 0.85f),
                                    Color(0xFF0077FF).copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(0.8.dp, T_TextPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { showDecisionBrief = false; onSignalSetup?.invoke() ?: run { step = 2 } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TITAN ∆ INSIGHT",
                        color = T_TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { showDecisionBrief = false; onSignalSetup?.invoke() ?: run { step = 2 } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = T_Cyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, T_Cyan.copy(alpha=0.72f)),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) { Text("SIGNAL SETUP", fontWeight = FontWeight.Black, fontSize = 11.sp) }
                    
                    Button(
                        onClick = { showDecisionBrief = false; onAcceptSignal?.invoke() ?: run { step = 2 } },
                        colors = ButtonDefaults.buttonColors(containerColor = T_Green, contentColor = T_Bg),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) { Text("ACCEPT SIGNAL", fontWeight = FontWeight.Black, fontSize = 11.sp) }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDecisionBrief = false },
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, T_TextSecondary.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = T_TextPrimary),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally).height(36.dp).widthIn(min = 126.dp)
                ) {
                    Text("CLOSE", color = T_TextPrimary, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 0.8.sp)
                }
            }
        }
    }

    if (step == 2) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { /* Locked setup cockpit: only CLOSE dismisses it. */ },
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
                    .border(0.9.dp, T_Cyan.copy(alpha = 0.30f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("SIGNAL SETUP", fontSize = 20.sp, fontWeight = FontWeight.Black, color = T_Cyan)
                Text("Auto-filled from the current signal. Review before accepting.", fontSize = 11.sp, color = T_TextSecondary)

                SignalInsightSetupPanel(title = "ONE-TAP QUICK SELECTION", context = snapshot.context) {
                    Text(
                        "RECOMMENDED is the default setup. Select SETUP-1 / SETUP-2 for faster simulation review before Mission Center handoff.",
                        color = T_TextSecondary,
                        fontSize = 9.sp,
                        lineHeight = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SignalInsightPresetChip("RECOMMENDED", true, T_Green, Modifier.weight(1f))
                        SignalInsightPresetChip("SETUP-1", false, T_Cyan, Modifier.weight(1f))
                        SignalInsightPresetChip("SETUP-2", false, T_Cyan, Modifier.weight(1f))
                    }
                }

                SignalInsightSetupPanel(title = "PRICE / EXIT MATRIX", context = snapshot.context) {
                    OutlinedTextField(value = snapshot.target, onValueChange = {}, label = { Text("TARGET") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = snapshot.stopLoss, onValueChange = {}, label = { Text("SL1 / STOP LOSS") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = snapshot.allocation, onValueChange = {}, label = { Text("ALLOCATION") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Text("Remark: ${snapshot.remark}", color = T_Cyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { step = 0 }, modifier = Modifier.weight(1f)) {
                        Text("CLOSE", color = T_TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { guardedSetupMutation { onAcceptSignal?.invoke() ?: run { step = 0 } } },
                        colors = ButtonDefaults.buttonColors(containerColor = T_Green, contentColor = T_Bg),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) { Text("ACCEPT SIGNAL", fontWeight = FontWeight.Black) }
                }
            }
        }
    }

    if (false && step == 1) {
        AlertDialog(
            onDismissRequest = { step = 0 },
            title = { Text("Verify Trade Details", color = T_Cyan, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Direction: LONG (FUTURES)", color = T_TextSecondary, fontSize = 12.5.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text("Locked Entry Price:", color = T_TextSecondary, fontSize = 11.sp)
                    Text("$63,920.00", color = T_Green, fontSize = 21.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) // The bright green dollar value user requested
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Once accepted, this entry will activate personal mission tracking.", color = T_Gold, fontSize = 10.sp)
                }
            },
            confirmButton = {
                Button(onClick = { step = 0 }, colors = ButtonDefaults.buttonColors(containerColor = T_Green)) {
                    Text("CONFIRM MISSION", fontWeight = FontWeight.Black, color = T_Bg)
                }
            },
            dismissButton = {
                TextButton(onClick = { step = 0 }) { Text("Cancel", color = T_TextSecondary) }
            },
            containerColor = Color(0xFF030712)
        )
    }

    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF02050D), Color(0xFF0B1220), Color(0xFF02050D))))
                    .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(T_Cyan.copy(alpha=0.10f), T_Green.copy(alpha=0.08f), T_Cyan.copy(alpha=0.10f))))
                    .background(androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color.Transparent, T_Cyan.copy(alpha=0.42f), T_TextPrimary.copy(alpha=0.12f), T_Green.copy(alpha=0.26f), Color.Transparent), startX = recoSweepX, endX = recoSweepX + 520f))
                    .border(0.8.dp, T_Cyan.copy(alpha=0.66f), RoundedCornerShape(10.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("← BACK", fontSize = 11.sp, fontWeight = FontWeight.Black, color = T_TextPrimary, letterSpacing = 0.8.sp)
            }

            Box(
                modifier = Modifier
                    .scale(scale)
                    .weight(1f)
                    .height(44.dp)
                    .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(T_Cyan.copy(alpha = pulseAlpha), T_Green.copy(alpha = pulseAlpha))), RoundedCornerShape(10.dp))
                    .border(0.8.dp, T_TextPrimary.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
                    .clickable(interactionSource = interactionSource, indication = androidx.compose.foundation.LocalIndication.current, onClick = { onAcceptSignal?.invoke() ?: run { step = 2 } })
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ACCEPT SIGNAL →", fontWeight = FontWeight.Black, fontSize = 11.sp, color = T_TextPrimary, letterSpacing = 0.8.sp)
                }
            }
        }
    }
}

@Composable
fun ChipLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SurfaceBlock(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, T_BorderMedium, RoundedCornerShape(6.dp))
            .background(T_Surface)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(title, color = T_TextMuted, fontSize = 9.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

