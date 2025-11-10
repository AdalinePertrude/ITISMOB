package com.mobdeve.s17.itismob_mc0

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.AddedRecipeToCalendarBinding

class AddedRecipeAdapter(private val data: ArrayList<DishesModel>) : RecyclerView.Adapter<AddedRecipeViewHolder>() {

    var onItemLongClickListener: ((Int, DishesModel) -> Unit)? = null
    var onItemClickListener: ((Int, DishesModel) -> Unit)? = null
    var onDragStarted: ((Int) -> Unit)? = null
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

        // Set click listeners
        holder.itemView.setOnClickListener {
            if (position in 0 until data.size) {
                onItemClickListener?.invoke(position, data[position])
            }
        }

        // Set up touch listener for dragging
        holder.itemView.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_MOVE) {
                if (position in 0 until data.size) {
                    // Start drag when user moves finger after long press
                    onDragStarted?.invoke(position)

                    // Create shadow builder for drag
                    val shadowBuilder = View.DragShadowBuilder(v)
                    v.startDragAndDrop(null, shadowBuilder, null, 0)
                    return@setOnTouchListener true
                }
            }
            false
        }

        holder.itemView.setOnLongClickListener {
            // Check if position is still valid
            if (position in 0 until data.size) {
                // Start shake animation
                val shakeAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.shake_animation)
                holder.itemView.startAnimation(shakeAnimation)

                onItemLongClickListener?.invoke(position, data[position])
                true
            } else {
                false
            }
        }

        // Set drag listener to handle when drag ends
        holder.itemView.setOnDragListener { v, event ->
            when (event.action) {
                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    // Stop shaking when drag ends (whether dropped or cancelled)
                    stopShakeAnimation(v)
                    onDragEnded?.invoke(position)
                    true
                }
                else -> false
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // Helper function to stop shake animation
    private fun stopShakeAnimation(view: View) {
        view.clearAnimation()
    }

    // Add this method to safely remove items
    fun removeItem(position: Int) {
        if (position in 0 until data.size) {
            data.removeAt(position)
            notifyItemRemoved(position)
            // Notify about range change to update remaining items
            notifyItemRangeChanged(position, data.size - position)
        }
    }
}