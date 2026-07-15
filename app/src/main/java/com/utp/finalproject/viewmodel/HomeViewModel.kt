package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.local.entity.TaskEntity
import com.utp.finalproject.data.repository.DashboardData
import com.utp.finalproject.data.repository.HomePetRepository
import com.utp.finalproject.domain.PetRules
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val pet: PetEntity? = null,
    val urgentTasks: List<TaskEntity> = emptyList(),
    val todayTasks: List<TaskEntity> = emptyList(),
    val unlockedRewards: Int = 0,
    val xpMax: Int = 100
)

class HomeViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    // Transforma los tres Flow de Room combinados por el Repository en un estado listo para dibujar.
    val uiState: StateFlow<HomeUiState> = repository.dashboardFlow
        .map { data -> data.toHomeUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            repository.prepareInitialData()
        }
    }

    fun completeTask(task: TaskEntity) {
        // La Activity entrega el evento; viewModelScope sobrevive a cambios de configuración.
        viewModelScope.launch {
            repository.completeTask(task)
        }
    }

    private fun DashboardData.toHomeUiState(): HomeUiState {
        val activeTasks = tasks.filter {
            it.status == TaskEntity.STATUS_PENDING || it.status == TaskEntity.STATUS_OVERDUE
        }
        return HomeUiState(
            pet = pet,
            urgentTasks = activeTasks.sortedBy { it.dueAtMillis }.take(3),
            todayTasks = tasks.sortedBy { it.dueAtMillis }.take(6),
            unlockedRewards = rewards.count { it.isUnlocked },
            xpMax = PetRules.experienceForLevel(pet?.level ?: 1)
        )
    }
}
