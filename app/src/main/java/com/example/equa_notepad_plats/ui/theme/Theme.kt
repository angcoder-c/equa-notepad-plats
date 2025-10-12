package com.example.app.ui.theme

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
import androidx.compose.ui.platform.LocalContext

val EquaGreenPrimary = Color(0xFF1E9C89)    // Verde teal oscuro
val EquaGreenLight = Color(0xFF6AE1D0)     // Verde teal claro
val EquaGreenDark = Color(0xFF006A5C)      // Verde teal muy oscuro
val EquaPaperLight = Color(0xFFE6F3F0)
val EquaSecondaryLight = Color(0xFFEFF8F5)
// Verde muy claro
val EquaPaperDark = Color(0xFF212B21)     // Verde muy oscuro
val EquaSecondaryDark = Color(0xFF1A211A)     // Verde muy oscuro
val EquaGreenAccent = Color(0xFF4BCFB8)    // Verde teal medio

private val DarkColorScheme = darkColorScheme(
    primary = EquaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = EquaGreenDark,
    onPrimaryContainer = Color.White,
    secondary = EquaGreenAccent,
    onSecondary = Color.White,
    secondaryContainer = EquaSecondaryDark,
    onSecondaryContainer = Color.White,
    tertiary = EquaGreenLight,
    onTertiary = Color.White,
    background = Color(0xFF0F1113),
    onBackground = Color.White,
    surfaceContainer = EquaPaperDark,
    surfaceContainerHighest = EquaPaperDark,
    surface = EquaPaperDark,
    onSurface = Color.White,
    error = Color(0xFFE8584C),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EquaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = EquaGreenLight,
    onPrimaryContainer = Color.Black,
    secondary = EquaGreenAccent,
    onSecondary = Color.White,
    secondaryContainer = EquaSecondaryLight,
    surfaceContainerHighest = EquaPaperLight,
    onSecondaryContainer = Color.Black,
    tertiary = EquaGreenDark,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surfaceContainer = EquaPaperLight,
    surface = EquaPaperLight,
    onSurface = Color.Black,
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}