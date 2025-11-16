package com.mobdeve.s17.itismob_mc0

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s17.itismob_mc0.databinding.SavedLayoutBinding

class SavedRecipeViewHolder(private val binding: SavedLayoutBinding, private val context: android.content.Context)
    : RecyclerView.ViewHolder(binding.root) {

    private val recipeImageIv: ImageView = binding.srDishimageIv
    private val recipeNameTv: TextView = binding.srDishnameTv
    private val timeServingTv: TextView = binding.srTimeServingTv
    private val ratingTv: TextView = binding.srRatingTv
    private val authorTv: TextView = binding.srAuthorTv
    private val saveBtnIb: ImageButton = binding.srSaveBtn
    private val calendarBtn: ImageButton = binding.srAddBtn

    // Create local DB helper instance
    private val localDb = LocalDatabaseHelper(context)

    fun bindData(recipe: RecipeModel) {
        recipeNameTv.text = recipe.label
        authorTv.text = recipe.author
        timeServingTv.text = "${recipe.prepTime} mins | Serving for ${recipe.serving}"
        ratingTv.text = String.format("%.1f / 5.0", recipe.rating)

        Glide.with(itemView.context)
            .load(recipe.imageId)
            .placeholder(R.drawable.ic_launcher_background)
            .into(recipeImageIv)

        // --- Save/Unsave ---
        saveBtnIb.isSelected = recipe.isSaved
        updateSaveButtonVisual()

        saveBtnIb.setOnClickListener {
            if (saveBtnIb.isSelected) {
                saveBtnIb.isSelected = false
                localDb.unsaveRecipeLocal(recipe.id)
            } else {
                saveBtnIb.isSelected = true
                localDb.saveRecipeLocal(recipe.id) 
            }
            updateSaveButtonVisual()
        }

        // --- Calendar Button ---
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

    private fun updateSaveButtonVisual() {
        if (saveBtnIb.isSelected) {
            saveBtnIb.setBackgroundResource(R.drawable.savedbtn_design)
            saveBtnIb.setImageResource(R.drawable.ic_saved)
        } else {
            saveBtnIb.setBackgroundResource(R.drawable.savebtn_design)
            saveBtnIb.setImageResource(R.drawable.ic_save)
        }
    }
}
