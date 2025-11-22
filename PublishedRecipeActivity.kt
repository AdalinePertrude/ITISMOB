package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.mobdeve.s17.itismob_mc0.databinding.PublishedPageBinding

class PublishedRecipeActivity : ComponentActivity() {
    private lateinit var viewBinding: PublishedPageBinding
    private lateinit var publishedRecipesRv: RecyclerView
    private val publishedRecipeData: ArrayList<RecipeModel> = ArrayList()
    private lateinit var publishedAdapter: PublishedRecipeAdapter

    private val USER_PREFERENCE = "USER_PREFERENCE"
    private lateinit var sp: SharedPreferences

    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PublishedPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Get userName from SharedPreferences
        sp = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        userName = sp.getString("userName", null)

        setupRecyclerView()
        setupBackButton()

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val recipeToDelete = publishedRecipeData[position]

                DatabaseHelper.deleteRecipe(recipeToDelete.id) { success ->
                    if (success) {
                        publishedRecipeData.removeAt(position)
                        publishedAdapter.notifyItemRemoved(position)

                        if (publishedRecipeData.isEmpty()) {
                            viewBinding.noRecipesText.visibility = View.VISIBLE
                            viewBinding.recipesRv.visibility = View.GONE
                        }
                    } else {
                        publishedAdapter.notifyItemChanged(position)
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(publishedRecipesRv)
        loadPublishedRecipes()
    }

    private fun setupRecyclerView() {
        publishedRecipesRv = viewBinding.recipesRv
        publishedAdapter = PublishedRecipeAdapter(publishedRecipeData)

        publishedRecipesRv.adapter = publishedAdapter
        publishedRecipesRv.layoutManager = LinearLayoutManager(
            this, LinearLayoutManager.VERTICAL, false
        )

        publishedAdapter.setOnItemClickListener { recipe ->
            navigateToViewRecipe(recipe.id)
        }
    }

    private fun loadPublishedRecipes() {
        // Get userId from SharedPreferences instead of userName
        val userId = sp.getString("userId", "") ?: ""

        if (userId.isEmpty()) {
            viewBinding.noRecipesText.visibility = View.VISIBLE
            viewBinding.recipesRv.visibility = View.GONE
            Log.e("PublishedRecipe", "userId is null or empty")
            return
        }

        Log.d("PublishedRecipe", "Fetching recipes for user ID: $userId")

        // Use the updated fetchRecipesByAuthor that takes userId
        DatabaseHelper.fetchRecipesByAuthor(userId) { recipes ->
            runOnUiThread {
                Log.d("PublishedRecipe", "Received ${recipes.size} recipes from database")

                publishedRecipeData.clear()

                // Since we're fetching from user's Published Recipes collection,
                // all recipes should already be published, so no need to filter
                publishedRecipeData.addAll(recipes)
                publishedAdapter.notifyDataSetChanged()

                Log.d("PublishedRecipe", "Displaying ${publishedRecipeData.size} recipes")

                if (publishedRecipeData.isEmpty()) {
                    viewBinding.noRecipesText.visibility = View.VISIBLE
                    viewBinding.recipesRv.visibility = View.GONE
                    Log.d("PublishedRecipe", "No published recipes found for user: $userId")
                } else {
                    viewBinding.noRecipesText.visibility = View.GONE
                    viewBinding.recipesRv.visibility = View.VISIBLE
                    Log.d("PublishedRecipe", "Displaying ${publishedRecipeData.size} published recipes")
                }
            }
        }
    }

    private fun setupBackButton() {
        val returnPageBtn: ImageButton = viewBinding.returnPageBtn2
        returnPageBtn.setOnClickListener {
            finish()
        }
    }

    private fun navigateToViewRecipe(recipeId: String) {
        val intent = Intent(this, ViewRecipeActivity::class.java)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivity(intent)
    }
}