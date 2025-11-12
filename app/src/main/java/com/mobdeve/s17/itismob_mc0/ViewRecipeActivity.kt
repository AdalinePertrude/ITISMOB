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
        var ingNum = 1
        var stepNum = 1
        val ingredientsArray = ArrayList<String>()
        ingredientsArray.add("Sugar")
        ingredientsArray.add("Salt")
        ingredientsArray.add("Milk")
        ingredientsArray.add("Eggs")
        val stepsArray = ArrayList<String>()
        stepsArray.add("Step 1")
        stepsArray.add("Step 2")
        val calendarAdd: Button = findViewById(R.id.addToCalendarBtn)
        val savedBtn: ImageButton = findViewById(R.id.savedRecBtn)
        val returnPageButton: ImageButton = findViewById(R.id.returnPageBtn)
        val viewCommentsButton: Button = findViewById(R.id.viewCommsBtn)
        var ifClicked= false

        val recipeId = intent.getStringExtra("RECIPE_ID")
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
        starRating.setOnRatingBarChangeListener {starRating, rating, fromUser ->
            if (fromUser) {
                Toast.makeText(this, "You rated $rating stars!", Toast.LENGTH_SHORT).show()
            }
        }
        val ingredientLayout: LinearLayout = findViewById(R.id.ingredientsLayout)
        for(i in ingredientsArray){
            val temp = TextView(this)
            temp.text = ingNum.toString() + ". " + i
            ingredientLayout.addView(temp)
        }
        val stepLayout: LinearLayout = findViewById(R.id.stepsLayout)
        for(i in stepsArray){
            val temp = TextView(this)
            temp.text = stepNum.toString() + ". " + i
            stepLayout.addView(temp)
            stepNum+= 1
        }
        calendarAdd.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
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

        RecipeDatabaseHelper.searchRecipeByField("id", recipeId) { recipe ->
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
    }


}