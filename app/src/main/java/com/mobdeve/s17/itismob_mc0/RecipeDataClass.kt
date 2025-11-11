package com.mobdeve.s17.itismob_mc0
import com.google.gson.annotations.SerializedName //sure that it is received correctly even though val name is different

// data class for api recieved
data class RecipeResponse(
    @SerializedName("from") val from: Int,
    @SerializedName("to") val to: Int,
    @SerializedName("count") val count: Int,
    @SerializedName("hits") val hits: List<RecipeHit>
)

data class RecipeHit(
    @SerializedName("recipe") val recipe: Recipe
)

// structure of recipe data
data class Recipe(
    @SerializedName("uri") val uri: String,
    @SerializedName("label") val label: String,
    @SerializedName("image") val image: String,
    @SerializedName("source") val source: String,
    @SerializedName("url") val url: String,
    @SerializedName("shareAs") val shareAs: String,
    @SerializedName("yield") val yield: Double,
    @SerializedName("calories") val calories: Double,
    @SerializedName("totalWeight") val totalWeight: Double,
    @SerializedName("dietLabels") val dietLabels: List<String>,
    @SerializedName("healthLabels") val healthLabels: List<String>,
    @SerializedName("cautions") val cautions: List<String>,
    @SerializedName("ingredientLines") val ingredientLines: List<String>,
    @SerializedName("cuisineType") val cuisineType: List<String>,
    @SerializedName("mealType") val mealType: List<String>,
    @SerializedName("dishType") val dishType: List<String>
)