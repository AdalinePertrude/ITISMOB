package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.provider.CalendarContract
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s17.itismob_mc0.databinding.SavedLayoutBinding

class SavedRecipeViewHolder(private val binding: SavedLayoutBinding)
    : RecyclerView.ViewHolder(binding.root) {

    private val recipeImageIv: ImageView = binding.srDishimageIv
    private val recipeNameTv: TextView = binding.srDishnameTv
    private val timeServingTv: TextView = binding.srTimeServingTv
    private val ratingTv: TextView = binding.srRatingTv
    private val authorTv: TextView = binding.srAuthorTv
    private val saveBtnIb: ImageButton = binding.srSaveBtn
    private val calendarBtn: ImageButton = binding.srAddBtn

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
                DatabaseHelper.unsaveRecipe(recipe.id) { }
            } else {
                saveBtnIb.isSelected = true
                DatabaseHelper.saveRecipe(recipe.id) { }
            }
            updateSaveButtonVisual()
        }

        calendarBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, recipe.label)
                putExtra(CalendarContract.Events.DESCRIPTION, "Plan to cook this recipe.")
            }
            itemView.context.startActivity(intent)
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
