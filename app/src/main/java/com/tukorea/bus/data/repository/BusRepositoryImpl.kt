package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.Bus
import com.tukorea.bus.domain.repository.BusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusRepositoryImpl @Inject constructor(
) : BusRepository {
    
    override fun getAvailableBuses(from: String, to: String): Flow<List<Bus>> {
        val dummyBuses = listOf(
            Bus(
                id = "1",
                route = "A노선",
                time = "09:00",
                seats = "여유",
                color = "blue",
                stops = 5,
                arrivalTime = "09:25",
                from = from,
                to = to
            ),
            Bus(
                id = "2",
                route = "B노선",
                time = "09:15",
                seats = "보통",
                color = "green",
                stops = 4,
                arrivalTime = "09:35",
                from = from,
                to = to
            ),
            Bus(
                id = "3",
                route = "순환",
                time = "09:30",
                seats = "혼잡",
                color = "orange",
                stops = 8,
                arrivalTime = "10:00",
                from = from,
                to = to
            )
        )
        return flowOf(dummyBuses)
    }
}

