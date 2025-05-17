package com.moviles.examen1.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.examen1.data.CourseDao
import com.moviles.examen1.models.Course
import com.moviles.examen1.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CourseViewModel(private val courseDao: CourseDao) : ViewModel() {
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> get() = _courses

    private val _loadingMessage = MutableStateFlow<String?>(null)
    val loadingMessage: StateFlow<String?> get() = _loadingMessage

    init {
        fetchCourses()
    }

    fun fetchCourses() {
        viewModelScope.launch {
            try {
                val coursesFromApi = RetrofitInstance.api.getCourses()
                _courses.value = coursesFromApi
                _loadingMessage.value = "Datos cargados desde API"

                courseDao.deleteAllCourses()
                courseDao.insertCourses(coursesFromApi)

                Log.i("CourseViewModel", "Fetched courses from API and saved to DB")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error fetching from API: ${e.message}")

                val coursesFromDb = courseDao.getAllCourses()
                _courses.value = coursesFromDb
                _loadingMessage.value = "Datos cargados desde almacenamiento local"

                Log.i("CourseViewModel", "Loaded courses from local DB")
            }
        }
    }

    fun clearLoadingMessage() {
        _loadingMessage.value = null
    }

    fun addCourse(course: Course, uri: Uri?, context: Context) {
        viewModelScope.launch {
            try {
                val imagePart: MultipartBody.Part? = uri?.let {
                    val inputStream = context.contentResolver.openInputStream(it) ?: return@let null
                    val bytes = inputStream.readBytes()
                    val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("File", "filename.jpg", requestFile)
                }

                val name = course.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val description = course.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val professor = course.professor.toRequestBody("text/plain".toMediaTypeOrNull())
                val schedule = course.schedule.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitInstance.api.addCourse(
                    name,
                    description,
                    professor,
                    schedule,
                    imagePart
                )
                // Actualizar lista en memoria
                _courses.value += response

                // Guardar en base local
                courseDao.insertCourse(response)

            } catch (e: Exception) {
                Log.e("ViewModelError", "Error uploading: ${e.message}", e)
            }
        }
    }

    fun updateCourse(course: Course, imageUri: Uri? = null) {
        viewModelScope.launch {
            try {
                Log.i("CourseViewModel", "Updating course: $course, imageUri: $imageUri")
                val updated = RetrofitInstance.api.updateCourse(course.id ?: return@launch, course)

                // Actualizar lista en memoria
                _courses.value = _courses.value.map { if (it.id == updated.id) updated else it }

                // Actualizar en base local
                courseDao.updateCourse(updated)

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
                // Actualizar lista en memoria
                _courses.value = _courses.value.filter { it.id != id }

                // Eliminar de base local
                val courseToDelete = _courses.value.find { it.id == id }
                if (courseToDelete != null) {
                    courseDao.deleteCourse(courseToDelete)
                }

                Log.i("CourseViewModel", "Deleted course ID: $id")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error deleting: ${e.message}")
            }
        }
    }
}
