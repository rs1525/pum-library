package com.akustom15.pum.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Centralized color palette for PUM library These colors can be overridden by implementing apps */
object PumColors {
    // Background colors
    val Background = Color(0xFF1C1C1E) // Dark background
    val Surface = Color(0xFF2C2C2E) // Slightly lighter surface
    val SurfaceVariant = Color(0xFF2F2F32) // Card backgrounds
    val PreviewBackground = Color(0xFF2F2F32) // Preview area inside cards (same as card)

    // Primary/Accent colors (configurable dynamically)
    private val DefaultPrimary = Color(0xFF2196F3) // Blue accent color (default)
    var Primary by mutableStateOf(DefaultPrimary)
        private set
    
    val PrimaryVariant: Color
        get() = Primary.copy(alpha = 0.7f)
    
    val Secondary = Color(0xFF5AC8FA) // Light blue

    // Text colors
    val OnBackground = Color(0xFFFFFFFF) // White text on dark background
    val OnSurface = Color(0xFFFFFFFF) // White text
    val OnSurfaceVariant = Color(0xFFAEAEB2) // Gray text

    // Interactive elements
    val SearchBarActive: Color
        get() = Primary
    val SearchBarInactive = SurfaceVariant // Search bar color when inactive
    val SearchBarBorder = Color(0xFF48484A) // Border color

    // Status colors
    val Error = Color(0xFFFF453A)
    val Success = Color(0xFF32D74B)
    val Warning = Color(0xFFFFD60A)
    
    // Function to update primary color dynamically
    fun updatePrimary(color: Color) {
        Primary = color
    }
}
