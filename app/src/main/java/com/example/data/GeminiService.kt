package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.FuturesSignal
import com.example.model.NewsItem
import com.example.model.DeepInsightItem
import com.example.model.OracleAnalysisResponse
import com.example.model.SpotSignal
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.Request
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// Body representation for Gemini API calls
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun fetchBinancePrices(): Map<String, Double> {
        val request = Request.Builder()
            .url("https://api.binance.com/api/v3/ticker/price")
            .build()
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyMap()
                val bodyString = response.body?.string() ?: return emptyMap()
                val jsonArray = JSONArray(bodyString)
                val prices = mutableMapOf<String, Double>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val symbol = obj.getString("symbol")
                    val price = obj.getDouble("price")
                    prices[symbol] = price
                }
                return prices
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch prices from Binance API", e)
        }
        return emptyMap()
    }

    fun updateResponseWithBinancePrices(
        response: OracleAnalysisResponse,
        prices: Map<String, Double>
    ): OracleAnalysisResponse {
        val updatedSpot = response.spotSignals.map { spot ->
            val livePrice = prices[spot.coinSymbol + "USDT"] ?: prices[spot.coinSymbol] ?: spot.currentPrice
            val newGrowthPct = ((spot.projectedPrice - livePrice) / livePrice) * 100.0
            val newGrowthTwelvePct = spot.projectedPriceTwelveHours?.let { ((it - livePrice) / livePrice) * 100.0 }
            spot.copy(
                currentPrice = livePrice,
                growthPotentialPct = newGrowthPct,
                growthPotentialTwelveHoursPct = newGrowthTwelvePct ?: spot.growthPotentialTwelveHoursPct
            )
        }
        val updatedLong = response.futuresLongSignals.map { fut ->
            val livePrice = prices[fut.coinSymbol + "USDT"] ?: prices[fut.coinSymbol] ?: fut.currentPrice
            val target = fut.targetPrice
            val targetTwelve = fut.targetPriceTwelveHours
            fut.copy(
                currentPrice = livePrice,
                priceChangePct = ((target - livePrice) / livePrice) * 100.0,
                priceChangeTwelveHoursPct = targetTwelve?.let { ((it - livePrice) / livePrice) * 100.0 } ?: fut.priceChangeTwelveHoursPct
            )
        }
        val updatedShort = response.futuresShortSignals.map { fut ->
            val livePrice = prices[fut.coinSymbol + "USDT"] ?: prices[fut.coinSymbol] ?: fut.currentPrice
            val target = fut.targetPrice
            val targetTwelve = fut.targetPriceTwelveHours
            fut.copy(
                currentPrice = livePrice,
                priceChangePct = ((livePrice - target) / livePrice) * 100.0,
                priceChangeTwelveHoursPct = targetTwelve?.let { ((livePrice - it) / livePrice) * 100.0 } ?: fut.priceChangeTwelveHoursPct
            )
        }
        val updatedInsights = response.deepInsights.map { insight ->
            val livePrice = prices[insight.coinSymbol + "USDT"] ?: prices[insight.coinSymbol] ?: insight.targetPrice * 0.9
            val change = if (insight.direction == "PUMP") ((insight.targetPrice - livePrice) / livePrice) * 100.0 else ((livePrice - insight.targetPrice) / livePrice) * 100.0
            insight.copy(expectedChangePct = change)
        }
        return response.copy(
            spotSignals = updatedSpot,
            futuresLongSignals = updatedLong,
            futuresShortSignals = updatedShort,
            deepInsights = updatedInsights
        )
    }

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun sendChatMessage(history: List<GeminiContent>, newMessage: String, isBengali: Boolean): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasValidKey) {
            return if (isBengali) "অনুগ্রহ করে API কী সেট করুন।" else "Please set up your Gemini API key."
        }

        try {
            val systemPrompt = """
                You are the "Crypto Oracle Assistant", an expert AI trading advisor.
                You provide highly accurate, analytical, and professional cryptocurrency market advice.
                Respond in ${if (isBengali) "Bengali (Bangla)" else "English"}.
                Keep responses concise, insightful, and formatted cleanly.
            """.trimIndent()

            val contents = history.toMutableList()
            contents.add(GeminiContent(parts = listOf(GeminiPart(text = newMessage)), role = "user"))

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                generationConfig = GeminiGenerationConfig(temperature = 0.5)
            )

            val response = api.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            return responseText?.trim() ?: if (isBengali) "দুঃখিত, কোনো উত্তর পাওয়া যায়নি।" else "Sorry, no response available."
        } catch (e: Exception) {
            Log.e(TAG, "Chat API failed", e)
            return if (isBengali) "নেটওয়ার্ক ত্রুটি।" else "Network Error."
        }
    }

    /**
     * Fetches Oracle data using the Gemini API if requested and available,
     * otherwise generates realistic dynamic simulation data instantly.
     */
    suspend fun getOracleData(useAI: Boolean): OracleAnalysisResponse {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (useAI && hasValidKey) {
            try {
                val systemPrompt = """
                    You are the "Crypto Signal Oracle", a highly advanced cryptocurrency market analyzer.
                    The current simulation date is May 31, 2026.
                    You MUST return a valid JSON object matching the exact structure of OracleAnalysisResponse.
                    Do not add any Markdown formatting or backticks. Return the JSON object directly.
                    
                    The JSON structure MUST look like this:
                    {
                      "newsList": [
                        {
                          "title": "Crypto News Title",
                          "source": "CoinDesk",
                          "timeAgo": "15m ago",
                          "summary": "Brief explanation.",
                          "sentiment": "BULLISH"
                        }
                      ],
                      "spotSignals": [
                        {
                          "coinName": "Bitcoin",
                          "coinSymbol": "BTC",
                          "priceSixHoursAgo": 65210.0,
                          "currentPrice": 66450.0,
                          "projectedPrice": 69100.0,
                          "growthPotentialPct": 3.99,
                          "confidencePct": 89,
                          "priceTwelveHoursAgo": 64100.0,
                          "projectedPriceTwelveHours": 71200.0,
                          "growthPotentialTwelveHoursPct": 7.15,
                          "confidenceTwelveHoursPct": 85
                        }
                      ],
                      "futuresLongSignals": [
                        {
                           "coinName": "Solana",
                           "coinSymbol": "SOL",
                           "currentPrice": 164.20,
                           "targetPrice": 178.50,
                           "priceChangePct": 8.7,
                           "probabilityPct": 82,
                           "isLong": true,
                           "targetPriceTwelveHours": 189.50,
                           "priceChangeTwelveHoursPct": 15.4,
                           "probabilityTwelveHoursPct": 79
                        }
                      ],
                      "futuresShortSignals": [
                        {
                           "coinName": "Ripple",
                           "coinSymbol": "XRP",
                           "currentPrice": 0.512,
                           "targetPrice": 0.482,
                           "priceChangePct": -5.8,
                           "probabilityPct": 75,
                           "isLong": false,
                           "targetPriceTwelveHours": 0.468,
                           "priceChangeTwelveHoursPct": -8.5,
                           "probabilityTwelveHoursPct": 72
                        }
                      ],
                      "deepInsights": [
                        {
                          "coinName": "Solana",
                          "coinSymbol": "SOL",
                          "direction": "PUMP",
                          "timeframe": "Next 48 Hours",
                          "expectedChangePct": 15.4,
                          "targetPrice": 189.50,
                          "whyEnglish": "A massive surge in Solana DEX volume indications...",
                          "whyBengali": "সোলানা ডেক্স (DEX) ভলিউমে ব্যাপক বৃদ্ধি..."
                        }
                      ]
                    }
                    
                    Generate exactly 3 news items, exactly 3 deep research items in deepInsights, exactly 10 Spot signals, exactly 10 Futures Long signals, and exactly 10 Futures Short signals. Keep the values mathematically coherent. Ensure prices are realistic for today (BTC around ${'$'}65k-${'$'}70k, ETH around ${'$'}3.3k-${'$'}3.7k, SOL around ${'$'}150-${'$'}180).
                """.trimIndent()

                val prompt = "Provide the latest technical indicators and crypto signals dashboard as of May 31, 2026."

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
                    generationConfig = GeminiGenerationConfig(
                        responseMimeType = "application/json",
                        temperature = 0.7
                    )
                )

                val response = api.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!responseText.isNullOrEmpty()) {
                    val cleanedText = cleanJsonString(responseText)
                    val adapter = moshi.adapter(OracleAnalysisResponse::class.java)
                    val parsed = adapter.fromJson(cleanedText)
                    if (parsed != null) {
                        return parsed
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed calling Gemini API, falling back to simulator", e)
            }
        }

        // Return beautiful, high-fidelity mock simulator data
        return generateSimulatorData()
    }

    private fun cleanJsonString(raw: String): String {
        var temp = raw.trim()
        if (temp.startsWith("```json")) {
            temp = temp.removePrefix("```json")
        } else if (temp.startsWith("```")) {
            temp = temp.removePrefix("```")
        }
        if (temp.endsWith("```")) {
            temp = temp.removeSuffix("```")
        }
        return temp.trim()
    }

    /**
     * Local high-fidelity simulation engine with realistic walk functions
     * to keep numbers alive, beautifully structured for Spot and Futures.
     */
    fun generateSimulatorData(): OracleAnalysisResponse {
        val news = listOf(
            NewsItem(
                title = "Institutional Cash Inflow Drives Bitcoin Close to Resistance at \$68k",
                source = "Oracle Financials",
                timeAgo = "12m ago",
                summary = "Active Spot ETFs report net inflows of over \$320M in the past 24 hours, stimulating positive derivative premiums.",
                sentiment = "BULLISH",
                titleBengali = "প্রাতিষ্ঠানিক ফান্ড প্রবাহের কারণে বিটকয়েন \$৬৮k রেজিস্ট্যান্সের কাছাকাছি",
                summaryBengali = "সক্রিয় স্পট ইটিএফ গত ২৪ ঘন্টায় ৩২০ মিলিয়ন ডলারের বেশি নেট প্রবাহের রিপোর্ট করেছে, যা ডেরিভেটিভ প্রিমিয়াম বাড়াচ্ছে।",
                sourceBengali = "ওরাকল ফাইন্যান্সিয়ালস",
                timeAgoBengali = "১২মি আগে"
            ),
            NewsItem(
                title = "Ethereum Gas Fees Drop to 2-Year Low of 3 Gwei Amid Layer-2 Dominance",
                source = "Gas Tracker AI",
                timeAgo = "45m ago",
                summary = "BLOB space optimizations have drastically improved throughput on Base and Arbitrum, relieving L1 execution pressures.",
                sentiment = "NEUTRAL",
                titleBengali = "লেয়ার-২ আধিপত্যের মধ্যে ইথেরিয়ামের গ্যাস ফি ২ বছরের সর্বনিম্ন ৩ Gwei-তে নেমে এসেছে",
                summaryBengali = "ব্লব স্পেস অপ্টিমাইজেশন বেস এবং আরবিট্রামে থ্রুপুটকে নাটকীয়ভাবে উন্নত করেছে, যা L1 সম্পাদন চাপ কমিয়েছে।",
                sourceBengali = "গ্যাস ট্র্যাকার এআই",
                timeAgoBengali = "৪৫মি আগে"
            ),
            NewsItem(
                title = "SEC Retains Focus on Secondary DeFi Assets as Regulatory Deadlock Persists",
                source = "Legal Ledger",
                timeAgo = "2h ago",
                summary = "Market analysts predict local consolidation or mild downward leverage as legal desks digest ongoing litigation proceedings.",
                sentiment = "BEARISH",
                titleBengali = "নিয়ন্ত্রণাধীন অচলাবস্থা অব্যাহত থাকায় এসইসি ডেফি সম্পদের দিকে নজর বজায় রেখেছে",
                summaryBengali = "বাজার বিশ্লেষকরা চলমান মামলা প্রক্রিয়ার মধ্যে স্থানীয় বাজারে মাঝারি মন্থরতা ও কিছু ডাউন-লিভারেজের পূর্বাভাস দেখছেন।",
                sourceBengali = "লিগ্যাল লেজার",
                timeAgoBengali = "২ঘণ্টা আগে"
            ),
            NewsItem(
                title = "Solana Outflow Stabilizes at Pivot Support; Analysts Eye Target Breakout",
                source = "Oracle Financials",
                timeAgo = "3h ago",
                summary = "High liquidity pool volume and constant derivative funding rates are suggesting Solana’s structural base is priming for expansion.",
                sentiment = "BULLISH",
                titleBengali = "সোলানা আউটফ্লো পিভট সাপোর্টে স্থিতিশীল; বিশ্লেষকরা লক্ষ্য ব্রেকআউটের দিকে তাকিয়ে",
                summaryBengali = "উচ্চ তারল্য পুলের ভলিউম এবং ধ্রুবক ডেরিভেটিভ ফান্ডিং রেট ইঙ্গিত দিচ্ছে যে সোলানার কাঠামোগত ভিত্তি বৃদ্ধির জন্য প্রস্তুত হচ্ছে।",
                sourceBengali = "ওরাকল ফাইন্যান্সিয়ালস",
                timeAgoBengali = "৩ঘণ্টা আগে"
            )
        ).shuffled().take(3)

        // Generate dynamic spot targets (10 Coins)
        val coinsSpot = listOf(
            Triple("Bitcoin", "BTC", 66450.0),
            Triple("Ethereum", "ETH", 3485.5),
            Triple("Solana", "SOL", 164.20),
            Triple("Ripple", "XRP", 0.512),
            Triple("Cardano", "ADA", 0.435),
            Triple("Polkadot", "DOT", 6.85),
            Triple("Chainlink", "LINK", 15.42),
            Triple("Avalanche", "AVAX", 34.25),
            Triple("Dogecoin", "DOGE", 0.146),
            Triple("Shiba Inu", "SHIB", 0.0000215)
        )

        // Seed confidence percentages to provide structured descending probability in UI
        val prob6hSeed = listOf(96, 94, 91, 88, 85, 83, 80, 78, 75, 72)
        val prob12hSeed = listOf(94, 92, 90, 87, 84, 82, 79, 77, 74, 71)

        val spotSignals = coinsSpot.mapIndexed { index, (name, symbol, basePrice) ->
            val change6h = 1.2 + (index * 0.35)
            val change12h = 2.5 + (index * 0.65)
            val pSix = basePrice * (1.0 - (change6h / 100.0) * 0.4)
            val pTwelve = basePrice * (1.0 - (change12h / 100.0) * 0.4)
            val opportunityScore = 98 - (index * 3)
            val confidencePct = prob6hSeed[index]
            val grade = when {
                confidencePct >= 92 -> "A+"
                confidencePct >= 88 -> "A"
                confidencePct >= 82 -> "B+"
                confidencePct >= 75 -> "B"
                else -> "C"
            }
            val risk = when {
                index < 3 -> "Low"
                index < 7 -> "Medium"
                else -> "High"
            }

            SpotSignal(
                coinName = name,
                coinSymbol = symbol,
                priceSixHoursAgo = pSix,
                currentPrice = basePrice,
                projectedPrice = basePrice * (1 + change6h / 100.0),
                growthPotentialPct = change6h,
                confidencePct = confidencePct,
                priceTwelveHoursAgo = pTwelve,
                projectedPriceTwelveHours = basePrice * (1 + change12h / 100.0),
                growthPotentialTwelveHoursPct = change12h,
                confidenceTwelveHoursPct = prob12hSeed[index],
                opportunityScore = opportunityScore,
                confidenceGrade = grade,
                riskGrade = risk,
                oracleScore = opportunityScore - 4,
                trendStrength = if (index % 2 == 0) "STRONG BULLISH" else "MODERATE BULLISH",
                volumeStrength = if (index < 4) "SURGING" else "MODERATE",
                momentumStrength = if (index % 3 == 0) "HIGH" else "MEDIUM",
                liquidityStrength = if (index < 5) "EXCELLENT" else "MEDIUM",
                whyThisSignalEnglish = "RSI bullish breakout combined with daily moving averages support confirmation. Accumulation spotted near the local floor.",
                whyThisSignalBengali = "দৈনিক মুভিং এভারেজ সাপোর্ট নিশ্চিতকরণের সাথে আরএসআই বুলিশ ব্রেকআউট। লোকাল ফ্লোরের কাছাকাছি সঞ্চয় দেখা গেছে।",
                invalidationPrice = basePrice * 0.965,
                isInvalidated = false,
                healthScore = 95 - index
            )
        }

        // Generate dynamic Futures Long Targets (10 Coins)
        val longCoins = listOf(
            Triple("Solana", "SOL", 164.20),
            Triple("Render Token", "RNDR", 8.45),
            Triple("NEAR Protocol", "NEAR", 6.12),
            Triple("Floki", "FLOKI", 0.000215),
            Triple("Dogecoin", "DOGE", 0.146),
            Triple("Optimism", "OP", 2.15),
            Triple("Arbitrum", "ARB", 1.05),
            Triple("Fantom", "FTM", 0.88),
            Triple("Pepe", "PEPE", 0.0000142),
            Triple("Sui", "SUI", 1.22)
        )

        val futuresLong = longCoins.mapIndexed { index, (name, symbol, basePrice) ->
            val currentPrice = basePrice * (1 + Random.nextDouble(-0.005, 0.005))
            val percentGain6h = 5.2 + (index * 0.8)
            val percentGain12h = 9.5 + (index * 1.5)
            val opportunityScore = 95 - (index * 2)
            val probability = prob6hSeed[index]
            val grade = when {
                probability >= 90 -> "A+"
                probability >= 83 -> "B+"
                else -> "B"
            }
            val risk = if (index > 6) "Extreme" else if (index > 3) "High" else "Medium"
            
            FuturesSignal(
                coinName = name,
                coinSymbol = symbol,
                currentPrice = currentPrice,
                targetPrice = currentPrice * (1 + (percentGain6h / 100.0)),
                priceChangePct = percentGain6h,
                probabilityPct = probability,
                isLong = true,
                targetPriceTwelveHours = currentPrice * (1 + (percentGain12h / 100.0)),
                priceChangeTwelveHoursPct = percentGain12h,
                probabilityTwelveHoursPct = prob12hSeed[index],
                opportunityScore = opportunityScore,
                confidenceGrade = grade,
                riskGrade = risk,
                oracleScore = opportunityScore - 2,
                trendStrength = "STRONG BULLISH",
                volumeStrength = "SURGING",
                momentumStrength = "HIGH",
                liquidityStrength = "EXCELLENT",
                whyThisSignalEnglish = "Liquidations of late short-sellers triggered an aggressive buy wall. Highly positive funding rate supports continued long momentum.",
                whyThisSignalBengali = "দেরিতে আসা শর্ট-সেলারদের লিকুইডেশন একটি আক্রমণাত্মক ক্রয় প্রাচীর সক্রিয় করেছে। অত্যন্ত ইতিবাচক ফান্ডিং রেট দীর্ঘায়িত মোমেন্টাম সমর্থন করে।",
                invalidationPrice = currentPrice * 0.94,
                isInvalidated = false,
                healthScore = 90 - index,
                leverageConservative = 3,
                leverageBalanced = 5,
                leverageAggressive = 10,
                leverageRecommended = index < 8
            )
        }

        // Generate dynamic Futures Short Targets (10 Coins)
        val shortCoins = listOf(
            Triple("Ripple", "XRP", 0.512),
            Triple("Cardano", "ADA", 0.435),
            Triple("dogwifhat", "WIF", 2.48),
            Triple("Terra Classic", "LUNA", 0.421),
            Triple("Polygon", "MATIC", 0.625),
            Triple("Cosmos", "ATOM", 8.42),
            Triple("TRON", "TRX", 0.118),
            Triple("Uniswap", "UNI", 7.65),
            Triple("Aptos", "APT", 8.85),
            Triple("Starknet", "STRK", 1.12)
        )

        val futuresShort = shortCoins.mapIndexed { index, (name, symbol, basePrice) ->
            val currentPrice = basePrice * (1 + Random.nextDouble(-0.005, 0.005))
            val percentDrop6h = 4.2 + (index * 0.7)
            val percentDrop12h = 8.1 + (index * 1.2)
            val opportunityScore = 92 - (index * 2)
            val probability = prob6hSeed[index]
            val grade = when {
                probability >= 90 -> "A"
                probability >= 83 -> "B+"
                else -> "B-"
            }
            val risk = if (index > 5) "Extreme" else if (index > 2) "High" else "Medium"

            FuturesSignal(
                coinName = name,
                coinSymbol = symbol,
                currentPrice = currentPrice,
                targetPrice = currentPrice * (1 - (percentDrop6h / 100.0)),
                priceChangePct = -percentDrop6h,
                probabilityPct = probability,
                isLong = false,
                targetPriceTwelveHours = currentPrice * (1 - (percentDrop12h / 100.0)),
                priceChangeTwelveHoursPct = -percentDrop12h,
                probabilityTwelveHoursPct = prob12hSeed[index],
                opportunityScore = opportunityScore,
                confidenceGrade = grade,
                riskGrade = risk,
                oracleScore = opportunityScore - 3,
                trendStrength = "STRONG BEARISH",
                volumeStrength = "SURGING",
                momentumStrength = "HIGH",
                liquidityStrength = "HIGH",
                whyThisSignalEnglish = "Heavy sell-offs triggered after rejection at local resistance. Derivative order book is loaded with massive ask wall.",
                whyThisSignalBengali = "স্থানীয় বাধা স্তরে প্রত্যাখ্যানের পরে ভারী বিক্রি শুরু হয়েছিল। ডেরিভেটিভ অর্ডার বুক বিশাল অফার প্রাচীর দিয়ে লোড করা হয়েছে।",
                invalidationPrice = currentPrice * 1.055,
                isInvalidated = false,
                healthScore = 88 - index,
                leverageConservative = 2,
                leverageBalanced = 4,
                leverageAggressive = 8,
                leverageRecommended = index < 9
            )
        }

        // Generate 3 deep research articles
        val deepInsightsList = listOf(
            DeepInsightItem(
                coinName = "Solana",
                coinSymbol = "SOL",
                direction = "PUMP",
                timeframe = "Next 48 Hours",
                expectedChangePct = 15.4,
                targetPrice = 189.50,
                whyEnglish = "A massive surge in Solana DEX trading volume paired with positive derivative funding rates indicates strong bullish momentum. Institutional accumulation at the $160 support level has established a robust base, and key technical indicators like the daily RSI are priming for an imminent breakout above the local resistance.",
                whyBengali = "সোলানা ডেক্স (DEX) ট্রেডিং ভলিউমে ব্যাপক বৃদ্ধি এবং পজিটিভ ডেরিভেটিভ ফান্ডিং রেট জোরালো বুলিশ মোমেন্টাম নির্দেশ করছে। $১৬০ সাপোর্ট স্তরে প্রাতিষ্ঠানিক ক্রয়ের ফলে একটি শক্তিশালী ঘাঁটি তৈরি হয়েছে এবং ডেইলি আরএসআই (RSI)-এর মতো মূল টেকনিক্যাল ইন্ডিকেটরগুলো লোকাল রেজিস্ট্যান্স অতিক্রম করার ইঙ্গিত দিচ্ছে।"
            ),
            DeepInsightItem(
                coinName = "Ripple",
                coinSymbol = "XRP",
                direction = "DUMP",
                timeframe = "Next 24 Hours",
                expectedChangePct = 8.5,
                targetPrice = 0.468,
                whyEnglish = "Continued regulatory uncertainty and a sudden transfer of $80M XRP by whales from private cold wallets to major spot exchanges indicate imminent distribution and selling pressure. From a technical perspective, XRP was rejected at the 200-day EMA and a head-and-shoulders bearish pattern is forming on the 4-hour chart.",
                whyBengali = "চলমান আইনি অনিশ্চয়তা এবং তিমিদের (whales) দ্বারা কোল্ড ওয়ালেট থেকে এক্সচেঞ্জে ৮০ মিলিয়ন ডলার সমমূল্যের XRP স্থানান্তরের ফলে বাজারে বড় ধরণের বিক্রির চাপ তৈরি হতে পারে। টেকনিক্যাল গ্রাফে XRP ২০০-দিনের ইএমএ (EMA) স্পর্শ করে প্রত্যাখ্যাত হয়েছে এবং চার্টে একটি বিয়ারিশ হেড-অ্যান্ড-শোল্ডার প্যাটার্ন তৈরি হচ্ছে।"
            ),
            DeepInsightItem(
                coinName = "Cardano",
                coinSymbol = "ADA",
                direction = "PUMP",
                timeframe = "Next 7 Days",
                expectedChangePct = 18.2,
                targetPrice = 0.514,
                whyEnglish = "Cardano's network upgrade is successfully processing over 120,000 smart contracts, driving increased network utility and transaction throughput. Active wallet addresses have surged by 22% this week, while MACD crossover on the daily level confirms a solid trend reversal from oversold conditions.",
                whyBengali = "কার্ডানোর নতুন নেটওয়ার্ক আপগ্রেড সফলভাবে ১,২০,০০০-এর বেশি স্মার্ট চুক্তি প্রসেস করছে, যা নেটওয়ার্কের ব্যবহার বৃদ্ধি করেছে। চলতি সপ্তাহে সক্রিয় ওয়ালেটের সংখ্যা ২২% বৃদ্ধি পেয়েছে এবং ডেইলি চার্টে এমএসিডি (MACD) বুলিশ ক্রসওভার একটি জোরালো ট্রেন্ড রিভার্সাল নিশ্চিত করেছে।"
            )
        )

        return OracleAnalysisResponse(
            newsList = news,
            spotSignals = spotSignals,
            futuresLongSignals = futuresLong,
            futuresShortSignals = futuresShort,
            deepInsights = deepInsightsList
        )
    }
}
