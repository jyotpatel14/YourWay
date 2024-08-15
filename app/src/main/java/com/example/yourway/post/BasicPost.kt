package com.example.yourway.post

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

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = FragmentBasicPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Set up the button click listener
        binding.btnGotoPostReview.setOnClickListener {
            savePostToFirestore()
        }
    }

    private fun savePostToFirestore() {
        // Get the title and description text
        val title = binding.etPostTitle.text.toString()
        val description = binding.etPostDescription.text.toString()

        // Validate inputs
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in both fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new post map
        val post = hashMapOf(
            "title" to title,
            "description" to description
        )

        // Save to Firestore
        db.collection("posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post added successfully", Toast.LENGTH_SHORT).show()
                // Optionally clear the fields
                binding.etPostTitle.text.clear()
                binding.etPostDescription.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to add post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks
        _binding = null
    }
}