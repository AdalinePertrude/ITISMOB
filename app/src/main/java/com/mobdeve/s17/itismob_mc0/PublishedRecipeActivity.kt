package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.PublishedPageBinding
import androidx.recyclerview.widget.ItemTouchHelper

class PublishedRecipeActivity : ComponentActivity() {
    private lateinit var viewBinding: PublishedPageBinding
    private lateinit var publishedRecipesRv: RecyclerView
    private val publishedRecipeData: ArrayList<RecipeModel> = ArrayList()
    private lateinit var publishedAdapter: PublishedRecipeAdapter
    private val USER_PREFERENCE = "USER_PREFERENCE"
    private lateinit var sp: SharedPreferences
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PublishedPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Get user ID from SharedPreferences
        sp = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        userId = sp.getString("userId", null)

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

                // Delete from Firebase
                DatabaseHelper.deleteRecipe(recipeToDelete.id) { success ->
                    if (success) {
                        publishedRecipeData.removeAt(position)
                        publishedAdapter.notifyItemRemoved(position)

                        if (publishedRecipeData.isEmpty()) {
                            viewBinding.noRecipesText.visibility = View.VISIBLE
                            viewBinding.recipesRv.visibility = View.GONE
                        }
                    } else {
                        publishedAdapter.notifyItemChanged(position) // rollback if failure
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
        if (userId.isNullOrEmpty()) {
            viewBinding.noRecipesText.visibility = View.VISIBLE
            viewBinding.recipesRv.visibility = View.GONE
            return
        }

        DatabaseHelper.fetchRecipesByAuthor(userId!!) { recipes ->
            runOnUiThread {
                publishedRecipeData.clear()
                publishedRecipeData.addAll(recipes.filter { it.isPublished })
                publishedAdapter.notifyDataSetChanged()

                // Show/hide empty state
                if (publishedRecipeData.isEmpty()) {
                    viewBinding.noRecipesText.visibility = View.VISIBLE
                    viewBinding.recipesRv.visibility = View.GONE
                } else {
                    viewBinding.noRecipesText.visibility = View.GONE
                    viewBinding.recipesRv.visibility = View.VISIBLE
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