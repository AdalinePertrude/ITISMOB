package com.mobdeve.s17.itismob_mc0

import android.app.DatePickerDialog
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewRecipeActivity : ComponentActivity() {
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var currentRecipe: RecipeModel
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        // Set up back button handling with OnBackPressedDispatcher
        setupBackNavigation()

        val calendarAdd: Button = findViewById(R.id.addToCalendarBtn)
        val savedBtn: ImageButton = findViewById(R.id.savedRecBtn)
        val returnPageButton: ImageButton = findViewById(R.id.returnPageBtn)
        val viewCommentsButton: Button = findViewById(R.id.viewCommsBtn)
        var ifClicked = false
        val userId = getSharedPreferences("USER_PREFERENCE", android.content.Context.MODE_PRIVATE).getString("userId", "")!!
        val recipeId = intent.getStringExtra("RECIPE_ID") ?: return

        if (recipeId != null) {
            loadRecipeData(recipeId)
        } else {
            finish()
        }

        setupNotificationScheduling()

        // Update return button to handle different scenarios
        returnPageButton.setOnClickListener {
            handleBackNavigation()
        }

        savedBtn.setOnClickListener {
            if(ifClicked) {
                savedBtn.setImageResource(R.drawable.ic_saved)
                savedBtn.setBackgroundResource(R.drawable.savedbtn_design)
            } else {
                savedBtn.setImageResource(R.drawable.ic_save)
                savedBtn.setBackgroundResource(R.drawable.savebtn_design)
            }
            ifClicked = !ifClicked
        }

        val starRating: RatingBar = findViewById(R.id.starsRb)
        DatabaseHelper.loadUserRating(recipeId, userId, starRating)

        starRating.setOnRatingBarChangeListener { starRating, rating, fromUser ->
            if (!fromUser) return@setOnRatingBarChangeListener

            val ratingtemp = RatingModel(
                rater = userId,
                rating = rating.toDouble(),
            )

            DatabaseHelper.addOrUpdateRecipeRating(recipeId, ratingtemp) { success ->
                if (success) {
                    Log.d("RATING", "Rating saved")
                    Toast.makeText(this, "You rated $rating stars!", Toast.LENGTH_SHORT).show()
                } else Log.e("RATING", "Failed to save rating")
            }
        }

        calendarAdd.setOnClickListener {
            val sp = getSharedPreferences("USER_PREFERENCE", android.content.Context.MODE_PRIVATE)
            val userId = sp.getString("userId", null) ?: return@setOnClickListener

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    DatabaseHelper.addRecipeToCalendar(
                        userid = userId,
                        recipeId = recipeId.toString(),
                        year = selectedYear,
                        month = selectedMonth,
                        day = selectedDay
                    ) { success ->
                        if (success) {
                            Toast.makeText(this, "Added to planner!", Toast.LENGTH_SHORT).show()

                            if (::currentRecipe.isInitialized) {
                                scheduleNotificationForRecipe(currentRecipe, selectedYear, selectedMonth, selectedDay)
                            }
                        } else {
                            Toast.makeText(this, "Failed to add to planner", Toast.LENGTH_SHORT).show()
                        }
                    }

                    val date = "Date Selected: ${selectedDay}/${selectedMonth + 1}/$selectedYear"
                    Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        viewCommentsButton.setOnClickListener {
            val intent = Intent(this, CommentActivity::class.java)
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }
    }

    private fun setupBackNavigation() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackNavigation()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun handleBackNavigation() {
        if (isTaskRoot) {
            // If this is the root activity (launched from notification), go to Home
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } else {
            // If there's a parent activity, use normal back behavior
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun loadRecipeData(recipeId: String) {
        DatabaseHelper.searchRecipeByField("id", recipeId) { recipe ->
            runOnUiThread {
                if (recipe != null) {
                    currentRecipe = recipe
                    updateUI(recipe)
                } else {
                    Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(recipe: RecipeModel) {
        // Update your UI elements with recipe data
        findViewById<TextView>(R.id.recipeNameTv).text = recipe.label

        // Load image with Glide
        Glide.with(this)
            .load(recipe.imageId)
            .into(findViewById(R.id.imageView2))

        // Safely handle ingredients and instructions
        val ingredientsArray = recipe.ingredients?.let {
            if (it is ArrayList<*>) it as ArrayList<String> else ArrayList(it)
        } ?: ArrayList()

        val stepsArray = recipe.instructions?.let {
            if (it is ArrayList<*>) it as ArrayList<String> else ArrayList(it)
        } ?: ArrayList()

        val ingredientLayout: LinearLayout = findViewById(R.id.ingredientsLayout)
        val stepLayout: LinearLayout = findViewById(R.id.stepsLayout)

        // Clear existing views to avoid duplicates
        ingredientLayout.removeAllViews()
        stepLayout.removeAllViews()

        // Add ingredients
        for (ingredient in ingredientsArray) {
            val temp = TextView(this)
            temp.text = ingredient
            temp.setTextSize(16f)
            temp.setPadding(0, 8, 0, 8)
            ingredientLayout.addView(temp)
        }

        // Add steps with numbering
        var stepNum = 1
        for (step in stepsArray) {
            val temp = TextView(this)
            temp.text = "$stepNum. $step"
            temp.setTextSize(16f)
            temp.setPadding(0, 8, 0, 8)
            stepLayout.addView(temp)
            stepNum += 1
        }

        // Also update other recipe information
        findViewById<TextView>(R.id.authorTv)?.text = "By ${recipe.author}"
        findViewById<TextView>(R.id.prepTv)?.text = "${recipe.prepTime} mins"
        findViewById<TextView>(R.id.servingTv)?.text = "Serves ${recipe.serving}"
        findViewById<TextView>(R.id.caloriesTv)?.text = "${recipe.calories} calories"
        findViewById<TextView>(R.id.ratingTv)?.text = recipe.rating.toString()
    }

    private fun scheduleNotificationForRecipe(recipe: RecipeModel, year: Int, month: Int, day: Int) {
        try {
            val scheduledCalendar = Calendar.getInstance().apply {
                set(year, month, day, 9, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val scheduledRecipe = ScheduledRecipe(
                recipe = recipe,
                scheduledDateTime = scheduledCalendar.time
            )

            notificationScheduler.scheduleRecipeNotification(scheduledRecipe)

            val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            Log.d("ViewRecipeActivity", "✅ Notification scheduled for: ${dateFormat.format(scheduledCalendar.time)}")

        } catch (e: Exception) {
            Log.e("ViewRecipeActivity", "❌ Error scheduling notification", e)
            Toast.makeText(this, "Error scheduling reminder", Toast.LENGTH_SHORT).show()
        }
        NotificationScheduler.testNotification(this, recipe)
    }

    private fun setupNotificationScheduling() {
        Toast.makeText(this, "✅ Recipe reminders enabled!", Toast.LENGTH_SHORT).show()
        notificationScheduler = NotificationScheduler(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the callback to prevent memory leaks
        backPressedCallback.remove()
    }
}