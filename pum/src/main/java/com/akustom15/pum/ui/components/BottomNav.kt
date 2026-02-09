package com.akustom15.pum.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.akustom15.pum.R
import com.akustom15.pum.config.PumTab

/** Modern floating bottom navigation bar with pill shape - Telegram style */
@Composable
fun PumBottomNavigation(
        visibleTabs: List<PumTab>,
        selectedTab: PumTab,
        onTabSelected: (PumTab) -> Unit,
        modifier: Modifier = Modifier
) {
        if (visibleTabs.isEmpty()) return

        val context = LocalContext.current
        val isDark = isSystemInDarkTheme()
        val navbarColor = if (isDark) {
                Color(ContextCompat.getColor(context, R.color.pum_navbar_color_dark))
        } else {
                Color(ContextCompat.getColor(context, R.color.pum_navbar_color_light))
        }

        val pillShape = RoundedCornerShape(50)
        val borderColor = if (isDark)
                Color.White.copy(alpha = 0.08f)
        else
                Color.Black.copy(alpha = 0.06f)

        Box(
                modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp)
                        .navigationBarsPadding()
        ) {
                // Pill container - opaque base layer covers any artifact, then semi-transparent overlay
                Box(
                        modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, pillShape)
                                .background(navbarColor.copy(alpha = 0.80f), pillShape)
                                .border(
                                        width = 0.5.dp,
                                        color = borderColor,
                                        shape = pillShape
                                )
                ) {
                        Row(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .height(62.dp)
                                        .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                visibleTabs.forEach { tab ->
                                        val isSelected = selectedTab == tab
                                        val tabColor by animateColorAsState(
                                                targetValue = if (isSelected)
                                                        MaterialTheme.colorScheme.primary
                                                else
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                animationSpec = tween(200),
                                                label = "tabColor"
                                        )
                                        val tabLabel = when (tab) {
                                                PumTab.Widgets -> stringResource(R.string.tab_widgets)
                                                PumTab.Wallpapers -> stringResource(R.string.tab_wallpapers)
                                                PumTab.WallpaperCloud -> stringResource(R.string.tab_wallpaper_cloud)
                                        }

                                        Column(
                                                modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clickable(
                                                                interactionSource = remember { MutableInteractionSource() },
                                                                indication = null
                                                        ) { onTabSelected(tab) }
                                                        .padding(vertical = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                        ) {
                                                // Indicator line for selected tab
                                                Box(
                                                        modifier = Modifier
                                                                .width(24.dp)
                                                                .height(3.dp)
                                                                .background(
                                                                        if (isSelected)
                                                                                MaterialTheme.colorScheme.primary
                                                                        else
                                                                                Color.Transparent,
                                                                        RoundedCornerShape(50)
                                                                )
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Icon(
                                                        imageVector = getTabIcon(tab, isSelected),
                                                        contentDescription = tabLabel,
                                                        tint = tabColor,
                                                        modifier = Modifier.size(22.dp)
                                                )

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                        text = tabLabel,
                                                        color = tabColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected) FontWeight.SemiBold
                                                                else FontWeight.Normal,
                                                        maxLines = 1
                                                )
                                        }
                                }
                        }
                }
        }
}

private fun getTabIcon(tab: PumTab, selected: Boolean): ImageVector {
        return when (tab) {
                PumTab.Widgets -> if (selected) Icons.Filled.Widgets else Icons.Outlined.Widgets
                PumTab.Wallpapers -> if (selected) Icons.Filled.Image else Icons.Outlined.Image
                PumTab.WallpaperCloud -> if (selected) Icons.Filled.Cloud else Icons.Outlined.Cloud
        }
}
