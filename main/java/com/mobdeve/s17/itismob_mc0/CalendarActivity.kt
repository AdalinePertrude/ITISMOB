package com.mobdeve.s17.itismob_mc0

import android.os.Bundle
import android.view.View
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

    private lateinit var addedDishes_rv : RecyclerView
    private val addedRecipeData : kotlin.collections.ArrayList<DishesModel> = AddedRecipeDataGenerator.generateAddedDishesData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = CalendarPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
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
}

    private fun setMonthView(){
        monthYear_tv.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)
        val daysInMonth = getDaysInMonth(selectedDate)
        val calendarAdapter = CalendarAdapter(daysInMonth, this)
        val layoutManager = GridLayoutManager(applicationContext, 7)
        calendar_rv.layoutManager = layoutManager
        calendar_rv.adapter = calendarAdapter
    }

    private fun setAddedRecipe(){
        this.addedDishes_rv = viewBinding.dailyPlannerRv
        this.addedDishes_rv.adapter = AddedRecipeAdapter(this.addedRecipeData)
        this.addedDishes_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
        if (dayText.isNotEmpty()) {
            val message = "Selected Date: $dayText ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            viewBinding.dailyPlannerLl.visibility = View.VISIBLE
            viewBinding.plannerDateTv.text = " $dayText ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            viewBinding.dailyPlannerAddBtn.setOnClickListener {
                Toast.makeText(this, "add button clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun previousMonthAction() {
        selectedDate.add(Calendar.MONTH, -1)
        setMonthView()
        viewBinding.dailyPlannerLl.visibility = View.INVISIBLE
    }

    private fun nextMonthAction() {
        selectedDate.add(Calendar.MONTH, 1)
        setMonthView()
        viewBinding.dailyPlannerLl.visibility = View.INVISIBLE
    }



}














