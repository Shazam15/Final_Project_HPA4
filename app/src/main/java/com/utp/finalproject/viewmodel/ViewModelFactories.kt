package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.utp.finalproject.data.repository.HomePetRepository

class RepositoryViewModelFactory(
    private val repository: HomePetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> OnboardingViewModel(repository)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repository)
            modelClass.isAssignableFrom(TasksViewModel::class.java) -> TasksViewModel(repository)
            modelClass.isAssignableFrom(TaskFormViewModel::class.java) -> TaskFormViewModel(repository)
            modelClass.isAssignableFrom(RewardsViewModel::class.java) -> RewardsViewModel(repository)
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(repository)
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository)
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository)
            else -> throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
        } as T
    }
}
