package com.tukorea.bus.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tukorea.bus.domain.usecase.GetNotificationsUseCase
import com.tukorea.bus.domain.usecase.MarkAllNotificationsAsReadUseCase
import com.tukorea.bus.domain.usecase.MarkNotificationAsReadUseCase
import com.tukorea.bus.ui.common.ErrorMapper
import com.tukorea.bus.ui.common.collectWithUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 알림 목록, 읽음 상태, 에러/로딩 상태를 관리하는 ViewModel.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val errorMapper: ErrorMapper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private companion object {
        private const val TAG = "NotificationsVM"
    }
    
    init {
        Log.d(TAG, "초기화: 알림 목록을 불러옵니다.")
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase()
                .collectWithUiState(
                    state = _uiState,
                    onStart = { state ->
                        Log.d(TAG, "알림 목록 불러오기 시작")
                        state.copy(isLoading = true, error = null)
                    },
                    onError = { state, throwable ->
                        val message = errorMapper.run { throwable.toUserMessage() }
                        Log.w(TAG, "알림 목록 불러오기 실패: 메시지=$message", throwable)
                        state.copy(
                        isLoading = false,
                            error = message
                    )
                    },
                    onSuccess = { state, notifications ->
                        Log.d(TAG, "알림 목록 불러오기 성공: 개수=${notifications.size}")
                        state.copy(
                        notifications = notifications,
                        isLoading = false,
                        error = null
                    )
                }
                )
        }
    }

    fun markAsRead(notificationId: Int) {
        Log.d(TAG, "알림 읽음 처리: id=$notificationId")
        viewModelScope.launch {
            markNotificationAsReadUseCase(notificationId)
        }
    }

    fun markAllAsRead() {
        Log.d(TAG, "모든 알림을 읽음 처리합니다.")
        viewModelScope.launch {
            markAllNotificationsAsReadUseCase()
        }
    }
}

