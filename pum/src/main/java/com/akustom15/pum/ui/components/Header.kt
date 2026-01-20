package com.akustom15.pum.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Header component with app icon, title, subtitle, search and menu buttons */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumHeader(
        appName: String,
        appSubtitle: String,
        appIcon: Int?,
        onSearchClick: () -> Unit = {},
        onMenuClick: () -> Unit = {}
) {
    TopAppBar(
            title = {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Icon + Title + Subtitle
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.weight(1f)
                    ) {
                        // App Icon
                        if (appIcon != null) {
                            Image(
                                    painter = painterResource(id = appIcon),
                                    contentDescription = "App Icon",
                                    modifier = Modifier.size(56.dp).clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        // Title and Subtitle
                        Column {
                            Text(
                                    text = appName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                            )

                            if (appSubtitle.isNotEmpty()) {
                                Text(
                                        text = appSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Right side: Search + Menu
                    Row(
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = onMenuClick) {
                            Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            colors =
                    TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                    ),
            modifier = Modifier.height(80.dp)
    )
}
