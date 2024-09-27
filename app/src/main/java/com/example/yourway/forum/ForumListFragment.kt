package com.example.yourway.forum

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ForumListFragment : Fragment() {

    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var forumAdapter: ForumAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val forumList = mutableListOf<Forum>()

    private var lastVisible: DocumentSnapshot? = null
    private val forumsPerPage = 10 // Limit to 10 documents per fetch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forum_list, container, false)

        forumRecyclerView = view.findViewById(R.id.rv_forum_list)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_forum_list)
        forumRecyclerView.layoutManager = LinearLayoutManager(context)

        forumAdapter = ForumAdapter(forumList) { forumId ->
            navigateToForumDetail(forumId)
        }
        forumRecyclerView.adapter = forumAdapter

        // Initial fetch of forums
        fetchForums()

        // Set up SwipeRefreshLayout to refresh forum list
        swipeRefreshLayout.setOnRefreshListener {
            refreshForums()
        }

        // Set up endless scroll for pagination
        forumRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (totalItemCount <= (lastVisibleItem + 2)) {
                    fetchMoreForums()
                }
            }
        })

        return view
    }

    // Fetch the initial set of forums
    private fun fetchForums() {
        FirebaseFirestore.getInstance().collection("forums")
//            .orderBy("upvotes", Query.Direction.DESCENDING) // Filter by upvotes
//            .orderBy("createdAt", Query.Direction.DESCENDING) // Filter by recent upload time
            .limit(forumsPerPage.toLong()) // Limit to 10 forums
            .get()
            .addOnSuccessListener { querySnapshot ->
                forumList.clear() // Clear the list for fresh fetch
                for (document in querySnapshot) {
                    val forum = document.toObject(Forum::class.java)
                    forumList.add(forum)
                }
                forumAdapter.notifyDataSetChanged()

                // Get the last visible document for pagination
                if (querySnapshot.documents.isNotEmpty()) {
                    lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                }

                swipeRefreshLayout.isRefreshing = false // Hide the refresh indicator
            }
    }

    // Refresh the forums (Pull to refresh)
    private fun refreshForums() {
        fetchForums()
    }

    // Fetch more forums when scrolling reaches the bottom (pagination)
    private fun fetchMoreForums() {
        if (lastVisible != null) {
            FirebaseFirestore.getInstance().collection("forums")
//                .orderBy("upvotes", Query.Direction.DESCENDING) // Filter by upvotes
//                .orderBy("createdAt", Query.Direction.DESCENDING) // Filter by recent upload time
                .startAfter(lastVisible) // Start after the last fetched document
                .limit(forumsPerPage.toLong()) // Limit to 10 more forums
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val forum = document.toObject(Forum::class.java)
                        forumList.add(forum)
                    }
                    forumAdapter.notifyDataSetChanged()

                    // Update the last visible document for further pagination
                    if (querySnapshot.documents.isNotEmpty()) {
                        lastVisible = querySnapshot.documents[querySnapshot.size() - 1]
                    }
                }
        }
    }

    // Navigate to forum detail screen
    private fun navigateToForumDetail(forumId: String) {
        val fragment = ForumDetailFragment()
        val bundle = Bundle()
        bundle.putString("forumId", forumId)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_forum, fragment)
            .addToBackStack(null)
            .commit()
    }
}
