package com.mobdeve.s17.itismob_mc0

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.AddedRecipeToCalendarBinding
import com.squareup.picasso.Picasso

class AddedRecipeViewHolder (private val viewBinding: AddedRecipeToCalendarBinding) : RecyclerView.ViewHolder(viewBinding.root){

    fun bindAddedDishModel(model : DishesModel) {

        Picasso.get()
            .load(Uri.parse(model.imageId))
            .fit()
            .centerCrop()
            .into(viewBinding.atcDishIv)


        viewBinding.atcDishnameTv.setText(model.dishname);
        viewBinding.atcRatingTv.text = "${model.rating} / 5.0"
        viewBinding.atcTimeServingTv.text = "${model.prepTime} mins | Serving for ${model.serving}"
    }
}