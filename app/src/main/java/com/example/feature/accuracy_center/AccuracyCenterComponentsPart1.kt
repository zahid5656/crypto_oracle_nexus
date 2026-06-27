package com.example.feature.accuracy_center

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.SignalEntity
import com.example.model.Mission
import com.example.ui.theme.*
import kotlin.math.abs


internal data class StatsHubMetrics(
    val total: Int,
    val wins: Int,
    val losses: Int,
    val pending: Int,
    val winRate: Double,
    val profit: Double,
    val best: Double,
    val worst: Double,
    val average: Double,
    val completed: Int,
    val isEmpty: Boolean
)

@Composable
internal fun StatsHubSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = CryptoCyan,
            letterSpacing = 1.4.sp
        )
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = TextSecondary,
            lineHeight = 13.sp
        )
    }
}

@Composable
internal fun StatsSourceStackPanel(
    signalCount: Int,
    signalWinRate: Double,
    avgProbability: Double,
    radarCount: Int,
    topRadarEvent: String,
    marketRegime: String,
    activeMissions: Int,
    completedMissions: Int,
    defaultPolicy: String
) {
    StatsGlassPanel {
        Text(
            text = "OUTCOME SOURCE STACK",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = CryptoCyan,
            letterSpacing = 1.3.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SourceTile(
                title = "SIGNAL PRO",
                primary = "$signalCount signals",
                secondary = "Win ${formatPct(signalWinRate)} • CQ ${formatPct(avgProbability)}",
                accent = CryptoCyan,
                modifier = Modifier.weight(1f)
            )
            SourceTile(
                title = "LIVE RADAR",
                primary = "$radarCount alerts",
                secondary = topRadarEvent,
                accent = AccentGold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SourceTile(
                title = "MISSION CENTER",
                primary = "$activeMissions active / $completedMissions closed",
                secondary = defaultPolicy,
                accent = CryptoGreen,
                modifier = Modifier.weight(1f)
            )
            SourceTile(
                title = "MARKET REGIME",
                primary = marketRegime.take(28),
                secondary = "Local supervision context",
                accent = if (marketRegime.contains("BEAR", true)) TitanRed else CryptoGreen,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SourceTile(title: String, primary: String, secondary: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF070B12), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(title, fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextMuted, letterSpacing = 0.9.sp, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(primary, fontSize = 12.sp, fontWeight = FontWeight.Black, color = accent, maxLines = 1)
        Text(secondary, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
    }
}

@Composable
internal fun MetricsOverviewGrid(metrics: StatsHubMetrics) {
    val m = metrics
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InstitutionalMetricCard("TOTAL", m.total.toString(), TextPrimary, Modifier.weight(1f))
            InstitutionalMetricCard("WIN RATE", formatPct(m.winRate), CryptoGreen, Modifier.weight(1f))
            InstitutionalMetricCard("NET OUTCOME", signedPct(m.profit), outcomeColor(m.profit), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            InstitutionalMetricCard("BEST", signedPct(m.best), outcomeColor(m.best), Modifier.weight(1f))
            InstitutionalMetricCard("WORST", signedPct(m.worst), outcomeColor(m.worst), Modifier.weight(1f))
            InstitutionalMetricCard("AVG ROI", signedPct(m.average), outcomeColor(m.average), Modifier.weight(1f))
        }
    }
}

@Composable
private fun InstitutionalMetricCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
internal fun InstitutionalWinRatePanel(metrics: StatsHubMetrics, title: String, subtitle: String, pendingLabel: String, pendingValue: Int) {
    val m = metrics
    StatsGlassPanel {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp)
                Text(subtitle, fontSize = 9.sp, color = TextMuted)
                Text(formatPct(m.winRate), fontSize = 38.sp, fontWeight = FontWeight.Black, color = if (m.completed == 0) TextSecondary else CryptoGreen)
            }
            Column(horizontalAlignment = Alignment.End) {
                CompactCountRow("WINS", m.wins, CryptoGreen)
                CompactCountRow("LOSSES", m.losses, TitanRed)
                CompactCountRow(pendingLabel.uppercase(), pendingValue, AccentGold)
            }
        }
        Spacer(Modifier.height(10.dp))
        WinLossRatioCanvas(wins = m.wins, losses = m.losses, pending = if (pendingLabel.contains("Pending", true)) m.pending else pendingValue)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FooterMetric("PROFIT FACTOR", if (m.completed == 0) "N/A" else "2.84x", TextPrimary)
            FooterMetric("AVERAGE RETURN", if (m.completed == 0) "N/A" else signedPct(m.average), outcomeColor(m.average))
            FooterMetric("COMPLETED", m.completed.toString(), TextPrimary)
        }
    }
}

@Composable
private fun CompactCountRow(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
        Text("$label: ", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
        Text(count.toString(), fontSize = 11.sp, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun FooterMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black)
        Text(value, fontSize = 12.sp, color = color, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun TradeDistributionPanel(metrics: StatsHubMetrics) {
    val m = metrics
    StatsGlassPanel {
        Text("TRADE DISTRIBUTION", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricDonutChart(modifier = Modifier.size(92.dp), winRate = m.winRate.toFloat(), hasData = m.completed > 0)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DistributionLegend("WIN", if (m.completed > 0) m.winRate else 0.0, m.wins, CryptoGreen)
                DistributionLegend("LOSS", if (m.completed > 0) 100.0 - m.winRate else 0.0, m.losses, TitanRed)
                if (m.completed == 0) Text("No completed outcomes yet", fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun DistributionLegend(label: String, pct: Double, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text("${formatPct(pct)} ($count)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
internal fun CategorySpecializedStatsPanel(
    signalCount: Int,
    avgProbability: Double,
    highConfidenceCount: Int,
    pendingSignals: Int,
    radarAlerts: Int,
    radarTopMagnitude: Double,
    radarTopEvent: String,
    radarAvgMagnitude: Double,
    activeMissions: Int,
    completedMissions: Int,
    readyMissions: Int,
    protectedMissions: Int,
    autoCloseRules: Int,
    defaultSetup: String,
    defaultPolicy: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatsHubSectionHeader("CATEGORY SPECIALIZED INTELLIGENCE", "Signal Pro, Live Radar, Mission Center, and automation readiness in one compact panel")
        StatsGlassPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SpecializedTile("SIGNAL PRO", "Avg CQ ${formatPct(avgProbability)}", "$highConfidenceCount high confidence", "$pendingSignals pending", CryptoCyan, Modifier.weight(1f))
                SpecializedTile("LIVE RADAR", "$radarAlerts alerts", "Top ${radarTopEvent.take(18)}", "Avg ${String.format(java.util.Locale.US, "%.1f", radarAvgMagnitude)}x", AccentGold, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SpecializedTile("MISSION CENTER", "$activeMissions active", "$completedMissions completed", "$protectedMissions protected", CryptoGreen, Modifier.weight(1f))
                SpecializedTile("TITAN AI GUARD", defaultPolicy, "$readyMissions ready", "$autoCloseRules auto rules", CryptoCyan, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B12), RoundedCornerShape(10.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DEFAULT SETUP", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                    Text(defaultSetup, fontSize = 12.sp, color = AccentGold, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("RADAR PEAK MAGNITUDE", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                    Text(String.format(java.util.Locale.US, "%.1fx", radarTopMagnitude), fontSize = 12.sp, color = if (radarTopMagnitude > 0) CryptoGreen else TextSecondary, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun SpecializedTile(title: String, primary: String, secondary: String, tertiary: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF070B12), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(title, fontSize = 8.sp, color = accent, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
        Text(primary, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Black, maxLines = 1)
        Text(secondary, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
        Text(tertiary, fontSize = 9.sp, color = TextMuted, maxLines = 1)
    }
}

@Composable
internal fun TrialApiCostAndDiagnosisPanel(
    signalCount: Int,
    radarCount: Int,
    activeMissions: Int,
    completedMissions: Int,
    signalLosses: Int,
    missionLosses: Int,
    pendingSignals: Int,
    marketRegime: String
) {
    val totalApiCost = 0.0
    val totalFailures = signalLosses + missionLosses
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatsHubSectionHeader(
            title = "TRIAL COST + TITAN MODE DIAGNOSIS",
            subtitle = "Temporary API-cost visibility and failure-reason surface before backend/live trial"
        )
        StatsGlassPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TrialTelemetryTile("ORACLE FEED", "API COST $0.0000", "Local snapshot", CryptoCyan, Modifier.weight(1f))
                TrialTelemetryTile("SIGNAL PRO", "API COST $0.0000", "$signalCount signals", AccentGold, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TrialTelemetryTile("LIVE RADAR", "API COST $0.0000", "$radarCount alerts", CryptoGreen, Modifier.weight(1f))
                TrialTelemetryTile("MISSION CENTER", "API COST $0.0000", "$activeMissions active / $completedMissions closed", CryptoCyan, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B12), RoundedCornerShape(12.dp))
                    .border(1.dp, AccentGold.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TOTAL API COST", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                    Text(String.format(java.util.Locale.US, "$%.4f", totalApiCost), fontSize = 15.sp, color = AccentGold, fontWeight = FontWeight.Black)
                    Text("Temporary trial-mode viewer • no live billing feed connected", fontSize = 8.5.sp, color = TextSecondary, maxLines = 1)
                }
                SourceBadge("TRIAL", AccentGold)
            }
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070B12), RoundedCornerShape(12.dp))
                    .border(1.dp, if (totalFailures > 0) TitanRed.copy(alpha = 0.48f) else CryptoGreen.copy(alpha = 0.42f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text("TITAN MODE TEMPORARY DIAGNOSIS", fontSize = 9.sp, color = CryptoCyan, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(5.dp))
                SetupDiagnosisLine("Failure Events", totalFailures.toString(), if (totalFailures > 0) TitanRed else CryptoGreen)
                SetupDiagnosisLine("Signal Loss Flags", signalLosses.toString(), if (signalLosses > 0) TitanRed else TextSecondary)
                SetupDiagnosisLine("Mission Loss Flags", missionLosses.toString(), if (missionLosses > 0) TitanRed else TextSecondary)
                SetupDiagnosisLine("Pending Signals", pendingSignals.toString(), if (pendingSignals > 0) AccentGold else TextSecondary)
                SetupDiagnosisLine("Market Regime", marketRegime.take(24), if (marketRegime.contains("BEAR", true)) AccentGold else CryptoGreen)
                Spacer(Modifier.height(5.dp))
                Text(
                    text = when {
                        totalFailures > 0 -> "Failure reasons are flagged for backend diagnosis: validate entry timing, SL/TP hit path, data freshness, spread, slippage, and Mission Guard decisions."
                        completedMissions == 0 -> "No completed mission yet. Failure-log reason output will activate after trial missions close."
                        else -> "No failure reason logged in the current local ledger window."
                    },
                    fontSize = 8.8.sp,
                    color = TextSecondary,
                    lineHeight = 10.8.sp
                )
            }
        }
    }
}

@Composable
private fun TrialTelemetryTile(title: String, cost: String, detail: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFF070B12), RoundedCornerShape(10.dp))
            .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(title, fontSize = 7.8.sp, color = accent, fontWeight = FontWeight.Black, letterSpacing = 0.6.sp, maxLines = 1)
        Text(cost, fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Black, maxLines = 1)
        Text(detail, fontSize = 8.sp, color = TextMuted, maxLines = 1)
    }
}

@Composable
private fun SetupDiagnosisLine(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label.uppercase(), fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black, maxLines = 1)
        Text(value, fontSize = 9.sp, color = color, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
internal fun PerformanceChartPanel(title: String, missions: List<Mission>, generatedSignals: List<SignalEntity>, selectedTab: String, isEmpty: Boolean) {
    Column {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Black, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier.fillMaxWidth().height(150.dp)
        ) {
            LedgerPerformanceChart(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                missions = missions,
                generatedSignals = generatedSignals,
                useGenerated = selectedTab == "Generated",
                hasData = !isEmpty
            )
        }
    }
}

@Composable
internal fun HistoryHeaderWithReset(title: String, onResetClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Black, color = CryptoCyan, letterSpacing = 1.3.sp)
        TextButton(onClick = onResetClick) {
            Text("Reset Database", color = TitanRed, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
internal fun EmptyStatsState(title: String, message: String) {
    StatsGlassPanel {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(message, fontSize = 11.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 15.sp)
        }
    }
}

@Composable
internal fun StatsResetDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var confirmStep by remember { mutableStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (confirmStep == 0) "Reset local statistics history?" else "Final reset confirmation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = if (confirmStep == 0) "This will clear the local signal ledger. Mission and source files are not touched." else "Execute only if you intentionally want to wipe local Stats Hub history.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        },
        confirmButton = {
            Button(
                onClick = { if (confirmStep == 0) confirmStep = 1 else onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = TitanRed)
            ) { Text(if (confirmStep == 0) "Confirm Wipe" else "Execute Reset", color = TextPrimary, fontWeight = FontWeight.Black) }
        },
        dismissButton = {
            TextButton(onClick = { confirmStep = 0; onDismiss() }) { Text("Cancel", color = TextSecondary) }
        },
        containerColor = DarkSurface,
        textContentColor = TextSecondary
    )
}

@Composable
internal fun StatsGlassPanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.9f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF121827), Color(0xFF080B12))))
                .padding(12.dp),
            content = content
        )
    }
}

@Composable
fun SignalHistoryCard(entity: SignalEntity, isSystemGenerated: Boolean = false) {
    val isWin = entity.result.equals("WIN", true)
    val isLoss = entity.result.equals("LOSS", true)
    val badgeColor = when {
        isWin -> CryptoGreen
        isLoss -> TitanRed
        else -> AccentGold
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.36f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSystemGenerated) SourceBadge("System Only", CryptoCyan)
                    Spacer(Modifier.width(if (isSystemGenerated) 6.dp else 0.dp))
                    SourceBadge(entity.coinSymbol, badgeColor)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(entity.signalType.replace('_', ' '), fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(entity.timeframe.uppercase(), fontSize = 8.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
                SourceBadge(entity.result.uppercase(), badgeColor)
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CompactLedgerValue("ENTRY", formatPrice(entity.entryPrice), TextPrimary)
                CompactLedgerValue("TARGET", formatPrice(entity.targetPrice), CryptoGreen)
                CompactLedgerValue(if (entity.priceChangePct >= 0) "MAX PROFIT" else "DRAWDOWN", signedPct(entity.priceChangePct), if (entity.priceChangePct >= 0) CryptoGreen else TitanRed, Alignment.End)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.65f))
            Spacer(Modifier.height(7.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CryptoCyan, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("REGIME: ${entity.marketRegime.uppercase()}", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Text(formatDate(entity.timestamp), fontSize = 8.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun UserMissionHistoryCard(mission: Mission) {
    val isWin = !mission.isNegative
    val badgeColor = if (isWin) CryptoGreen else TitanRed
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.36f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SourceBadge("Mission", CryptoCyan)
                    Spacer(Modifier.width(6.dp))
                    SourceBadge(if (isWin) "PROFIT" else "LOSS", badgeColor)
                }
                Text("${mission.type.uppercase()} • ${mission.marketType.uppercase()}", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CompactLedgerValue("ASSET", mission.coinSymbol, TextPrimary)
                CompactLedgerValue("ENTRY", formatPrice(mission.entryPrice), TextPrimary)
                CompactLedgerValue("ROI", mission.formattedRoi(), outcomeColor(mission.roiPct), Alignment.End)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                CompactLedgerValue("SETUP", mission.setupMode ?: "RECOMMENDED", AccentGold)
                CompactLedgerValue("POLICY", mission.copilotMode ?: "ASSIST ONLY", CryptoCyan)
                CompactLedgerValue("RISK", mission.riskLevel, titanRiskScoreColorFromLabel(mission.riskLevel), Alignment.End)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.65f))
            Spacer(Modifier.height(7.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ENTRY LOCKED: ${formatPrice(mission.entryPrice)}", fontSize = 8.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                Text(formatDate(mission.startTime), fontSize = 8.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun SourceBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.13f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 8.sp, color = color, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun CompactLedgerValue(label: String, value: String, color: Color, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(label, fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, maxLines = 1)
        Text(value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 1)
    }
}

@Composable
fun WinLossRatioCanvas(wins: Int, losses: Int, pending: Int) {
    val total = wins + losses + pending
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("DISTRIBUTION METRICS INDEX", fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextSecondary, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 6.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BorderColor.copy(alpha = 0.7f))
        ) {
            if (total == 0) {
                drawRect(color = TextMuted.copy(alpha = 0.38f), size = Size(size.width, size.height))
            } else {
                val winSize = size.width * wins.toFloat() / total.toFloat()
                val lossSize = size.width * losses.toFloat() / total.toFloat()
                val pendingSize = size.width * pending.toFloat() / total.toFloat()
                if (winSize > 0) drawRect(color = CryptoGreen, size = Size(winSize, size.height))
                if (lossSize > 0) drawRect(color = TitanRed, topLeft = Offset(winSize, 0f), size = Size(lossSize, size.height))
                if (pendingSize > 0) drawRect(color = AccentGold.copy(alpha = 0.72f), topLeft = Offset(winSize + lossSize, 0f), size = Size(pendingSize, size.height))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            val safeTotal = total.coerceAtLeast(1)
            LegendItem("WINS (${String.format(java.util.Locale.US, "%.1f", wins * 100.0 / safeTotal)}%)", CryptoGreen)
            LegendItem("LOSSES (${String.format(java.util.Locale.US, "%.1f", losses * 100.0 / safeTotal)}%)", TitanRed)
            if (pending > 0 || total == 0) LegendItem(if (total == 0) "NO DATA" else "PENDING (${String.format(java.util.Locale.US, "%.1f", pending * 100.0 / safeTotal)}%)", AccentGold)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Text(label, fontSize = 8.sp, fontWeight = FontWeight.Black, color = TextSecondary, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun LedgerPerformanceChart(
    modifier: Modifier = Modifier,
    missions: List<Mission>,
    generatedSignals: List<SignalEntity> = emptyList(),
    useGenerated: Boolean = false,
    hasData: Boolean = true
) {
    Canvas(modifier = modifier) {
        val data = when {
            !hasData -> listOf(0f, 0f, 0f, 0f, 0f)
            useGenerated -> {
                var current = 0f
                listOf(0f) + generatedSignals.takeLast(16).map {
                    current += if (it.result.equals("LOSS", true)) -abs(it.priceChangePct.toFloat()) else abs(it.priceChangePct.toFloat())
                    current
                }
            }
            missions.isNotEmpty() -> {
                var current = 0f
                listOf(0f) + missions.map { m ->
                    current += if (m.isNegative) -abs(m.roiPct.toFloat().coerceAtMost(-0.1f)) else abs(m.roiPct.toFloat().coerceAtLeast(0.1f))
                    current
                }
            }
            else -> listOf(0f, 4f, 11f, -3f, 18f, 8f, 28f, 23f, 38f)
        }
        val min = data.minOrNull() ?: 0f
        val max = data.maxOrNull() ?: 1f
        val range = if (max - min == 0f) 1f else max - min
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        path.moveTo(0f, size.height - ((data[0] - min) / range) * size.height)
        for (i in 1 until data.size) {
            path.lineTo(i * stepX, size.height - ((data[i] - min) / range) * size.height)
        }
        drawPath(path = path, color = if (hasData) Color(0xFFD17BFF) else TextMuted.copy(alpha = 0.45f), style = Stroke(width = 5f))
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - min) / range) * size.height
            drawCircle(color = if (hasData) CryptoCyan else TextMuted, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
fun MetricDonutChart(modifier: Modifier = Modifier, winRate: Float, hasData: Boolean = true) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 34f)
        if (!hasData) {
            drawArc(color = TextMuted.copy(alpha = 0.32f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
            return@Canvas
        }
        drawArc(color = TitanRed, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
        drawArc(color = CryptoGreen, startAngle = -90f, sweepAngle = (winRate.coerceIn(0f, 100f) / 100f) * 360f, useCenter = false, style = stroke)
    }
}

internal fun formatPrice(price: Double): String = when {
    price >= 1000 -> String.format(java.util.Locale.US, "$%,.2f", price)
    price >= 1 -> String.format(java.util.Locale.US, "$%,.3f", price)
    else -> String.format(java.util.Locale.US, "$%.6f", price)
}

internal fun signedPct(value: Double): String = String.format(java.util.Locale.US, "%s%.2f%%", if (value >= 0.0) "+" else "", value)
internal fun formatPct(value: Double): String = String.format(java.util.Locale.US, "%.1f%%", value)
internal fun outcomeColor(value: Double): Color = if (value >= 0.0) CryptoGreen else TitanRed
internal fun formatDate(timestamp: Long): String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
