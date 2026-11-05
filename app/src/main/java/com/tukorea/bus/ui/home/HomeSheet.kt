package com.tukorea.bus.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.UpcomingDepartures
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.BusButtonStyle
import com.tukorea.bus.ui.common.DirectionPicker
import com.tukorea.bus.ui.common.StatusDot
import com.tukorea.bus.ui.common.boardingRes
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.nameRes
import com.tukorea.bus.ui.theme.BusTheme
import com.tukorea.bus.ui.theme.busColors

data class HomeSheetCallbacks(
    val onSelectDirection: (ShuttleDirection) -> Unit,
    val onReserve: () -> Unit,
    val onCancelReserve: () -> Unit,
    val onOpenTimetable: () -> Unit,
    val onRetry: () -> Unit,
)

@Composable
fun HomeSheet(
    state: HomeUiState,
    callbacks: HomeSheetCallbacks,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 16.dp,
    ) {
        Column(modifier = Modifier.padding(top = 24.dp, bottom = 20.dp)) {
            when {
                state.isLoading -> LoadingBlock()
                state.loadFailed -> NoTimetableBlock(onRetry = callbacks.onRetry)
                else -> ScheduleBlock(state, callbacks)
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            LiveRow(state = state, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
private fun ScheduleBlock(state: HomeUiState, callbacks: HomeSheetCallbacks) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(R.string.home_where_to),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (state.directions.isNotEmpty()) {
                DirectionPicker(
                    directions = state.directions,
                    selected = state.selectedDirection,
                    onSelect = callbacks.onSelectDirection,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                )
            }
        }

        state.outOfPeriod?.let { period ->
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(R.string.home_out_of_period, period),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.busColors.warning,
            )
        }

        AnimatedContent(
            modifier = Modifier.padding(horizontal = 24.dp),
            targetState = state.selectedDirection to state.upcoming,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "upcoming",
        ) { (direction, upcoming) ->
            when (upcoming) {
                null, UpcomingDepartures.NoService -> NoServiceBlock()
                is UpcomingDepartures.Finished -> FinishedBlock(upcoming.firstDeparture, callbacks.onOpenTimetable)
                is UpcomingDepartures.Scheduled -> UpcomingBlock(
                    origin = direction?.origin,
                    upcoming = upcoming,
                    state = state,
                    callbacks = callbacks,
                )
            }
        }
    }
}

@Composable
private fun UpcomingBlock(
    origin: ShuttleStop?,
    upcoming: UpcomingDepartures.Scheduled,
    state: HomeUiState,
    callbacks: HomeSheetCallbacks,
) {
    val next = upcoming.next.firstOrNull()
    val bus = MaterialTheme.busColors
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(
                    if (upcoming.onDemandUntil != null) R.string.home_on_demand_now else R.string.home_next_shuttle,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = bus.textSecondary,
            )
            if (next != null) {
                Countdown(minutesLeft = next.minute - state.nowMinute, departMinute = next.minute)
            } else {
                Text(
                    text = stringResource(R.string.home_on_demand_title),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            val details = buildList {
                if (next == null && upcoming.onDemandUntil != null) {
                    add(stringResource(R.string.home_on_demand_until, formatMinute(upcoming.onDemandUntil)))
                }
                origin?.let { add(stringResource(R.string.home_board_at, stringResource(it.boardingRes()))) }
                if (next != null && state.nextRiders > 0) {
                    add(stringResource(R.string.home_riders_planned, state.nextRiders))
                }
            }
            Text(
                text = details.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
            )
            val later = upcoming.next.drop(1)
            if (later.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.home_later, later.joinToString("  ") { formatMinute(it.minute) }),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            next?.note?.let { note ->
                Text(text = note, style = MaterialTheme.typography.bodySmall, color = bus.warning)
            }
        }

        if (next != null) {
            val reserved = state.reservedForNext
            if (reserved == null) {
                BusButton(text = stringResource(R.string.home_reserve), onClick = callbacks.onReserve)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.home_reserved, stringResource(reserved.dropOff.nameRes())),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = bus.success,
                    )
                    BusButton(
                        text = stringResource(R.string.home_cancel_reserve),
                        onClick = callbacks.onCancelReserve,
                        style = BusButtonStyle.Secondary,
                    )
                }
            }
        }
    }
}

/**
 * 1시간 안이면 "8 분 후 ……… 14:10 출발", 그보다 멀면 "10:00 출발 ……… 7시간 1분 후".
 */
@Composable
private fun Countdown(minutesLeft: Int, departMinute: Int) {
    if (minutesLeft >= 60) {
        FarCountdown(minutesLeft = minutesLeft, departMinute = departMinute)
        return
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        if (minutesLeft <= 1) {
            Text(
                text = stringResource(R.string.home_departing_now),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
            )
        } else {
            Text(
                text = stringResource(R.string.home_minutes_left_value, minutesLeft),
                fontSize = 56.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                text = stringResource(R.string.home_minutes_left_unit),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(R.string.home_depart_at, formatMinute(departMinute)),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.busColors.textSecondary,
        )
    }
}

@Composable
private fun FarCountdown(minutesLeft: Int, departMinute: Int) {
    val hours = minutesLeft / 60
    val minutes = minutesLeft % 60
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(
            text = formatMinute(departMinute),
            fontSize = 56.sp,
            lineHeight = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(R.string.home_depart_unit),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = if (minutes == 0) {
                stringResource(R.string.home_in_hours, hours)
            } else {
                stringResource(R.string.home_in_hours_minutes, hours, minutes)
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.busColors.textSecondary,
        )
    }
}

@Composable
private fun FinishedBlock(first: Departure, onOpenTimetable: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.home_finished_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_finished_first, formatMinute(first.minute)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.busColors.textSecondary,
            )
        }
        BusButton(
            text = stringResource(R.string.home_view_timetable),
            onClick = onOpenTimetable,
            style = BusButtonStyle.Secondary,
        )
    }
}

@Composable
private fun NoServiceBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.home_no_service_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.home_no_service_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.busColors.textSecondary,
        )
    }
}

@Composable
private fun NoTimetableBlock(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.home_no_timetable_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_no_timetable_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.busColors.textSecondary,
            )
        }
        BusButton(text = stringResource(R.string.action_retry), onClick = onRetry)
    }
}

@Composable
private fun LoadingBlock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
    }
}

@Composable
private fun LiveRow(state: HomeUiState, modifier: Modifier = Modifier) {
    val bus = MaterialTheme.busColors
    val running = state.runningCount
    val text = when {
        state.liveUnavailable && state.liveBuses.isEmpty() -> stringResource(R.string.live_unavailable)
        state.liveBuses.isEmpty() -> stringResource(R.string.live_none)
        else -> buildList {
            if (running > 0) add(stringResource(R.string.live_running, running))
            if (state.waitingCount > 0) add(stringResource(R.string.live_waiting, state.waitingCount))
        }.joinToString(" · ")
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusDot(color = if (running > 0) bus.success else bus.textTertiary, pulsing = running > 0)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeSheetPreview() {
    BusTheme {
        HomeSheet(
            state = HomeUiState(
                isLoading = false,
                directions = listOf(ShuttleDirection.STATION_TO_CAMPUS1, ShuttleDirection.CAMPUS1_TO_STATION),
                selectedDirection = ShuttleDirection.STATION_TO_CAMPUS1,
                upcoming = UpcomingDepartures.Scheduled(
                    onDemandUntil = null,
                    next = listOf(
                        Departure(ShuttleDirection.STATION_TO_CAMPUS1, DayType.WEEKDAY, 14 * 60 + 10),
                        Departure(ShuttleDirection.STATION_TO_CAMPUS1, DayType.WEEKDAY, 14 * 60 + 20),
                    ),
                ),
                nowMinute = 14 * 60 + 2,
                nextRiders = 3,
            ),
            callbacks = HomeSheetCallbacks({}, {}, {}, {}, {}),
        )
    }
}
