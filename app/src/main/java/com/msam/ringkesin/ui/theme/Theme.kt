package com.msam.ringkesin.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class RingkesinTheme(val label: String) {
    AI_NATIVE("AI-Native"),
    OLED("OLED"),
    LIGHT("Light"),
    VIBRANT("Vibrant"),
    BIOPHILIC("Biophilic"),
    GLASS("Glassmorphism")
}

private val DarkColorScheme = darkColorScheme(
    primary = AiNativePrimary,
    onPrimary = AiNativeOnPrimary,
    secondary = AiNativeTertiary,
    background = AiNativeBackground,
    surface = AiNativeSurface,
    surfaceVariant = AiNativeSurfaceVariant,
    onBackground = AiNativeOnSurface,
    onSurface = AiNativeOnSurface,
    onSurfaceVariant = AiNativeOnSurfaceVariant,
    outline = AiNativeOutline,
    error = Error,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = Color(0xFFE8EAED),
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = Color(0xFFDADCE0),
    error = Error,
)

fun themeToColorScheme(theme: RingkesinTheme, darkTheme: Boolean): ColorScheme {
    return when (theme) {
        RingkesinTheme.AI_NATIVE -> if (darkTheme) darkColorScheme(
            primary = AiNativePrimary, onPrimary = AiNativeOnPrimary,
            secondary = AiNativeTertiary, background = AiNativeBackground,
            surface = AiNativeSurface, surfaceVariant = AiNativeSurfaceVariant,
            onBackground = AiNativeOnSurface, onSurface = AiNativeOnSurface,
            onSurfaceVariant = AiNativeOnSurfaceVariant, outline = AiNativeOutline,
            error = Error,
        ) else darkColorScheme(
            primary = AiNativePrimary, onPrimary = AiNativeOnPrimary,
            secondary = AiNativeTertiary, background = AiNativeBackground,
            surface = AiNativeSurface, surfaceVariant = AiNativeSurfaceVariant,
            onBackground = AiNativeOnSurface, onSurface = AiNativeOnSurface,
            onSurfaceVariant = AiNativeOnSurfaceVariant, outline = AiNativeOutline,
            error = Error,
        )

        RingkesinTheme.OLED -> darkColorScheme(
            primary = OledPrimary, onPrimary = Color(0xFF002B14),
            background = OledBackground, surface = OledSurface,
            surfaceVariant = OledSurfaceVariant,
            onBackground = Color(0xFFF5F5F7), onSurface = Color(0xFFF5F5F7),
            onSurfaceVariant = Color(0xFFA2A2AA), outline = Color(0xFF1C1C1F),
            error = Error,
        )

        RingkesinTheme.LIGHT -> lightColorScheme(
            primary = LightPrimary, onPrimary = Color.White,
            background = LightBackground, surface = LightSurface,
            surfaceVariant = Color(0xFFF1F3F4),
            onBackground = LightOnSurface, onSurface = LightOnSurface,
            onSurfaceVariant = LightOnSurfaceVariant,
            outline = Color(0xFFDADCE0), error = Error,
        )

        RingkesinTheme.VIBRANT -> darkColorScheme(
            primary = VibrantPrimary, onPrimary = VibrantOnPrimary,
            background = Color(0xFF1A0520), surface = Color(0xFF2A0A30),
            surfaceVariant = Color(0xFF3A1040),
            onBackground = Color.White, onSurface = Color.White,
            onSurfaceVariant = Color(0xFFCCB8D0),
            outline = Color(0xFF4A2050), error = Error,
        )

        RingkesinTheme.BIOPHILIC -> lightColorScheme(
            primary = BiophilicPrimary, onPrimary = Color.White,
            background = BiophilicBackground, surface = BiophilicSurface,
            surfaceVariant = Color(0xFFF3EFE5),
            onBackground = BiophilicOnSurface, onSurface = BiophilicOnSurface,
            onSurfaceVariant = Color(0xFF6F685A),
            outline = Color(0xFFD4CDBF), error = Color(0xFFC47A52),
        )

        RingkesinTheme.GLASS -> darkColorScheme(
            primary = GlassPrimary, onPrimary = Color.White,
            background = Color(0xFF1A2332), surface = Color(0xFF243044),
            surfaceVariant = Color(0xFF2E3D55),
            onBackground = Color(0xFFE8EDF2), onSurface = Color(0xFFE8EDF2),
            onSurfaceVariant = Color(0xFF9AACBF),
            outline = Color(0xFF3D5068), error = Error,
        )
    }
}

@Composable
fun RingkesinTheme(
    theme: RingkesinTheme = RingkesinTheme.AI_NATIVE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = themeToColorScheme(theme, darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                theme == RingkesinTheme.LIGHT || theme == RingkesinTheme.BIOPHILIC
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
