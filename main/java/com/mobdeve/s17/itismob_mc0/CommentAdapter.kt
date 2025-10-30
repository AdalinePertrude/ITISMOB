package com.mobdeve.s17.itismob_mc0

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.CommentLayoutBinding

class CommentAdapter (val data : ArrayList<CommentModel>) : RecyclerView.Adapter<CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val CommentViewBinding: CommentLayoutBinding = CommentLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        return CommentViewHolder(CommentViewBinding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bindCommentModel(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}