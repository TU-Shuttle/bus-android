package com.tukorea.bus.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.InitializeAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val initializeAppUseCase: InitializeAppUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state

    fun initialize() {
        viewModelScope.launch {
            try {
                delay(1000L)
                initializeAppUseCase()
                _state.value = _state.value.copy(isInitialized = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Initialization failed",
                    isInitialized = true
                )
            }
        }
    }
}
