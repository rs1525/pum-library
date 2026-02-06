package com.akustom15.pum.ui.screens

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import com.akustom15.pum.R
import com.akustom15.pum.model.CloudWallpaperItem
import com.akustom15.pum.ui.components.CachedImage
import com.akustom15.pum.ui.viewmodel.CloudWallpaperUiState
import com.akustom15.pum.ui.viewmodel.CloudWallpaperViewModel
import com.akustom15.pum.ui.viewmodel.DownloadState
import com.akustom15.pum.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Grid screen for cloud wallpapers with scroll animations
 */
@Composable
fun CloudWallpaperGrid(
    packageName: String,
    appIcon: Int?,
    appName: String,
    searchQuery: String = "",
    cloudWallpapersUrl: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: CloudWallpaperViewModel = viewModel(
        factory = CloudWallpaperViewModel.Factory(
            context.applicationContext as android.app.Application,
            cloudWallpapersUrl
        )
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    var selectedWallpaper by remember { mutableStateOf<CloudWallpaperItem?>(null) }
    val scope = rememberCoroutineScope()
    
    // Get localized strings for toasts
    val wallpaperDownloadedMsg = stringResource(R.string.wallpaper_downloaded)
    val downloadFailedMsg = stringResource(R.string.download_failed)
    val wifiOnlyMsg = stringResource(R.string.wifi_only_enabled)
    
    // Show download toast
    LaunchedEffect(downloadState) {
        when (downloadState) {
            is DownloadState.Success -> {
                Toast.makeText(context, wallpaperDownloadedMsg, Toast.LENGTH_SHORT).show()
                viewModel.resetDownloadState()
            }
            is DownloadState.Error -> {
                Toast.makeText(context, downloadFailedMsg, Toast.LENGTH_SHORT).show()
                viewModel.resetDownloadState()
            }
            else -> {}
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (uiState) {
            is CloudWallpaperUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.loading_wallpapers),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            is CloudWallpaperUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_wallpapers_available),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp
                        )
                    }
                }
            }
            
            is CloudWallpaperUiState.Error -> {
                val message = (uiState as CloudWallpaperUiState.Error).message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadWallpapers() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            
            is CloudWallpaperUiState.Success -> {
                val allWallpapers = (uiState as CloudWallpaperUiState.Success).wallpapers
                val wallpapers = remember(allWallpapers, searchQuery) {
                    if (searchQuery.isBlank()) {
                        allWallpapers
                    } else {
                        allWallpapers.filter { 
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.author.contains(searchQuery, ignoreCase = true) ||
                            it.collections.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }
                
                val gridState = rememberLazyGridState()
                
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        wallpapers,
                        key = { it.id }
                    ) { wallpaper ->
                        val index = wallpapers.indexOf(wallpaper)
                        AnimatedWallpaperItem(
                            wallpaper = wallpaper,
                            index = index,
                            gridState = gridState
                        ) {
                            selectedWallpaper = wallpaper
                        }
                    }
                }
            }
        }
    }
    
    // Detail dialog
    if (selectedWallpaper != null) {
        WallpaperDetailDialog(
            wallpaper = selectedWallpaper!!,
            onDismiss = { selectedWallpaper = null },
            onDownload = { wallpaper ->
                if (NetworkUtils.isDownloadAllowed(context)) {
                    downloadWallpaper(context, wallpaper)
                } else {
                    Toast.makeText(context, wifiOnlyMsg, Toast.LENGTH_SHORT).show()
                }
            },
            onApply = { wallpaper, flag ->
                if (NetworkUtils.isDownloadAllowed(context)) {
                    scope.launch {
                        applyWallpaper(context, wallpaper.url, flag)
                    }
                } else {
                    Toast.makeText(context, wifiOnlyMsg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

/**
 * Animated wallpaper item with scroll-based animations
 */
@Composable
fun AnimatedWallpaperItem(
    wallpaper: CloudWallpaperItem,
    index: Int,
    gridState: LazyGridState,
    onClick: () -> Unit
) {
    val visibleItemsInfo = gridState.layoutInfo.visibleItemsInfo
    val isVisible = visibleItemsInfo.any { it.index == index }
    
    // Scale animation - dramatic effect
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "wallpaper_scale"
    )
    
    // Alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.2f,
        animationSpec = tween(
            durationMillis = 600,
            easing = EaseOutBack
        ),
        label = "wallpaper_alpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.50f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(4.dp, RoundedCornerShape(30.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box {
            CachedImage(
                url = wallpaper.url,
                contentDescription = wallpaper.name,
                modifier = Modifier.fillMaxSize(),
                useThumbnail = true,
                thumbnailWidth = 400
            )
        }
    }
}

/**
 * Fullscreen dialog for wallpaper details with zoom support
 */
@Composable
fun WallpaperDetailDialog(
    wallpaper: CloudWallpaperItem,
    onDismiss: () -> Unit,
    onDownload: (CloudWallpaperItem) -> Unit,
    onApply: (CloudWallpaperItem, Int) -> Unit
) {
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf(false) }
    var showApplyOptionsDialog by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Zoomable wallpaper
            ZoomableWallpaper(
                url = wallpaper.url,
                contentDescription = wallpaper.name
            )
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            // Bottom controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color.Black.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(vertical = 16.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Wallpaper info
                Text(
                    text = wallpaper.name,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stringResource(R.string.wallpaper_by_author, wallpaper.author),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Info button
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = "Info",
                            tint = Color.White
                        )
                    }
                    
                    // Apply button
                    IconButton(
                        onClick = { showApplyOptionsDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.Wallpaper,
                            contentDescription = "Apply",
                            tint = Color.White
                        )
                    }
                    
                    // Download button
                    IconButton(
                        onClick = { onDownload(wallpaper) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = "Download",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
    
    // Apply options dialog
    if (showApplyOptionsDialog) {
        ApplyWallpaperDialog(
            onDismiss = { showApplyOptionsDialog = false },
            onApplyHome = { onApply(wallpaper, WallpaperManager.FLAG_SYSTEM) },
            onApplyLock = { onApply(wallpaper, WallpaperManager.FLAG_LOCK) },
            onApplyBoth = { onApply(wallpaper, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK) }
        )
    }
    
    // Info dialog
    if (showInfoDialog) {
        WallpaperInfoDialog(
            wallpaper = wallpaper,
            onDismiss = { showInfoDialog = false }
        )
    }
}

@Composable
fun ApplyWallpaperDialog(
    onDismiss: () -> Unit,
    onApplyHome: () -> Unit,
    onApplyLock: () -> Unit,
    onApplyBoth: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.apply_wallpaper),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                TextButton(
                    onClick = {
                        onApplyHome()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.home_screen), color = Color.White, fontSize = 16.sp)
                }
                
                TextButton(
                    onClick = {
                        onApplyLock()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.lock_screen), color = Color.White, fontSize = 16.sp)
                }
                
                TextButton(
                    onClick = {
                        onApplyBoth()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(stringResource(R.string.both_screens), color = Color.White, fontSize = 16.sp)
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.cancel), color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun WallpaperInfoDialog(
    wallpaper: CloudWallpaperItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.wallpaper_info),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                InfoRow(stringResource(R.string.wallpaper_info_name), wallpaper.name)
                InfoRow(stringResource(R.string.wallpaper_info_author), wallpaper.author)
                if (!wallpaper.collections.isNullOrBlank()) {
                    InfoRow(stringResource(R.string.wallpaper_info_collection), wallpaper.collections)
                }
                if (!wallpaper.dimensions.isNullOrBlank()) {
                    InfoRow(stringResource(R.string.wallpaper_info_resolution), wallpaper.dimensions)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.close), fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "$label:",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ZoomableWallpaper(
    url: String,
    contentDescription: String?
) {
    var scale by remember(url) { mutableStateOf(1f) }
    var offset by remember(url) { mutableStateOf(Offset.Zero) }
    val minScale = 1f
    val maxScale = 4f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(url) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val proposedScale = (scale * zoom).coerceIn(minScale, maxScale)

                        if (proposedScale <= (minScale + 0.001f)) {
                            scale = minScale
                            offset = Offset.Zero
                        } else {
                            val scaleChange = proposedScale / scale
                            scale = proposedScale

                            var newOffset = offset + pan * scaleChange
                            val maxX = (containerWidthPx * (scale - 1f)) / 2f
                            val maxY = (containerHeightPx * (scale - 1f)) / 2f
                            newOffset = Offset(
                                x = newOffset.x.coerceIn(-maxX, maxX),
                                y = newOffset.y.coerceIn(-maxY, maxY)
                            )
                            offset = newOffset
                        }
                    }
                }
        ) {
            CachedImage(
                url = url,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit,
                useThumbnail = false
            )
        }
    }
}

/**
 * Download wallpaper using DownloadManager
 */
private fun downloadWallpaper(context: Context, wallpaper: CloudWallpaperItem) {
    try {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(wallpaper.url)
        
        val request = DownloadManager.Request(uri).apply {
            setTitle(context.getString(R.string.downloading_title, wallpaper.name))
            setDescription(context.getString(R.string.downloading_description))
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                "PUM_Wallpapers/${wallpaper.name.replace(" ", "_")}.jpg"
            )
            setAllowedOverMetered(false)
            setAllowedOverRoaming(false)
        }
        
        downloadManager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.download_started), Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show()
    }
}

/**
 * Apply wallpaper to the device
 */
private suspend fun applyWallpaper(context: Context, imageUrl: String, which: Int) {
    try {
        withContext(Dispatchers.IO) {
            val wallpaperManager = WallpaperManager.getInstance(context)
            
            // Load image using Coil singleton (shared cache)
            val imageLoader = coil.ImageLoader(context)
            val request = coil.request.ImageRequest.Builder(context)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .allowHardware(false)
                .build()
            
            val result = imageLoader.execute(request)
            val drawable = result.drawable
            
            if (drawable != null) {
                // Create bitmap from drawable (handles all drawable types)
                val bitmap = android.graphics.Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                
                try {
                    wallpaperManager.setBitmap(bitmap, null, true, which)
                    val message = when (which) {
                        WallpaperManager.FLAG_SYSTEM -> context.getString(R.string.wallpaper_applied_home)
                        WallpaperManager.FLAG_LOCK -> context.getString(R.string.wallpaper_applied_lock)
                        else -> context.getString(R.string.wallpaper_applied_both)
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                } finally {
                    bitmap.recycle()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.wallpaper_failed_load), Toast.LENGTH_SHORT).show()
                }
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.getString(R.string.wallpaper_failed_apply), Toast.LENGTH_SHORT).show()
        }
    }
}
