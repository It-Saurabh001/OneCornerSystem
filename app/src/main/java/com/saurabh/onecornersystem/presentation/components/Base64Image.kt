package com.saurabh.onecornersystem.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.utils.ImageUtils

/**
 * Create a painter from base64 string using existing ImageUtils
 */
@Composable
fun rememberBase64ImagePainter(base64String: String): Painter? {
    return remember(base64String) {
        val bitmap = ImageUtils.base64ToBitmap(base64String)
        bitmap?.let { BitmapPainter(it.asImageBitmap()) }
    }
}

/**
 * Smart image painter that handles both URL and Base64 images
 */
@Composable
fun rememberSmartImagePainter(imageSource: String): Painter? {
    return if (ImageUtils.isBase64Image(imageSource)) {
        rememberBase64ImagePainter(imageSource)
    } else {
        rememberAsyncImagePainter(imageSource)
    }
}

/**
 * Composable that displays image from either URL or Base64 string
 */
@Composable
fun Base64Image(
    imageSource: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val painter = rememberSmartImagePainter(imageSource)

    painter?.let {
        Image(
            painter = it,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

