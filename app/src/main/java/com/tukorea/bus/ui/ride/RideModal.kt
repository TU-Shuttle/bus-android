package com.tukorea.bus.ui.ride

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R
import com.tukorea.bus.ui.common.BottomModal
import com.tukorea.bus.ui.home.ModalHeight
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.theme.*

/**
 * 탑승 화면의 모달 컴포넌트
 * 공통 BottomModal을 사용하여 구현됩니다.
 */
@Composable
fun RideModal(
    remainingMinutes: Int,
    departureStation: String,
    departureTime: String,
    arrivalStation: String,
    arrivalTime: String,
    busStatus: String,
    modalHeight: ModalHeight,
    onModalHeightChange: (ModalHeight) -> Unit,
    screenHeight: Dp? = null,
    density: Density? = null,
    isVisible: Boolean,
    onBoardingStationClick: () -> Unit,
    onStatusClick: () -> Unit,
    onHomeClick: () -> Unit
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
        RideInfoContent(
            remainingMinutes = remainingMinutes,
            departureStation = departureStation,
            departureTime = departureTime,
            arrivalStation = arrivalStation,
            arrivalTime = arrivalTime,
            busStatus = busStatus,
            onBoardingStationClick = onBoardingStationClick,
            onStatusClick = onStatusClick,
            onHomeClick = onHomeClick
        )
    }
}

@Composable
fun RideInfoContent(
    remainingMinutes: Int,
    departureStation: String,
    departureTime: String,
    arrivalStation: String,
    arrivalTime: String,
    busStatus: String,
    onBoardingStationClick: () -> Unit,
    onStatusClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val arrivalText = if (remainingMinutes > 0) {
                stringResource(id = R.string.ride_arrival_in_minutes, remainingMinutes)
            } else {
                stringResource(id = R.string.ride_arrival_soon)
            }
            Text(
                text = arrivalText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        }

        OutlinedButton(
            onClick = onBoardingStationClick,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            ),
            border = BorderStroke(1.5.dp, Blue100)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = PrimaryBlue
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = R.string.ride_boarding_station),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Gray50,
        border = BorderStroke(1.dp, Gray200)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = departureStation,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = departureTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    val dashWidth = 8f
                    val dashGap = 6f
                    val pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashWidth, dashGap),
                        0f
                    )

                    drawLine(
                        color = Gray400,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2f,
                        pathEffect = pathEffect
                    )

                    drawLine(
                        color = PrimaryBlue,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width * 0.3f, size.height / 2),
                        strokeWidth = 3f
                    )

                    drawCircle(
                        color = PrimaryBlue,
                        radius = 6f,
                        center = Offset(size.width * 0.3f, size.height / 2)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = arrivalStation,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = arrivalTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Blue50
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = PrimaryBlue
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(id = R.string.ride_bus_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Text(
                            text = stringResource(id = R.string.ride_bus_route_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                }

                TextButton(
                    onClick = onStatusClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.ride_status_button),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    OutlinedButton(
        onClick = onHomeClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Gray700
        ),
        border = BorderStroke(1.5.dp, Gray200)
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.ride_home_button),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
}

