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
import com.mobdeve.s17.itismob_mc0.databinding.ActivityViewRecipeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewRecipeActivity : ComponentActivity() {
    private lateinit var binding: ActivityViewRecipeBinding
    private lateinit var recipeRepository: RecipeRepository
    private var currentRecipe: RecipeModel? = null
    private var ifClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeRepository = RecipeRepository(this)

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

        val recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId != null) {
            loadRecipeData(recipeId)
        } else {
            finish()
        }

        savedBtn.setOnClickListener {
            currentRecipe?.let { recipe ->
                if (!ifClicked) {
                    // Save recipe
                    saveOffline(recipe)
                } else {
                    // Unsave recipe
                    unsaveOffline(recipe)
                }
            } ?: run {
                Toast.makeText(this, "Recipe data not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }

        val starRating: RatingBar = findViewById(R.id.starsRb)
        starRating.setOnRatingBarChangeListener { starRating, rating, fromUser ->
            if (fromUser) {
                Toast.makeText(this, "You rated $rating stars!", Toast.LENGTH_SHORT).show()
            }
        }

        val ingredientLayout: LinearLayout = findViewById(R.id.ingredientsLayout)
        for (i in ingredientsArray) {
            val temp = TextView(this)
            temp.text = ingNum.toString() + ". " + i
            ingredientLayout.addView(temp)
        }

        val stepLayout: LinearLayout = findViewById(R.id.stepsLayout)
        for (i in stepsArray) {
            val temp = TextView(this)
            temp.text = stepNum.toString() + ". " + i
            stepLayout.addView(temp)
            stepNum += 1
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
                        Toast.makeText(
                            this,
                            if (success) "Added to planner!" else "Failed to add",
                            Toast.LENGTH_SHORT
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
            intent.putExtra("RECIPE_ID", recipeId)
            startActivity(intent)
        }
    }

    private fun loadRecipeData(recipeId: String) {
        // check if recipe is already saved locally
        val savedRecipe = recipeRepository.getRecipeWithImage(recipeId)
        if (savedRecipe != null) {
            // Recipe exists in local database
            currentRecipe = savedRecipe
            runOnUiThread {
                updateUI(savedRecipe)
                updateSaveButtonUI(savedRecipe.isSaved)
                ifClicked = savedRecipe.isSaved
            }
        } else {
            // Fetch from Firebase
            DatabaseHelper.searchRecipeByField("id", recipeId) { recipe ->
                runOnUiThread {
                    if (recipe != null) {
                        currentRecipe = recipe
                        updateUI(recipe)
                        // Check if recipe is saved in the manager
                        val isSavedInManager = SavedRecipeManager.isRecipeSaved(recipeId)
                        updateSaveButtonUI(isSavedInManager)
                        ifClicked = isSavedInManager
                    } else {
                        Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun updateUI(recipe: RecipeModel) {
        // Load image with Glide
        Glide.with(this)
            .load(recipe.imageId)
            .into(findViewById(R.id.imageView2))

        findViewById<TextView>(R.id.recipeNameTv).text = recipe.label
        findViewById<TextView>(R.id.authorTv)?.text = "By ${recipe.author}"
        findViewById<TextView>(R.id.prepTv)?.text = "${recipe.prepTime} mins"
        findViewById<TextView>(R.id.servingTv)?.text = "Serves ${recipe.serving}"
        findViewById<TextView>(R.id.caloriesTv)?.text = "${recipe.calories} calories"
        findViewById<TextView>(R.id.ratingTv)?.text = recipe.rating.toString()
    }

    private fun saveOffline(recipe: RecipeModel) {
        binding.savedRecBtn.isEnabled = false

        recipeRepository.saveRecipeWithImage(recipe) { success ->
            binding.savedRecBtn.isEnabled = true

            if (success) {
                Log.d("SaveOperation", "Recipe saved offline: ${recipe.label}")
                Toast.makeText(this, "${recipe.label} saved offline", Toast.LENGTH_SHORT).show()
                SavedRecipeManager.addSavedRecipe(recipe.id)
                ifClicked = true
                updateSaveButtonUI(true)
            } else {
                Log.e("SaveOperation", "Failed to save recipe: ${recipe.label}")
                Toast.makeText(this, "Failed to save ${recipe.label}", Toast.LENGTH_SHORT).show()
                ifClicked = false
                updateSaveButtonUI(false)
            }
        }
    }

    private fun unsaveOffline(recipe: RecipeModel) {
        Log.d("DeleteDebug", "=== STARTING DELETE ===")

        recipeRepository.getRecipeWithImage(recipe.id)?.let { freshRecipe ->
            val success = recipeRepository.deleteRecipeWithImage(freshRecipe)
            if (success) {
                Log.d("SaveOperation", "Recipe removed from offline: ${freshRecipe.label}")
                Toast.makeText(this, "${freshRecipe.label} removed from offline", Toast.LENGTH_SHORT).show()
                SavedRecipeManager.removeSavedRecipe(recipe.id)
                ifClicked = false
                updateSaveButtonUI(false)
            } else {
                Log.e("SaveOperation", "Failed to remove recipe: ${freshRecipe.label}")
                Toast.makeText(this, "Failed to remove ${freshRecipe.label}", Toast.LENGTH_SHORT).show()
                ifClicked = true
                updateSaveButtonUI(true)
            }
        } ?: run {
            Log.e("DeleteDebug", "Recipe not found in database: ${recipe.id}")
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            ifClicked = true
            updateSaveButtonUI(true)
        }
    }

    private fun updateSaveButtonUI(isSaved: Boolean) {
        if (isSaved) {
            binding.savedRecBtn.setBackgroundResource(R.drawable.savedbtn_design)
            binding.savedRecBtn.setImageResource(R.drawable.ic_saved)
        } else {
            binding.savedRecBtn.setBackgroundResource(R.drawable.savebtn_design)
            binding.savedRecBtn.setImageResource(R.drawable.ic_save)
        }
    }
}