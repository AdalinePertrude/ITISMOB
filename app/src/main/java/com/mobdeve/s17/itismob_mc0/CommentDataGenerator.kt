package com.mobdeve.s17.itismob_mc0

class CommentDataGenerator {
    companion object {
        private val comment1 : CommentModel = CommentModel("Adrian", "2025-03-23", "Hello this is nice" )
        private val comment2 : CommentModel = CommentModel("Lance", "2025-04-23", "Hello this is not nice" )
        private val comment3 : CommentModel = CommentModel("Adam", "2025-04-26", "Hello this is not nice" )

        fun generateCommentData(): ArrayList<CommentModel> {
            return arrayListOf<CommentModel>(comment1,comment2, comment3)
        }
    }
}