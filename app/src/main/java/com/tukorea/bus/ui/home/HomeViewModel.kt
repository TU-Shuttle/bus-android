package com.tukorea.bus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetNextReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNextReservationUseCase: GetNextReservationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadNextReservation()
    }

    private fun loadNextReservation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getNextReservationUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "예약 정보를 불러오는 중 오류가 발생했습니다."
                    )
                }
                .collect { reservation ->
                    _uiState.value = _uiState.value.copy(
                        nextReservation = reservation,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun updateModalHeight(height: ModalHeight) {
        _uiState.value = _uiState.value.copy(modalHeight = height)
    }

    fun toggleModal() {
        _uiState.value = _uiState.value.copy(
            isModalVisible = !_uiState.value.isModalVisible,
            modalHeight = if (!_uiState.value.isModalVisible) ModalHeight.LOW else ModalHeight.LOW
        )
    }
}
