// MainActivity.kt
package com.example.projecthub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.projecthub.databinding.ActivityMainBinding
import com.example.projecthub.R
import com.example.projecthub.CreateProjectBottomSheet
import com.example.projecthub.fragments.*
import com.example.projecthub.utils.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        // Validate session again in MainActivity
        if (!sessionManager.isLoggedIn()) {
            // No valid session, redirect to login
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

//        setupBottomNavigation()
        setupBottomNavigation()

        // Load Dashboard Fragment by default
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_projects -> {
                    loadFragment(ProjectsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

        // Add project button click
        binding.fabAddProject.setOnClickListener {
            CreateProjectBottomSheet().show(supportFragmentManager, "CreateProjectBottomSheet")
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}