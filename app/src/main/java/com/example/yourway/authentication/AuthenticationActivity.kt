package com.example.yourway.authentication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yourway.BaseActivity
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.userprofile.CreateUserProfileActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthenticationActivity : AppCompatActivity() {
    private lateinit var ivAuthLogo : ImageView
    private lateinit var etAuthEmail : EditText
    private lateinit var etAuthPassword : EditText
    private lateinit var ivAuthShowPass :ImageView
    private lateinit var btnAuthSignUp : Button
    private lateinit var btnAuthSignIn :Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authentication)

        checkIfUserIsLoggedIn()

        ivAuthLogo = findViewById(R.id.iv_auth_logo)
        etAuthEmail = findViewById(R.id.et_auth_email)
        etAuthPassword = findViewById(R.id.et_auth_password)
        ivAuthShowPass = findViewById(R.id.iv_auth_showpass)
        btnAuthSignUp = findViewById(R.id.btn_auth_signUp)
        btnAuthSignIn = findViewById(R.id.btn_auth_signIn)

        ivAuthShowPass.setOnClickListener{
            showPasswordTemporarily(etAuthPassword)
        }

        btnAuthSignIn.setOnClickListener {
            val email = etAuthEmail.text.toString().trim()
            val password = etAuthPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast("Please enter email and password", this)
                return@setOnClickListener
            }

            // Basic email format check (can be improved)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast("Please enter a valid email address", this)
                return@setOnClickListener
            }

            val userData = UserData(email, password)

            userData.signIn { signInResult ->
                if (signInResult == UserData.SIGN_IN_SUCCESS) {

                    saveUserDataToPreferences(email,password,"success")

                    val intent = Intent(this@AuthenticationActivity, BaseActivity::class.java)
                    intent.putExtra("email",email)

                    startActivity(intent)
                    finish()
                } else {
                    Toast("Sign-in failed. Please check your credentials.", this)
                }
            }
        }

        btnAuthSignUp.setOnClickListener {
            val email = etAuthEmail.text.toString().trim()
            val password = etAuthPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast("Please enter email and password", this)
                return@setOnClickListener
            }

            // Basic email format check (can be improved)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast("Please enter a valid email address", this)
                return@setOnClickListener
            }

            val userData = UserData(email, password)

            userData.signUp { resultCode ->
                when (resultCode) {
                    UserData.USER_EXISTS -> {
                        userData.signIn { signInResult ->
                            if (signInResult == UserData.SIGN_IN_SUCCESS) {
                                val intent = Intent(this, BaseActivity::class.java)
                                intent.putExtra("email",email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast("Sign-in failed. Please check your credentials.", this)
                            }
                        }
                    }
                    UserData.NEW_USER -> {
                        val intent = Intent(this, CreateUserProfileActivity::class.java)
                        intent.putExtra("email",email)
                        startActivity(intent)
                        finish()
                    }
                    UserData.SIGN_IN_FAILED -> {

                        Toast("Sign-up failed. Please try again.", this)
                    }
                }
            }
        }
    }

    private fun saveUserDataToPreferences(email: String, password: String, status: String) {
        var username :String

        lifecycleScope.launch {
            // Attempt to get the username by email
            val user = getUsernameByEmail(email) // Pass the email here
            if (user != null) {
                username = user
                val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("email", email)
                editor.putString("password", password)
                editor.putString("status", status)
                editor.putString("username",username)
                editor.apply() // Save changes asynchronously
                Toast( "Username on Signin: $username", applicationContext)
            } else {
                println("No Username found for the given email.")
                Toast(  "No username found for this $email", applicationContext)
            }
        }
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

    private fun checkIfUserIsLoggedIn() {
        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val status = sharedPref.getString("status", null)
        val email = sharedPref.getString("email", null)

        if (status == "success" && email != null) {
            // User is already logged in, navigate to BaseActivity
            val intent = Intent(this@AuthenticationActivity, BaseActivity::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
            finish() // Close the AuthenticationActivity
        }
    }

    private fun showPasswordTemporarily(passwordEditText: EditText) {
        // Show the password
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        // Revert back to hidden password after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordEditText.setSelection(passwordEditText.text.length) // Maintain cursor position
        }, 3000)  // 3000 milliseconds = 3 seconds
    }
}