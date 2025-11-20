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
        try {
            Log.d(TAG, "Recipe notification triggered")

            val recipeLabel = intent.getStringExtra("recipe_label") ?: "Unknown Recipe"
            val recipeId = intent.getStringExtra("recipe_id")
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

        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical error in NotificationReceiver", e)
            // Don't crash the system UI
        }
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

            // Create intent to open the app
            val appIntent = Intent(context, ViewRecipeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("source", "notification")
                recipeId?.let { putExtra("RECIPE_ID", it) }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🍳 Meal Reminder")
                .setContentText("Time to prepare your dish!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Ensure notification channel exists
            createNotificationChannel(context)

            // Try to notify
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "✅ Simple notification shown for: $recipeLabel")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error showing notification", e)
            // Fallback: try with even simpler notification
            showFallbackNotification(context, recipeLabel, notificationId)
        }
    }

    // Fallback method with minimal configuration
    private fun showFallbackNotification(context: Context, recipeLabel: String, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Recipe Reminder")
                .setContentText(recipeLabel)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)

            notificationManager.notify(notificationId + 1000, builder.build())
            Log.d(TAG, "✅ Fallback notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Even fallback notification failed", e)
        }
    }


    private fun createNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            // Check if channel already exists to avoid recreation
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                try {
                    val channel = android.app.NotificationChannel(
                        CHANNEL_ID,
                        "Recipe Reminders",
                        android.app.NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Reminders for your scheduled recipes"
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 250, 250, 250) // Simpler pattern
                        setShowBadge(true)
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        // Add sound
                        setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
                    }
                    notificationManager.createNotificationChannel(channel)
                    Log.d(TAG, "✅ Notification channel created successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating notification channel", e)
                }
            }
        }
    }
}