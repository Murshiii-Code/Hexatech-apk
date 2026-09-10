package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkBlueColorScheme =
  darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = DarkBlueSurfaceVariant,
    onPrimaryContainer = AccentCyan,
    secondary = AccentCyan,
    onSecondary = DarkBlueBackground,
    secondaryContainer = DarkBlueCard,
    onSecondaryContainer = TextPrimary,
    background = DarkBlueBackground,
    onBackground = TextPrimary,
    surface = DarkBlueSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkBlueSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBlueBorder,
    outlineVariant = DarkBlueDivider,
    error = RiskHigh,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Enforce consistent enterprise dark-blue visual identity
  MaterialTheme(
    colorScheme = DarkBlueColorScheme,
    typography = Typography,
    content = content
  )
}

