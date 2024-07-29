package com.example.librarymanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult

class DisplayItemsActivity : AppCompatActivity() {

    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private val files = mutableListOf<FileItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_items)
        val backButton: Button = findViewById(R.id.back1)

        itemsRecyclerView = findViewById(R.id.itemsRecyclerView)
        itemsRecyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns in the grid

        // Initialize the adapter with the proper parameters
        fileAdapter = FileAdapter(this, files) { fileItem ->
            // Handle item click
            Toast.makeText(this, "Clicked ${fileItem.name}", Toast.LENGTH_SHORT).show()
        }
        itemsRecyclerView.adapter = fileAdapter

        backButton.setOnClickListener {
            navigateToHome()
        }

        // Load files from storage or database
        loadFilesFromStorage()
    }

    private fun loadFilesFromStorage() {
        val storageRef = FirebaseStorage.getInstance().reference.child("images")
        val firestoreDb = FirebaseFirestore.getInstance()

        storageRef.listAll().addOnSuccessListener { listResult ->
            files.clear()
            for (item in listResult.items) {
                val fileName = item.name
                firestoreDb.collection("fileMetadata").document(fileName).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val description = document.getString("description") ?: "No description"
                            val price = document.getString("price") ?: "No price"
                            val imageUrl = document.getString("imageUrl") ?: ""

                            files.add(FileItem(fileName, description, price, imageUrl))
                            fileAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this, "No metadata found for $fileName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val description = document.getString("description") ?: "No description"
                            val price = document.getString("price") ?: "No price"
                            val imageUrl = document.getString("imageUrl") ?: ""

                            // Log the data or show a toast
                            Toast.makeText(this, "Desc: $description, Price: $price, Image: $imageUrl", Toast.LENGTH_LONG).show()

                            files.add(FileItem(fileName, description, price, imageUrl))
                            fileAdapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this, "No metadata found for $fileName", Toast.LENGTH_SHORT).show()
                        }
                    }

                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to retrieve metadata.", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load files.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Optional: close the current activity
    }
}