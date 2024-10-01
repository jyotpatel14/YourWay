package com.example.yourway.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.yourway.R
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.yourway.Toast
import com.example.yourway.userprofile.UserProfile
import com.example.yourway.userprofile.postimagegrid.UserPostImageGridRVFragment
import com.example.yourway.userprofile.postimagegrid.VPAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DisplayOtherUserProfile : Fragment() {

    // Views
    private lateinit var usernameTextView: TextView
    private lateinit var displayNameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var linkTextView: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var vpAdapter: VPAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_other_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        usernameTextView = view.findViewById(R.id.tv_display_profile_username)
        displayNameTextView = view.findViewById(R.id.tv_display_profile_displayName)
        bioTextView = view.findViewById(R.id.tv_display_profile_bio)
        linkTextView = view.findViewById(R.id.tv_display_profile_link)
        profileImageView = view.findViewById(R.id.iv_display_profile_profileImage)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_display_profile)

        // Get username from arguments
        val username = arguments?.getString("username") ?: return

        // Fetch user profile from Firestore
        fetchUserProfileFromDatabase(username, view)

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            // On refresh, fetch the latest user profile data
            fetchUserProfileFromDatabase(username, view)
        }
    }

    private fun setupVP(view: View, username: String) {
        tabLayout = view.findViewById(R.id.tabLayout_profile)
        viewPager = view.findViewById(R.id.viewPager_profile)

        vpAdapter = VPAdapter(childFragmentManager, lifecycle)
        vpAdapter.addFragment(UserPostImageGridRVFragment.newInstance(username), "Posts")
        // Add other fragments if needed
        viewPager.adapter = vpAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = vpAdapter.getPageTitle(position)
        }.attach()
    }

    private fun loadUserProfileToUI(userProfile: UserProfile) {
        // Update views with user profile data
        usernameTextView.text = userProfile.username
        displayNameTextView.text = userProfile.displayName
        bioTextView.text = userProfile.bio
        linkTextView.text = userProfile.link

        // Load image using Glide and make it round
        Glide.with(this)
            .load(userProfile.imgSrc)
            .placeholder(R.drawable.placeholder_image) // Placeholder image
            .transform(CircleCrop()) // Make image round
            .into(profileImageView)
    }

    private fun fetchUserProfileFromDatabase(username: String, view: View) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(username)

        // Fetch the user profile from Firestore
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val documentSnapshot = docRef.get().await()
                if (documentSnapshot.exists()) {
                    // Extract data from document
                    val userProfile = UserProfile(
                        username = documentSnapshot.id,
                        displayName = documentSnapshot.getString("displayName") ?: "",
                        bio = documentSnapshot.getString("bio") ?: "",
                        link = documentSnapshot.getString("link") ?: "",
                        imgSrc = documentSnapshot.getString("imageSrc") ?: ""
                    )

                    // Update UI with the fetched user profile data
                    loadUserProfileToUI(userProfile)

                    // Set up ViewPager after loading user profile
                    setupVP(view, username)
                } else {
                    Toast("Profile not found", requireContext())
                }
            } catch (e: Exception) {
                Toast("Error fetching profile: ${e.message}", requireContext())
            } finally {
                // Stop the refreshing animation
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
