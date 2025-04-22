// UserSelectionAdapter.kt
package com.example.projecthub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projecthub.R
import com.example.projecthub.models.User

class UserSelectionAdapter(
    private var users: List<User>,
    private val selectedUsers: MutableSet<String> = mutableSetOf()
) : RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_selection, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }

    fun getSelectedUsers(): List<String> {
        return selectedUsers.toList()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfilePic: ImageView = itemView.findViewById(R.id.iv_profile_pic)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_user)

        fun bind(user: User) {
            tvUserName.text = user.name
            tvUserEmail.text = user.email

            if (user.profilePicUrl != null) {
                Glide.with(itemView.context)
                    .load(user.profilePicUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(ivProfilePic)
            } else {
                ivProfilePic.setImageResource(R.drawable.ic_profile)
            }

            // Set checkbox state based on selection
            checkBox.isChecked = selectedUsers.contains(user.id)

            // Handle item click
            itemView.setOnClickListener {
                checkBox.toggle()
            }

            // Handle checkbox changes
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUsers.add(user.id)
                } else {
                    selectedUsers.remove(user.id)
                }
            }
        }
    }
}