package com.example.projecthub.models

data class User(
    val id: String,
    val name: String,
    val email: String,
    val profilePicUrl: String? = null
)