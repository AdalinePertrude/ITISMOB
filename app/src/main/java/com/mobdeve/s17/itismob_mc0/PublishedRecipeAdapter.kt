package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s17.itismob_mc0.databinding.PublishedLayoutBinding

class PublishedRecipeAdapter(private val data: ArrayList<RecipeModel>) : Adapter<PublishedRecipeViewHolder>() {

    private var onItemClickListener: ((RecipeModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (RecipeModel) -> Unit) {
        this.onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublishedRecipeViewHolder {
        val binding = PublishedLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PublishedRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PublishedRecipeViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(data[position])
            val intent = Intent(holder.itemView.context, ViewRecipeActivity::class.java)
            intent.putExtra("RECIPE_ID", data[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateData(newRecipesList: ArrayList<RecipeModel>) {
        data.clear()
        data.addAll(newRecipesList)
        notifyDataSetChanged()
    }
}