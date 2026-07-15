package com.utp.finalproject.data.preferences

import android.content.Context

/** Guarda configuración pequeña en formato clave-valor; los datos del dominio viven en Room. */
class HomePetPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }

    fun areNotificationsEnabled(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getReminderHour(): Int {
        return preferences.getInt(KEY_REMINDER_HOUR, 18)
    }

    fun setReminderHour(hour: Int) {
        preferences.edit().putInt(KEY_REMINDER_HOUR, hour.coerceIn(0, 23)).apply()
    }

    fun getThemeMode(): String {
        return preferences.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun setThemeMode(mode: String) {
        preferences.edit().putString(KEY_THEME_MODE, ThemePreferencePolicy.normalize(mode)).apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_LOGGED_IN, false)
    }

    // LoginViewModel envía estos valores; apply() los persiste de forma asíncrona en el dispositivo.
    fun saveSession(userName: String, email: String) {
        preferences.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserName(): String = preferences.getString(KEY_USER_NAME, "") ?: ""

    // Cerrar sesión conserva mascota y tareas; solo elimina las claves de autenticación local.
    fun clearSession() {
        preferences.edit()
            .remove(KEY_LOGGED_IN)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "homepet_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"

        const val THEME_LIGHT = "Claro"
        const val THEME_DARK = "Oscuro"
        const val THEME_SYSTEM = "Sistema"
    }
}
