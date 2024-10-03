package com.example.yourway.fetchpost

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.yourway.Toast
import com.example.yourway.forum.ForumAdapter
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text

class PostAdapter(
    private val postList:List<Post>,
    private val onItemClick: (String) -> Unit
): RecyclerView.Adapter<PostAdapter.PostViewHolder>(){

    private val TAG = "PostAdapter"

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvUsername: TextView = itemView.findViewById(R.id.tv_post_username)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_post_description)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_post_title)
        val ivProfileImage: ImageView = itemView.findViewById(R.id.iv_post_userProfile)
        val vpImages: ViewPager2 = itemView.findViewById(R.id.vp_post)
        val tvlikesCount:TextView = itemView.findViewById(R.id.tv_post_likes)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
        val btnShowComment: ImageButton = itemView.findViewById(R.id.ibtn_post_show_comment)
        val btnPostLike: ImageButton = itemView.findViewById(R.id.ibtn_post_like)

    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostAdapter.PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent,false)
        return PostViewHolder(view)
    }

    override fun getItemCount(): Int = postList.size

    override fun onBindViewHolder(holder: PostAdapter.PostViewHolder, position: Int) {
        val post = postList[position]
        holder.tvUsername.text = post.username
        holder.tvTitle.text = post.title
        holder.tvDescription.text = post.description
        holder.tvlikesCount.text = post.likes.toString()
        holder.tvCommentCount.text = post.commentCount.toString()

        // Add real-time listeners for likes and comments
        observePostChanges(post.postId, holder)

        holder.btnShowComment.setOnClickListener {
            Toast("hello , {${post.postId}}",holder.itemView.context)
            //now i want to call the comments bottom sheet fragment here
            val commentsBottomSheet = CommentBottomSheetFragment.newInstance(post.postId)
            commentsBottomSheet.show((holder.itemView.context as FragmentActivity).supportFragmentManager, "CommentsBottomSheet")

        }

        holder.btnPostLike.setOnClickListener {
            // Check if the button is already in the "liked" state
            if (holder.btnPostLike.tag != "liked") {
                val db = FirebaseFirestore.getInstance().collection("posts").document(post.postId)

                // Increment the "likes" field in Firestore
                db.update("likes", FieldValue.increment(1))
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully incremented like count for post: ${post.postId}")

                        // Change the button icon to the "liked" state
                        holder.btnPostLike.setImageResource(R.drawable.baseline_thumb_up_alt_24)
                        holder.btnPostLike.tag = "liked" // Set the tag to "liked"
                        Toast("Post liked!", holder.itemView.context)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error incrementing like count: ${exception.message}")
                        Toast("Failed to like post", holder.itemView.context)
                    }
            }
        }



        loadUserProfileImage(post.username,holder.ivProfileImage)
        val imageAdapter = ImagePagerAdapter(post.imageUrls)
        holder.vpImages.adapter = imageAdapter


    }

    private fun observePostChanges(postId: String, holder: PostAdapter.PostViewHolder) {
        val db = FirebaseFirestore.getInstance().collection("posts").document(postId)

        db.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                Log.e(TAG, "Listen failed: $exception")
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                val newLikesCount = documentSnapshot.getLong("likes") ?: 0
                val newCommentCount = documentSnapshot.getLong("commentCount") ?: 0

                // Update UI with the new values
                holder.tvlikesCount.text = newLikesCount.toString()
                holder.tvCommentCount.text = newCommentCount.toString()
            } else {
                Log.d(TAG, "Current data: null")
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
