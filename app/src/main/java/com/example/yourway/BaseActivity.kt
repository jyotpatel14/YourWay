package com.example.yourway

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.example.yourway.explore.ExploreFragment
import com.example.yourway.userprofile.DisplayUserProfile
import com.google.android.material.bottomnavigation.BottomNavigationView

class BaseActivity : AppCompatActivity() {

    private lateinit var fcvBase: FragmentContainerView
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)

        bottomNavigationView = findViewById(R.id.bottom_nav)
        fcvBase = findViewById(R.id.fcv_base)

        var addPostLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {

//                        setFragment(Explore())


                }
                bottomNavigationView.selectedItemId = R.id.home
            }

        // Set the initial fragment
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
//                    setFragment(Explore())
                    return@setOnItemSelectedListener true
                }

                R.id.explore -> {
                    setFragment(ExploreFragment())
                    return@setOnItemSelectedListener true
                }

                R.id.addpost -> {
                    val intent = Intent(this@BaseActivity, AddPostActivity::class.java)
                    addPostLauncher.launch(intent)

                    return@setOnItemSelectedListener true
                }

                R.id.messages -> {
//                    setFragment(Contacts())
                    return@setOnItemSelectedListener true
                }

                R.id.profile -> {
                    val fragment = DisplayUserProfile() // Replace with your Fragment class
                    val email = intent.getStringExtra("email") // Retrieve the email from the intent

                    // Create a Bundle to hold the email
                    val bundle = Bundle().apply {
                        putString("email", email) // Add the email to the Bundle
                    }

                    fragment.arguments = bundle
                    setFragment(fragment)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }


    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fcv_base, fragment).commit()
    }
}