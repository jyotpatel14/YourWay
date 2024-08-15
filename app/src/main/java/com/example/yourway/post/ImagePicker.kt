package com.example.yourway.post

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourway.R
import com.example.yourway.databinding.FragmentImagePickerBinding
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ImagePickerFragment(private val onImagesPicked: (MutableList<String>) -> Unit) : Fragment() {
    private lateinit var binding: FragmentImagePickerBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ImageAdapter
    private val selectedImagePaths = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerViewImages
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ImageAdapter(selectedImagePaths)
        recyclerView.adapter = adapter

        binding.btnSelectImages.setOnClickListener {
            val intent = ImagePicker.with(requireActivity())
                .provider(ImageProvider.GALLERY) // Allows both camera and gallery
                .setMultipleAllowed(true)
                .crop()
                .cropFreeStyle()
                .galleryMimeTypes(
                    mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg")
                )
                .createIntent()
            imagePickerLauncher.launch(intent)
        }

        binding.btnDone.setOnClickListener {
            onImagesPicked(selectedImagePaths)
            parentFragmentManager.popBackStack()
        }
    }

    // Activity result launcher for image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            // Handle successful image selection
            val clipData = res.data?.clipData
//            val uriList = mutableListOf<Uri>()

            val uriList: ArrayList<Uri>? = res.data?.getParcelableArrayListExtra("extra.multiple_file_path")


            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    uriList!!.add(clipData.getItemAt(i).uri)
                }
            } else {
                res.data?.data?.let { uriList!!.add(it) }
            }

            // Process and display the images
            processImages(uriList)
            Toast.makeText(requireContext(), "Process Image Completer ${res.data} " + clipData?.itemCount, Toast.LENGTH_SHORT).show()
        } else if (res.resultCode == ImagePicker.RESULT_ERROR) {
            // Handle errors more gracefully
            val error = ImagePicker.getError(res.data)
            Toast.makeText(requireContext(), "Image Picker Error: $error", Toast.LENGTH_SHORT).show()
        } else if (res.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireContext(), "Image Picker Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    // Process the selected images and update the adapter
    private fun processImages(imageUris: List<Uri>?) {
        imageUris?.let {
            for (uri in imageUris) {
                val filePath = getRealPathFromURI(uri)
                if (filePath != null) {
                    selectedImagePaths.add(filePath)

                    Log.d("SelectedImage", "Image path: $filePath")
                } else {
                    Log.e("ImagePickerFragment", "Failed to get the file path from URI: $uri")
                }
            }
            adapter.notifyDataSetChanged()
            // Notify the adapter that the data has changed
        }
    }

    // Get the real file path from the URI
    private fun getRealPathFromURI(uri: Uri): String? {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File.createTempFile("temp", ".jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file.absolutePath
    }

    // Adapter to display the images
    private class ImageAdapter(private val imagePaths: MutableList<String>) : RecyclerView.Adapter<ImageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val imagePath = imagePaths[position]
            val bitmap = BitmapFactory.decodeFile(imagePath)
            holder.imageView.setImageBitmap(bitmap) // Set the decoded bitmap to the ImageView
        }

        override fun getItemCount(): Int {
            return imagePaths.size
        }
    }

    private class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_view)
    }
}
