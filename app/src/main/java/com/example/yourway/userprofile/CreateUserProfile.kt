package com.example.yourway.userprofile

import Toast
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentHostCallback
import com.example.yourway.R
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class CreateUserProfile : Fragment() {

    private lateinit var btnEditProfileUsername: Button
    private lateinit var popupWindow: PopupWindow

    private lateinit var tvProfileUsername : TextView
    private lateinit var etProfileDisplayName : EditText
    private lateinit var etProfileBio : EditText
    private lateinit var etProfileLink : EditText

    private lateinit var btnProfileSelectImage : Button
    private lateinit var imageUri : Uri
    private lateinit var selectedImagePath : String

    private lateinit var ivProfileImage : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_create_user_profile, container, false)

        btnEditProfileUsername = view.findViewById(R.id.btn_profile_edit_username)
        tvProfileUsername = view.findViewById(R.id.tv_profile_username)
        etProfileDisplayName = view.findViewById(R.id.et_profile_display_name)
        etProfileBio = view.findViewById(R.id.et_profile_bio)
        etProfileLink = view.findViewById(R.id.et_profile_link)
        btnProfileSelectImage = view.findViewById(R.id.btn_profile_select_profile_image)
        ivProfileImage = view.findViewById(R.id.iv_profile_image)

        btnEditProfileUsername.setOnClickListener {
            showUsernameUpdatePopupWindow()
        }

        btnProfileSelectImage.setOnClickListener{
            val intent = ImagePicker.with(requireActivity())
                .provider(ImageProvider.GALLERY)
                .setMultipleAllowed(false)
                .crop()
                .galleryMimeTypes(
                    mimeTypes = arrayOf("image/png","image/jpg", "image/jpeg")
                )
                .createIntent()
            imagePickerLauncher.launch(intent)

        }





        loadUserProfileBasicData("jyot14"){
            userProfileBasicData ->
            if(userProfileBasicData != null){
                tvProfileUsername.text = "jyot14"
                etProfileDisplayName.setText(userProfileBasicData.displayName)
                etProfileBio.setText(userProfileBasicData.bio)
                etProfileLink.setText(userProfileBasicData.link)

               loadProfileImageView(userProfileBasicData.imageSrc)

            } else {
                Toast("Unable to Load Profile...",requireContext())
            }
        }

        //set image picker


        return view
    }
    private fun loadProfileImageView(imageSrc: String?) {
        val task = object : AsyncTask<String, Void, Bitmap>() {
            override fun doInBackground(vararg params: String): Bitmap? {
                var bitmap: Bitmap? = null
                try {
                    val url = URL(params[0])
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return bitmap
            }

            override fun onPostExecute(bitmap: Bitmap?) {
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap)
                }
            }
        }
        task.execute(imageSrc)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
            res ->
        if (res.resultCode == Activity.RESULT_OK){
            val clipdata = res.data?.clipData

            clipdata?.let {
                res.data?.data?.let { imageUri = it }
            }
            processImage(imageUri)

            ivProfileImage.setImageURI(imageUri)

        }
    }


    private fun processImage(imageUri: Uri) {
        imageUri?.let {
            val filePath = getRealPathFromURI(imageUri)
            if (filePath!=null){
                selectedImagePath = filePath
            }
        }
    }

    private fun getRealPathFromURI(imageUri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val file = File.createTempFile("temp",".jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
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

    private fun showUsernameUpdatePopupWindow() {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_username_update, null)

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.isFocusable = true
        popupWindow.animationStyle = R.style.PopupAnimation

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        val etPopupUsername = popupView.findViewById<EditText>(R.id.et_popup_username)
        val btnPopupCheck = popupView.findViewById<Button>(R.id.btn_popup_check)
        val btnPopupSaveChanges = popupView.findViewById<Button>(R.id.btn_popup_save_changes)
        val tvPopupError = popupView.findViewById<TextView>(R.id.tv_popup_error)
        val tvPopupCancel = popupView.findViewById<TextView>(R.id.tv_popup_cancel)

        btnPopupSaveChanges.isEnabled = false
        var isUsernameUpdated = false
        popupWindow.setOnDismissListener {
            if (isUsernameUpdated) {
                Toast("Username Updated", requireContext())
            } else if (etPopupUsername.text.isNotEmpty()) {
                Toast("Changes Discarded", requireContext())
            }
        }
        tvPopupCancel.setOnClickListener {
            popupWindow.dismiss()
        }
        btnPopupCheck.setOnClickListener {
            Toast("Checking For username !!!", requireContext())
            val username = etPopupUsername.text.toString()
            if (username.isNotEmpty()) {
                checkIfUsernameExists(username) { exists ->
                    if (!exists) {
                        tvPopupError.text = "Valid Username... All Good to Go..!!!"
                        tvPopupError.isVisible = true
                        btnPopupSaveChanges.isEnabled = true
                    } else {
                        tvPopupError.text = "Username Exists... Try Other..."
                        tvPopupError.isVisible = true
                        btnPopupSaveChanges.isEnabled = false
                    }
                }
            } else {
                tvPopupError.text = "Username cannot be Empty"
                tvPopupError.isVisible = true
                btnPopupSaveChanges.isEnabled = false
            }
        }
        btnPopupSaveChanges.setOnClickListener {
            val newUsername = etPopupUsername.text.toString()
            val oldUsername = tvProfileUsername.text.toString()

            if (newUsername.isNotEmpty()) {
                checkIfUsernameExists(newUsername) { exists ->
                    if (exists) {
                        tvPopupError.text = "Username Exists... Try Other..."
                        tvPopupError.isVisible = true
                        btnPopupSaveChanges.isEnabled = false
                    } else {
                        updateUsername(oldUsername, newUsername)
                        isUsernameUpdated = true
                        tvProfileUsername.text = newUsername
                        popupWindow.dismiss()
                    }
                }
            }
        }
    }

    //vertical realign with keyboard popup
    override fun onResume() {
        super.onResume()
        val view = view
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keyboardHeight = screenHeight - rect.bottom
            if (keyboardHeight > screenHeight * 0.15) {
                // Keyboard is visible, adjust the popup window's position
                popupWindow.update(0, 0, popupWindow.width, popupWindow.height)
            }
        }
    }

    private fun updateUsername(oldUsername: String, newUsername: String) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.document(oldUsername).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    val data = document.data
                    usersCollection.document(oldUsername).delete()
                    usersCollection.document(newUsername).set(data!!)
                } else {
                    Toast("Data not Found", requireContext())
                }
            } else {
                Toast("Task Failed", requireContext())
            }
        }
    }

    private fun checkIfUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val userCollection = db.collection("users")
        userCollection.document(username).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    callback(true)
                } else {
                    callback(false)
                }
            } else {
                Toast("Error", requireContext())
                callback(false)
            }
        }
    }


}