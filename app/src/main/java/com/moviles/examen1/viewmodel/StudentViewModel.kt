package com.moviles.examen1.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.examen1.models.Student
import com.moviles.examen1.network.RetrofitInstance
import com.moviles.examen1.services.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel : ViewModel() {
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> get() = _students

    private val _studentsByCourse = MutableStateFlow<List<Student>>(emptyList())
    val studentsByCourse: StateFlow<List<Student>> get() = _studentsByCourse

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun fetchStudents() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _students.value = RetrofitInstance.api.getStudents()
                Log.i("StudentViewModel", "Fetched all students: ${_students.value}")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createStudent(student: Student, onSuccess: () -> Unit = {}, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.createStudent(student)
                _students.value += response
                if (student.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                    _studentsByCourse.value += response
                }
                Log.i("StudentViewModel", "Created student: $response")

                // Enviar notificación local
                val notificationMessage = "Estudiante: ${student.name}, se ha inscrito al curso: ${student.courseName}"
                NotificationService.showNotification(
                    context,
                    "Nuevo estudiante registrado",
                    notificationMessage
                )

                onSuccess()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("StudentViewModel", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error creating student: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun fetchStudentsByCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _studentsByCourse.value = RetrofitInstance.api.getStudentsByCourse(courseId)
                Log.i("StudentViewModel", "Fetched students for course $courseId")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students by course: ${e.message}")
                _studentsByCourse.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createStudent(student: Student, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.createStudent(student)
                _students.value += response
                if (student.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                    _studentsByCourse.value += response
                }
                Log.i("StudentViewModel", "Created student: $response")
                onSuccess()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("StudentViewModel", "HTTP Error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error creating student: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateStudent(student: Student, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updated = RetrofitInstance.api.updateStudent(student.id ?: return@launch, student)
                _students.value = _students.value.map { if (it.id == updated.id) updated else it }
                if (updated.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                    _studentsByCourse.value = _studentsByCourse.value.map { if (it.id == updated.id) updated else it }
                }
                Log.i("StudentViewModel", "Updated student: $updated")
                onSuccess()
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error updating student: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStudent(id: Int, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                RetrofitInstance.api.deleteStudent(id)
                val deletedStudent = _students.value.find { it.id == id }
                _students.value = _students.value.filter { it.id != id }
                deletedStudent?.let {
                    if (it.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                        _studentsByCourse.value = _studentsByCourse.value.filter { student -> student.id != id }
                    }
                }
                Log.i("StudentViewModel", "Deleted student ID: $id")
                onSuccess()
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error deleting student: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStudents() {
        _studentsByCourse.value = emptyList()
    }
}