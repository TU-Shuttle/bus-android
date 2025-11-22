package com.tukorea.bus.domain.error

/**
 * 맵 관련 에러 타입
 * Domain 레이어에서 에러를 정의하여 UI 레이어에서 메시지로 변환합니다.
 */
sealed class MapError {
    object LocationNotFound : MapError()
    object PermissionDenied : MapError()
    object Timeout : MapError()
    data class Unknown(val errorMessage: String?) : MapError()

    fun getMessage(): String {
        return when (this) {
            is LocationNotFound -> "위치를 가져올 수 없습니다."
            is PermissionDenied -> "위치 권한이 필요합니다."
            is Timeout -> "위치를 가져오는 데 시간이 너무 오래 걸렸습니다."
            is Unknown -> errorMessage ?: "알 수 없는 오류가 발생했습니다."
        }
    }
}

