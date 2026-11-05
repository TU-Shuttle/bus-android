package com.tukorea.bus.domain.error

/**
 * 애플리케이션 전역 에러 타입. UI 레이어의 ErrorMapper 가 사용자 문구로 바꾼다.
 */
sealed class AppError {
    data object NetworkError : AppError()

    /** 서버에 해당 자원이 없음(예: 시간표 미수집, 지난 출발편). */
    data object NotFound : AppError()

    /** 서버가 요청을 거절함(예: 갈 수 없는 하차지, 기간 밖 날짜). */
    data object Rejected : AppError()

    data object UnknownError : AppError()

    data class CustomError(val errorMessage: String?) : AppError()
}
