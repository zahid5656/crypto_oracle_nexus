package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0F0F12)
val DarkSurface = Color(0xFF16161E)
val DarkSurfaceVariant = Color(0xFF22222E)
val CryptoGreen = Color(0xFF10B981) // High upward momentum green
val CryptoRed = Color(0xFFDC2626)       // Premium Blood Red (deeper tone)
val CryptoRedContainer = Color(0xFF2D0E12) // Deepest dark wine/blood red for safe container backgrounds
val CryptoRedText = Color(0xFFFF647C)    // Clear eye-safe legible pinkish-red text for maximum contrast
val CryptoCyan = Color(0xFF06B6D4)  // Technical indicators, confidence metrics
val AccentGold = Color(0xFFF59E0B)  // Highlights & Bitcoin standard gold
val TextPrimary = Color(0xFFF3F4F6)
val TextSecondary = Color(0xFF9CA3AF)
val TextMuted = Color(0xFF6B7280)
val BorderColor = Color(0xFF2A2A38)

fun setupStatusColor(status: String?): Color {
    return when (status?.uppercase() ?: "UNKNOWN") {
        "READY" -> CryptoGreen
        "INCOMPLETE SETUP" -> AccentGold
        "INVALID", "INVALID / HIGH RISK" -> CryptoRed
        else -> TextMuted
    }
}
