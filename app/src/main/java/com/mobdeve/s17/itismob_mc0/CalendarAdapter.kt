package com.mobdeve.s17.itismob_mc0

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class CalendarAdapter( private val daysOfMonth: ArrayList<String>,
                       private val onItemListener: OnItemListener,
                       private val currentMonth: Calendar, // Add current month reference
                       private val currentDate: Calendar = Calendar.getInstance()
) : RecyclerView.Adapter<CalendarViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.calendar_cell, parent, false)
        return CalendarViewHolder(view, onItemListener)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = daysOfMonth[position]
        holder.dayOfMonth.text = day

        // Check if this date is in the past
        val isPastDate = isPastDate(day, position)

        if (isPastDate) {
            // Make past dates look disabled
            holder.dayOfMonth.setTextColor(Color.GRAY)
            holder.itemView.isEnabled = false
            holder.itemView.alpha = 0.5f
        } else {
            // Normal dates
            holder.dayOfMonth.setTextColor(Color.BLACK)
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1f
        }

        holder.itemView.setOnClickListener {
            if (!isPastDate && day.isNotEmpty()) {
                onItemListener.onItemClick(position, day)
            }
        }
    }

    private fun isPastDate(dayText: String, position: Int): Boolean {
        if (dayText.isEmpty()) return false

        try {
            // Create calendar instance for this cell's date
            val cellDate = currentMonth.clone() as Calendar
            cellDate.set(Calendar.DAY_OF_MONTH, dayText.toInt())

            // Reset time parts for accurate date comparison
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            cellDate.set(Calendar.HOUR_OF_DAY, 0)
            cellDate.set(Calendar.MINUTE, 0)
            cellDate.set(Calendar.SECOND, 0)
            cellDate.set(Calendar.MILLISECOND, 0)

            // Return true if cell date is before today
            return cellDate.before(today)

        } catch (e: Exception) {
            Log.e("CalendarAdapter", "Error checking past date", e)
            return false
        }
    }
    override fun getItemCount(): Int {
        return daysOfMonth.size
    }
}