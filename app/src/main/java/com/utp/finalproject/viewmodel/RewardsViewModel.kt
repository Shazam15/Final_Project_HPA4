package com.utp.finalproject.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utp.finalproject.data.local.entity.RewardEntity
import com.utp.finalproject.data.repository.HomePetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val repository: HomePetRepository
) : ViewModel() {
    val rewards: StateFlow<List<RewardEntity>> = repository.rewardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    fun buyOrEquip(reward: RewardEntity) {
        viewModelScope.launch {
            _message.value = repository.buyOrEquipReward(reward)
        }
    }
}
