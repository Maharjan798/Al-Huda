package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldSecondary,
    onPrimary = DarkBackground,
    primaryContainer = EmeraldPrimaryDark,
    onPrimaryContainer = GoldLight,
    secondary = GoldAccent,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = IvoryTextOnDark,
    surface = DarkSurface,
    onSurface = IvoryTextOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SubtitleOnDark,
    outline = GoldSecondary.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = GoldSecondary,
    secondary = GoldSecondary,
    onSecondary = EmeraldPrimary,
    background = NaturalBackground,
    onBackground = EmeraldPrimary,
    surface = NaturalSurface,
    onSurface = EmeraldPrimary,
    surfaceVariant = NaturalSurfaceVariant,
    onSurfaceVariant = EmeraldPrimary.copy(alpha = 0.7f),
    outline = NaturalGoldBorder
)

@Composable
fun AlHudaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
