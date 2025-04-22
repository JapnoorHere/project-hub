// UserAdapter.kt
package com.example.projecthub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projecthub.models.User

import com.example.projecthub.R

class UserAdapter(
    private var users: List<User>,
    private val listener: OnUserClickListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    interface OnUserClickListener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
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

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProfilePic: ImageView = itemView.findViewById(R.id.iv_profile_pic)
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)

        fun bind(user: User) {
            tvUserName.text = user.name

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

            itemView.setOnClickListener {
                listener.onUserClick(user)
            }
        }
    }
}