package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.repository.HomePetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    val pet: StateFlow<PetEntity?> = repository.petFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notificationsEnabled: Boolean
        get() = repository.areNotificationsEnabled()

    val reminderHour: Int
        get() = repository.getReminderHour()

    val themeMode: String
        get() = repository.getThemeMode()

    fun savePet(name: String, type: String) {
        viewModelScope.launch {
            repository.updatePetSettings(name, type)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }

    fun setReminderHour(hour: Int) {
        repository.setReminderHour(hour)
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
    }

    fun logout() {
        repository.clearSession()
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
        }
    }
}
