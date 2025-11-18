package com.mobdeve.s17.itismob_mc0

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// ---------------------
// DATA CLASSES
// ---------------------


data class NutritionRequest(
    val title: String,
    val ingr: List<String>
)

data class NutritionResponse(
    val calories: Double?
)


// ---------------------
// API SERVICE
// ---------------------

interface EdamamApiService {

    // 🌐 Your original recipe search endpoint
    @GET("api/recipes/v2")
    suspend fun searchRecipes(
        @Query("type") type: String = "public",
        @Query("q") query: String,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("from") from: Int = 0,
        @Query("to") to: Int = 30,
        @Query("diet") diet: String? = null,
        @Query("health") health: String? = null
    ): RecipeResponse


    // 🌐 NEW: Edamam Nutrition Analysis API
    @POST("api/nutrition-details")
    suspend fun analyzeNutrition(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Body body: NutritionRequest
    ): NutritionResponse
}