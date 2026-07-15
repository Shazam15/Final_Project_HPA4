package com.utp.finalproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet")
// Reúne progreso, bienestar y nombres de capas visuales de la única mascota local.
data class PetEntity(
    @PrimaryKey
    val id: Int = DEFAULT_ID,
    val name: String,
    val type: String,
    val level: Int = 1,
    val experience: Int = 0,
    val health: Int = 100,
    val hunger: Int = 100,
    val energy: Int = 100,
    val happiness: Int = 100,
    val mood: String = MOOD_HAPPY,
    val coins: Int = 0,
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val lastInteractionAt: Long = System.currentTimeMillis(),
    val lastStatsUpdateAt: Long = System.currentTimeMillis(),
    val lastAppOpenedAt: Long = System.currentTimeMillis(),
    val lastDecayNotificationAt: Long = 0L,
    val lastNotificationLevel: String = "",
    val equippedAccessory: String = "",
    val equippedColor: String = "Clásico",
    val equippedHat: String = "",
    val equippedClothing: String = "",
    val equippedBackground: String = ""
) {
    companion object {
        const val DEFAULT_ID = 1
        const val TYPE_DOG = "Perro"
        const val TYPE_CAT = "Gato"
        const val TYPE_RABBIT = "Conejo"

        const val MOOD_HAPPY = "Feliz"
        const val MOOD_NEUTRAL = "Neutral"
        const val MOOD_SAD = "Triste"
        const val MOOD_SICK = "Enferma"
        const val MOOD_DANGER = "En peligro"
    }
}
