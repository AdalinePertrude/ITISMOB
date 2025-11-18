package com.mobdeve.s17.itismob_mc0

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import com.google.firebase.auth.FirebaseAuth
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
    private var ingredientCount = 1
    private var stepCount = 1
    private val ingredientRows = mutableListOf<IngredientRow>()
    private val stepRows = mutableListOf<StepRow>()
    private val edamamHelper = EdamamNutritionHelper()
    private var totalCalories = 0.0

    private val firestore = FirebaseFirestore.getInstance()

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

    /*** INGREDIENTS ***/
    private fun setupIngredientButtons() {
        val firstRow = ingredientContainer.findViewById<LinearLayout>(R.id.ingredientRow1)
        val firstAuto = firstRow.findViewById<AutoCompleteTextView>(R.id.ingredientAutoComplete1)
        val firstGrams = firstRow.findViewById<EditText>(R.id.ingredientGrams1)
        val firstCals = firstRow.findViewById<TextView>(R.id.ingredientCals1)
        val firstRemove = firstRow.findViewById<ImageButton>(R.id.removeIngredientBtn1)

        val rowObj = IngredientRow(firstAuto, firstGrams, firstCals)
        ingredientRows.add(rowObj)

        setupAutoCompleteDynamic(firstAuto)

        firstGrams.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateCalories(rowObj) }
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
        ingredientCount++
        val firstRow = ingredientContainer.findViewById<LinearLayout>(R.id.ingredientRow1)
        val newRow = LinearLayout(this)
        newRow.orientation = LinearLayout.HORIZONTAL
        newRow.gravity = firstRow.gravity
        newRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val auto = AutoCompleteTextView(this)
        auto.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        auto.hint = "Ingredient"
        auto.setPadding(8, 8, 8, 8)

        val grams = EditText(this)
        grams.layoutParams = LinearLayout.LayoutParams(150, LinearLayout.LayoutParams.WRAP_CONTENT)
        grams.hint = "g"
        grams.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        grams.gravity = android.view.Gravity.CENTER

        val cals = TextView(this)
        cals.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cals.text = "0 kcal"
        cals.setPadding(8, 0, 8, 0)

        val remove = ImageButton(this)
        remove.layoutParams = LinearLayout.LayoutParams(80, 80)
        remove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        remove.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        newRow.addView(auto)
        newRow.addView(grams)
        newRow.addView(cals)
        newRow.addView(remove)
        ingredientContainer.addView(newRow)

        val rowObj = IngredientRow(auto, grams, cals)
        ingredientRows.add(rowObj)

        setupAutoCompleteDynamic(auto)

        grams.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { calculateCalories(rowObj) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        remove.setOnClickListener {
            ingredientContainer.removeView(newRow)
            ingredientRows.remove(rowObj)
            updateTotalCalories()
        }
    }

    private fun setupAutoCompleteDynamic(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    fetchIngredientSuggestions(query) { suggestions ->
                        val adapter = ArrayAdapter(
                            this@AddRecipeActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            suggestions
                        )
                        autoCompleteTextView.setAdapter(adapter)
                        autoCompleteTextView.showDropDown()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchIngredientSuggestions(query: String, onComplete: (List<String>) -> Unit) {
        firestore.collection("ingredients")
            .orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(10)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.getString("name") }
                onComplete(list)
            }
            .addOnFailureListener { onComplete(emptyList()) }
    }

    private fun calculateCalories(row: IngredientRow) {
        val name = row.name.text.toString()
        val gramsStr = row.grams.text.toString()
        val grams = gramsStr.toDoubleOrNull() ?: 0.0
        if (name.isEmpty() || grams <= 0) {
            row.calories.text = "0 kcal"
            updateTotalCalories()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cal = edamamHelper.getCalories(name, grams)
                withContext(Dispatchers.Main) {
                    row.calories.text = "${cal.toInt()} kcal"
                    updateTotalCalories()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun updateTotalCalories() {
        totalCalories = ingredientRows.sumOf { it.calories.text.toString().replace(" kcal", "").toDoubleOrNull() ?: 0.0 }
        totalCalsValue.text = "${totalCalories.toInt()} kcal"
    }

    /*** STEPS ***/
    private fun setupStepButtons() {
        val firstStepRow = stepContainer.getChildAt(0) as LinearLayout
        val firstStepEt = firstStepRow.findViewById<EditText>(R.id.stepEt1)
        val firstStepObj = StepRow(firstStepEt)
        stepRows.add(firstStepObj)

        addStepBtn.setOnClickListener { addNewStepRow() }
    }

    private fun addNewStepRow() {
        stepCount++
        val firstStepRow = stepContainer.getChildAt(0) as LinearLayout

        val newRow = LinearLayout(this)
        newRow.orientation = LinearLayout.HORIZONTAL
        newRow.gravity = firstStepRow.gravity
        newRow.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val stepEt = EditText(this)
        stepEt.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        stepEt.hint = "Describe step"
        stepEt.setPadding(8, 8, 8, 8)

        val removeBtn = ImageButton(this)
        removeBtn.layoutParams = LinearLayout.LayoutParams(80, 80)
        removeBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        removeBtn.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        newRow.addView(stepEt)
        newRow.addView(removeBtn)
        stepContainer.addView(newRow)

        val stepObj = StepRow(stepEt)
        stepRows.add(stepObj)

        removeBtn.setOnClickListener {
            stepContainer.removeView(newRow)
            stepRows.remove(stepObj)
        }
    }

    private fun setupImageButton() {
        selectImageBtn.setOnClickListener { pickImageLauncher.launch("image/*") }
    }

    private fun setupPublishButton() {
        publishBtn.setOnClickListener { publishRecipe() }
    }

    private fun publishRecipe() {
        val id = UUID.randomUUID().toString()
        val name = recipeNameEt.text.toString()
        val cuisine = listOf(cuisineSpinner.selectedItem.toString())
        val prepTime = prepTimeEt.text.toString().toIntOrNull() ?: 0
        val serving = servingEt.text.toString().toIntOrNull() ?: 1
        val description = descriptionEt.text.toString()
        val ingredients = ingredientRows.map { "${it.name.text} - ${it.grams.text}g" }
        val steps = stepRows.map { it.step.text.toString() }

        if (name.isBlank()) {
            Toast.makeText(this, "Recipe name is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Just store a placeholder string for image
        val imageUrl = imageUri?.toString() ?: ""

        // Get the current user
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val authorName = firebaseUser?.displayName ?: firebaseUser?.email ?: "Unknown"

        val recipe = RecipeModel(
            id = id,
            author = authorName,
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
            isPublished = true
        )

        DatabaseHelper.addRecipe(recipe) { success ->
            if (success) {
                Toast.makeText(this, "Recipe published!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to publish recipe", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

data class IngredientRow(val name: AutoCompleteTextView, val grams: EditText, val calories: TextView)
data class StepRow(val step: EditText)
