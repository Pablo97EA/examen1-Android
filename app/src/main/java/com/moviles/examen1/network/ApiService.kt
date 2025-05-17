package com.moviles.examen1.network

import com.moviles.examen1.models.Course
import com.moviles.examen1.models.Student
import okhttp3.MultipartBody

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ------ COURSE ENDPOINTS -------
    @GET("api/course")
    suspend fun getCourses(): List<Course>

    @POST("api/course")
    suspend fun addCourse(@Body courseDto: Course): Course

    @Multipart
    @POST("api/course/{id}/upload-image")
    suspend fun uploadCourseImage(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    @PUT("api/course/{id}")
    suspend fun updateCourse(@Path("id") id: Int?, @Body courseDto: Course): Course

    @DELETE("api/course/{id}")
    suspend fun deleteCourse(@Path("id") id: Int?): Response<Unit>


    // ------ STUDENT ENDPOINTS -------
    @GET("api/student")
    suspend fun getStudents(): List<Student>

    @GET("api/student/{id}")
    suspend fun getStudentById(@Path("id") id: Int): Student

    @GET("api/student/bycourse/{courseId}")
    suspend fun getStudentsByCourse(@Path("courseId") courseId: Int): List<Student>

    @POST("api/student")
    suspend fun createStudent(@Body studentDto: Student): Student

    @PUT("api/student/{id}")
    suspend fun updateStudent(@Path("id") id: Int, @Body studentDto: Student): Student

    @DELETE("api/student/{id}")
    suspend fun deleteStudent(@Path("id") id: Int): Response<Unit>
}
