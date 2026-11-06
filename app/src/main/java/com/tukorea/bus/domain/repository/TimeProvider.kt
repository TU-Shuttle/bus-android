package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.ServiceMoment

/** 현재 시각. 테스트에서 고정할 수 있도록 인터페이스로 둔다. */
fun interface TimeProvider {
    fun now(): ServiceMoment
}
