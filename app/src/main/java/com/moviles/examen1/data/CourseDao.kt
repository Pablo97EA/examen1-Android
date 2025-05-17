package com.moviles.examen1.data

import androidx.room.*
import com.moviles.examen1.models.Course

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>): List<Long>

    @Update
    suspend fun updateCourse(course: Course): Int

    @Delete
    suspend fun deleteCourse(course: Course): Int

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses(): Int
}
