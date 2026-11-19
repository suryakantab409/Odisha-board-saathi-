package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Navy800,
    onPrimary = Color.White,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy900,
    secondary = Saffron500,
    onSecondary = Color.White,
    secondaryContainer = Saffron100,
    onSecondaryContainer = Saffron600,
    tertiary = Indigo600,
    onTertiary = Color.White,
    tertiaryContainer = Indigo100,
    onTertiaryContainer = Navy900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    outlineVariant = Slate200,
    error = Crimson600,
    onError = Color.White,
    errorContainer = Crimson100,
    onErrorContainer = Crimson600
)

private val DarkColorScheme = darkColorScheme(
    primary = Saffron500,
    onPrimary = Color.White,
    primaryContainer = Navy700,
    onPrimaryContainer = Color.White,
    secondary = Saffron500,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Saffron100,
    tertiary = Indigo100,
    onTertiary = Navy900,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate200,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = Crimson600,
    onError = Color.White,
    errorContainer = Color(0xFF4C1111),
    onErrorContainer = Crimson100
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BoardSaathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
