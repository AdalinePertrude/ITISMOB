package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s17.itismob_mc0.databinding.HpRecipeCardLayoutBinding


class HomeAdapter (private val data : ArrayList<RecipeModel>) : Adapter<HomeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {

        val RecipesViewBinding: HpRecipeCardLayoutBinding = HpRecipeCardLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return HomeViewHolder(RecipesViewBinding)
    }

    fun updateData(newDishesList: ArrayList<RecipeModel>) {
        data.clear()
        data.addAll(newDishesList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewRecipeActivity::class.java)
            intent.putExtra("RECIPE_ID", data[position].id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}