package com.tukorea.bus.ui.ride

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.bus.ui.map.MapViewModel
import com.tukorea.bus.ui.map.NaverMapView
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RideScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: RideViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mapState by mapViewModel.state.collectAsStateWithLifecycle()
    val remainingMinutes = uiState.remainingMinutes
    val busStatus = uiState.busStatus

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            mapViewModel.onLocationPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (locationPermissionsState.allPermissionsGranted) {
            mapViewModel.loadCurrentLocation()
        }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val modalHeight = screenHeight * 0.4f

    Box(modifier = Modifier.fillMaxSize()) {
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
                (modalHeight.toPx()).toInt()
            },
            onMapInitialized = { naverMap ->
            }
        )

        FloatingActionButton(
            onClick = { onNavigateTo(Screen.Home.route) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding(),
            containerColor = White,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "홈",
                tint = Gray700
            )
        }

        FloatingActionButton(
            onClick = { mapViewModel.loadCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = with(density) { (modalHeight.toPx()).toInt().dp + 16.dp }),
            containerColor = White,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "현재 위치",
                tint = Gray700
            )
        }

        RideInfoModal(
            remainingMinutes = remainingMinutes,
            departureStation = uiState.departureStation,
            departureTime = uiState.departureTime,
            arrivalStation = uiState.arrivalStation,
            arrivalTime = uiState.arrivalTime,
            busStatus = busStatus,
            onBoardingStationClick = { /* TODO: 탑승 정류장 표시 기능 구현 */ },
            onStatusClick = { /* TODO: 버스 상태 상세 화면 구현 */ },
            onHomeClick = { onNavigateTo(Screen.Home.route) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun RideInfoModal(
    remainingMinutes: Int,
    departureStation: String,
    departureTime: String,
    arrivalStation: String,
    arrivalTime: String,
    busStatus: String,
    onBoardingStationClick: () -> Unit,
    onStatusClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = White,
        shadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Gray300, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (remainingMinutes > 0) "${remainingMinutes}분" else "곧",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Text(
                        text = " 후 도착",
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
                        text = "탑승정류장",
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
                                    text = "셔틀버스",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Gray900
                                )
                                Text(
                                    text = "A노선",
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
                                text = "상태",
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
                    text = "셔틀버스 홈으로",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RideScreenPreview() {
    BusTheme {
        RideScreen(onNavigateTo = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun RideInfoModalPreview() {
    BusTheme {
        RideInfoModal(
            remainingMinutes = 5,
            departureStation = "정왕역",
            departureTime = "0:30 AM",
            arrivalStation = "제 1캠퍼스",
            arrivalTime = "3:30 AM",
            busStatus = "운행중",
            onBoardingStationClick = {},
            onStatusClick = {},
            onHomeClick = {}
        )
    }
}

