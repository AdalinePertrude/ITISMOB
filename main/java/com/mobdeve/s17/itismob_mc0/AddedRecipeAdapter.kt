package com.mobdeve.s17.itismob_mc0

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.mobdeve.s17.itismob_mc0.databinding.AddedRecipeToCalendarBinding


class AddedRecipeAdapter (private val data: ArrayList<DishesModel>) : Adapter<AddedRecipeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedRecipeViewHolder {

        val addRecipeToCalViewBinding: AddedRecipeToCalendarBinding = AddedRecipeToCalendarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return AddedRecipeViewHolder(addRecipeToCalViewBinding)
    }

    override fun onBindViewHolder(holder: AddedRecipeViewHolder, position: Int) {
        holder.bindAddedDishModel(data[position])
    }

    override fun getItemCount(): Int {
       return data.size
    }

}