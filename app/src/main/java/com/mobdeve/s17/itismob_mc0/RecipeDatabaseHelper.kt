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
                            rating = document.getDouble("rating") ?: 0.0,
                            serving = document.getLong("yield")?.toInt() ?: 0,
                            isSaved = document.getBoolean("isSaved") ?: false
                        )
                        recipes.add(recipe)
                    }
                    println("DEBUG: Fetched ${recipes.size} recipes with all fields")
                    onComplete(recipes)
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
                            isSaved = document.getBoolean("isSaved") ?: false
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

        // Add this to RecipeDatabaseHelper companion object
        fun addCommentToRecipe(recipeId: String, comment: CommentModel, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            val commentData = hashMapOf(
                "commentor" to comment.commentor,
                "comment" to comment.comment,
                "comment_date" to FieldValue.serverTimestamp(), // Use server timestamp for consistency
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