package com.tukorea.bus.data.util

import com.tukorea.bus.domain.error.AppError
import com.tukorea.bus.domain.util.AppResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

object NetworkErrorHandler {

    /** 네트워크 요청을 실행하고 예외를 도메인 오류로 바꾼다. 코루틴 취소는 그대로 전파한다. */
    suspend fun <T> handleNetworkRequest(block: suspend () -> T): AppResult<T> =
        try {
            Result.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Result.Error(
                when (e.code()) {
                    404 -> AppError.NotFound
                    in 400..499 -> AppError.Rejected
                    else -> AppError.UnknownError
                },
            )
        } catch (e: IOException) {
            Result.Error(AppError.NetworkError)
        } catch (e: RuntimeException) {
            Result.Error(AppError.CustomError(e.message))
        }
}
