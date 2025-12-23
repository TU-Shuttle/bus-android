package com.tukorea.bus.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetNextReservationUseCase
import com.tukorea.bus.domain.usecase.GetNotificationsUseCase
import com.tukorea.bus.domain.usecase.GetUnreadImportantNotificationsUseCase
import com.tukorea.bus.ui.common.ErrorMapper
import com.tukorea.bus.ui.common.collectWithUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 홈 화면의 다음 예약 정보와 중요 알림 배너 상태를 관리하는 ViewModel.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNextReservationUseCase: GetNextReservationUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val getUnreadImportantNotificationsUseCase: GetUnreadImportantNotificationsUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        Log.d(TAG, "초기화: 다음 예약과 중요 알림을 불러옵니다.")
        loadNextReservation()
        observeImportantNotifications()
    }

    /**
     * 다음 예약 정보를 불러와 UI 상태를 업데이트합니다.
     * - 로딩 중/성공/실패에 따라 로딩 플래그와 에러 메시지를 관리합니다.
     */
    private fun loadNextReservation() {
        Log.d(TAG, "다음 예약 불러오기 시작")
        viewModelScope.launch {
            getNextReservationUseCase()
                .collectWithUiState(
                    state = _uiState,
                    onStart = { state ->
                        state.copy(isLoading = true, error = null)
                    },
                    onError = { state, throwable ->
                        val message = errorMapper.run { throwable.toUserMessage() }
                        Log.w(TAG, "다음 예약 불러오기 실패: 메시지=$message", throwable)
                        state.copy(
                        isLoading = false,
                            error = message
                    )
                    },
                    onSuccess = { state, reservation ->
                        Log.d(TAG, "다음 예약 불러오기 성공: 예약=$reservation")
                        state.copy(
                        nextReservation = reservation,
                        isLoading = false,
                        error = null
                    )
                }
                )
        }
    }

    /**
     * 홈 화면에서 사용하는 모달(bottom sheet)의 현재 높이 상태를 갱신합니다.
     *
     * @param height 현재 모달의 높이(열림/반쯤/닫힘 등)를 나타내는 값
     */
    fun updateModalHeight(height: ModalHeight) {
        Log.d(TAG, "모달 높이 변경: $height")
        _uiState.value = _uiState.value.copy(modalHeight = height)
    }

    /**
     * 중요 알림(공지) 스트림을 구독하여, 읽지 않은 중요 알림 배너 표시 여부를 관리합니다.
     * - 스트림 구독만 수행하며, 별도의 수동 갱신 호출은 필요 없습니다.
     */
    private fun observeImportantNotifications() {
        Log.d(TAG, "중요 알림 관찰 시작")
        viewModelScope.launch {
            getNotificationsUseCase()
                .collectWithUiState(
                    state = _uiState,
                    onStart = { state ->
                        // 로딩 상태는 모달/예약에서 사용하므로 알림 배너에는 영향 주지 않음
                        state
                    },
                    onError = { state, throwable ->
                        val message = errorMapper.run { throwable.toUserMessage() }
                        Log.w(TAG, "중요 알림 관찰 중 오류: 메시지=$message", throwable)
                        // 오류가 발생해도 알림 배너는 표시하지 않음
                        state.copy(
                            hasUnreadImportantNotice = false,
                            firstUnreadImportantNoticeId = null
                        )
                    },
                    onSuccess = { state, notifications ->
                        val result = getUnreadImportantNotificationsUseCase(notifications)
                        Log.d(
                            TAG,
                            "읽지 않은 중요 알림: 있음=${result.hasUnreadImportant}, ID=${result.firstUnreadImportantId}"
                        )

                        state.copy(
                            hasUnreadImportantNotice = result.hasUnreadImportant,
                            firstUnreadImportantNoticeId = result.firstUnreadImportantId
                        )
                    }
                )
        }
    }
}
