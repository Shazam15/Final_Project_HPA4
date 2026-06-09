package com.utp.finalproject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.PetTask
import com.utp.finalproject.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskFormUiState(
    val task: PetTask? = null,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false
)

class TaskFormViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: Long) {
        if (taskId == 0L) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(task = taskRepository.getTask(taskId))
        }
    }

    fun saveTask(task: PetTask) {
        viewModelScope.launch {
            val savedId = taskRepository.saveTask(task)
            _uiState.value = _uiState.value.copy(
                task = task.copy(id = savedId),
                isSaved = true
            )
        }
    }

    fun deleteTask(taskId: Long) {
        if (taskId == 0L) {
            return
        }

        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}

class TaskFormViewModelFactory(
    private val taskRepository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskFormViewModel(taskRepository) as T
    }
}
