package com.tukorea.bus.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.Reservation
import com.tukorea.bus.ui.common.BottomModal
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.theme.*

/**
 * 홈 화면의 모달 컴포넌트
 * 공통 BottomModal을 사용하여 구현됩니다.
 */
@Composable
fun HomeModal(
    hasUnreadImportantNotice: Boolean,
    firstUnreadImportantNoticeId: Int?,
    nextReservation: Reservation?,
    runningBusesCount: Int,
    waitingBusesCount: Int,
    modalHeight: ModalHeight,
    onModalHeightChange: (ModalHeight) -> Unit,
    screenHeight: Dp? = null,
    density: Density? = null,
    isVisible: Boolean,
    onNavigateTo: (String) -> Unit
) {
    val localScreenHeight = screenHeight ?: LocalConfiguration.current.screenHeightDp.dp
    val localDensity = density ?: LocalDensity.current

    BottomModal(
        modalHeight = modalHeight,
        onModalHeightChange = onModalHeightChange,
        screenHeight = localScreenHeight,
        density = localDensity,
        isVisible = isVisible
    ) {
        // 읽지 않은 중요 알림이 있을 때만 공지사항 배너 표시
        if (hasUnreadImportantNotice) {
            NoticeBanner(
                onNavigateTo = onNavigateTo,
                firstUnreadImportantNoticeId = firstUnreadImportantNoticeId
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        val hasReservation = remember(nextReservation) { nextReservation != null }

        if (hasReservation) {
            NextReservationCard(
                reservation = nextReservation!!,
                onNavigateTo = onNavigateTo
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
    reservation: Reservation,
    onNavigateTo: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reservation_card_scale"
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
                }
            )
            .clickable(
                interactionSource = interactionSource,
                onClick = { onNavigateTo(Screen.Ride.route) }
            )
            .semantics {
                contentDescription = reservationA11yText
            }
    ) {
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
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
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
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color.White
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
                        Blue50,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
            onClick = { onNavigateTo(Screen.QuickRide.route) }
        )
        QuickActionButton(
            icon = Icons.Default.DateRange,
            label = stringResource(id = R.string.home_reservation_label),
            subtitle = stringResource(id = R.string.home_reservation_subtitle),
            backgroundColor = Green50,
            iconColor = SuccessGreen,
            textColor = Gray900,
            subtitleColor = Gray500,
            modifier = Modifier.weight(1f),
            onClick = { onNavigateTo(Screen.Calendar.route) }
        )
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
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "quick_action_scale"
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
            },
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
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
    runningBusesCount: Int,
    waitingBusesCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.home_realtime_info_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildString {
                        if (runningBusesCount > 0) {
                            append(stringResource(id = R.string.home_realtime_running))
                            append(" ")
                            append(stringResource(id = R.string.home_realtime_bus_count, runningBusesCount))
                        }
                        if (runningBusesCount > 0 && waitingBusesCount > 0) {
                            append(" · ")
                        }
                        if (waitingBusesCount > 0) {
                            append(stringResource(id = R.string.home_realtime_waiting))
                            append(" ")
                            append(stringResource(id = R.string.home_realtime_bus_count, waitingBusesCount))
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = PrimaryBlue
            )
        }
    }
}

