package com.example.feature.live_radar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*   // ← keeps your color & typography tokens

/* ─────────────────────────────────────────  PUBLIC COMPOSABLE  ───────────────────────────────────────── */

@Composable
fun AiAutoPilotMockupSection(isBengali: Boolean) {

    /* animated radar rotation (used in “Titan Vision” button) */
    val radarRotation by rememberInfiniteTransition(label = "radar")
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2_800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radarRotation"
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {

        /* ───────── HEADER ───────── */
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
            BadgePill("SIMULATION ONLY")
        }

        Spacer(Modifier.height(16.dp))

        /* ───────── POWER-CORE BUTTON  (“SCAN ∆ TITAN VISION”) ───────── */
        TitanVisionButton(
            radarRotation = radarRotation,
            isBengali = isBengali,
            onClick = {
                // TODO: viewModel.activateTitanVision()
            }
        )

        Spacer(Modifier.height(16.dp))

        /* ───────── STATE  (unchanged) ───────── */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionChip("SCAN ∆ TITAN VISION")
            Spacer(Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.5f)
            ) {
                Text("STATE", color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(2.dp))
                Text(
                    "STANDBY",
                    color = CryptoCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ───────── SCOPE  ───────── */
        SectionLabel("SCOPE")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScopeChip("SPOT")
            ScopeChip("FUTURES LONG")
            ScopeChip("FUTURES SHORT")
        }

        Spacer(Modifier.height(16.dp))

        /* ───────── PRESETS  ───────── */
        SectionLabel("INVESTMENT PRESETS")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PresetChip("IN1 $10", isLocked = false)
            PresetChip("IN2 $15", isLocked = false)
            PresetChip("IN3 $30", isLocked = true)
            PresetChip("IN4 $50", isLocked = true)
        }

        Spacer(Modifier.height(16.dp))

        /* ───────── HARD-GATE PREVIEW  ───────── */
        SectionLabel("HARD GATE PREVIEW")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground, RoundedCornerShape(8.dp))
                .border(0.5.dp, BorderColor, RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "Signal Direction",
                "Stop-Loss Validity",
                "Risk / Reward",
                "Risk Score",
                "Execution Readiness",
                "Data Freshness",
                "Spread",
                "Slippage",
                "Liquidity",
                "Consensus Confidence",
                "Consensus Disagreement",
                "Conflict Flag",
                "Portfolio Exposure",
                "Validity Window"
            ).forEach { label ->
                HardGateRow(label, "STANDBY", CryptoCyan)
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ───────── DISCLAIMER ───────── */
        Text(
            text = "TITAN AI Auto Pilot is simulation-only in this phase. No exchange order will be opened.",
            color = AccentGold,
            fontSize = 10.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 14.sp
        )
    }
}

/* ─────────────────────────────────────────  REUSABLE UI ELEMENTS  ───────────────────────────────────────── */

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun BadgePill(text: String) {
    Box(
        modifier = Modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionChip(label: String) {
    Box(
        modifier = Modifier
            .background(DarkSurfaceVariant, RoundedCornerShape(4.dp))
            .border(0.5.dp, BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .weight(1f)
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
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
            .background(
                if (isLocked) DarkBackground else DarkSurfaceVariant,
                RoundedCornerShape(4.dp)
            )
            .border(
                0.5.dp,
                if (isLocked) BorderColor.copy(alpha = 0.5f) else BorderColor,
                RoundedCornerShape(4.dp)
            )
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
                Spacer(Modifier.width(4.dp))
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

/* ─────────────────────────────────────────  HELPER COMPOSABLE  ───────────────────────────────────────── */

@Composable
private fun TitanVisionButton(
    radarRotation: Float,
    isBengali: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(CryptoCyan.copy(alpha = 0.12f), Color.Transparent)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.5.dp, CryptoCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ----- animated radar icon ----- */
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.radar_scan), // supply this asset
                    contentDescription = "Titan Radar",
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { rotationZ = radarRotation }
                )
                /* pulsing sweep */
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CryptoGreen.copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                        .graphicsLayer {
                            alpha = (1 + kotlin.math.sin(radarRotation * 0.1f)) * 0.6f
                        }
                )
            }

            /* ----- label ----- */
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SCAN ∆ TITAN VISION",
                    color = CryptoCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isBengali) "ইনস্ট্যান্ট মার্কেট ওভাররাইড" else "INSTANT MARKET OVERRIDE",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}