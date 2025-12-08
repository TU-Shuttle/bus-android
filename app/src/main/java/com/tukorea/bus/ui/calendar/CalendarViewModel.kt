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

    private val dayOrder = listOf("월", "화", "수", "목", "금", "토", "일")

    private fun sortDays(days: List<String>): List<String> {
        return days.sortedBy { dayOrder.indexOf(it) }
    }

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
                    // 초기 로드 시 시간표 로드
                    loadTimesForRoute()
                }
        }
    }

    private fun loadTimesForRoute() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedFrom.isNotEmpty() && state.selectedTo.isNotEmpty()) {
                calendarRepository.getTimesByRoute(state.selectedFrom, state.selectedTo)
                    .catch { _ ->
                    }
                    .collect { times ->
                        _uiState.value = _uiState.value.copy(times = times)
                    }
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
        val newDays = if (currentDays.contains(day)) {
            currentDays.filter { it != day }
        } else {
            currentDays + day
        }

        _uiState.value = _uiState.value.copy(
            selectedDays = newDays,
            selectedTimes = if (newDays.isEmpty()) emptyList() else _uiState.value.selectedTimes
        )
    }

    fun selectTime(time: String) {
        if (_uiState.value.selectedDays.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "먼저 요일을 선택해주세요"
            )
            return
        }

        val currentTimes = _uiState.value.selectedTimes

        val newTimes = if (currentTimes.contains(time)) {
            // 이미 선택된 시간을 클릭하면 해제
            currentTimes.filter { it != time }
        } else {
            // 새로운 시간 선택
            if (currentTimes.size >= 2) {
                // 2개 이상이면 가장 오래된 것 제거
                currentTimes.drop(1) + time
            } else {
                currentTimes + time
            }
        }

        _uiState.value = _uiState.value.copy(
            selectedTimes = newTimes
        )
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun selectFrom(from: String) {
        _uiState.value = _uiState.value.copy(
            selectedFrom = from,
            selectedTimes = emptyList() // 출발지 변경시 선택된 시간 초기화
        )
        loadTimesForRoute()
    }

    fun selectTo(to: String) {
        _uiState.value = _uiState.value.copy(
            selectedTo = to,
            selectedTimes = emptyList() // 도착지 변경시 선택된 시간 초기화
        )
        loadTimesForRoute()
    }

    fun addReservation() {
        viewModelScope.launch {
            val state = _uiState.value

            // 유효성 검사
            when {
                state.selectedFrom == state.selectedTo -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "출발지와 도착지가 같습니다"
                    )
                    return@launch
                }
                state.selectedDays.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "요일을 선택해주세요"
                    )
                    return@launch
                }
                state.selectedTimes.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "시간을 선택해주세요"
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val newReservation = Reservation(
                    id = 0,
                    days = sortDays(state.selectedDays),
                    time = state.selectedTimes.first(),
                    from = state.selectedFrom,
                    to = state.selectedTo
                )
                addReservationUseCase(newReservation)
                val currentLocations = _uiState.value.locations
                _uiState.value = _uiState.value.copy(
                    selectedDays = emptyList(),
                    selectedTimes = emptyList(),
                    selectedFrom = if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    selectedTo = if (currentLocations.size > 1) currentLocations[1] else if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    isLoading = false,
                    errorMessage = null
                )
                loadTimesForRoute()
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
            selectedTimes = listOf(reservation.time),
            selectedFrom = reservation.from,
            selectedTo = reservation.to
        )
    }

    fun cancelEdit() {
        val currentLocations = _uiState.value.locations
        _uiState.value = _uiState.value.copy(
            editingReservationId = null,
            selectedDays = emptyList(),
            selectedTimes = emptyList(),
            selectedFrom = if (currentLocations.isNotEmpty()) currentLocations.first() else "",
            selectedTo = if (currentLocations.size > 1) currentLocations[1] else if (currentLocations.isNotEmpty()) currentLocations.first() else ""
        )
        loadTimesForRoute()
    }

    fun updateReservation() {
        viewModelScope.launch {
            val state = _uiState.value
            val editingId = state.editingReservationId ?: return@launch

            // 유효성 검사
            when {
                state.selectedFrom == state.selectedTo -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "출발지와 도착지가 같습니다"
                    )
                    return@launch
                }
                state.selectedDays.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "요일을 선택해주세요"
                    )
                    return@launch
                }
                state.selectedTimes.isEmpty() -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "시간을 선택해주세요"
                    )
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val updatedReservation = Reservation(
                    id = editingId,
                    days = sortDays(state.selectedDays),
                    time = state.selectedTimes.first(),
                    from = state.selectedFrom,
                    to = state.selectedTo
                )
                updateReservationUseCase(updatedReservation)

                val currentLocations = _uiState.value.locations
                _uiState.value = _uiState.value.copy(
                    editingReservationId = null,
                    selectedDays = emptyList(),
                    selectedTimes = emptyList(),
                    selectedFrom = if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    selectedTo = if (currentLocations.size > 1) currentLocations[1] else if (currentLocations.isNotEmpty()) currentLocations.first() else "",
                    isLoading = false,
                    errorMessage = null
                )
                loadTimesForRoute()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "예약 수정 중 오류가 발생했습니다."
                )
            }
        }
    }

}
