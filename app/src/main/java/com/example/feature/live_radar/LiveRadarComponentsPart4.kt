package com.example.feature.live_radar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable

@Composable
fun AiAutoPilotMockupSection(isBengali: Boolean) {
    var simulationState by remember { mutableStateOf("STANDBY") }
    var candidateSymbol by remember { mutableStateOf("-") }

    LaunchedEffect(simulationState) {
        if (simulationState == "SCANNING PREVIEW") {
            candidateSymbol = "Searching..."
            kotlinx.coroutines.delay(1200)
            simulationState = "GATE REVIEW"
            candidateSymbol = "BTC/USDT"
            kotlinx.coroutines.delay(1500)
            simulationState = "SIMULATION READY"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TITAN AI AUTO PILOT",
                    color = CryptoCyan,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Simulation-only guarded radar scan",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SIMULATION ONLY",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // State & Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitanOracleVisionScanner(
                state = simulationState,
                modifier = Modifier.weight(1f),
                onScan = {
                    if (simulationState == "STANDBY" || simulationState == "SIMULATION READY" || simulationState == "BLOCKED") {
                        simulationState = "SCANNING PREVIEW"
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.48f)) {
                Text(text = "STATE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.SansSerif)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = simulationState, color = CryptoCyan, fontSize = 11.sp, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scope Chips
        Text(text = "SCOPE", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScopeChip("SPOT")
            ScopeChip("FUTURES LONG")
            ScopeChip("FUTURES SHORT")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Presets
        Text(text = "INVESTMENT PRESETS", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PresetChip("IN1 $10", isLocked = false)
            PresetChip("IN2 $15", isLocked = false)
            PresetChip("IN3 $30", isLocked = true)
            PresetChip("IN4 $50", isLocked = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (simulationState != "STANDBY") {
            Text(text = "CANDIDATE PREVIEW", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground, RoundedCornerShape(8.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HardGateRow("Candidate", candidateSymbol, CryptoCyan)
                HardGateRow("Scope", "SPOT", CryptoCyan)
                HardGateRow("Direction", "WATCH LONG", CryptoCyan)
                HardGateRow("Risk Score", "MEDIUM", AccentGold)
                HardGateRow("Execution Readiness", "ACCEPTABLE", CryptoGreen)
                HardGateRow("Consensus Confidence", "82%", CryptoGreen)
                HardGateRow("Gate Result", if (simulationState == "SIMULATION READY" || simulationState == "BLOCKED") simulationState else "EVALUATING...", if (simulationState == "BLOCKED") CryptoRedText else if (simulationState == "SIMULATION READY") CryptoGreen else AccentGold)
                if (simulationState == "SIMULATION READY" || simulationState == "BLOCKED") {
                    Text(
                        text = if (simulationState == "BLOCKED") "Reason: Spread, slippage, or validity window failed local preview." else "Reason: Direction, SL validity, RR, liquidity, and freshness passed local preview.",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        val gateStatus = when (simulationState) {
            "STANDBY" -> "STANDBY"
            "SCANNING PREVIEW" -> "EVALUATING"
            "GATE REVIEW" -> "CHECKING"
            else -> "PASS"
        }
        val gateColor = when (simulationState) {
            "STANDBY" -> CryptoCyan
            "SCANNING PREVIEW", "GATE REVIEW" -> AccentGold
            else -> CryptoGreen
        }

        // Hard Gate Preview
        Text(text = "HARD GATE PREVIEW", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground, RoundedCornerShape(8.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HardGateRow("Signal Direction", gateStatus, gateColor)
            HardGateRow("Stop-Loss Validity", gateStatus, gateColor)
            HardGateRow("Risk / Reward", gateStatus, gateColor)
            HardGateRow("Risk Score", gateStatus, gateColor)
            HardGateRow("Execution Readiness", gateStatus, gateColor)
            HardGateRow("Data Freshness", gateStatus, gateColor)
            HardGateRow("Spread", gateStatus, gateColor)
            HardGateRow("Slippage", gateStatus, gateColor)
            HardGateRow("Liquidity", gateStatus, gateColor)
            HardGateRow("Consensus Confidence", gateStatus, gateColor)
            HardGateRow("Consensus Disagreement", gateStatus, gateColor)
            HardGateRow("Conflict Flag", gateStatus, gateColor)
            HardGateRow("Portfolio Exposure", gateStatus, gateColor)
            HardGateRow("Validity Window", gateStatus, gateColor)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Safety Copy
        Text(
            text = "TITAN AI Auto Pilot is simulation-only in this phase. No exchange order will be opened.",
            color = AccentGold,
            fontSize = 10.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 14.sp
        )
    }
}


@Composable
private fun TitanOracleVisionScanner(
    state: String,
    modifier: Modifier = Modifier,
    onScan: () -> Unit
) {
    val scanning = state == "SCANNING PREVIEW" || state == "GATE REVIEW"
    val transition = rememberInfiniteTransition(label = "VisionScanner")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "RadarRotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.24f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(animation = tween(900, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "RadarPulse"
    )

    Row(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        DarkBackground,
                        CryptoCyan.copy(alpha = 0.10f),
                        DarkSurfaceVariant.copy(alpha = 0.78f)
                    )
                )
            )
            .border(0.8.dp, CryptoCyan.copy(alpha = if (scanning) 0.72f else 0.38f), RoundedCornerShape(14.dp))
            .clickable { onScan() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(62.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(color = CryptoCyan.copy(alpha = 0.20f + pulse * 0.18f), style = Stroke(width = 2.2f))
                drawCircle(color = CryptoGreen.copy(alpha = 0.18f), radius = size.minDimension * 0.31f, style = Stroke(width = 1.3f))
                drawCircle(color = CryptoCyan.copy(alpha = 0.10f), radius = size.minDimension * 0.18f)
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .rotate(if (scanning) rotation else 0f)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(27.dp)
                        .height(1.5.dp)
                        .background(CryptoCyan.copy(alpha = if (scanning) 0.90f else 0.45f), RoundedCornerShape(50))
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (scanning) CryptoGreen else CryptoCyan)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TITAN AI ORACLE RADAR",
                color = CryptoCyan,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = if (scanning) "TRACKING MISSION" else "VISION SCANNER",
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                maxLines = 1
            )
            Text(
                text = if (scanning) "Local gate analysis running" else "Tap to preview guarded scan",
                color = TextMuted,
                fontSize = 8.8.sp,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 11.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ScopeChip(label: String) {
    Box(
        modifier = Modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun PresetChip(label: String, isLocked: Boolean) {
    Box(
        modifier = Modifier
            .background(if (isLocked) DarkBackground else DarkSurfaceVariant, RoundedCornerShape(4.dp))
            .border(0.5.dp, if (isLocked) BorderColor.copy(alpha = 0.5f) else BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = if (isLocked) TextMuted else TextPrimary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isLocked) FontWeight.Normal else FontWeight.Bold
            )
            if (isLocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "[LOCKED]",
                    color = AccentGold,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun HardGateRow(label: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = status,
            color = statusColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}
