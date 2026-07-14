package com.utp.finalproject.ui

import androidx.appcompat.app.AppCompatDelegate
import com.utp.finalproject.data.preferences.HomePetPreferences

object ThemeManager {
    fun apply(mode: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                HomePetPreferences.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                HomePetPreferences.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
