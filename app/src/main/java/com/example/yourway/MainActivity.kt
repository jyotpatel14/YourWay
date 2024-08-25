package com.example.yourway

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import com.example.yourway.userprofile.CreateUserProfile

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentContainerView: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        fragmentContainerView = findViewById(R.id.fcv_main)

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fcv_main,CreateUserProfile())
        fragmentTransaction.commit()

    }
}