package com.mobdeve.s17.itismob_mc0

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteDatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RecipesDatabase"
        private const val DATABASE_VERSION = 3

        // Main recipe table
        const val RECIPE_TABLE = "recipes"
        const val RECIPE_ID = "id"
        const val RECIPE_RECIPE_ID = "recipe_id"
        const val RECIPE_AUTHOR = "author"
        const val RECIPE_CALORIES = "calories"
        const val RECIPE_CREATED_AT = "created_at"
        const val RECIPE_IMAGE_ID = "image_id"
        const val RECIPE_LABEL = "label"
        const val RECIPE_PREP_TIME = "prep_time"
        const val RECIPE_RATING = "rating"
        const val RECIPE_SERVING = "serving"
        const val RECIPE_IS_SAVED = "is_saved"

        const val RECIPE_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $RECIPE_TABLE (
                $RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $RECIPE_RECIPE_ID TEXT UNIQUE NOT NULL,
                $RECIPE_AUTHOR TEXT,
                $RECIPE_CALORIES INTEGER,
                $RECIPE_CREATED_AT TEXT,
                $RECIPE_IMAGE_ID TEXT,
                $RECIPE_LABEL TEXT NOT NULL,
                $RECIPE_PREP_TIME INTEGER,
                $RECIPE_RATING REAL,
                $RECIPE_SERVING INTEGER,
                $RECIPE_IS_SAVED INTEGER DEFAULT 0,
                $RECIPE_DESCRIPTION TEXT DEFAULT '' 
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $RECIPE_TABLE")
        onCreate(db)
    }


    fun getSavedRecipes(): List<RecipeModel> {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            RECIPE_TABLE,
            null,
            "$RECIPE_IS_SAVED = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        val list = ArrayList<RecipeModel>()
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToRecipe(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun saveRecipe(recipeId: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(RECIPE_IS_SAVED, 1)
        }
        db.update(RECIPE_TABLE, values, "$RECIPE_RECIPE_ID = ?", arrayOf(recipeId))
    }

    fun unsaveRecipe(recipeId: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(RECIPE_IS_SAVED, 0)
        }
        db.update(RECIPE_TABLE, values, "$RECIPE_RECIPE_ID = ?", arrayOf(recipeId))
    }

    private fun cursorToRecipe(cursor: Cursor): RecipeModel {
        return RecipeModel(
            id = cursor.getString(cursor.getColumnIndexOrThrow(RECIPE_RECIPE_ID)),
            author = cursor.getString(cursor.getColumnIndexOrThrow(RECIPE_AUTHOR)) ?: "",
            label = cursor.getString(cursor.getColumnIndexOrThrow(RECIPE_LABEL)) ?: "",
            imageId = cursor.getString(cursor.getColumnIndexOrThrow(RECIPE_IMAGE_ID)) ?: "",
            prepTime = cursor.getInt(cursor.getColumnIndexOrThrow(RECIPE_PREP_TIME)),
            serving = cursor.getInt(cursor.getColumnIndexOrThrow(RECIPE_SERVING)),
            rating = cursor.getDouble(cursor.getColumnIndexOrThrow(RECIPE_RATING)),
            isSaved = cursor.getInt(cursor.getColumnIndexOrThrow(RECIPE_IS_SAVED)) == 1,
            calories = cursor.getInt(cursor.getColumnIndexOrThrow(RECIPE_CALORIES)),
            cautions = emptyList(),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow(RECIPE_CREATED_AT)) ?: "",
            cuisineType = emptyList(),
            dietLabels = emptyList(),
            dishType = emptyList(),
            healthLabels = emptyList(),
            ingredients = emptyList(),
            instructions = emptyList(),
            mealType = emptyList(),
            isPublished = false,
            description = cursor.getString(cursor.getColumnIndexOrThrow(RECIPE_DESCRIPTION)) ?: ""
        )
    }
}
