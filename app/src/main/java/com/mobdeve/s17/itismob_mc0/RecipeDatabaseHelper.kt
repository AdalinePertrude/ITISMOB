package com.mobdeve.s17.itismob_mc0

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale

class RecipeDatabaseHelper {
    companion object {
        fun fetchRecipeData(onComplete: (ArrayList<RecipeModel>) -> Unit) {
            val db = Firebase.firestore
            val recipes = ArrayList<RecipeModel>()

            db.collection("recipes")
                .get()
                .addOnSuccessListener { documents ->
                    val recipeList = ArrayList<RecipeModel>()

                    // First, get all recipes without ratings
                    for (document in documents) {
                        val recipe = RecipeModel(
                            id = document.getString("id") ?: "",
                            author = document.getString("author") ?: "",
                            calories = document.getLong("calories")?.toInt() ?: 0,
                            cautions = document.get("cautions") as? List<String> ?: emptyList(),
                            createdAt = document.getTimestamp("createdAt")?.toDate().toString() ?: "",
                            cuisineType = document.get("cuisineType") as? List<String> ?: emptyList(),
                            dietLabels = document.get("dietLabels") as? List<String> ?: emptyList(),
                            dishType = document.get("dishType") as? List<String> ?: emptyList(),
                            healthLabels = document.get("healthLabels") as? List<String> ?: emptyList(),
                            imageId = document.getString("image") ?: "",
                            ingredients = document.get("ingredientLines") as? List<String> ?: emptyList(),
                            instructions = document.get("instructions") as? List<String> ?: emptyList(),
                            label = document.getString("label") ?: "",
                            mealType = document.get("mealType") as? List<String> ?: emptyList(),
                            prepTime = document.getLong("preptime")?.toInt() ?: 0,
                            rating = 0.0, // Temporary, will be updated
                            serving = document.getLong("yield")?.toInt() ?: 0,
                            isSaved = document.getBoolean("isSaved") ?: false,
                            description = document.getString("description") ?: ""
                        )
                        recipeList.add(recipe)
                    }

                    // Now fetch ratings for each recipe
                    val recipesWithRatings = ArrayList<RecipeModel>()
                    var completedCount = 0

                    recipeList.forEach { recipe ->
                        fetchRecipeRating(recipe.id) { averageRating ->
                            val updatedRecipe = recipe.copy(rating = averageRating)
                            recipesWithRatings.add(updatedRecipe)
                            completedCount++

                            if (completedCount == recipeList.size) {
                                println("DEBUG: Fetched ${recipesWithRatings.size} recipes with real-time ratings")
                                onComplete(recipesWithRatings)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching recipes: ${exception.message}")
                    onComplete(ArrayList())
                }
        }

        fun searchRecipeByField(field: String, value: String, onComplete: (RecipeModel?) -> Unit) {
            val db = Firebase.firestore

            db.collection("recipes")
                .whereEqualTo(field, value)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val recipe = RecipeModel(
                            id = document.getString("id") ?: "",
                            author = document.getString("author") ?: "",
                            calories = document.getLong("calories")?.toInt() ?: 0,
                            cautions = document.get("cautions") as? List<String> ?: emptyList(),
                            createdAt = document.getTimestamp("createdAt")?.toDate().toString() ?: "",
                            cuisineType = document.get("cuisineType") as? List<String> ?: emptyList(),
                            dietLabels = document.get("dietLabels") as? List<String> ?: emptyList(),
                            dishType = document.get("dishType") as? List<String> ?: emptyList(),
                            healthLabels = document.get("healthLabels") as? List<String> ?: emptyList(),
                            imageId = document.getString("image") ?: "",
                            ingredients = document.get("ingredientLines") as? List<String> ?: emptyList(),
                            instructions = document.get("instructions") as? List<String> ?: emptyList(),
                            label = document.getString("label") ?: "",
                            mealType = document.get("mealType") as? List<String> ?: emptyList(),
                            prepTime = document.getLong("preptime")?.toInt() ?: 0,
                            rating = document.getDouble("rating") ?: 0.0,
                            serving = document.getLong("yield")?.toInt() ?: 0,
                            isSaved = document.getBoolean("isSaved") ?: false,
                            description = document.getString("description") ?: ""
                        )
                        onComplete(recipe)
                    } else {
                        onComplete(null)
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error searching recipe: ${exception.message}")
                    onComplete(null)
                }
        }

        fun fetchRecipeComments(recipeid: String, onComplete: (ArrayList<CommentModel>) -> Unit) {
            val db = Firebase.firestore
            val comments = ArrayList<CommentModel>()
            val commentsCollection = db.collection("recipes")
                .document(recipeid)
                .collection("comments")
                .orderBy("comment_date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val comment = CommentModel(
                            commentor = document.getString("commentor") ?: "",
                            comment = document.getString("comment") ?: "",
                            commentDate = formatDate(document.getTimestamp("comment_date")?.toDate().toString()) ?: "",
                            userid = document.getString("user_id") ?: ""
                        )
                        comments.add(comment)
                    }
                    println("DEBUG: Fetched ${comments.size} comments for recipe $recipeid")
                    onComplete(comments)
                }
                .addOnFailureListener { exception ->
                    println("Error fetching comments: ${exception.message}")
                    onComplete(ArrayList()) // Return empty list
                }
        }

        fun addCommentToRecipe(recipeId: String, comment: CommentModel, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            val commentData = hashMapOf(
                "commentor" to comment.commentor,
                "comment" to comment.comment,
                "comment_date" to FieldValue.serverTimestamp(), // Use server timestamp for consistency
                "user_id" to comment.userid
            )

            db.collection("recipes")
                .document(recipeId)
                .collection("comments")
                .add(commentData)
                .addOnSuccessListener {
                    Log.d("DEBUG", "Comment successfully added to Firestore")
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    Log.e("DEBUG", "Failed to add comment: ${exception.message}")
                    onComplete(false)
                }
        }


        fun fetchRecipeRating(recipeid: String, onComplete: (Double) -> Unit) {
            val db = Firebase.firestore
            val ratingsCollection = db.collection("recipes")
                .document(recipeid)
                .collection("ratings")
                .get()
                .addOnSuccessListener { documents ->
                    var totalRating = 0.0
                    var ratingCount = 0

                    for (document in documents) {
                        val rating = document.getDouble("rating") ?: 0.0
                        totalRating += rating
                        ratingCount++
                    }

                    val averageRating = if (ratingCount > 0) totalRating / ratingCount else 0.0
                    println("DEBUG: Average rating for recipe $recipeid: $averageRating ($ratingCount ratings)")

                    // Update the average rating in the recipe document
                    updateRecipeAvgRating(recipeid, averageRating) { success ->
                        if (success) {
                            println("DEBUG: Successfully updated average rating in recipe document")
                        } else {
                            println("DEBUG: Failed to update average rating in recipe document")
                        }
                        // Return the average rating regardless of update success
                        onComplete(averageRating)
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching ratings: ${exception.message}")
                    onComplete(0.0)
                }
        }

        fun updateRecipeAvgRating(recipeid: String, avgRating: Double, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            db.collection("recipes")
                .document(recipeid)
                .update("rating", avgRating)
                .addOnSuccessListener {
                    println("DEBUG: Average rating updated to $avgRating for recipe $recipeid")
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    println("ERROR: Failed to update average rating: ${exception.message}")
                    onComplete(false)
                }
        }
        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date)
            } catch (e: Exception) {
                "Unknown date"
            }
        }


    }
}