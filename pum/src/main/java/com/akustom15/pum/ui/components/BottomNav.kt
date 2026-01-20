package com.akustom15.pum.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.akustom15.pum.R
import com.akustom15.pum.config.PumTab

/** Bottom navigation bar with configurable tabs */
@Composable
fun PumBottomNavigation(
        visibleTabs: List<PumTab>,
        selectedTab: PumTab,
        onTabSelected: (PumTab) -> Unit,
        modifier: Modifier = Modifier
) {
        if (visibleTabs.isEmpty()) return

        NavigationBar(
                modifier = modifier,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
        ) {
                visibleTabs.forEach { tab ->
                        NavigationBarItem(
                                icon = { /* Icon handled in label for simplicity */},
                                label = {
                                        Text(
                                                text =
                                                        when (tab) {
                                                                PumTab.Widgets -> stringResource(R.string.tab_widgets)
                                                                PumTab.Wallpapers -> stringResource(R.string.tab_wallpapers)
                                                                PumTab.WallpaperCloud -> stringResource(R.string.tab_wallpaper_cloud)
                                                        }
                                        )
                                },
                                selected = selectedTab == tab,
                                onClick = { onTabSelected(tab) },
                                colors =
                                        NavigationBarItemDefaults.colors(
                                                selectedIconColor =
                                                        MaterialTheme.colorScheme.primary,
                                                selectedTextColor =
                                                        MaterialTheme.colorScheme.primary,
                                                unselectedIconColor =
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor =
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                indicatorColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                        )
                        )
                }
        }
}
