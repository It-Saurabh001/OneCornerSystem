package com.saurabh.onecornersystem.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream


object ImageUtils {

    private const val MAX_IMAGE_SIZE_KB = 100 // Target 100KB limit

    fun uriToBase64(uri: Uri, contentResolver: ContentResolver): String? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream) ?: return null

            // --- Resize Section ---
            // 800px resolution 100KB ke liye best balance hai
            bitmap = scaleToFit(bitmap, 800)
            // ---------------------------

            val outputStream = ByteArrayOutputStream()
            var quality = 80
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            // Compression loop (Aggressive target for 100KB)
            while (outputStream.size() / 1024 > MAX_IMAGE_SIZE_KB && quality > 10) {
                outputStream.reset()
                quality -= 5 // 5-5% kam karega faster results ke liye
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.NO_WRAP) // Clean string for Firestore
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---  Helper function to reduce width/height ---
    private fun scaleToFit(realImage: Bitmap, maxDimension: Int): Bitmap {
        val width = realImage.width
        val height = realImage.height

        val ratio: Float = width.toFloat() / height.toFloat()

        var finalWidth = maxDimension
        var finalHeight = maxDimension

        if (width > height) {
            finalHeight = (maxDimension / ratio).toInt()
        } else {
            finalWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(realImage, finalWidth, finalHeight, true)
    }

    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isBase64Image(str: String): Boolean {
        return str.startsWith("/9j/") || str.startsWith("iVBOR")
    }
}