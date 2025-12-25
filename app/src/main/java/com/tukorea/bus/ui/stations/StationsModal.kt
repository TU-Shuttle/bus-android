package com.tukorea.bus.ui.stations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.BusStop
import com.tukorea.bus.domain.model.BusStopBusInfo
import com.tukorea.bus.domain.model.BusStatus
import com.tukorea.bus.ui.common.BottomModal
import com.tukorea.bus.ui.common.ConfirmDeleteDialog
import com.tukorea.bus.ui.home.ModalHeight
import com.tukorea.bus.ui.theme.*

/**
 * 정류장 정보를 보여주는 모달 컴포넌트
 * 공통 BottomModal을 사용하여 구현됩니다.
 */
@Composable
fun StationsModal(
    busStop: BusStop,
    buses: List<BusStopBusInfo>, // ViewModel에서 관리하는 버스 목록
    isVisible: Boolean,
    modalHeight: ModalHeight = ModalHeight.MID,
    onModalHeightChange: (ModalHeight) -> Unit = {},
    screenHeight: Dp? = null,
    density: Density? = null,
    onDismiss: () -> Unit,
    onViewTimeTable: () -> Unit,
    onRideStart: (BusStopBusInfo) -> Unit = {}
) {
    val localScreenHeight = screenHeight ?: LocalConfiguration.current.screenHeightDp.dp
    val localDensity = density ?: LocalDensity.current

    // 확인 다이얼로그 상태
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedBus by remember { mutableStateOf<BusStopBusInfo?>(null) }

    BottomModal(
        modalHeight = modalHeight,
        onModalHeightChange = onModalHeightChange,
        screenHeight = localScreenHeight,
        density = localDensity,
        isVisible = isVisible
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더: 정류장 이름
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = busStop.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = busStop.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Gray100, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.bus_stop_close_cd),
                        tint = Gray700,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = Gray200)

            // 운행 중인 버스 목록
            if (buses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Gray50
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsTransit,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Gray300
                        )
                        Text(
                            text = stringResource(id = R.string.bus_stop_no_buses),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray400
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(id = R.string.bus_stop_running_buses_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    buses.forEach { bus ->
                        BusInfoCard(
                            bus = bus,
                            onClick = {
                                selectedBus = bus
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // 전체 시간표 보기 버튼
            OutlinedButton(
                onClick = onViewTimeTable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue
                ),
                border = androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.bus_stop_view_timetable),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // 탑승 확인 다이얼로그
    selectedBus?.let { bus ->
        ConfirmDeleteDialog(
            showDialog = showConfirmDialog,
            onDismiss = {
                showConfirmDialog = false
                selectedBus = null
            },
            onConfirm = {
                showConfirmDialog = false
                onRideStart(bus)
                selectedBus = null
            },
            title = stringResource(id = R.string.bus_ride_confirm_title),
            message = stringResource(id = R.string.bus_ride_confirm_message, bus.route, bus.time, bus.destination),
            icon = Icons.Default.DirectionsTransit,
            confirmButtonText = stringResource(id = R.string.common_confirm),
            dismissButtonText = stringResource(id = R.string.common_cancel),
            confirmButtonColor = PrimaryBlue
        )
    }
}

/**
 * 버스 정보 카드
 */
@Composable
fun BusInfoCard(
    bus: BusStopBusInfo,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bus_info_card_scale"
    )

    val routeColor = remember(bus.route) {
        when (bus.route) {
            "A노선" -> Pair(Blue100, Blue700)
            "B노선" -> Pair(Green100, Green700)
            "C노선" -> Pair(Orange100, Orange700)
            "순환" -> Pair(Orange100, Orange700)
            else -> Pair(Blue100, Blue700)
        }
    }

    val statusColor = remember(bus.status) {
        when (bus.status) {
            BusStatus.RUNNING -> Pair(Green100, Green700)
            BusStatus.WAITING -> Pair(Orange50, Orange600)
            BusStatus.FINISHED -> Pair(Gray200, Gray600)
        }
    }

    val statusText = when (bus.status) {
        BusStatus.RUNNING -> stringResource(id = R.string.bus_stop_status_running)
        BusStatus.WAITING -> stringResource(id = R.string.bus_stop_status_waiting)
        BusStatus.FINISHED -> stringResource(id = R.string.bus_stop_status_finished)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(interactionSource = interactionSource, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = routeColor.first
                    ) {
                        Text(
                            text = bus.route,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = routeColor.second
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = statusColor.first
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = statusColor.second
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.bus_stop_arrival_time, bus.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(
                    text = stringResource(id = R.string.bus_stop_destination, bus.destination),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Gray100
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsTransit,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp),
                    tint = Gray700
                )
            }
        }
    }
}

