package com.mobdeve.s17.itismob_mc0

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s17.itismob_mc0.databinding.PublishedLayoutBinding

class PublishedRecipeViewHolder(private val binding: PublishedLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

    private val recipeImageIv: ImageView = itemView.findViewById(R.id.pub_recipe_image_iv)
    private val recipeNameTv: TextView = itemView.findViewById(R.id.pub_recipe_name_tv)
    private val timeServingTv: TextView = itemView.findViewById(R.id.pub_recipe_time_serving_tv)
    private val ratingTv: TextView = itemView.findViewById(R.id.pub_recipe_rating_tv)

    fun bindData(recipe: RecipeModel) {
        // Set dish name
        recipeNameTv.text = recipe.label

        // Set time and serving information
        val timeServing = "${recipe.prepTime} mins | Serving for ${recipe.serving}"
        timeServingTv.text = timeServing

        // Set rating
        DatabaseHelper.countRecipeRatings(recipe.id) { count ->
            // Update UI with rating count
            if(count == 0) {
                ratingTv.text = "---"
            }else{
                ratingTv.text = String.format("%.1f / 5.0", recipe.rating)
            }
        }

        // Load image using Glide - FIXED
        Glide.with(itemView.context)
            .load(recipe.imageId)
            .placeholder(R.drawable.ic_recipe_placeholder)
            .error(R.drawable.ic_recipe_error)
            .into(recipeImageIv)
    }
}