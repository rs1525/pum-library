package com.akustom15.pum.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

/** App header with LARGE circular icon + app name + subtitle Exactly like Lunex design */
@Composable
fun AppHeader(
        appName: String,
        appSubtitle: String = "",
        @DrawableRes appIcon: Int? = null,
        modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
    ) {
        // LARGE circular app icon (supports adaptive-icons)
        if (appIcon != null) {
            val drawable = remember(appIcon) { context.getDrawable(appIcon) }
            drawable?.let { d ->
                val bitmap = remember(d) { d.toBitmap(width = 240, height = 240) }
                Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "App icon",
                        modifier = Modifier.size(80.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
        }

        // App name and subtitle
        Column {
            Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
            )

            if (appSubtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = appSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        fontSize = 14.sp
                )
            }
        }
    }
}
