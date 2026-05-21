package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Let's configure a custom dark color scheme matching our premium neon look
private val CyberColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonViolet,
    tertiary = NeonPink,
    background = CyberBackground,
    surface = CyberCard,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = CyberCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode for premium sci-fi feel
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our brand neon look
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
