package com.levelup.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary          = AccentBlue,
    onPrimary        = TextPrimary,
    primaryContainer = SurfaceCard,
    secondary        = AccentGold,
    onSecondary      = Background,
    background       = Background,
    surface          = Surface,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline          = Border,
    error            = AccentRed,
    onError          = TextPrimary
)

@Composable
fun LevelUpTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography,
        content     = content
    )
}
