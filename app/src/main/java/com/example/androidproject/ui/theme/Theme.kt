package com.example.androidproject.ui.theme

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

private val AppPrimaryColor = Color(0xFF0D47A1) // Dark Blue seed

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimaryColor,
    // Provide a seed color if using fromSeed is preferred, but manual schemes work too.
)

private val LightColorScheme = lightColorScheme(
    primary = AppPrimaryColor,
)

@Composable
fun AndroidProjectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = Color(0xFF90CAF9),
            onPrimary = Color(0xFF003258),
            primaryContainer = Color(0xFF00497D),
            onPrimaryContainer = Color(0xFFD1E4FF),
            surface = Color(0xFF1A1C1E),
            onSurface = Color(0xFFE2E2E5),
            surfaceVariant = Color(0xFF42474E),
            onSurfaceVariant = Color(0xFFC2C7CF),
            outline = Color(0xFF8C9199),
            outlineVariant = Color(0xFF42474E)
        )
        else -> lightColorScheme(
            primary = AppPrimaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1E4FF),
            onPrimaryContainer = Color(0xFF001D36),
            surface = Color(0xFFFDFCFF),
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = Color(0xFFDFE2EB),
            onSurfaceVariant = Color(0xFF42474E),
            outline = Color(0xFF72777F),
            outlineVariant = Color(0xFFC2C7CF)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}