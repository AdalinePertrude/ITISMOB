package com.mobdeve.s17.itismob_mc0

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
// import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobdeve.s17.itismob_mc0.databinding.HomePageBinding
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private lateinit var viewBinding: HomePageBinding
    private lateinit var recipeRv: RecyclerView
    private val recipeData: ArrayList<RecipeModel> = ArrayList() // Initialize as empty
    private lateinit var searchView: SearchView
    private lateinit var searchList: ArrayList<RecipeModel>
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var fabAddRecipe: FloatingActionButton
    private var isAscending = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = HomePageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setupBackPressedHandler()
        setupSpinner()
        setupSortOrderButton()
        setupRecyclerView() // This will initialize with empty data first
        setupSearchView()
        setupFAB()
        setupNavBar()
        loadDataFromFirebase() // Load data from Firebase after UI setup
    }

    private fun loadDataFromFirebase() {
        DatabaseHelper.fetchRecipeData { dishesList ->
            runOnUiThread {
                // runs when Firebase data is loaded
                recipeData.clear()
                recipeData.addAll(dishesList)

                searchList.clear()
                searchList.addAll(dishesList)

                // Update the adapter with new data
                (recipeRv.adapter as? HomeAdapter)?.updateData(ArrayList(searchList))

                // Show/hide empty state
                if (dishesList.isEmpty()) {
                    viewBinding.noResultsTv.visibility = View.VISIBLE
                    viewBinding.recipesRv.visibility = View.GONE
                   // Toast.makeText(this, "No recipes found", Toast.LENGTH_SHORT).show()
                } else {
                    viewBinding.noResultsTv.visibility = View.GONE
                    viewBinding.recipesRv.visibility = View.VISIBLE
                   // Toast.makeText(this, "Loaded ${dishesList.size} recipes", Toast.LENGTH_SHORT).show()
                }
            }
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
        val filters = arrayOf("All", "Date Added", "Rating", "Duration", "Calories", "Ingredients")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = filters[position]
               //  Toast.makeText(this@HomeActivity, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
                applyFilter(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSortOrderButton() {

        updateSortButtonIcon()

        viewBinding.sortOrderBtn.setOnClickListener {
            isAscending = !isAscending
            updateSortButtonIcon()
            // Re-apply current filter with new order
            val currentFilter = viewBinding.filterSpinner.selectedItem as String
            applyFilter(currentFilter)
        }
    }

    private fun updateSortButtonIcon() {
        val iconRes= if (isAscending) {
            R.drawable.ic_sort_ascending
        } else {
            R.drawable.ic_sort_descending
        }

        viewBinding.sortOrderBtn.setImageResource(iconRes)
    }


    private fun applyFilter(filterType: String) {
        val filteredList = when (filterType) {
            "Date Added" -> if (isAscending) filterByDateAddedAsc() else filterByDateAddedDesc()
            "Rating" -> if (isAscending) filterByRatingAsc() else filterByRatingDesc()
            "Duration" -> if (isAscending) filterByDurationAsc() else filterByDurationDesc()
            "Calories" -> if (isAscending) filterByCaloriesAsc() else filterByCaloriesDesc()
            "Ingredients" -> if (isAscending) filterByIngredientsAsc() else filterByIngredientsDesc()
            else -> ArrayList(recipeData)
        }

        searchList.clear()
        searchList.addAll(filteredList)
        (recipeRv.adapter as? HomeAdapter)?.updateData(ArrayList(searchList))
    }
    
    // Ascending methods
    private fun filterByDateAddedAsc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedBy { parseDate(it.createdAt) })
    }

    private fun filterByRatingAsc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedBy { it.rating })
    }

    private fun filterByDurationAsc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedBy { it.prepTime })
    }

    private fun filterByCaloriesAsc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedBy { it.calories })
    }

    private fun filterByIngredientsAsc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedBy { it.ingredients.size })
    }

    // Descending methods
    private fun filterByDateAddedDesc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedByDescending { parseDate(it.createdAt) })
    }

    private fun filterByRatingDesc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedByDescending { it.rating })
    }

    private fun filterByDurationDesc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedByDescending { it.prepTime })
    }

    private fun filterByCaloriesDesc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedByDescending { it.calories })
    }

    private fun filterByIngredientsDesc(): ArrayList<RecipeModel> {
        return ArrayList(recipeData.sortedByDescending { it.ingredients.size })
    }

    private fun parseDate(dateString: String): Long {
        return try {
            // Try the format you're using when saving dates
            val format = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            format.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            // Fallback to default format
            try {
                SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.getDefault())
                    .parse(dateString)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }
    }


    private fun setupRecyclerView() {
        this.recipeRv = viewBinding.recipesRv
        this.searchList = ArrayList()
        // Start with empty data, will be populated from Firebase
        this.recipeRv.adapter = HomeAdapter(ArrayList(searchList))
        this.recipeRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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
                val searchText = newText?.lowercase(Locale.getDefault()) ?: ""

                if (searchText.isNotEmpty()) {
                    // Filter the recipeData and update searchList
                    val filteredList = recipeData.filter {
                        val searchTextLower = searchText.lowercase(Locale.getDefault())

                        it.label.lowercase(Locale.getDefault()).contains(searchTextLower) ||
                        it.cuisineType.any { cuisine ->
                            cuisine.lowercase(Locale.getDefault()).contains(searchTextLower)
                        } ||
                        it.ingredients.any { ingredientLine ->
                            // split ingredient lines into individual words for better matching
                            ingredientLine.lowercase(Locale.getDefault()).split(" ").any { word ->
                                word.contains(searchTextLower)
                            } || ingredientLine.lowercase(Locale.getDefault()).contains(searchTextLower)
                        }
                    }
                    searchList.clear()
                    searchList.addAll(filteredList)
                } else {
                    // Show all data when search is empty
                    searchList.clear()
                    searchList.addAll(recipeData)
                }

                // Update the adapter with the filtered list
                (recipeRv.adapter as? HomeAdapter)?.updateData(ArrayList(searchList))

                // Show/hide empty state
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