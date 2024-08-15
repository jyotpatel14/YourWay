package com.example.yourway.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.yourway.R
import com.example.yourway.databinding.FragmentPostReviewBinding
import com.google.firebase.firestore.FirebaseFirestore

class PostReview : Fragment() {

    private lateinit var binding: FragmentPostReviewBinding
    private lateinit var post: Post
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance()

        // Retrieve the Post object from arguments
        post = requireArguments().getParcelable("post")!!

        // Display the post title, description, and selected images
        binding.tvTitle.text = post.title
        binding.tvDescription.text = post.description
        // Bind the selected images to a RecyclerView or GridView

        binding.btnSubmitPost.setOnClickListener {
            // Upload logic to Firestore and Firebase Storage
            uploadPostToFirestore()
        }
    }

    private fun uploadPostToFirestore() {
        // Create a new post document in Firestore
        val postDoc = db.collection("posts").document()

        // Set the post data
        val postData = hashMapOf(
            "title" to post.title,
            "description" to post.description,
            // Add other post fields as needed
        )

        // Upload the post data to Firestore
        postDoc.set(postData)
            .addOnSuccessListener {
                Toast.makeText(context, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error uploading post: $e", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(post: Post): PostReview {
            val fragment = PostReview()
            val args = Bundle()
            args.putParcelable("post", post)
            fragment.arguments = args
            return fragment
        }
    }
}