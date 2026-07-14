package com.utp.finalproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.utp.finalproject.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pet WHERE id = :id LIMIT 1")
    fun observePet(id: Int = PetEntity.DEFAULT_ID): Flow<PetEntity?>

    @Query("SELECT * FROM pet WHERE id = :id LIMIT 1")
    suspend fun getPet(id: Int = PetEntity.DEFAULT_ID): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: PetEntity)

    @Update
    suspend fun update(pet: PetEntity)

    @Query("DELETE FROM pet")
    suspend fun clear()
}
