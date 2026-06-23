package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
