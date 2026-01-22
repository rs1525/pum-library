package com.akustom15.pum.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import com.akustom15.pum.R
import androidx.compose.foundation.background

/** Card component - todas las imágenes mismo tamaño SIN fondo negro extra */
@Composable
fun ItemCard(
        name: String,
        description: String,
        previewUrl: String? = null,
        appIcon: Int? = null,
        appName: String? = null,
        onApplyClick: () -> Unit = {},
        onClick: () -> Unit = {}
) {
        val context = LocalContext.current
        
        Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp) // Más pequeño
                ) {
                        // Preview container - aspect ratio 16:9 para widgets horizontales
                        Box(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .aspectRatio(16f / 9f) // Proporción horizontal uniforme
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                        ) {
                                if (previewUrl != null) {
                                        // Imagen que ocupa todo el espacio disponible
                                        AsyncImage(
                                                model = previewUrl,
                                                contentDescription = name,
                                                modifier = Modifier.fillMaxSize()
                                                        .padding(8.dp),
                                                contentScale = ContentScale.Fit // Mantiene proporción
                                        )
                                } else {
                                        Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nombre
                        Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Descripción
                        Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Fila inferior
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Icono + nombre app
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (appIcon != null) {
                                                val drawable = remember(appIcon) { context.getDrawable(appIcon) }
                                                drawable?.let { d ->
                                                        val bitmap = remember(d) { d.toBitmap(width = 66, height = 66) }
                                                        Image(
                                                                bitmap = bitmap.asImageBitmap(),
                                                                contentDescription = "App icon",
                                                                modifier = Modifier.size(22.dp)
                                                                        .clip(RoundedCornerShape(4.dp)),
                                                                contentScale = ContentScale.Crop
                                                        )
                                                }
                                                Spacer(modifier = Modifier.width(5.dp))
                                        }

                                        if (appName != null) {
                                                Text(
                                                        text = appName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                        }
                                }

                                // Botón Apply
                                Button(
                                        onClick = {
                                                android.util.Log.d("ItemCard", "Apply: $name")
                                                onApplyClick()
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp),
                                        contentPadding =
                                                PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Apply",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                                text = stringResource(R.string.btn_apply),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}
