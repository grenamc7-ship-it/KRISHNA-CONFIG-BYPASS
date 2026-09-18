package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme = darkColorScheme(
  primary = BloodRedPrimary,
  onPrimary = TextWhite,
  primaryContainer = BloodRedDeep,
  onPrimaryContainer = BloodRedGlow,
  secondary = NeonGreen,
  onSecondary = CyberDarkBg,
  secondaryContainer = NeonGreenMuted,
  onSecondaryContainer = NeonGreenBright,
  tertiary = NeonGreenBright,
  background = CyberDarkBg,
  onBackground = TextWhite,
  surface = CyberCardGlass,
  onSurface = TextWhite,
  surfaceVariant = CyberCardGlassLight,
  onSurfaceVariant = TextMuted,
  error = StatusError,
  onError = TextWhite,
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CyberColorScheme,
    typography = Typography,
    content = content
  )
}

