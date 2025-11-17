package com.mobdeve.s17.itismob_mc0

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

    fun interface OnItemListener {
        fun onItemClick(position: Int, dayText: String)
    }

    class CalendarViewHolder (itemView : View, private val onItemListener: OnItemListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
            val dayOfMonth: TextView = itemView.findViewById(R.id.day_of_month_tv)

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemListener.onItemClick(position, dayOfMonth.text.toString())
                }
            }


    }

