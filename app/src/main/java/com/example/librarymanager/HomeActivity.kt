package com.example.librarymanager

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.librarymanager.data.FileItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var pickPdfLauncher: ActivityResultLauncher<Intent>
    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private val files = mutableListOf<FileItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val uploadPdfButton = findViewById<Button>(R.id.uploadPdfButton)
        filesRecyclerView = findViewById(R.id.filesRecyclerView)

        fileAdapter = FileAdapter(this, files) { fileItem ->
            startDownload(fileItem.downloadUrl, fileItem.fileName)
        }
        filesRecyclerView.layoutManager = LinearLayoutManager(this)
        filesRecyclerView.adapter = fileAdapter

        logoutButton.setOnClickListener {
            logout()
        }

        // Initialize the PDF picker launcher
        pickPdfLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val filePath = result.data?.data
                if (filePath != null) {
                    uploadFile(filePath)
                }
            }
        }

        uploadPdfButton.setOnClickListener {
            // Start an intent to pick a PDF
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            pickPdfLauncher.launch(intent)
        }

        // Load the list of files from Firebase Storage
        loadFilesFromStorage()
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the HomeActivity
    }

    private fun uploadFile(uri: Uri) {
        val storageRef = storage.reference
        val pdfRef = storageRef.child("pdfs/${uri.lastPathSegment}")
        val uploadTask = pdfRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            // File uploaded successfully
            Toast.makeText(this, "File uploaded", Toast.LENGTH_SHORT).show()
            loadFilesFromStorage()
        }.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(this, "File upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFilesFromStorage() {
        val storageRef = storage.reference.child("pdfs")
        storageRef.listAll().addOnSuccessListener { listResult ->
            files.clear()
            for (item in listResult.items) {
                item.downloadUrl.addOnSuccessListener { uri ->
                    val author = item.name
                    val fileName = item.name
                    val downloadUrl = uri.toString()
                    files.add(FileItem(author, fileName, downloadUrl))
                    fileAdapter.notifyDataSetChanged()
                }
            }
        }.addOnFailureListener {
            // Handle any errors
            Toast.makeText(this, "Failed to load files: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDownload(url: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading PDF")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
    }
}
