package com.example.yourway.userprofile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.yourway.R
import com.example.yourway.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.net.URL

class DisplayUserProfile : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvDisplayName: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvLink: TextView
    private lateinit var ivLinkIcon: ImageView

    private var username: String? = null
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the email from the arguments
        email = arguments?.getString("email") ?: return // Return if email is null

        // Fetch username by email using coroutines
        lifecycleScope.launch {
            // Attempt to get the username by email
            val user = getUsernameByEmail(email) // Pass the email here
            if (user != null) {
                username = user
                // Fetch user data once username is retrieved
                fetchUserData()
                Toast( "Username: $username", requireContext())
            } else {
                println("No Username found for the given email.")
                Toast(  "No username found for this $email", requireContext())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_display_user_profile, container, false)

        tvUsername = view.findViewById(R.id.tv_display_profile_username)
        ivProfileImage = view.findViewById(R.id.iv_display_profile_profileImage)
        tvDisplayName = view.findViewById(R.id.tv_display_profile_displayName)
        tvBio = view.findViewById(R.id.tv_display_profile_bio)
        tvLink = view.findViewById(R.id.tv_display_profile_link)
        ivLinkIcon = view.findViewById(R.id.iv_display_profile_linkIcon)

        return view
    }

    private suspend fun getUsernameByEmail(email: String): String? {
        val db = FirebaseFirestore.getInstance()

        return try {
            // Reference to the "usernametoemail" collection
            val document = db.collection("usernametoemail").document(email).get().await()
            if (document.exists()) {
                // Check if the "username" field exists in the document
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

    private fun fetchUserData() {
        // Fetch the user's basic profile data using the retrieved username
        username?.let {
            loadUserProfileBasicData(it) { userProfileBasicData ->
                if (userProfileBasicData != null) {
                    // Populate UI with the user data
                    tvUsername.text = username
                    tvDisplayName.text = userProfileBasicData.displayName
                    tvBio.text = userProfileBasicData.bio
                    tvLink.text = userProfileBasicData.link
                    loadProfileImageView(userProfileBasicData.imageSrc)
                } else {
                    Toast("Unable to load profile", requireContext())
                }
            }
        }
    }

    private fun loadProfileImageView(imageSrc: String?) {
        lifecycleScope.launch {
            try {
                val bitmap = loadBitmapFromUrl(imageSrc)
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadBitmapFromUrl(urlString: String?): Bitmap? {
        return try {
            val url = URL(urlString)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun loadUserProfileBasicData(
        username: String,
        callback: (UserProfileBasicData?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(username).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val userData = document.data
                    val userProfileBasicData = UserProfileBasicData(
                        displayName = userData?.get("displayName") as? String,
                        bio = userData?.get("bio") as? String,
                        link = userData?.get("link") as? String,
                        email = userData?.get("email") as? String,
                        imageSrc = userData?.get("imageSrc") as? String
                    )
                    callback(userProfileBasicData)
                } else {
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }
}

