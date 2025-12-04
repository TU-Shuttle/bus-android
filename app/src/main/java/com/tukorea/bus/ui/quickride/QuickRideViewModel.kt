package com.tukorea.bus.ui.quickride

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.repository.CalendarRepository
import com.tukorea.bus.domain.usecase.GetAvailableBusesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickRideViewModel @Inject constructor(
    private val getAvailableBusesUseCase: GetAvailableBusesUseCase,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickRideUiState())
    val uiState: StateFlow<QuickRideUiState> = _uiState.asStateFlow()

    init {
        loadDestinations()
    }
    
    private fun loadDestinations() {
        viewModelScope.launch {
            calendarRepository.getLocations()
                .catch { exception ->
                }
                .collect { locations ->
                    val currentState = _uiState.value
                    _uiState.value = currentState.copy(
                        destinations = locations,
                        currentLocation = if (currentState.currentLocation.isEmpty() && locations.isNotEmpty()) {
                            locations.first()
                        } else {
                            currentState.currentLocation
                        },
                        selectedDestination = if (currentState.selectedDestination.isEmpty() && locations.size > 1) {
                            locations[1]
                        } else if (currentState.selectedDestination.isEmpty() && locations.isNotEmpty()) {
                            locations.first()
                        } else {
                            currentState.selectedDestination
                        }
                    )
                    if (_uiState.value.currentLocation.isNotEmpty() && _uiState.value.selectedDestination.isNotEmpty()) {
                        loadAvailableBuses()
                    }
                }
        }
    }

    fun updateDestination(destination: String) {
        _uiState.value = _uiState.value.copy(selectedDestination = destination)
        loadAvailableBuses()
    }

    fun toggleTimeTable() {
        _uiState.value = _uiState.value.copy(showTimeTable = !_uiState.value.showTimeTable)
    }

    private fun loadAvailableBuses() {
        viewModelScope.launch {
        val state = _uiState.value
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getAvailableBusesUseCase(state.currentLocation, state.selectedDestination)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "버스 목록을 불러오는 중 오류가 발생했습니다."
                    )
                }
                .collect { buses ->
        _uiState.value = _uiState.value.copy(
                        availableBuses = buses,
                        isLoading = false,
                        error = null
        )
                }
        }
    }
}
