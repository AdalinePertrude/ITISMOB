package com.mobdeve.s17.itismob_mc0

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s17.itismob_mc0.databinding.CommentPageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentActivity : ComponentActivity(){
    private lateinit var viewBinding : CommentPageBinding
    private lateinit var comments_rv : RecyclerView
    private lateinit var commentAdapter: CommentAdapter

    private val commentData = ArrayList(CommentDataGenerator.generateCommentData())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = CommentPageBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        commentAdapter = CommentAdapter(commentData)

        this.comments_rv = viewBinding.commentsRv
        this.comments_rv.adapter = commentAdapter
        this.comments_rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

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
        val newComment = CommentModel(
            "Current User", // Replace with actual username
            getCurrentDate(),
            text.trim()
        )
        //add comment
        commentData.add(newComment)
        // notify adapter
        commentAdapter.notifyItemInserted(commentData.size - 1)
        // clear input field
        viewBinding.addCommentEtv.text.clear()
        updateCommentVisibility()

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