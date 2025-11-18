package com.mobdeve.s17.itismob_mc0

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class EdamamIngredientRequest(val ingr: List<String>)
data class NutrientDetail(val quantity: Double?, val unit: String?)
data class EdamamNutrient(val ENERC_KCAL: NutrientDetail?)
data class EdamamNutritionResponse(val totalNutrients: EdamamNutrient)

interface EdamamServiceApi {
    @POST("api/nutrition-details")
    suspend fun getNutrition(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Body request: EdamamIngredientRequest
    ): EdamamNutritionResponse
}

class EdamamNutritionHelper {

    companion object {
        private const val BASE_URL = "https://api.edamam.com/"
        private const val APP_ID = "00540cc5"
        private const val APP_KEY = "8b0209195f7d3520fc0ad63e93923d46"
    }

    private val api: EdamamServiceApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(EdamamServiceApi::class.java)
    }


    suspend fun getCalories(ingredientName: String, grams: Double): Double = withContext(Dispatchers.IO) {
        try {
            val requestText = listOf("$grams g $ingredientName")
            val request = EdamamIngredientRequest(requestText)
            val response = api.getNutrition(APP_ID, APP_KEY, request)
            val kcal = response.totalNutrients.ENERC_KCAL?.quantity ?: 0.0
            kcal
        } catch (e: Exception) {
            Log.e("EdamamHelper", "Error fetching calories", e)
            0.0
        }
    }

    fun getIngredientSuggestions(query: String): List<String> {
        val allIngredients = listOf(
            "Chicken breast", "Beef", "Pork", "Salmon", "Egg", "Milk", "Cheese", "Butter",
            "Rice", "Pasta", "Tomato", "Onion", "Garlic", "Potato", "Carrot", "Spinach", "Broccoli",
            "Olive oil", "Salt", "Sugar", "Flour", "Yogurt", "Banana", "Apple"
        )
        return allIngredients.filter { it.contains(query, ignoreCase = true) }
    }
}

