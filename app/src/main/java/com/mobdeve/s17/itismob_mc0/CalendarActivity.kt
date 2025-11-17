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

    private lateinit var notificationScheduler: NotificationScheduler
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted - you can schedule notifications
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied - handle accordingly
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_LONG).show()
        }
    }


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
        askNotificationPermission()
    }

    private fun setMonthView(){
        monthYear_tv.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)
        val daysInMonth = getDaysInMonth(selectedDate)
        val calendarAdapter = CalendarAdapter(daysInMonth, this)
        val layoutManager = GridLayoutManager(applicationContext, 7)
        calendar_rv.layoutManager = layoutManager
        calendar_rv.adapter = calendarAdapter
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

    override fun onItemClick(position: Int, dayText: String) {
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val userid = sp.getString("userId", "").toString()

        if (dayText.isNotEmpty() && !isDeleteMode) {
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


     fun onItemLongClick(position: Int, dayText: String) {
        if (dayText.isNotEmpty()) {
            isDeleteMode = true
            selectedItemPosition = position

            // Start shake animation for calendar day
            val holder = calendar_rv.findViewHolderForAdapterPosition(position)
            holder?.itemView?.let { view ->
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation)
                view.startAnimation(shakeAnimation)
            }

            // Show delete area
            viewBinding.deleteArea.visibility = View.VISIBLE

            Toast.makeText(this, "Drag to delete area to remove", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAddedRecipe(){
        this.addedDishes_rv = viewBinding.dailyPlannerRv
        val adapter = AddedRecipeAdapter(this.addedRecipeData)

        // Set up drag listener for recipes
        adapter.onDragStarted = { position ->
            isDeleteMode = true
            selectedRecipePosition = position

            // Show delete area
            viewBinding.deleteArea.visibility = View.VISIBLE

            Toast.makeText(this, "Drag recipe to delete", Toast.LENGTH_SHORT).show()
        }

        // Handle when drag ends (item released)
        adapter.onDragEnded = { position ->
            // If item was dragged but NOT dropped in delete area, stop shaking
            if (isDeleteMode && selectedRecipePosition == position) {
                Toast.makeText(this, "Release in delete area to remove", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up click listeners for recipes
        adapter.onItemLongClickListener = { position, dish ->
            // Handle recipe long press (shake and delete)
            isDeleteMode = true
            selectedRecipePosition = position

            // Start shake animation for recipe
            val holder = addedDishes_rv.findViewHolderForAdapterPosition(position)
            holder?.itemView?.let { view ->
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation)
                view.startAnimation(shakeAnimation)
            }

            // Show delete area
            viewBinding.deleteArea.visibility = View.VISIBLE

            Toast.makeText(this, "Drag recipe to delete area to remove", Toast.LENGTH_SHORT).show()
        }

        adapter.onItemClickListener = { position, dish ->
            if (!isDeleteMode) {
                Toast.makeText(this, "Clicked: ${dish.label}", Toast.LENGTH_SHORT).show()
            }
        }

        this.addedDishes_rv.adapter = adapter
        this.addedDishes_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun deleteRecipe(position: Int) {
        try {
            val recipe = addedRecipeData[position]

            // Cancel the notification if scheduled
            if (::notificationScheduler.isInitialized) {
                // You'll need to track which ScheduledRecipe corresponds to which RecipeModel
                // For now, we'll create a temporary ScheduledRecipe to cancel
                val tempScheduledRecipe = ScheduledRecipe(
                    recipe = recipe,
                    scheduledDateTime = Date() // This would need to be the actual scheduled date
                )
                notificationScheduler.cancelRecipeNotification(tempScheduledRecipe)
            }

            val adapter = addedDishes_rv.adapter as? AddedRecipeAdapter
            adapter?.removeItem(position)

            // Reset delete mode
            resetDeleteMode()
            Toast.makeText(this, "Recipe removed", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error deleting recipe", e)
            Toast.makeText(this, "Error removing recipe", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteItem(position: Int) {
        // Reset delete mode
        resetDeleteMode()

        // Here you would remove the actual calendar data
        Toast.makeText(this, "Calendar item deleted", Toast.LENGTH_SHORT).show()
    }

    private fun resetDeleteMode() {
        isDeleteMode = false
        selectedItemPosition = -1
        selectedRecipePosition = -1
        viewBinding.deleteArea.visibility = View.GONE

        // Clear animations from all calendar cells
        for (i in 0 until calendar_rv.childCount) {
            calendar_rv.getChildAt(i).clearAnimation()
        }

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
                    if (selectedItemPosition != -1) {
                        deleteItem(selectedItemPosition)
                    } else if (selectedRecipePosition != -1) {
                        // Use the safe method to delete recipe
                        deleteRecipe(selectedRecipePosition)
                    }
                    return true
                }

                android.view.DragEvent.ACTION_DRAG_ENDED -> {
                    viewBinding.deleteArea.setBackgroundColor(getColor(android.R.color.holo_red_dark))
                    if (isDeleteMode) {
                        resetDeleteMode()
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

        // Remove duplicate home button click listener
        viewBinding.savedBtnLl.setOnClickListener {
            // Uncomment when you have SavedActivity
            // val intent = Intent(this, SavedActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - need to request permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
                setupNotificationScheduling() // Proceed with your notification setup
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 and below - permission granted by default
            setupNotificationScheduling() // Proceed with your notification setup
        }
    }

    private fun setupNotificationScheduling() {
        // Initialize your notification scheduler here
        // This will work on all Android versions
        Toast.makeText(this, "✅ Recipe reminders enabled!", Toast.LENGTH_SHORT).show()

        notificationScheduler = NotificationScheduler(this)
    }

}