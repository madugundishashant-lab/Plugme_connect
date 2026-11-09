package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PlugmeCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F4FF),
    onPrimaryContainer = Color(0xFF003859),
    secondary = PlugmeCoral,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFECE6),
    onSecondaryContainer = Color(0xFF5D1E10),
    tertiary = PlugmeGold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF7DB),
    onTertiaryContainer = Color(0xFF543C00),
    background = PlugmeBg,
    onBackground = PlugmeText,
    surface = PlugmeSurface,
    onSurface = PlugmeText,
    surfaceVariant = PlugmeSurfaceVariant,
    onSurfaceVariant = PlugmeSub,
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = PlugmeCyan,
    onPrimary = Color.Black,
    secondary = PlugmeCoral,
    onSecondary = Color.Black,
    tertiary = PlugmeGold,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun PlugmeTheme(
    darkTheme: Boolean = false, // Keep light pastel bubble theme as default for the clean glassmorphic design
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
