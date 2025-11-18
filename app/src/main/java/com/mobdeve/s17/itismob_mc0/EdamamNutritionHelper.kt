package com.mobdeve.s17.itismob_mc0

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EdamamNutritionHelper {

    companion object {
        const val APP_ID = "00540cc5"
        const val APP_KEY = "8b0209195f7d3520fc0ad63e93923d46"
        private const val BASE_URL = "https://api.edamam.com/"
    }

    private val api: EdamamApiService

    init {
        api = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EdamamApiService::class.java)
    }

    // Returns calories for "grams g ingredientName"
    suspend fun getCalories(ingredientName: String, grams: Double): Double {
        val ingredientString = "${grams} g $ingredientName"

        val request = NutritionRequest(
            title = ingredientName,
            ingr = listOf(ingredientString)
        )

        val response = api.analyzeNutrition(
            APP_ID,
            APP_KEY,
            request
        )

        return response.calories ?: 0.0
    }
}
