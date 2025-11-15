package com.mobdeve.s17.itismob_mc0

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast

class RecipeRepository(private val context: Context) {
    private val recipeDatabase = SQLiteRecipeDatabase(context)
    private val imageDownloader = ImageDownloader(context)

    // Save recipe with image
    fun saveRecipeWithImage(recipe: RecipeModel, callback: (Boolean) -> Unit) {
        try {
            // If recipe has image URL, download and save it
            if (recipe.imageId.isNotEmpty() && recipe.imageId.startsWith("http")) {
                imageDownloader.downloadAndSaveImage(recipe.imageId, recipe.id) { localImagePath ->
                    if (localImagePath != null) {
                        // Create a copy of recipe with local image path
                        val recipeWithLocalImage = recipe.copy(imageId = localImagePath)
                        val result = recipeDatabase.addRecipe(recipeWithLocalImage) != -1
                        Log.d("RecipeRepository", "Saved recipe with local image: ${recipe.label} - $result")
                        callback(result)
                    } else {
                        // If image download fails, save recipe without image
                        val result = recipeDatabase.addRecipe(recipe) != -1
                        Log.d("RecipeRepository", "Saved recipe with online image: ${recipe.label} - $result")
                        callback(result)
                    }
                }
            } else {
                // Recipe already has local image path or no image
                val result = recipeDatabase.addRecipe(recipe) != -1
                Log.d("RecipeRepository", "Saved recipe: ${recipe.label} - $result")
                callback(result)
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error saving recipe ${recipe.label}: ${e.message}", e)
            Toast.makeText(context, "Error saving recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }

    // Get recipe with image loading
    fun getRecipeWithImage(recipeId: String): RecipeModel? {
        return try {
            val recipe = recipeDatabase.getRecipeById(recipeId)
            if (recipe != null) {
                Log.d("RecipeRepository", "Retrieved recipe: ${recipe.label}")
                Log.d("RecipeRepository", "  Image ID: ${recipe.imageId}")
                Log.d("RecipeRepository", "  Is Local Path: ${!recipe.imageId.startsWith("http")}")
            } else {
                Log.d("RecipeRepository", "Recipe not found: $recipeId")
            }
            recipe
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipe $recipeId: ${e.message}", e)
            null
        }
    }

    // Load image bitmap for a recipe
    fun loadRecipeImage(recipe: RecipeModel): Bitmap? {
        return try {
            if (recipe.imageId.isNotEmpty() && !recipe.imageId.startsWith("http")) {
                val bitmap = imageDownloader.loadImageFromStorage(recipe.imageId)
                if (bitmap != null) {
                    Log.d("RecipeRepository", "Loaded image for: ${recipe.label}")
                } else {
                    Log.w("RecipeRepository", "Failed to load image for: ${recipe.label}")
                }
                bitmap
            } else {
                Log.d("RecipeRepository", "No local image for: ${recipe.label}")
                null
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error loading image for ${recipe.label}: ${e.message}", e)
            null
        }
    }

    // Get all recipes
    fun getAllRecipes(): List<RecipeModel> {
        return try {
            val recipes = recipeDatabase.getAllRecipes()
            Log.d("RecipeRepository", "Retrieved ${recipes.size} recipes from database")
            recipes
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting all recipes: ${e.message}", e)
            emptyList()
        }
    }

    // Update recipe
    fun updateRecipe(recipe: RecipeModel): Boolean {
        return try {
            val result = recipeDatabase.updateRecipe(recipe) > 0
            Log.d("RecipeRepository", "Updated recipe ${recipe.label}: $result")
            result
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error updating recipe ${recipe.label}: ${e.message}", e)
            Toast.makeText(context, "Error updating recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Delete recipe and its image
    fun deleteRecipeWithImage(recipe: RecipeModel): Boolean {
        return try {
            // First delete from database
            val dbResult = recipeDatabase.deleteRecipe(recipe) > 0

            if (dbResult) {
                Log.d("RecipeRepository", "Deleted recipe from database: ${recipe.label}")

                // Then delete local image file if it exists
                if (recipe.imageId.isNotEmpty() && !recipe.imageId.startsWith("http")) {
                    val imageDeleted = imageDownloader.deleteImage(recipe.imageId)
                    if (imageDeleted) {
                        Log.d("RecipeRepository", "✅ Deleted image file: ${recipe.imageId}")
                    } else {
                        Log.w("RecipeRepository", "❌ Failed to delete image file: ${recipe.imageId}")
                    }
                }
                true
            } else {
                Log.w("RecipeRepository", "Failed to delete recipe from database: ${recipe.label}")
                false
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error deleting recipe ${recipe.label}: ${e.message}", e)
            Toast.makeText(context, "Error deleting recipe: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Delete recipe by ID
    fun deleteRecipeById(recipeId: String): Boolean {
        return try {
            val recipe = recipeDatabase.getRecipeById(recipeId)
            if (recipe != null) {
                deleteRecipeWithImage(recipe)
            } else {
                Log.w("RecipeRepository", "Recipe not found for deletion: $recipeId")
                false
            }
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error deleting recipe by ID $recipeId: ${e.message}", e)
            false
        }
    }

    // Check if recipe exists
    fun recipeExists(recipeId: String): Boolean {
        return try {
            val exists = recipeDatabase.getRecipeById(recipeId) != null
            Log.d("RecipeRepository", "Recipe $recipeId exists: $exists")
            exists
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error checking if recipe exists $recipeId: ${e.message}", e)
            false
        }
    }

    // Get recipes count
    fun getRecipesCount(): Int {
        return try {
            val count = recipeDatabase.getAllRecipes().size
            Log.d("RecipeRepository", "Total recipes count: $count")
            count
        } catch (e: Exception) {
            Log.e("RecipeRepository", "Error getting recipes count: ${e.message}", e)
            0
        }
    }

}