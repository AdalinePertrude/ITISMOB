package com.mobdeve.s17.itismob_mc0
class AddedRecipeDataGenerator {

    companion object {
        private val dish1 : DishesModel = DishesModel("spaghetti", 5.0, "android.resource://com.mobdeve.s17.bea.adrianvince.mco2/drawable/spaghetti", 20, 5, false)
        private val dish2 : DishesModel = DishesModel("dish", 4.0, "android.resource://com.mobdeve.s17.bea.adrianvince.mco2/drawable/dish", 40, 5, false)
        private val dish3 : DishesModel = DishesModel("carbonara", 5.0, "android.resource://com.mobdeve.s17.bea.adrianvince.mco2/drawable/carbonara", 20, 5, false)
        private val dish4 : DishesModel = DishesModel("sisig", 4.0, "android.resource://com.mobdeve.s17.bea.adrianvince.mco2/drawable/sisig", 40, 5, false)

        fun generateAddedDishesData(): ArrayList<DishesModel> {
            return arrayListOf<DishesModel>(dish4, dish2, dish3, dish4)
        }
    }



}