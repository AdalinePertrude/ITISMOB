package com.mobdeve.s17.itismob_mc0

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobdeve.s17.itismob_mc0.databinding.AddedRecipeToCalendarBinding

class AddedRecipeViewHolder (private val viewBinding: AddedRecipeToCalendarBinding) : RecyclerView.ViewHolder(viewBinding.root){

    fun bindAddedDishModel(model : DishesModel) {
        loadImage(model.imageId)
        viewBinding.atcDishnameTv.setText(model.dishname);
        viewBinding.atcRatingTv.text = "${model.rating} / 5.0"
        viewBinding.atcTimeServingTv.text = "${model.prepTime} mins | Serving for ${model.serving}"
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(viewBinding.root.context)
            .load(imageUrl)
            .centerCrop()
            .into(viewBinding.atcDishIv)
    }
}