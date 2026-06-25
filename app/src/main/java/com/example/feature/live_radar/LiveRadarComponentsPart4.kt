package com.example.feature.live_radar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun AiAutoPilotMockupSection(isBengali: Boolean) {
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
                    text = "AI AUTO PILOT",
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
            Box(
                modifier = Modifier
                    .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
                    .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "SCAN FOR AI AUTO PILOT",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(0.5f)) {
                Text(text = "STATE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "STANDBY", color = CryptoCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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
            HardGateRow("Signal Direction", "STANDBY", CryptoCyan)
            HardGateRow("Stop-Loss Validity", "STANDBY", CryptoCyan)
            HardGateRow("Risk / Reward", "STANDBY", CryptoCyan)
            HardGateRow("Risk Score", "STANDBY", CryptoCyan)
            HardGateRow("Execution Readiness", "STANDBY", CryptoCyan)
            HardGateRow("Data Freshness", "STANDBY", CryptoCyan)
            HardGateRow("Spread", "STANDBY", CryptoCyan)
            HardGateRow("Slippage", "STANDBY", CryptoCyan)
            HardGateRow("Liquidity", "STANDBY", CryptoCyan)
            HardGateRow("Consensus Confidence", "STANDBY", CryptoCyan)
            HardGateRow("Consensus Disagreement", "STANDBY", CryptoCyan)
            HardGateRow("Conflict Flag", "STANDBY", CryptoCyan)
            HardGateRow("Portfolio Exposure", "STANDBY", CryptoCyan)
            HardGateRow("Validity Window", "STANDBY", CryptoCyan)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Safety Copy
        Text(
            text = "AI Auto Pilot is simulation-only in this phase. No exchange order will be opened.",
            color = AccentGold,
            fontSize = 10.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 14.sp
        )
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
