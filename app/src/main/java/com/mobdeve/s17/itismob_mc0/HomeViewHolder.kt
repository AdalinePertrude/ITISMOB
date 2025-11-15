package com.mobdeve.s17.itismob_mc0

import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobdeve.s17.itismob_mc0.databinding.HpRecipeCardLayoutBinding
import java.io.File
import java.util.logging.Handler

class HomeViewHolder(private var viewBinding: HpRecipeCardLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    private lateinit var recipeRepository: RecipeRepository

    init {
        recipeRepository = RecipeRepository(viewBinding.root.context)
    }

    fun bindData(model: RecipeModel) {
        loadImage(model.imageId)
        viewBinding.hpDishnameTv.text = model.label
        getRating(model.id)
        viewBinding.hpTimeServingTv.text = "${model.prepTime} mins | Serving for ${model.serving}"

        // Check if recipe is already saved offline
        checkIfSaved(model.id) { isSaved ->
            model.isSaved = isSaved
            updateSaveButtonUI(isSaved)
        }

        viewBinding.hpSaveBtn.setOnClickListener {
            model.isSaved = !model.isSaved
            if(model.isSaved){
                saveOffline(model)
            } else {
                unsaveOffline(model)
            }
            updateSaveButtonUI(model.isSaved)
        }
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(viewBinding.root.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)
            .override(300, 300)
            .fitCenter()
            .encodeQuality(70)
            .into(viewBinding.hpDishimageIv)
    }

    private fun updateSaveButtonUI(isSaved: Boolean) {
        if (isSaved) {
            viewBinding.hpSaveBtn.setBackgroundResource(R.drawable.savedbtn_design)
            viewBinding.hpSaveBtn.setImageResource(R.drawable.ic_saved)
        } else {
            viewBinding.hpSaveBtn.setBackgroundResource(R.drawable.savebtn_design)
            viewBinding.hpSaveBtn.setImageResource(R.drawable.ic_save)
        }
    }

    fun getRating(recipeId: String) {
        DatabaseHelper.fetchRecipeRating(recipeId) { rating ->
            viewBinding.hpRatingTv.text = "$rating / 5.0"
        }
    }

    private fun checkIfSaved(recipeId: String, callback: (Boolean) -> Unit) {
        val exists = recipeRepository.recipeExists(recipeId)
        callback(exists)
    }

    private fun saveOffline(recipe: RecipeModel) {
        // Show loading state
        viewBinding.hpSaveBtn.isEnabled = false

        recipeRepository.saveRecipeWithImage(recipe) { success ->
            // Re-enable button and show result
            viewBinding.hpSaveBtn.isEnabled = true

            if (success) {
                Log.d("SaveOperation", "Recipe saved offline: ${recipe.label}")
                Toast.makeText(viewBinding.root.context, "${recipe.label} saved offline", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("SaveOperation", "Failed to save recipe: ${recipe.label}")
                Toast.makeText(viewBinding.root.context, "Failed to save ${recipe.label}", Toast.LENGTH_SHORT).show()
                // Revert the UI state if save failed
                recipe.isSaved = false
                updateSaveButtonUI(false)
            }
        }
    }

    private fun unsaveOffline(recipe: RecipeModel) {
        Log.d("DeleteDebug", "=== STARTING DELETE ===")

        // get the fresh recipe data from database to ensure the local image path
        recipeRepository.getRecipeWithImage(recipe.id)?.let { freshRecipe ->

            val success = recipeRepository.deleteRecipeWithImage(freshRecipe)
            if (success) {
                Log.d("SaveOperation", "Recipe removed from offline: ${freshRecipe.label}")
                Toast.makeText(viewBinding.root.context, "${freshRecipe.label} removed from offline", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("SaveOperation", "Failed to remove recipe: ${freshRecipe.label}")
                Toast.makeText(viewBinding.root.context, "Failed to remove ${freshRecipe.label}", Toast.LENGTH_SHORT).show()
                // Revert the UI state if delete failed
                recipe.isSaved = true
                updateSaveButtonUI(true)
            }
        } ?: run {
            Log.e("DeleteDebug", "Recipe not found in database: ${recipe.id}")
            Toast.makeText(viewBinding.root.context, "Recipe not found", Toast.LENGTH_SHORT).show()
            recipe.isSaved = true
            updateSaveButtonUI(true)
        }
    }
}