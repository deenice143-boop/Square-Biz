package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalThemeDark = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = AccentTechBlue,
    secondary = UnisysBlue,
    tertiary = SlateSubtle,
    background = SlateNavy,
    surface = Color(0xFF1E293B),
    onPrimary = CleanWhite,
    onSecondary = CleanWhite,
    onBackground = LightBackground,
    onSurface = CleanWhite,
    outline = SlateSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = UnisysBlue,
    secondary = AccentTechBlue,
    tertiary = SlateSubtle,
    background = LightBackground,
    surface = CleanWhite,
    onPrimary = CleanWhite,
    onSecondary = CleanWhite,
    onBackground = SlateNavy,
    onSurface = SlateNavy,
    outline = BorderSlate
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Standardize brand theme colors rather than dynamic system overlay overlay
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalThemeDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

class ThemeColors(
    val CleanWhite: Color,
    val SlateNavy: Color,
    val SlateSubtle: Color,
    val BorderSlate: Color,
    val LightBackground: Color,
    val UnisysBlue: Color,
    val AccentTechBlue: Color
)

@Composable
fun getThemeColors(isDarkMode: Boolean): ThemeColors {
    return if (isDarkMode) {
        ThemeColors(
            CleanWhite = Color(0xFF1E293B),         // dark surface
            SlateNavy = Color(0xFFFFFFFF),          // light text
            SlateSubtle = Color(0xFF94A3B8),        // light-slate text
            BorderSlate = Color(0xFF334155),        // dark border
            LightBackground = Color(0xFF0F172A),    // dark background
            UnisysBlue = Color(0xFF3B82F6),         // bright blue
            AccentTechBlue = Color(0xFF60A5FA)      // lighter blue
        )
    } else {
        ThemeColors(
            CleanWhite = Color(0xFFFFFFFF),
            SlateNavy = Color(0xFF0F172A),
            SlateSubtle = Color(0xFF475569),
            BorderSlate = Color(0xFFE2E8F0),
            LightBackground = Color(0xFFF8FAFC),
            UnisysBlue = Color(0xFF0A3E72),
            AccentTechBlue = Color(0xFF2563EB)
        )
    }
}
