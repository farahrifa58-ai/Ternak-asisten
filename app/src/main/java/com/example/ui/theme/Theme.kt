package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = FarmGreenDarkPrimary,
    onPrimary = FarmGreenDarkOnPrimary,
    primaryContainer = FarmGreenDarkPrimaryContainer,
    onPrimaryContainer = FarmGreenDarkOnPrimaryContainer,
    secondary = FarmTealDarkSecondary,
    onSecondary = FarmTealDarkOnSecondary,
    secondaryContainer = FarmTealDarkSecondaryContainer,
    onSecondaryContainer = FarmTealDarkOnSecondaryContainer,
    tertiary = HarvestAmberDarkTertiary,
    onTertiary = HarvestAmberDarkOnTertiary,
    tertiaryContainer = HarvestAmberDarkTertiaryContainer,
    onTertiaryContainer = HarvestAmberDarkOnTertiaryContainer,
    background = FarmDarkBg,
    onBackground = FarmDarkOnSurface,
    surface = FarmDarkSurface,
    onSurface = FarmDarkOnSurface,
    surfaceVariant = FarmDarkSurfaceVariant,
    onSurfaceVariant = FarmDarkOnSurfaceVariant,
    outline = FarmDarkOutline,
    outlineVariant = FarmDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = FarmGreenOnPrimary,
    primaryContainer = FarmGreenPrimaryContainer,
    onPrimaryContainer = FarmGreenOnPrimaryContainer,
    secondary = FarmTealSecondary,
    onSecondary = FarmTealOnSecondary,
    secondaryContainer = FarmTealSecondaryContainer,
    onSecondaryContainer = FarmTealOnSecondaryContainer,
    tertiary = HarvestAmberTertiary,
    onTertiary = HarvestAmberOnTertiary,
    tertiaryContainer = HarvestAmberTertiaryContainer,
    onTertiaryContainer = HarvestAmberOnTertiaryContainer,
    background = FarmLightBg,
    onBackground = FarmLightOnSurface,
    surface = FarmLightSurface,
    onSurface = FarmLightOnSurface,
    surfaceVariant = FarmLightSurfaceVariant,
    onSurfaceVariant = FarmLightOnSurfaceVariant,
    outline = FarmLightOutline,
    outlineVariant = FarmLightOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
