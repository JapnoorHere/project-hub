// TaskAdapter.kt
package com.example.projecthub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projecthub.models.Task
import com.example.projecthub.models.TaskStatus
import java.text.SimpleDateFormat
import java.util.*
import com.example.projecthub.R
class TaskAdapter(
    private var tasks: List<Task>,
    private val listener: OnTaskClickListener,
    private val currentUserId: String // Add this parameter
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onTaskClick(task: Task)
        fun onStatusChanged(task: Task, newStatus: TaskStatus)
        fun onProgressChanged(task: Task, progress: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        this.tasks = newTasks
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardTask: CardView = itemView.findViewById(R.id.card_task)
        private val tvTaskTitle: TextView = itemView.findViewById(R.id.tv_task_title)
        private val tvTaskDescription: TextView = itemView.findViewById(R.id.tv_task_description)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tv_due_date)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_status)
        private val ivAssignee: ImageView = itemView.findViewById(R.id.iv_assignee)
        private val tvAssigneeName: TextView = itemView.findViewById(R.id.tv_assignee_name)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val ivNextStatus: ImageView = itemView.findViewById(R.id.iv_next_status)

        fun bind(task: Task) {
            tvTaskTitle.text = task.title
            tvTaskDescription.text = task.description

            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            tvDueDate.text = "Due: ${dateFormat.format(Date(task.dueDate))}"

            // Set status
            tvStatus.text = when (task.status) {
                TaskStatus.PENDING -> "Pending"
                TaskStatus.IN_PROGRESS -> "In Progress"
                TaskStatus.COMPLETED -> "Completed"
            }

            tvStatus.setTextColor(when (task.status) {
                TaskStatus.PENDING -> itemView.context.getColor(R.color.taskStatusPending)
                TaskStatus.IN_PROGRESS -> itemView.context.getColor(R.color.taskStatusInProgress)
                TaskStatus.COMPLETED -> itemView.context.getColor(R.color.taskStatusCompleted)
            })

            // Set assignee
            tvAssigneeName.text = task.assignedTo.name

            if (task.assignedTo.profilePicUrl != null) {
                Glide.with(itemView.context)
                    .load(task.assignedTo.profilePicUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(ivAssignee)
            } else {
                ivAssignee.setImageResource(R.drawable.ic_profile)
            }

            // Set progress
            progressBar.progress = task.progress
            tvProgress.text = "${task.progress}%"

            // Set next status button visibility
            if (task.status == TaskStatus.COMPLETED) {
                ivNextStatus.visibility = View.GONE
            } else {
                ivNextStatus.visibility = View.VISIBLE
                ivNextStatus.setImageResource(
                    if (task.status == TaskStatus.PENDING) R.drawable.ic_play
                    else R.drawable.ic_check
                )
            }

            if (task.status == TaskStatus.COMPLETED) {
                ivNextStatus.visibility = View.GONE
            } else {
                // Only show the next status button if current user is the task assignee
                if (task.assignedTo.id == currentUserId) {
                    ivNextStatus.visibility = View.VISIBLE
                    ivNextStatus.setImageResource(
                        if (task.status == TaskStatus.PENDING) R.drawable.ic_play
                        else R.drawable.ic_check
                    )
                } else {
                    ivNextStatus.visibility = View.GONE
                }
            }


            // Setup click listeners
            cardTask.setOnClickListener {
                listener.onTaskClick(task)
            }

            ivNextStatus.setOnClickListener {
                val newStatus = if (task.status == TaskStatus.PENDING) {
                    TaskStatus.IN_PROGRESS
                } else {
                    TaskStatus.COMPLETED
                }
                listener.onStatusChanged(task, newStatus)
            }

            progressBar.setOnClickListener {
                // For simplicity, we'll just toggle between a few progress values
                // In a real app, you might want a better UI for this
                val newProgress = when {
                    task.progress < 25 -> 25
                    task.progress < 50 -> 50
                    task.progress < 75 -> 75
                    task.progress < 100 -> 100
                    else -> 0
                }
                listener.onProgressChanged(task, newProgress)
            }
        }
    }
}