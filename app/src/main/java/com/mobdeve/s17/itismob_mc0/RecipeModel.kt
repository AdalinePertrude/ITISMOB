package com.mobdeve.s17.itismob_mc0

data class RecipeModel(
    val id: String,
    val author : String,
    val calories : Int,
    val cautions : List<String>,
    val createdAt: String,
    val cuisineType : List<String>,
    val dietLabels : List<String>,
    val dishType : List<String>,
    val healthLabels : List<String>,
    val imageId : String,
    val ingredients : List<String>,
    val instructions : List<String>,
    val label : String,
    val mealType : List<String>,
    val prepTime: Int,
    val rating: Double,
    val serving: Int,
    val isPublished: Boolean = false,
    var isSaved: Boolean = false,
    val description: String
)