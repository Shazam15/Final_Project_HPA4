package com.utp.finalproject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val savedUserName: String = ""
)

class LoginViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.value = LoginUiState(
                savedUserName = preferencesRepository.getUserName()
            )
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            preferencesRepository.saveUserName(name)
            _uiState.value = _uiState.value.copy(savedUserName = name)
        }
    }
}

class LoginViewModelFactory(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(preferencesRepository) as T
    }
}
