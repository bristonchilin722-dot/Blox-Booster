package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    onPrimary = BackgroundDark,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = NeonPurple,
    onSecondary = TextPrimary,
    secondaryContainer = NeonPurpleDark,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonGreen,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderAccent,
    outlineVariant = BorderSubtle,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Always use the tailored gaming cyberpunk palette for Blox Booster
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

