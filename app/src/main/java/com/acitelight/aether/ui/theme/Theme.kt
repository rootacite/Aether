package com.acitelight.aether.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext

fun generateColorScheme(primaryColor: Color, isDarkMode: Boolean): ColorScheme {

    val background = if (isDarkMode) Color(0xFF121212) else Color(0xFFFFFFFF)
    val surface = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)

    val surfaceContainer = if (isDarkMode) Color(0xFF232323) else Color(0xFFFDFDFD)
    val surfaceContainerLow = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val surfaceContainerHigh = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFFAFAFA)
    val surfaceContainerHighest = if (isDarkMode) Color(0xFF333333) else Color(0xFFFFFFFF)
    val surfaceContainerLowest = if (isDarkMode) Color(0xFF0F0F0F) else Color(0xFFF0F0F0)
    val surfaceBright = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFFFFFFF)
    val surfaceDim = if (isDarkMode) Color(0xFF141414) else Color(0xFFF8F8F8)

    fun tint(surface: Color, factor: Float) = lerp(surface, primaryColor, factor)

    return if (isDarkMode) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = tint(primaryColor, 0.2f),
            onPrimaryContainer = Color.White,
            inversePrimary = tint(primaryColor, 0.6f),

            secondary = tint(primaryColor, 0.4f),
            onSecondary = Color.White,
            secondaryContainer = tint(primaryColor, 0.2f),
            onSecondaryContainer = Color.White,

            tertiary = tint(primaryColor, 0.5f),
            onTertiary = Color.White,
            tertiaryContainer = tint(primaryColor, 0.2f),
            onTertiaryContainer = Color.White,

            background = background,
            onBackground = Color.White,

            surface = surface,
            onSurface = Color.White,

            surfaceVariant = tint(surface, 0.1f),
            onSurfaceVariant = Color(0xFFE0E0E0),

            surfaceTint = primaryColor,

            inverseSurface = Color.White,
            inverseOnSurface = Color(0xFF121212),

            error = Color(0xFFCF6679),
            onError = Color.Black,
            errorContainer = Color(0xFFB00020),
            onErrorContainer = Color.White,

            outline = Color(0xFF757575),
            outlineVariant = Color(0xFF494949),
            scrim = Color.Black,

            surfaceBright = tint(surfaceBright, 0.1f),
            surfaceContainer = tint(surfaceContainer, 0.1f),
            surfaceContainerHigh = tint(surfaceContainerHigh, 0.12f),
            surfaceContainerHighest = tint(surfaceContainerHighest, 0.15f),
            surfaceContainerLow = tint(surfaceContainerLow, 0.08f),
            surfaceContainerLowest = tint(surfaceContainerLowest, 0.05f),
            surfaceDim = tint(surfaceDim, 0.1f)
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = tint(primaryColor, 0.3f),
            onPrimaryContainer = Color.Black,
            inversePrimary = tint(primaryColor, 0.6f),

            secondary = tint(primaryColor, 0.4f),
            onSecondary = Color.Black,
            secondaryContainer = tint(primaryColor, 0.2f),
            onSecondaryContainer = Color.Black,

            tertiary = tint(primaryColor, 0.5f),
            onTertiary = Color.Black,
            tertiaryContainer = tint(primaryColor, 0.3f),
            onTertiaryContainer = Color.Black,

            background = background,
            onBackground = Color.Black,

            surface = surface,
            onSurface = Color.Black,

            surfaceVariant = tint(surface, 0.1f),
            onSurfaceVariant = Color(0xFF49454F),

            surfaceTint = primaryColor,

            inverseSurface = Color(0xFF121212),
            inverseOnSurface = Color.White,

            error = Color(0xFFB00020),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color.Black,

            outline = Color(0xFF737373),
            outlineVariant = Color(0xFFD0C4C9),
            scrim = Color.Black,

            surfaceBright = tint(surfaceBright, 0.1f),
            surfaceContainer = tint(surfaceContainer, 0.1f),
            surfaceContainerHigh = tint(surfaceContainerHigh, 0.12f),
            surfaceContainerHighest = tint(surfaceContainerHighest, 0.15f),
            surfaceContainerLow = tint(surfaceContainerLow, 0.08f),
            surfaceContainerLowest = tint(surfaceContainerLowest, 0.05f),
            surfaceDim = tint(surfaceDim, 0.05f)
        )
    }
}

private val DarkColorScheme = generateColorScheme(Color(0xFF4A6F9F), isDarkMode = true)
private val LightColorScheme = generateColorScheme(Color(0xFF4A6F9F), isDarkMode = false)

@Composable
fun AetherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}