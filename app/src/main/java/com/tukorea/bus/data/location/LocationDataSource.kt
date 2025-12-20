package com.tukorea.bus.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tukorea.bus.domain.error.MapError
import com.tukorea.bus.domain.model.MapLocation
import com.tukorea.bus.domain.util.MapResult
import com.tukorea.bus.domain.util.Result
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LocationDataSource @Inject constructor(
    private val context: Context
) {
    /**
     * FusedLocationProviderClient 인스턴스
     * GPS, Wi-Fi, 셀룰러 네트워크를 결합하여 가장 정확한 위치를 제공합니다.
     */
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * 위치 권한이 허용되었는지 확인합니다.
     * FINE_LOCATION 또는 COARSE_LOCATION 중 하나라도 허용되면 true를 반환합니다.
     * @return 위치 권한 허용 여부
     */
    fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 현재 위치를 가져옵니다.
     * Google Play Services의 FusedLocationProviderClient를 사용하여
     * 높은 정확도로 위치를 가져옵니다.
     * 
     * 먼저 getLastLocation()으로 최근 위치를 시도하고,
     * 실패하면 getCurrentLocation()으로 새 위치를 요청합니다.
     *
     * @return 성공 시 위치 정보, 실패 시 에러를 포함한 Result
     */
    suspend fun getCurrentLocation(): MapResult<MapLocation> {
        if (!isLocationPermissionGranted()) {
            Log.w(TAG, "위치 권한이 없습니다.")
            return Result.Error(MapError.PermissionDenied)
        }

        return try {
            // 1. 먼저 마지막으로 알려진 위치를 시도 (빠름)
            @SuppressLint("MissingPermission")
            val lastLocation = fusedLocationClient.lastLocation.await()
            
            lastLocation?.let {
                Log.d(
                    TAG,
                    "마지막 위치 가져오기 성공: lat=${it.latitude}, lng=${it.longitude}, accuracy=${it.accuracy}"
                )
                return Result.Success(
                    MapLocation(
                        latitude = it.latitude, 
                        longitude = it.longitude, 
                        accuracy = it.accuracy
                    )
                )
            }
            
            // 2. 마지막 위치가 없으면 새 위치 요청
            Log.d(TAG, "마지막 위치가 없음. 새 위치 요청 중...")
            val cancellationTokenSource = CancellationTokenSource()

            @SuppressLint("MissingPermission") 
            val location: Location? = withTimeout(15000) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            }

            // Location 객체를 MapLocation 모델로 변환
            location?.let {
                Log.d(
                    TAG,
                    "현재 위치 가져오기 성공: lat=${it.latitude}, lng=${it.longitude}, accuracy=${it.accuracy}"
                )
                Result.Success(
                    MapLocation(
                        latitude = it.latitude, 
                        longitude = it.longitude, 
                        accuracy = it.accuracy
                    )
                )
            } ?: run {
                Log.w(TAG, "위치 정보가 null입니다. 위치 서비스가 꺼져있을 수 있습니다.")
                Result.Error(MapError.LocationNotFound)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "위치 권한이 거부", e)
            Result.Error(MapError.PermissionDenied)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "위치 가져오기 타임아웃 (15초)", e)
            Result.Error(MapError.Timeout)
        } catch (e: Exception) {
            Log.e(TAG, "위치 가져오기 실패", e)
            Result.Error(MapError.Unknown(e.message))
        }
    }

    companion object {
        // logcat 필터링 태그
        private const val TAG = "LocationDataSource"
    }
}

