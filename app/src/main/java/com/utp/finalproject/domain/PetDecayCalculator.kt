package com.utp.finalproject.domain

import com.utp.finalproject.data.local.entity.PetEntity

data class PetDecayResult(
    val pet: PetEntity,
    val appliedIntervals: Long,
    val changed: Boolean
)

object PetDecayCalculator {
    const val INTERVAL_MILLIS = 6L * 60L * 60L * 1000L
    private const val HUNGER_LOSS_PER_INTERVAL = 8
    private const val HAPPINESS_LOSS_PER_INTERVAL = 4
    private const val ENERGY_LOSS_PER_INTERVAL = 3
    private const val HEALTH_LOSS_PER_DAY = 5

    fun calculate(pet: PetEntity, now: Long): PetDecayResult {
        val lastUpdate = pet.lastStatsUpdateAt
        if (lastUpdate <= 0L || now <= lastUpdate) {
            return PetDecayResult(
                pet = if (lastUpdate <= 0L) pet.copy(lastStatsUpdateAt = now) else pet,
                appliedIntervals = 0,
                changed = lastUpdate <= 0L
            )
        }

        val intervals = (now - lastUpdate) / INTERVAL_MILLIS
        if (intervals == 0L) {
            return PetDecayResult(pet, 0, false)
        }

        val hunger = (pet.hunger - intervals * HUNGER_LOSS_PER_INTERVAL).toInt().coerceIn(0, 100)
        val happiness = (pet.happiness - intervals * HAPPINESS_LOSS_PER_INTERVAL).toInt().coerceIn(0, 100)
        val energy = (pet.energy - intervals * ENERGY_LOSS_PER_INTERVAL).toInt().coerceIn(0, 100)
        val days = intervals / 4L
        val healthPenalty = if (hunger < 30 || happiness < 30) {
            (days * HEALTH_LOSS_PER_DAY).toInt()
        } else {
            0
        }
        val health = (pet.health - healthPenalty).coerceIn(0, 100)
        val mood = PetRules.calculateMood(
            completedToday = 0,
            pendingToday = 0,
            overdueTasks = 0,
            health = health,
            happiness = happiness,
            hunger = hunger
        )

        return PetDecayResult(
            pet = pet.copy(
                health = health,
                hunger = hunger,
                energy = energy,
                happiness = happiness,
                mood = mood,
                lastStatsUpdateAt = lastUpdate + intervals * INTERVAL_MILLIS,
                lastUpdatedAt = now
            ),
            appliedIntervals = intervals,
            changed = true
        )
    }
}
