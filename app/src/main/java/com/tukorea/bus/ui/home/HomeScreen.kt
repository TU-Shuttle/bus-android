package com.tukorea.bus.ui.home

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeRoute(
    onOpenTimetable: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    )
    LaunchedEffect(Unit) {
        if (!locationPermission.allPermissionsGranted) locationPermission.launchMultiplePermissionRequest()
    }


    // 버스를 따라가는 중이면 뒤로 가기로 먼저 따라가기를 끝낸다.
    BackHandler(enabled = state.selectedBus != null, onBack = viewModel::clearSelectedBus)

    var sheetHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        ShuttleMap(
            modifier = Modifier.fillMaxSize(),
            direction = state.selectedDirection,
            buses = state.liveBuses,
            showMyLocation = locationPermission.allPermissionsGranted,
            bottomPadding = sheetHeight,
            route = state.route,
            headings = state.headings,
            selectedBusId = state.selectedBus?.bus?.id,
            onBusClick = viewModel::selectBus,
            onMapClick = viewModel::clearSelectedBus,
        )

        HomeSheet(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { sheetHeight = with(density) { it.height.toDp() } },
            state = state,
            callbacks = HomeSheetCallbacks(
                onSelectDirection = viewModel::selectDirection,
                onOpenTimetable = onOpenTimetable,
                onRetry = viewModel::refresh,
                onCloseBus = viewModel::clearSelectedBus,
            ),
        )
    }
}
