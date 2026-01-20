package com.akustom15.pum.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size

/**
 * Cached image component with fade-in animation, loading indicator and optimized caching.
 * Supports Cloudinary thumbnail transformations for reduced data usage.
 * 
 * @param url The image URL
 * @param contentDescription Accessibility description
 * @param modifier Modifier for the component
 * @param contentScale How the image should be scaled
 * @param useThumbnail If true, loads a smaller thumbnail version (for grids)
 * @param thumbnailWidth Width for thumbnail (default 400px for optimal grid display)
 */
@Composable
fun CachedImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    useThumbnail: Boolean = false,
    thumbnailWidth: Int = 400
) {
    var isLoading by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0f else 1f,
        animationSpec = tween(300),
        label = "image_alpha"
    )
    val context = LocalContext.current
    
    // Optimize URL for Cloudinary if thumbnail is requested
    val optimizedUrl = remember(url, useThumbnail) {
        if (useThumbnail) {
            getOptimizedCloudinaryUrl(url, thumbnailWidth)
        } else {
            url
        }
    }
    
    Box(modifier = modifier) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(optimizedUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCacheKey(optimizedUrl)
                .diskCacheKey(optimizedUrl)
                .crossfade(true)
                .size(if (useThumbnail) Size(thumbnailWidth, thumbnailWidth * 2) else Size.ORIGINAL)
                .listener(
                    onStart = { isLoading = true },
                    onSuccess = { _, _ -> isLoading = false },
                    onError = { _, _ -> isLoading = false }
                )
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha),
            contentScale = contentScale
        )
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Converts a Cloudinary URL to use thumbnail transformations.
 * This significantly reduces data usage for grid views.
 */
private fun getOptimizedCloudinaryUrl(url: String, width: Int): String {
    return try {
        if (url.contains("cloudinary.com") && url.contains("/upload/")) {
            url.replace("/upload/", "/upload/w_${width},q_auto,f_auto/")
        } else if (url.contains("cloudinary.com") && url.contains("/image/upload/")) {
            url.replace("/image/upload/", "/image/upload/w_${width},q_auto,f_auto/")
        } else {
            url
        }
    } catch (e: Exception) {
        url
    }
}
