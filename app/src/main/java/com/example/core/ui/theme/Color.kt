package com.example.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized TITAN ORACLE color primitives.
 * Existing screens may consume these directly or through the legacy
 * com.example.ui.theme compatibility bridge.
 */
object TitanOracleColors {
    val LauncherFloatingBackground = Color(0xFF080B11)

    val DarkBackground = Color(0xFF0F0F12)
    val DarkSurface = Color(0xFF16161E)
    val DarkSurfaceVariant = Color(0xFF22222E)
    val CardBackground = Color(0xFF111112)
    val CardElevatedBackground = Color(0xFF1A1A22)

    val CyberCyan = Color(0xFF06B6D4)
    val CyberBlue = Color(0xFF32ADE6)
    val AccentGold = Color(0xFFFFD60A)
    val WarningGold = Color(0xFFFFD60A)
    val InstitutionalOrange = Color(0xFFFF7A1A)

    val SuccessLongGreen = Color(0xFF0ECB81)
    val InstitutionalGreen = SuccessLongGreen
    val DangerShortRed = Color(0xFFFF453A)
    val DangerRedContainer = Color(0x1AFF453A)
    val DangerRedText = DangerShortRed

    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFF9CA3AF)
    val TextMuted = Color(0xFF6B7280)
    val BorderColor = Color(0xFF2A2A38)
}

val TitanLauncherFloatingBackground = TitanOracleColors.LauncherFloatingBackground
val TitanDarkBackground = TitanOracleColors.DarkBackground
val TitanDarkSurface = TitanOracleColors.DarkSurface
val TitanDarkSurfaceVariant = TitanOracleColors.DarkSurfaceVariant
val TitanCardBackground = TitanOracleColors.CardBackground
val TitanCardElevatedBackground = TitanOracleColors.CardElevatedBackground
val TitanCyberCyan = TitanOracleColors.CyberCyan
val TitanCyberBlue = TitanOracleColors.CyberBlue
val TitanAccentGold = TitanOracleColors.AccentGold
val TitanWarningGold = TitanOracleColors.WarningGold
val TitanInstitutionalOrange = TitanOracleColors.InstitutionalOrange
val TitanSuccessLongGreen = TitanOracleColors.SuccessLongGreen
val TitanInstitutionalGreen = TitanOracleColors.InstitutionalGreen
val TitanDangerShortRed = TitanOracleColors.DangerShortRed
val TitanDangerRedContainer = TitanOracleColors.DangerRedContainer
val TitanDangerRedText = TitanOracleColors.DangerRedText
val TitanTextPrimary = TitanOracleColors.TextPrimary
val TitanTextSecondary = TitanOracleColors.TextSecondary
val TitanTextMuted = TitanOracleColors.TextMuted
val TitanBorderColor = TitanOracleColors.BorderColor

fun titanSetupStatusColor(status: String?): Color {
    return when (status?.uppercase() ?: "UNKNOWN") {
        "READY" -> TitanSuccessLongGreen
        "INCOMPLETE SETUP" -> TitanAccentGold
        "INVALID", "INVALID / HIGH RISK" -> TitanDangerShortRed
        else -> TitanTextMuted
    }
}
