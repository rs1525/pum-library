package com.akustom15.pum.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Diálogo de actualización disponible
 * 
 * Uso en Compose:
 * ```
 * var showUpdateDialog by remember { mutableStateOf(false) }
 * var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
 * 
 * LaunchedEffect(Unit) {
 *     updateInfo = UpdateChecker.checkForUpdate(context, "https://tu-servidor.com/version.json")
 *     if (updateInfo?.isUpdateAvailable == true) {
 *         showUpdateDialog = true
 *     }
 * }
 * 
 * if (showUpdateDialog && updateInfo != null) {
 *     UpdateDialog(
 *         updateInfo = updateInfo!!,
 *         onDismiss = { showUpdateDialog = false },
 *         onUpdate = { /* navegar a Play Store */ }
 *     )
 * }
 * ```
 */
@Composable
fun UpdateDialog(
    updateInfo: UpdateChecker.UpdateInfo,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit = {},
    title: String = "¡Actualización disponible!",
    updateButtonText: String = "Actualizar",
    laterButtonText: String = "Más tarde"
) {
    val context = LocalContext.current
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Versión ${updateInfo.versionName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (updateInfo.changelog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = updateInfo.changelog,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Start
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.updateUrl))
                        context.startActivity(intent)
                        onUpdate()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(updateButtonText)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = {
                        UpdateChecker.dismissVersion(context, updateInfo.versionCode)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(laterButtonText)
                }
            }
        }
    }
}
