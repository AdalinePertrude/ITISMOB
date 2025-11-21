package com.mobdeve.s17.itismob_mc0

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.RatingBar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
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
                            createdAt = document.getTimestamp("createdAt")?.toDate().toString()
                                ?: "",
                            cuisineType = document.get("cuisineType") as? List<String>
                                ?: emptyList(),
                            dietLabels = document.get("dietLabels") as? List<String> ?: emptyList(),
                            dishType = document.get("dishType") as? List<String> ?: emptyList(),
                            healthLabels = document.get("healthLabels") as? List<String>
                                ?: emptyList(),
                            imageId = document.getString("image") ?: "",
                            ingredients = document.get("ingredientLines") as? List<String>
                                ?: emptyList(),
                            instructions = document.get("instructions") as? List<String>
                                ?: emptyList(),
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
                            createdAt = document.getTimestamp("createdAt")?.toDate().toString()
                                ?: "",
                            cuisineType = document.get("cuisineType") as? List<String>
                                ?: emptyList(),
                            dietLabels = document.get("dietLabels") as? List<String> ?: emptyList(),
                            dishType = document.get("dishType") as? List<String> ?: emptyList(),
                            healthLabels = document.get("healthLabels") as? List<String>
                                ?: emptyList(),
                            imageId = document.getString("image") ?: "",
                            ingredients = document.get("ingredientLines") as? List<String>
                                ?: emptyList(),
                            instructions = document.get("instructions") as? List<String>
                                ?: emptyList(),
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
                            commentDate = formatDate(
                                document.getTimestamp("comment_date")?.toDate().toString()
                            ) ?: "",
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

        fun addOrUpdateRecipeRating(recipeId: String, rating: RatingModel, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            val ratingData = hashMapOf(
                "rater" to rating.rater,   // userId
                "rating" to rating.rating,
                "ratedAt" to FieldValue.serverTimestamp()
            )

            db.collection("recipes")
                .document(recipeId)
                .collection("ratings")
                .document(rating.rater)       // 👈 overwrite OR create if not existing
                .set(ratingData)
                .addOnSuccessListener {
                    println("DEBUG: Rating saved/updated")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    println("ERROR: Failed to save rating: ${e.message}")
                    onComplete(false)
                }
        }

         fun loadUserRating(recipeId: String, userId: String, ratingBar: RatingBar) {
            val db = Firebase.firestore

            db.collection("recipes")
                .document(recipeId)
                .collection("ratings")
                .document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val rating = doc.getDouble("rating")?.toFloat() ?: 0f
                        ratingBar.rating= rating
                    }
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

        fun updateUserInfo(userid: String, name: String, onComplete: (Boolean, String?) -> Unit) {
            val db = Firebase.firestore

            val userInfo = mapOf<String, Any>(
                "fullname" to name,
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

                        val isMatch =
                            PasswordHasher.verifyPassword(inputPassword, storedHashedPassword)
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
                val outputFormat =
                    SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
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
                "authorid" to recipe.author,

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
                "isSaved" to recipe.isSaved,
                "description" to recipe.description
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

        fun getAddedToCalendarRecipes(selectedDay: String, userid: String, onComplete: (ArrayList<RecipeModel>) -> Unit) {
            val db = Firebase.firestore

            Log.d("DatabaseHelper", "Searching for recipes for date: '$selectedDay'")

            // Parse the target date from string
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val targetDate = try {
                // Extract just the date part (e.g., "November 17, 2025")
                val datePart = selectedDay.substringBefore(" at ")
                dateFormat.parse(datePart)
            } catch (e: Exception) {
                Log.e("DatabaseHelper", "Error parsing date: $selectedDay", e)
                onComplete(ArrayList())
                return
            }

            if (targetDate == null) {
                Log.e("DatabaseHelper", "Could not parse date: $selectedDay")
                onComplete(ArrayList())
                return
            }

            // Create start and end of day for query range
            val calendar = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.time

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.time

            Log.d("DatabaseHelper", "Querying dates from $startOfDay to $endOfDay")

            db.collection("users")
                .document(userid)
                .collection("Added To Calendar Recipes")
                .whereGreaterThanOrEqualTo("selectedDate", startOfDay)
                .whereLessThan("selectedDate", endOfDay)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d(
                        "DatabaseHelper",
                        "Found ${querySnapshot.documents.size} calendar entries"
                    )

                    val recipeList = ArrayList<RecipeModel>()
                    val recipeIds = ArrayList<String>()

                    // Collect all recipe IDs
                    for (document in querySnapshot.documents) {
                        val recipeId = document.getString("recipeId")
                        val selectedDate = document.getTimestamp("selectedDate")?.toDate()
                        Log.d(
                            "DatabaseHelper",
                            "Found calendar entry - recipeId: '$recipeId', date: $selectedDate"
                        )
                        recipeId?.let { recipeIds.add(it) }
                    }

                    if (recipeIds.isEmpty()) {
                        Log.d("DatabaseHelper", "No recipes found for this date")
                        onComplete(recipeList)
                        return@addOnSuccessListener
                    }

                    Log.d("DatabaseHelper", "Fetching recipes with IDs: $recipeIds")

                    // Fetch recipe details
                    db.collection("recipes")
                        .whereIn("id", recipeIds)
                        .get()
                        .addOnSuccessListener { recipesSnapshot ->
                            for (document in recipesSnapshot.documents) {
                                val recipe = RecipeModel(
                                    id = document.getString("id") ?: "",
                                    author = document.getString("author") ?: "",
                                    calories = document.getLong("calories")?.toInt() ?: 0,
                                    cautions = document.get("cautions") as? List<String>
                                        ?: emptyList(),
                                    createdAt = document.getTimestamp("createdAt")?.toDate()
                                        .toString() ?: "",
                                    cuisineType = document.get("cuisineType") as? List<String>
                                        ?: emptyList(),
                                    dietLabels = document.get("dietLabels") as? List<String>
                                        ?: emptyList(),
                                    dishType = document.get("dishType") as? List<String>
                                        ?: emptyList(),
                                    healthLabels = document.get("healthLabels") as? List<String>
                                        ?: emptyList(),
                                    imageId = document.getString("image")
                                        ?: "", // Note: field name might be "image"
                                    ingredients = document.get("ingredientLines") as? List<String>
                                        ?: emptyList(),
                                    instructions = document.get("instructions") as? List<String>
                                        ?: emptyList(),
                                    label = document.getString("label") ?: "",
                                    mealType = document.get("mealType") as? List<String>
                                        ?: emptyList(),
                                    prepTime = document.getLong("preptime")?.toInt() ?: 0,
                                    rating = document.getDouble("rating") ?: 0.0,
                                    serving = document.getLong("yield")?.toInt() ?: 0,
                                    isSaved = document.getBoolean("isSaved") ?: false,
                                    description = document.getString("description") ?: ""
                                )
                                recipeList.add(recipe)
                                Log.d("DatabaseHelper", "Added recipe: ${recipe.label}")
                            }
                            Log.d(
                                "DatabaseHelper",
                                "Successfully fetched ${recipeList.size} recipes"
                            )
                            onComplete(recipeList)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DatabaseHelper", "Error fetching recipes: ${exception.message}")
                            onComplete(ArrayList())
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("DatabaseHelper", "Error fetching calendar entries: ${exception.message}")
                    onComplete(ArrayList())
                }
        }

        fun addRecipeToCalendar(userid: String, recipeId: String, year: Int, month: Int, day: Int, callback: (Boolean) -> Unit) {
            val db = Firebase.firestore

            // Create date with consistent time (start of day)
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0) // Set to start of day
                set(Calendar.MILLISECOND, 0)
            }
            val selectedDate = Timestamp(calendar.time)

            val data = hashMapOf(
                "recipeId" to recipeId,
                "selectedDate" to selectedDate,
                "addedAt" to FieldValue.serverTimestamp()
            )

            db.collection("users")
                .document(userid)
                .collection("Added To Calendar Recipes")
                .add(data)
                .addOnSuccessListener {
                    Log.d("Calendar", "Recipe added to calendar successfully for $year-$month-$day")
                    callback(true)
                }
                .addOnFailureListener { exception ->
                    Log.e("Calendar", "Error adding recipe to calendar: ${exception.message}")
                    callback(false)
                }
        }

        fun deleteRecipeFromCalendar(
            userid: String,
            recipeId: String,
            year: Int,
            month: Int,
            day: Int,
            callback: (Boolean) -> Unit
        ) {
            val db = Firebase.firestore

            // Create date from parameters - set time to start of day for exact matching
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0) // Set to start of day
                set(Calendar.MILLISECOND, 0)   // Clear milliseconds
            }
            val selectedDate = Timestamp(calendar.time)

            Log.d("DatabaseHelper", "Searching for recipe: $recipeId on date: $selectedDate ($year-$month-$day)")

            // Query to find the specific recipe on the specific date
            db.collection("users")
                .document(userid)
                .collection("Added To Calendar Recipes")
                .whereEqualTo("recipeId", recipeId)
                .whereEqualTo("selectedDate", selectedDate)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DatabaseHelper", "Found ${querySnapshot.documents.size} matching documents")

                    if (querySnapshot.isEmpty) {
                        Log.d("DatabaseHelper", "No recipe found to delete for $year-$month-$day")

                        // Debug: Let's see what dates are actually in the database
                        debugCalendarEntries(userid, year, month, day)

                        callback(false)
                        return@addOnSuccessListener
                    }

                    // Delete all matching documents
                    val batch = db.batch()
                    for (document in querySnapshot.documents) {
                        Log.d("DatabaseHelper", "Deleting document: ${document.id}")
                        batch.delete(document.reference)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("DatabaseHelper", "Recipe deleted from calendar successfully for $year-$month-$day")
                            callback(true)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DatabaseHelper", "Error deleting recipe from calendar: ${exception.message}")
                            callback(false)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("DatabaseHelper", "Error finding recipe to delete: ${exception.message}")
                    callback(false)
                }
        }

        // Helper function to debug what's actually in the database
        private fun debugCalendarEntries(userid: String, year: Int, month: Int, day: Int) {
            val db = Firebase.firestore
            val calendar = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val targetDate = Timestamp(calendar.time)

            db.collection("users")
                .document(userid)
                .collection("Added To Calendar Recipes")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DatabaseHelper", "=== DEBUG: All calendar entries ===")
                    for (document in querySnapshot.documents) {
                        val docRecipeId = document.getString("recipeId")
                        val docDate = document.getTimestamp("selectedDate")
                        val docId = document.id

                        Log.d("DatabaseHelper", "Doc: $docId, Recipe: $docRecipeId, Date: $docDate")

                        // Check if dates match
                        val datesMatch = docDate?.toDate()?.time == targetDate.toDate().time
                        Log.d("DatabaseHelper", "Date matches target: $datesMatch")
                    }
                    Log.d("DatabaseHelper", "=== END DEBUG ===")
                }
        }


        fun updateUsernameAcrossAllData(oldUsername: String, newUsername: String, onComplete: (Boolean, String?) -> Unit) {
            val db = Firebase.firestore

            Log.d("DatabaseHelper", "Starting username update from '$oldUsername' to '$newUsername'")

            // Update in recipes collection (as author)
            updateRecipeAuthors(oldUsername, newUsername) { recipeSuccess ->
                if (!recipeSuccess) {
                    Log.e("DatabaseHelper", "Failed to update recipe authors")
                    onComplete(false, "Failed to update recipe authors")
                    return@updateRecipeAuthors
                }

                Log.d("DatabaseHelper", "Successfully updated recipe authors")

                // Update in comments across all recipes
                updateCommentsAuthor(oldUsername, newUsername) { commentSuccess ->
                    if (!commentSuccess) {
                        Log.e("DatabaseHelper", "Failed to update comments")
                        onComplete(false, "Failed to update comments")
                        return@updateCommentsAuthor
                    }

                    Log.d("DatabaseHelper", "Successfully updated comments")

                    // Update in ratings across all recipes
                    updateRatingsAuthor(oldUsername, newUsername) { ratingSuccess ->
                        if (!ratingSuccess) {
                            Log.e("DatabaseHelper", "Failed to update ratings")
                            onComplete(false, "Failed to update ratings")
                            return@updateRatingsAuthor
                        }

                        Log.d("DatabaseHelper", "Successfully updated ratings")
                        Log.d("DatabaseHelper", "Username update completed successfully")
                        onComplete(true, null)
                    }
                }
            }
        }

        /**
         * Update author name in all recipes created by this user
         */
        private fun updateRecipeAuthors(oldUsername: String, newUsername: String, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            db.collection("recipes")
                .whereEqualTo("author", oldUsername)
                .get()
                .addOnSuccessListener { documents ->
                    Log.d("DatabaseHelper", "Found ${documents.size()} recipes to update")

                    if (documents.isEmpty) {
                        Log.d("DatabaseHelper", "No recipes found for author: $oldUsername")
                        onComplete(true)
                        return@addOnSuccessListener
                    }

                    val batch = db.batch()
                    for (document in documents) {
                        batch.update(document.reference, "author", newUsername)
                        Log.d("DatabaseHelper", "Updating recipe author: ${document.id}")
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("DatabaseHelper", "Updated author name in ${documents.size()} recipes")
                            onComplete(true)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DatabaseHelper", "Failed to update recipe authors: ${exception.message}")
                            onComplete(false)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("DatabaseHelper", "Error finding recipes to update: ${exception.message}")
                    onComplete(false)
                }
        }

        /**
         * Update commentor name in all comments across all recipes
         */
        private fun updateCommentsAuthor(oldUsername: String, newUsername: String, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            // Search across all comments in all recipes
            db.collectionGroup("comments")
                .whereEqualTo("commentor", oldUsername)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DatabaseHelper", "Found ${querySnapshot.documents.size} comments to update")

                    if (querySnapshot.isEmpty) {
                        Log.d("DatabaseHelper", "No comments found for user: $oldUsername")
                        onComplete(true)
                        return@addOnSuccessListener
                    }

                    val batch = db.batch()
                    for (document in querySnapshot.documents) {
                        val recipeId = document.reference.parent.parent?.id
                        Log.d("DatabaseHelper", "Updating comment in recipe: $recipeId, comment: ${document.id}")
                        batch.update(document.reference, "commentor", newUsername)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("DatabaseHelper", "Updated commentor name in ${querySnapshot.size()} comments")
                            onComplete(true)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DatabaseHelper", "Failed to update comments: ${exception.message}")
                            onComplete(false)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("DatabaseHelper", "Error finding comments to update: ${exception.message}")
                    onComplete(false)
                }
        }

        /**
         * Update rater name in all ratings across all recipes
         */
        private fun updateRatingsAuthor(oldUsername: String, newUsername: String, onComplete: (Boolean) -> Unit) {
            val db = Firebase.firestore

            // Search across all ratings in all recipes
            db.collectionGroup("ratings")
                .whereEqualTo("rater", oldUsername)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    Log.d("DatabaseHelper", "Found ${querySnapshot.documents.size} ratings to update")

                    if (querySnapshot.isEmpty) {
                        Log.d("DatabaseHelper", "No ratings found for user: $oldUsername")
                        onComplete(true)
                        return@addOnSuccessListener
                    }

                    val batch = db.batch()
                    for (document in querySnapshot.documents) {
                        val recipeId = document.reference.parent.parent?.id
                        Log.d("DatabaseHelper", "Updating rating in recipe: $recipeId, rating: ${document.id}")
                        batch.update(document.reference, "rater", newUsername)
                    }

                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("DatabaseHelper", "Updated rater name in ${querySnapshot.size()} ratings")
                            onComplete(true)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("DatabaseHelper", "Failed to update ratings: ${exception.message}")
                            onComplete(false)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("DatabaseHelper", "Error finding ratings to update: ${exception.message}")
                    onComplete(false)
                }
        }
    }
}

