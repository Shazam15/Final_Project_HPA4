package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.repository.HomePetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val shouldOpenHome: Boolean = false,
    val shouldOpenLogin: Boolean = false,
    val saved: Boolean = false
)

class OnboardingViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepareInitialData()
            val onboardingCompleted = repository.isOnboardingCompleted()
            _uiState.value = _uiState.value.copy(
                shouldOpenHome = onboardingCompleted && repository.isLoggedIn(),
                shouldOpenLogin = onboardingCompleted && !repository.isLoggedIn()
            )
        }
    }

    fun savePet(name: String, type: String) {
        viewModelScope.launch {
            repository.createPet(
                name = name,
                type = type.ifBlank { PetEntity.TYPE_DOG }
            )
            _uiState.value = OnboardingUiState(shouldOpenLogin = true, saved = true)
        }
    }
}
