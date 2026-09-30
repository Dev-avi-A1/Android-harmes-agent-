package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HarmesCyan,
    onPrimary = Color(0xFF041E26),
    primaryContainer = Color(0xFF0C3845),
    onPrimaryContainer = Color(0xFFBCEEFA),
    secondary = HarmesElectricBlue,
    onSecondary = Color(0xFF032B44),
    secondaryContainer = Color(0xFF133E5E),
    onSecondaryContainer = Color(0xFFC7E8FC),
    tertiary = HarmesTeal,
    onTertiary = Color(0xFF003822),
    background = HarmesDeepSpace,
    onBackground = HarmesTextPrimaryDark,
    surface = HarmesSurfaceDark,
    onSurface = HarmesTextPrimaryDark,
    surfaceVariant = HarmesCardDark,
    onSurfaceVariant = HarmesTextSecondaryDark,
    outline = HarmesCardBorderDark,
    error = HarmesRose,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = HarmesPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF2FC),
    onPrimaryContainer = Color(0xFF00354E),
    secondary = Color(0xFF0369A1),
    onSecondary = Color.White,
    background = HarmesSurfaceLight,
    onBackground = HarmesTextPrimaryLight,
    surface = HarmesCardLight,
    onSurface = HarmesTextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = HarmesTextSecondaryLight,
    outline = HarmesCardBorderLight,
    error = HarmesRose,
    onError = Color.White
)

@Composable
fun HarmesTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                if (Build.VERSION.SDK_INT < 35) {
                    @Suppress("DEPRECATION")
                    window.statusBarColor = colorScheme.background.toArgb()
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = colorScheme.background.toArgb()
                }
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for any legacy references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HarmesTheme(darkTheme = darkTheme, content = content)
}
