package com.utp.finalproject.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.PetTask
import com.utp.finalproject.data.TaskRepository
import com.utp.finalproject.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val userName: String = "",
    val orderBy: String = TaskRepository.ORDER_BY_DATE,
    val tasks: List<PetTask> = emptyList(),
    val isLoading: Boolean = true
)

class MainViewModel(
    private val taskRepository: TaskRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val userName = preferencesRepository.getUserName()
            val orderBy = preferencesRepository.getTaskOrder()
            val tasks = taskRepository.getTasks(orderBy)

            _uiState.value = MainUiState(
                userName = userName,
                orderBy = orderBy,
                tasks = tasks,
                isLoading = false
            )
        }
    }

    fun refreshTasks() {
        viewModelScope.launch {
            val tasks = taskRepository.getTasks(_uiState.value.orderBy)
            _uiState.value = _uiState.value.copy(tasks = tasks, isLoading = false)
        }
    }

    fun updateOrder(orderBy: String) {
        viewModelScope.launch {
            preferencesRepository.saveTaskOrder(orderBy)
            val tasks = taskRepository.getTasks(orderBy)
            _uiState.value = _uiState.value.copy(orderBy = orderBy, tasks = tasks)
        }
    }

    fun updateCompletion(task: PetTask, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateCompletion(task.id, isCompleted)
            refreshTasks()
        }
    }

    fun deleteTask(task: PetTask) {
        viewModelScope.launch {
            taskRepository.deleteTask(task.id)
            refreshTasks()
        }
    }
}

class MainViewModelFactory(
    private val taskRepository: TaskRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(taskRepository, preferencesRepository) as T
    }
}
