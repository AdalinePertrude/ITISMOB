package com.mobdeve.s17.itismob_mc0

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.edamam.com/"

    // Create OkHttpClient with User header
    private fun createOkHttpClient(username: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                // Add Edamam-Account-User header
                val request = original.newBuilder()
                    .header("Edamam-Account-User", username)
                    .method(original.method, original.body)
                    .build()

                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // Function to get service with user header
    fun createEdamamService(username: String): EdamamApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(username))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(EdamamApiService::class.java)
    }
}

// Upload recipes from Edamam API
object RealEdamamDataUploader {
    private val db = Firebase.firestore

    suspend fun uploadRealEdamamRecipes(username: String, appId: String, appKey: String) {
        try {
            Log.d("RealEdamam", "Starting upload of 30 categorized recipes")

            val apiService = RetrofitClient.createEdamamService(username)

            // Organized by categories to ensure variety
            val recipeCategories = mapOf(
                "Proteins" to listOf("chicken breast", "salmon fillet", "tofu stir fry", "beef steak", "shrimp scampi"),
                "Vegetarian" to listOf("vegetable curry", "bean salad", "mushroom pasta", "spinach quiche", "lentil soup"),
                "Quick Meals" to listOf("sandwich", "wrap", "omelette", "stir fry", "pasta"),
                "Healthy" to listOf("quinoa bowl", "salad", "smoothie", "grilled vegetables", "buddha bowl"),
                "Comfort Food" to listOf("mac and cheese", "burger", "pizza", "lasagna", "mashed potatoes"),
                "International" to listOf("tacos", "sushi", "curry", "pad thai", "hummus")
            )

            var totalUploaded = 0

            // Take 5 queries from each category (6 categories × 5 = 30)
            for ((category, queries) in recipeCategories) {
                if (totalUploaded >= 30) break

                Log.d("RealEdamam", "Processing category: $category")

                for (query in queries.take(5)) {
                    if (totalUploaded >= 30) break

                    Log.d("RealEdamam", "Searching: $query (${totalUploaded + 1}/30)")

                    try {
                        val response = apiService.searchRecipes(
                            query = query,
                            appId = appId,
                            appKey = appKey,
                            from = 0,
                            to = 1
                        )

                        if (response.hits.isNotEmpty()) {
                            saveRealEdamamRecipe(response.hits[0].recipe)
                            totalUploaded++
                            Log.d("RealEdamam", "Progress: $totalUploaded/30 - Category: $category")

                            // Respectful delay
                            delay(2000)
                        }

                    } catch (e: Exception) {
                        Log.e("RealEdamam", "Error for $query: ${e.message}")
                    }
                }
            }

            Log.d("RealEdamam", "Upload completed! Total: $totalUploaded/30 recipes")

        } catch (e: Exception) {
            Log.e("RealEdamam", "Error uploading recipes: ${e.message}")
        }
    }
    private suspend fun saveRealEdamamRecipe(recipe: Recipe) {
        try {
            val recipeId = recipe.uri.substringAfterLast("_")

            val firebaseRecipe = hashMapOf(
                "id" to recipeId,
                "uri" to recipe.uri,
                "label" to recipe.label,
                "image" to recipe.image,
                "source" to recipe.source,
                "url" to recipe.url,
                "shareAs" to recipe.shareAs,
                "yield" to recipe.yield,
                "calories" to recipe.calories,
                "totalWeight" to recipe.totalWeight,
                "dietLabels" to recipe.dietLabels,
                "healthLabels" to recipe.healthLabels,
                "cautions" to recipe.cautions,
                "ingredientLines" to recipe.ingredientLines,
                "cuisineType" to recipe.cuisineType,
                "mealType" to recipe.mealType,
                "dishType" to recipe.dishType,
                "isFromEdamam" to true,
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("recipes")
                .document(recipeId)
                .set(firebaseRecipe)
                .await()

            Log.d("RealEdamam", "Saved: ${recipe.label}")

        } catch (e: Exception) {
            Log.e("RealEdamam", "Error saving recipe: ${recipe.label}", e)
        }
    }
}