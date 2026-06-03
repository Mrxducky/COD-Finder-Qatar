package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TalabatOrange,
    onPrimary = Color.White,
    primaryContainer = TalabatOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = TalabatOrange,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = TextLight,
    surface = SurfaceDarkElevated,
    onSurface = TextLight,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextMuted,
    error = AlertDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = TalabatOrange,
    onPrimary = Color.White,
    primaryContainer = TalabatOrangeLight,
    onPrimaryContainer = TalabatOrangeDark,
    secondary = TalabatOrange,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF1F5F9), // Light Slate Gray
    onSurfaceVariant = TextMuted,
    error = AlertDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
