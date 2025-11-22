package com.tukorea.bus.data.repository

import com.tukorea.bus.data.location.LocationDataSource
import com.tukorea.bus.domain.model.MapLocation
import com.tukorea.bus.domain.repository.MapRepository
import com.tukorea.bus.domain.util.MapResult
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MapRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
) : MapRepository {
    
    /**
     * LocationDataSource를 통해 현재 위치를 가져옵니다.
     */
    override suspend fun getCurrentLocation(): MapResult<MapLocation> {
        return locationDataSource.getCurrentLocation()
    }
    
    /**
     * LocationDataSource를 통해 위치 권한 상태를 확인합니다.
     */
    override fun isLocationPermissionGranted(): Boolean {
        return locationDataSource.isLocationPermissionGranted()
    }
}

