// ApiService.kt
package com.example.projecthub.api

import com.example.projecthub.models.MemberIdsRequest
import com.example.projecthub.models.Project
import com.example.projecthub.models.Task
import com.example.projecthub.models.TaskStatus
import com.example.projecthub.models.User
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    // User endpoints
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<LoginResponse>



    @GET("auth/users")
    fun getAllUsers(): Call<List<User>>

    // ApiService.kt - add this method
    @PUT("projects/{projectId}/members")
    fun addProjectMembers(
        @Path("projectId") projectId: String,
        @Body request: MemberIdsRequest
    ): Call<Project>

    // Project endpoints
    @GET("projects")
    fun getUserProjects(): Call<List<Project>>

    @POST("projects")
    fun createProject(@Body project: ProjectRequest): Call<Project>

    @GET("projects/{projectId}")
    fun getProjectDetails(@Path("projectId") projectId: String): Call<Project>

    @PUT("projects/{projectId}")
    fun updateProject(@Path("projectId") projectId: String, @Body project: ProjectRequest): Call<Project>

    @PUT("projects/{projectId}/complete")
    fun markProjectAsCompleted(@Path("projectId") projectId: String): Call<Project>

    // Task endpoints
    @POST("projects/{projectId}/tasks")
    fun createTask(@Path("projectId") projectId: String, @Body task: TaskRequest): Call<Task>

    @PUT("tasks/{taskId}")
    fun updateTask(@Path("taskId") taskId: String, @Body task: TaskRequest): Call<Task>

    @PUT("tasks/{taskId}/status")
    fun updateTaskStatus(@Path("taskId") taskId: String, @Body statusRequest: StatusRequest): Call<Task>

    @PUT("tasks/{taskId}/progress")
    fun updateTaskProgress(@Path("taskId") taskId: String, @Body progressRequest: ProgressRequest): Call<Task>
}

// Request and Response data classes
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginResponse(val token: String, val user: User)
data class ProjectRequest(val name: String, val description: String, val startDate: Long, val endDate: Long, val memberIds: List<String>)
data class TaskRequest(val title: String, val description: String, val dueDate: Long, val assignedToId: String)
data class StatusRequest(val status: TaskStatus)
data class ProgressRequest(val progress: Int)