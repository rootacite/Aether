package com.acitelight.aether.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VideoRecord::class], version = 1)
abstract class VideoRecordDatabase : RoomDatabase()  {
    abstract fun userDao(): VideoRecordDao

    companion object {
        @Volatile
        private var INSTANCE: VideoRecordDatabase? = null

        fun getDatabase(context: Context): VideoRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoRecordDatabase::class.java,
                    "videorecord_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}