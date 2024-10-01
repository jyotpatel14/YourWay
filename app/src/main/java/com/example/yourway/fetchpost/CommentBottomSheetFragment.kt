package com.example.yourway.fetchpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CommentBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    companion object {
        private const val ARG_POST_ID = "postId"

        fun newInstance(postId: String): CommentBottomSheetFragment {
            return CommentBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_POST_ID, postId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_comment_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)

        val postId = arguments?.getString(ARG_POST_ID)
        postId?.let { fetchComments(it) }
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.rv_post_comments)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentAdapter = CommentAdapter()
        recyclerView.adapter = commentAdapter
    }

    private fun fetchComments(postId: String) {
        val commentsRef = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        commentsRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val comments = snapshot.toObjects(Comment::class.java)
                    commentAdapter.submitList(comments)
                }
            }
            .addOnFailureListener { exception ->
                handleFetchError(exception)
            }
    }

    private fun handleFetchError(exception: Exception) {
        // Handle error logic, such as displaying a Toast or logging the error
        exception.printStackTrace()
    }
}
