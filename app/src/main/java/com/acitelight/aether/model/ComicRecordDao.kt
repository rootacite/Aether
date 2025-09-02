package com.acitelight.aether.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicRecordDao {
    @Query("SELECT * FROM comicrecord")
    fun getAll(): Flow<List<ComicRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rec: ComicRecord)

    @Update
    suspend fun update(rec: ComicRecord)

    @Delete
    suspend fun delete(rec: ComicRecord)

    @Query("SELECT * FROM comicrecord WHERE id = :id")
    suspend fun getById(id: Int): ComicRecord?
}