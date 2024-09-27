package com.example.yourway.forum

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ForumDetailFragment : Fragment() {

    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()
    private lateinit var forumId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forum_detail, container, false)

        // Initialize RecyclerView and Adapter
        forumRecyclerView = view.findViewById(R.id.rv_forum_comments)
        forumRecyclerView.layoutManager = LinearLayoutManager(context)

        commentAdapter = CommentAdapter(commentList)
        forumRecyclerView.adapter = commentAdapter

        // Retrieve the forumId from arguments
        forumId = arguments?.getString("forumId").toString()
        if (forumId != null) {
            fetchComments()
        } else {
            // Handle error case when forumId is null
            Toast( "Forum ID not found",requireContext())
        }

        return view
    }

    private fun fetchComments() {
        FirebaseFirestore.getInstance().collection("comments")
            .whereEqualTo("forumId", forumId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener
                commentList.clear()
                for (document in value) {
                    val comment = document.toObject(Comment::class.java)
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }
}
