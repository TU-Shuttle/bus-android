package com.tukorea.bus.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tukorea.bus.domain.model.GeoPoint
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleRoute
import com.tukorea.bus.domain.repository.RouteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱에 넣어 둔 노선 경로(`assets/shuttle_routes.json`, 방향 코드 → [[lat, lng], …]).
 * 경로는 OpenStreetMap 도로 데이터로 만든 근사값이다. 실제 운행 경로와 다르면 이 파일만 고치면 된다.
 */
@Singleton
class AssetRouteRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
) : RouteRepository {

    private val routes: Map<ShuttleDirection, ShuttleRoute> by lazy {
        val type = object : TypeToken<Map<String, List<List<Double>>>>() {}.type
        val raw: Map<String, List<List<Double>>> =
            context.assets.open(FILE).bufferedReader().use { gson.fromJson(it, type) }
        raw.mapNotNull { (code, coordinates) ->
            val direction = ShuttleDirection.entries.firstOrNull { it.name == code } ?: return@mapNotNull null
            direction to ShuttleRoute(direction, coordinates.map { GeoPoint(it[0], it[1]) })
        }.toMap()
    }

    override fun routeOf(direction: ShuttleDirection): ShuttleRoute? = routes[direction]

    private companion object {
        const val FILE = "shuttle_routes.json"
    }
}
