package com.mobdeve.s17.itismob_mc0

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mobdeve.s17.itismob_mc0.databinding.HpRecipeCardLayoutBinding

class HomeViewHolder(private var viewBinding: HpRecipeCardLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(model: RecipeModel) {
        loadImage(model.imageId)
        viewBinding.hpDishnameTv.text = model.label
        getRating(model.id)
        viewBinding.hpTimeServingTv.text = "${model.prepTime} mins | Serving for ${model.serving}"

        updateSaveButtonUI(model.isSaved)

        viewBinding.hpSaveBtn.setOnClickListener {
            model.isSaved = !model.isSaved
            updateSaveButtonUI(model.isSaved)
        }
    }
    private fun loadImage(imageUrl: String) {
        Glide.with(viewBinding.root.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(true) // Don't cache in memory to prevent OOM
            .override(300, 300) // Limit image size to reduce memory usage
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
    fun getRating(recipeid : String){
        RecipeDatabaseHelper.fetchRecipeRating(recipeid) { rating ->
            viewBinding.hpRatingTv.text = "$rating / 5.0"
        }
    }

}