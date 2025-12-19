package com.tukorea.bus.ui.home

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.ui.common.BottomModal
import com.tukorea.bus.ui.map.BusStatus
import com.tukorea.bus.ui.map.MapViewModel
import com.tukorea.bus.ui.map.NaverMapView
import com.tukorea.bus.ui.navigation.Screen
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
    val mapState by mapViewModel.state.collectAsStateWithLifecycle()

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
                BottomModal(
                    modalHeight = ModalHeight.MID,
                    onModalHeightChange = {},
                    screenHeight = screenHeight,
                    density = density,
                    isVisible = mapState.isBusStopModalVisible
                ) {
                    com.tukorea.bus.ui.map.BusStopInfoModal(
                        busStop = mapState.selectedBusStop!!,
                        onDismiss = {
                            mapViewModel.closeBusStopModal()
                            viewModel.updateModalHeight(ModalHeight.MID)
                        },
                        onViewTimeTable = {
                            onNavigateTo(Screen.QuickRide.route)
                        },
                        onRideStart = { bus ->
                            if (bus.status == BusStatus.DEPARTED) {
                                // 버스가 이미 출발한 경우 바로 탑승 화면으로 이동
                                mapViewModel.closeBusStopModal()
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

            // 홈 모달 (정류장 모달이 없을 때만 표시)
            BottomModal(
                modalHeight = uiState.modalHeight,
                onModalHeightChange = viewModel::updateModalHeight,
                screenHeight = screenHeight,
                density = density,
                isVisible = uiState.isModalVisible && !mapState.isBusStopModalVisible
            ) {
                // 읽지 않은 중요 알림이 있을 때만 공지사항 배너 표시
                if (uiState.hasUnreadImportantNotice) {
                    NoticeBanner(
                        onNavigateTo = onNavigateTo,
                        firstUnreadImportantNoticeId = uiState.firstUnreadImportantNoticeId
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val hasReservation = remember(uiState.nextReservation) { uiState.nextReservation != null }

                if (hasReservation) {
                    NextReservationCard(
                        reservation = uiState.nextReservation!!, onNavigateTo = onNavigateTo
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    NoReservationCard(onNavigateTo = onNavigateTo)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                QuickActionGrid(onNavigateTo = onNavigateTo)

                Spacer(modifier = Modifier.height(16.dp))

                RealtimeArrivalInfo(
                    runningBusesCount = uiState.runningBusesCount,
                    waitingBusesCount = uiState.waitingBusesCount
                )
            }

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


@Composable
fun NoticeBanner(
    onNavigateTo: (String) -> Unit,
    firstUnreadImportantNoticeId: Int? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 첫 번째 중요 알림이 있으면 그 상세 화면으로, 없으면 알림 목록으로 이동
                if (firstUnreadImportantNoticeId != null) {
                    onNavigateTo(Screen.NotificationDetail.createRoute(firstUnreadImportantNoticeId))
                } else {
                    onNavigateTo(Screen.Notifications.route)
                }
            },
        shape = RoundedCornerShape(16.dp),
        color = Orange50,
        border = BorderStroke(1.5.dp, Orange600)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Orange600, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.home_notice_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(id = R.string.home_notice_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun NextReservationCard(
    reservation: Reservation, onNavigateTo: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "reservation_card_scale"
    )

    val gradientColors = remember {
        listOf(
            PrimaryBlue, Blue500
        )
    }

    // 접근성용 설명 텍스트는 Composable 컨텍스트에서 미리 계산한다
    val reservationA11yText = stringResource(
        id = R.string.home_next_reservation_a11y,
        reservation.time,
        reservation.from,
        reservation.to
    )

    // 실시간 위치 보기 버튼 접근성 텍스트
    val realtimeLocationA11y = stringResource(id = R.string.home_realtime_view_location_a11y)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = remember(gradientColors) {
                    Brush.horizontalGradient(colors = gradientColors)
                })
            .clickable(
                interactionSource = interactionSource,
                onClick = { onNavigateTo(Screen.Ride.route) })
            .semantics {
                contentDescription = reservationA11yText
            }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.home_next_reservation_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = reservation.time,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsTransit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reservation.from,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = reservation.to,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(Screen.Ride.route) }
                    .semantics {
                        // Composable 컨텍스트에서 미리 계산한 문자열을 사용
                        contentDescription = realtimeLocationA11y
                    }, shape = RoundedCornerShape(12.dp), color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.home_realtime_view_location),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }
        }
    }
}

@Composable
fun NoReservationCard(onNavigateTo: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아이콘
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Blue50, CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = PrimaryBlue
                )
            }

            // 메시지
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.home_no_reservation_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(
                    text = stringResource(id = R.string.home_no_reservation_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500
                )
            }

            // 예약하기 버튼
            Button(
                onClick = { onNavigateTo(Screen.Calendar.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(id = R.string.home_reserve_button),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionGrid(onNavigateTo: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.LocalFireDepartment,
            label = stringResource(id = R.string.home_quick_ride_label),
            subtitle = stringResource(id = R.string.home_quick_ride_subtitle),
            backgroundColor = Blue50,
            iconColor = PrimaryBlue,
            textColor = Gray900,
            subtitleColor = Gray500,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(Screen.QuickRide.route) })
        QuickActionButton(
            icon = Icons.Default.DateRange,
            label = stringResource(id = R.string.home_reservation_label),
            subtitle = stringResource(id = R.string.home_reservation_subtitle),
            backgroundColor = Green50,
            iconColor = SuccessGreen,
            textColor = Gray900,
            subtitleColor = Gray500,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(Screen.Calendar.route) })
    }
}


@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    subtitle: String,
    backgroundColor: Color,
    iconColor: Color,
    textColor: Color,
    subtitleColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "quick_action_scale"
    )

    // elevation을 파생 상태로 최적화
    val elevation = remember(isPressed) {
        if (isPressed) 2.dp else 1.dp
    }

    Card(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .semantics {
                contentDescription = "$label 버튼, $subtitle"
            }, onClick = onClick, colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 아이콘 박스
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 제목과 부제목
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor
                )
            }
        }
    }
}

@Composable
fun RealtimeArrivalInfo(
    runningBusesCount: Int = 0,
    waitingBusesCount: Int = 0
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(id = R.string.home_realtime_info_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard(
                label = stringResource(id = R.string.home_realtime_running),
                value = if (runningBusesCount > 0)
                    stringResource(id = R.string.home_realtime_bus_count, runningBusesCount)
                else
                    "",
                color = SuccessGreen
            )
            InfoCard(
                label = stringResource(id = R.string.home_realtime_waiting),
                value = if (waitingBusesCount > 0)
                    stringResource(id = R.string.home_realtime_bus_count, waitingBusesCount)
                else
                    "",
                color = PrimaryBlue
            )
        }
    }
}


@Composable
fun RowScope.InfoCard(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label, style = MaterialTheme.typography.labelMedium, color = Gray600
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// Preview 함수 제거 - 실제 백엔드 연동 시 사용하지 않음

