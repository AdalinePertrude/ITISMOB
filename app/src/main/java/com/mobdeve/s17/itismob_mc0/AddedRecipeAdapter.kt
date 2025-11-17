package com.mobdeve.s17.itismob_mc0

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.AddedRecipeToCalendarBinding

class AddedRecipeAdapter(private val data: ArrayList<RecipeModel>) : RecyclerView.Adapter<AddedRecipeViewHolder>() {
    var onItemLongClickListener: ((Int, RecipeModel, View) -> Unit)? = null
    var onItemClickListener: ((Int, RecipeModel) -> Unit)? = null
    var onDragStarted: ((Int, View) -> Unit)? = null
    var onDragEnded: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedRecipeViewHolder {
        val addRecipeToCalViewBinding: AddedRecipeToCalendarBinding = AddedRecipeToCalendarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return AddedRecipeViewHolder(addRecipeToCalViewBinding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: AddedRecipeViewHolder, position: Int) {
        // Check if position is valid before binding
        if (position < 0 || position >= data.size) {
            return
        }

        val dish = data[position]
        holder.bindAddedDishModel(dish)

        holder.itemView.setOnClickListener {
            if (position in 0 until data.size) {
                onItemClickListener?.invoke(position, data[position])
            }
        }

        // Set up long click listener for dragging
        holder.itemView.setOnLongClickListener {
            // Check if position is valid
            if (position in 0 until data.size) {
                // Start shake animation
                val shakeAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.shake_animation)
                holder.itemView.startAnimation(shakeAnimation)

                // Start drag operation immediately on long click
                startDragForRecipe(holder.itemView, position)

                // Notify about long click
                onItemLongClickListener?.invoke(position, data[position], holder.itemView)
                true
            } else {
                false
            }
        }
        // Set drag listener to handle when drag ends
        holder.itemView.setOnDragListener { v, event ->
            when (event.action) {
                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    // Stop shaking when drag ends
                    stopShakeAnimation(v)
                    onDragEnded?.invoke(position)
                    true
                }
                else -> false
            }
        }
    }

    private fun startDragForRecipe(view: View, position: Int) {
        // Create shadow builder
        val shadowBuilder = View.DragShadowBuilder(view)

        // Create clip data with the position information
        val item = "recipe::$position"
        val clipData = android.content.ClipData.newPlainText("", item)

        onDragStarted?.invoke(position, view)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(clipData, shadowBuilder, null, 0)
        } else {
            view.startDrag(clipData, shadowBuilder, null, 0)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // helper function to stop shake animation
    private fun stopShakeAnimation(view: View) {
        view.clearAnimation()
    }
}