package com.example.rentlog.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CalmDarkPrimary,
    secondary = CalmDarkSecondary,
    tertiary = CalmDarkPrimary,
    background = CalmDarkBackground,
    surface = CalmDarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = CalmDarkTextPrimary,
    onSurface = CalmDarkTextPrimary,
    primaryContainer = CalmDarkSecondary,
    onPrimaryContainer = Color.White,
    surfaceVariant = CalmDarkSurface,
    onSurfaceVariant = CalmDarkTextSecondary,
    outline = Color(0xFF1B2623)
)

private val LightColorScheme = lightColorScheme(
    primary = CalmPrimary,
    secondary = CalmSecondary,
    tertiary = CalmPrimary,
    background = CalmBackground,
    surface = CalmSurface,
    onPrimary = Color.White,
    onSecondary = CalmPrimary,
    onTertiary = Color.White,
    onBackground = CalmTextPrimary,
    onSurface = CalmTextPrimary,
    primaryContainer = CalmPrimary,
    onPrimaryContainer = Color.White,
    surfaceVariant = CalmAccent,
    onSurfaceVariant = CalmTextSecondary,
    outline = CalmOutline
)

@Composable
fun RentLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
