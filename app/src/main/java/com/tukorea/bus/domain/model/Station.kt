package com.tukorea.bus.domain.model

data class Station(
    val name: String,
    val time: String,
    val active: Boolean,
    val nextTimes: List<String>
)

