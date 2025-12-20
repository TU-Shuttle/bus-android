package com.tukorea.bus

import android.app.Application
import android.util.Log
import com.tukorea.bus.service.LocationTrackingService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BusApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // TODO: 위치 권한이 있을 때만 서비스 시작하도록 수정 필요
        // 현재는 앱 시작 시 크래시를 방지하기 위해 주석 처리
        // startLocationTrackingService()

        Log.d(TAG, "BusApplication 초기화 완료")
    }

    /**
     * 위치 추적 서비스 시작
     * 앱 시작 시 자동으로 백그라운드 위치 추적 시작
     */
    private fun startLocationTrackingService() {
        try {
            LocationTrackingService.startService(this)
            Log.d(TAG, "LocationTrackingService 시작 요청 완료")
        } catch (e: Exception) {
            Log.e(TAG, "LocationTrackingService 시작 실패", e)
        }
    }

    companion object {
        private const val TAG = "BusApplication"

        lateinit var instance: BusApplication
            private set
    }
}