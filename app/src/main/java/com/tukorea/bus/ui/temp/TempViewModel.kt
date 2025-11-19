package com.tukorea.bus.ui.temp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetTempListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TempViewModel @Inject constructor(
    private val getTempListUseCase: GetTempListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TempUiState())
    val state: StateFlow<TempUiState> = _state

    fun loadTemps() {
        viewModelScope.launch {
            _state.value = TempUiState(isLoading = true)
            try {
                val temps = getTempListUseCase()
                _state.value = TempUiState(temps = temps)
            } catch (e: Exception) {
                _state.value = TempUiState(error = e.message ?: "Unknown error")
            }
        }
    }
}