package com.mobdeve.s17.itismob_mc0

import android.os.Bundle
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ViewRecipeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)
        val favoriteBtn: ImageButton = findViewById(R.id.favoriteRecBtn)
        var ifClicked= false
        favoriteBtn.setOnClickListener {
            if(ifClicked)
                favoriteBtn.setImageResource(R.drawable.star)
            else
                favoriteBtn.setImageResource(R.drawable.starred)
            ifClicked = !ifClicked
        }
        val starRating: RatingBar = findViewById(R.id.starsRb)
        starRating.setOnRatingBarChangeListener {starRating, rating, fromUser ->
            if (fromUser) {
                Toast.makeText(this, "You rated $rating stars!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}