package com.utp.finalproject.domain

import com.utp.finalproject.data.local.entity.PetEntity

enum class WellbeingAlertLevel {
    NONE,
    WARNING,
    CRITICAL
}

data class PetNotificationDecision(
    val level: WellbeingAlertLevel,
    val shouldNotify: Boolean,
    val affectedStat: String
)

object PetNotificationEvaluator {
    const val WARNING_THRESHOLD = 30
    const val CRITICAL_HEALTH_THRESHOLD = 15
    const val CRITICAL_HUNGER_THRESHOLD = 10
    const val CRITICAL_AVERAGE_THRESHOLD = 20
    const val WARNING_COOLDOWN_MILLIS = 12L * 60L * 60L * 1000L
    const val CRITICAL_COOLDOWN_MILLIS = 6L * 60L * 60L * 1000L

    // El Worker entrega la mascota; esta función decide nivel y cooldown sin mostrar la notificación.
    fun evaluate(pet: PetEntity, now: Long): PetNotificationDecision {
        val stats = linkedMapOf(
            "salud" to pet.health,
            "hambre" to pet.hunger,
            "energía" to pet.energy,
            "felicidad" to pet.happiness
        )
        val average = stats.values.average()
        val level = when {
            pet.health <= CRITICAL_HEALTH_THRESHOLD ||
                pet.hunger <= CRITICAL_HUNGER_THRESHOLD ||
                average <= CRITICAL_AVERAGE_THRESHOLD -> WellbeingAlertLevel.CRITICAL
            stats.values.any { it <= WARNING_THRESHOLD } -> WellbeingAlertLevel.WARNING
            else -> WellbeingAlertLevel.NONE
        }
        val previousLevel = runCatching {
            WellbeingAlertLevel.valueOf(pet.lastNotificationLevel)
        }.getOrDefault(WellbeingAlertLevel.NONE)
        val cooldown = if (level == WellbeingAlertLevel.CRITICAL) {
            CRITICAL_COOLDOWN_MILLIS
        } else {
            WARNING_COOLDOWN_MILLIS
        }
        val cooldownExpired = now - pet.lastDecayNotificationAt >= cooldown
        val worsened = level == WellbeingAlertLevel.CRITICAL && previousLevel != WellbeingAlertLevel.CRITICAL

        return PetNotificationDecision(
            level = level,
            shouldNotify = level != WellbeingAlertLevel.NONE && (cooldownExpired || worsened),
            affectedStat = stats.minByOrNull { it.value }?.key.orEmpty()
        )
    }
}
