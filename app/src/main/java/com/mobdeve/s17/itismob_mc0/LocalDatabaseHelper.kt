package com.mobdeve.s17.itismob_mc0

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LocalDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipes_db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_RECIPES = "recipes"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_RECIPES (
                id TEXT PRIMARY KEY,
                author TEXT,
                label TEXT,
                isSaved INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RECIPES")
        onCreate(db)
    }


    fun getSavedRecipesLocal(): List<RecipeModel> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RECIPES WHERE isSaved = 1", null)
        val list = ArrayList<RecipeModel>()
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRecipe(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun saveRecipeLocal(recipeId: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("isSaved", 1) }
        db.update(TABLE_RECIPES, values, "id = ?", arrayOf(recipeId))
    }

    fun unsaveRecipeLocal(recipeId: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("isSaved", 0) }
        db.update(TABLE_RECIPES, values, "id = ?", arrayOf(recipeId))
    }

    private fun cursorToRecipe(cursor: android.database.Cursor): RecipeModel {
        return RecipeModel(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            author = cursor.getString(cursor.getColumnIndexOrThrow("author")),
            label = cursor.getString(cursor.getColumnIndexOrThrow("label")),
            isSaved = cursor.getInt(cursor.getColumnIndexOrThrow("isSaved")) == 1,
            calories = 0, // add more fields if needed
            cautions = emptyList(),
            createdAt = "",
            cuisineType = emptyList(),
            dietLabels = emptyList(),
            dishType = emptyList(),
            healthLabels = emptyList(),
            imageId = "",
            ingredients = emptyList(),
            instructions = emptyList(),
            mealType = emptyList(),
            prepTime = 0,
            rating = 0.0,
            serving = 0,
            isPublished = false
        )
    }
}
