package com.tukorea.bus.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetAppVersionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppVersionUseCase: GetAppVersionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAppVersion()
    }

    private fun loadAppVersion() {
        viewModelScope.launch {
            val version = getAppVersionUseCase()
            _uiState.update { state ->
                state.copy(appVersion = version)
            }
        }
    }

    fun onPushNotificationChanged(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(isPushNotificationEnabled = enabled)
        }
    }

    fun onReservationNotificationChanged(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(isReservationNotificationEnabled = enabled)
        }
    }
}
