package com.moviles.examen1.network
import com.moviles.examen1.models.Course
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("api/course")
    suspend fun getCourses(): List<Course>

    @POST("api/course")
    suspend fun addCourse(@Body courseDto: Course): Course

    @PUT("api/course/{id}")
    suspend fun updateCourse(@Path("id") id: Int?, @Body courseDto: Course): Course

    @DELETE("api/course/{id}")
    suspend fun deleteCourse(@Path("id") id: Int?): Response<Unit>
}