package com.akustom15.pum.data

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enum para el tema de la aplicación
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Enum para el idioma de la aplicación
 */
enum class AppLanguage(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English"),
    FRENCH("fr", "Français"),
    GERMAN("de", "Deutsch"),
    PORTUGUESE("pt", "Português (Brasil)"),
    ARABIC("ar", "العربية"),
    ITALIAN("it", "Italiano"),
    HINDI("hi", "हिन्दी"),
    INDONESIAN("in", "Bahasa Indonesia"),
    CHINESE("zh", "简体中文"),
    SYSTEM("system", "Automático / Auto")
}

/**
 * Enum para los colores de acento disponibles
 * DEFAULT usa el color definido en colors.xml de la app (pum_accent_color)
 */
enum class AccentColor(val colorValue: Long, val displayName: String, val isDefault: Boolean = false) {
    DEFAULT(0xFF2196F3, "Predeterminado", true), // Color se obtiene de resources
    BLUE(0xFF2196F3, "Azul"),
    PURPLE(0xFF9C27B0, "Púrpura"),
    GREEN(0xFF4CAF50, "Verde"),
    ORANGE(0xFFFF9800, "Naranja"),
    RED(0xFFF44336, "Rojo"),
    TEAL(0xFF009688, "Turquesa"),
    PINK(0xFFE91E63, "Rosa"),
    CYAN(0xFF00BCD4, "Cian")
}

/**
 * Gestor de preferencias de PUM usando SharedPreferences
 */
class PumPreferences private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // StateFlows para observar cambios
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _appLanguage = MutableStateFlow(getAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()
    
    private val _accentColor = MutableStateFlow(getAccentColor())
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()
    
    private val _downloadOnWifiOnly = MutableStateFlow(getDownloadOnWifiOnly())
    val downloadOnWifiOnly: StateFlow<Boolean> = _downloadOnWifiOnly.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(getNotificationsEnabled())
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    // Tema
    fun getThemeMode(): ThemeMode {
        val value = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(value)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }
    
    // Idioma
    fun getAppLanguage(): AppLanguage {
        val value = prefs.getString(KEY_APP_LANGUAGE, AppLanguage.SYSTEM.name) ?: AppLanguage.SYSTEM.name
        return try {
            AppLanguage.valueOf(value)
        } catch (e: Exception) {
            AppLanguage.SYSTEM
        }
    }
    
    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language.name).apply()
        _appLanguage.value = language
        
        // Aplicar el idioma usando AppCompatDelegate (funciona sin reiniciar)
        val localeList = when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(language.code)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    // Color de acento
    fun getAccentColor(): AccentColor {
        val value = prefs.getString(KEY_ACCENT_COLOR, AccentColor.DEFAULT.name) ?: AccentColor.DEFAULT.name
        return try {
            AccentColor.valueOf(value)
        } catch (e: Exception) {
            AccentColor.DEFAULT
        }
    }
    
    fun setAccentColor(color: AccentColor) {
        prefs.edit().putString(KEY_ACCENT_COLOR, color.name).apply()
        _accentColor.value = color
    }
    
    // Descargar solo en WiFi
    fun getDownloadOnWifiOnly(): Boolean {
        return prefs.getBoolean(KEY_DOWNLOAD_WIFI_ONLY, false)
    }
    
    fun setDownloadOnWifiOnly(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DOWNLOAD_WIFI_ONLY, enabled).apply()
        _downloadOnWifiOnly.value = enabled
    }
    
    // Notificaciones
    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }
    
    // Limpiar caché (retorna true si se limpió correctamente)
    fun clearImageCache(context: Context): Boolean {
        return try {
            val cacheDir = context.cacheDir
            cacheDir.deleteRecursively()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        private const val PREFS_NAME = "pum_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_APP_LANGUAGE = "app_language"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_DOWNLOAD_WIFI_ONLY = "download_wifi_only"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        
        @Volatile
        private var instance: PumPreferences? = null
        
        fun getInstance(context: Context): PumPreferences {
            return instance ?: synchronized(this) {
                instance ?: PumPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
