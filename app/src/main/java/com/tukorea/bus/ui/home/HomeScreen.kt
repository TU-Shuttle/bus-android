package com.tukorea.bus.ui.home

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tukorea.bus.R
import com.tukorea.bus.ui.ride.RideSheet

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeRoute(
    onOpenTimetable: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationPermission = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
    )
    LaunchedEffect(Unit) {
        if (!locationPermission.allPermissionsGranted) locationPermission.launchMultiplePermissionRequest()
    }

    LaunchedEffect(state.messageRes) {
        val res = state.messageRes ?: return@LaunchedEffect
        snackbar.showSnackbar(context.getString(res))
        viewModel.consumeMessage()
    }

    var sheetHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = Modifier.fillMaxSize()) {
        ShuttleMap(
            modifier = Modifier.fillMaxSize(),
            direction = state.selectedDirection,
            buses = state.liveBuses,
            showMyLocation = locationPermission.allPermissionsGranted,
            bottomPadding = sheetHeight,
        )

        HomeSheet(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { sheetHeight = with(density) { it.height.toDp() } },
            state = state,
            callbacks = HomeSheetCallbacks(
                onSelectDirection = viewModel::selectDirection,
                onReserve = viewModel::reserveNext,
                onCancelReserve = viewModel::cancelReservedNext,
                onOpenTimetable = onOpenTimetable,
                onRetry = viewModel::refresh,
            ),
        )

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = sheetHeight + 8.dp),
        )
    }

    state.rideSheet?.let { sheet ->
        RideSheet(
            state = sheet,
            dateLabel = stringResource(R.string.date_today),
            onSelect = viewModel::selectDropOff,
            onConfirm = viewModel::confirmRide,
            onDismiss = viewModel::dismissRideSheet,
        )
    }
}
