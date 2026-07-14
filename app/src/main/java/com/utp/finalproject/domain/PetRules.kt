package com.utp.finalproject.domain

import com.utp.finalproject.data.local.entity.PetEntity
import com.utp.finalproject.data.local.entity.TaskEntity
import kotlin.math.max
import kotlin.math.min

data class RewardResult(
    val xp: Int,
    val coins: Int,
    val completedOnTime: Boolean
)

object PetRules {

    fun calculateReward(task: TaskEntity, completedAtMillis: Long): RewardResult {
        val baseXp = when (task.priority) {
            TaskEntity.PRIORITY_HIGH -> 35
            TaskEntity.PRIORITY_MEDIUM -> 20
            else -> 10
        }
        val frequencyBonus = when (task.frequency) {
            TaskEntity.FREQUENCY_DAILY -> 5
            TaskEntity.FREQUENCY_WEEKLY -> 10
            TaskEntity.FREQUENCY_MONTHLY -> 15
            else -> 0
        }
        val completedOnTime = completedAtMillis <= task.dueAtMillis
        val xp = if (completedOnTime) baseXp + frequencyBonus else (baseXp + frequencyBonus) / 2
        return RewardResult(xp = xp, coins = max(2, xp / 5), completedOnTime = completedOnTime)
    }

    fun updatePetAfterTask(pet: PetEntity, reward: RewardResult, overdueTasks: Int): PetEntity {
        var totalExperience = pet.experience + reward.xp
        var level = pet.level
        var coins = pet.coins + reward.coins

        while (totalExperience >= experienceForLevel(level)) {
            totalExperience -= experienceForLevel(level)
            level += 1
            coins += 20
        }

        val health = clamp(pet.health + if (reward.completedOnTime) 5 else 1 - overdueTasks)
        val hunger = clamp(pet.hunger + if (reward.completedOnTime) 8 else 4)
        val energy = clamp(pet.energy + if (reward.completedOnTime) 4 else 1)
        val happiness = clamp(pet.happiness + if (reward.completedOnTime) 8 else 3 - overdueTasks)
        val mood = calculateMood(
            completedToday = 1,
            pendingToday = 0,
            overdueTasks = overdueTasks,
            health = health,
            happiness = happiness,
            hunger = hunger
        )

        return pet.copy(
            level = level,
            experience = totalExperience,
            health = health,
            hunger = hunger,
            energy = energy,
            happiness = happiness,
            mood = mood,
            coins = coins,
            lastUpdatedAt = System.currentTimeMillis(),
            lastInteractionAt = System.currentTimeMillis()
        )
    }

    fun updatePetByTaskPressure(pet: PetEntity, pendingToday: Int, overdueTasks: Int): PetEntity {
        val health = clamp(pet.health - overdueTasks * 3)
        val energy = clamp(pet.energy - pendingToday)
        val happiness = clamp(pet.happiness - overdueTasks * 4)
        return pet.copy(
            health = health,
            energy = energy,
            happiness = happiness,
            mood = calculateMood(0, pendingToday, overdueTasks, health, happiness, pet.hunger),
            lastUpdatedAt = System.currentTimeMillis()
        )
    }

    fun calculateMood(
        completedToday: Int,
        pendingToday: Int,
        overdueTasks: Int,
        health: Int,
        happiness: Int,
        hunger: Int = 100
    ): String {
        return when {
            health <= 20 || happiness <= 20 || hunger <= 10 || overdueTasks >= 5 -> PetEntity.MOOD_DANGER
            health <= 40 || happiness <= 40 || hunger <= 30 || overdueTasks >= 3 -> PetEntity.MOOD_SICK
            overdueTasks >= 1 || pendingToday > completedToday -> PetEntity.MOOD_SAD
            completedToday >= 3 && overdueTasks == 0 -> PetEntity.MOOD_HAPPY
            else -> PetEntity.MOOD_NEUTRAL
        }
    }

    fun experienceForLevel(level: Int): Int = 80 + (level - 1) * 35

    private fun clamp(value: Int): Int = min(100, max(0, value))
}
