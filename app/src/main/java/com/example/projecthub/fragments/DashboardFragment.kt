// DashboardFragment.kt
package com.example.projecthub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.projecthub.R
import com.example.projecthub.api.ApiClient
import com.example.projecthub.databinding.FragmentDashboardBinding
import com.example.projecthub.models.Project
import com.example.projecthub.models.TaskStatus
import com.example.projecthub.utils.SessionManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    // Use nullable binding to avoid null pointer exceptions
    private val binding get() = _binding

    private lateinit var sessionManager: SessionManager
    private var projectList: List<Project> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val user = sessionManager.getUserDetails()

        binding?.tvUserName?.text = "Hello, ${user?.name ?: "User"}!"

        // Setup swipe refresh
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            loadDashboardData()
        }

        // Load initial data
        loadDashboardData()
    }

    private fun loadDashboardData() {
        binding?.progressBar?.visibility = View.VISIBLE

        ApiClient.apiService.getUserProjects().enqueue(object : Callback<List<Project>> {
            override fun onResponse(call: Call<List<Project>>, response: Response<List<Project>>) {
                // Check if binding is null before using it
                if (_binding == null) return

                binding?.progressBar?.visibility = View.GONE
                binding?.swipeRefreshLayout?.isRefreshing = false

                if (response.isSuccessful) {
                    projectList = response.body() ?: listOf()
                    updateDashboardUI(projectList)
                } else {
                    Toast.makeText(context, "Failed to load projects", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Project>>, t: Throwable) {
                // Check if binding is null before using it
                if (_binding == null) return

                binding?.progressBar?.visibility = View.GONE
                binding?.swipeRefreshLayout?.isRefreshing = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDashboardUI(projects: List<Project>) {
        // Check if binding is null before using it
        if (_binding == null) return

        // Update project stats
        val totalProjects = projects.size
        val completedProjects = projects.count { it.isCompleted }
        val ongoingProjects = totalProjects - completedProjects

        binding?.tvTotalProjects?.text = totalProjects.toString()
        binding?.tvCompletedProjects?.text = completedProjects.toString()
        binding?.tvOngoingProjects?.text = ongoingProjects.toString()

        // Calculate task statistics
        val allTasks = projects.flatMap { it.tasks }
        val pendingTasks = allTasks.count { it.status == TaskStatus.PENDING }
        val inProgressTasks = allTasks.count { it.status == TaskStatus.IN_PROGRESS }
        val completedTasks = allTasks.count { it.status == TaskStatus.COMPLETED }

        binding?.tvPendingTasks?.text = pendingTasks.toString()
        binding?.tvInProgressTasks?.text = inProgressTasks.toString()
        binding?.tvCompletedTasks?.text = completedTasks.toString()

        // Update pie chart if there are tasks
        if (allTasks.isNotEmpty()) {
            updateTasksPieChart(pendingTasks, inProgressTasks, completedTasks)
        }

        // Update project progress bars for ongoing projects
        val ongoingProjectsList = projects.filter { !it.isCompleted }.take(3)

        if (ongoingProjectsList.isEmpty()) {
            binding?.cardRecentProjects?.visibility = View.GONE
        } else {
            binding?.cardRecentProjects?.visibility = View.VISIBLE

            // Clear previous views first
            binding?.layoutRecentProjects?.removeAllViews()

            // Inflate project progress views
            for (project in ongoingProjectsList) {
                val projectView = LayoutInflater.from(context).inflate(
                    R.layout.item_project_progress,
                    binding?.layoutRecentProjects,
                    false
                )

                val tvProjectName = projectView.findViewById<android.widget.TextView>(R.id.tv_project_name)
                val progressBar = projectView.findViewById<android.widget.ProgressBar>(R.id.progress_bar)
                val tvProgress = projectView.findViewById<android.widget.TextView>(R.id.tv_progress)

                tvProjectName.text = project.name
                progressBar.progress = project.progress
                tvProgress.text = "${project.progress}%"

                binding?.layoutRecentProjects?.addView(projectView)
            }
        }
    }

    private fun updateTasksPieChart(pending: Int, inProgress: Int, completed: Int) {
        if (_binding == null) return

        val entries = ArrayList<PieEntry>()

        // Add entries in correct order
        if (pending > 0) entries.add(PieEntry(pending.toFloat(), "Pending"))
        if (inProgress > 0) entries.add(PieEntry(inProgress.toFloat(), "In Progress"))
        if (completed > 0) entries.add(PieEntry(completed.toFloat(), "Completed"))

        if (entries.isEmpty()) return // Add this check to avoid empty chart

        val dataSet = PieDataSet(entries, "Tasks")

        // Set colors in the SAME ORDER as entries were added
        dataSet.colors = listOf(
            requireContext().getColor(R.color.taskStatusPending),      // Orange/Yellow for Pending
            requireContext().getColor(R.color.taskStatusInProgress),   // Blue for In Progress
            requireContext().getColor(R.color.taskStatusCompleted)     // Teal/Green for Completed
        )

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(requireContext().getColor(R.color.white))

        binding?.pieChartTasks?.apply {
            this.data = data
            description.isEnabled = false
            setEntryLabelColor(requireContext().getColor(R.color.white))
            legend.isEnabled = true  // Set to false if you don't want the legend in the chart
            setDrawEntryLabels(false)
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}