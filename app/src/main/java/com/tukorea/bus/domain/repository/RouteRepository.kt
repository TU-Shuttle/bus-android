package com.tukorea.bus.domain.repository

import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleRoute

interface RouteRepository {
    fun routeOf(direction: ShuttleDirection): ShuttleRoute?
}
