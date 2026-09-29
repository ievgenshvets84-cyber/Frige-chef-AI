package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FreshGreenPrimaryDark,
    onPrimary = FreshGreenOnPrimaryDark,
    primaryContainer = FreshGreenContainerDark,
    onPrimaryContainer = FreshGreenOnContainerDark,
    secondary = FreshOrangeSecondaryDark,
    onSecondary = FreshOrangeOnSecondaryDark,
    secondaryContainer = FreshOrangeContainerDark,
    onSecondaryContainer = FreshOrangeOnContainerDark,
    tertiary = FreshTertiaryDark,
    onTertiary = FreshOnTertiaryDark,
    tertiaryContainer = FreshTertiaryContainerDark,
    onTertiaryContainer = FreshTertiaryContainerDark,
    background = FreshBackgroundDark,
    surface = FreshSurfaceDark,
    surfaceVariant = FreshSurfaceVariantDark,
    outline = FreshOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = FreshGreenPrimary,
    onPrimary = FreshGreenOnPrimary,
    primaryContainer = FreshGreenContainer,
    onPrimaryContainer = FreshGreenOnContainer,
    secondary = FreshOrangeSecondary,
    onSecondary = FreshOrangeOnSecondary,
    secondaryContainer = FreshOrangeContainer,
    onSecondaryContainer = FreshOrangeOnContainer,
    tertiary = FreshTertiary,
    onTertiary = FreshOnTertiary,
    tertiaryContainer = FreshTertiaryContainer,
    onTertiaryContainer = FreshOnTertiaryContainer,
    background = FreshBackgroundLight,
    surface = FreshSurfaceLight,
    surfaceVariant = FreshSurfaceVariantLight,
    outline = FreshOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
