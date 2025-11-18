package com.mobdeve.s17.itismob_mc0

class SQLiteRecipeItem(
    var id: Int = -1,
    val recipeId: String = "",
    val author: String = "",
    val calories: Int = 0,
    val createdAt: String = "",
    val imageId: String = "",
    val label: String = "",
    val prepTime: Int = 0,
    val rating: Double = 0.0,
    val serving: Int = 0,
    var isSaved: Boolean = true,
    val description: String = ""
) {
    companion object {
        private const val DEFAULT_ID = -1
    }

    //constructor for creating from RecipeModel
    constructor(recipe: RecipeModel) : this(
        id = DEFAULT_ID,
        recipeId = recipe.id,
        author = recipe.author,
        calories = recipe.calories,
        createdAt = recipe.createdAt,
        imageId = recipe.imageId,
        label = recipe.label,
        prepTime = recipe.prepTime,
        rating = recipe.rating,
        serving = recipe.serving,
        isSaved = recipe.isSaved,
        description = recipe.description
    )

    // Default constructor
    constructor() : this(
        id = DEFAULT_ID,
        recipeId = "",
        author = "",
        calories = 0,
        createdAt = "",
        imageId = "",
        label = "",
        prepTime = 0,
        rating = 0.0,
        serving = 0,
        isSaved = true,
        description = ""
    )

    // Convert to RecipeModel (will need to load related data separately)
    fun toRecipeModel(
        cautions: List<String> = emptyList(),
        cuisineType: List<String> = emptyList(),
        dietLabels: List<String> = emptyList(),
        dishType: List<String> = emptyList(),
        healthLabels: List<String> = emptyList(),
        ingredients: List<String> = emptyList(),
        instructions: List<String> = emptyList(),
        mealType: List<String> = emptyList()
    ): RecipeModel {
        return RecipeModel(
            id = this.recipeId,
            author = this.author,
            calories = this.calories,
            cautions = cautions,
            createdAt = this.createdAt,
            cuisineType = cuisineType,
            dietLabels = dietLabels,
            dishType = dishType,
            healthLabels = healthLabels,
            imageId = this.imageId,
            ingredients = ingredients,
            instructions = instructions,
            label = this.label,
            mealType = mealType,
            prepTime = this.prepTime,
            rating = this.rating,
            serving = this.serving,
            isSaved = this.isSaved,
            description = this.description
        )
    }
}