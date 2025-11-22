package com.mobdeve.s17.itismob_mc0

data class RecipeModel(
    var id: String = "",
    var author: String? = "",
    var calories: Int = 0,
    var cautions: List<String> = emptyList(),
    var createdAt: String = "",
    var cuisineType: List<String> = emptyList(),
    var dietLabels: List<String> = emptyList(),
    var dishType: List<String> = emptyList(),
    var healthLabels: List<String> = emptyList(),
    var imageId: String = "",
    var ingredients: List<String> = emptyList(),
    var instructions: List<String> = emptyList(),
    var label: String = "",
    var mealType: List<String> = emptyList(),
    var prepTime: Int = 0,
    var rating: Double = 0.0,
    var serving: Int = 1,
    var isPublished: Boolean = false,
    var isSaved: Boolean = false,
    var description: String = ""
) {
    // Add a no-argument constructor
    constructor() : this(
        id = "",
        author = "",
        calories = 0,
        cautions = emptyList(),
        createdAt = "",
        cuisineType = emptyList(),
        dietLabels = emptyList(),
        dishType = emptyList(),
        healthLabels = emptyList(),
        imageId = "",
        ingredients = emptyList(),
        instructions = emptyList(),
        label = "",
        mealType = emptyList(),
        prepTime = 0,
        rating = 0.0,
        serving = 1,
        isPublished = false,
        isSaved = false,
        description = ""
    )
}