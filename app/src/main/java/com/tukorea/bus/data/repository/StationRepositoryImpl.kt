package com.tukorea.bus.data.repository

import com.tukorea.bus.domain.model.Station
import com.tukorea.bus.domain.repository.StationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepositoryImpl @Inject constructor(
) : StationRepository {
    
    private val stationsByRoute = mapOf(
        "하교 방향" to listOf(
            Station("2캠퍼스 셔틀버스 정류장", "5분 후", true, listOf("20분 후", "35분 후")),
            Station("1캠퍼스 (산융)", "12분 후", false, listOf("27분 후", "42분 후")),
            Station("정왕역", "18분 후", false, listOf("33분 후", "48분 후")),
        ),
        "등교 방향" to listOf(
            Station("정왕역", "3분 후", true, listOf("18분 후", "33분 후")),
            Station("1캠퍼스 (산융)", "8분 후", false, listOf("23분 후", "38분 후")),
            Station("2캠퍼스 셔틀버스 정류장", "15분 후", false, listOf("30분 후", "45분 후")),
        ),
//        "C노선" to listOf(
//            Station("도서관", "7분 후", true, listOf("22분 후", "37분 후")),
//            Station("체육관", "13분 후", false, listOf("28분 후", "43분 후")),
//            Station("공대", "20분 후", false, listOf("35분 후", "50분 후"))
//        ),
//        "순환" to listOf(
//            Station("기숙사", "10분 후", true, listOf("25분 후", "40분 후")),
//            Station("본관", "15분 후", false, listOf("30분 후", "45분 후")),
//            Station("역", "20분 후", false, listOf("35분 후", "50분 후")),
//            Station("정문", "25분 후", false, listOf("40분 후", "55분 후")),
//            Station("기숙사", "30분 후", false, listOf("45분 후", "60분 후"))
//        )
    )
    
    private val _routes = MutableStateFlow(listOf("하교 방향", "등교 방향", "C노선", "순환"))
    private val routes: StateFlow<List<String>> = _routes.asStateFlow()
    
    override fun getStationsByRoute(route: String): Flow<List<Station>> {
        return routes.map { 
            stationsByRoute[route] ?: emptyList()
        }
    }
    
    override fun getAllRoutes(): Flow<List<String>> {
        return routes
    }
}

