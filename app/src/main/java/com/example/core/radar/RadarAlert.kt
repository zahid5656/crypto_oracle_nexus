package com.example.core.radar

data class RadarAlert(
    val id: String,
    val coinSymbol: String,
    val eventType: String, // "VOLUME_EXPLOSION", "BREAKOUT", "MOMENTUM_SURGE", "TREND_REVERSAL"
    val descriptionEnglish: String,
    val descriptionBengali: String,
    val magnitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
