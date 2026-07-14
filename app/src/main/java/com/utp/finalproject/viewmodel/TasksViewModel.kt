package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.repository.HomePetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val filter: String = "Todas",
    val order: String = "Fecha"
)

class TasksViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    private val filter = MutableStateFlow("Todas")
    private val order = MutableStateFlow("Fecha")

    val uiState: StateFlow<TasksUiState> = combine(repository.tasksFlow, filter, order) { tasks, selectedFilter, selectedOrder ->
        val filtered = when (selectedFilter) {
            TaskEntity.STATUS_PENDING -> tasks.filter { it.status == TaskEntity.STATUS_PENDING }
            TaskEntity.STATUS_COMPLETED -> tasks.filter {
                it.status == TaskEntity.STATUS_COMPLETED || it.status == TaskEntity.STATUS_COMPLETED_LATE
            }
            TaskEntity.STATUS_OVERDUE -> tasks.filter { it.status == TaskEntity.STATUS_OVERDUE }
            else -> tasks
        }
        val sorted = when (selectedOrder) {
            "Prioridad" -> filtered.sortedWith(compareByDescending<TaskEntity> { priorityWeight(it.priority) }.thenBy { it.dueAtMillis })
            "Categoria" -> filtered.sortedBy { it.category }
            else -> filtered.sortedBy { it.dueAtMillis }
        }
        TasksUiState(sorted, selectedFilter, selectedOrder)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun setFilter(value: String) {
        filter.value = value
    }

    fun setOrder(value: String) {
        order.value = value
    }

    fun completeTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.completeTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun priorityWeight(priority: String): Int {
        return when (priority) {
            TaskEntity.PRIORITY_HIGH -> 3
            TaskEntity.PRIORITY_MEDIUM -> 2
            else -> 1
        }
    }
}
