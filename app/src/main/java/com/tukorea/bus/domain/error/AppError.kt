package com.tukorea.bus.domain.error

/**
 * 애플리케이션 전역 에러 타입
 * Domain 레이어에서 에러를 정의하여 UI 레이어에서 메시지로 변환합니다.
 */
sealed class AppError {
    object NetworkError : AppError()
    object UnknownError : AppError()
    data class CustomError(val errorMessage: String?) : AppError()

    fun getMessage(): String {
        return when (this) {
            is NetworkError -> "네트워크 오류가 발생했습니다."
            is UnknownError -> "알 수 없는 오류가 발생했습니다."
            is CustomError -> errorMessage ?: "오류가 발생했습니다."
        }
    }
}

