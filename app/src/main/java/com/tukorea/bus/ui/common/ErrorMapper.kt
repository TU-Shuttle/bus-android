package com.tukorea.bus.ui.common

import androidx.annotation.StringRes
import com.tukorea.bus.R
import com.tukorea.bus.domain.error.AppError

/** 도메인 오류 → 사용자 문구. ViewModel은 문자열 리소스 id만 들고 있고, 화면이 stringResource로 읽는다. */
@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.NetworkError -> R.string.error_network
    AppError.NotFound -> R.string.error_not_found
    AppError.Rejected -> R.string.error_rejected
    AppError.UnknownError, is AppError.CustomError -> R.string.error_unknown
}
