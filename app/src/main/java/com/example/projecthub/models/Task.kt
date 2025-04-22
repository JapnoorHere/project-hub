package com.example.projecthub.models

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: Long,
    val assignedTo: User,
    val assignedBy: User,
    val status: TaskStatus = TaskStatus.PENDING,
    val progress: Int = 0
)