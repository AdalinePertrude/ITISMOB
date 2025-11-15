package com.mobdeve.s17.itismob_mc0

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.URL

class ImageDownloader(private val context: Context) {

    companion object {
        private const val TAG = "ImageDownloader"
        private const val IMAGE_DIR = "recipe_images"
    }

    private val downloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Download image and save to internal storage using coroutines
    fun downloadAndSaveImage(imageUrl: String, recipeId: String, callback: (String?) -> Unit) {
        downloadScope.launch {
            try {
                // Check if URL is valid
                if (imageUrl.isBlank() || !imageUrl.startsWith("http")) {
                    Log.w(TAG, "Invalid image URL: $imageUrl")
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                    return@launch
                }

                // Create directory if it doesn't exist
                val imageDir = File(context.filesDir, IMAGE_DIR)
                if (!imageDir.exists()) {
                    imageDir.mkdirs()
                }

                // Clean the URL and create proper filename
                val cleanUrl = removeQueryParameters(imageUrl)
                val fileExtension = getFileExtension(cleanUrl)
                val fileName = "recipe_${recipeId}$fileExtension"
                val imageFile = File(imageDir, fileName)

                Log.d(TAG, "Original URL: $imageUrl")
                Log.d(TAG, "Clean URL: $cleanUrl")
                Log.d(TAG, "Saving to: ${imageFile.absolutePath}")

                // Download image
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val inputStream = connection.getInputStream()
                val outputStream = FileOutputStream(imageFile)

                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                Log.d(TAG, "Image downloaded successfully: ${imageFile.length()} bytes")

                // Return the file path on main thread
                withContext(Dispatchers.Main) {
                    callback(imageFile.absolutePath)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error downloading image from $imageUrl: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    // Remove query parameters from URL
    private fun removeQueryParameters(url: String): String {
        return try {
            if (url.contains('?')) {
                url.substring(0, url.indexOf('?'))
            } else {
                url
            }
        } catch (e: Exception) {
            url
        }
    }

    // Get file extension from clean URL
    private fun getFileExtension(url: String): String {
        return try {
            val cleanUrl = removeQueryParameters(url)
            val lastDot = cleanUrl.lastIndexOf(".")
            if (lastDot != -1) {
                cleanUrl.substring(lastDot)
            } else {
                ".jpg"
            }
        } catch (e: Exception) {
            ".jpg"
        }
    }

    // Load image from local storage
    fun loadImageFromStorage(filePath: String): Bitmap? {
        return try {
            if (filePath.isBlank()) {
                Log.w(TAG, "Empty file path provided")
                return null
            }

            val file = File(filePath)
            if (file.exists()) {
                Log.d(TAG, "Loading image from: $filePath (${file.length()} bytes)")
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                Log.w(TAG, "Image file not found: $filePath")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image from $filePath: ${e.message}", e)
            null
        }
    }

    // Check if image file exists
    fun imageExists(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    // Get all saved image files
    fun getAllSavedImages(): List<File> {
        val imageDir = File(context.filesDir, IMAGE_DIR)
        return if (imageDir.exists() && imageDir.isDirectory) {
            imageDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }

    // Delete image file
    fun deleteImage(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            val deleted = file.delete()
            if (deleted) {
                Log.d(TAG, "Image deleted: $filePath")
            } else {
                Log.w(TAG, "Failed to delete image: $filePath")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image: ${e.message}", e)
            false
        }
    }

    // Cancel all ongoing downloads
    fun cancelAllDownloads() {
        downloadScope.cancel()
    }
}