package com.example.librarymanager

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Basic validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters long.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            signUp(email, password)
        }
    }

    private fun signUp(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Add user data to Firestore
                        val db = FirebaseFirestore.getInstance()
                        val userData = hashMapOf(
                            "email" to email,
                            "uid" to it.uid,
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(it.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Sign-up successful and data saved.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToHome()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error saving user data", e)
                                Toast.makeText(
                                    this,
                                    "Sign-up successful but data save failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToHome()
                            }
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Weak password. Please choose a stronger password."
                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                        is FirebaseAuthUserCollisionException -> {
                            "User with this email already exists."
                        }

                        else -> "Authentication failed."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()

                    // If user already exists, navigate to home
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        navigateToHome()
                    }
                }
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Optional: close the current activity
    }
}