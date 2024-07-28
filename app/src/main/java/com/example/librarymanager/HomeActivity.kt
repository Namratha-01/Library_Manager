package com.example.librarymanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val viewItemsButton = findViewById<Button>(R.id.viewItemsButton)
        val uploadPdfButton = findViewById<Button>(R.id.uploadPdfButton)

        logoutButton.setOnClickListener {
            logout()
        }

        viewItemsButton.setOnClickListener {
            val intent = Intent(this, DisplayItemsActivity::class.java)
            startActivity(intent)
        }

        uploadPdfButton.setOnClickListener {
            // Handle PDF upload functionality
            selectImageForUpload()
        }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the HomeActivity
    }

    private fun selectImageForUpload() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                // Show a dialog to enter name, description, and price
                showUploadDialog(imageUri)
            }
        }
    }

    private fun showUploadDialog(imageUri: Uri) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val priceEditText = dialogView.findViewById<EditText>(R.id.priceEditText)

        AlertDialog.Builder(this)
            .setTitle("Upload Image")
            .setView(dialogView)
            .setPositiveButton("Upload") { _, _ ->
                val name = nameEditText.text.toString()
                val description = descriptionEditText.text.toString()
                val price = priceEditText.text.toString()
                uploadImageToStorage(name, description, price, imageUri)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun uploadImageToStorage(name: String, description: String, price: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val fileItem = FileItem(name, description, price, downloadUri.toString())
                saveFileToFirestore(fileItem)
            } else {
                Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFileToFirestore(fileItem: FileItem) {
        val db = FirebaseFirestore.getInstance()
        db.collection("files").add(fileItem)
            .addOnSuccessListener {
                Toast.makeText(this, "File uploaded and data saved.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save file data.", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 100
    }
}
