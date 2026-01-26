package com.akustom15.pum.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.akustom15.pum.R
import com.akustom15.pum.data.AccentColor
import com.akustom15.pum.data.AppLanguage
import com.akustom15.pum.data.GridColumns
import com.akustom15.pum.data.PumPreferences
import com.akustom15.pum.data.ThemeMode
import com.akustom15.pum.ui.theme.PumTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    packageName: String,
    appVersion: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember { PumPreferences.getInstance(context) }
    
    // Estados observados
    val themeMode by preferences.themeMode.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val accentColor by preferences.accentColor.collectAsState()
    val gridColumns by preferences.gridColumns.collectAsState()
    val downloadOnWifiOnly by preferences.downloadOnWifiOnly.collectAsState()
    val notificationsEnabled by preferences.notificationsEnabled.collectAsState()
    
    // Estados para diálogos
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAccentColorDialog by remember { mutableStateOf(false) }
    var showGridColumnsDialog by remember { mutableStateOf(false) }
    
    // Manejar botón atrás
    BackHandler {
        onNavigateBack()
    }
    
    PumTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.about_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Sección: Apariencia
                SettingsSection(title = stringResource(R.string.settings_appearance)) {
                    // Tema
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.settings_theme),
                        subtitle = when (themeMode) {
                            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        },
                        onClick = { showThemeDialog = true }
                    )
                    
                    // Color de acento - usar pum_accent_color cuando es DEFAULT
                    val displayColor = if (accentColor == AccentColor.DEFAULT) {
                        Color(ContextCompat.getColor(context, R.color.pum_accent_color))
                    } else {
                        Color(accentColor.colorValue)
                    }
                    SettingsItemWithColor(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.settings_accent_color),
                        subtitle = accentColor.displayName,
                        color = displayColor,
                        onClick = { showAccentColorDialog = true }
                    )
                    
                    // Vista de cuadrícula (columnas)
                    SettingsItem(
                        icon = Icons.Default.GridView,
                        title = stringResource(R.string.settings_grid_view),
                        subtitle = when (gridColumns) {
                            GridColumns.ONE -> stringResource(R.string.settings_one_column)
                            GridColumns.TWO -> stringResource(R.string.settings_two_columns)
                        },
                        onClick = { showGridColumnsDialog = true }
                    )
                }
                
                // Sección: Idioma
                SettingsSection(title = stringResource(R.string.settings_language)) {
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_app_language),
                        subtitle = appLanguage.displayName,
                        onClick = { showLanguageDialog = true }
                    )
                }
                
                // Sección: Datos y Caché
                SettingsSection(title = stringResource(R.string.settings_data_cache)) {
                    val cacheCleared = stringResource(R.string.settings_cache_cleared)
                    val cacheError = stringResource(R.string.settings_cache_error)
                    SettingsItem(
                        icon = Icons.Default.DeleteSweep,
                        title = stringResource(R.string.settings_clear_cache),
                        subtitle = stringResource(R.string.settings_clear_cache_desc),
                        onClick = {
                            val success = preferences.clearImageCache(context)
                            val message = if (success) cacheCleared else cacheError
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                    
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Wifi,
                        title = stringResource(R.string.settings_download_wifi),
                        subtitle = stringResource(R.string.settings_download_wifi_desc),
                        checked = downloadOnWifiOnly,
                        onCheckedChange = { preferences.setDownloadOnWifiOnly(it) }
                    )
                }
                
                // Sección: Notificaciones
                SettingsSection(title = stringResource(R.string.settings_notifications)) {
                    SettingsItemWithSwitch(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_notifications),
                        subtitle = stringResource(R.string.settings_notifications_desc),
                        checked = notificationsEnabled,
                        onCheckedChange = { preferences.setNotificationsEnabled(it) }
                    )
                }
                
                // Sección: Información
                SettingsSection(title = stringResource(R.string.settings_info)) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_version),
                        subtitle = appVersion,
                        onClick = { }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.settings_rate),
                        subtitle = stringResource(R.string.settings_rate_desc),
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                                context.startActivity(intent)
                            }
                        }
                    )
                    
                    val noEmailApp = stringResource(R.string.settings_no_email_app)
                    SettingsItem(
                        icon = Icons.Default.BugReport,
                        title = stringResource(R.string.settings_report),
                        subtitle = stringResource(R.string.settings_report_desc),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:akustom15@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Reporte de problema - $packageName")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, noEmailApp, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // Diálogo de tema
    val themeLight = stringResource(R.string.settings_theme_light)
    val themeDark = stringResource(R.string.settings_theme_dark)
    val themeSystem = stringResource(R.string.settings_theme_system)
    
    if (showThemeDialog) {
        SelectionDialog(
            title = stringResource(R.string.settings_select_theme),
            options = ThemeMode.entries.map { mode ->
                when (mode) {
                    ThemeMode.LIGHT -> themeLight
                    ThemeMode.DARK -> themeDark
                    ThemeMode.SYSTEM -> themeSystem
                }
            },
            selectedIndex = ThemeMode.entries.indexOf(themeMode),
            onSelect = { index ->
                preferences.setThemeMode(ThemeMode.entries[index])
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Diálogo de idioma
    if (showLanguageDialog) {
        SelectionDialog(
            title = stringResource(R.string.settings_select_language),
            options = AppLanguage.entries.map { it.displayName },
            selectedIndex = AppLanguage.entries.indexOf(appLanguage),
            onSelect = { index ->
                preferences.setAppLanguage(AppLanguage.entries[index])
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    // Diálogo de color de acento
    if (showAccentColorDialog) {
        AccentColorDialog(
            selectedColor = accentColor,
            onSelect = { color ->
                preferences.setAccentColor(color)
                showAccentColorDialog = false
            },
            onDismiss = { showAccentColorDialog = false }
        )
    }
    
    // Diálogo de columnas
    val oneColumn = stringResource(R.string.settings_one_column)
    val twoColumns = stringResource(R.string.settings_two_columns)
    
    if (showGridColumnsDialog) {
        SelectionDialog(
            title = stringResource(R.string.settings_grid_view),
            options = listOf(oneColumn, twoColumns),
            selectedIndex = GridColumns.entries.indexOf(gridColumns),
            onSelect = { index ->
                preferences.setGridColumns(GridColumns.entries[index])
                showGridColumnsDialog = false
            },
            onDismiss = { showGridColumnsDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsItemWithColor(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

@Composable
private fun SettingsItemWithSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AccentColorDialog(
    selectedColor: AccentColor,
    onSelect: (AccentColor) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // Color de DEFAULT desde recursos de la app
    val defaultColorFromResources = Color(ContextCompat.getColor(context, R.color.pum_accent_color))
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_select_accent_color),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                AccentColor.entries.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { color ->
                            // Usar el color de recursos para DEFAULT
                            val displayColor = if (color == AccentColor.DEFAULT) {
                                defaultColorFromResources
                            } else {
                                Color(color.colorValue)
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(displayColor)
                                    .clickable { onSelect(color) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (color == selectedColor) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp)
    )
}
