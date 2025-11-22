package com.tukorea.bus.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.InitializeAppUseCase
import com.tukorea.bus.domain.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
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
            when (val result = initializeAppUseCase()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isInitialized = true,
                        error = null
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isInitialized = true,
                        error = result.error.getMessage()
                    )
                }
            }
        }
    }
}
