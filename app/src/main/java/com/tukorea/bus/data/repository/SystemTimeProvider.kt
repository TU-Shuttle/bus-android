package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.ServiceMoment
import com.tukorea.bus.domain.repository.TimeProvider
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

/** 기기 시간대와 상관없이 셔틀 기준인 한국 시간으로 계산한다(minSdk 25라 java.util 사용). */
class SystemTimeProvider @Inject constructor() : TimeProvider {

    override fun now(): ServiceMoment {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val isoDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1
        return ServiceMoment(
            isoDate = "%04d-%02d-%02d".format(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
            ),
            isoDayOfWeek = isoDayOfWeek,
            minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE),
        )
    }
}
