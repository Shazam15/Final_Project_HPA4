package com.utp.finalproject.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserPreferencesRepository(context: Context) {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    suspend fun saveUserName(name: String) = withContext(Dispatchers.IO) {
        preferences.edit()
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    suspend fun getUserName(): String = withContext(Dispatchers.IO) {
        preferences.getString(KEY_USER_NAME, "") ?: ""
    }

    suspend fun saveTaskOrder(order: String) = withContext(Dispatchers.IO) {
        preferences.edit()
            .putString(KEY_TASK_ORDER, order)
            .apply()
    }

    suspend fun getTaskOrder(): String = withContext(Dispatchers.IO) {
        preferences.getString(KEY_TASK_ORDER, TaskRepository.ORDER_BY_DATE)
            ?: TaskRepository.ORDER_BY_DATE
    }

    companion object {
        private const val PREFERENCES_NAME = "homepet_preferences"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_TASK_ORDER = "task_order"
    }
}
