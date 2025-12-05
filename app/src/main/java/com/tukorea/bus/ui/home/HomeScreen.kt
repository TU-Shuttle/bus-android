package com.tukorea.bus.ui.home

import android.Manifest
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.bus.domain.model.Reservation
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
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                )
            }

            BottomModal(
                reservation = uiState.nextReservation,
                modalHeight = uiState.modalHeight,
                onModalHeightChange = viewModel::updateModalHeight,
                onNavigateTo = onNavigateTo,
                screenHeight = screenHeight,
                density = density,
                isVisible = uiState.isModalVisible,
                runningBusesCount = uiState.runningBusesCount,
                waitingBusesCount = uiState.waitingBusesCount,
                hasUnreadImportantNotice = uiState.hasUnreadImportantNotice,
                firstUnreadImportantNoticeId = uiState.firstUnreadImportantNoticeId
            )

            // 알림 버튼 (오른쪽 상단)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
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
                            contentDescription = "알림",
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
fun BottomModal(
    reservation: Reservation?,
    modalHeight: ModalHeight,
    onModalHeightChange: (ModalHeight) -> Unit,
    onNavigateTo: (String) -> Unit,
    screenHeight: Dp,
    density: Density,
    isVisible: Boolean = true,
    runningBusesCount: Int = 0,
    waitingBusesCount: Int = 0,
    hasUnreadImportantNotice: Boolean = false,
    firstUnreadImportantNoticeId: Int? = null
) {
    // 3단계 높이 정의
    val lowHeightPx = with(density) { 80.dp.toPx() }                       // 1단계: 80dp (드래그 바만 보임)
    val midHeightPx = with(density) { (screenHeight * 0.40f).toPx() }      // 2단계: 40%
    val highHeightPx = with(density) { (screenHeight * 0.75f).toPx() }     // 3단계: 75% (상단 바에 안 가리게)

    // 현재 목표 높이 (단계별)
    val targetHeightPx = when (modalHeight) {
        ModalHeight.LOW -> lowHeightPx
        ModalHeight.MID -> midHeightPx
        ModalHeight.HIGH -> highHeightPx
    }

    // 드래그 상태
    var isDragging by remember { mutableStateOf(false) }

    // 현재 실제 높이 (드래그 중에도 유지)
    var currentHeightPx by remember { mutableStateOf(midHeightPx) }

    // modalHeight가 변경되면 목표 높이 업데이트
    LaunchedEffect(modalHeight) {
        currentHeightPx = when (modalHeight) {
            ModalHeight.LOW -> lowHeightPx
            ModalHeight.MID -> midHeightPx
            ModalHeight.HIGH -> highHeightPx
        }
    }

    // 애니메이션 높이 (드래그 중이 아닐 때만 목표로 이동)
    val animatedHeightPx by animateFloatAsState(
        targetValue = if (isVisible) {
            if (isDragging) currentHeightPx else targetHeightPx
        } else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "modal_height",
        finishedListener = {
            // 애니메이션 완료 후 현재 높이 업데이트
            if (!isDragging) {
                currentHeightPx = targetHeightPx
            }
        }
    )

    // 최종 높이
    val finalHeight = with(density) { animatedHeightPx.toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(finalHeight)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 드래그 핸들 영역 (터치 영역 확장)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                },
                                onDragEnd = {
                                    // 현재 높이를 기준으로 가장 가까운 단계로 스냅
                                    val newHeight = when {
                                        currentHeightPx < (lowHeightPx + midHeightPx) / 2 -> ModalHeight.LOW
                                        currentHeightPx < (midHeightPx + highHeightPx) / 2 -> ModalHeight.MID
                                        else -> ModalHeight.HIGH
                                    }

                                    // 드래그 종료
                                    isDragging = false

                                    // 항상 새로운 단계로 변경 (같은 단계여도 스냅되도록)
                                    onModalHeightChange(newHeight)
                                },
                                onDragCancel = {
                                    isDragging = false
                                },
                                onVerticalDrag = { _, dragAmount ->
                                    // 드래그량만큼 높이 변경 (아래로 = 양수 = 감소, 위로 = 음수 = 증가)
                                    currentHeightPx =
                                        (currentHeightPx - dragAmount).coerceIn(
                                            lowHeightPx,
                                            highHeightPx
                                        )
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = Gray300,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }

                val scrollState = rememberScrollState()

                // 스크롤이 맨 위에 있을 때 아래로 드래그하면 모달을 내리기 위한 nestedScroll
                val nestedScrollConnection = remember {
                    object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                        override fun onPreScroll(
                            available: androidx.compose.ui.geometry.Offset,
                            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                        ): androidx.compose.ui.geometry.Offset {
                            // 스크롤이 맨 위에 있고, 아래로 스크롤하려는 경우 (available.y > 0)
                            if (scrollState.value == 0 && available.y > 0) {
                                // 모달 높이를 줄임
                                currentHeightPx = (currentHeightPx - available.y).coerceIn(lowHeightPx, highHeightPx)
                                isDragging = true
                                return available // 스크롤 이벤트를 소비
                            }
                            return androidx.compose.ui.geometry.Offset.Zero
                        }

                        override fun onPostScroll(
                            consumed: androidx.compose.ui.geometry.Offset,
                            available: androidx.compose.ui.geometry.Offset,
                            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                        ): androidx.compose.ui.geometry.Offset {
                            return androidx.compose.ui.geometry.Offset.Zero
                        }

                        override suspend fun onPreFling(available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                            if (isDragging) {
                                // Fling 종료 시 가장 가까운 단계로 스냅
                                val newHeight = when {
                                    currentHeightPx < (lowHeightPx + midHeightPx) / 2 -> ModalHeight.LOW
                                    currentHeightPx < (midHeightPx + highHeightPx) / 2 -> ModalHeight.MID
                                    else -> ModalHeight.HIGH
                                }
                                isDragging = false
                                onModalHeightChange(newHeight)
                                return available // velocity를 소비
                            }
                            return androidx.compose.ui.unit.Velocity.Zero
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .nestedScroll(nestedScrollConnection)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 읽지 않은 중요 알림이 있을 때만 공지사항 배너 표시
                    if (hasUnreadImportantNotice) {
                        NoticeBanner(
                            onNavigateTo = onNavigateTo,
                            firstUnreadImportantNoticeId = firstUnreadImportantNoticeId
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    val hasReservation = remember(reservation) { reservation != null }

                    if (hasReservation) {
                        NextReservationCard(
                            reservation = reservation!!, onNavigateTo = onNavigateTo
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        NoReservationCard(onNavigateTo = onNavigateTo)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    QuickActionGrid(onNavigateTo = onNavigateTo)

                    Spacer(modifier = Modifier.height(16.dp))

                    RealtimeArrivalInfo(
                        runningBusesCount = runningBusesCount,
                        waitingBusesCount = waitingBusesCount
                    )

                    // 하단 여백 추가 (콘텐츠가 잘리지 않도록)
                    Spacer(modifier = Modifier.height(24.dp))
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
                    text = "공지사항",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "셔틀버스 운행 시간 변경 안내",
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
                contentDescription =
                    "다음 예약: ${reservation.time}, ${reservation.from}에서 ${reservation.to}로"
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
                        text = "다음 예약",
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
                        contentDescription = "실시간 위치 보기"
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
                        text = "실시간 위치 보기",
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
                    text = "다음 예약이 없습니다",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(
                    text = "일정을 예약하고 편리하게 이용하세요",
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
                    text = "예약하기",
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
            label = "빠른 탑승",
            subtitle = "지금 바로 타기",
            backgroundColor = Blue50,
            iconColor = PrimaryBlue,
            textColor = Gray900,
            subtitleColor = Gray500,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(Screen.QuickRide.route) })
        QuickActionButton(
            icon = Icons.Default.DateRange,
            label = "예약하기",
            subtitle = "미리 예약",
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
            text = "실시간 도착 정보",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoCard("운행중", if (runningBusesCount > 0) "${runningBusesCount}대" else "", SuccessGreen)
            InfoCard("대기중", if (waitingBusesCount > 0) "${waitingBusesCount}대" else "", PrimaryBlue)
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

