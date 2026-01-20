package com.akustom15.pum.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akustom15.pum.data.CloudWallpaperRepository
import com.akustom15.pum.model.CloudWallpaperItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing cloud wallpapers UI state
 */
class CloudWallpaperViewModel(
    private val repository: CloudWallpaperRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CloudWallpaperUiState>(CloudWallpaperUiState.Loading)
    val uiState: StateFlow<CloudWallpaperUiState> = _uiState.asStateFlow()
    
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()
    
    init {
        loadWallpapers()
    }
    
    fun loadWallpapers() {
        viewModelScope.launch {
            _uiState.value = CloudWallpaperUiState.Loading
            try {
                val wallpapers = repository.getWallpapers()
                if (wallpapers.isEmpty()) {
                    _uiState.value = CloudWallpaperUiState.Empty
                } else {
                    _uiState.value = CloudWallpaperUiState.Success(wallpapers)
                }
            } catch (e: Exception) {
                _uiState.value = CloudWallpaperUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun downloadWallpaper(wallpaper: CloudWallpaperItem) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Loading
            try {
                repository.downloadWallpaper(wallpaper.url, wallpaper.name)
                _downloadState.value = DownloadState.Success
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(e.message ?: "Download failed")
            }
        }
    }
    
    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }
    
    /**
     * Factory for creating CloudWallpaperViewModel with repository
     */
    class Factory(
        private val application: Application,
        private val cloudWallpapersUrl: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repository = CloudWallpaperRepository(
                application.applicationContext,
                cloudWallpapersUrl
            )
            return CloudWallpaperViewModel(repository) as T
        }
    }
}

/**
 * UI state for cloud wallpapers screen
 */
sealed class CloudWallpaperUiState {
    data object Loading : CloudWallpaperUiState()
    data object Empty : CloudWallpaperUiState()
    data class Success(val wallpapers: List<CloudWallpaperItem>) : CloudWallpaperUiState()
    data class Error(val message: String) : CloudWallpaperUiState()
}

/**
 * Download operation state
 */
sealed class DownloadState {
    data object Idle : DownloadState()
    data object Loading : DownloadState()
    data object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}
