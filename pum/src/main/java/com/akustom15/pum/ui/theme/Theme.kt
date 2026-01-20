package com.akustom15.pum.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.akustom15.pum.data.AccentColor
import com.akustom15.pum.data.PumPreferences
import com.akustom15.pum.data.ThemeMode

private fun getDarkColorScheme(accentColor: Color) =
        darkColorScheme(
                primary = accentColor,
                primaryContainer = accentColor.copy(alpha = 0.7f),
                secondary = PumColors.Secondary,
                background = PumColors.Background,
                surface = PumColors.Surface,
                surfaceVariant = PumColors.SurfaceVariant,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = PumColors.OnBackground,
                onSurface = PumColors.OnSurface,
                onSurfaceVariant = PumColors.OnSurfaceVariant,
                error = PumColors.Error
        )

private fun getLightColorScheme(accentColor: Color) =
        lightColorScheme(
                primary = accentColor,
                primaryContainer = accentColor.copy(alpha = 0.3f),
                secondary = PumColors.Secondary,
                background = Color(0xFFF5F5F5),
                surface = Color.White,
                surfaceVariant = Color(0xFFE8E8E8),
                onPrimary = Color.White,
                onSecondary = Color.White,
                onBackground = Color(0xFF1C1C1E),
                onSurface = Color(0xFF1C1C1E),
                onSurfaceVariant = Color(0xFF636366),
                error = PumColors.Error
        )

/** PUM theme with support for light/dark/system themes and accent colors */
@Composable
fun PumTheme(
        content: @Composable () -> Unit
) {
        val context = LocalContext.current
        val preferences = remember { PumPreferences.getInstance(context) }
        
        val themeMode = preferences.themeMode.collectAsState().value
        val accentColorPref = preferences.accentColor.collectAsState().value
        val accentColor = Color(accentColorPref.colorValue)
        
        val systemDarkTheme = isSystemInDarkTheme()
        
        val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDarkTheme
        }
        
        val colorScheme = if (useDarkTheme) {
                getDarkColorScheme(accentColor)
        } else {
                getLightColorScheme(accentColor)
        }
        
        // Update PumColors.Primary dynamically
        PumColors.updatePrimary(accentColor)
        
        val view = LocalView.current
        if (!view.isInEditMode) {
                SideEffect {
                        val window = (view.context as Activity).window
                        window.statusBarColor = colorScheme.background.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
                }
        }

        MaterialTheme(colorScheme = colorScheme, typography = PumTypography, content = content)
}
