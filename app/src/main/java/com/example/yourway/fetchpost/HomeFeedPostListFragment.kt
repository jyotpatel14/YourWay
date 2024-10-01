package com.example.yourway.fetchpost

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.R
import com.example.yourway.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
class HomeFeedPostListFragment : Fragment() {

    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val firestore = FirebaseFirestore.getInstance()
    private var lastVisiblePost: DocumentSnapshot? = null
    private var isLoading = false
    private val pageSize = 4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_feed_post_list, container, false)

        recyclerView = view.findViewById(R.id.rv_homefeed)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_homefeed_post)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter { postId -> openPostFragment(postId) }
        recyclerView.adapter = postAdapter

        swipeRefreshLayout.setOnRefreshListener {
            refreshPosts()
        }

        // Infinite scroll listener
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    fetchPosts()
                }
            }
        })

        fetchPosts()

        return view
    }

    private fun refreshPosts() {
        // Reset adapter and last visible post
        postAdapter.submitList(emptyList())
        lastVisiblePost = null
        fetchPosts()
    }

    private fun fetchPosts() {
        isLoading = true
        val query: Query = if (lastVisiblePost == null) {
            firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())
        } else {
            Toast("new fetched",requireContext())
            firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisiblePost)
                .limit(pageSize.toLong())
        }

        query.get().addOnSuccessListener { snapshot ->
            // Check if snapshot is empty before proceeding
            if (!snapshot.isEmpty) {
                // Get the new posts and directly map them to Post objects
                val newPosts = snapshot.documents.mapNotNull { document ->
                    document.toObject(Post::class.java)
                }

                // Submit the new posts to the adapter
                val updatedList = postAdapter.currentList.toMutableList()
                updatedList.addAll(newPosts) // Avoid duplicates
                postAdapter.submitList(updatedList)

                lastVisiblePost = snapshot.documents[snapshot.size() - 1] // Update the last visible post
            } else {
                // If no new posts are available, reset lastVisiblePost
                lastVisiblePost = null
            }
            isLoading = false
            swipeRefreshLayout.isRefreshing = false
        }.addOnFailureListener { exception ->
            // Handle failure
            exception.printStackTrace() // Log the error
            Toast("Error fetching posts: ${exception.message}", requireContext())
            isLoading = false
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun openPostFragment(postId: String) {
        val postFragment = PostFragment.newInstance(postId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fcv_base, postFragment)
            .addToBackStack(null)
            .commit()
    }
}

