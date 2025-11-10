package com.mobdeve.s17.itismob_mc0

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s17.itismob_mc0.databinding.HpRecipeCardLayoutBinding

class HomeViewHolder(private var viewBinding: HpRecipeCardLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(model: DishesModel) {
        loadImage(model.imageId)
        viewBinding.hpDishnameTv.text = model.dishname
        viewBinding.hpRatingTv.text = "${model.rating} / 5.0"
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
            .centerCrop()
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
}