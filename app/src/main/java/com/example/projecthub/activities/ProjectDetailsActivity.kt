package com.example.projecthub.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projecthub.R
import com.example.projecthub.adapters.TaskAdapter
import com.example.projecthub.adapters.UserAdapter
import com.example.projecthub.adapters.UserSelectionAdapter
import com.example.projecthub.api.ApiClient
import com.example.projecthub.api.StatusRequest
import com.example.projecthub.databinding.ActivityProjectDetailsBinding
import com.example.projecthub.models.Project
import com.example.projecthub.api.ProgressRequest
import com.example.projecthub.api.ProjectRequest
import com.example.projecthub.models.Task
import com.example.projecthub.models.TaskStatus
import com.example.projecthub.models.User
import com.example.projecthub.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ProjectDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var memberAdapter: UserAdapter

    private var projectId: String = ""
    private var project: Project? = null
    private lateinit var sessionManager: SessionManager
    private var currentUserId: String = ""
    private var isProjectCreator: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        currentUserId = sessionManager.getUserDetails()?.id ?: ""

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Project Details"

        // Get project ID from intent
        projectId = intent.getStringExtra("PROJECT_ID") ?: ""
        if (projectId.isEmpty()) {
            Toast.makeText(this, "Invalid project ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup recycler views
        setupRecyclerViews()

        // Setup swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadProjectDetails()
        }

        // Add task floating action button
        binding.fabAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        // Load project details
        loadProjectDetails()
    }

    private fun setupRecyclerViews() {
        // Tasks RecyclerView
        taskAdapter = TaskAdapter(arrayListOf(), object : TaskAdapter.OnTaskClickListener {
            override fun onTaskClick(task: Task) {
                showTaskDetailsDialog(task)
            }

            override fun onStatusChanged(task: Task, newStatus: TaskStatus) {
                if (task.assignedTo.id == currentUserId) {
                    updateTaskStatus(task, newStatus)
                } else {
                    Toast.makeText(
                        this@ProjectDetailsActivity,
                        "Only the assigned user can change task status",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onProgressChanged(task: Task, progress: Int) {
                if (task.assignedTo.id == currentUserId) {
                    updateTaskProgress(task, progress)
                } else {
                    Toast.makeText(
                        this@ProjectDetailsActivity,
                        "Only the assigned user can update task progress",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        },
            currentUserId)

        binding.recyclerViewTasks.apply {
            layoutManager = LinearLayoutManager(this@ProjectDetailsActivity)
            adapter = taskAdapter
        }

        // Members RecyclerView
        memberAdapter = UserAdapter(arrayListOf(), object : UserAdapter.OnUserClickListener {
            override fun onUserClick(user: User) {
                // Could show user details or options in the future
            }
        })

        binding.recyclerViewMembers.apply {
            layoutManager = LinearLayoutManager(this@ProjectDetailsActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = memberAdapter
        }
    }

    private fun loadProjectDetails() {
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.apiService.getProjectDetails(projectId).enqueue(object : Callback<Project> {
            override fun onResponse(call: Call<Project>, response: Response<Project>) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false

                if (response.isSuccessful) {
                    project = response.body()
                    project?.let {
                        updateUI(it)
                    }
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Failed to load project details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Project>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.isRefreshing = false
                Toast.makeText(this@ProjectDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUI(project: Project) {
        // Update project info
        binding.tvProjectName.text = project.name
        binding.tvProjectDescription.text = project.description

        // Format dates
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvStartDate.text = "Start: ${dateFormat.format(Date(project.startDate))}"
        binding.tvEndDate.text = "Due: ${dateFormat.format(Date(project.endDate))}"

        // Update progress - using the new ID
        binding.progressBarProject.progress = project.progress
        binding.tvProgress.text = "${project.progress}%"

        // Update status
        if (project.isCompleted) {
            binding.tvStatus.text = "Completed"
            binding.tvStatus.setTextColor(getColor(R.color.taskStatusCompleted))
            binding.fabAddTask.visibility = View.GONE
        } else {
            binding.tvStatus.text = "Ongoing"
            binding.tvStatus.setTextColor(getColor(R.color.taskStatusInProgress))
            binding.fabAddTask.visibility = View.VISIBLE
        }

        // Update members
        memberAdapter.updateUsers(project.members)

        // Update tasks
        taskAdapter.updateTasks(project.tasks)

        // Show/hide empty views
        if (project.members.isEmpty()) {
            binding.tvNoMembers.visibility = View.VISIBLE
            binding.recyclerViewMembers.visibility = View.GONE
        } else {
            binding.tvNoMembers.visibility = View.GONE
            binding.recyclerViewMembers.visibility = View.VISIBLE
        }

        if (project.tasks.isEmpty()) {
            binding.tvNoTasks.visibility = View.VISIBLE
            binding.recyclerViewTasks.visibility = View.GONE
        } else {
            binding.tvNoTasks.visibility = View.GONE
            binding.recyclerViewTasks.visibility = View.VISIBLE
        }
        isProjectCreator = project.createdBy.id == currentUserId
        invalidateOptionsMenu()
    }

    private fun showAddTaskDialog() {
        // Will implement this method in the next file
        val intent = android.content.Intent(this, CreateTaskActivity::class.java).apply {
            putExtra("PROJECT_ID", projectId)
        }
        startActivity(intent)
    }

    private fun showTaskDetailsDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_details, null)

        // Set up the dialog view with task details
        val tvTaskTitle = dialogView.findViewById<TextView>(R.id.tv_task_title)
        val tvTaskDescription = dialogView.findViewById<TextView>(R.id.tv_task_description)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tv_status)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tv_due_date)
        val tvAssignedBy = dialogView.findViewById<TextView>(R.id.tv_assigned_by)
        val tvAssignedTo = dialogView.findViewById<TextView>(R.id.tv_assigned_to)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
        val tvProgress = dialogView.findViewById<TextView>(R.id.tv_progress)

        // Set values
        tvTaskTitle.text = task.title
        tvTaskDescription.text = task.description

        // Format date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        tvDueDate.text = "Due: ${dateFormat.format(Date(task.dueDate))}"

        // Set status
        when (task.status) {
            TaskStatus.PENDING -> {
                tvStatus.text = "Pending"
                tvStatus.setTextColor(getColor(R.color.taskStatusPending))
            }
            TaskStatus.IN_PROGRESS -> {
                tvStatus.text = "In Progress"
                tvStatus.setTextColor(getColor(R.color.taskStatusInProgress))
            }
            TaskStatus.COMPLETED -> {
                tvStatus.text = "Completed"
                tvStatus.setTextColor(getColor(R.color.taskStatusCompleted))
            }
        }

        // Assignees
        tvAssignedTo.text = "Assigned to: ${task.assignedTo.name}"
        tvAssignedBy.text = "Assigned by: ${task.assignedBy.name}"

        // Progress
        progressBar.progress = task.progress
        tvProgress.text = "${task.progress}%"

        // Create and show dialog
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Task Details")
            .setPositiveButton("Close", null)
            .create()
            .show()
    }

    private fun updateTaskStatus(task: Task, newStatus: TaskStatus) {
        if (task.assignedTo.id != currentUserId) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }
        ApiClient.apiService.updateTaskStatus(task.id, StatusRequest(newStatus))
            .enqueue(object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (response.isSuccessful) {
                        // Refresh project details to update progress
                        loadProjectDetails()
                    } else {
                        Toast.makeText(this@ProjectDetailsActivity, "Failed to update task status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Toast.makeText(this@ProjectDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateTaskProgress(task: Task, progress: Int) {
        if (task.assignedTo.id != currentUserId) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }
        ApiClient.apiService.updateTaskProgress(task.id, ProgressRequest(progress))
            .enqueue(object : Callback<Task> {
                override fun onResponse(call: Call<Task>, response: Response<Task>) {
                    if (response.isSuccessful) {
                        // Refresh project details to update progress
                        loadProjectDetails()
                    } else {
                        Toast.makeText(this@ProjectDetailsActivity, "Failed to update task progress", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Task>, t: Throwable) {
                    Toast.makeText(this@ProjectDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.project_details_menu, menu)

        // Hide complete project option if already completed
        project?.let {
            // Only show "Complete Project" if the user is the creator AND project is not completed
            menu.findItem(R.id.action_complete_project).isVisible =
                isProjectCreator && !it.isCompleted

            // Only show "Add Member" if the user is the creator
            menu.findItem(R.id.action_add_member).isVisible = isProjectCreator
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_add_member -> {
                showAddMemberDialog()
                true
            }
            R.id.action_complete_project -> {
                showCompleteProjectConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddMemberDialog() {
        // Double-check permission before showing dialog
        if (!isProjectCreator) {
            Toast.makeText(this, "Only the project creator can add members", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the dialog with a custom view
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_member, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_view_users)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progress_bar)
        val tvNoUsers = dialogView.findViewById<TextView>(R.id.tv_no_users)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val userSelectionAdapter = UserSelectionAdapter(emptyList())
        recyclerView.adapter = userSelectionAdapter

        // Create the dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Team Member")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null) // We'll set the listener later
            .create()

        // Show the dialog
        dialog.show()

        // Update the positive button to handle user selection
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedUserIds = userSelectionAdapter.getSelectedUsers()

            if (selectedUserIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addMembersToProject(selectedUserIds)
            dialog.dismiss()
        }

        // Fetch available users (users not already in the project)
        fetchAvailableUsers(userSelectionAdapter, progressBar, tvNoUsers, recyclerView)
    }

    private fun fetchAvailableUsers(
        adapter: UserSelectionAdapter,
        progressBar: ProgressBar,
        tvNoUsers: TextView,
        recyclerView: RecyclerView
    ) {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvNoUsers.visibility = View.GONE

        // Get all users
        ApiClient.apiService.getAllUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val allUsers = response.body() ?: emptyList()

                    // Filter out users who are already members of the project
                    val existingMemberIds = project?.members?.map { it.id } ?: emptyList()
                    val availableUsers = allUsers.filter { user ->
                        !existingMemberIds.contains(user.id)
                    }

                    if (availableUsers.isEmpty()) {
                        tvNoUsers.visibility = View.VISIBLE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        adapter.updateUsers(availableUsers)
                    }
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Failed to load users", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ProjectDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMembersToProject(memberIds: List<String>) {
        if (project == null) return

        // Double-check permission before API call
        if (!isProjectCreator) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        // Get existing member IDs
        val existingMemberIds = project?.members?.map { it.id } ?: emptyList()

        // Combine existing and new member IDs
        val updatedMemberIds = (existingMemberIds + memberIds).distinct()

        // Create project update request
        val projectUpdateRequest = ProjectRequest(
            name = project!!.name,
            description = project!!.description,
            startDate = project!!.startDate,
            endDate = project!!.endDate,
            memberIds = updatedMemberIds
        )

        // Update the project
        ApiClient.apiService.updateProject(project!!.id, projectUpdateRequest)
            .enqueue(object : Callback<Project> {
                override fun onResponse(call: Call<Project>, response: Response<Project>) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        Toast.makeText(this@ProjectDetailsActivity, "Members added successfully", Toast.LENGTH_SHORT).show()
                        loadProjectDetails() // Refresh project details
                    } else {
                        Toast.makeText(this@ProjectDetailsActivity, "Failed to add members", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Project>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@ProjectDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showCompleteProjectConfirmation() {
        // Double-check permission before showing dialog
        if (!isProjectCreator) {
            Toast.makeText(this, "Only the project creator can mark a project as completed", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Complete Project")
            .setMessage("Are you sure you want to mark this project as completed?")
            .setPositiveButton("Yes") { _, _ ->
                completeProject()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun completeProject() {
        if (!isProjectCreator) {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        ApiClient.apiService.markProjectAsCompleted(projectId).enqueue(object : Callback<Project> {
            override fun onResponse(call: Call<Project>, response: Response<Project>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    Toast.makeText(this@ProjectDetailsActivity, "Project marked as completed", Toast.LENGTH_SHORT).show()
                    loadProjectDetails()
                    invalidateOptionsMenu()
                } else {
                    Toast.makeText(this@ProjectDetailsActivity, "Failed to complete project", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Project>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ProjectDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}