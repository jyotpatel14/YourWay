package com.example.yourway.fetchpost

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
    private val onPostClick: (String) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    private val TAG = "PostAdapter" // Define a tag for logging

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        Log.d(TAG, "onCreateViewHolder: Creating ViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: Binding post at position $position")
        val post = getItem(position)
        holder.bind(post)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageIv: ImageView = itemView.findViewById(R.id.iv_post_userProfile)
        private val usernameTextView: TextView = itemView.findViewById(R.id.tv_post_username)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_post_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tv_post_description)
        private val likesTextView: TextView = itemView.findViewById(R.id.likesTextView)
        private val commentButton: Button = itemView.findViewById(R.id.commentButton)
        private val commentCountTextView: TextView = itemView.findViewById(R.id.commentCountTextView)
        private val viewPager: ViewPager2 = itemView.findViewById(R.id.viewPagerImages)

        fun bind(post: Post) {
            Log.d(TAG, "bind: Binding post with ID ${post.postId}")

            titleTextView.text = post.title
            descriptionTextView.text = post.description
            likesTextView.text = "Likes: ${post.likes}"
            commentCountTextView.text = "${post.commentCount} Comments"
            usernameTextView.text = post.username

            // Load user profile image
            loadUserProfileImage(post.username, profileImageIv)

            // Set up ViewPager for images
            val imageAdapter = ImagePagerAdapter(post.imageUrls)
            viewPager.adapter = imageAdapter

            // Handle post click
            itemView.setOnClickListener {
                Log.d(TAG, "onClick: Post clicked with ID ${post.postId}")
                onPostClick(post.postId)
            }

            // Handle comment button click
            commentButton.setOnClickListener {
                Log.d(TAG, "commentButton: Comment button clicked for post ID ${post.postId}")
                // Implement the comment feature here
                // You can start a new activity or show a dialog for commenting
            }
        }
    }

    private fun loadUserProfileImage(username: String, imageView: ImageView) {
        if (username.isEmpty()) {
            Log.w(TAG, "loadUserProfileImage: Username is empty")
            imageView.setImageResource(R.drawable.placeholder_image) // Set default image
            return
        }

        Log.d(TAG, "loadUserProfileImage: Loading image for username $username")
        val firestore = FirebaseFirestore.getInstance()
        val userDocRef = firestore.collection("users").document(username)

        // Fetch the user's document
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val imageSrc = document.getString("imageSrc")
                Log.d(TAG, "loadUserProfileImage: Found image source $imageSrc for username $username")

                // Use Glide to load the image into the ImageView
                if (!imageSrc.isNullOrEmpty()) {
                    Glide.with(imageView.context)
                        .load(imageSrc)
                        .circleCrop()
                        .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                        .into(imageView)
                } else {
                    Log.w(TAG, "loadUserProfileImage: No image source found for username $username")
                    imageView.setImageResource(R.drawable.placeholder_image) // Default image
                }
            } else {
                Log.w(TAG, "loadUserProfileImage: Document does not exist for username $username")
                imageView.setImageResource(R.drawable.placeholder_image) // Default image
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "loadUserProfileImage: Error fetching document for username $username", exception)
            imageView.setImageResource(R.drawable.placeholder_image) // Set a default image in case of an error
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.postId == newItem.postId
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
