package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Utilitarian Tool Palette: Studio Charcoal & Muted Slate (Zero neon, zero gimmick)
val CanvasDeep = Color(0xFF121316)             // Clean neutral dark surface, like VS Code / Android Studio
val CanvasCard = Color(0xFF181A1F)             // Structured tool pane
val CanvasCardElevated = Color(0xFF20232A)     // Elevated utility group
val CanvasCardBorder = Color(0xFF2D3139)       // Crisp 1dp hairline divider
val CanvasCardBorderActive = Color(0xFF454B57) // Focused border

// Editorial & Technical Typography Colors
val MoonWhite = Color(0xFFE6EDF3)              // Crisp reading off-white
val MoonMuted = Color(0xFF8B949E)              // Human readable metadata
val MoonSubtle = Color(0xFF5E6573)             // Subtle hint text

// Restrained, desaturated system accents (No neons)
val CalmSage = Color(0xFF3FB950)               // Standard system green for active/connected state
val CalmSageGlow = Color(0xFF162B1D)
val MoonlightAmber = Color(0xFFD29922)         // Standard warm amber for warnings / timers
val MoonlightAmberGlow = Color(0xFF2C2213)
val SoftIris = Color(0xFF58A6FF)               // Standard clean utilitarian blue
val CrimsonQuiet = Color(0xFFF85149)           // Standard red for error / stop

// Backwards compatibility tokens so existing code compiles smoothly
val DeepMidnight = CanvasDeep
val SurfaceObsidian = CanvasCard
val SurfaceVariantDark = CanvasCardElevated
val SurfaceCard = CanvasCard
val NeonCyan = CalmSage
val NeonCyanVariant = SoftIris
val DopamineMagenta = SoftIris
val DopaminePink = MoonlightAmber
val DreamPurple = SoftIris
val DreamViolet = CanvasCardElevated
val CandlelightAmber = MoonlightAmber
val SunsetOrange = MoonlightAmber
val RestfulEmerald = CalmSage
val SoftHapticCyan = CalmSage
val TextHighContrast = MoonWhite
val TextSecondary = MoonMuted
val TextTertiary = MoonSubtle
val OutlineDark = CanvasCardBorder
