package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mobdeve.s17.itismob_mc0.databinding.ActivityMainBinding
// import androidx.lifecycle.lifecycleScope
// import kotlinx.coroutines.launch
// import android.util.Log

class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val USER_PREFERENCE = "USER_PREFERENCE"

    // private val edamamUsername = "adrianb"
    // private val edamamAppId = "00540cc5"
    // private val edamamAppKey = "8b0209195f7d3520fc0ad63e93923d46"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isUserLoggedIn()) {
            navigateToHome()
            return
        }

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setupButtonListeners()

        viewBinding.logoIv.setBackgroundColor(Color.TRANSPARENT)

    // Test Recipe Search API with user header
//        lifecycleScope.launch {
//            testRecipeSearchConnection()
//        }
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

    private fun isUserLoggedIn(): Boolean {
        sharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }


    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

/* function used to generate data from Edamam API
    private suspend fun testRecipeSearchConnection() {
        try {
            Log.d("RecipeSearchTest", "Testing API connection")

            val apiService = RetrofitClient.createEdamamService(edamamUsername)
            val response = apiService.searchRecipes(
                query = "chicken",
                appId = edamamAppId,
                appKey = edamamAppKey,
                from = 0,
                to = 1
            )

            Log.d("RecipeSearchTest", "SUCCESS! Found ${response.hits.size} recipes")

            // Upload 30 random recipes
            RealEdamamDataUploader.uploadRealEdamamRecipes(edamamUsername, edamamAppId, edamamAppKey)

        } catch (e: Exception) {
            Log.e("RecipeSearchTest", "FAILED: ${e.message}")
        }
    }
*/
}