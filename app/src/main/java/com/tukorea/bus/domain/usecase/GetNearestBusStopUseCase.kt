package com.tukorea.bus.domain.usecase

import android.util.Log
import com.tukorea.bus.data.datasource.BusStopDataSource
import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.MapLocation
import com.tukorea.bus.domain.error.MapError
import com.tukorea.bus.domain.util.DistanceCalculator
import com.tukorea.bus.domain.util.MapResult
import com.tukorea.bus.domain.util.Result
import java.util.Calendar
import javax.inject.Inject

/**
 * 현재 위치에서 가장 가까운 정류장을 찾는 UseCase
 */
class GetNearestBusStopUseCase @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val busStopDataSource: BusStopDataSource
) {
    private companion object {
        private const val TAG = "GetNearestBusStopUseCase"
    }

    /**
     * 현재 위치를 기반으로 특정 목적지로 가는 가장 가까운 정류장을 찾습니다.
     * 하버사인 공식을 사용하여 거리를 계산하고, 해당 목적지로 가며 현재 운행 중인 정류장 중에서 선택합니다.
     *
     * @param destination 목적지 이름 (예: "정왕역", "1캠퍼스", "2캠퍼스")
     * @return 성공 시 가장 가까운 정류장과 거리, 실패 시 에러
     */
    suspend operator fun invoke(destination: String): MapResult<NearestBusStopResult> {
        // 1. 현재 위치 가져오기
        val locationResult = getCurrentLocationUseCase()
        if (locationResult is Result.Error) {
            Log.w(TAG, "위치 정보를 가져올 수 없습니다: ${locationResult.error}")
            return locationResult
        }

        val currentLocation = (locationResult as Result.Success).data
        Log.d(
            TAG,
            "현재 위치: lat=${currentLocation.latitude}, lng=${currentLocation.longitude}"
        )

        // 2. 목적지로 가며 현재 시간 기준으로 운행 중인 정류장 가져오기
        val currentTimeMinutes = getCurrentTimeInMinutes()
        val operatingStops = busStopDataSource.getOperatingBusStops(destination, currentTimeMinutes)

        if (operatingStops.isEmpty()) {
            Log.w(TAG, "목적지($destination)로 가는 현재 운행 중인 정류장이 없습니다.")
            return Result.Error(MapError.Unknown("목적지로 가는 현재 운행 중인 정류장이 없습니다"))
        }

        Log.d(TAG, "목적지($destination)로 가는 운행 중인 정류장 개수: ${operatingStops.size}")

        // 3. 각 정류장까지의 거리 계산 및 가장 가까운 정류장 찾기
        val nearestStop = operatingStops
            .map { busStop ->
                val distance = DistanceCalculator.calculateDistance(
                    lat1 = currentLocation.latitude,
                    lon1 = currentLocation.longitude,
                    lat2 = busStop.latitude,
                    lon2 = busStop.longitude
                )
                Pair(busStop, distance)
            }
            .minByOrNull { it.second }

        return if (nearestStop != null) {
            Log.d(
                TAG,
                "가장 가까운 정류장: ${nearestStop.first.name}, 거리: ${DistanceCalculator.formatDistance(nearestStop.second)}"
            )
            Result.Success(
                NearestBusStopResult(
                    busStop = nearestStop.first,
                    distance = nearestStop.second,
                    currentLocation = currentLocation
                )
            )
        } else {
            Log.w(TAG, "가장 가까운 정류장을 찾을 수 없습니다.")
            Result.Error(MapError.Unknown("가장 가까운 정류장을 찾을 수 없습니다"))
        }
    }

    /**
     * 현재 시간을 분 단위로 반환합니다.
     *
     * @return 현재 시간 (분 단위, 예: 10시 30분 = 630)
     */
    private fun getCurrentTimeInMinutes(): Int {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        return hours * 60 + minutes
    }
}

/**
 * 가장 가까운 정류장 검색 결과
 *
 * @property busStop 가장 가까운 정류장
 * @property distance 거리 (미터)
 * @property currentLocation 현재 위치
 */
data class NearestBusStopResult(
    val busStop: BusStop,
    val distance: Double,
    val currentLocation: MapLocation
)
