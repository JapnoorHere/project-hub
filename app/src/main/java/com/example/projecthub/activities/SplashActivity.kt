// SplashActivity.kt
package com.example.projecthub.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import retrofit2.Callback
import androidx.appcompat.app.AppCompatActivity
import com.example.projecthub.R
import com.example.projecthub.api.ApiClient
import com.example.projecthub.models.Project
import com.example.projecthub.utils.SessionManager
import retrofit2.Call
import retrofit2.Response

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sessionManager = SessionManager(this)

        // Delay for 2 seconds then check if user is logged in
        Handler(Looper.getMainLooper()).postDelayed({
            if (sessionManager.validateSession(this)) {
                // User is logged in, verify token validity with API
                verifySession()
            } else {
                // User is not logged in, go to LoginActivity
                navigateToLogin()
            }
        }, 2000) // 2 seconds delay
    }

    private fun verifySession() {
        // Optional: Make a lightweight API call to verify token is still valid
        ApiClient.apiService.getUserProjects().enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                if (response.isSuccessful) {
                    // Token is valid, go to MainActivity
                    navigateToMain()
                } else {
                    // Token is invalid, clear session and go to login
                    sessionManager.clearSession()
                    navigateToLogin()
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                // Network error - if it's just a network issue, we'll still try to go to main
                // The main activity will handle retrying
                navigateToMain()
            }
        })
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}