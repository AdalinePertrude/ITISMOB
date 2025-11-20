package com.mobdeve.s17.itismob_mc0

import android.app.DatePickerDialog
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
import com.bumptech.glide.Glide
import java.util.Calendar

class ViewRecipeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)
        val calendarAdd: Button = findViewById(R.id.addToCalendarBtn)
        val savedBtn: ImageButton = findViewById(R.id.savedRecBtn)
        val returnPageButton: ImageButton = findViewById(R.id.returnPageBtn)
        val viewCommentsButton: Button = findViewById(R.id.viewCommsBtn)
        var ifClicked= false
        val userId = getSharedPreferences("USER_PREFERENCE", android.content.Context.MODE_PRIVATE).getString("userId", "")!!
        val recipeId = intent.getStringExtra("RECIPE_ID") ?: return
        // Log.d("DEBUG", "id: $recipeId")
        if (recipeId != null) {
            loadRecipeData(recipeId)
        } else {
            // Handle error - no recipe ID provided
            finish()
        }


        savedBtn.setOnClickListener {
            if(ifClicked) {
                savedBtn.setImageResource(R.drawable.ic_saved)
                savedBtn.setBackgroundResource(R.drawable.savedbtn_design)

            }else {
                savedBtn.setImageResource(R.drawable.ic_save)
                savedBtn.setBackgroundResource(R.drawable.savebtn_design)
            }
            ifClicked = !ifClicked
        }
        val starRating: RatingBar = findViewById(R.id.starsRb)

       DatabaseHelper.loadUserRating(recipeId, userId, starRating)

        starRating.setOnRatingBarChangeListener {starRating, rating, fromUser ->
            if (!fromUser) return@setOnRatingBarChangeListener

            val ratingtemp = RatingModel(
                rater = userId,
                rating = rating.toDouble(),
            )

            DatabaseHelper.addOrUpdateRecipeRating(recipeId, ratingtemp) { success ->
                if (success) {
                    Log.d("RATING", "Rating saved")
                    Toast.makeText(this, "You rated $rating stars!", Toast.LENGTH_SHORT).show()
                }
                else Log.e("RATING", "Failed to save rating")
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
                    // Use the function directly with year/month/day
                    DatabaseHelper.addRecipeToCalendar(
                        userid = userId,
                        recipeId = recipeId.toString(),
                        year = selectedYear,
                        month = selectedMonth,
                        day = selectedDay
                    ) { success ->
                        Toast.makeText(
                            this,
                            if (success) "Added to planner!" else "Failed to add",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    val date = "Date Selected: ${selectedDay}/${selectedMonth + 1}/$selectedYear"
                    Toast.makeText(this, date, Toast.LENGTH_SHORT).show()
                },
                year, month, day
            )
            datePickerDialog.show()
        }


        returnPageButton.setOnClickListener {
            finish()
        }
        viewCommentsButton.setOnClickListener {
            val intent = Intent(this, CommentActivity::class.java)
            intent.putExtra("RECIPE_ID",recipeId)
            startActivity(intent)
        }
    }

    private fun loadRecipeData(recipeId: String) {

        DatabaseHelper.searchRecipeByField("id", recipeId) { recipe ->
            runOnUiThread {
                if (recipe != null) {
                    // Update UI with recipe data
                    updateUI(recipe)
                } else {

                }

            }
        }
    }

    private fun updateUI(recipe: RecipeModel) {
        // Update your UI elements with recipe data
        findViewById<TextView>(R.id.recipeNameTv).text = recipe.label

        // Load image with Glide/Picasso
        Glide.with(this)
            .load(recipe.imageId)
            .into(findViewById(R.id.imageView2))
        var ingredientsArray = ArrayList<String>()
        var stepsArray = ArrayList<String>()
        ingredientsArray = recipe.ingredients as ArrayList<String>
        stepsArray = recipe.instructions as ArrayList<String>
        val ingNum = 1
        var stepNum = 1
        val ingredientLayout: LinearLayout = findViewById(R.id.ingredientsLayout)
        val stepLayout: LinearLayout = findViewById(R.id.stepsLayout)
        for(i in ingredientsArray){
            val temp = TextView(this)
            temp.text = i
            ingredientLayout.addView(temp)
        }
        for(i in stepsArray){
            val temp = TextView(this)
            temp.text = stepNum.toString() + ". " + i
            stepLayout.addView(temp)
            stepNum+= 1
        }
    }


}