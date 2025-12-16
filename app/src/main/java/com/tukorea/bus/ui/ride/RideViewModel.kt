package com.tukorea.bus.ui.ride

import androidx.lifecycle.ViewModel
import com.tukorea.bus.ui.home.ModalHeight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 탑승(운행 중) 화면의 상태를 관리하는 ViewModel.
 *
 * 현재는 정적인 UI만 표시하며, 향후 실시간 위치/ETA 연동 시 확장된다.
 */
@HiltViewModel
class RideViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RideUiState())
    val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

    /**
     * 모달 높이를 변경합니다.
     */
    fun updateModalHeight(height: ModalHeight) {
        _uiState.value = _uiState.value.copy(modalHeight = height)
    }
}

