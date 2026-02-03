package com.tukorea.bus.ui.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.bus.domain.model.BusMarkerLocation
import com.tukorea.bus.domain.repository.BusStatus
import com.tukorea.bus.ui.map.MapViewModel
import com.tukorea.bus.ui.map.NaverMapView
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.stations.StationsModal
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val mapState by mapViewModel.uiState.collectAsStateWithLifecycle()

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            mapViewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.modalHeight != ModalHeight.MID) {
            viewModel.updateModalHeight(ModalHeight.MID)
        }

        if (locationPermissionsState.allPermissionsGranted) {
            mapViewModel.loadCurrentLocation()
        }
    }


    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val density = LocalDensity.current

    // 모달 높이
    val fixedHeight = screenHeight * 0.35f

    // 서울시청 좌표에 버스 마커 추가
    val seoulCityHallBusMarker = remember {
        listOf(
            BusMarkerLocation(
                latitude = 37.5665, // 서울시청 위도
                longitude = 126.9780, // 서울시청 경도
                caption = "버스"
            )
        )
    }

    // 정류장 데이터는 MapViewModel에서 관리 (비동기 로드)
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NaverMapView(
                    modifier = Modifier.fillMaxSize(),
                    currentLocation = mapState.currentLocation,
                    isLocationPermissionGranted = locationPermissionsState.allPermissionsGranted,
                    busMarkers = seoulCityHallBusMarker, // 서울시청 좌표에 버스 마커 표시
                    busStops = mapState.busStops, // ViewModel에서 관리하는 정류장 목록 사용
                    onMapReady = {
                        if (locationPermissionsState.allPermissionsGranted) {
                            mapViewModel.loadCurrentLocation()
                        }
                    },
                    bottomPadding = with(density) {
                        (fixedHeight.toPx()).toInt()
                    },
                    onMapInitialized = { _ ->
                    },
                    onBusStopMarkerClick = { busStop ->
                        mapViewModel.onBusStopSelected(busStop)
                        viewModel.updateModalHeight(ModalHeight.HIGH)
                    },
                    onMapClick = {
                        // 지도 클릭 시 정류장 모달이 열려있으면 닫고, 홈 모달을 MID로 변경
                        if (mapState.isBusStopModalVisible) {
                            mapViewModel.closeBusStopModal()
                        }
                        // 홈 모달이 LOW 상태가 아니면 MID로 변경
                        if (uiState.modalHeight != ModalHeight.MID) {
                            viewModel.updateModalHeight(ModalHeight.MID)
                        }
                    }
                )
            }

            // 정류장 정보 모달 (홈 모달보다 위에 표시)
            if (mapState.isBusStopModalVisible && mapState.selectedBusStop != null) {
                var busStopModalHeight by remember { mutableStateOf(ModalHeight.MID) }

                StationsModal(
                    busStop = mapState.selectedBusStop!!,
                    buses = mapState.busesForSelectedStop,
                    isVisible = mapState.isBusStopModalVisible,
                    modalHeight = busStopModalHeight,
                    onModalHeightChange = { busStopModalHeight = it },
                    screenHeight = screenHeight,
                    density = density,
                    onDismiss = {
                        mapViewModel.closeBusStopModal()
                        viewModel.updateModalHeight(ModalHeight.MID)
                    },
                    onViewTimeTable = {
                        onNavigateTo(Screen.QuickRide.route)
                    },
                    onRideStart = { bus ->
                        if (bus.status == BusStatus.DEPARTED) {
                            mapViewModel.closeBusStopModal()
                            onNavigateTo(Screen.Ride.route)
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.bus_ride_alarm_reserved),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            // 홈 모달 (정류장 모달이 없을 때만 표시)
            HomeModal(
                hasUnreadImportantNotice = uiState.hasUnreadImportantNotice,
                firstUnreadImportantNoticeId = uiState.firstUnreadImportantNoticeId,
                nextReservation = uiState.nextReservation,
                runningBusesCount = uiState.runningBusesCount,
                waitingBusesCount = uiState.waitingBusesCount,
                modalHeight = uiState.modalHeight,
                onModalHeightChange = viewModel::updateModalHeight,
                screenHeight = screenHeight,
                density = density,
                isVisible = uiState.isModalVisible && !mapState.isBusStopModalVisible,
                onNavigateTo = onNavigateTo
            )

            // 알림 버튼 (오른쪽 상단)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 38.dp, end = 28.dp)
            ) {
                Surface(
                    onClick = { onNavigateTo(Screen.Notifications.route) },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = White,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = stringResource(id = R.string.home_notification_icon_cd),
                            tint = Gray900,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
