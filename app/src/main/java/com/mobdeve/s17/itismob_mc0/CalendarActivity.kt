package com.mobdeve.s17.itismob_mc0

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.mobdeve.s17.itismob_mc0.databinding.CalendarPageBinding
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : ComponentActivity(), OnItemListener {

    private lateinit var monthYear_tv : TextView
    private lateinit var calendar_rv : RecyclerView
    private lateinit var selectedDate : Calendar
    private lateinit var viewBinding : CalendarPageBinding

    private val USER_PREFERENCE = "USER_PREFERENCE"
    private var isDeleteMode = false
    private var selectedItemPosition = -1
    private var selectedRecipePosition = -1

    private lateinit var addedDishes_rv : RecyclerView
    private val addedRecipeData : ArrayList<RecipeModel> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = CalendarPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        testFirestoreQuery()

        // Initialize delete area drag listener
        viewBinding.deleteArea.setOnDragListener(DeleteDragListener())

        val prevMonthBtn: ImageButton = viewBinding.previousMonthBtn
        val nextMonthBtn: ImageButton = viewBinding.nextMonthBtn
        monthYear_tv = viewBinding.monthYearTv
        calendar_rv = viewBinding.calendarRv
        selectedDate = Calendar.getInstance()

        prevMonthBtn.setOnClickListener {
            previousMonthAction()
        }
        nextMonthBtn.setOnClickListener {
            nextMonthAction()
        }
        setMonthView()
        setAddedRecipe()
        setupNavBar()
    }

    private fun setMonthView(){
        monthYear_tv.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)
        val daysInMonth = getDaysInMonth(selectedDate)

        // Pass current month and current date to adapter
        val calendarAdapter = CalendarAdapter(daysInMonth, this, selectedDate)
        val layoutManager = GridLayoutManager(applicationContext, 7)
        calendar_rv.layoutManager = layoutManager
        calendar_rv.adapter = calendarAdapter
    }

    override fun onItemClick(position: Int, dayText: String) {
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val userid = sp.getString("userId", "").toString()

        if (dayText.isNotEmpty() && !isDeleteMode) {
            // Additional safety check - prevent clicking past dates
            if (isPastDate(dayText)) {
                Toast.makeText(this, "Cannot select past dates", Toast.LENGTH_SHORT).show()
                return
            }

            val formattedDate = formatDateForFirestore(dayText)
            getAddedRecipeofSelectedDate(formattedDate, userid)
            val message = "Selected Date: $dayText ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewBinding.dailyPlannerLl.visibility = View.VISIBLE
            viewBinding.plannerDateTv.text = " $dayText ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            viewBinding.dailyPlannerAddBtn.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
        }
    }


    // Helper function to check if a date is in the past
    private fun isPastDate(dayText: String): Boolean {
        if (dayText.isEmpty()) return false

        try {
            val cellDate = selectedDate.clone() as Calendar
            cellDate.set(Calendar.DAY_OF_MONTH, dayText.toInt())

            // Reset time parts for accurate comparison
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            cellDate.set(Calendar.HOUR_OF_DAY, 0)
            cellDate.set(Calendar.MINUTE, 0)
            cellDate.set(Calendar.SECOND, 0)
            cellDate.set(Calendar.MILLISECOND, 0)

            return cellDate.before(today)

        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error checking past date", e)
            return false
        }
    }

    private fun getDaysInMonth(date: Calendar): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val calendar = date.clone() as Calendar
        val daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // add cells for days before the first day of the month
        for (i in 1 until dayOfWeek) {
            daysInMonthArray.add("")
        }

        // add the days of the month
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(i.toString())
        }

        val filledCells = daysInMonthArray.size

        //check if row is filled or not  7-7 = 0 => all filled else add empty cells
        val remainingCells = 7 - (filledCells % 7)

        //fill remaining cells if row cells is not filled
        if(remainingCells != 7) {
            for (i in 1..remainingCells) {
                daysInMonthArray.add("")
            }
        }

        return daysInMonthArray
    }
    //test if query works
    private fun testFirestoreQuery() {
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val userid = sp.getString("userId", "").toString()

        val db = Firebase.firestore
        db.collection("users")
            .document(userid)
            .collection("Added To Calendar Recipes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("CalendarActivity", "=== ALL CALENDAR ENTRIES ===")
                for (document in querySnapshot.documents) {
                    val recipeId = document.getString("recipeId")
                    val selectedDateTimestamp = document.getTimestamp("selectedDate")

                    Log.d("CalendarActivity", "Entry - recipeId: '$recipeId'")
                    Log.d("CalendarActivity", "selectedDate as Timestamp: $selectedDateTimestamp")

                    // Convert timestamp
                    selectedDateTimestamp?.toDate()?.let { date ->
                        val formatted = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a z", Locale.getDefault()).format(date)
                        Log.d("CalendarActivity", "Converted date: '$formatted'")
                    }
                    Log.d("CalendarActivity", "Full document data: ${document.data}")
                }
                Log.d("CalendarActivity", "=== END CALENDAR ENTRIES ===")
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarActivity", "Error testing Firestore query: ${exception.message}")
            }
    }

    private fun formatDateForFirestore(dayText: String): String {
        try {
            // Create a calendar instance for the selected day
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
                set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, dayText.toInt())
            }

            val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            val formattedDate = outputFormat.format(calendar.time)

            Log.d("CalendarActivity", "Formatted date: '$formattedDate'")
            return formattedDate
        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error formatting date", e)
            return ""
        }
    }

    private fun getAddedRecipeofSelectedDate(selectedDay: String, userID : String){
        DatabaseHelper.getAddedToCalendarRecipes(selectedDay, userID) { recipes ->
            addedRecipeData.clear()
            addedRecipeData.addAll(recipes)
            setAddedRecipe()
        }
    }

    private fun setAddedRecipe(){
        this.addedDishes_rv = viewBinding.dailyPlannerRv
        val adapter = AddedRecipeAdapter(this.addedRecipeData)

        // Set up drag listener for recipes
        adapter.onDragStarted = { position, view ->
            isDeleteMode = true
            selectedRecipePosition = position

            // Show delete area
            viewBinding.deleteArea.visibility = View.VISIBLE

            Toast.makeText(this, "Drag recipe to delete area to remove", Toast.LENGTH_SHORT).show()
        }

        // Handle when drag ends
        adapter.onDragEnded = { position ->
            // If item was dragged but NOT dropped in delete area, stop shaking
            if (isDeleteMode && selectedRecipePosition == position) {
                // reset if not dropped in delete area
                resetDeleteMode()
                Toast.makeText(this, "Release in delete area to remove", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up click listeners for recipes
        adapter.onItemLongClickListener = { position, dish, view ->
            // Handle recipe long press shake and delete
            isDeleteMode = true
            selectedRecipePosition = position

            // Start shake animation for recipe
            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation)
            view.startAnimation(shakeAnimation)

            // Show delete area
            viewBinding.deleteArea.visibility = View.VISIBLE

            Toast.makeText(this, "Drag recipe to delete area to remove", Toast.LENGTH_SHORT).show()
        }

        adapter.onItemClickListener = { position, dish ->
            if (!isDeleteMode) {
                val intent = Intent(this, ViewRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", dish.id)
                startActivity(intent)
            }
        }

        this.addedDishes_rv.adapter = adapter
        this.addedDishes_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }


    private fun deleteRecipe(position: Int) {
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val userid = sp.getString("userId", "") ?: ""

        try {
            // Check if position is valid
            if (position < 0 || position >= addedRecipeData.size) {
                Log.e("CalendarActivity", "Invalid position: $position, data size: ${addedRecipeData.size}")
                resetDeleteMode()
                return
            }

            val recipe = addedRecipeData[position]
            val recipeId = recipe.id

            Log.d("CalendarActivity", "Deleting recipe: ${recipe.label} with ID: $recipeId")

            // Get the date components from the selected calendar day
            val dateComponents = getDateComponentsFromSelectedDay()

            if (dateComponents != null) {
                val (year, month, day) = dateComponents

                Log.d("CalendarActivity", "Date components - Year: $year, Month: $month, Day: $day")

                // Store the recipe for potential recovery
                val removedRecipe = addedRecipeData[position]

                // Remove from local data and update adapter
                runOnUiThread {
                    addedRecipeData.removeAt(position)
                    val adapter = addedDishes_rv.adapter as? AddedRecipeAdapter
                    adapter?.notifyItemRemoved(position)
                    resetDeleteMode()
                }

                // Delete from Firebase
                DatabaseHelper.deleteRecipeFromCalendar(userid, recipeId, year, month, day) { success ->
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Recipe removed", Toast.LENGTH_SHORT).show()
                        } else {
                            // If Firebase deletion fails, add the recipe back to local data
                            Log.e("CalendarActivity", "Firebase deletion failed, restoring recipe")
                            addedRecipeData.add(position, removedRecipe)
                            val adapter = addedDishes_rv.adapter as? AddedRecipeAdapter
                            adapter?.notifyItemInserted(position)
                            Toast.makeText(this, "Error removing recipe from cloud", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Log.e("CalendarActivity", "Could not get date components")
                Toast.makeText(this, "Error: Could not get date information", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error deleting recipe", e)
            Toast.makeText(this, "Error removing recipe", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to get date components safely
    private fun getDateComponentsFromSelectedDay(): Triple<Int, Int, Int>? {
        return try {
            // Get the day from the calendar display
            val selectedDayText = viewBinding.plannerDateTv.text.toString().trim()
            Log.d("CalendarActivity", "Planner date text: '$selectedDayText'")

            // Parse the date text - format: "17 November 2025"
            val dateParts = selectedDayText.split(" ").filter { it.isNotBlank() }

            if (dateParts.size >= 3) {
                val day = dateParts[0].toIntOrNull()
                val monthName = dateParts[1]
                val year = dateParts[2].toIntOrNull()

                if (day != null && year != null) {
                    val month = convertMonthNameToNumber(monthName)
                    Triple(year, month, day)
                } else {
                    null
                }
            } else {
                // Fallback: use the currently selected calendar date
                val year = selectedDate.get(Calendar.YEAR)
                val month = selectedDate.get(Calendar.MONTH)
                val day = selectedDate.get(Calendar.DAY_OF_MONTH)
                Triple(year, month, day)
            }
        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error getting date components", e)
            null
        }
    }

    // Improved month conversion with better error handling
    private fun convertMonthNameToNumber(monthName: String): Int {
        return try {
            val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val date = dateFormat.parse(monthName)
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.MONTH)
        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error converting month: $monthName, using current month", e)
            selectedDate.get(Calendar.MONTH) // Fallback to current month
        }
    }


    private fun resetDeleteMode() {
        isDeleteMode = false
        selectedRecipePosition = -1
        viewBinding.deleteArea.visibility = View.GONE

        // Clear animations from all recipe cells
        for (i in 0 until addedDishes_rv.childCount) {
            addedDishes_rv.getChildAt(i).clearAnimation()
        }
    }

    private fun previousMonthAction() {
        selectedDate.add(Calendar.MONTH, -1)
        setMonthView()
        viewBinding.dailyPlannerLl.visibility = View.INVISIBLE
        resetDeleteMode() // Reset delete mode when changing months
    }

    private fun nextMonthAction() {
        selectedDate.add(Calendar.MONTH, 1)
        setMonthView()
        viewBinding.dailyPlannerLl.visibility = View.INVISIBLE
        resetDeleteMode() // Reset delete mode when changing months
    }

    inner class DeleteDragListener : View.OnDragListener {
        override fun onDrag(v: View, event: android.view.DragEvent): Boolean {
            when (event.action) {
                android.view.DragEvent.ACTION_DRAG_STARTED -> {
                    viewBinding.deleteArea.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                    return true
                }

                android.view.DragEvent.ACTION_DRAG_ENTERED -> {
                    viewBinding.deleteArea.setBackgroundColor(getColor(android.R.color.holo_red_light))
                    return true
                }

                android.view.DragEvent.ACTION_DRAG_EXITED -> {
                    viewBinding.deleteArea.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                    return true
                }

                android.view.DragEvent.ACTION_DROP -> {
                    // Get the dragged item data from clip data
                    val clipData = event.clipData
                    if (clipData != null) {
                        val itemData = clipData.getItemAt(0).text.toString()

                        when {
                            itemData.startsWith("recipe::") -> {
                                val parts = itemData.split("::")
                                if (parts.size >= 2) {
                                    val position = parts[1].toIntOrNull()
                                    position?.let { deleteRecipe(it) }
                                }
                            }
                        }
                    }
                    return true
                }

                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    viewBinding.deleteArea.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                    // Check if the drag ended without dropping in delete area
                    val wasDropped = event.result
                    if (!wasDropped && isDeleteMode) {
                        // Item was dragged but not dropped in delete area - cancel delete mode
                        resetDeleteMode()
                        Toast.makeText(this@CalendarActivity, "Drag cancelled", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun setupNavBar(){
        viewBinding.homeBtnLl.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        viewBinding.profileBtnLl.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        viewBinding.savedBtnLl.setOnClickListener {
             val intent = Intent(this, SavedRecipeActivity::class.java)
             startActivity(intent)
        }
    }

}