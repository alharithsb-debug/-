package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val IslamicDarkColorScheme = darkColorScheme(
    primary = IslamicGoldPrimary,
    onPrimary = IslamicEmeraldDark,
    primaryContainer = IslamicEmeraldCard,
    onPrimaryContainer = IslamicGoldSecondary,
    secondary = IslamicAccentMint,
    onSecondary = IslamicEmeraldDark,
    secondaryContainer = IslamicEmeraldCardLight,
    onSecondaryContainer = IslamicSoftWhite,
    tertiary = IslamicGoldSecondary,
    onTertiary = IslamicEmeraldDark,
    background = IslamicEmeraldDark,
    onBackground = IslamicSoftWhite,
    surface = IslamicEmeraldDeep,
    onSurface = IslamicSoftWhite,
    surfaceVariant = IslamicEmeraldCard,
    onSurfaceVariant = IslamicMutedGold,
    outline = IslamicMutedGold.copy(alpha = 0.4f),
    error = IslamicError,
    onError = Color.White
)

val IslamicLightColorScheme = lightColorScheme(
    primary = IslamicLightPrimary,
    onPrimary = Color.White,
    primaryContainer = IslamicLightCard,
    onPrimaryContainer = IslamicLightPrimary,
    secondary = IslamicLightGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1EADA),
    onSecondaryContainer = IslamicLightGold,
    tertiary = IslamicLightGold,
    onTertiary = Color.White,
    background = IslamicLightBackground,
    onBackground = IslamicLightText,
    surface = IslamicLightSurface,
    onSurface = IslamicLightText,
    surfaceVariant = IslamicLightCard,
    onSurfaceVariant = IslamicLightTextMuted,
    outline = Color(0xFFD2DCD6),
    error = IslamicError,
    onError = Color.White
)

@Composable
fun QuranAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IslamicDarkColorScheme else IslamicLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QuranTypography,
        content = content
    )
}
