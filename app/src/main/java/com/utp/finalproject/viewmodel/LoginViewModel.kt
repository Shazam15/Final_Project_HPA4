package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import com.utp.finalproject.data.repository.HomePetRepository

class LoginViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    val savedUserName: String
        // Lee una preferencia a través del Repository sin exponer Context al ViewModel.
        get() = repository.getUserName()

    fun login(userName: String, email: String) {
        // Delega la sesión local a SharedPreferences; no se almacena la contraseña.
        repository.saveSession(userName, email)
    }
}
