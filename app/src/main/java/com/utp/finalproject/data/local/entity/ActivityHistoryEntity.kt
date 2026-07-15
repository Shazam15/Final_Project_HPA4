package com.utp.finalproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_history")
// El Repository crea estas filas como evidencia de acciones; HistoryActivity solo las consulta.
data class ActivityHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val detail: String,
    val type: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val xpDelta: Int = 0,
    val coinDelta: Int = 0
)
