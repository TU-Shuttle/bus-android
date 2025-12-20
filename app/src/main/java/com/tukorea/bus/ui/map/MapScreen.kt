package com.tukorea.bus.ui.map

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import android.graphics.Color
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.tukorea.bus.domain.model.MapLocation
import com.tukorea.bus.domain.util.LocationUtils

// 카메라 설정 상수
private const val MAP_MARKER_ZOOM_LEVEL = 15.0
private const val MAP_MARKER_LATITUDE_OFFSET = -0.0015 // 음수: 마커가 화면 위쪽에 표시, 양수: 마커가 화면 아래쪽에 표시
private const val MAP_CAMERA_ANIMATION_DURATION_MS = 500

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateTo: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
    val density = androidx.compose.ui.platform.LocalDensity.current

    // 모달 높이 상태
    var modalHeight by remember { mutableStateOf(com.tukorea.bus.ui.home.ModalHeight.LOW) }

    // 위치 권한 상태
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 권한 요청 플래그 (중복 요청 방지)
    var hasRequestedPermission by remember { mutableStateOf(false) }
    
    // 화면 진입 시 한 번만 권한 확인 및 요청
    LaunchedEffect(Unit) {
        if (locationPermissionsState.allPermissionsGranted) {
            // 이미 권한이 있으면 바로 위치 로드
            viewModel.onLocationPermissionGranted()
            hasRequestedPermission = true
        } else if (!hasRequestedPermission) {
            // 권한이 없고 아직 요청하지 않았을 때만 요청 (중복 방지)
            hasRequestedPermission = true
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
    
    // 권한 상태 변경 시 처리 (권한 요청 후 허용된 경우)
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted && hasRequestedPermission) {
            // 권한이 허용되었을 때 처리
            viewModel.onLocationPermissionGranted()
        }
    }

    // 에러 토스트
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 네이버 맵
        NaverMapView(
            modifier = Modifier.fillMaxSize(),
            currentLocation = state.currentLocation,
            isLocationPermissionGranted = locationPermissionsState.allPermissionsGranted,
            busRoutes = state.busRoutes,
            busStops = state.busStops,
            onMapReady = {
                // 맵 준비되고 권한 있으면 현재 위치 로드
                if (locationPermissionsState.allPermissionsGranted) {
                    viewModel.loadCurrentLocation()
                }
                if (state.busRoutes.isEmpty()) {
                    viewModel.loadBusRoutes()
                }
                if (state.busStops.isEmpty()) {
                    viewModel.loadBusStops()
                }
            },
            onBusStopMarkerClick = { busStop ->
                viewModel.onBusStopSelected(busStop)
                modalHeight = com.tukorea.bus.ui.home.ModalHeight.MID
            },
            onMapClick = {
                // 지도 클릭 시 모달 닫기
                if (state.isBusStopModalVisible) {
                    viewModel.closeBusStopModal()
                    modalHeight = com.tukorea.bus.ui.home.ModalHeight.LOW
                }
            }
        )

        // 로딩 인디케이터
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 현재 위치 버튼
        FloatingActionButton(
            onClick = { viewModel.loadCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(id = R.string.map_my_location_cd)
            )
        }

        // 정류장 정보 모달
        if (state.isBusStopModalVisible && state.selectedBusStop != null) {
            com.tukorea.bus.ui.common.BottomModal(
                modalHeight = modalHeight,
                onModalHeightChange = { newHeight ->
                    modalHeight = newHeight
                },
                screenHeight = screenHeight,
                density = density,
                isVisible = state.isBusStopModalVisible
            ) {
                BusStopInfoModal(
                    busStop = state.selectedBusStop!!,
                    onDismiss = {
                        viewModel.closeBusStopModal()
                        modalHeight = com.tukorea.bus.ui.home.ModalHeight.LOW
                    },
                    onViewTimeTable = {
                        onNavigateTo(com.tukorea.bus.ui.navigation.Screen.QuickRide.route)
                    },
                    onRideStart = { bus ->
                        if (bus.status == BusStatus.DEPARTED) {
                            // 버스가 이미 출발한 경우 바로 탑승 화면으로 이동
                            viewModel.closeBusStopModal()
                            onNavigateTo(com.tukorea.bus.ui.navigation.Screen.Ride.route)
                        } else {
                            // 대기 상태이면 알람 예약
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.bus_ride_alarm_reserved),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            // TODO: 도착 예정 10분 전 알람 설정
                            // TODO: 버스 출발 시 자동으로 RideScreen으로 이동
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NaverMapView(
    modifier: Modifier = Modifier,
    currentLocation: MapLocation?,
    isLocationPermissionGranted: Boolean,
    busRoutes: List<BusRoute> = emptyList(),
    busStops: List<com.tukorea.bus.domain.model.BusStop> = emptyList(),
    onMapReady: () -> Unit = {},
    bottomPadding: Int = 0,
    onMapInitialized: ((NaverMap) -> Unit)? = null,
    onBusStopMarkerClick: (com.tukorea.bus.domain.model.BusStop) -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // MapView 인스턴스 (Configuration change에도 유지)
    val mapView = remember { MapView(context) }

    // FusedLocationSource (네이버맵 SDK 위치 소스)
    // Note: requestCode는 Accompanist가 권한을 처리하므로 사용되지 않음
    val locationSource = remember {
        FusedLocationSource(context as Activity, 0)
    }

    // NaverMap 인스턴스
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    // 마지막 업데이트 위치 (중복 업데이트 방지)
    var lastUpdatedLocation by remember { mutableStateOf<MapLocation?>(null) }

    // 맵이 준비되었는지 여부
    var isMapReady by remember { mutableStateOf(false) }
    
    // 버스 경로 오버레이 리스트
    val pathOverlays = remember { mutableListOf<PathOverlay>() }

    // 버스 경로 표시
    LaunchedEffect(busRoutes, naverMap, isMapReady) {
        naverMap?.let { map ->
            if (isMapReady) {
                // 기존 경로 오버레이 제거
                pathOverlays.forEach { it.map = null }
                pathOverlays.clear()

                // 새로운 경로 오버레이 추가
                busRoutes.forEachIndexed { index, route ->
                    if (route.coordinates.isNotEmpty()) {
                        val pathOverlay = PathOverlay().apply {
                            coords = route.coordinates.map { coord -> 
                                LatLng(coord.latitude, coord.longitude) 
                            }
                            // 노선별로 다른 색상 지정
                            color = when (index % 3) {
                                0 -> Color.parseColor("#4285F4") // 파란색
                                1 -> Color.parseColor("#34A853") // 초록색
                                else -> Color.parseColor("#FBBC05") // 주황색
                            }
                            outlineColor = Color.parseColor("#FFFFFF")
                            width = 12
                            outlineWidth = 2
                        }
                        // map 할당은 apply 블록 외부에서
                        pathOverlay.map = map
                        pathOverlays.add(pathOverlay)
                    }
                }
            }
        }
    }

    // 권한 상태 변경 시 LocationTrackingMode 업데이트
    LaunchedEffect(isLocationPermissionGranted, naverMap) {
        naverMap?.let { map ->
            if (isLocationPermissionGranted) {
                map.locationTrackingMode = LocationTrackingMode.Follow
            } else {
                map.locationTrackingMode = LocationTrackingMode.None
            }
        }
    }

    // 현재 위치 변경 시 카메라 이동
    LaunchedEffect(currentLocation, naverMap) {
        currentLocation?.let { location ->
            naverMap?.let { map ->
                val shouldUpdate = lastUpdatedLocation?.let { last ->
                    LocationUtils.isDistanceGreaterThan(
                        from = last,
                        to = location,
                        thresholdMeters = 2.0
                    )
                } ?: true

                if (shouldUpdate) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdate.scrollTo(latLng)
                        .animate(CameraAnimation.Easing, 500)
                    map.moveCamera(cameraUpdate)
                    lastUpdatedLocation = location
                }
            }
        }
    }

    LaunchedEffect(naverMap, isMapReady) {
        naverMap?.let { map ->
            if (isMapReady) {
                onMapInitialized?.invoke(map)
            }
        }
    }

    // 맵 준비 완료 콜백
    LaunchedEffect(isMapReady) {
        if (isMapReady) {
            onMapReady()
        }
    }

    // 라이프사이클 관리 및 리소스 정리
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            try {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> {
                        // AndroidView factory에서 이미 onCreate를 호출하므로 여기서는 호출하지 않음
                    }
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> {
                        // onDispose에서 처리
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // lifecycle 관리 중 예외 발생 시 무시 (이미 destroyed 상태일 수 있음)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            // 버스 경로 오버레이 정리
            pathOverlays.forEach { overlay ->
                try {
                    overlay.map = null
                } catch (e: Exception) {
                    // 무시
                }
            }
            pathOverlays.clear()
            
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                // lifecycle이 DESTROYED가 아닐 때만 onDestroy 호출
                if (lifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
                    mapView.onDestroy()
                }
            } catch (e: Exception) {
                // 이미 destroyed 상태일 수 있음
            }
        }
    }

    // LowMemory 처리
    DisposableEffect(Unit) {
        onDispose {
            mapView.onLowMemory()
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                onCreate(Bundle())
                getMapAsync { map ->
                    naverMap = map

                    // 위치 소스 설정 (FusedLocationSource)
                    map.locationSource = locationSource

                    // UI 설정
                    map.uiSettings.apply {
                        isLocationButtonEnabled = false // Compose FAB 사용
                        isZoomControlEnabled = true
                    }

                    // 위치 추적 모드 설정
                    if (isLocationPermissionGranted) {
                        map.locationTrackingMode = LocationTrackingMode.Follow
                    }

                    // 지도 클릭 리스너 설정
                    map.setOnMapClickListener { _, _ ->
                        onMapClick()
                    }

                    // 정류장 마커 추가
                    busStops.forEach { busStop ->
                        val marker = com.naver.maps.map.overlay.Marker().apply {
                            position = LatLng(busStop.latitude, busStop.longitude)
                            captionText = busStop.name
                            icon = com.naver.maps.map.overlay.OverlayImage.fromResource(com.tukorea.bus.R.drawable.ic_location_tracking)
                            tag = busStop
                        }

                        marker.setOnClickListener {
                            // 마커 클릭 시 카메라 이동 (모달 위치를 고려하여 위로 오프셋)
                            val targetLatLng = LatLng(
                                busStop.latitude + MAP_MARKER_LATITUDE_OFFSET, 
                                busStop.longitude
                            )
                            // 위치 이동
                            val scrollUpdate = CameraUpdate.scrollTo(targetLatLng)
                                .animate(CameraAnimation.Easing, MAP_CAMERA_ANIMATION_DURATION_MS.toLong())
                            map.moveCamera(scrollUpdate)
                            
                            // 줌 레벨 설정
                            val zoomUpdate = CameraUpdate.zoomTo(MAP_MARKER_ZOOM_LEVEL)
                                .animate(CameraAnimation.Easing, 300)
                            map.moveCamera(zoomUpdate)

                            onBusStopMarkerClick(busStop)
                            true
                        }

                        marker.map = map
                    }

                    isMapReady = true
                }
            }
        },
        update = { view ->
            // 시스템 바 영역만큼 맵 UI 컨트롤 위치 조정
            naverMap?.let { map ->
                val density = view.context.resources.displayMetrics.density
                val bottomPaddingPx = (80 * density).toInt() // 네비게이션 바 + 여유 공간
                map.setContentPadding(0, 0, 0, bottomPaddingPx)
            }
        },
        modifier = modifier
    )
}
