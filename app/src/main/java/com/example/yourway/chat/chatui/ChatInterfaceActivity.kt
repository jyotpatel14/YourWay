package com.example.yourway.chat.chatui

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentContainerView
import com.example.yourway.R
import com.google.firebase.firestore.FirebaseFirestore


class ChatInterfaceActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var ivBack: ImageView
    private lateinit var tvChatName: TextView
    private lateinit var fcvChatUi: FragmentContainerView
    private lateinit var ibtnAttach: ImageButton
    private lateinit var etMessage: EditText
    private lateinit var ibtnCancel: ImageButton
    private lateinit var ibtnSend: ImageButton
    private lateinit var chatId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_interface)

        // Initialize views
        toolbar = findViewById(R.id.toolbar_chatui)
        ivBack = findViewById(R.id.iv_chatui_back_button)
        tvChatName = findViewById(R.id.tv_chatui_chatname)
        fcvChatUi = findViewById(R.id.fcv_chatui)
        ibtnAttach = findViewById(R.id.ibtn_chatui_attach)
        etMessage = findViewById(R.id.et_chatui_messagebox)
        ibtnCancel = findViewById(R.id.ibtn_chatui_cancel)
        ibtnSend = findViewById(R.id.ibtn_chatui_send)


        // Retrieve chatId from the intent
        chatId = intent.getStringExtra("chatId") ?: throw IllegalArgumentException("Chat ID not provided")
        getChatDisplayName(chatId) { displayname ->
            tvChatName.text = displayname
        }

        setupToolbar()
        loadMessagesFragment()

        // Handle Send button click
        ibtnSend.setOnClickListener {
            sendMessage()
        }

        // Handle Attach button click (for selecting and sending media)
        ibtnAttach.setOnClickListener {
            selectMedia()
        }


    }

    private fun setupToolbar() {
//        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ivBack.setOnClickListener {
            finish() // Return to previous activity
        }
    }

    private fun getChatDisplayName(chatId: String, callback: (String?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve either groupName or displayName
                    val displayName = document.getString("groupName") ?: document.getString("displayName")

                    // Return the retrieved value through the callback
                    callback(displayName)
                } else {
                    Log.d("getChatDisplayName", "No such document")
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("getChatDisplayName", "Error getting document: ", e)
                callback(null)
            }
    }

    private fun loadMessagesFragment() {
        // Load the MessagesFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcv_chatui, MessagesFragment.newInstance(chatId))
            .commit()
    }

    private fun sendMessage() {
        val messageText = etMessage.text.toString()
        if (messageText.isNotEmpty()) {
            // Call the fragment's send message function
            (supportFragmentManager.findFragmentById(R.id.fcv_chatui) as? MessagesFragment)?.sendMessage(messageText)
            etMessage.text.clear() // Clear the input after sending
        }
    }

    private fun selectMedia() {
        // Handle media selection logic
        // You can use an intent to open file picker or gallery
    }
}