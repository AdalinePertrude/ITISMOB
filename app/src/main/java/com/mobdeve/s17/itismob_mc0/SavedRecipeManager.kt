package com.mobdeve.s17.itismob_mc0

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log


object SavedRecipeManager {
    private val _savedRecipeIds = mutableSetOf<String>()
    private val listeners = mutableListOf<(Set<String>) -> Unit>()

//    fun initializeFromDatabase(dbHandler: SQLiteDatabaseHandler, context: Context) {
//        Thread {
//            val savedRecipes = dbHandler.getSavedRecipes()
//            val savedIds = savedRecipes.map { it.id }.toSet()
//            // Use the context to run on UI thread
//            (context as? Activity)?.runOnUiThread {
//                _savedRecipeIds.clear()
//                _savedRecipeIds.addAll(savedIds)
//                notifyListeners()
//            }
//        }.start()
//    }


    fun updateSavedRecipes(recipeIds: Set<String>) {
        _savedRecipeIds.clear()
        _savedRecipeIds.addAll(recipeIds)
        notifyListeners()
    }

    fun addSavedRecipe(recipeId: String) {
        _savedRecipeIds.add(recipeId)
        notifyListeners()
    }

    fun removeSavedRecipe(recipeId: String) {
        _savedRecipeIds.remove(recipeId) // Only remove the specific recipe
        notifyListeners()
    }

    fun getSavedRecipeIds(): Set<String> {
        return _savedRecipeIds.toSet()
    }

//    fun isRecipeSaved(recipeId: String): Boolean {
//        return _savedRecipeIds.contains(recipeId)
//    }

    fun addListener(listener: (Set<String>) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Set<String>) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        val currentIds = getSavedRecipeIds()
        Log.d("SyncDebug", "Notifying ${listeners.size} listeners about ${currentIds.size} saved recipes: $currentIds")

        listeners.forEach { listener ->
            android.os.Handler(Looper.getMainLooper()).post {
                listener.invoke(currentIds)
            }
        }
    }
}