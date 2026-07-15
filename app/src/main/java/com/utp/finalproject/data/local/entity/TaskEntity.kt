package com.utp.finalproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
// Cada instancia se convierte en una fila Room y también viaja entre Repository, ViewModel y Adapter.
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val frequency: String,
    val status: String = STATUS_PENDING,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val dueAtMillis: Long,
    val completedAtMillis: Long? = null,
    val xpReward: Int = 0,
    val completedOnTime: Boolean = false,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val placeId: String? = null
) {
    companion object {
        const val STATUS_PENDING = "Pendiente"
        const val STATUS_COMPLETED = "Completada"
        const val STATUS_COMPLETED_LATE = "Completada tarde"
        const val STATUS_OVERDUE = "Vencida"

        const val PRIORITY_LOW = "Baja"
        const val PRIORITY_MEDIUM = "Media"
        const val PRIORITY_HIGH = "Alta"

        const val FREQUENCY_ONCE = "Una vez"
        const val FREQUENCY_DAILY = "Diaria"
        const val FREQUENCY_WEEKLY = "Semanal"
        const val FREQUENCY_MONTHLY = "Mensual"
    }
}
