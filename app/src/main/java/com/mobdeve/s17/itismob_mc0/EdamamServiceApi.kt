package com.mobdeve.s17.itismob_mc0
import retrofit2.http.GET
import retrofit2.http.Query

// for api requests
interface EdamamApiService {
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
}