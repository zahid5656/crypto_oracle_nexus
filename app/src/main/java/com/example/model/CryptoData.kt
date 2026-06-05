package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewsItem(
    val title: String,
    val source: String,
    val timeAgo: String,
    val summary: String,
    val sentiment: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val titleBengali: String? = null,
    val summaryBengali: String? = null,
    val sourceBengali: String? = null,
    val timeAgoBengali: String? = null
)

@JsonClass(generateAdapter = true)
data class DeepInsightItem(
    val coinName: String,
    val coinSymbol: String,
    val direction: String, // "PUMP" or "DUMP"
    val timeframe: String,
    val expectedChangePct: Double,
    val targetPrice: Double,
    val whyEnglish: String,
    val whyBengali: String
)

@JsonClass(generateAdapter = true)
data class SpotSignal(
    val coinName: String,
    val coinSymbol: String,
    val priceSixHoursAgo: Double,
    val currentPrice: Double,
    val projectedPrice: Double,
    val growthPotentialPct: Double,
    val confidencePct: Int,
    val priceTwelveHoursAgo: Double? = null,
    val projectedPriceTwelveHours: Double? = null,
    val growthPotentialTwelveHoursPct: Double? = null,
    val confidenceTwelveHoursPct: Int? = null,
    
    // Crypto Oracle Intelligence Suite Pro Upgrades
    val opportunityScore: Int = 85,
    val confidenceGrade: String = "A",
    val riskGrade: String = "Medium",
    val oracleScore: Int = 84,
    val trendStrength: String = "STRONG BULLISH",
    val volumeStrength: String = "SURGING",
    val momentumStrength: String = "HIGH",
    val liquidityStrength: String = "EXCELLENT",
    val whyThisSignalEnglish: String = "RSI breakout on the hourly chart combined with strong Spot volume inflows. MACD crossover confirms short-term trend reversal.",
    val whyThisSignalBengali: String = "ঘণ্টাভিত্তিক চার্টে আরএসআই ব্রেকআউট এবং শক্তিশালী স্পট ভলিউম প্রবাহ। এমএসিডি ক্রসওভার নিকটস্থ ট্রেন্ড রিভার্সাল নিশ্চিত করে।",
    val invalidationPrice: Double = 0.0,
    val isInvalidated: Boolean = false,
    val healthScore: Int = 88
)

@JsonClass(generateAdapter = true)
data class FuturesSignal(
    val coinName: String,
    val coinSymbol: String,
    val currentPrice: Double,
    val targetPrice: Double, // Projected highest (for Long) or lowest (for Short) in next 6h
    val priceChangePct: Double, // Expected percentage increase (Long) or decrease (Short)
    val probabilityPct: Int, // Confidence percentage
    val isLong: Boolean, // true for Buy Long, false for Sell Short
    val targetPriceTwelveHours: Double? = null,
    val priceChangeTwelveHoursPct: Double? = null,
    val probabilityTwelveHoursPct: Int? = null,
    
    // Crypto Oracle Intelligence Suite Pro Upgrades
    val opportunityScore: Int = 80,
    val confidenceGrade: String = "B+",
    val riskGrade: String = "High",
    val oracleScore: Int = 78,
    val trendStrength: String = "WEAK BULLISH",
    val volumeStrength: String = "MODERATE",
    val momentumStrength: String = "HIGH",
    val liquidityStrength: String = "MEDIUM",
    val whyThisSignalEnglish: String = "Long-squeeze potential liquidations cleared at range low, leading to bullish order block validation under high buy momentum.",
    val whyThisSignalBengali: String = "সীমার মধ্যে সর্বনিম্ন লেভেলে লং-স্কুইজ লিকুইডেশন সম্পন্ন হয়েছে, যার ফলে উচ্চ ক্রয় মোমেন্টামের অধীনে বুলিশ অর্ডার ব্লক সক্রিয় রয়েছে।",
    val invalidationPrice: Double = 0.0,
    val isInvalidated: Boolean = false,
    val healthScore: Int = 75,
    val leverageConservative: Int = 3,
    val leverageBalanced: Int = 5,
    val leverageAggressive: Int = 10,
    val leverageRecommended: Boolean = true
)

@JsonClass(generateAdapter = true)
data class OracleAnalysisResponse(
    val newsList: List<NewsItem>,
    val spotSignals: List<SpotSignal>,
    val futuresLongSignals: List<FuturesSignal>,
    val futuresShortSignals: List<FuturesSignal>,
    val deepInsights: List<DeepInsightItem> = emptyList()
)

data class RadarAlert(
    val id: String,
    val coinSymbol: String,
    val eventType: String, // "VOLUME_EXPLOSION", "BREAKOUT", "MOMENTUM_SURGE", "TREND_REVERSAL"
    val descriptionEnglish: String,
    val descriptionBengali: String,
    val magnitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class Mission(
    val id: String = java.util.UUID.randomUUID().toString(),
    val coinSymbol: String,
    val type: String, // LONG, SHORT
    val marketType: String, // Spot, Futures
    val entryPrice: Double,
    val originalSignalEntry: Double = 0.0,
    val currentPrice: Double,
    val targets: String,
    val stopLoss: String,
    val confidence: Int,
    val aiStatusEnglish: String = "Bullish Momentum Strong\nNo Immediate Risk Detected",
    val aiStatusBengali: String = "বুলিশ মোমেন্টাম শক্তিশালী\nকোন তাৎক্ষণিক ঝুঁকি নেই",
    val isNegative: Boolean = false,
    val startTime: Long = System.currentTimeMillis()
)
