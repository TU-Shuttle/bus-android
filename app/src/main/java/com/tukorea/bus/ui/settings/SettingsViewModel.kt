package com.tukorea.bus.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private companion object {
        private const val TAG = "SettingsViewModel"
    }

    fun onPushNotificationChanged(enabled: Boolean) {
        Log.d(TAG, "푸시 알림 설정 변경: $enabled")
        _uiState.update { state ->
            state.copy(isPushNotificationEnabled = enabled)
        }
    }

    fun onReservationNotificationChanged(enabled: Boolean) {
        Log.d(TAG, "예약 알림 설정 변경: $enabled")
        _uiState.update { state ->
            state.copy(isReservationNotificationEnabled = enabled)
        }
    }
}
