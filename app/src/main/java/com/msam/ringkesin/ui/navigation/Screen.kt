package com.msam.ringkesin.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Record : Screen("record", "Rekam", Icons.Default.Mic)
    data object Summary : Screen("summary", "Ringkasan", Icons.Default.AutoAwesome)
    data object History : Screen("history", "Riwayat", Icons.Default.History)
    data object Settings : Screen("settings", "Setelan", Icons.Default.Settings)

    fun displayName(lang: String): String = when (this) {
        Record -> com.msam.ringkesin.ui.localization.S.tabRecord(lang)
        Summary -> com.msam.ringkesin.ui.localization.S.tabSummary(lang)
        History -> com.msam.ringkesin.ui.localization.S.tabHistory(lang)
        Settings -> com.msam.ringkesin.ui.localization.S.tabSettings(lang)
    }
}

val bottomNavItems = listOf(Screen.Record, Screen.Summary, Screen.History, Screen.Settings)
