package com.tukorea.bus.ui.ride

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.bus.ui.map.MapViewModel
import com.tukorea.bus.ui.map.NaverMapView
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.ride.RideModal
import com.tukorea.bus.ui.theme.*
import com.tukorea.bus.R


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
            mapViewModel.loadCurrentLocation()
        }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMapView(
            modifier = Modifier.fillMaxSize(),
            currentLocation = mapState.currentLocation,
            isLocationPermissionGranted = locationPermissionsState.allPermissionsGranted,
            busStops = mapState.busStops, // ViewModel에서 관리하는 정류장 목록 사용
            onMapReady = {
                if (locationPermissionsState.allPermissionsGranted) {
                    mapViewModel.loadCurrentLocation()
                }
            },
            bottomPadding = with(LocalDensity.current) { 260.dp.toPx().toInt() },
            onMapInitialized = { }
        )

        FloatingActionButton(
            onClick = { mapViewModel.loadCurrentLocation() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = White,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(id = R.string.ride_my_location_cd),
                tint = Gray700
            )
        }

        RideModal(
            remainingMinutes = remainingMinutes,
            departureStation = uiState.departureStation,
            departureTime = uiState.departureTime,
            arrivalStation = uiState.arrivalStation,
            arrivalTime = uiState.arrivalTime,
            busStatus = busStatus,
            modalHeight = uiState.modalHeight,
            onModalHeightChange = viewModel::updateModalHeight,
            screenHeight = screenHeight,
            density = density,
            isVisible = true,
            onBoardingStationClick = { /* TODO */ },
            onStatusClick = { /* TODO */ },
            onHomeClick = { onNavigateTo(Screen.Home.route) }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RideScreenPreview() {
    BusTheme {
        RideScreen(onNavigateTo = {})
    }
}

