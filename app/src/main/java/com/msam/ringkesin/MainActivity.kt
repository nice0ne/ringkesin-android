package com.msam.ringkesin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.msam.ringkesin.ui.navigation.AppNavigation
import com.msam.ringkesin.ui.theme.RingkesinTheme as ThemeEnum
import com.msam.ringkesin.ui.theme.RingkesinTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Baca tema tersimpan dari SharedPreferences
        val prefs = getSharedPreferences("ringkesin_settings", Context.MODE_PRIVATE)
        val savedTheme = ThemeEnum.valueOf(
            prefs.getString("theme", ThemeEnum.AI_NATIVE.name) ?: ThemeEnum.AI_NATIVE.name
        )

        setContent {
            var currentTheme by remember { mutableStateOf(savedTheme) }

            val onThemeChanged: (ThemeEnum) -> Unit = { theme ->
                currentTheme = theme
                // Simpan juga di ViewModel (via SharedPreferences)
            }

            RingkesinTheme(theme = currentTheme) {
                AppNavigation(onThemeChanged = onThemeChanged)
            }
        }
    }
}
