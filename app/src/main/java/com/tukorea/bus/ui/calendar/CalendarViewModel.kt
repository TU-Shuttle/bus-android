package com.tukorea.bus.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.error.ValidationError
import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.domain.usecase.AddReservationUseCase
import com.tukorea.bus.domain.usecase.DeleteReservationUseCase
import com.tukorea.bus.domain.usecase.GetAllReservationsUseCase
import com.tukorea.bus.domain.usecase.GetDaysUseCase
import com.tukorea.bus.domain.usecase.GetLocationsUseCase
import com.tukorea.bus.domain.usecase.GetDefaultLocationsUseCase
import com.tukorea.bus.domain.usecase.GetTimesByRouteUseCase
import com.tukorea.bus.domain.usecase.SortDaysUseCase
import com.tukorea.bus.domain.usecase.UpdateReservationUseCase
import com.tukorea.bus.domain.usecase.ValidateReservationUseCase
import com.tukorea.bus.ui.common.ErrorMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 예약 생성/수정/삭제 + 캘린더용 요일/시간/장소 데이터를 관리하는 ViewModel.
 *
 * - 캘린더/QuickRide가 공유하는 예약 정보의 단일 진입점 역할을 한다.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getAllReservationsUseCase: GetAllReservationsUseCase,
    private val addReservationUseCase: AddReservationUseCase,
    private val updateReservationUseCase: UpdateReservationUseCase,
    private val deleteReservationUseCase: DeleteReservationUseCase,
    private val getDaysUseCase: GetDaysUseCase,
    private val getLocationsUseCase: GetLocationsUseCase,
    private val getTimesByRouteUseCase: GetTimesByRouteUseCase,
    private val validateReservationUseCase: ValidateReservationUseCase,
    private val sortDaysUseCase: SortDaysUseCase,
    private val getDefaultLocationsUseCase: GetDefaultLocationsUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private companion object {
        private const val TAG = "CalendarViewModel"
    }

    init {
        Log.d(TAG, "초기화: 캘린더 데이터와 예약 목록을 불러옵니다.")
        loadCalendarData()
        loadReservations()
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            getDaysUseCase()
                .catch { throwable ->
                    Log.w(TAG, "요일 목록 불러오기 실패", throwable)
                }
                .collect { days ->
                    Log.d(TAG, "요일 목록 불러오기 성공: 개수=${days.size}")
                    _uiState.value = _uiState.value.copy(days = days)
                }
        }

        viewModelScope.launch {
            getLocationsUseCase()
                .catch { throwable ->
                    Log.w(TAG, "정류장 목록 불러오기 실패", throwable)
                }
                .collect { locations ->
                    Log.d(TAG, "정류장 목록 불러오기 성공: 개수=${locations.size}")
                    val currentState = _uiState.value
                    val defaultLocations = getDefaultLocationsUseCase(
                        locations = locations,
                        currentLocation = currentState.selectedFrom,
                        currentDestination = currentState.selectedTo
                    )

                    _uiState.value = currentState.copy(
                        locations = locations,
                        selectedFrom = defaultLocations.from,
                        selectedTo = defaultLocations.to
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
                Log.d(
                    TAG,
                    "시간표 불러오기: 출발지=${state.selectedFrom}, 도착지=${state.selectedTo}"
                )
                getTimesByRouteUseCase(state.selectedFrom, state.selectedTo)
                    .catch { throwable ->
                        Log.w(TAG, "시간표 불러오기 실패", throwable)
                    }
                    .collect { times ->
                        Log.d(TAG, "시간표 불러오기 성공: 개수=${times.size}")
                        _uiState.value = _uiState.value.copy(times = times)
                    }
            }
        }
    }

    private fun loadReservations() {
        viewModelScope.launch {
            Log.d(TAG, "예약 목록 불러오기 시작")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            getAllReservationsUseCase()
                .catch { exception ->
                    val message = errorMapper.run { exception.toUserMessage() }
                    Log.w(TAG, "예약 목록 불러오기 실패: 메시지=$message", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = message
                    )
                }
                .collect { reservations ->
                    Log.d(TAG, "예약 목록 불러오기 성공: 개수=${reservations.size}")
                    _uiState.value = _uiState.value.copy(
                        reservations = reservations,
                        isLoading = false,
                        error = null
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
        Log.d(TAG, "요일 선택 변경: day=$day, 선택된 요일=$newDays")

        _uiState.value = _uiState.value.copy(
            selectedDays = newDays,
            selectedTimes = if (newDays.isEmpty()) emptyList() else _uiState.value.selectedTimes
        )
    }

    fun selectTime(time: String) {
        val state = _uiState.value
        if (state.selectedDays.isEmpty()) {
            Log.d(TAG, "시간 선택 거절: 선택된 요일이 없습니다.")
            _uiState.value = state.copy(error = errorMapper.run { ValidationError.SelectDayFirst.toUserMessage() })
            return
        }

        val currentTimes = state.selectedTimes

        val newTimes = if (currentTimes.contains(time)) {
            // 이미 선택된 시간을 클릭하면 해제
            currentTimes.filter { it != time }
        } else {
            // 새로운 시간 선택 (최대 2개 유지)
            if (currentTimes.size >= 2) {
                currentTimes.drop(1) + time
            } else {
                currentTimes + time
            }
        }
        Log.d(TAG, "시간 선택 변경: time=$time, 선택된 시간=$newTimes")

        _uiState.value = state.copy(selectedTimes = newTimes)
    }

    fun clearErrorMessage() {
        if (_uiState.value.error != null) {
            Log.d(TAG, "에러 메시지 초기화")
        }
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun selectFrom(from: String) {
        Log.d(TAG, "출발지 선택: $from")
        _uiState.value = _uiState.value.copy(
            selectedFrom = from, selectedTimes = emptyList() // 출발지 변경시 선택된 시간 초기화
        )
        loadTimesForRoute()
    }

    fun selectTo(to: String) {
        Log.d(TAG, "도착지 선택: $to")
        _uiState.value = _uiState.value.copy(
            selectedTo = to, selectedTimes = emptyList() // 도착지 변경시 선택된 시간 초기화
        )
        loadTimesForRoute()
    }

    fun addReservation() {
        viewModelScope.launch {
            val state = _uiState.value

            val validationError = validateReservationUseCase(
                selectedFrom = state.selectedFrom,
                selectedTo = state.selectedTo,
                selectedDays = state.selectedDays,
                selectedTimes = state.selectedTimes
            )
            if (validationError != null) {
                Log.d(TAG, "예약 추가 검증 실패: $validationError")
                _uiState.value = state.copy(error = errorMapper.run { validationError.toUserMessage() })
                return@launch
            }

            Log.d(
                TAG,
                "예약 추가: days=${state.selectedDays}, time=${state.selectedTimes}, from=${state.selectedFrom}, to=${state.selectedTo}"
            )

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val newReservation = Reservation(
                    id = 0,
                    days = sortDaysUseCase(state.selectedDays),
                    time = state.selectedTimes.first(),
                    from = state.selectedFrom,
                    to = state.selectedTo
                )
                addReservationUseCase(newReservation)
                val currentLocations = _uiState.value.locations
                val defaultLocations = getDefaultLocationsUseCase(currentLocations)
                _uiState.value = _uiState.value.copy(
                    selectedDays = emptyList(),
                    selectedTimes = emptyList(),
                    selectedFrom = defaultLocations.from,
                    selectedTo = defaultLocations.to,
                    isLoading = false,
                    error = null
                )
                Log.d(TAG, "예약 추가 성공")
                loadTimesForRoute()
            } catch (e: Exception) {
                val message = errorMapper.run { e.toUserMessage() }
                Log.w(TAG, "예약 추가 실패: 메시지=$message", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = message
                )
            }
        }
    }

    fun showDeleteDialog(id: Long) {
        Log.d(TAG, "삭제 다이얼로그 표시: id=$id")
        _uiState.value = _uiState.value.copy(deletingReservationId = id)
    }

    fun hideDeleteDialog() {
        Log.d(TAG, "삭제 다이얼로그 닫기")
        _uiState.value = _uiState.value.copy(deletingReservationId = null)
    }

    fun deleteReservation(id: Long) {
        viewModelScope.launch {
            Log.d(TAG, "예약 삭제 진행: id=$id")
            _uiState.value = _uiState.value.copy(
                isLoading = true, error = null, deletingReservationId = null
            )

            try {
                deleteReservationUseCase(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = null
                )
                Log.d(TAG, "예약 삭제 성공: id=$id")
            } catch (e: Exception) {
                val message = errorMapper.run { e.toUserMessage() }
                Log.w(TAG, "예약 삭제 실패: 메시지=$message", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = message
                )
            }
        }
    }

    fun startEditReservation(reservation: Reservation) {
        Log.d(TAG, "예약 수정 시작: id=${reservation.id}")
        _uiState.value = _uiState.value.copy(
            editingReservationId = reservation.id,
            selectedDays = reservation.days,
            selectedTimes = listOf(reservation.time),
            selectedFrom = reservation.from,
            selectedTo = reservation.to
        )
    }

    fun cancelEdit() {
        Log.d(TAG, "예약 수정 취소")
        val currentLocations = _uiState.value.locations
        val defaultLocations = getDefaultLocationsUseCase(currentLocations)
        _uiState.value = _uiState.value.copy(
            editingReservationId = null,
            selectedDays = emptyList(),
            selectedTimes = emptyList(),
            selectedFrom = defaultLocations.from,
            selectedTo = defaultLocations.to
        )
        loadTimesForRoute()
    }

    fun updateReservation() {
        viewModelScope.launch {
            val state = _uiState.value
            val editingId = state.editingReservationId ?: return@launch

            val validationError = validateReservationUseCase(
                selectedFrom = state.selectedFrom,
                selectedTo = state.selectedTo,
                selectedDays = state.selectedDays,
                selectedTimes = state.selectedTimes
            )
            if (validationError != null) {
                Log.d(TAG, "예약 수정 검증 실패: $validationError")
                _uiState.value = state.copy(error = errorMapper.run { validationError.toUserMessage() })
                return@launch
            }

            Log.d(
                TAG,
                "예약 수정 진행: id=$editingId, days=${state.selectedDays}, time=${state.selectedTimes}, from=${state.selectedFrom}, to=${state.selectedTo}"
            )

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                val updatedReservation = Reservation(
                    id = editingId,
                    days = sortDaysUseCase(state.selectedDays),
                    time = state.selectedTimes.first(),
                    from = state.selectedFrom,
                    to = state.selectedTo
                )
                updateReservationUseCase(updatedReservation)

                val currentLocations = _uiState.value.locations
                val defaultLocations = getDefaultLocationsUseCase(currentLocations)
                _uiState.value = _uiState.value.copy(
                    editingReservationId = null,
                    selectedDays = emptyList(),
                    selectedTimes = emptyList(),
                    selectedFrom = defaultLocations.from,
                    selectedTo = defaultLocations.to,
                    isLoading = false,
                    error = null
                )
                Log.d(TAG, "예약 수정 성공: id=$editingId")
                loadTimesForRoute()
            } catch (e: Exception) {
                val message = errorMapper.run { e.toUserMessage() }
                Log.w(TAG, "예약 수정 실패: 메시지=$message", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = message
                )
            }
        }
    }
}
