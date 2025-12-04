package com.tukorea.bus.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.repository.CalendarRepository
import com.tukorea.bus.domain.usecase.AddReservationUseCase
import com.tukorea.bus.domain.usecase.DeleteReservationUseCase
import com.tukorea.bus.domain.usecase.GetAllReservationsUseCase
import com.tukorea.bus.domain.usecase.UpdateReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getAllReservationsUseCase: GetAllReservationsUseCase,
    private val addReservationUseCase: AddReservationUseCase,
    private val updateReservationUseCase: UpdateReservationUseCase,
    private val deleteReservationUseCase: DeleteReservationUseCase,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCalendarData()
        loadReservations()
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            calendarRepository.getDays()
                .catch { _ ->
                }
                .collect { days ->
                    _uiState.value = _uiState.value.copy(days = days)
                }
        }

        viewModelScope.launch {
            calendarRepository.getTimes()
                .catch { _ ->
                }
                .collect { times ->
                    _uiState.value = _uiState.value.copy(times = times)
                }
        }

        viewModelScope.launch {
            calendarRepository.getLocations()
                .catch { _ ->
                }
                .collect { locations ->
                    val currentState = _uiState.value
                    _uiState.value = _uiState.value.copy(
                        locations = locations,
                        selectedFrom = if (currentState.selectedFrom.isEmpty() && locations.isNotEmpty()) {
                            locations.first()
                        } else {
                            currentState.selectedFrom
                        },
                        selectedTo = if (currentState.selectedTo.isEmpty() && locations.size > 1) {
                            locations[1]
                        } else if (currentState.selectedTo.isEmpty() && locations.isNotEmpty()) {
                            locations.first()
                        } else {
                            currentState.selectedTo
                        }
                    )
                }
        }
    }

    private fun loadReservations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            getAllReservationsUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "예약 목록을 불러오는 중 오류가 발생했습니다."
                    )
                }
                .collect { reservations ->
                    _uiState.value = _uiState.value.copy(
                        reservations = reservations,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    fun toggleDay(day: String) {
        val currentDays = _uiState.value.selectedDays
        _uiState.value = _uiState.value.copy(
            selectedDays = if (currentDays.contains(day)) {
                currentDays.filter { it != day }
            } else {
                currentDays + day
            }
        )
    }

    fun selectTime(time: String) {
        _uiState.value = _uiState.value.copy(selectedTime = time)
    }

    fun selectFrom(from: String) {
        _uiState.value = _uiState.value.copy(selectedFrom = from)
    }

    fun selectTo(to: String) {
        _uiState.value = _uiState.value.copy(selectedTo = to)
    }

    fun addReservation() {
        viewModelScope.launch {
            val state = _uiState.value

            // 유효성 검사
            when {
                state.selectedDays.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "요일을 선택해주세요"
                    )
                    return@launch
                }
                state.selectedTime.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "시간을 선택해주세요"
                    )
                    return@launch
                }
                state.selectedFrom == state.selectedTo -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "출발지와 도착지가 같습니다"
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val newReservation = Reservation(
                    id = 0,
                    days = state.selectedDays,
                    time = state.selectedTime,
                    from = state.selectedFrom,
                    to = state.selectedTo
                )
                addReservationUseCase(newReservation)
                val currentLocations = _uiState.value.locations
                _uiState.value = _uiState.value.copy(
                    selectedDays = emptyList(),
                    selectedTime = "",
                    selectedFrom = if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    selectedTo = if (currentLocations.size > 1) currentLocations[1] else if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "예약 추가 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun showDeleteDialog(id: Long) {
        _uiState.value = _uiState.value.copy(deletingReservationId = id)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(deletingReservationId = null)
    }

    fun deleteReservation(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                deletingReservationId = null
            )

            try {
                deleteReservationUseCase(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "예약 삭제 중 오류가 발생했습니다."
                )
            }
        }
    }

    fun startEditReservation(reservation: Reservation) {
        _uiState.value = _uiState.value.copy(
            editingReservationId = reservation.id,
            selectedDays = reservation.days,
            selectedTime = reservation.time,
            selectedFrom = reservation.from,
            selectedTo = reservation.to
        )
    }

    fun cancelEdit() {
        val currentLocations = _uiState.value.locations
        _uiState.value = _uiState.value.copy(
            editingReservationId = null,
            selectedDays = emptyList(),
            selectedTime = "",
            selectedFrom = if (currentLocations.isNotEmpty()) currentLocations.first() else "",
            selectedTo = if (currentLocations.size > 1) currentLocations[1] else if (currentLocations.isNotEmpty()) currentLocations.first() else ""
        )
    }

    fun updateReservation() {
        viewModelScope.launch {
            val state = _uiState.value
            val editingId = state.editingReservationId ?: return@launch

            // 유효성 검사
            when {
                state.selectedDays.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "요일을 선택해주세요"
                    )
                    return@launch
                }
                state.selectedTime.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "시간을 선택해주세요"
                    )
                    return@launch
                }
                state.selectedFrom == state.selectedTo -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "출발지와 도착지가 같습니다"
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val updatedReservation = Reservation(
                    id = editingId,
                    days = state.selectedDays,
                    time = state.selectedTime,
                    from = state.selectedFrom,
                    to = state.selectedTo
                )
                updateReservationUseCase(updatedReservation)

                val currentLocations = _uiState.value.locations
                _uiState.value = _uiState.value.copy(
                    editingReservationId = null,
                    selectedDays = emptyList(),
                    selectedTime = "",
                    selectedFrom = if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    selectedTo = if (currentLocations.size > 1) currentLocations[1] else if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "예약 수정 중 오류가 발생했습니다."
                )
            }
        }
    }

}
