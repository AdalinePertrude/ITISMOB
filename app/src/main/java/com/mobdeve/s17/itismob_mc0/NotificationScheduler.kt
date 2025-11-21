package com.mobdeve.s17.itismob_mc0

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.String

class NotificationScheduler(private val context: Context) {

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val REQUEST_CODE_BASE = 1000

        fun testNotification(context: Context, testRecipe : RecipeModel) {
            Log.d(TAG, "🧪 Testing notification system...")

            val testScheduledRecipe = ScheduledRecipe(
                recipe = testRecipe,
                scheduledDateTime = Date(System.currentTimeMillis() + 5000) // 5 seconds from now
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, RecipeNotificationReceiver::class.java).apply {
                putExtra("recipe_id", testRecipe.id)
                putExtra("recipe_label", testRecipe.label)
                putExtra("recipe_image_id", testRecipe.imageId)
                putExtra("notification_id", 9999)
                putExtra("scheduled_time", System.currentTimeMillis())
                putExtra("prep_time", testRecipe.prepTime)
                putExtra("meal_type", "Test Meal")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                9999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule for 1 second from now
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                pendingIntent
            )

            Log.d(TAG, "🧪 Test notification scheduled for 1 second from now")
        }

    }

    fun scheduleRecipeNotification(scheduledRecipe: ScheduledRecipe) {
        val notificationTime = scheduledRecipe.getNotificationTime()
        val currentTime = System.currentTimeMillis()

        // Enhanced logging
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.getDefault())

        Log.d(TAG, "=== SCHEDULING NOTIFICATION ===")
        Log.d(TAG, "Recipe: ${scheduledRecipe.recipe.label}")
        Log.d(TAG, "Current time: ${dateFormat.format(Date(currentTime))}")
        Log.d(TAG, "Scheduled time: ${dateFormat.format(Date(notificationTime))}")
        Log.d(TAG, "Time until notification: ${(notificationTime - currentTime) / 1000 / 60} minutes")
        Log.d(TAG, "Notification ID: ${scheduledRecipe.notificationId}")

        if (notificationTime <= currentTime) {
            Log.w(TAG, "❌ Notification time is in the past - NOT scheduling")
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

            // Schedule the alarm
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

            Log.d(TAG, "✅ Notification scheduled successfully!")
            Log.d(TAG, "⏰ Will trigger at: ${dateFormat.format(Date(notificationTime))}")

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ AlarmManager permission denied", e)
            showPermissionWarning()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error scheduling notification", e)
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

}