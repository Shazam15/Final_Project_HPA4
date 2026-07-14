package com.utp.finalproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val type: String,
    val description: String,
    val assetName: String,
    val requiredLevel: Int,
    val cost: Int,
    val isUnlocked: Boolean = false,
    val isEquipped: Boolean = false
)
