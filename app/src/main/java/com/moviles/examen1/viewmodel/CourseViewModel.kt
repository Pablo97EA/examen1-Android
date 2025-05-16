package com.moviles.examen1.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.examen1.models.Course
import com.moviles.examen1.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import android.net.Uri

class CourseViewModel : ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> get() = _courses

    init {
        fetchCourses()
    }

     fun fetchCourses() {
        viewModelScope.launch {
            try {
                _courses.value = RetrofitInstance.api.getCourses()
                Log.i("CourseViewModel", "Fetched courses: ${_courses.value}")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error fetching: ${e.message}")
            }
        }
    }

    fun addCourse(course: Course, uri: Uri?){
        viewModelScope.launch {
            try {
                Log.i("ViewModelInfo", "Event: ${course}")
                val response = RetrofitInstance.api.addCourse(course)
                _courses.value += response
                Log.i("ViewModelInfo", "Response: ${response}")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ViewModelError", "HTTP Error: ${e.message()}, Response Body: $errorBody")
            } catch (e: Exception) {
                Log.e("ViewModelError", "Error: ${e.message}", e)
            }
        }
    }

    fun updateCourse(course: Course, imageUri: Uri? = null) {
        viewModelScope.launch {
            try {
                Log.i("CourseViewModel", "Updating course: $course, imageUri: $imageUri")
                // Aquí la lógica para actualizar la imagen si imageUri != null
                val updated = RetrofitInstance.api.updateCourse(course.id ?: return@launch, course)
                _courses.value = _courses.value.map { if (it.id == updated.id) updated else it }
                Log.i("CourseViewModel", "Updated course: $updated")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating course: ${e.message}")
            }
        }
    }


    fun deleteCourse(id: Int?) {
        if (id == null) return
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteCourse(id)
                _courses.value = _courses.value.filter { it.id != id }
                Log.i("CourseViewModel", "Deleted course ID: $id")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error deleting: ${e.message}")
            }
        }
    }
}
