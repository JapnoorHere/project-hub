// ProfileFragment.kt
package com.example.projecthub.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.projecthub.R
import com.example.projecthub.activities.LoginActivity
import com.example.projecthub.api.ApiClient
import com.example.projecthub.databinding.FragmentProfileBinding
import com.example.projecthub.models.Project
import com.example.projecthub.models.TaskStatus
import com.example.projecthub.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ProfileFragment.kt
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Load user profile
        loadUserProfile()

        // Set up logout button
        binding?.btnLogout?.setOnClickListener {
            logout()
        }
    }

    private fun loadUserProfile() {
        val user = sessionManager.getUserDetails()

        user?.let {
            binding?.tvUserName?.text = it.name
            binding?.tvUserEmail?.text = it.email

            // Load profile image if available
            it.profilePicUrl?.let { url ->
                binding?.ivProfilePic?.let { imageView ->
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                        .into(imageView)
                }
            }
        }

        // Get project stats
        fetchProjectStats()
    }

    private fun fetchProjectStats() {
        binding?.progressBar?.visibility = View.VISIBLE

        ApiClient.apiService.getUserProjects().enqueue(object : Callback<List<Project>> {
            override fun onResponse(
                call: Call<List<Project>>,
                response: Response<List<Project>>
            ) {
                // First check if binding is null (fragment view destroyed)
                if (_binding == null) return

                binding?.progressBar?.visibility = View.GONE

                if (response.isSuccessful) {
                    val projects = response.body() ?: listOf()
                    updateProjectStats(projects)
                } else {
                    context?.let {
                        Toast.makeText(it, "Failed to load project statistics", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                // First check if binding is null (fragment view destroyed)
                if (_binding == null) return

                binding?.progressBar?.visibility = View.GONE

                context?.let {
                    Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateProjectStats(projects: List<Project>) {
        if (_binding == null) return

        val totalProjects = projects.size
        val completedProjects = projects.count { it.isCompleted }
        val ongoingProjects = totalProjects - completedProjects

        binding?.tvTotalProjects?.text = totalProjects.toString()
        binding?.tvCompletedProjects?.text = completedProjects.toString()
        binding?.tvOngoingProjects?.text = ongoingProjects.toString()

        // Get all tasks from all projects
        val allTasks = projects.flatMap { it.tasks }
        val totalTasks = allTasks.size
        val completedTasks = allTasks.count { it.status == TaskStatus.COMPLETED }

        binding?.tvTotalTasks?.text = totalTasks.toString()
        binding?.tvCompletedTasks?.text = completedTasks.toString()

        // Set task completion rate
        val completionRate = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt()
        } else {
            0
        }
        binding?.tvTaskCompletionRate?.text = "$completionRate%"
    }

    private fun logout() {
        sessionManager.clearSession()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}