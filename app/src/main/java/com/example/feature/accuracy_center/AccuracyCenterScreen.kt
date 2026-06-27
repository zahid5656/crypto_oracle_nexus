package com.example.feature.accuracy_center

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.SignalEntity
import com.example.model.Mission
import com.example.ui.theme.*
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun AccuracyCenterScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val rawSignals by viewModel.signalHistory.collectAsState()
    val userMissions by viewModel.missionHistory.collectAsState()
    val activeMissions by viewModel.activeMissions.collectAsState()
    val radarAlerts by viewModel.radarAlerts.collectAsState()
    val marketRegime by viewModel.marketRegime.collectAsState()
    val defaultAiPolicy by viewModel.defaultAiPolicy.collectAsState()
    val defaultSetupName by viewModel.defaultSetupName.collectAsState()
    val recommendedAutoClose by viewModel.recommendedAutoCloseConditions.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedAccTab by remember { mutableStateOf("Generated") }
    var selectedFilterIndex by remember { mutableStateOf(1) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredSignals = remember(rawSignals, selectedFilterIndex) {
        val now = System.currentTimeMillis()
        when (selectedFilterIndex) {
            0 -> rawSignals.filter { now - it.timestamp <= 7L * 24 * 60 * 60 * 1000 }
            1 -> rawSignals.filter { now - it.timestamp <= 30L * 24 * 60 * 60 * 1000 }
            2 -> rawSignals.filter { now - it.timestamp <= 90L * 24 * 60 * 60 * 1000 }
            else -> rawSignals
        }
    }

    val metrics = remember(selectedAccTab, filteredSignals, userMissions) {
        if (selectedAccTab == "Generated") {
            generatedSignalMetrics(filteredSignals)
        } else {
            userMissionMetrics(userMissions)
        }
    }

    val signalCoverage = remember(filteredSignals) {
        val avgProbability = filteredSignals.map { it.probabilityPct }.averageIntOrZero()
        val highConfidence = filteredSignals.count { it.probabilityPct >= 85 }
        val pending = filteredSignals.count { it.result.equals("PENDING", true) }
        Triple(avgProbability, highConfidence, pending)
    }

    val radarCoverage = remember(radarAlerts) {
        val topMagnitude = radarAlerts.maxOfOrNull { it.magnitude } ?: 0.0
        val topEvent = radarAlerts.maxByOrNull { it.magnitude }?.eventType?.replace('_', ' ') ?: "STANDBY"
        val avgMagnitude = radarAlerts.map { it.magnitude }.averageDoubleOrZero()
        Triple(topMagnitude, topEvent, avgMagnitude)
    }

    val guardianCoverage = remember(activeMissions, userMissions, recommendedAutoClose, defaultAiPolicy) {
        val autoCloseCount = (activeMissions + userMissions).count { it.autoCloseEnabled }
        val readyCount = activeMissions.count { it.setupStatus?.contains("READY", true) == true }
        val protectedCount = activeMissions.count { it.roiPct >= 1.0 || it.autoCloseConditions.isNotEmpty() }
        Triple(autoCloseCount + recommendedAutoClose.size, readyCount, protectedCount)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 112.dp)
    ) {
        item {
            StatsHubHeader(
                selectedTab = selectedAccTab,
                onTabSelected = { selectedAccTab = it },
                selectedFilterIndex = selectedFilterIndex,
                onFilterSelected = { selectedFilterIndex = it }
            )
        }

        item {
            StatsSourceStackPanel(
                signalCount = filteredSignals.size,
                signalWinRate = generatedSignalMetrics(filteredSignals).winRate,
                avgProbability = signalCoverage.first,
                radarCount = radarAlerts.size,
                topRadarEvent = radarCoverage.second,
                marketRegime = marketRegime,
                activeMissions = activeMissions.size,
                completedMissions = userMissions.size,
                defaultPolicy = defaultAiPolicy
            )
        }

        item {
            StatsHubSectionHeader(
                title = if (selectedAccTab == "Generated") "SIGNAL PRO OUTCOME COMMAND" else "MISSION CENTER OUTCOME COMMAND",
                subtitle = if (selectedAccTab == "Generated") "System-generated signal results, quality, distribution, and source trace" else "User-accepted mission results, guardian state, ROI, and execution trace"
            )
        }

        item {
            MetricsOverviewGrid(metrics = metrics)
        }

        item {
            InstitutionalWinRatePanel(
                metrics = metrics,
                title = if (selectedAccTab == "Generated") "ESTABLISHED SIGNAL PRO WIN-RATE" else "MISSION CENTER WIN-RATE",
                subtitle = if (selectedAccTab == "Generated") "Generated signal ledger" else "Completed user mission ledger",
                pendingLabel = if (selectedAccTab == "Generated") "Pending Signals" else "Active Missions",
                pendingValue = if (selectedAccTab == "Generated") metrics.pending else activeMissions.size
            )
        }

        item {
            TradeDistributionPanel(metrics = metrics)
        }

        item {
            CategorySpecializedStatsPanel(
                signalCount = filteredSignals.size,
                avgProbability = signalCoverage.first,
                highConfidenceCount = signalCoverage.second,
                pendingSignals = signalCoverage.third,
                radarAlerts = radarAlerts.size,
                radarTopMagnitude = radarCoverage.first,
                radarTopEvent = radarCoverage.second,
                radarAvgMagnitude = radarCoverage.third,
                activeMissions = activeMissions.size,
                completedMissions = userMissions.size,
                readyMissions = guardianCoverage.second,
                protectedMissions = guardianCoverage.third,
                autoCloseRules = guardianCoverage.first,
                defaultSetup = defaultSetupName,
                defaultPolicy = defaultAiPolicy
            )
        }

        item {
            TrialApiCostAndDiagnosisPanel(
                signalCount = filteredSignals.size,
                radarCount = radarAlerts.size,
                activeMissions = activeMissions.size,
                completedMissions = userMissions.size,
                signalLosses = filteredSignals.count { it.result.equals("LOSS", true) },
                missionLosses = userMissions.count { it.isNegative },
                pendingSignals = signalCoverage.third,
                marketRegime = marketRegime
            )
        }

        item {
            val filters = listOf("7D", "30D", "90D", "All")
            PerformanceChartPanel(
                title = "PERFORMANCE CURVE (${filters[selectedFilterIndex]})",
                missions = userMissions,
                generatedSignals = filteredSignals,
                selectedTab = selectedAccTab,
                isEmpty = metrics.isEmpty
            )
        }

        item {
            HistoryHeaderWithReset(
                title = if (selectedAccTab == "Generated") "HISTORICAL SIGNALS RECORD" else "MISSION EVENT HISTORY",
                onResetClick = { showResetDialog = true }
            )
        }

        if (selectedAccTab == "Generated") {
            if (filteredSignals.isEmpty()) {
                item {
                    EmptyStatsState(
                        title = "NO GENERATED SIGNALS IN THIS WINDOW",
                        message = "Signal Pro outcomes will appear here after the local ledger records generated signals."
                    )
                }
            } else {
                items(filteredSignals) { entity ->
                    SignalHistoryCard(entity = entity, isSystemGenerated = true)
                }
            }
        } else {
            if (userMissions.isEmpty()) {
                item {
                    EmptyStatsState(
                        title = "NO COMPLETED USER MISSIONS",
                        message = "Accepted Mission Center outcomes will appear here after missions are closed."
                    )
                }
            } else {
                items(userMissions) { mission ->
                    UserMissionHistoryCard(mission = mission)
                }
            }
        }
    }

    if (showResetDialog) {
        StatsResetDialog(
            onDismiss = { showResetDialog = false },
            onConfirm = {
                scope.launch { viewModel.clearHistory() }
                showResetDialog = false
            }
        )
    }
}

private fun generatedSignalMetrics(signals: List<SignalEntity>): StatsHubMetrics {
    val wins = signals.count { it.result.equals("WIN", true) }
    val losses = signals.count { it.result.equals("LOSS", true) }
    val pending = signals.count { it.result.equals("PENDING", true) }
    val completed = wins + losses
    val winRate = if (completed > 0) wins.toDouble() / completed.toDouble() * 100.0 else 0.0
    val outcomes = signals.filter { it.result.equals("WIN", true) || it.result.equals("LOSS", true) }.map {
        if (it.result.equals("WIN", true)) abs(it.priceChangePct) else -abs(it.priceChangePct)
    }
    return metricFromOutcomes(total = signals.size, wins = wins, losses = losses, pending = pending, completed = completed, outcomes = outcomes)
        .copy(winRate = winRate)
}

private fun userMissionMetrics(missions: List<Mission>): StatsHubMetrics {
    val wins = missions.count { !it.isNegative }
    val losses = missions.count { it.isNegative }
    val completed = wins + losses
    val winRate = if (completed > 0) wins.toDouble() / completed.toDouble() * 100.0 else 0.0
    val outcomes = missions.map { mission ->
        val base = mission.roiPct
        if (mission.isNegative) -abs(base.takeUnless { it == 0.0 } ?: 1.82) else abs(base.takeUnless { it == 0.0 } ?: 2.45)
    }
    return metricFromOutcomes(total = missions.size, wins = wins, losses = losses, pending = 0, completed = completed, outcomes = outcomes)
        .copy(winRate = winRate)
}

private fun metricFromOutcomes(
    total: Int,
    wins: Int,
    losses: Int,
    pending: Int,
    completed: Int,
    outcomes: List<Double>
): StatsHubMetrics {
    val profit = outcomes.sum()
    val best = outcomes.maxOrNull() ?: 0.0
    val worst = outcomes.minOrNull() ?: 0.0
    val average = outcomes.averageDoubleOrZero()
    val winRate = if (completed > 0) wins.toDouble() / completed.toDouble() * 100.0 else 0.0
    return StatsHubMetrics(
        total = total,
        wins = wins,
        losses = losses,
        pending = pending,
        winRate = winRate,
        profit = profit,
        best = best,
        worst = worst,
        average = average,
        completed = completed,
        isEmpty = total == 0 || completed == 0
    )
}

private fun List<Double>.averageDoubleOrZero(): Double = if (isEmpty()) 0.0 else average()
private fun List<Int>.averageIntOrZero(): Double = if (isEmpty()) 0.0 else average()

@Composable
private fun StatsHubHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    selectedFilterIndex: Int,
    onFilterSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "STATS HUB",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "TITAN outcome intelligence center",
                fontSize = 10.sp,
                color = CryptoCyan,
                letterSpacing = 1.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(24.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatsSegmentButton(
                text = "GENERATED SIGNAL",
                selected = selectedTab == "Generated",
                onClick = { onTabSelected("Generated") },
                modifier = Modifier.weight(1f)
            )
            StatsSegmentButton(
                text = "USER ACTIVITY",
                selected = selectedTab == "User",
                onClick = { onTabSelected("User") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0F18), RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("7D", "30D", "90D", "ALL").forEachIndexed { idx, label ->
                StatsSegmentButton(
                    text = label,
                    selected = selectedFilterIndex == idx,
                    onClick = { onFilterSelected(idx) },
                    modifier = Modifier.weight(1f),
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun StatsSegmentButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 8.dp else 20.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(CryptoCyan.copy(alpha = 0.28f), AccentGold.copy(alpha = 0.18f)))
                else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .border(
                1.dp,
                if (selected) CryptoCyan.copy(alpha = 0.9f) else Color.Transparent,
                RoundedCornerShape(if (compact) 8.dp else 20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = if (compact) 9.dp else 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (compact) 11.sp else 10.sp,
            fontWeight = FontWeight.Black,
            color = if (selected) TextPrimary else TextSecondary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            letterSpacing = 0.6.sp
        )
    }
}
