package com.example.yourway.explore.userlist

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yourway.BaseActivity
import com.example.yourway.R

class UserListAdapter(private val users: MutableList<User>) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvUsername: TextView = itemView.findViewById(R.id.tvChatUlLastMessage)
        private val tvDisplayName: TextView = itemView.findViewById(R.id.tvChatUlDisplayName)
        private val ivProfileImage: ImageView = itemView.findViewById(R.id.ivChatUlProfileImage)

        fun bind(user: User) {
            tvUsername.text = user.username
            tvDisplayName.text = user.displayName

            // Load the profile image using the correct property (imageSrc)
            Glide.with(itemView.context)
                .load(user.imageSrc) // Ensure this points to the correct property
                .circleCrop()  // This makes the image circular
                .placeholder(R.drawable.placeholder_image) // Optional: Add a placeholder image
                .into(ivProfileImage)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, BaseActivity::class.java)
                intent.putExtra("user", user)
                itemView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_explore_userlist_item, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}