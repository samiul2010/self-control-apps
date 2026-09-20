package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = FocusTeal,
  onPrimary = Color(0xFF003828),
  primaryContainer = FocusTealDark,
  onPrimaryContainer = FocusTealLight,
  secondary = CyanGlow,
  onSecondary = Color(0xFF003544),
  tertiary = AmberAlert,
  onTertiary = Color(0xFF432C00),
  background = SlateDarkBackground,
  onBackground = TextPrimaryDark,
  surface = SlateDarkSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = SlateDarkSurfaceVariant,
  onSurfaceVariant = TextSecondaryDark,
  error = CoralWarning,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = FocusTealDark,
  onPrimary = Color.White,
  primaryContainer = FocusTealLight,
  onPrimaryContainer = Color(0xFF003828),
  secondary = CyanGlow,
  onSecondary = Color.White,
  tertiary = AmberAlert,
  onTertiary = Color(0xFF432C00),
  background = SlateLightBackground,
  onBackground = TextPrimaryLight,
  surface = SlateLightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = SlateLightSurfaceVariant,
  onSurfaceVariant = TextSecondaryLight,
  error = CoralWarning,
  onError = Color.White
)

@Composable
fun FocusLockTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Keep alias for compatibility if needed
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = FocusLockTheme(darkTheme, dynamicColor, content)

