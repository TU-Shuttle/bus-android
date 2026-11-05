package com.tukorea.bus.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.InitializeAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/** 시간표를 미리 받아 둔다. 오프라인이어도 저장된 시간표로 쓰면 되므로 실패해도 메인으로 넘어간다. */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val initializeApp: InitializeAppUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            withTimeoutOrNull(INIT_TIMEOUT_MS) { initializeApp() }
            _uiState.value = SplashUiState(isInitialized = true)
        }
    }

    private companion object {
        const val INIT_TIMEOUT_MS = 2_500L
    }
}
