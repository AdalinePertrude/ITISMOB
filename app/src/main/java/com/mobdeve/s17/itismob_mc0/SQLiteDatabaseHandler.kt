package com.mobdeve.s17.itismob_mc0

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteDatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RecipesDatabase"
        private const val DATABASE_VERSION = 2

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
        const val RECIPE_IS_SAVED = "is_saved" // ADDED THIS MISSING COLUMN
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
                $RECIPE_IS_SAVED INTEGER DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $RECIPE_TABLE")
        onCreate(db)
    }
}