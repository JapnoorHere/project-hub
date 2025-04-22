// CreateTaskActivity.kt
package com.example.projecthub.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projecthub.api.ApiClient
import com.example.projecthub.api.TaskRequest
import com.example.projecthub.databinding.ActivityCreateTaskBinding
import com.example.projecthub.models.Project
import com.example.projecthub.models.Task
import com.example.projecthub.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTaskBinding

    private var projectId: String = ""
    private var dueDate: Long = 0
    private var selectedUserId: String = ""
    private var projectMembers: List<User> = listOf()

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Task"

        // Get project ID from intent
        projectId = intent.getStringExtra("PROJECT_ID") ?: ""
        if (projectId.isEmpty()) {
            Toast.makeText(this, "Invalid project ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup date picker
        setupDatePicker()

        // Load project members
        loadProjectMembers()

        // Setup create button
        binding.btnCreateTask.setOnClickListener {
            createTask()
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 7) // Default to one week from now
        dueDate = calendar.timeInMillis
        binding.tvDueDate.text = dateFormat.format(Date(dueDate))

        binding.layoutDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = dueDate

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dueDate = cal.timeInMillis
                    binding.tvDueDate.text = dateFormat.format(Date(dueDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun loadProjectMembers() {
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.apiService.getProjectDetails(projectId).enqueue(object : Callback<Project> {
            override fun onResponse(call: Call<Project>, response: Response<Project>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val project = response.body()
                    project?.let {
                        projectMembers = it.members
                        setupAssigneeSpinner()
                    }
                } else {
                    Toast.makeText(this@CreateTaskActivity, "Failed to load project members", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Project>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@CreateTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAssigneeSpinner() {
        if (projectMembers.isEmpty()) {
            binding.spinnerAssignee.isEnabled = false
            binding.tvNoMembers.visibility = View.VISIBLE
            return
        }

        binding.tvNoMembers.visibility = View.GONE

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            projectMembers.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAssignee.adapter = adapter

        binding.spinnerAssignee.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUserId = projectMembers[position].id
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedUserId = ""
            }
        })

        // Select the first user by default
        if (projectMembers.isNotEmpty()) {
            selectedUserId = projectMembers[0].id
        }
    }

    private fun createTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Task title is required"
            return
        }

        if (selectedUserId.isEmpty()) {
            Toast.makeText(this, "Please select an assignee", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateTask.isEnabled = false

        val taskRequest = TaskRequest(
            title = title,
            description = description,
            dueDate = dueDate,
            assignedToId = selectedUserId
        )

        ApiClient.apiService.createTask(projectId, taskRequest).enqueue(object : Callback<Task> {
            override fun onResponse(call: Call<Task>, response: Response<Task>) {
                binding.progressBar.visibility = View.GONE
                binding.btnCreateTask.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@CreateTaskActivity, "Task created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateTaskActivity, "Failed to create task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Task>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.btnCreateTask.isEnabled = true
                Toast.makeText(this@CreateTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}