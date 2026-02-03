package com.tukorea.bus.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tukorea.bus.R
import com.tukorea.bus.data.location.LocationDataSource
import com.tukorea.bus.domain.util.Result
import com.tukorea.bus.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 백그라운드에서 위치를 추적하는 Foreground Service
 *
 * 주요 기능:
 * - 3초마다 현재 위치 수집
 * - Foreground Service로 동작하여 백그라운드에서도 안정적으로 실행
 * - Notification 표시로 사용자에게 위치 추적 중임을 알림
 * - LocationDataSource를 DI로 주입받아 클린 아키텍처 유지
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var locationDataSource: LocationDataSource

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "LocationTrackingService 생성됨")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LocationTrackingService 시작 요청")

        // 위치 권한 체크
        if (!hasLocationPermission()) {
            Log.w(TAG, "위치 권한이 없습니다. 서비스를 중지합니다.")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "LocationTrackingService 시작됨")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationTracking()

        return START_STICKY
    }

    /**
     * 위치 권한이 있는지 확인
     */
    private fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LocationTrackingService 종료됨")
        stopLocationTracking()
    }

    /**
     * 위치 추적 시작
     * 3초마다 현재 위치를 수집하고 로그에 출력
     */
    private fun startLocationTracking() {
        trackingJob?.cancel()
        trackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    val result = locationDataSource.getCurrentLocation()
                    when (result) {
                        is Result.Success -> {
                            val location = result.data
                            Log.i(
                                TAG,
                                "[위치 추적] 위도=${location.latitude}, 경도=${location.longitude}, 정확도=${location.accuracy}m"
                            )

                            // TODO: 근처 정류장 탐색 로직 추가
                            // TODO: 버스 탑승 중 위치 업데이트 로직 추가
                        }
                        is Result.Error -> {
                            Log.w(TAG, "[위치 추적] 위치 가져오기 실패: ${result.error}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[위치 추적] 예외 발생", e)
                }

                delay(LOCATION_UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * 위치 추적 중지
     */
    private fun stopLocationTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    /**
     * Notification Channel 생성 (Android 8.0 이상 필수)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.location_tracking_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.location_tracking_channel_description)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Foreground Service Notification 생성
     */
    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.location_tracking_notification_title))
            .setContentText(getString(R.string.location_tracking_notification_text))
            .setSmallIcon(R.drawable.ic_location_tracking)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracking_channel"
        private const val LOCATION_UPDATE_INTERVAL_MS = 3000L

        /**
         * Service 시작 Helper 함수
         */
        fun startService(context: Context) {
            // 위치 권한 체크
            val fineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!fineLocation && !coarseLocation) {
                Log.w(TAG, "위치 권한이 없어 서비스를 시작할 수 없습니다.")
                return
            }

            val intent = Intent(context, LocationTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Service 중지 Helper 함수
         */
        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
            context.stopService(intent)
        }
    }
}
