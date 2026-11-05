package com.tukorea.bus.ui.rides

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.RideIntent
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.EmptyState
import com.tukorea.bus.ui.common.ScreenTitle
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.label
import com.tukorea.bus.ui.common.nameRes
import com.tukorea.bus.ui.theme.busColors
import java.util.Calendar
import java.util.TimeZone

@Composable
fun MyRidesRoute(viewModel: MyRidesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.messageRes) {
        val res = state.messageRes ?: return@LaunchedEffect
        snackbar.showSnackbar(context.getString(res))
        viewModel.consumeMessage()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MyRidesScreen(state = state, onCancel = viewModel::cancel, onRetry = viewModel::refresh)
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun MyRidesScreen(
    state: MyRidesUiState,
    onCancel: (Long) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenTitle(
                modifier = Modifier.padding(bottom = 8.dp),
                title = stringResource(R.string.my_rides_title),
                subtitle = stringResource(R.string.my_rides_subtitle),
            )
        }
        when {
            state.rides.isNotEmpty() -> items(state.rides, key = { it.id }) { ride ->
                RideCard(
                    ride = ride,
                    dateLabel = relativeDate(ride.serviceDate, state.today),
                    isCancelling = ride.id in state.cancellingIds,
                    onCancel = { onCancel(ride.id) },
                )
            }
            state.isLoading -> Unit
            state.loadFailed -> item {
                EmptyState(
                    title = stringResource(R.string.error_network),
                    body = stringResource(R.string.home_no_timetable_body),
                    action = { BusButton(text = stringResource(R.string.action_retry), onClick = onRetry) },
                )
            }
            else -> item {
                EmptyState(
                    title = stringResource(R.string.my_rides_empty_title),
                    body = stringResource(R.string.my_rides_empty_body),
                )
            }
        }
    }
}

@Composable
private fun RideCard(ride: RideIntent, dateLabel: String, isCancelling: Boolean, onCancel: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.busColors.textSecondary,
                )
                Text(
                    text = formatMinute(ride.departMinute),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(text = ride.direction.label(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = stringResource(R.string.my_rides_drop_off, stringResource(ride.dropOff.nameRes())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.busColors.textSecondary,
                )
            }
            TextButton(onClick = onCancel, enabled = !isCancelling) {
                Text(
                    text = stringResource(R.string.my_rides_cancel),
                    color = MaterialTheme.busColors.danger,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** "오늘", "내일", "11월 8일" */
@Composable
private fun relativeDate(isoDate: String, today: String): String {
    if (isoDate == today) return stringResource(R.string.date_today)
    val parts = isoDate.split("-").mapNotNull { it.toIntOrNull() }
    if (parts.size != 3) return isoDate
    val tomorrow = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
        add(Calendar.DAY_OF_MONTH, 1)
    }
    val isTomorrow = tomorrow.get(Calendar.YEAR) == parts[0] &&
        tomorrow.get(Calendar.MONTH) + 1 == parts[1] &&
        tomorrow.get(Calendar.DAY_OF_MONTH) == parts[2]
    if (isTomorrow) return stringResource(R.string.date_tomorrow)
    return stringResource(R.string.date_month_day, parts[1], parts[2])
}
