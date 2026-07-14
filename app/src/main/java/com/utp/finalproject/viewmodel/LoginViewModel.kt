package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import com.utp.finalproject.data.repository.HomePetRepository

class LoginViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    val savedUserName: String
        get() = repository.getUserName()

    fun login(userName: String, email: String) {
        repository.saveSession(userName, email)
    }
}
