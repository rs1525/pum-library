package com.akustom15.pum.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import com.akustom15.pum.model.WidgetItem
import com.akustom15.pum.ui.components.AppHeader
import com.akustom15.pum.ui.components.ItemCard
import com.akustom15.pum.utils.AssetsReader
import com.akustom15.pum.utils.KustomIntegration

/** Grid de widgets KWGT con header que hace scroll */
@Composable
fun WidgetGrid(
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
    val widgets = remember { mutableStateOf<List<WidgetItem>>(emptyList()) }

    // Load widgets
    LaunchedEffect(Unit) { widgets.value = AssetsReader.getWidgetsFromAssets(context) }

    // Filter widgets
    val filteredWidgets =
            remember(widgets.value, searchQuery) {
                if (searchQuery.isBlank()) {
                    widgets.value
                } else {
                    widgets.value.filter { widget ->
                        widget.name.contains(searchQuery, ignoreCase = true) ||
                                widget.fileName.contains(searchQuery, ignoreCase = true)
                    }
                }
            }

    if (widgets.value.isEmpty()) {
        // Empty state
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                    text = stringResource(R.string.no_widgets_found),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        // Grid con header que hace scroll
        LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns.count),
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), // Más compacto
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header DENTRO del grid para que haga scroll (ocupa toda la fila)
            if (showHeader) {
                item(span = { GridItemSpan(maxLineSpan) }) { 
                    AppHeader(appName = appName, appSubtitle = appSubtitle, appIcon = appIcon) 
                }
            }

            // Widgets
            items(filteredWidgets) { widget ->
                ItemCard(
                        name = widget.name,
                        description = widget.description,
                        previewUrl = widget.previewUrl,
                        appIcon = appIcon,
                        appName = appName,
                        onApplyClick = {
                            KustomIntegration.applyWidget(
                                    context = context,
                                    widgetFileName = widget.fileName,
                                    packageName = packageName
                            )
                        }
                )
            }
        }
    }
}
