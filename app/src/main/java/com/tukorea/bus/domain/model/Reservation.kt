package com.tukorea.bus.domain.model

data class Reservation(
    val id: Long = 0,
    val days: List<String>,
    val time: String,
    val from: String,
    val to: String,
    val createdAt: Long = System.currentTimeMillis()
)

