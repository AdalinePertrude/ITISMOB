package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class ProfileActivity : ComponentActivity() {
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        backHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}