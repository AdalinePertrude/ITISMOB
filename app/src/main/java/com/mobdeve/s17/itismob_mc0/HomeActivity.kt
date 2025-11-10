package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobdeve.s17.itismob_mc0.databinding.HomePageBinding
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private lateinit var viewBinding: HomePageBinding
    private lateinit var recipe_rv: RecyclerView
    private val RecipeData: ArrayList<DishesModel> = AddedRecipeDataGenerator.generateAddedDishesData()
    private lateinit var searchView: SearchView
    private lateinit var searchList: ArrayList<DishesModel>

    private val filters = arrayOf("date added", "rating", "difficulty")
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var fabAddRecipe: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = HomePageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        val profilePage = viewBinding.profileBtnLl
        setupBackPressedHandler()
        setupSpinner()
        setupRecyclerView()
        setupSearchView()
        setupFAB()
        setupNavBar()

        profilePage.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        viewBinding.logoIv.post {
            println("DEBUG: Logo width: ${viewBinding.logoIv.width}")
            println("DEBUG: Logo height: ${viewBinding.logoIv.height}")
            println("DEBUG: Logo visibility: ${viewBinding.logoIv.visibility}")
            println("DEBUG: Logo drawable: ${viewBinding.logoIv.drawable}")
        }

    }

    private fun setupNavBar(){
        viewBinding.calendarBtnLl.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        viewBinding.profileBtnLl.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
//      viewBinding.savedBtnLl.setOnClickListener {
//          val intent = Intent(this, SavedActivity::class.java)
//          startActivity(intent)
//      }
    }
    private fun setupFAB() {
        fabAddRecipe = viewBinding.addRecipeFab

        fabAddRecipe.setOnClickListener {
            //navigate to AddRecipeActivity
            //navigateToAddRecipe()
        }
    }

//    private fun navigateToAddRecipe() {
//        // Replace with your actual AddRecipeActivity
//        val intent = Intent(this, AddRecipeActivity::class.java)
//        startActivity(intent)
//
//        // Optional: Add animation
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//    }





    private fun setupBackPressedHandler() {
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (searchView.hasFocus()) {
                    collapseSearch()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun setupSpinner() {
        val spinner = viewBinding.filterSpinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = filters[position]
                Toast.makeText(this@HomeActivity, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        this.recipe_rv = viewBinding.recipesRv
        this.searchList = ArrayList()
        this.searchList.addAll(RecipeData)
        this.recipe_rv.adapter = HomeAdapter(this.searchList)
        this.recipe_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun setupSearchView() {
        this.searchView = viewBinding.searchViewSv
        searchView.clearFocus()
        // searching
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                expandSearch()
                fabAddRecipe.hide()
            } else {
                collapseSearch()
                fabAddRecipe.show()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                // real-time search as user types
                searchList.clear()
                val searchText = newText?.lowercase(Locale.getDefault()) ?: ""

                if (searchText.isNotEmpty()) {
                    RecipeData.forEach {
                        if (it.dishname.lowercase(Locale.getDefault()).contains(searchText)) {
                            searchList.add(it)
                        }
                    }
                } else {
                    searchList.addAll(RecipeData)
                }

                recipe_rv.adapter?.notifyDataSetChanged()

                //show message if no results found
                if (searchList.isEmpty() && searchText.isNotEmpty()) {
                    viewBinding.noResultsTv.visibility = View.VISIBLE
                    viewBinding.recipesRv.visibility = View.GONE
                } else {
                    viewBinding.noResultsTv.visibility = View.GONE
                    viewBinding.recipesRv.visibility = View.VISIBLE
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        })
    }

    private fun expandSearch() {
        backPressedCallback.isEnabled = true
        viewBinding.logoIv.visibility = View.GONE
        viewBinding.filterLl.visibility = View.GONE
        viewBinding.searchViewSv.elevation = 10f

        val params = viewBinding.searchViewSv.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        params.horizontalBias = 0.5f
        params.marginStart = 32
        params.marginEnd = 32

        viewBinding.searchViewSv.layoutParams = params

        // Force layout update
        viewBinding.root.requestLayout()
    }

    private fun collapseSearch() {
        viewBinding.logoIv.visibility = View.VISIBLE
        viewBinding.filterLl.visibility = View.VISIBLE
        viewBinding.searchViewSv.elevation = 0f

        val params = viewBinding.searchViewSv.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
        params.startToEnd = viewBinding.logoIv.id
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
        params.horizontalBias = 0f
        params.marginStart = 8
        params.marginEnd = 12

        viewBinding.searchViewSv.layoutParams = params
        viewBinding.searchViewSv.clearFocus()
        backPressedCallback.isEnabled = false

        viewBinding.root.requestLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressedCallback.remove()
    }
}