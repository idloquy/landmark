package com.idloquy.landmark.ui.theme

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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF88522B),
    onPrimary = Color(0xFFFFF7F4),
    primaryContainer = Color(0xFFFEB788),
    onPrimaryContainer = Color(0xFF633410),
    secondary = Color(0xFF765946),
    onSecondary = Color(0xFFFFF7F4),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF3D3027),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF3D3027),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF5BA94),
    onPrimary = Color(0xFF5E381A),
    primaryContainer = Color(0xFF73472B),
    onPrimaryContainer = Color(0xFFFFDBC7),
    secondary = Color(0xFFE6BFA8),
    onSecondary = Color(0xFF543B29),
    background = Color(0xFF120D0A),
    onBackground = Color(0xFFF7E0D6),
    surface = Color(0xFF120D0A),
    onSurface = Color(0xFFF7E0D6),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF61140F)
)

@Composable
fun LandmarkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography, content = content
    )
}