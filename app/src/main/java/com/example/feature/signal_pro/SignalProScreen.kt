package com.example.feature.signal_pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.theme.DarkBackground
import com.example.viewmodel.AnalysisState
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider

val LocalExpandedAsset = compositionLocalOf<MutableState<String?>> { error("No expanded asset provided") }

@Composable
fun SignalProScreen(
    viewModel: CryptoViewModel,
    modifier: Modifier = Modifier
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val expandedAsset = remember { mutableStateOf<String?>(null) }

    androidx.activity.compose.BackHandler {
        viewModel.navigateTo(AppScreen.Home)
    }

    CompositionLocalProvider(LocalExpandedAsset provides expandedAsset) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            when (val state = analysisState) {
                is AnalysisState.Idle -> {
                    val initData by viewModel.newsFeedData.collectAsState()
                    PredictionDashboard(
                        data = initData,
                        viewModel = viewModel
                    )
                }
                is AnalysisState.Analyzing -> {
                    AnalyzingTelemetryScreen(stepMessage = state.statusMessage)
                }
                is AnalysisState.Success -> {
                    PredictionDashboard(
                        data = state.data,
                        viewModel = viewModel
                    )
                }
                is AnalysisState.Error -> {
                    ScannerErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.runScanner() },
                        onGoBack = { viewModel.navigateTo(AppScreen.Home) }
                    )
                }
            }
        }
    }
}
