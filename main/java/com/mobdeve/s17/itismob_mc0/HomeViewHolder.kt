package com.mobdeve.s17.itismob_mc0

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.HpRecipeCardLayoutBinding
import com.squareup.picasso.Picasso
class HomeViewHolder(private var viewBinding: HpRecipeCardLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bindData(model: DishesModel) {
        if (model.imageId.isNotEmpty()) {
            println("Loading image: ${model.imageId}")

            Picasso.get()
                .load(model.imageId)
                .fit()
                .centerCrop()
                .config(Bitmap.Config.RGB_565) // Reduce memory usage by half
                .centerCrop()
                .into(viewBinding.hpDishimageIv, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        println("Image loaded successfully")
                    }
                    override fun onError(e: Exception?) {
                        println("Image load failed: ${e?.message}")
                    }
                })
        } else {
            println("No image URL provided")
        }

        viewBinding.hpDishnameTv.text = model.dishname
        viewBinding.hpRatingTv.text = "${model.rating} / 5.0"
        viewBinding.hpTimeServingTv.text = "${model.prepTime} mins | Serving for ${model.serving}"
    }
}