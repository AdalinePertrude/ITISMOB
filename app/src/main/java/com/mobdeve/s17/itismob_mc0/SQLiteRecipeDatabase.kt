package com.mobdeve.s17.itismob_mc0

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException

class SQLiteRecipeDatabase(context: Context) {
    private lateinit var databaseHandler: SQLiteDatabaseHandler

    init {
        this.databaseHandler = SQLiteDatabaseHandler(context)
    }

    // Inserts a provided recipe into the database. Returns the id provided by the DB.
    fun addRecipe(recipe: RecipeModel): Int {
        val db = databaseHandler.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(SQLiteDatabaseHandler.RECIPE_RECIPE_ID, recipe.id)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_AUTHOR, recipe.author)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_CALORIES, recipe.calories)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_CREATED_AT, recipe.createdAt)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_IMAGE_ID, recipe.imageId)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_LABEL, recipe.label)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_PREP_TIME, recipe.prepTime)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_RATING, recipe.rating)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_SERVING, recipe.serving)
        contentValues.put(SQLiteDatabaseHandler.RECIPE_IS_SAVED, if (recipe.isSaved) 1 else 0) // FIXED: Added isSaved

        val _id = db.insert(SQLiteDatabaseHandler.RECIPE_TABLE, null, contentValues)

        db.close()

        return _id.toInt()
    }

    // Updates a provided recipe's variables
    fun updateRecipe(recipe: RecipeModel): Int {
        val db = databaseHandler.writableDatabase
        val contentValues = ContentValues().apply {
            put(SQLiteDatabaseHandler.RECIPE_AUTHOR, recipe.author)
            put(SQLiteDatabaseHandler.RECIPE_CALORIES, recipe.calories)
            put(SQLiteDatabaseHandler.RECIPE_CREATED_AT, recipe.createdAt)
            put(SQLiteDatabaseHandler.RECIPE_IMAGE_ID, recipe.imageId)
            put(SQLiteDatabaseHandler.RECIPE_LABEL, recipe.label)
            put(SQLiteDatabaseHandler.RECIPE_PREP_TIME, recipe.prepTime)
            put(SQLiteDatabaseHandler.RECIPE_RATING, recipe.rating)
            put(SQLiteDatabaseHandler.RECIPE_SERVING, recipe.serving)
            put(SQLiteDatabaseHandler.RECIPE_IS_SAVED, if (recipe.isSaved) 1 else 0) // FIXED: Added isSaved
        }

        val result = db.update(SQLiteDatabaseHandler.RECIPE_TABLE, contentValues,
            "${SQLiteDatabaseHandler.RECIPE_RECIPE_ID} = ?", arrayOf(recipe.id))

        db.close()
        return result
    }

    // Deletes the provided recipe from the DB
    fun deleteRecipe(recipe: RecipeModel): Int {
        val db = databaseHandler.writableDatabase

        val result = db.delete(SQLiteDatabaseHandler.RECIPE_TABLE,
            "${SQLiteDatabaseHandler.RECIPE_RECIPE_ID} = ?", arrayOf(recipe.id))

        db.close()
        return result
    }

    // Retrieves all recipes from the DB and places them into an array list
    fun getAllRecipes(): ArrayList<RecipeModel> {
        val result = ArrayList<RecipeModel>()
        val selectQuery = "SELECT * FROM ${SQLiteDatabaseHandler.RECIPE_TABLE}"
        val db = databaseHandler.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.close()
            return ArrayList()
        }

        if (cursor.moveToFirst()) {
            do {
                result.add(getRecipeFromCursor(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return result
    }

    // Get recipe by ID
    fun getRecipeById(recipeId: String): RecipeModel? {
        val selectQuery = "SELECT * FROM ${SQLiteDatabaseHandler.RECIPE_TABLE} WHERE ${SQLiteDatabaseHandler.RECIPE_RECIPE_ID} = ?"
        val db = databaseHandler.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, arrayOf(recipeId))
        } catch (e: SQLiteException) {
            db.close()
            return null
        }

        return if (cursor.moveToFirst()) {
            val recipe = getRecipeFromCursor(cursor)
            cursor.close()
            db.close()
            recipe
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    // Check if recipe exists
    fun recipeExists(recipeId: String): Boolean {
        return getRecipeById(recipeId) != null
    }

    // Update only the saved status
    fun updateSavedStatus(recipeId: String, isSaved: Boolean): Int {
        val db = databaseHandler.writableDatabase
        val contentValues = ContentValues().apply {
            put(SQLiteDatabaseHandler.RECIPE_IS_SAVED, if (isSaved) 1 else 0)
        }

        val result = db.update(SQLiteDatabaseHandler.RECIPE_TABLE, contentValues,
            "${SQLiteDatabaseHandler.RECIPE_RECIPE_ID} = ?", arrayOf(recipeId))

        db.close()
        return result
    }

    // Helper method to extract recipe from cursor
    private fun getRecipeFromCursor(cursor: Cursor): RecipeModel {
        return RecipeModel(
            id = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_RECIPE_ID)),
            author = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_AUTHOR)),
            calories = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_CALORIES)),
            cautions = emptyList(),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_CREATED_AT)),
            cuisineType = emptyList(),
            dietLabels = emptyList(),
            dishType = emptyList(),
            healthLabels = emptyList(),
            imageId = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_IMAGE_ID)),
            ingredients = emptyList(),
            instructions = emptyList(),
            label = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_LABEL)),
            mealType = emptyList(),
            prepTime = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_PREP_TIME)),
            rating = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_RATING)),
            serving = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_SERVING)),
            isSaved = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteDatabaseHandler.RECIPE_IS_SAVED)) == 1 // FIXED: Read from database
        )
    }
}