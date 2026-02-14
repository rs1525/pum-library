package com.akustom15.pum.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.akustom15.pum.R
import com.akustom15.pum.config.PumConfig
import com.akustom15.pum.config.PumTab
import com.akustom15.pum.ui.components.AnimatedSearchTopBar
import com.akustom15.pum.ui.screens.about.AboutScreen
import com.akustom15.pum.ui.screens.settings.SettingsScreen
import com.akustom15.pum.ui.components.ChangelogDialog
import com.akustom15.pum.ui.components.PumBottomNavigation
import com.akustom15.pum.ui.screens.CloudWallpaperGrid
import com.akustom15.pum.ui.screens.WallpaperGrid
import com.akustom15.pum.ui.screens.WidgetGrid
import com.akustom15.pum.ui.theme.PumTheme
import com.akustom15.pum.utils.AssetsReader
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.content.ContextCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

/**
 * Main PUM screen TopBar solo tiene search + menu Header con icono está DENTRO del contenido y hace
 * scroll
 */
@Composable
fun PumScreen(config: PumConfig) {
    PumScreenContent(config)
}

@Composable
private fun PumScreenContent(config: PumConfig) {
        val context = LocalContext.current
        val visibleTabs = remember(config) { config.getVisibleTabs() }
        var selectedTab by
                remember(visibleTabs) {
                        mutableStateOf(visibleTabs.firstOrNull() ?: PumTab.Widgets)
                }
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        
        // Menu state
        var showMenu by remember { mutableStateOf(false) }
        var showChangelogDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var showSettingsDialog by remember { mutableStateOf(false) }
        
        // Get app version automatically from PackageManager
        val appVersion = remember {
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                "v${packageInfo.versionName}"
            } catch (e: PackageManager.NameNotFoundException) {
                "v1.0"
            }
        }
        
        // Get widget and wallpaper counts
        val widgetCount = remember(config.packageName) {
            try {
                AssetsReader.getWidgetsFromAssets(context).size
            } catch (e: Exception) {
                0
            }
        }
        
        val wallpaperCount = remember(config.packageName) {
            try {
                AssetsReader.getWallpapersFromAssets(context).size
            } catch (e: Exception) {
                0
            }
        }

        PumTheme {
        // Calculate bottom padding for grids: nav bar inset + pill height if visible
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val bottomContentPadding = if (visibleTabs.size > 1) navBarBottom + 80.dp else navBarBottom

        // Haze state for frosted glass blur on bottom nav pill
        val hazeState = remember { HazeState() }

        Box(
                modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
        ) {
                Column(modifier = Modifier.fillMaxSize()) {
                        // Solo search + menu (NO el header con icono)
                        AnimatedSearchTopBar(
                                isSearchActive = isSearchActive,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onSearchActiveChange = { isSearchActive = it },
                                onMenuClick = { showMenu = true },
                                showMenuDropdown = showMenu,
                                onMenuDismiss = { showMenu = false },
                                onChangelogClick = { showChangelogDialog = true },
                                onAboutClick = { showAboutDialog = true },
                                onSettingsClick = { showSettingsDialog = true }
                        )

                        // Contenido con header que hace scroll
                        Box(
                                modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .haze(state = hazeState)
                        ) {
                                when (selectedTab) {
                                        PumTab.Widgets -> {
                                                WidgetGrid(
                                                        packageName = config.packageName,
                                                        appIcon = config.appIcon,
                                                        appName = config.appName,
                                                        appSubtitle = config.appSubtitle,
                                                        searchQuery = searchQuery,
                                                        showHeader =
                                                                !isSearchActive, // Ocultar header
                                                        // cuando busca
                                                        bottomContentPadding = bottomContentPadding
                                                        )
                                        }
                                        PumTab.Wallpapers -> {
                                                WallpaperGrid(
                                                        packageName = config.packageName,
                                                        appIcon = config.appIcon,
                                                        appName = config.appName,
                                                        appSubtitle = config.appSubtitle,
                                                        searchQuery = searchQuery,
                                                        showHeader = !isSearchActive,
                                                        bottomContentPadding = bottomContentPadding
                                                )
                                        }
                                        PumTab.WallpaperCloud -> {
                                                CloudWallpaperGrid(
                                                        packageName = config.packageName,
                                                        appIcon = config.appIcon,
                                                        appName = config.appName,
                                                        searchQuery = searchQuery,
                                                        cloudWallpapersUrl = config.cloudWallpapersUrl,
                                                        bottomContentPadding = bottomContentPadding
                                                )
                                        }
                                }
                        }
                }

                // Floating bottom navigation - overlays content, no black background
                if (visibleTabs.size > 1) {
                        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                        val navbarColor = if (isDark) {
                                Color(ContextCompat.getColor(context, R.color.pum_navbar_color_dark))
                        } else {
                                Color(ContextCompat.getColor(context, R.color.pum_navbar_color_light))
                        }
                        val navBarBottom = WindowInsets.navigationBars
                                .asPaddingValues().calculateBottomPadding()
                        // pillAreaHeight = nav bar inset + 8dp bottom padding + 62dp pill
                        val pillAreaHeight = navBarBottom + 8.dp + 62.dp

                        // Gradient topbar - full width, exactly above the pill
                        Box(
                                modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(bottom = pillAreaHeight)
                                        .height(62.dp)
                                        .background(
                                                brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                                Color.Transparent,
                                                                navbarColor.copy(alpha = 0.80f)
                                                        )
                                                )
                                        )
                        )

                        // Pill with icons
                        PumBottomNavigation(
                                visibleTabs = visibleTabs,
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                hazeState = hazeState,
                                modifier = Modifier.align(Alignment.BottomCenter)
                        )
                }
        }
        
        // Changelog dialog
        if (showChangelogDialog) {
            ChangelogDialog(
                changelog = config.changelog,
                appVersion = appVersion,
                widgetCount = widgetCount,
                wallpaperCount = wallpaperCount,
                onDismiss = { showChangelogDialog = false }
            )
        }
        
        // About screen
        if (showAboutDialog) {
            Dialog(
                onDismissRequest = { showAboutDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                AboutScreen(
                    appIcon = config.appIcon,
                    developerLogoUrl = config.developerLogoUrl,
                    developerName = config.developerName,
                    moreAppsUrl = config.moreAppsUrl,
                    moreApps = config.moreApps,
                    moreAppsJsonUrl = config.moreAppsJsonUrl,
                    privacyPolicyUrl = config.privacyPolicyUrl,
                    xIcon = config.xIcon,
                    instagramIcon = config.instagramIcon,
                    youtubeIcon = config.youtubeIcon,
                    facebookIcon = config.facebookIcon,
                    telegramIcon = config.telegramIcon,
                    onNavigateBack = { showAboutDialog = false }
                )
            }
        }
        
        // Settings screen
        if (showSettingsDialog) {
            Dialog(
                onDismissRequest = { showSettingsDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                SettingsScreen(
                    packageName = config.packageName,
                    appVersion = appVersion,
                    updateJsonUrl = config.updateJsonUrl,
                    onNavigateBack = { showSettingsDialog = false }
                )
            }
        }
        } // PumTheme
}
