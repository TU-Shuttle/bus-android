package com.tukorea.bus.domain.model

data class Bus(
    val id: String,
    val route: String,
    val time: String,
    val seats: String,
    val color: String,
    val stops: Int,
    val arrivalTime: String,
    val from: String,
    val to: String
)

