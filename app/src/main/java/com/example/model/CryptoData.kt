package com.example.model

import com.squareup.moshi.JsonClass
import kotlin.math.abs
import kotlin.math.max
import java.util.Locale

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

    // User locked entry. This is locked only after the user confirms mission activation.
    val entryPrice: Double,

    // Immutable original signal entry. Used for historical signal validation.
    val originalSignalEntry: Double = 0.0,

    // Live market price. This can update continuously from Binance.
    val currentPrice: Double,

    // Existing display fields preserved.
    val targets: String,
    val stopLoss: String,
    val confidence: Int,
    val aiStatusEnglish: String = "Bullish Momentum Strong\nNo Immediate Risk Detected",
    val aiStatusBengali: String = "বুলিশ মোমেন্টাম শক্তিশালী\nকোন তাৎক্ষণিক ঝুঁকি নেই",
    val isNegative: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),

    // Titan Crypto Oracle Nexus mission intelligence fields.
    val generatedTime: Long = System.currentTimeMillis(),
    val marketRegime: String = "UNKNOWN",
    val signalTimeframe: String = "6h",
    val exitPrice: Double? = null,
    val exitReason: String? = null,
    val closedTime: Long? = null,
    val finalAnalysisEnglish: String? = null,
    val finalAnalysisBengali: String? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val target: String? = null,
    val tp1: String? = null,
    val tp2: String? = null,
    val tp3: String? = null,
    val manualStopLoss: String? = null, // Used as SL1
    val sl2: String? = null,
    val leverage: String? = null,
    val positionSize: String? = null,
    val riskProfile: String? = null,
    val copilotMode: String? = null,
    val executionMode: String? = null,
    val autoCloseEnabled: Boolean = false,
    val autoCloseConditions: List<String> = emptyList(),
    val conditionValidity: String? = null,
    val conditionInvalidReason: String? = null,
    val setupMode: String? = null, // "RECOMMENDED SETUP", "CUSTOM SETUP-1", "CUSTOM SETUP-2"
    val setupRemark: String? = null,
    val setupStatus: String? = null,
    val setupRiskReward: String? = null,
    val missionHistoryLog: List<String> = emptyList()
) {
    val userLockedEntry: Double
        get() = entryPrice

    val signalEntryForValidation: Double
        get() = if (originalSignalEntry > 0.0) originalSignalEntry else entryPrice

    val roiPct: Double
        get() {
            if (entryPrice <= 0.0) return 0.0
            return if (type.equals("SHORT", ignoreCase = true)) {
                ((entryPrice - currentPrice) / entryPrice) * 100.0
            } else {
                ((currentPrice - entryPrice) / entryPrice) * 100.0
            }
        }

    val isProfitable: Boolean
        get() = roiPct >= 0.0

    val missionDurationMs: Long
        get() = (closedTime ?: System.currentTimeMillis()) - startTime

    val tradeHealthScore: Int
        get() {
            val confidenceWeight = confidence.coerceIn(0, 100)
            val roiWeight = (50 + (roiPct * 4)).toInt().coerceIn(0, 100)
            val drawdownPenalty = if (roiPct < -3.0) 18 else if (roiPct < -1.5) 8 else 0
            return ((confidenceWeight * 0.55) + (roiWeight * 0.45)).toInt().coerceIn(0, 100) - drawdownPenalty
        }

    val riskLevel: String
        get() = when {
            tradeHealthScore >= 82 && roiPct >= 0 -> "LOW"
            tradeHealthScore >= 62 -> "MODERATE"
            tradeHealthScore >= 42 -> "ELEVATED"
            else -> "HIGH"
        }

    val currentTrend: String
        get() = when {
            roiPct >= 4.0 -> if (type.equals("SHORT", ignoreCase = true)) "Strong Bearish Follow-Through" else "Strong Bullish Continuation"
            roiPct >= 1.0 -> "Momentum Still Healthy"
            roiPct > -1.0 -> "Structure Neutral"
            roiPct > -3.0 -> "Momentum Weakening"
            else -> "Risk Increasing"
        }

    val tp1Probability: Int
        get() = (confidence + roiPct * 3).toInt().coerceIn(5, 95)

    val tp2Probability: Int
        get() = (confidence - 8 + roiPct * 2).toInt().coerceIn(5, 92)

    val tp3Probability: Int
        get() = (confidence - 16 + roiPct).toInt().coerceIn(5, 88)

    val guardianRecommendationEnglish: String
        get() = when {
            roiPct >= 5.0 -> "Profit zone reached. Consider partial profit while monitoring momentum."
            roiPct >= 2.0 && tradeHealthScore >= 75 -> "Hold Position. Momentum remains healthy and structure is still favorable."
            roiPct >= 0.0 && tradeHealthScore >= 60 -> "Stay Patient. Current structure remains acceptable, but monitor volatility."
            roiPct < -4.0 -> "Risk Increasing. Review position and consider profit protection or exit planning."
            roiPct < -2.0 -> "Watch Carefully. Momentum weakening and downside risk is increasing."
            else -> "Monitor Position. No critical change detected."
        }

    val guardianRecommendationBengali: String
        get() = when {
            roiPct >= 5.0 -> "প্রফিট জোনে পৌঁছেছে। মোমেন্টাম পর্যবেক্ষণ করে আংশিক প্রফিট বিবেচনা করা যেতে পারে।"
            roiPct >= 2.0 && tradeHealthScore >= 75 -> "পজিশন ধরে রাখা যেতে পারে। মোমেন্টাম এখনো স্বাস্থ্যকর এবং স্ট্রাকচার অনুকূল।"
            roiPct >= 0.0 && tradeHealthScore >= 60 -> "ধৈর্য ধরে পর্যবেক্ষণ করুন। বর্তমান স্ট্রাকচার গ্রহণযোগ্য, তবে ভোলাটিলিটি খেয়াল রাখুন।"
            roiPct < -4.0 -> "ঝুঁকি বাড়ছে। পজিশন রিভিউ করুন এবং প্রফিট প্রটেকশন বা এক্সিট প্ল্যান বিবেচনা করুন।"
            roiPct < -2.0 -> "সতর্কভাবে পর্যবেক্ষণ করুন। মোমেন্টাম দুর্বল হচ্ছে এবং ডাউনসাইড ঝুঁকি বাড়ছে।"
            else -> "পজিশন পর্যবেক্ষণ করুন। কোনো গুরুত্বপূর্ণ পরিবর্তন ধরা পড়েনি।"
        }

    fun formattedRoi(): String = String.format(Locale.US, "%+.2f%%", roiPct)

    fun closedCopy(reason: String, finalPrice: Double = currentPrice): Mission {
        val finalRoi = if (entryPrice <= 0.0) 0.0 else if (type.equals("SHORT", ignoreCase = true)) {
            ((entryPrice - finalPrice) / entryPrice) * 100.0
        } else {
            ((finalPrice - entryPrice) / entryPrice) * 100.0
        }
        val resultText = if (finalRoi >= 0.0) "Mission closed with positive ROI." else "Mission closed with negative ROI. Review signal conditions and risk behavior."
        return copy(
            currentPrice = finalPrice,
            exitPrice = finalPrice,
            exitReason = reason,
            closedTime = System.currentTimeMillis(),
            isNegative = finalRoi < 0.0,
            finalAnalysisEnglish = resultText,
            finalAnalysisBengali = if (finalRoi >= 0.0) "মিশন পজিটিভ ROI সহ বন্ধ হয়েছে।" else "মিশন নেগেটিভ ROI সহ বন্ধ হয়েছে। সিগনাল কন্ডিশন এবং ঝুঁকি আচরণ রিভিউ করুন।",
            lastUpdated = System.currentTimeMillis()
        )
    }
}

data class CustomSetupProfile(
    val target: String = "",
    val tp1: String = "",
    val tp2: String = "",
    val tp3: String = "",
    val stopLoss: String = "", // SL1
    val sl2: String = "",
    val leverage: String = "",
    val positionSize: String = "",
    val riskProfile: String = "",
    val remark: String = "",
    val autoCloseConditions: List<String> = emptyList()
)
