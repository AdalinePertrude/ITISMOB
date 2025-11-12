package com.mobdeve.s17.itismob_mc0

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.CommentPageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentActivity : ComponentActivity(){
    private val USER_PREFERENCE = "USER_PREFERENCE"
    private lateinit var viewBinding : CommentPageBinding
    private lateinit var comments_rv : RecyclerView
    private lateinit var commentAdapter: CommentAdapter

    private val commentData: ArrayList<CommentModel> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        val recipeId = intent.getStringExtra("RECIPE_ID")
        super.onCreate(savedInstanceState)
        viewBinding = CommentPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        commentAdapter = CommentAdapter(commentData)
        this.comments_rv = viewBinding.commentsRv
        this.comments_rv.adapter = commentAdapter
        this.comments_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        RecipeDatabaseHelper.fetchRecipeComments(recipeId.toString()) { comments ->
            runOnUiThread {
                commentData.clear()
                commentData.addAll(comments)
                commentAdapter.notifyDataSetChanged()
                updateCommentVisibility()
            }
        }

        updateSendButtonState()
        setupCommentInput()
        updateCommentVisibility()
    }
    private fun setupCommentInput() {
        // Initial button state
        updateSendButtonState()

        // Text watcher to enable/disable button based on input
        viewBinding.addCommentEtv.addTextChangedListener( object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSendButtonState()
            }
            })

        viewBinding.sendCommentBtn.setOnClickListener {
            val commentText = viewBinding.addCommentEtv.text.toString().trim()

            if (commentText.isNotEmpty()) {
                addNewComment(commentText)
            }
        }
    }

    private fun updateSendButtonState() {
        val hasText = viewBinding.addCommentEtv.text?.isNotEmpty() == true
        viewBinding.sendCommentBtn.isEnabled = hasText
    }

    private fun addNewComment(text: String) {
        val recipeId = intent.getStringExtra("RECIPE_ID") ?: return
        val sp: SharedPreferences = getSharedPreferences(USER_PREFERENCE, MODE_PRIVATE)
        val user = sp.getString("userName", "User")

        val newComment = CommentModel(
            user.toString(),
            getCurrentDate(),
            text.trim(),
            sp.getString("userId", "").toString()
        )

        Log.d("DEBUG", "Adding comment: $newComment")

        commentData.add(newComment)
        commentAdapter.notifyItemInserted(commentData.size - 1)
        viewBinding.addCommentEtv.text.clear()
        updateCommentVisibility()
        comments_rv.scrollToPosition(commentData.size - 1)
        saveCommentToFirestore(recipeId, newComment)
    }

    private fun saveCommentToFirestore(recipeId: String, comment: CommentModel) {
        RecipeDatabaseHelper.addCommentToRecipe(recipeId, comment) { success ->
            runOnUiThread {
                if (success) {
                    Log.d("DEBUG", "Comment saved successfully to Firestore")
                } else {
                    Log.e("DEBUG", "Failed to save comment to Firestore")
                    // Optional: Remove from local list if save failed
                    commentData.remove(comment)
                    commentAdapter.notifyDataSetChanged()
                    updateCommentVisibility()
                }
            }
        }
    }

    private fun updateCommentVisibility() {
        if (commentData.isEmpty()) {
            viewBinding.noCommentMessageTv.visibility = View.VISIBLE
            viewBinding.commentsRv.visibility = View.GONE
        } else {
            viewBinding.noCommentMessageTv.visibility = View.GONE
            viewBinding.commentsRv.visibility = View.VISIBLE
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }


}