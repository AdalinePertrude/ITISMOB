package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.CalendarPageBinding
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale

class CalendarActivity : ComponentActivity(), OnItemListener {

    private lateinit var monthYear_tv : TextView
    private lateinit var calendar_rv : RecyclerView
    private lateinit var selectedDate : Calendar
    private lateinit var viewBinding : CalendarPageBinding

    private var isDeleteMode = false
    private var selectedItemPosition = -1
    private var selectedRecipePosition = -1

    private lateinit var addedDishes_rv : RecyclerView
    private val addedRecipeData : kotlin.collections.ArrayList<DishesModel> = AddedRecipeDataGenerator.generateAddedDishesData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = CalendarPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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
        if (dayText.isNotEmpty() && !isDeleteMode) {
            val message = "Selected Date: $dayText ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewBinding.dailyPlannerLl.visibility = View.VISIBLE
            viewBinding.plannerDateTv.text = " $dayText ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            viewBinding.dailyPlannerAddBtn.setOnClickListener {
                Toast.makeText(this, "add button clicked", Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(this, "Clicked: ${dish.dishname}", Toast.LENGTH_SHORT).show()
            }
        }

        this.addedDishes_rv.adapter = adapter
        this.addedDishes_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    // Add this method to handle recipe deletion
    private fun deleteRecipe(position: Int) {
        val adapter = addedDishes_rv.adapter as? AddedRecipeAdapter
        adapter?.removeItem(position)

        // Reset delete mode
        resetDeleteMode()
        Toast.makeText(this, "Recipe removed", Toast.LENGTH_SHORT).show()
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

    // Enhanced DeleteDragListener with visual feedback
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
}