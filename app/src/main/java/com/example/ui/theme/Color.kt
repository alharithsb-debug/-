package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Islamic Luxury Emerald & Gold Palette
val IslamicEmeraldDark = Color(0xFF071811)
val IslamicEmeraldDeep = Color(0xFF0D251C)
val IslamicEmeraldCard = Color(0xFF133227)
val IslamicEmeraldCardLight = Color(0xFF1B4033)
val IslamicGoldPrimary = Color(0xFFE5B842)
val IslamicGoldSecondary = Color(0xFFF3CF67)
val IslamicGoldDark = Color(0xFFB88E28)
val IslamicAccentMint = Color(0xFF2DD4BF)
val IslamicSoftWhite = Color(0xFFF5F8F6)
val IslamicMutedGold = Color(0xFFC7B281)
val IslamicMutedGray = Color(0xFF8F9E97)
val IslamicError = Color(0xFFF87171)

// Light Theme Palette
val IslamicLightBackground = Color(0xFFF7F8F4)
val IslamicLightSurface = Color(0xFFFFFFFF)
val IslamicLightCard = Color(0xFFEFF3EB)
val IslamicLightPrimary = Color(0xFF0E4331)
val IslamicLightGold = Color(0xFFB8860B)
val IslamicLightText = Color(0xFF11221B)
val IslamicLightTextMuted = Color(0xFF5A6E64)

// Gradients
val IslamicGoldGradient = Brush.horizontalGradient(
    colors = listOf(IslamicGoldPrimary, IslamicGoldSecondary, IslamicGoldDark)
)

val IslamicEmeraldGradient = Brush.verticalGradient(
    colors = listOf(IslamicEmeraldDeep, IslamicEmeraldDark)
)

val IslamicCardGradient = Brush.linearGradient(
    colors = listOf(IslamicEmeraldCard, IslamicEmeraldDeep)
)
