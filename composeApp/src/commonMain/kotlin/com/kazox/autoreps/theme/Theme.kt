package com.kazox.autoreps.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnEverything,
    secondary = Secondary,
    onSecondary = OnEverything,
    background = BackgroundDark,
    onBackground = OnEverything,
    surface = SurfaceDark,
    onSurface = OnEverything,
    error = Error,
    onError = OnEverything
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnEverything,
    secondary = Secondary,
    onSecondary = OnEverything,
    background = BackgroundLight,
    onBackground = OnEverythingLight,
    surface = SurfaceLight,
    onSurface = OnEverythingLight,
    error = Error,
    onError = OnEverything
)

val ColorScheme.success: Color
    @Composable
    get() = Success

val ColorScheme.onSuccess: Color
    @Composable
    get() = OnEverything

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {

    val darkTheme = isSystemInDarkTheme()

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}