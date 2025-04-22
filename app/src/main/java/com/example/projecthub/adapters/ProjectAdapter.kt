// ProjectAdapter.kt
package com.example.projecthub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.projecthub.R
import com.example.projecthub.models.Project
import java.text.SimpleDateFormat
import java.util.*

class ProjectAdapter(
    private var projects: List<Project>,
    private val listener: OnProjectClickListener
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    interface OnProjectClickListener {
        fun onProjectClick(project: Project)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position])
    }

    override fun getItemCount() = projects.size

    fun updateProjects(newProjects: List<Project>) {
        this.projects = newProjects
        notifyDataSetChanged()
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardProject: CardView = itemView.findViewById(R.id.card_project)
        private val tvProjectName: TextView = itemView.findViewById(R.id.tv_project_name)
        private val tvProjectDescription: TextView = itemView.findViewById(R.id.tv_project_description)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        private val tvMemberCount: TextView = itemView.findViewById(R.id.tv_member_count)
        private val tvTaskCount: TextView = itemView.findViewById(R.id.tv_task_count)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)

        fun bind(project: Project) {
            tvProjectName.text = project.name
            tvProjectDescription.text = project.description

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvDueDate.text = "Due: ${dateFormat.format(Date(project.endDate))}"

            // Member and task count
            tvMemberCount.text = "${project.members.size} members"
            tvTaskCount.text = "${project.tasks.size} tasks"

            // Progress
            progressBar.progress = project.progress
            tvProgress.text = "${project.progress}%"

            // Status
            if (project.isCompleted) {
                tvStatus.text = "Completed"
                tvStatus.setTextColor(itemView.context.resources.getColor(R.color.taskStatusCompleted))
            } else {
                tvStatus.text = "Ongoing"
                tvStatus.setTextColor(itemView.context.resources.getColor(R.color.taskStatusInProgress))
            }

            // Item click
            cardProject.setOnClickListener {
                listener.onProjectClick(project)
            }
        }
    }
}