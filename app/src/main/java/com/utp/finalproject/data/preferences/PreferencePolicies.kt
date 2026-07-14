package com.utp.finalproject.data.preferences

data class SessionSnapshot(
    val isLoggedIn: Boolean,
    val userName: String,
    val email: String
)

object ThemePreferencePolicy {
    fun normalize(mode: String): String {
        return when (mode) {
            HomePetPreferences.THEME_LIGHT,
            HomePetPreferences.THEME_DARK,
            HomePetPreferences.THEME_SYSTEM -> mode
            else -> HomePetPreferences.THEME_SYSTEM
        }
    }
}

object SessionPreferencePolicy {
    fun cleared(): SessionSnapshot = SessionSnapshot(false, "", "")
}
