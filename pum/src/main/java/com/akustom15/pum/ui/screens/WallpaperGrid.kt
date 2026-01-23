package com.akustom15.pum.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akustom15.pum.R
import com.akustom15.pum.data.PumPreferences
import com.akustom15.pum.model.WallpaperItem
import com.akustom15.pum.ui.components.AppHeader
import com.akustom15.pum.ui.components.WallpaperCard
import com.akustom15.pum.utils.AssetsReader
import com.akustom15.pum.utils.KustomIntegration

/** Grid de wallpapers KLWP - Previews MÁS GRANDES que widgets */
@Composable
fun WallpaperGrid(
        packageName: String,
        appIcon: Int?,
        appName: String,
        appSubtitle: String = "",
        searchQuery: String = "",
        showHeader: Boolean = true,
        modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { PumPreferences.getInstance(context) }
    val gridColumns by preferences.gridColumns.collectAsState()
    val wallpapers = remember { mutableStateOf<List<WallpaperItem>>(emptyList()) }

    LaunchedEffect(Unit) { wallpapers.value = AssetsReader.getWallpapersFromAssets(context) }

    val filteredWallpapers =
            remember(wallpapers.value, searchQuery) {
                if (searchQuery.isBlank()) {
                    wallpapers.value
                } else {
                    wallpapers.value.filter { wallpaper ->
                        wallpaper.name.contains(searchQuery, ignoreCase = true) ||
                                wallpaper.fileName.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

    if (wallpapers.value.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                    text = stringResource(R.string.no_wallpapers_found),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns.count),
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showHeader) {
                item { AppHeader(appName = appName, appSubtitle = appSubtitle, appIcon = appIcon) }
            }

            // WALLPAPER CARD - MÁS GRANDE (450dp vs 160dp de widgets)
            items(filteredWallpapers) { wallpaper ->
                WallpaperCard(
                        name = wallpaper.name,
                        description = wallpaper.description,
                        previewUrl = wallpaper.previewUrl,
                        appIcon = appIcon,
                        appName = appName,
                        onApplyClick = {
                            KustomIntegration.applyWallpaper(
                                    context = context,
                                    wallpaperFileName = wallpaper.fileName,
                                    packageName = packageName
                            )
                        }
                )
            }
        }
    }
}
