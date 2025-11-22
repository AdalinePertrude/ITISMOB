package com.mobdeve.s17.itismob_mc0

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AddRecipeActivity : ComponentActivity() {

    private lateinit var recipeImageView: ImageView
    private lateinit var selectImageBtn: ImageButton
    private lateinit var recipeNameEt: EditText
    private lateinit var cuisineSpinner: Spinner
    private lateinit var prepTimeEt: EditText
    private lateinit var servingEt: EditText
    private lateinit var descriptionEt: EditText
    private lateinit var ingredientContainer: LinearLayout
    private lateinit var addIngredientBtn: ImageButton
    private lateinit var totalCalsValue: TextView
    private lateinit var stepContainer: LinearLayout
    private lateinit var addStepBtn: ImageButton
    private lateinit var publishBtn: Button

    private var imageUri: Uri? = null
    private val ingredientRows = mutableListOf<IngredientRow>()
    private val stepRows = mutableListOf<StepRow>()


    private var totalCalories = 0.0

    private val USER_PREFERENCE = "USER_PREFERENCE"

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                recipeImageView.setImageURI(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.publish_recipe)

        bindViews()
        setupCuisineSpinner()
        setupIngredientButtons()
        setupStepButtons()
        setupImageButton()
        setupPublishButton()
    }

    private fun bindViews() {
        recipeImageView = findViewById(R.id.recipeImageView)
        selectImageBtn = findViewById(R.id.selectImageBtn)
        recipeNameEt = findViewById(R.id.recipeNameEt)
        cuisineSpinner = findViewById(R.id.cuisineSpinner)
        prepTimeEt = findViewById(R.id.prepTimeEt)
        servingEt = findViewById(R.id.servingEt)
        descriptionEt = findViewById(R.id.descriptionEt)
        ingredientContainer = findViewById(R.id.ingredientInputContainer)
        addIngredientBtn = findViewById(R.id.addIngredientBtn)
        totalCalsValue = findViewById(R.id.totalCalsValue)
        stepContainer = findViewById(R.id.stepInputContainer)
        addStepBtn = findViewById(R.id.addStepBtn)
        publishBtn = findViewById(R.id.publishBtn)
    }

    private fun setupCuisineSpinner() {
        val cuisines = listOf("Select Cuisine", "Italian", "Chinese", "American", "Indian", "Other")
        cuisineSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cuisines)
    }


    private fun setupIngredientButtons() {
        val firstRow = ingredientContainer.findViewById<LinearLayout>(R.id.ingredientRow1)
        val firstAuto = firstRow.findViewById<AutoCompleteTextView>(R.id.ingredientAutoComplete1)
        val firstGrams = firstRow.findViewById<EditText>(R.id.ingredientGrams1)
        val firstCals = firstRow.findViewById<EditText>(R.id.ingredientCals1)
        val firstRemove = firstRow.findViewById<ImageButton>(R.id.removeIngredientBtn1)

        val rowObj = IngredientRow(firstAuto, firstGrams, firstCals)
        ingredientRows.add(rowObj)



        firstGrams.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateCalories(rowObj)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        firstRemove.setOnClickListener {
            ingredientContainer.removeView(firstRow)
            ingredientRows.remove(rowObj)
            updateTotalCalories()
        }

        addIngredientBtn.setOnClickListener { addNewIngredientRow() }
    }

    private fun addNewIngredientRow() {
        val newRow = LinearLayout(this)
        newRow.orientation = LinearLayout.HORIZONTAL

        val auto = AutoCompleteTextView(this)
        auto.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        auto.hint = "Ingredient"

        val grams = EditText(this)
        grams.layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT)
        grams.hint = "g"
        grams.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL


        val cals = EditText(this)
        cals.layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT)
        cals.hint = "0 kcal"
        cals.inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

        val remove = ImageButton(this)
        remove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        remove.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        newRow.addView(auto)
        newRow.addView(grams)
        newRow.addView(cals)
        newRow.addView(remove)
        ingredientContainer.addView(newRow)

        val rowObj = IngredientRow(auto, grams, cals)
        ingredientRows.add(rowObj)

        grams.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateCalories(rowObj)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        remove.setOnClickListener {
            ingredientContainer.removeView(newRow)
            ingredientRows.remove(rowObj)
            updateTotalCalories()
        }
    }

    private fun calculateCalories(row: IngredientRow) {
        updateTotalCalories()
    }

    private fun updateTotalCalories() {
        totalCalories = ingredientRows.sumOf {
            it.calories.text.toString().replace(" kcal", "").toDoubleOrNull() ?: 0.0
        }
        totalCalsValue.text = "${totalCalories.toInt()} kcal"
    }


    private fun setupStepButtons() {
        val firstRow = stepContainer.getChildAt(0) as LinearLayout
        val numberView = firstRow.getChildAt(0) as TextView
        val stepEt = firstRow.findViewById<EditText>(R.id.stepEt1)

        stepRows.add(StepRow(stepEt, numberView))
        updateStepNumbers()

        addStepBtn.setOnClickListener { addNewStepRow() }
    }

    private fun addNewStepRow() {
        val newRow = LinearLayout(this)
        newRow.orientation = LinearLayout.HORIZONTAL
        newRow.setPadding(0, 10, 0, 10)

        val numberView = TextView(this)
        numberView.textSize = 15f
        numberView.setPadding(0, 0, 16, 0)

        val stepEt = EditText(this)
        stepEt.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        stepEt.hint = "Describe step"

        val removeBtn = ImageButton(this)
        removeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        removeBtn.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        newRow.addView(numberView)
        newRow.addView(stepEt)
        newRow.addView(removeBtn)
        stepContainer.addView(newRow)

        val stepObj = StepRow(stepEt, numberView)
        stepRows.add(stepObj)

        updateStepNumbers()

        removeBtn.setOnClickListener {
            stepContainer.removeView(newRow)
            stepRows.remove(stepObj)
            updateStepNumbers()
        }
    }

    private fun updateStepNumbers() {
        stepRows.forEachIndexed { index, row ->
            row.numberView.text = "Step ${index + 1}"
        }
    }

    private fun validateSteps(): Boolean {
        for (i in stepRows.indices) {
            val stepText = stepRows[i].step.text.toString().trim()
            if (stepText.isEmpty()) {
                Toast.makeText(this, "Please complete Step ${i + 1}", Toast.LENGTH_SHORT).show()
                stepRows[i].step.requestFocus()
                return false
            }
        }
        return true
    }

    private fun setupImageButton() {
        selectImageBtn.setOnClickListener { pickImageLauncher.launch("image/*") }
    }

    private fun setupPublishButton() {
        publishBtn.setOnClickListener { publishRecipe() }
    }


    private fun validateIngredients(): Boolean {
        if (ingredientRows.isEmpty()) {
            Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show()
            return false
        }

        ingredientRows.forEachIndexed { index, row ->
            val name = row.name.text.toString().trim()
            val grams = row.grams.text.toString().trim()
            if (name.isEmpty() || grams.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please complete Ingredient ${index + 1}",
                    Toast.LENGTH_SHORT
                ).show()
                row.name.requestFocus()
                return false
            }
        }

        return true
    }
    private fun publishRecipe() {
        if (!validateIngredients()) return
        if (!validateSteps()) return

        val id = UUID.randomUUID().toString()
        val name = recipeNameEt.text.toString()
        if (name.isBlank()) {
            Toast.makeText(this, "Recipe name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val cuisine = listOf(cuisineSpinner.selectedItem.toString())
        val prepTime = prepTimeEt.text.toString().toIntOrNull() ?: 0
        val serving = servingEt.text.toString().toIntOrNull() ?: 1
        val description = descriptionEt.text.toString()

        val ingredients = ingredientRows.map { "${it.name.text} - ${it.grams.text}g" }
        val steps = stepRows.map { it.step.text.toString() }

        val imageUrl = imageUri?.toString() ?: ""

        // Get user info from SharedPreferences
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val userName = sp.getString("userName", "") ?: ""
        val userId = sp.getString("userId", "") ?: ""

        val recipe = RecipeModel(
            id = id,
            author = userName,
            calories = totalCalories.toInt(),
            cautions = emptyList(),
            createdAt = Date().toString(),
            cuisineType = cuisine,
            dietLabels = emptyList(),
            dishType = emptyList(),
            healthLabels = emptyList(),
            imageId = imageUrl,
            ingredients = ingredients,
            instructions = steps,
            label = name,
            mealType = emptyList(),
            prepTime = prepTime,
            rating = 0.0,
            serving = serving,
            isPublished = true,
            description = description
        )

        DatabaseHelper.addRecipe(recipe, userId ) { success ->
            if (success) {
                Toast.makeText(this, "Recipe published!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to publish recipe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


data class IngredientRow(val name: AutoCompleteTextView, val grams: EditText, val calories: EditText)
data class StepRow(val step: EditText, val numberView: TextView)

