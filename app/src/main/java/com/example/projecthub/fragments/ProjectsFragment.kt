// ProjectsFragment.kt
package com.example.projecthub.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projecthub.activities.ProjectDetailsActivity
import com.example.projecthub.adapters.ProjectAdapter
import com.example.projecthub.api.ApiClient
import com.example.projecthub.databinding.FragmentProjectsBinding
import com.example.projecthub.models.Project
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ProjectsFragment.kt
class ProjectsFragment : Fragment(), ProjectAdapter.OnProjectClickListener {
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding

    private lateinit var projectAdapter: ProjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Setup swipe refresh
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            loadProjects()
        }

        // Initial data load
        loadProjects()
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter(arrayListOf(), this)
        binding?.recyclerViewProjects?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
        }
    }

    fun loadProjects() {
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.tvNoProjects?.visibility = View.GONE

        ApiClient.apiService.getUserProjects().enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                // First check if binding is null (fragment view destroyed)
                if (_binding == null) return

                binding?.progressBar?.visibility = View.GONE
                binding?.swipeRefreshLayout?.isRefreshing = false

                if (response.isSuccessful) {
                    val projects = response.body() ?: listOf()
                    projectAdapter.updateProjects(projects)

                    // Show empty state if no projects
                    if (projects.isEmpty()) {
                        binding?.tvNoProjects?.visibility = View.VISIBLE
                    } else {
                        binding?.tvNoProjects?.visibility = View.GONE
                    }
                } else {
                    context?.let {
                        Toast.makeText(it, "Failed to load projects", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                // First check if binding is null (fragment view destroyed)
                if (_binding == null) return

                binding?.progressBar?.visibility = View.GONE
                binding?.swipeRefreshLayout?.isRefreshing = false

                context?.let {
                    Toast.makeText(it, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onProjectClick(project: Project) {
        val intent = Intent(requireContext(), ProjectDetailsActivity::class.java).apply {
            putExtra("PROJECT_ID", project.id)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}