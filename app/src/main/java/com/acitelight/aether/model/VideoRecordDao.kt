package com.acitelight.aether.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoRecordDao {
    @Query("SELECT * FROM videorecord")
    fun getAll(): Flow<List<VideoRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rec: VideoRecord)

    @Update
    suspend fun update(rec: VideoRecord)

    @Delete
    suspend fun delete(rec: VideoRecord)

    @Query("SELECT * FROM videorecord WHERE id = :id")
    suspend fun getById(id: String): VideoRecord?
}