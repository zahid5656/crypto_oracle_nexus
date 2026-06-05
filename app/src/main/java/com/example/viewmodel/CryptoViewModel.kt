package com.example.viewmodel

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GeminiClient
import com.example.data.SignalEntity
import com.example.model.OracleAnalysisResponse
import com.example.model.RadarAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface AppScreen {
    object Home : AppScreen
    object Analysis : AppScreen
    object MarketRadar : AppScreen
    object MissionCenter : AppScreen
    object AccuracyCenter : AppScreen
}

sealed interface AnalysisState {
    object Idle : AnalysisState
    data class Analyzing(val statusMessage: String) : AnalysisState
    data class Success(val data: OracleAnalysisResponse) : AnalysisState
    data class Error(val message: String) : AnalysisState
}

class CryptoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val signalDao = database.signalDao()

    // Expose Room Signal History reactively
    val signalHistory = signalDao.getAllSignals().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    // Real-Time telemetry state for robust error handling and visual feedback
    private val _isLiveConnected = MutableStateFlow(false)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private val _livePrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val livePrices: StateFlow<Map<String, Double>> = _livePrices.asStateFlow()

    private val _hasGeminiApiKey = MutableStateFlow(com.example.BuildConfig.GEMINI_API_KEY.isNotEmpty() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")
    val hasGeminiApiKey: StateFlow<Boolean> = _hasGeminiApiKey.asStateFlow()

    // Home feed data (prefilled with fast simulation output so it's ready instantly)
    private val _newsFeedData = MutableStateFlow<OracleAnalysisResponse>(GeminiClient.generateSimulatorData())
    val newsFeedData: StateFlow<OracleAnalysisResponse> = _newsFeedData.asStateFlow()

    // Active sub-category in Prediction Dashboard
    // 0 = Spot Trading, 1 = Futures Long, 2 = Futures Short
    private val _selectedDashboardTab = MutableStateFlow(0)
    val selectedDashboardTab: StateFlow<Int> = _selectedDashboardTab.asStateFlow()

    // Controls whether we attempt direct AI Oracle inference or direct mathematical simulation
    private val _useAiOracle = MutableStateFlow(false)
    val useAiOracle: StateFlow<Boolean> = _useAiOracle.asStateFlow()

    // Short-Term Oracle Module State (Timeframes: 0 = 1m, 1 = 5m, 2 = 15m, 3 = 30m)
    private val _shortTermTimeframe = MutableStateFlow(2) // Default 15m
    val shortTermTimeframe: StateFlow<Int> = _shortTermTimeframe.asStateFlow()

    private val _lastScanTime = MutableStateFlow(System.currentTimeMillis())
    val lastScanTime: StateFlow<Long> = _lastScanTime.asStateFlow()

    // Market Radar live alerts
    private val _radarAlerts = MutableStateFlow<List<RadarAlert>>(emptyList())
    val radarAlerts: StateFlow<List<RadarAlert>> = _radarAlerts.asStateFlow()

    // Current general market regime conditions (Bull, Bear, Sideways, High Volatility, Low Liquidity)
    private val _marketRegime = MutableStateFlow("BULLISH TREND (STABLE VOLATILITY)")
    val marketRegime: StateFlow<String> = _marketRegime.asStateFlow()

    // Current Active Coin details for Advanced Intelligent Telemetry Sheet Dialog (null by default)
    private val _selectedTelemetryAsset = MutableStateFlow<Any?>(null)
    val selectedTelemetryAsset: StateFlow<Any?> = _selectedTelemetryAsset.asStateFlow()

    private val _isBengali = MutableStateFlow(false)
    val isBengali: StateFlow<Boolean> = _isBengali.asStateFlow()

    fun toggleLanguage() {
        _isBengali.value = !_isBengali.value
    }
    
    // Mission Center active missions
    private val _activeMissions = MutableStateFlow<List<com.example.model.Mission>>(emptyList())
    val activeMissions: StateFlow<List<com.example.model.Mission>> = _activeMissions.asStateFlow()

    private val _missionHistory = MutableStateFlow<List<com.example.model.Mission>>(emptyList())
    val missionHistory: StateFlow<List<com.example.model.Mission>> = _missionHistory.asStateFlow()

    fun startMission(mission: com.example.model.Mission) {
        _activeMissions.value = _activeMissions.value + mission
    }

    fun stopMission(missionId: String, isNegativeOverride: Boolean? = null) {
        val mission = _activeMissions.value.find { it.id == missionId } ?: return
        val currentPrice = mission.currentPrice // Simulated exit
        val isLogicallyNegative = isNegativeOverride ?: (if (mission.type == "LONG") currentPrice < mission.entryPrice else currentPrice > mission.entryPrice)
        _activeMissions.value = _activeMissions.value.filter { it.id != missionId }
        _missionHistory.value = _missionHistory.value + mission.copy(isNegative = isLogicallyNegative)
    }

    // Active scan coroutine job reference to prevent race conditions
    private var scannerJob: Job? = null
    private var binanceSyncJob: Job? = null
    private var radarSyncJob: Job? = null

    init {
        createNotificationChannel()
        initHistoricSignals()
        startBinancePriceSync()
        startLiveRadarEngine()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = getApplication<Application>()
            val channel = NotificationChannel(
                "crypto_oracle_alerts",
                "Crypto Signal Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High probability trading alerts from Crypto Oracle Suite"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendLocalAlert(title: String, message: String) {
        try {
            val context = getApplication<Application>()
            val builder = NotificationCompat.Builder(context, "crypto_oracle_alerts")
                .setSmallIcon(android.R.drawable.stat_notify_chat) // safe built-in fallback drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(Random.nextInt(1000, 9999), builder.build())
        } catch (e: Exception) {
            Log.e("CryptoViewModel", "Failed to send local push alert", e)
        }
    }

    private fun initHistoricSignals() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (signalDao.getCount() == 0) {
                    // Populate initial high-end historic record with robust WIN/LOSS distribution (approx ~80% winrate)
                    val sampleCoins = listOf(
                        Triple("Bitcoin", "BTC", "SPOT"),
                        Triple("Ethereum", "ETH", "SPOT"),
                        Triple("Solana", "SOL", "FUTURES_LONG"),
                        Triple("Ripple", "XRP", "FUTURES_SHORT"),
                        Triple("Cardano", "ADA", "FUTURES_SHORT"),
                        Triple("Polkadot", "DOT", "SPOT"),
                        Triple("Dogecoin", "DOGE", "FUTURES_LONG"),
                        Triple("Uniswap", "UNI", "FUTURES_SHORT"),
                        Triple("Arbitrum", "ARB", "SPOT"),
                        Triple("Optimism", "OP", "FUTURES_LONG"),
                        Triple("Render Token", "RNDR", "FUTURES_LONG"),
                        Triple("Aptos", "APT", "FUTURES_SHORT"),
                        Triple("Sui", "SUI", "SPOT"),
                        Triple("Fantom", "FTM", "FUTURES_LONG"),
                        Triple("Chainlink", "LINK", "SPOT")
                    )

                    val historicEntities = mutableListOf<SignalEntity>()
                    val now = System.currentTimeMillis()

                    sampleCoins.forEachIndexed { idx, (name, symbol, type) ->
                        val hoursAgo = (idx + 1) * 8
                        val timestamp = now - (hoursAgo * 60 * 60 * 1000L)
                        val price = if (symbol == "BTC") 65000.0 else if (symbol == "ETH") 3400.0 else 10.0
                        
                        // We simulate 11 wins and 4 losses for 15 signals = 73.3% Win Rate!
                        val isWin = idx != 3 && idx != 7 && idx != 11 && idx != 14
                        val result = if (isWin) "WIN" else "LOSS"
                        val priceChange = if (isWin) (3.5 + (idx * 0.4)) else -(2.0 + (idx * 0.2))
                        val regimes = listOf("BULLISH", "BEARISH", "SIDEWAYS", "ACCUMULATION", "DISTRIBUTION")
                        val regimeAssigned = regimes[idx % regimes.size]

                        historicEntities.add(
                            SignalEntity(
                                coinName = name,
                                coinSymbol = symbol,
                                signalType = type,
                                entryPrice = price * (1.0 - (priceChange / 200.0)),
                                currentPrice = price,
                                targetPrice = price * (1.0 + (priceChange / 100.0)),
                                priceChangePct = priceChange,
                                probabilityPct = 70 + (idx * 2) % 30,
                                timeframe = if (idx % 2 == 0) "6h" else "12h",
                                result = result,
                                isLong = type == "FUTURES_LONG" || type == "SPOT",
                                marketRegime = regimeAssigned,
                                timestamp = timestamp
                            )
                        )
                    }
                    signalDao.insertSignals(historicEntities)
                }
            } catch (e: Exception) {
                Log.e("CryptoViewModel", "Error populating signals history", e)
            }
        }
    }

    private var webSocket: okhttp3.WebSocket? = null

    private fun startBinancePriceSync() {
        binanceSyncJob?.cancel()
        webSocket?.cancel()
        
        val request = okhttp3.Request.Builder()
            .url("wss://stream.binance.com:9443/ws/!miniTicker@arr")
            .build()
            
        val client = okhttp3.OkHttpClient()
        webSocket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                _isLiveConnected.value = true
                Log.d("CryptoViewModel", "Binance WebSocket Connected")
            }
            
            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                try {
                    val jsonArray = org.json.JSONArray(text)
                    val newPrices = _livePrices.value.toMutableMap()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val symbol = obj.getString("s")
                        val price = obj.getDouble("c")
                        newPrices[symbol] = price
                        // Add normalized symbol counterpart
                        if (symbol.endsWith("USDT")) {
                            newPrices[symbol.removeSuffix("USDT")] = price
                        }
                    }
                    _livePrices.value = newPrices
                    
                    val currentNewsFeed = _newsFeedData.value
                    _newsFeedData.value = com.example.data.GeminiClient.updateResponseWithBinancePrices(currentNewsFeed, newPrices)

                    val currentAnalysisState = _analysisState.value
                    if (currentAnalysisState is AnalysisState.Success) {
                        _analysisState.value = AnalysisState.Success(
                            com.example.data.GeminiClient.updateResponseWithBinancePrices(currentAnalysisState.data, newPrices)
                        )
                    }

                    // Update active missions with live prices
                    val updatedMissions = _activeMissions.value.map { mission ->
                        val livePrice = newPrices[mission.coinSymbol + "USDT"] ?: newPrices[mission.coinSymbol]
                        if (livePrice != null) {
                            mission.copy(currentPrice = livePrice)
                        } else {
                            mission
                        }
                    }
                    _activeMissions.value = updatedMissions
                } catch (e: Exception) {
                    // Ignore transient parsing errors on live ticks
                }
            }
            
            override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
                _isLiveConnected.value = false
                Log.e("CryptoViewModel", "WebSocket failure", t)
                // Reconnect after delay
                viewModelScope.launch(Dispatchers.IO) {
                    kotlinx.coroutines.delay(5000)
                    startBinancePriceSync()
                }
            }
            
            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                _isLiveConnected.value = false
            }
        })
    }

    private fun startLiveRadarEngine() {
        radarSyncJob?.cancel()
        radarSyncJob = viewModelScope.launch(Dispatchers.IO) {
            val systemRegimes = listOf(
                "BULLISH MOMENTUM (LOW VOLATILITY)",
                "SIDEWAYS CONSOLIDATION (HIGH LIQUIDITY)",
                "BEARISH BREAKDOWN (HIGH VOLATILITY)",
                "ACCUMULATION RANGE (LOW LIQUIDITY)"
            )
            
            while (true) {
                try {
                    // Update general market regime occasionally
                    if (Random.nextInt(5) == 0) {
                        _marketRegime.value = systemRegimes[Random.nextInt(systemRegimes.size)]
                    }

                    // Generate a new breakout alert
                    val symbols = listOf("BTC", "ETH", "SOL", "XRP", "ADA", "LINK", "PEPE", "WIF")
                    val coin = symbols[Random.nextInt(symbols.size)]
                    val events = listOf(
                        Triple("VOLUME_EXPLOSION", "Volume Surging over +250% on Binance spot book!", "Binance স্পট বুকে ভলিউম +২৫০% এর বেশি বৃদ্ধি পেয়েছে!"),
                        Triple("BREAKOUT", "Asset broke local resistance level with massive speed dynamics!", "সম্পদটি বিশাল গতিবেগ নিয়ে স্থানীয় বাধা লেভেল অতিক্রম করেছে!"),
                        Triple("MOMENTUM_SURGE", "RSI indicator crossed 70 under bullish sentiment pressure!", "বুলিশ সেন্টিমেন্ট চাপের কারণে আরএসআই ৭০ লেভেল অতিক্রম করেছে!"),
                        Triple("TREND_REVERSAL", "MACD crossover on the hourly timeframe triggers immediate buy signal!", "ঘণ্টাভিত্তিক টাইমফ্রেমে এমএসিডি ক্রসওভার তাৎক্ষণিক কেনার সংকেত দেয়!")
                    )
                    
                    val selectedEvent = events[Random.nextInt(events.size)]
                    val magnitude = Random.nextDouble(5.0, 32.5)

                    val newAlert = RadarAlert(
                        id = System.nanoTime().toString(),
                        coinSymbol = coin,
                        eventType = selectedEvent.first,
                        descriptionEnglish = selectedEvent.second,
                        descriptionBengali = selectedEvent.third,
                        magnitude = magnitude
                    )

                    // Post push notification if magnitude is very high (>20)
                    if (magnitude > 22.0) {
                        sendLocalAlert(
                            title = "🚨 RADAR ALERT: $coin ${selectedEvent.first}",
                            message = "${selectedEvent.second} (Magnitude: ${String.format("%.1f", magnitude)}%)"
                        )
                    }

                    val currentList = _radarAlerts.value.toMutableList()
                    currentList.add(0, newAlert)
                    if (currentList.size > 20) {
                        currentList.removeAt(currentList.lastIndex)
                    }
                    _radarAlerts.value = currentList

                } catch (e: Exception) {
                    Log.e("CryptoViewModel", "Error in Live Radar generator", e)
                }
                delay(Random.nextLong(6000, 15000)) // Periodically trigger alerts
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setDashboardTab(index: Int) {
        _selectedDashboardTab.value = index
    }

    fun toggleOracleMode(enabled: Boolean) {
        _useAiOracle.value = enabled
    }

    fun setShortTermTimeframe(index: Int) {
        _shortTermTimeframe.value = index
    }

    fun selectTelemetryAsset(asset: Any?) {
        _selectedTelemetryAsset.value = asset
    }

    /**
     * Simulates technical technical analysis scans, saves generated outputs dynamically
     * to the Signal SQLite Database to preserve history and show winrates!
     */
    fun runScanner() {
        scannerJob?.cancel()
        scannerJob = viewModelScope.launch {
            _analysisState.value = AnalysisState.Idle
            
            val scanningSteps = listOf(
                "Initializing Oracle Quantum core...",
                "Scanning MACD/RSI relative weights...",
                "Calculating liquidations historical heatmaps...",
                "Compiling signal matrix outputs..."
            )

            for (step in scanningSteps) {
                _analysisState.value = AnalysisState.Analyzing(step)
                delay(550)
            }

            try {
                // Fetch dynamic predictions (either AI direct generated or local walk-simulator)
                val results = GeminiClient.getOracleData(useAI = _useAiOracle.value)
                _analysisState.value = AnalysisState.Success(results)
                // Sync latest news feed as well
                _newsFeedData.value = results
                _lastScanTime.value = System.currentTimeMillis()

                // Automatically persist a scanned signal into history to grow historical data live!
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val spotSig = results.spotSignals.firstOrNull()
                        if (spotSig != null) {
                            signalDao.insertSignal(
                                SignalEntity(
                                    coinName = spotSig.coinName,
                                    coinSymbol = spotSig.coinSymbol,
                                    signalType = "SPOT",
                                    entryPrice = spotSig.currentPrice,
                                    currentPrice = spotSig.currentPrice,
                                    targetPrice = spotSig.projectedPrice,
                                    priceChangePct = spotSig.growthPotentialPct,
                                    probabilityPct = spotSig.confidencePct,
                                    timeframe = "6h",
                                    result = if (Random.nextBoolean()) "WIN" else "PENDING",
                                    isLong = true,
                                    marketRegime = _marketRegime.value
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("CryptoViewModel", "Error saving scanned signal to database", e)
                    }
                }

            } catch (e: Exception) {
                _analysisState.value = AnalysisState.Error(e.localizedMessage ?: "Scanner compile failure. Re-initializing...")
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                signalDao.clearHistory()
            } catch (e: Exception) {
                Log.e("CryptoViewModel", "Error purging signal history", e)
            }
        }
    }

    fun archiveSignalOutcome(
        coinName: String,
        coinSymbol: String,
        signalType: String,
        entryPrice: Double,
        currentPrice: Double,
        targetPrice: Double,
        priceChangePct: Double,
        probabilityPct: Int,
        timeframe: String,
        result: String,
        isLong: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                signalDao.insertSignal(
                    SignalEntity(
                        coinName = coinName,
                        coinSymbol = coinSymbol,
                        signalType = signalType,
                        entryPrice = entryPrice,
                        currentPrice = currentPrice,
                        targetPrice = targetPrice,
                        priceChangePct = priceChangePct,
                        probabilityPct = probabilityPct,
                        timeframe = timeframe,
                        result = result,
                        isLong = isLong,
                        marketRegime = _marketRegime.value
                    )
                )
                sendLocalAlert(
                    title = "🎯 SIGNAL ARCHIVED: $coinSymbol",
                    message = "Simulated validation complete. Status: $result! Real-time Win Rate updated."
                )
            } catch (e: Exception) {
                Log.e("CryptoViewModel", "Error archiving signal outcome", e)
            }
        }
    }
}
