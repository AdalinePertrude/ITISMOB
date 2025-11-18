package com.mobdeve.s17.itismob_mc0

import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobdeve.s17.itismob_mc0.databinding.SavedLayoutBinding

class SavedRecipeViewHolder(
    private val binding: SavedLayoutBinding,
    private val context: android.content.Context,
    private val onRecipeUnsaved: (String) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private val recipeImageIv: ImageView = binding.srDishimageIv
    private val recipeNameTv: TextView = binding.srDishnameTv
    private val timeServingTv: TextView = binding.srTimeServingTv
    private val ratingTv: TextView = binding.srRatingTv
    private val authorTv: TextView = binding.srAuthorTv
    private val saveBtnIb: ImageButton = binding.srSaveBtn
    private val calendarBtn: ImageButton = binding.srAddBtn

    private val localDb = SQLiteDatabaseHandler(context)
    private lateinit var recipeRepository: RecipeRepository

    init {
        recipeRepository = RecipeRepository(binding.root.context)
    }

    fun bindData(recipe: RecipeModel) {
        recipeNameTv.text = recipe.label
        authorTv.text = recipe.author
        timeServingTv.text = "${recipe.prepTime} mins | Serving for ${recipe.serving}"
        ratingTv.text = String.format("%.1f / 5.0", recipe.rating)
        loadImage(recipe.imageId)

        checkIfSaved(recipe.id) { isSaved ->
            recipe.isSaved = isSaved
            updateSaveButtonUI(isSaved)
        }

        saveBtnIb.setOnClickListener {
            val newSavedState = !recipe.isSaved
            recipe.isSaved = newSavedState
            updateSaveButtonUI(newSavedState)

            if(newSavedState){
                saveOffline(recipe)
            } else {
                unsaveOffline(recipe)
            }
        }

        calendarBtn.setOnClickListener {
            val sp = context.getSharedPreferences("USER_PREFERENCE", android.content.Context.MODE_PRIVATE)
            val userId = sp.getString("userId", null) ?: return@setOnClickListener

            CalendarPickerDialog.show(context) { year, month, day ->
                DatabaseHelper.addRecipeToCalendar(
                    userid = userId,
                    recipeId = recipe.id,
                    year = year,
                    month = month,
                    day = day
                ) { success ->
                    android.widget.Toast.makeText(
                        context,
                        if (success) "Added to planner!" else "Failed to add",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateSaveButtonUI(isSaved: Boolean) {
        if (isSaved) {
            saveBtnIb.setBackgroundResource(R.drawable.savedbtn_design)
            saveBtnIb.setImageResource(R.drawable.ic_saved)
        } else {
            saveBtnIb.setBackgroundResource(R.drawable.savebtn_design)
            saveBtnIb.setImageResource(R.drawable.ic_save)
        }
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(binding.root.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)
            .override(300, 300)
            .fitCenter()
            .encodeQuality(70)
            .into(recipeImageIv)
    }

    private fun checkIfSaved(recipeId: String, callback: (Boolean) -> Unit) {
        Thread {
            val exists = recipeRepository.recipeExists(recipeId)
            android.os.Handler(context.mainLooper).post {
                callback(exists)
            }
        }.start()
    }

    private fun saveOffline(recipe: RecipeModel) {
        saveBtnIb.isEnabled = false

        recipeRepository.saveRecipeWithImage(recipe) { success ->
            saveBtnIb.isEnabled = true

            if (success) {
                Log.d("SaveOperation", "Recipe saved offline: ${recipe.label}")
                Toast.makeText(binding.root.context, "${recipe.label} saved offline", Toast.LENGTH_SHORT).show()
                SavedRecipeManager.addSavedRecipe(recipe.id)
            } else {
                Log.e("SaveOperation", "Failed to save recipe: ${recipe.label}")
                Toast.makeText(binding.root.context, "Failed to save ${recipe.label}", Toast.LENGTH_SHORT).show()
                recipe.isSaved = false
                updateSaveButtonUI(false)
            }
        }
    }

    private fun unsaveOffline(recipe: RecipeModel) {
        Log.d("DeleteDebug", "=== STARTING DELETE ===")

        recipeRepository.getRecipeWithImage(recipe.id)?.let { freshRecipe ->
            val success = recipeRepository.deleteRecipeWithImage(freshRecipe)
            if (success) {
                Log.d("SaveOperation", "Recipe removed from offline: ${freshRecipe.label}")
                Toast.makeText(binding.root.context, "${freshRecipe.label} removed from offline", Toast.LENGTH_SHORT).show()
                SavedRecipeManager.removeSavedRecipe(recipe.id)

                // Call the callback to notify adapter - THIS IS THE KEY CHANGE
                onRecipeUnsaved(recipe.id)

            } else {
                Log.e("SaveOperation", "Failed to remove recipe: ${freshRecipe.label}")
                Toast.makeText(binding.root.context, "Failed to remove ${freshRecipe.label}", Toast.LENGTH_SHORT).show()
                recipe.isSaved = true
                updateSaveButtonUI(true)
            }
        } ?: run {
            Log.e("DeleteDebug", "Recipe not found in database: ${recipe.id}")
            Toast.makeText(binding.root.context, "Recipe not found", Toast.LENGTH_SHORT).show()
            recipe.isSaved = true
            updateSaveButtonUI(true)
        }
    }
}