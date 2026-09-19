package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SamirPrimary,
    onPrimary = Color.White,
    primaryContainer = SamirPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = SamirSecondary,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = SamirFlame,
    onTertiary = Color.White,
    background = SamirDarkBg,
    onBackground = SamirTextPrimary,
    surface = SamirSurface,
    onSurface = SamirTextPrimary,
    surfaceVariant = SamirSurfaceVariant,
    onSurfaceVariant = SamirTextSecondary,
    outline = SamirSurfaceBorder,
    outlineVariant = Color(0xFF334155),
    error = SamirError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SamirPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDD6FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    tertiary = SamirFlame,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = SamirError,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Samir's signature dark aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
