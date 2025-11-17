package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.SavedPageBinding

class SavedRecipeActivity : ComponentActivity() {

    private lateinit var binding: SavedPageBinding
    private lateinit var savedRecipesRv: RecyclerView
    private lateinit var savedAdapter: SavedRecipeAdapter
    private val savedRecipeData: ArrayList<RecipeModel> = ArrayList()

    private lateinit var dbHandler: SQLiteDatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SavedPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHandler = SQLiteDatabaseHandler(this) // initialize local DB

        setupRecyclerView()
        setupBackButton()
        setupSwipeToUnsave()

        loadSavedRecipes()
    }

    private fun setupRecyclerView() {
        savedRecipesRv = binding.linearLayout2.getChildAt(0) as RecyclerView
        savedAdapter = SavedRecipeAdapter(this, savedRecipeData)

        savedRecipesRv.adapter = savedAdapter
        savedRecipesRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        savedAdapter.setOnItemClickListener { recipe ->
            navigateToViewRecipe(recipe.id)
        }
    }

    private fun setupSwipeToUnsave() {
        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.adapterPosition
                val recipe = savedRecipeData[pos]

                // Unsave in local DB
                dbHandler.unsaveRecipe(recipe.id)

                // Remove from list
                savedRecipeData.removeAt(pos)
                savedAdapter.notifyItemRemoved(pos)

                // Handle empty state
                if (savedRecipeData.isEmpty()) {
                    binding.noSavedText.visibility = View.VISIBLE
                    savedRecipesRv.visibility = View.GONE
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(savedRecipesRv)
    }

    private fun loadSavedRecipes() {
        Thread {
            // Fetch saved recipes from local DB
            val recipes = dbHandler.getSavedRecipes()

            runOnUiThread {
                savedRecipeData.clear()
                savedRecipeData.addAll(recipes)
                savedAdapter.notifyDataSetChanged()

                if (savedRecipeData.isEmpty()) {
                    binding.noSavedText.visibility = View.VISIBLE
                    savedRecipesRv.visibility = View.GONE
                } else {
                    binding.noSavedText.visibility = View.GONE
                    savedRecipesRv.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun setupBackButton() {
        val backBtn: ImageButton = binding.returnPageBtn2
        backBtn.setOnClickListener { finish() }
    }

    private fun navigateToViewRecipe(recipeId: String) {
        val intent = Intent(this, ViewRecipeActivity::class.java)
        intent.putExtra("RECIPE_ID", recipeId)
        startActivity(intent)
    }
}
