package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ProfileActivity : ComponentActivity() {
    private val USER_PREFERENCE = "USER_PREFERENCE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        val editProfile : ImageButton = findViewById(R.id.editProfile)
        val fullName: TextView = findViewById(R.id.fNameProfileTv)
        val email: TextView = findViewById(R.id.emailProfileTv)
        val savedRecipes: Button = findViewById(R.id.savedRecsBtn)
        val publishedRecipes: Button = findViewById(R.id.publishedRecsBtn)
        val backHome: Button = findViewById(R.id.backHomeBtn)
        val logout: Button = findViewById(R.id.logoutBtn)
        logout.setOnClickListener {
            logoutUser()
        }
        backHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logoutUser() {
        // Clear SharedPreferences
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}