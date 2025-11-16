package com.mobdeve.s17.itismob_mc0

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale

class DatabaseHelper {
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
                            isSaved = document.getBoolean("isSaved") ?: false
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
                    //println("DEBUG: Average rating for recipe $recipeid: $averageRating ($ratingCount ratings)")

                    // Update the average rating in the recipe document
                    updateRecipeAvgRating(recipeid, averageRating) { success ->
                        if (success) {
                            //println("DEBUG: Successfully updated average rating in recipe document")
                        } else {
                            //println("DEBUG: Failed to update average rating in recipe document")
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

        fun addRecipeRating(recipeId: String, rating: RatingModel, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            val ratingData = hashMapOf(
                "rater" to rating.rater,
                "rating" to rating.rating,
                "ratedAt" to FieldValue.serverTimestamp()
            )

            db.collection("recipes")
                .document(recipeId)
                .collection("ratings")
                .add(ratingData)
                .addOnSuccessListener {
                    println("DEBUG: Rating successfully added")
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    println("ERROR: Failed to add rating: ${exception.message}")
                    onComplete(false)
                }
        }

        fun updateRecipeAvgRating(recipeid: String, avgRating: Double, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            db.collection("recipes")
                .document(recipeid)
                .update("rating", avgRating)
                .addOnSuccessListener {
                    // println("DEBUG: Average rating updated to $avgRating for recipe $recipeid")
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    // println("ERROR: Failed to update average rating: ${exception.message}")
                    onComplete(false)
                }
        }

        fun updateUserInfo(userid: String, name: String, email: String, onComplete: (Boolean, String?) -> Unit) {
            val db = Firebase.firestore

            val userInfo = mapOf<String, Any>(
                "fullname" to name,
                "email" to email,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            db.collection("users")
                .document(userid)
                .update(userInfo)
                .addOnSuccessListener {
                    println("DEBUG: User info updated successfully")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    println("ERROR: Failed to update user info: ${exception.message}")
                    onComplete(false, exception.message)
                }
        }
        fun updateUserPassword(userid: String, oldPassword: String, newPassword: String, onComplete: (Boolean, String?) -> Unit) {
            verifyUserPassword(userid, oldPassword) { isCorrect, error ->
                if (isCorrect) {
                    // Hash and update new password
                    val newHashedPassword = PasswordHasher.hashPassword(newPassword)
                    val db = Firebase.firestore

                    db.collection("users")
                        .document(userid)
                        .update("password", newHashedPassword)
                        .addOnSuccessListener {
                            println("DEBUG: Password updated successfully")
                            onComplete(true, null)
                        }
                        .addOnFailureListener { exception ->
                            println("ERROR: Failed to update password: ${exception.message}")
                            onComplete(false, exception.message)
                        }
                } else {
                    onComplete(false, error ?: "Old password is incorrect")
                }
            }
        }

        fun verifyUserPassword(userid: String, inputPassword: String, onComplete: (Boolean, String?) -> Unit) {
            val db = Firebase.firestore

            db.collection("users")
                .document(userid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val storedHashedPassword = document.getString("password")

                        if (storedHashedPassword == null) {
                            onComplete(false, "Password not found")
                            return@addOnSuccessListener
                        }

                        val isMatch = PasswordHasher.verifyPassword(inputPassword, storedHashedPassword)
                        onComplete(isMatch, if (isMatch) null else "Incorrect password")
                    } else {
                        onComplete(false, "User not found")
                    }
                }
                .addOnFailureListener { exception ->
                    onComplete(false, "Database error: ${exception.message}")
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

        fun fetchRecipesByAuthor(authorId: String, callback: (List<RecipeModel>) -> Unit) {
            val db = Firebase.firestore
            db.collection("recipes")
                .whereEqualTo("authorId", authorId)
                .get()
                .addOnSuccessListener { documents ->
                    val recipes = mutableListOf<RecipeModel>()
                    for (document in documents) {
                        val recipe = document.toObject(RecipeModel::class.java)
                        recipes.add(recipe)
                    }
                    callback(recipes)
                }
                .addOnFailureListener { exception ->
                    Log.e("DatabaseHelper", "Error fetching recipes by author", exception)
                    callback(emptyList())
                }
        }

        fun addRecipe(recipe: RecipeModel, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            val recipeData = hashMapOf(
                "id" to recipe.id,
                "author" to recipe.author,
                "authorId" to recipe.author,

                "calories" to recipe.calories,
                "cautions" to recipe.cautions,


                "createdAt" to FieldValue.serverTimestamp(),

                "cuisineType" to recipe.cuisineType,
                "dietLabels" to recipe.dietLabels,
                "dishType" to recipe.dishType,
                "healthLabels" to recipe.healthLabels,

                "image" to recipe.imageId,
                "ingredientLines" to recipe.ingredients,
                "instructions" to recipe.instructions,

                "label" to recipe.label,
                "mealType" to recipe.mealType,
                "preptime" to recipe.prepTime,

                "rating" to recipe.rating,
                "yield" to recipe.serving,

                "isPublished" to recipe.isPublished,
                "isSaved" to recipe.isSaved
            )

            db.collection("recipes")
                .document(recipe.id)
                .set(recipeData)
                .addOnSuccessListener {
                    Log.d("DB", "Recipe successfully added!")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("DB", "Error adding recipe", e)
                    onComplete(false)
                }
        }
        fun deleteRecipe(recipeId: String, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            db.collection("recipes")
                .document(recipeId)
                .delete()
                .addOnSuccessListener {
                    Log.d("DB", "Recipe successfully deleted")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("DB", "Error deleting recipe", e)
                    onComplete(false)
                }
        }

        fun fetchSavedRecipes(callback: (List<RecipeModel>) -> Unit) {
            Firebase.firestore.collection("recipes")
                .whereEqualTo("isSaved", true)
                .get()
                .addOnSuccessListener { docs ->
                    val list = docs.map { it.toObject(RecipeModel::class.java) }
                    callback(list)
                }
                .addOnFailureListener { callback(emptyList()) }
        }

        fun saveRecipe(id: String, callback: (Boolean) -> Unit) {
            Firebase.firestore.collection("recipes")
                .document(id)
                .update("isSaved", true)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }

        fun unsaveRecipe(id: String, callback: (Boolean) -> Unit) {
            Firebase.firestore.collection("recipes")
                .document(id)
                .update("isSaved", false)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        }
    }
}