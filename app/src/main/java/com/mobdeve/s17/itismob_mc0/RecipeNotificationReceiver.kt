package com.mobdeve.s17.itismob_mc0

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class RecipeNotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "RecipeNotificationReceiver"
        private const val CHANNEL_ID = "recipe_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Recipe notification triggered")

        val recipeId = intent.getStringExtra("recipe_id")
        val recipeLabel = intent.getStringExtra("recipe_label") ?: "Unknown Recipe"
        val recipeImageId = intent.getStringExtra("recipe_image_id")
        val notificationId = intent.getIntExtra("notification_id", 0)
        val scheduledTime = intent.getLongExtra("scheduled_time", 0)
        val prepTime = intent.getIntExtra("prep_time", 0)
        val mealType = intent.getStringExtra("meal_type") ?: "meal"

        showRecipeReminderNotification(
            context,
            recipeLabel,
            recipeId,
            notificationId,
            scheduledTime,
            prepTime,
            mealType
        )
    }

    private fun showRecipeReminderNotification(
        context: Context,
        recipeLabel: String,
        recipeId: String?,
        notificationId: Int,
        scheduledTime: Long,
        prepTime: Int,
        mealType: String
    ) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            // Format the scheduled time for the notification
            val dateFormat = SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
            val scheduledTimeFormatted = if (scheduledTime > 0) {
                dateFormat.format(Date(scheduledTime))
            } else {
                "tomorrow"
            }

            // Create prep time message
            val prepTimeMessage = if (prepTime > 0) {
                " (takes ${prepTime}min to prepare)"
            } else {
                ""
            }

            // Create intent to open the app
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("source", "notification")
                putExtra("recipe_id", recipeId)
                putExtra("open_calendar", true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create the notification with recipe details
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("🍳 $mealType Reminder")
                .setContentText("$recipeLabel is tomorrow!")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Your $mealType '$recipeLabel' is scheduled for $scheduledTimeFormatted.$prepTimeMessage\n\nDon't forget to check your ingredients! 🛒"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())

            // Add action buttons
            val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
                putExtra("notification_id", notificationId)
            }

            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1000,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                R.drawable.ic_baseline_check_24,
                "Got it!",
                dismissPendingIntent
            )

            // Add "View Calendar" action
            val calendarIntent = Intent(context, CalendarActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val calendarPendingIntent = PendingIntent.getActivity(
                context,
                notificationId + 2000,
                calendarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                R.drawable.ic_baseline_calendar_today_24,
                "View Calendar",
                calendarPendingIntent
            )

            // Ensure notification channel exists
            createNotificationChannel(context)

            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "✅ Recipe reminder shown for: $recipeLabel")

        } catch (e: Exception) {
            Log.e(TAG, "Error showing recipe reminder notification", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Recipe Reminders",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your scheduled recipes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}