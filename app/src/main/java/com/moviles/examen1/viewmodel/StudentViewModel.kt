package com.moviles.examen1.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.examen1.models.Student
import com.moviles.examen1.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel : ViewModel() {
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> get() = _students

    private val _studentsByCourse = MutableStateFlow<List<Student>>(emptyList())
    val studentsByCourse: StateFlow<List<Student>> get() = _studentsByCourse

    fun fetchStudents() {
        viewModelScope.launch {
            try {
                _students.value = RetrofitInstance.api.getStudents()
                Log.i("StudentViewModel", "Fetched all students: ${_students.value}")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students: ${e.message}")
            }
        }
    }

    fun fetchStudentsByCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                _studentsByCourse.value = RetrofitInstance.api.getStudentsByCourse(courseId)
                Log.i("StudentViewModel", "Fetched students for course $courseId: ${_studentsByCourse.value}")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error fetching students by course: ${e.message}")
                _studentsByCourse.value = emptyList()
            }
        }
    }

    fun createStudent(student: Student) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createStudent(student)
                _students.value += response
                // Actualizar también la lista filtrada si pertenece al mismo curso
                if (student.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                    _studentsByCourse.value += response
                }
                Log.i("StudentViewModel", "Created student: $response")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("StudentViewModel", "HTTP Error: ${e.message()}, Response Body: $errorBody")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error creating student: ${e.message}")
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                val updated = RetrofitInstance.api.updateStudent(student.id ?: return@launch, student)
                _students.value = _students.value.map { if (it.id == updated.id) updated else it }
                // Actualizar también la lista filtrada si pertenece al mismo curso
                if (updated.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                    _studentsByCourse.value = _studentsByCourse.value.map { if (it.id == updated.id) updated else it }
                }
                Log.i("StudentViewModel", "Updated student: $updated")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error updating student: ${e.message}")
            }
        }
    }

    fun deleteStudent(id: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteStudent(id)
                val deletedStudent = _students.value.find { it.id == id }
                _students.value = _students.value.filter { it.id != id }
                // Actualizar también la lista filtrada si pertenece al mismo curso
                deletedStudent?.let {
                    if (it.courseId == _studentsByCourse.value.firstOrNull()?.courseId) {
                        _studentsByCourse.value = _studentsByCourse.value.filter { student -> student.id != id }
                    }
                }
                Log.i("StudentViewModel", "Deleted student ID: $id")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error deleting student: ${e.message}")
            }
        }
    }
    fun clearStudents() {
        _studentsByCourse.value = emptyList()
    }
}