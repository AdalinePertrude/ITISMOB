package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.SavedPageBinding

class SavedRecipeActivity : ComponentActivity() {
    private val savedRecipeListener: (Set<String>) -> Unit = { savedIds ->
        updateSavedRecipesList(savedIds)
    }
    private lateinit var binding: SavedPageBinding
    private lateinit var savedAdapter: SavedRecipeAdapter
    private val savedRecipeData: ArrayList<RecipeModel> = ArrayList()

    private lateinit var dbHandler: SQLiteDatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SavedPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHandler = SQLiteDatabaseHandler(this)

        setupRecyclerView()
        setupBackButton()
        loadSavedRecipes()
        SavedRecipeManager.addListener(savedRecipeListener)
    }

    private fun setupRecyclerView() {
        savedAdapter = SavedRecipeAdapter(this, savedRecipeData)

        savedAdapter.setOnRecipeUnsavedListener { recipeId ->
            //  called when a recipe is unsaved via the save button
            removeRecipeFromList(recipeId)
        }

        binding.savedRecipesRv.apply {
            adapter = savedAdapter
            layoutManager = LinearLayoutManager(
                this@SavedRecipeActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        savedAdapter.setOnItemClickListener { recipe ->
            navigateToViewRecipe(recipe.id)
        }
    }

    private fun removeRecipeFromList(recipeId: String) {
        //  adapter's removeRecipe method to handle the removal and animation
        savedAdapter.removeRecipe(recipeId)

        // Update UI state if list becomes empty
        if (savedRecipeData.isEmpty()) {
            binding.noSavedText.visibility = View.VISIBLE
            binding.savedRecipesRv.visibility = View.GONE
        }
    }

    private fun updateSavedRecipesList(savedIds: Set<String>) {
        Thread {
            val allRecipes = dbHandler.getSavedRecipes()
            val currentIds = allRecipes.map { it.id }.toSet()

            // If there's a mismatch, reload
            if (currentIds != savedIds) {
                runOnUiThread {
                    loadSavedRecipes()
                }
            }
        }.start()
    }

    private fun loadSavedRecipes() {
        Thread {
            val recipes = dbHandler.getSavedRecipes()

            runOnUiThread {
                savedRecipeData.clear()
                savedRecipeData.addAll(recipes)
                savedAdapter.notifyDataSetChanged()

                if (savedRecipeData.isEmpty()) {
                    binding.noSavedText.visibility = View.VISIBLE
                    binding.savedRecipesRv.visibility = View.GONE
                } else {
                    binding.noSavedText.visibility = View.GONE
                    binding.savedRecipesRv.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        SavedRecipeManager.removeListener(savedRecipeListener)
    }

    private fun setupBackButton() {
        binding.returnPageBtn2.setOnClickListener { finish() }
    }

    private fun navigateToViewRecipe(recipeId: String) {
        val intent = Intent(this, ViewRecipeActivity::class.java)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivity(intent)
    }
}