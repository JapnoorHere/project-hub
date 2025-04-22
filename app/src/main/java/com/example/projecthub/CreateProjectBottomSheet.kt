// CreateProjectBottomSheet.kt
package com.example.projecthub

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.projecthub.api.ApiClient
import com.example.projecthub.api.ProjectRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.projecthub.databinding.BottomSheetCreateProjectBinding
import com.example.projecthub.fragments.ProjectsFragment
import com.example.projecthub.models.Project
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CreateProjectBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetCreateProjectBinding? = null
    private val binding get() = _binding!!

    private var startDate: Long = 0
    private var endDate: Long = 0
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up date pickers
        setupDatePickers()

        // Set up create button
        binding.btnCreate.setOnClickListener {
            createProject()
        }

        // Close button
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        // Set default dates to today and a week later
        startDate = calendar.timeInMillis
        binding.tvStartDate.text = dateFormat.format(Date(startDate))

        calendar.add(Calendar.DAY_OF_MONTH, 7)
        endDate = calendar.timeInMillis
        binding.tvEndDate.text = dateFormat.format(Date(endDate))

        // Start date picker
        binding.layoutStartDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = startDate

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    startDate = cal.timeInMillis
                    binding.tvStartDate.text = dateFormat.format(Date(startDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // End date picker
        binding.layoutEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = endDate

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    cal.set(Calendar.YEAR, year)
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    endDate = cal.timeInMillis
                    binding.tvEndDate.text = dateFormat.format(Date(endDate))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun createProject() {
        val name = binding.etProjectName.text.toString().trim()
        val description = binding.etProjectDescription.text.toString().trim()

        if (name.isEmpty()) {
            binding.etProjectName.error = "Project name is required"
            return
        }

        if (description.isEmpty()) {
            binding.etProjectDescription.error = "Project description is required"
            return
        }

        if (startDate >= endDate) {
            Toast.makeText(context, "End date must be after start date", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreate.isEnabled = false

        val projectRequest = ProjectRequest(
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            memberIds = listOf() // Start with just the creator
        )

        ApiClient.apiService.createProject(projectRequest).enqueue(object : Callback<Project> {
            override fun onResponse(call: Call<Project>, response: Response<Project>) {
                binding.progressBar.visibility = View.GONE
                binding.btnCreate.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(context, "Project created successfully", Toast.LENGTH_SHORT).show()

                    // Refresh projects fragment
                    (parentFragment as? ProjectsFragment)?.loadProjects()

                    // Dismiss the bottom sheet
                    dismiss()
                } else {
                    Toast.makeText(context, "Failed to create project", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Project>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.btnCreate.isEnabled = true
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}