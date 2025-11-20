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
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 9) // 9:00 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
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