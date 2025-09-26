package com.acitelight.aether.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoRecord (
    @PrimaryKey(autoGenerate = false) val id: String = "",
    @ColumnInfo(name = "name") val klass: String = "",
    @ColumnInfo(name = "position") val position: Long,
    @ColumnInfo(name = "time") val time: Long,
    @ColumnInfo(name = "group") val group: String
)