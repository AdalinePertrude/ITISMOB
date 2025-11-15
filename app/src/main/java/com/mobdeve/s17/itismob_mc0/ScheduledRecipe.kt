package com.mobdeve.s17.itismob_mc0

import java.util.*

data class ScheduledRecipe(
    val recipe: RecipeModel,
    val scheduledDateTime: Date,
    val notificationId: Int = System.currentTimeMillis().toInt() and 0xfffffff,
    val addedTime: Calendar = Calendar.getInstance()
) {
    fun getNotificationTime(): Long {
        val calendar = Calendar.getInstance().apply {
            time = scheduledDateTime
        }
        // Calculate exactly 1 day before the scheduled time
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.timeInMillis
    }

    fun isNotificationScheduled(): Boolean {
        return scheduledDateTime.time > System.currentTimeMillis()
    }

    companion object {
        fun createFromRecipe(
            recipe: RecipeModel,
            scheduledDate: Calendar
        ): ScheduledRecipe {
            return ScheduledRecipe(
                recipe = recipe,
                scheduledDateTime = scheduledDate.time
            )
        }
    }
}