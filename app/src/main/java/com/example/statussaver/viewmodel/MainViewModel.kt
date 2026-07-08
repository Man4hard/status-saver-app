package com.example.statussaver.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statussaver.data.SettingsRepository
import com.example.statussaver.data.StatusModel
import com.example.statussaver.data.StatusRepository
import com.example.statussaver.data.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainUiState {
    object Loading : MainUiState()
    object PermissionRequired : MainUiState()
    data class Success(val statuses: List<StatusModel>) : MainUiState()
    data class Error(val message: String) : MainUiState()
}

sealed class MainEvent {
    data class ShowToast(val message: String) : MainEvent()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val statusRepository: StatusRepository,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _savedStatuses = MutableStateFlow<List<StatusModel>>(emptyList())
    val savedStatuses: StateFlow<List<StatusModel>> = _savedStatuses.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val events = _events.asSharedFlow()

    private var currentTreeUri: Uri? = null

    init {
        viewModelScope.launch {
            settingsRepository.waTreeUri.collectLatest { uri ->
                if (uri != null) {
                    currentTreeUri = uri
                    loadStatuses(uri)
                } else {
                    _uiState.value = MainUiState.PermissionRequired
                }
            }
        }
    }

    private suspend fun loadStatuses(uri: Uri) {
        _uiState.value = MainUiState.Loading
        try {
            val statuses = statusRepository.getStatuses(uri)
            _uiState.value = MainUiState.Success(statuses)
        } catch (e: Exception) {
            _uiState.value = MainUiState.Error(e.message ?: "Failed to load statuses")
        }
    }

    fun refresh() {
        loadSavedStatuses()
        currentTreeUri?.let { uri ->
            viewModelScope.launch {
                loadStatuses(uri)
            }
        }
    }

    fun loadSavedStatuses() {
        viewModelScope.launch {
            _savedStatuses.value = mediaRepository.getSavedMedia()
        }
    }

    fun saveUri(uriString: String) {
        viewModelScope.launch {
            settingsRepository.saveWaTreeUri(uriString)
        }
    }

    fun saveStatus(status: StatusModel) {
        viewModelScope.launch {
            val result = mediaRepository.saveMediaToGallery(status)
            if (result.isSuccess) {
                _events.emit(MainEvent.ShowToast(result.getOrNull() ?: "Saved"))
                loadSavedStatuses()
            } else {
                _events.emit(MainEvent.ShowToast(result.exceptionOrNull()?.message ?: "Failed to save"))
            }
        }
    }
}
