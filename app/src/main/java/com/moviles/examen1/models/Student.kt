package com.moviles.examen1.models

data class Student(
    val id: Int?,
    val name: String,
    val email: String,
    val phone: String,
    val courseId: Int,
    val course: Course?,
    val courseName: String?=null
)