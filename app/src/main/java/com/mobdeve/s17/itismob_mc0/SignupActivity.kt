package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import at.favre.lib.crypto.bcrypt.BCrypt
import com.google.firebase.firestore.FieldValue

object PasswordHasher {
    private const val BCRYPT_COST = 12

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, password.toCharArray())
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }
}

class SignupActivity : ComponentActivity() {

    private lateinit var fullname: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val loginText: TextView = findViewById(R.id.loginLinkTv)
        val signupButton: Button = findViewById(R.id.signupBtn)
        fullname = findViewById(R.id.full_nameEt)
        email = findViewById(R.id.email_signupEt)
        password = findViewById(R.id.password_signupEt)
        confirmPassword = findViewById(R.id.confirm_password_signupEt)

        signupButton.setOnClickListener {
            handleSignup()
        }

        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun handleSignup() {
        val fullnameText = fullname.text.toString().trim()
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString()
        val confirmPasswordText = confirmPassword.text.toString()

        // Input validation
        if (!validateInputs(fullnameText, emailText, passwordText, confirmPasswordText)) {
            return
        }

        lifecycleScope.launch {
            try {
                // Check if email exists
                val db = Firebase.firestore
                val existingUserQuery = db.collection("users")
                    .whereEqualTo("email", emailText.lowercase())
                    .limit(1)
                    .get()
                    .await()

                if (existingUserQuery.documents.isNotEmpty()) {
                    Toast.makeText(this@SignupActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Hash password and create user
                val hashedPassword = PasswordHasher.hashPassword(passwordText)
                val documentReference = db.collection("users").document()

                val user = hashMapOf(
                    "id" to documentReference.id,
                    "fullname" to fullnameText,
                    "email" to emailText.lowercase(),
                    "password" to hashedPassword,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                documentReference.set(user).await()

                Log.d("Signup", "User created with ID: ${documentReference.id}")

                Toast.makeText(this@SignupActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to login instead of home
                val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Log.e("Signup", "Error creating user", e)
                Toast.makeText(this@SignupActivity, "Error creating account: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(
        fullname: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}