package com.tukorea.bus.ui.temp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetTempListUseCase
import com.tukorea.bus.domain.util.Result
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
            _state.value = TempUiState(isLoading = true, error = null)

            when (val result = getTempListUseCase()) {
                is Result.Success -> {
                    _state.value = TempUiState(
                        isLoading = false, temps = result.data
                    )
                }

                is Result.Error -> {
                    _state.value = TempUiState(
                        isLoading = false, error = result.error.getMessage()
                    )
                }
            }
        }
    }
}