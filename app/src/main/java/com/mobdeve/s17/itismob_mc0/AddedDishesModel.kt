package com.mobdeve.s17.itismob_mc0

data class DishesModel(
    val dishname: String,
    val rating: Double,
    val imageId: String,
    val prepTime: Int,
    val serving: Int,
    var isSaved: Boolean = false
)