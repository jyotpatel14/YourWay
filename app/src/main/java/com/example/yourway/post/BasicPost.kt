package com.example.yourway.post

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.yourway.R
import com.example.yourway.databinding.FragmentBasicPostBinding
import com.google.firebase.firestore.FirebaseFirestore


class BasicPost : Fragment() {

    // Declare the binding variable
    private var _binding: FragmentBasicPostBinding? = null
    private val binding get() = _binding!!

    private val post = Post() // This will hold the post data including title, description, and images

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle Image Picker button click
        binding.btnDisplayImagePicker.setOnClickListener {
            val fragment = ImagePickerFragment { imageUris ->
                post.imageUrls = imageUris.map { it }.toMutableList()
            }
            childFragmentManager.beginTransaction()
                .replace(R.id.image_picker_container, fragment)
                .commit()
        }

        // Handle Review Post button click
        binding.btnGotoPostReview.setOnClickListener {
            // Collect title and description
            post.title = binding.etPostTitle.text.toString()
            post.description = binding.etPostDescription.text.toString()

            // Open PostReviewFragment and pass the post data
            val fragment = PostReview.newInstance(post)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcv_addpost, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}