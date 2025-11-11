package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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

object PasswordManager {
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }
}

class LoginActivity : ComponentActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var sharedPreferences: SharedPreferences

    private val USER_PREFERENCE = "USER_PREFERENCE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (isUserLoggedIn()) {
            navigateToHome()
            return
        }

        val signupText: TextView = findViewById(R.id.signupLinkTv)
        val loginButton: Button = findViewById(R.id.loginBtn)
        email = findViewById(R.id.email_loginEt)
        password = findViewById(R.id.password_loginEt)

        loginButton.setOnClickListener {
            if (email.text.toString().isEmpty() || password.text.toString().isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginWithFirestore()
            }
        }

        signupText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginWithFirestore() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString()

        lifecycleScope.launch {
            try {
                val db = Firebase.firestore
                val query = db.collection("users")
                    .whereEqualTo("email", emailText.lowercase())
                    .limit(1)
                    .get()
                    .await()

                if (query.documents.isNotEmpty()) {
                    val userDoc = query.documents[0]
                    val storedHashedPassword = userDoc.getString("password")

                    if (storedHashedPassword != null &&
                        PasswordManager.verifyPassword(passwordText, storedHashedPassword)) {

                        val userId = userDoc.id
                        val userName = userDoc.getString("fullname") ?: "User"

                        Toast.makeText(this@LoginActivity, "Welcome $userName!", Toast.LENGTH_SHORT).show()

                        saveLoginState(userId, userDoc.getString("email"), userName)
                        navigateToHome()

                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@LoginActivity, "User not found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Login", "Firestore error", e)
            }
        }
    }

    private fun saveLoginState(userId: String, userEmail: String? = null, userName: String? = null) {
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("userId", userId)
        editor.putString("userEmail", userEmail)
        editor.putString("userName", userName)
        editor.apply()
    }

    private fun isUserLoggedIn(): Boolean {
        sharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}