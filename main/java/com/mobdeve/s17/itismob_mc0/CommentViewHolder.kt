package com.mobdeve.s17.itismob_mc0

import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.CommentLayoutBinding

class CommentViewHolder(private val viewBinding: CommentLayoutBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindCommentModel(model: CommentModel) {
        viewBinding.commentorTv.text = model.commentor
        viewBinding.commentDateTv.text = model.commentDate
        viewBinding.commentTv.text = model.comment

    }
}