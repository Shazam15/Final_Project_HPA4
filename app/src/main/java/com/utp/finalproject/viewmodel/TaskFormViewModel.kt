package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.repository.HomePetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskFormUiState(
    val task: TaskEntity? = null,
    val saved: Boolean = false,
    val deleted: Boolean = false
)

class TaskFormViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: Long) {
        if (taskId == 0L) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(task = repository.getTask(taskId))
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.saveTask(task)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}
