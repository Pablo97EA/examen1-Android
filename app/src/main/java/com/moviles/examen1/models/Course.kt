package com.moviles.examen1.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "schedule")
    val schedule: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "professor")
    val professor: String,

    @ColumnInfo(name = "imageUrl")
    val imageUrl: String?
)