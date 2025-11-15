package com.mobdeve.s17.itismob_mc0

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class NotificationScheduler(private val context: Context) {

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val REQUEST_CODE_BASE = 1000
    }

    fun scheduleRecipeNotification(scheduledRecipe: ScheduledRecipe) {
        val notificationTime = scheduledRecipe.getNotificationTime()
        val currentTime = System.currentTimeMillis()

        // Format for logging
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val notificationTimeFormatted = dateFormat.format(Date(notificationTime))
        val scheduledTimeFormatted = dateFormat.format(scheduledRecipe.scheduledDateTime)

        Log.d(TAG, "Scheduling notification for: ${scheduledRecipe.recipe.label}")
        Log.d(TAG, "Scheduled meal time: $scheduledTimeFormatted")
        Log.d(TAG, "Notification time (1 day before): $notificationTimeFormatted")

        // Don't schedule if notification time is in the past
        if (notificationTime <= currentTime) {
            Log.w(TAG, "Notification time is in the past for: ${scheduledRecipe.recipe.label}")
            return
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, RecipeNotificationReceiver::class.java).apply {
                putExtra("recipe_id", scheduledRecipe.recipe.id)
                putExtra("recipe_label", scheduledRecipe.recipe.label)
                putExtra("recipe_image_id", scheduledRecipe.recipe.imageId)
                putExtra("notification_id", scheduledRecipe.notificationId)
                putExtra("scheduled_time", scheduledRecipe.scheduledDateTime.time)
                putExtra("prep_time", scheduledRecipe.recipe.prepTime)
                putExtra("meal_type", scheduledRecipe.recipe.mealType.firstOrNull() ?: "Meal")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + scheduledRecipe.notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use exact alarm for precise timing
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
            }

            Log.d(TAG, "✅ Notification scheduled successfully for: ${scheduledRecipe.recipe.label}")
            Log.d(TAG, "⏰ Will notify at: $notificationTimeFormatted")

        } catch (e: SecurityException) {
            Log.e(TAG, "AlarmManager permission denied", e)
            showPermissionWarning()
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling notification", e)
        }
    }

    private fun showPermissionWarning() {
        android.widget.Toast.makeText(
            context,
            "Please enable exact alarm permission for recipe reminders",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    fun cancelRecipeNotification(scheduledRecipe: ScheduledRecipe) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, RecipeNotificationReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + scheduledRecipe.notificationId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
                Log.d(TAG, "✅ Cancelled notification for: ${scheduledRecipe.recipe.label}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification", e)
        }
    }

    fun rescheduleAllNotifications(scheduledRecipes: List<ScheduledRecipe>) {
        Log.d(TAG, "Rescheduling ${scheduledRecipes.size} recipes")
        scheduledRecipes.forEach { scheduledRecipe ->
            if (scheduledRecipe.isNotificationScheduled()) {
                scheduleRecipeNotification(scheduledRecipe)
            }
        }
    }
}