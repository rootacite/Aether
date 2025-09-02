package com.acitelight.aether.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [ComicRecord::class], version = 1)
abstract class ComicRecordDatabase : RoomDatabase() {
    abstract fun userDao(): ComicRecordDao

    companion object {
        @Volatile
        private var INSTANCE: ComicRecordDatabase? = null

        fun getDatabase(context: Context): ComicRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ComicRecordDatabase::class.java,
                    "comicrecord_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}