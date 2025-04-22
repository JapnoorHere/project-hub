// SessionManager.kt
package com.example.projecthub.utils


import com.example.projecthub.api.*

import android.content.Context
import android.content.SharedPreferences
import com.example.projecthub.models.User

// SessionManager.kt
class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("PROJECT_MANAGER_PREFS", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_PROFILE_PIC = "profile_pic"
        private const val KEY_LOGIN_TIME = "login_time"
    }

    fun saveAuthToken(token: String) {
        editor.putString(KEY_TOKEN, token)
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
        editor.apply()

        // Update the API client with the new token
        ApiClient.setToken(token)
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun saveUserDetails(user: User) {
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_EMAIL, user.email)
        user.profilePicUrl?.let { editor.putString(KEY_PROFILE_PIC, it) }
        editor.apply()
    }

    fun getUserDetails(): User? {
        val id = sharedPreferences.getString(KEY_USER_ID, null) ?: return null
        val name = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
        val email = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
        val profilePic = sharedPreferences.getString(KEY_PROFILE_PIC, null)

        return User(id, name, email, profilePic)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
        ApiClient.setToken("")
    }

    fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        return !token.isNullOrEmpty() && getUserDetails() != null
    }

    // Add token validation in SplashActivity
    fun validateSession(context: Context): Boolean {
        if (!isLoggedIn()) return false

        // Setup API token for the session
        val token = getAuthToken()
        if (!token.isNullOrEmpty()) {
            ApiClient.setToken(token)
            return true
        }

        return false
    }
}