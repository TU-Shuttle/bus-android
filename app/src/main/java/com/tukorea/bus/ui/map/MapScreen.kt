package com.tukorea.bus.ui.map

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import com.tukorea.bus.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.util.FusedLocationSource
import com.tukorea.bus.data.datasource.BusStopDataSource
import com.tukorea.bus.domain.model.BusMarkerLocation
import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.MapLocation
import com.tukorea.bus.domain.repository.BusStatus
import com.tukorea.bus.domain.util.LocationUtils
import com.tukorea.bus.ui.common.BottomModal
import com.tukorea.bus.ui.common.ImageUtils
import com.tukorea.bus.ui.home.ModalHeight
import com.tukorea.bus.ui.navigation.Screen
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

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
    var modalHeight by remember { mutableStateOf(ModalHeight.LOW) }

    // 위치 권한 상태
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 권한 요청 결과 처리
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    // 권한 요청 (처음 진입 시)
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // 에러 토스트
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    // 테스트용 버스 마커 위치 (임의의 위도/경도)
    // 버스 아이콘이 표시될 위치들을 설정합니다
    val testBusMarkers = remember {
        listOf(
            BusMarkerLocation(
                latitude = 37.5665, // 서울시청 위도
                longitude = 126.9780, // 서울시청 경도
                caption = "버스 #1"
            ),
            BusMarkerLocation(
                latitude = 37.5680, // 서울시청 북쪽 약 200m
                longitude = 126.9790,
                caption = "버스 #2"
            ),
            BusMarkerLocation(
                latitude = 37.5650, // 서울시청 남쪽 약 200m
                longitude = 126.9770,
                caption = "버스 #3"
            )
            // 여기에 더 많은 버스 위치를 추가할 수 있습니다
            // 예: BusMarkerLocation(latitude = 위도, longitude = 경도, caption = "버스 이름")
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 네이버 맵
        NaverMapView(
            modifier = Modifier.fillMaxSize(),
            currentLocation = state.currentLocation,
            isLocationPermissionGranted = locationPermissionsState.allPermissionsGranted,
            busMarkers = testBusMarkers, // 버스 마커 추가
            busStops = state.busStops, // ViewModel에서 관리하는 정류장 목록
            onMapReady = {
                // 맵 준비되고 권한 있으면 현재 위치 로드
                if (locationPermissionsState.allPermissionsGranted) {
                    viewModel.loadCurrentLocation()
                }
            },
            onBusStopMarkerClick = { busStop ->
                viewModel.onBusStopSelected(busStop)
                modalHeight = ModalHeight.MID
            },
            onMapClick = {
                // 지도 클릭 시 모달 닫기
                if (state.isBusStopModalVisible) {
                    viewModel.closeBusStopModal()
                    modalHeight = ModalHeight.LOW
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


        // 정류장 정보 모달
        if (state.isBusStopModalVisible && state.selectedBusStop != null) {
            BottomModal(
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
                    buses = state.busesForSelectedStop, // ViewModel에서 관리하는 버스 목록
                    onDismiss = {
                        viewModel.closeBusStopModal()
                        modalHeight = ModalHeight.LOW
                    },
                    onViewTimeTable = {
                        onNavigateTo(Screen.QuickRide.route)
                    },
                    onRideStart = { bus ->
                        if (bus.status == BusStatus.DEPARTED) {
                            // 버스가 이미 출발한 경우 바로 탑승 화면으로 이동
                            viewModel.closeBusStopModal()
                            onNavigateTo(Screen.Ride.route)
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
    onMapReady: () -> Unit = {},
    bottomPadding: Int = 0,
    onMapInitialized: ((NaverMap) -> Unit)? = null,
    onBusStopMarkerClick: (BusStop) -> Unit = {},
    onMapClick: () -> Unit = {},
    busMarkers: List<BusMarkerLocation> = emptyList(), // 버스 마커 위치 리스트
    busStops: List<BusStop> = emptyList() // 정류장 목록 (ViewModel에서 관리)
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

    // 버스 마커 저장 (마커 제거/추가 관리용)
    val busMarkerList = remember { mutableListOf<Marker>() }

    // 정류장 마커 저장 (마커 제거/추가 관리용)
    val busStopMarkerList = remember { mutableListOf<Marker>() }

    // 정류장 데이터 가져오기 (이전 커밋처럼 동기적으로 로드)
    val localBusStops = remember {
        val dataSource = BusStopDataSource()
        dataSource.getAllBusStops()
    }

    // 마지막 업데이트 위치 (중복 업데이트 방지)
    var lastUpdatedLocation by remember { mutableStateOf<MapLocation?>(null) }

    // 맵이 준비되었는지 여부
    var isMapReady by remember { mutableStateOf(false) }
    
    // 버스 마커 아이콘 (리사이즈된 이미지, 64dp 크기)
    val busMarkerIcon = remember {
        ImageUtils.createResizedOverlayImage(context, R.drawable.ic_bus_marker, sizeInDp = 64)
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

    // 버스 마커 업데이트 (busMarkers 변경 시)
    LaunchedEffect(busMarkers, naverMap, isMapReady) {
        naverMap?.let { map ->
            if (isMapReady) {
                // 기존 버스 마커 제거
                busMarkerList.forEach { marker ->
                    marker.map = null
                }
                busMarkerList.clear()

                // 새로운 버스 마커 추가 (커스텀 버스 아이콘 사용, 리사이즈된 이미지)
                busMarkers.forEach { busLocation ->
                    val busMarker = Marker().apply {
                        position = LatLng(busLocation.latitude, busLocation.longitude)
                        captionText = busLocation.caption ?: "버스"
                        icon = busMarkerIcon
                        tag = "bus_${busLocation.latitude}_${busLocation.longitude}"
                    }

                    busMarker.setOnClickListener {
                        // 버스 마커 클릭 시 카메라 이동
                        val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                            LatLng(busLocation.latitude + MAP_MARKER_LATITUDE_OFFSET, busLocation.longitude),
                            MAP_MARKER_ZOOM_LEVEL
                        ).animate(CameraAnimation.Easing, MAP_CAMERA_ANIMATION_DURATION_MS.toLong())
                        map.moveCamera(cameraUpdate)
                        true
                    }

                    busMarker.map = map
                    busMarkerList.add(busMarker)
                }
            }
        }
    }


    // 라이프사이클 관리
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
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
                        isLocationButtonEnabled = true // 네이버 SDK 현재 위치 버튼 사용
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

                    // 정류장 마커 추가 (로컬에서 가져온 데이터)
                    localBusStops.forEach { busStop ->
                        val marker = Marker().apply {
                            position = LatLng(busStop.latitude, busStop.longitude)
                            captionText = busStop.name
                            icon = OverlayImage.fromResource(R.drawable.ic_location_tracking)
                            tag = busStop
                        }

                        marker.setOnClickListener {
                            // 마커 클릭 시 카메라 이동 (모달 위치를 고려하여 위로 오프셋)
                            val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                                LatLng(busStop.latitude + MAP_MARKER_LATITUDE_OFFSET, busStop.longitude),
                                MAP_MARKER_ZOOM_LEVEL
                            ).animate(CameraAnimation.Easing, MAP_CAMERA_ANIMATION_DURATION_MS.toLong())
                            map.moveCamera(cameraUpdate)

                            onBusStopMarkerClick(busStop)
                            true
                        }

                        marker.map = map
                        busStopMarkerList.add(marker)
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
