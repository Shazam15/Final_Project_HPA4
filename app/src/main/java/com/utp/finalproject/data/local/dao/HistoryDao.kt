package com.utp.finalproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.utp.finalproject.data.local.entity.ActivityHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM activity_history ORDER BY createdAtMillis DESC")
    fun observeHistory(): Flow<List<ActivityHistoryEntity>>

    @Insert
    suspend fun insert(history: ActivityHistoryEntity)

    @Query("DELETE FROM activity_history")
    suspend fun clear()
}
