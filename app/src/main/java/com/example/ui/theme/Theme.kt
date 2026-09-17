package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MoonWhite,
    onPrimary = CanvasDeep,
    primaryContainer = CanvasCardElevated,
    onPrimaryContainer = MoonWhite,
    secondary = CalmSage,
    onSecondary = CanvasDeep,
    secondaryContainer = CalmSageGlow,
    onSecondaryContainer = CalmSage,
    tertiary = MoonlightAmber,
    onTertiary = CanvasDeep,
    background = CanvasDeep,
    onBackground = MoonWhite,
    surface = CanvasCard,
    onSurface = MoonWhite,
    surfaceVariant = CanvasCardElevated,
    onSurfaceVariant = MoonMuted,
    outline = CanvasCardBorder,
    outlineVariant = CanvasCardBorderActive
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
