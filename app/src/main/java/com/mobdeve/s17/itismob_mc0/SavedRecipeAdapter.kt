package com.mobdeve.s17.itismob_mc0

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.SavedLayoutBinding

class SavedRecipeAdapter(
    private val data: ArrayList<RecipeModel>
) : RecyclerView.Adapter<SavedRecipeViewHolder>() {

    private var onItemClickListener: ((RecipeModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (RecipeModel) -> Unit) {
        this.onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRecipeViewHolder {
        val binding = SavedLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavedRecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedRecipeViewHolder, position: Int) {
        val recipe = data[position]
        holder.bindData(recipe)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(recipe)
        }
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: ArrayList<RecipeModel>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}
