package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Muted Light Scheme for minimalist aesthetic
private val MutedLightColorScheme = lightColorScheme(
  primary = SageTeal,
  onPrimary = MinimalWhite,
  primaryContainer = SageTealContainer,
  onPrimaryContainer = SageTealOnContainer,
  secondary = Slate800,
  onSecondary = MinimalWhite,
  secondaryContainer = Slate100,
  onSecondaryContainer = Slate900,
  tertiary = MutedAmber,
  onTertiary = MinimalWhite,
  tertiaryContainer = MutedAmberContainer,
  onTertiaryContainer = MutedAmberOnContainer,
  background = MinimalSurface,
  onBackground = Slate900,
  surface = MinimalWhite,
  onSurface = Slate900,
  surfaceVariant = MinimalSurfaceVariant,
  onSurfaceVariant = Slate700,
  outline = MinimalBorder,
  error = MutedCoral,
  errorContainer = MutedCoralContainer,
  onError = MinimalWhite,
  onErrorContainer = MutedCoralOnContainer
)

// Muted Dark Scheme
private val MutedDarkColorScheme = darkColorScheme(
  primary = SageTealMuted,
  onPrimary = Slate950,
  primaryContainer = SageTeal,
  onPrimaryContainer = SageTealContainer,
  secondary = Slate400,
  onSecondary = Slate950,
  secondaryContainer = Slate800,
  onSecondaryContainer = Slate100,
  tertiary = MutedAmberLight,
  onTertiary = Slate950,
  tertiaryContainer = MutedAmberOnContainer,
  onTertiaryContainer = MutedAmberContainer,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate400,
  outline = MinimalBorderDark,
  error = MutedCoralLight,
  errorContainer = MutedCoralOnContainer,
  onError = Slate950,
  onErrorContainer = MutedCoralContainer
)

// Universal rounded corners across all components as requested
val RoundedComponentShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our curated muted palette for consistent minimalist theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) MutedDarkColorScheme else MutedLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = RoundedComponentShapes,
    content = content
  )
}
