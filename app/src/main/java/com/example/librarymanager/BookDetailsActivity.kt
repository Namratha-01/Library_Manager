package com.example.librarymanager

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class BookDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        // Get data from intent
        val title = intent.getStringExtra("title") ?: "No Title"
        val description = intent.getStringExtra("description") ?: "No Description"
        val price = intent.getStringExtra("price") ?: "No Price"
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""

        // Find views
        val titleTextView: TextView = findViewById(R.id.bookTitleTextView)
        val descriptionTextView: TextView = findViewById(R.id.bookDescriptionTextView)
        val priceTextView: TextView = findViewById(R.id.bookPriceTextView)
        val bookImageView: ImageView = findViewById(R.id.bookImageView)

        // Set data
        titleTextView.text = title
        descriptionTextView.text = description
        priceTextView.text = price
        Glide.with(this).load(imageUrl).into(bookImageView)
    }
}