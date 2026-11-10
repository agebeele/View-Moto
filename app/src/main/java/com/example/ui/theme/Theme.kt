package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.VisualThemeMode

fun createCockpitColorScheme(themeMode: VisualThemeMode): ColorScheme {
    val primaryAccent = when (themeMode) {
        VisualThemeMode.NEON_MAGENTA -> NeonMagenta
        VisualThemeMode.CYBER_CYAN -> CyberCyan
        VisualThemeMode.RACING_ORANGE -> RacingOrange
        VisualThemeMode.ACID_LIME -> AcidLime
        VisualThemeMode.SOLAR_DAY -> Color(0xFFD8006C)
    }

    val secondaryAccent = when (themeMode) {
        VisualThemeMode.NEON_MAGENTA -> CyberCyan
        VisualThemeMode.CYBER_CYAN -> NeonMagenta
        VisualThemeMode.RACING_ORANGE -> WarningAmber
        VisualThemeMode.ACID_LIME -> CyberCyan
        VisualThemeMode.SOLAR_DAY -> Color(0xFF007A99)
    }

    return if (themeMode == VisualThemeMode.SOLAR_DAY) {
        lightColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFD6E7),
            onPrimaryContainer = Color(0xFF3F001C),
            secondary = secondaryAccent,
            onSecondary = Color.White,
            background = SolarDayBackground,
            onBackground = SolarDayText,
            surface = SolarDaySurface,
            onSurface = SolarDayText,
            surfaceVariant = SolarDayCard,
            onSurfaceVariant = Color(0xFF1E1E24),
            error = AlertRed,
            onError = Color.White
        )
    } else {
        darkColorScheme(
            primary = primaryAccent,
            onPrimary = Color.White,
            primaryContainer = primaryAccent.copy(alpha = 0.25f),
            onPrimaryContainer = HighContrastWhite,
            secondary = secondaryAccent,
            onSecondary = Color.Black,
            background = CockpitBackground,
            onBackground = HighContrastWhite,
            surface = CockpitSurface,
            onSurface = HighContrastWhite,
            surfaceVariant = CockpitCardBg,
            onSurfaceVariant = HighContrastGray,
            error = AlertRed,
            onError = Color.White
        )
    }
}

@Composable
fun MotoDashTheme(
    themeMode: VisualThemeMode = VisualThemeMode.NEON_MAGENTA,
    content: @Composable () -> Unit
) {
    val colorScheme = createCockpitColorScheme(themeMode)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
