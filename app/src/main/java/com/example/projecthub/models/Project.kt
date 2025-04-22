package com.example.projecthub.models

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val startDate: Long,
    val endDate: Long,
    val createdBy: User,
    val members: List<User>,
    val tasks: List<Task>,
    val isCompleted: Boolean = false,
    val progress: Int = 0
)