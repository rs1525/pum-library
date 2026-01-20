package com.akustom15.pum.ui.screens.about

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * ViewModel para la pantalla About
 */
class AboutViewModel : ViewModel() {
    // Estado para el idioma actual
    private val _currentLanguage = MutableStateFlow(Locale.getDefault())
    val currentLanguage: StateFlow<Locale> = _currentLanguage.asStateFlow()

    /**
     * Actualiza el idioma al valor actual del sistema
     */
    fun updateLanguage() {
        _currentLanguage.value = Locale.getDefault()
    }
}
