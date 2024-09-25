package com.example.yourway.userprofile

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
import com.bumptech.glide.load.resource.bitmap.CircleCrop // For circular cropping
import com.example.yourway.Toast
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DisplayUserProfile : Fragment() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    // Views
    private lateinit var usernameTextView: TextView
    private lateinit var displayNameTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var linkTextView: TextView
    private lateinit var profileImageView: ImageView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SharedPreferencesHelper
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        // Initialize Views
        usernameTextView = view.findViewById(R.id.tv_display_profile_username)
        displayNameTextView = view.findViewById(R.id.tv_display_profile_displayName)
        bioTextView = view.findViewById(R.id.tv_display_profile_bio)
        linkTextView = view.findViewById(R.id.tv_display_profile_link)
        profileImageView = view.findViewById(R.id.iv_display_profile_profileImage)

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_display_profile)

        // Get email from arguments
        val email = arguments?.getString("email") ?: return

        // Load user profile from SharedPreferences if available
        if (sharedPreferencesHelper.isUserProfileAvailable()) {
            loadUserProfileFromPreferences()
        } else {
            // Launch coroutine to fetch user profile
            CoroutineScope(Dispatchers.Main).launch {
                fetchUserProfileFromDatabase(email)
            }
        }

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            // On refresh, fetch the latest user profile data and update SharedPreferences
            CoroutineScope(Dispatchers.Main).launch {
                fetchUserProfileFromDatabase(email)
                swipeRefreshLayout.isRefreshing = false // Stop refresh animation
            }
        }
    }

    private fun loadUserProfileFromPreferences() {
        val userProfile = sharedPreferencesHelper.getUserProfile()
        if (userProfile != null) {
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
    }

    private suspend fun fetchUserProfileFromDatabase(email: String) {
        val username = getUsernameByEmail(email)
        if (username == null) {
            Toast("Username not found for email", requireContext())
            return
        }

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(username)

        // Add a real-time listener to the document
        docRef.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                // Handle error
                Toast("Exception: ${firebaseFirestoreException.message}", requireContext())
                return@addSnapshotListener
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Extract data from document
                val userProfile = UserProfile(
                    username = documentSnapshot.id,
                    displayName = documentSnapshot.getString("displayName") ?: "",
                    bio = documentSnapshot.getString("bio") ?: "",
                    link = documentSnapshot.getString("link") ?: "",
                    imgSrc = documentSnapshot.getString("imageSrc") ?: ""
                )

                // Save the new data to SharedPreferences
                sharedPreferencesHelper.saveUserProfile(userProfile)

                // Update the UI with the new data
                loadUserProfileFromPreferences()
            } else {
                // Handle the case where the document doesn't exist
                Toast("Profile not found", requireContext())
            }
        }
    }

    private suspend fun getUsernameByEmail(email: String): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Reference to the "usernametoemail" collection
            val document = db.collection("usernametoemail").document(email).get().await()
            if (document.exists()) {
                document.getString("username")
            } else {
                println("Document does not exist for this email")
                null // Document does not exist
            }
        } catch (e: Exception) {
            println("Error retrieving document: ${e.message}")
            null // Return null on failure
        }
    }
}

