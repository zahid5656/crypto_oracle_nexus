package com.example.feature.accuracy_center

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.SignalEntity
import com.example.feature.mission_center.TabButton
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch

// Extracted from AccuracyCenterScreen.kt to keep the public screen entry point compact.
@Composable
fun SignalHistoryCard(entity: SignalEntity, isSystemGenerated: Boolean = false) {
    val isWin = entity.result == "WIN"
    val isLoss = entity.result == "LOSS"
    
    val badgeBg = when {
        isWin -> CryptoGreen.copy(alpha = 0.12f)
        isLoss -> TitanRed.copy(alpha = 0.12f)
        else -> TextMuted.copy(alpha = 0.12f)
    }

    val badgeColor = when {
        isWin -> CryptoGreen
        isLoss -> TitanRed
        else -> TextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSystemGenerated) {
                        Box(
                            modifier = Modifier
                                .background(CryptoCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(1.dp, CryptoCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "[System Only]", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CryptoCyan)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = entity.coinSymbol,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = entity.signalType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = entity.result,
                        color = badgeColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ENTRY PRICE", fontSize = 8.sp, color = TextMuted, letterSpacing = 0.5.sp)
                    Text(
                        text = formatPrice(entity.entryPrice),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }

                Column {
                    Text(text = "TARGET PRICE", fontSize = 8.sp, color = TextMuted, letterSpacing = 0.5.sp)
                    Text(
                        text = formatPrice(entity.targetPrice),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (entity.priceChangePct >= 0) "MAX PROFIT" else "MAX DRAWDOWN",
                        fontSize = 8.sp,
                        color = badgeColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = String.format("%s%.2f%%", if (entity.priceChangePct >= 0) "+" else "", entity.priceChangePct),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = badgeColor,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Regime Trace",
                        tint = CryptoCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PERSISTED REGIME: ${entity.marketRegime.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(entity.timestamp)),
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
@Composable
fun UserMissionHistoryCard(mission: com.example.model.Mission) {
    val isWin = !mission.isNegative
    val badgeBg = if (isWin) CryptoGreen.copy(alpha = 0.12f) else TitanRed.copy(alpha = 0.12f)
    val badgeColor = if (isWin) CryptoGreen else TitanRed

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(bottom = 8.dp) // space between lists
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(CryptoCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(1.dp, CryptoCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(text = "[Traded]", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CryptoCyan)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(badgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isWin) "PROFITABLE" else "STOPPED OUT",
                            color = badgeColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                
                Text(
                    text = "${mission.type} • ${mission.marketType}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = mission.coinSymbol,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Confidence: ${mission.confidence}%",
                        fontSize = 11.sp,
                        color = Color(0xFF9D65FF)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Entry",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(mission.startTime)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderColor)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Signal Info",
                        tint = CryptoCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ENTRY LOCKED: ${formatPrice(mission.entryPrice)}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(mission.startTime)),
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
internal fun formatPrice(price: Double): String {
    return when {
        price >= 1000 -> String.format("$%,.2f", price)
        price >= 1 -> String.format("$%,.3f", price)
        else -> String.format("$%.6f", price)
    }
}
@Composable
fun WinLossRatioCanvas(wins: Int, losses: Int, pending: Int) {
    val total = wins + losses + pending
    if (total == 0) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "DISTRIBUTION METRICS INDEX",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Custom Canvas drawing a beautiful rounded segmented bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(BorderColor)
        ) {
            val winsWeight = wins.toFloat() / total
            val lossesWeight = losses.toFloat() / total
            val pendingWeight = pending.toFloat() / total

            val barWidth = size.width

            val winSize = barWidth * winsWeight
            val lossSize = barWidth * lossesWeight
            val pendingSize = barWidth * pendingWeight

            // 1. Draw wins (CryptoGreen)
            if (winSize > 0) {
                drawRect(
                    color = CryptoGreen,
                    size = Size(winSize, size.height)
                )
            }

            // 2. Draw losses (TitanRed)
            if (lossSize > 0) {
                drawRect(
                    color = TitanRed,
                    size = Size(lossSize, size.height),
                    topLeft = Offset(winSize, 0f)
                )
            }

            // 3. Draw pending (TextMuted)
            if (pendingSize > 0) {
                drawRect(
                    color = TextMuted,
                    size = Size(pendingSize, size.height),
                    topLeft = Offset(winSize + lossSize, 0f)
                )
            }
        }

        // Segment Labels and Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val winPct = (wins.toFloat() / total) * 100
            val lossPct = (losses.toFloat() / total) * 100
            val pendingPct = (pending.toFloat() / total) * 100

            LegendItem(label = "WINS (${String.format("%.1f", winPct)}%)", color = CryptoGreen)
            LegendItem(label = "LOSSES (${String.format("%.1f", lossPct)}%)", color = TitanRed)
            if (pending > 0) {
                LegendItem(label = "PENDING (${String.format("%.1f", pendingPct)}%)", color = TextMuted)
            }
        }
    }
}
@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )
    }
}
@Composable
fun StatCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
        }
    }
}
@Composable
fun LedgerPerformanceChart(modifier: Modifier = Modifier, missions: List<com.example.model.Mission>) {
    Canvas(modifier = modifier) {
        val path = androidx.compose.ui.graphics.Path()
        val data = if (missions.isEmpty()) {
            listOf(0f, 5f, 15f, -5f, 20f, 10f, 30f, 25f, 40f) 
        } else {
            val points = mutableListOf<Float>()
            var current = 0f
            points.add(current)
            missions.forEach { m ->
                val diff = if (m.entryPrice > 0) 
                    ((m.currentPrice - m.entryPrice) / m.entryPrice).toFloat() * 100f
                else 0f
                
                val pnl = if (m.type == "LONG") diff else -diff
                val activePnl = if (!m.isNegative) kotlin.math.abs(pnl).coerceAtLeast(2f) else -kotlin.math.abs(pnl).coerceAtLeast(2f)
                
                current += activePnl
                points.add(current)
            }
            points
        }
        val min = data.minOrNull() ?: 0f
        val maxVal = data.maxOrNull() ?: 100f
        val range = if (maxVal - min == 0f) 1f else maxVal - min
        
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        path.moveTo(0f, size.height - ((data[0] - min) / range) * size.height)
        for (i in 1 until data.size) {
            path.lineTo(i * stepX, size.height - ((data[i] - min) / range) * size.height)
        }
        
        drawPath(
            path = path,
            color = Color(0xFF9D65FF),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
        )
    }
}
@Composable
fun MetricDonutChart(modifier: Modifier = Modifier, winRate: Float) {
    Canvas(modifier = modifier) {
        val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = 40f)
        drawArc(
            color = TitanRed,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke
        )
        drawArc(
            color = CryptoGreen,
            startAngle = -90f,
            sweepAngle = (winRate / 100f) * 360f,
            useCenter = false,
            style = stroke
        )
    }
}
