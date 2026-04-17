package com.example.rentlog.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class ExtendedColors(
    val success: Color,
    val successContainer: Color,
    val onSuccess: Color
)

val LocalExtendedColors = compositionLocalOf {
    ExtendedColors(
        success = SuccessGreen,
        successContainer = SuccessGreenContainer,
        onSuccess = OnSuccessGreen
    )
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    secondary = SecondaryGold,
    tertiary = SecondaryGold,
    background = DarkGray,
    surface = SurfaceColor,
    surfaceVariant = SurfaceVariant,
    onPrimary = Color(0xFF1A1400),
    onSecondary = Color(0xFF1A1400),
    onTertiary = Color(0xFF1A1400),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    primaryContainer = AccentColor,
    onPrimaryContainer = PrimaryGold,
    error = ErrorRed,
    outline = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF222222)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    primaryContainer = LightGoldBackground,
    onPrimaryContainer = LightPrimary,
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFEDEDED)
)

@Composable
fun RentLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            success = SuccessGreen,
            successContainer = SuccessGreenContainer,
            onSuccess = OnSuccessGreen
        )
    } else {
        ExtendedColors(
            success = LightSuccessGreen,
            successContainer = LightSuccessContainer,
            onSuccess = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
