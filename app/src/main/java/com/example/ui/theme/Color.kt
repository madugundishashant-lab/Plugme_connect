package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PlugmeBg = Color(0xFFF5F7FB)
val PlugmeSurface = Color(0xFFFFFFFF)
val PlugmeSurfaceVariant = Color(0xFFF0F3FA)

val PlugmeCyan = Color(0xFF00AAFF)
val PlugmeCoral = Color(0xFFFF7A59)
val PlugmeGold = Color(0xFFFFB800)
val PlugmeGreen = Color(0xFF10B981)

val PlugmeText = Color(0xFF1A1D29)
val PlugmeSub = Color(0xFF6B7280)
val PlugmeDivider = Color(0xFFE5E7EB)

val PlugmeBubble1 = Color(0xFFE3F2FD)
val PlugmeBubble2 = Color(0xFFFFF3E0)

// 6 Pastel cycle colors for contact cards & bubble avatars
val PastelCycles = listOf(
    Color(0xFFE3F2FD), // Soft Sky
    Color(0xFFFFF3E0), // Soft Peach
    Color(0xFFF3E8FF), // Soft Lavender
    Color(0xFFE8F5E9), // Soft Mint
    Color(0xFFFCE4EC), // Soft Rose
    Color(0xFFE0F2F1)  // Soft Aqua
)

val PastelCycleBorders = listOf(
    Color(0xFF90CAF9),
    Color(0xFFFFCC80),
    Color(0xFFCE93D8),
    Color(0xFFA5D6A7),
    Color(0xFFF48FB1),
    Color(0xFF80CBC4)
)

// Signature Gradient: Cyan -> Coral
val SignatureGradient = Brush.horizontalGradient(
    colors = listOf(PlugmeCyan, PlugmeCoral)
)

val SignatureVerticalGradient = Brush.verticalGradient(
    colors = listOf(PlugmeCyan, PlugmeCoral)
)

val AvatarGradients = listOf(
    listOf(Color(0xFF00AAFF), Color(0xFF0077FF)),
    listOf(Color(0xFFFF7A59), Color(0xFFFF5252)),
    listOf(Color(0xFFFFB800), Color(0xFFFF8800)),
    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
    listOf(Color(0xFF10B981), Color(0xFF059669)),
    listOf(Color(0xFFEC4899), Color(0xFFDB2777))
)
