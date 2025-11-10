package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mobdeve.s17.itismob_mc0.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setupButtonListeners()

        viewBinding.logoIv.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun setupButtonListeners() {
        viewBinding.loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewBinding.signupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}