package com.utp.finalproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.utp.finalproject.data.local.entity.RewardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    // El Flow lleva compras/equipamiento desde Room hasta RewardsActivity y MainActivity.
    @Query("SELECT * FROM rewards ORDER BY requiredLevel ASC, cost ASC")
    fun observeRewards(): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards WHERE id = :id LIMIT 1")
    suspend fun getReward(id: Int): RewardEntity?

    @Query("SELECT * FROM rewards")
    suspend fun getRewards(): List<RewardEntity>

    @Query("SELECT COUNT(*) FROM rewards")
    suspend fun countRewards(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(rewards: List<RewardEntity>)

    @Update
    suspend fun update(reward: RewardEntity)

    // Antes de equipar una recompensa, desactiva otra del mismo tipo para evitar capas duplicadas.
    @Query("UPDATE rewards SET isEquipped = 0 WHERE type = :type")
    suspend fun unequipType(type: String)

    @Query("DELETE FROM rewards")
    suspend fun clear()
}
