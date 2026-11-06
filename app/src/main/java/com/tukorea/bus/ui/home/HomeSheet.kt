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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.domain.model.UpcomingDepartures
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.BusButtonStyle
import com.tukorea.bus.ui.common.DirectionPicker
import com.tukorea.bus.ui.common.RowDivider
import com.tukorea.bus.ui.common.StatusDot
import com.tukorea.bus.ui.common.boardingRes
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.theme.BusTheme
import com.tukorea.bus.ui.theme.busColors

private val Inset = 24.dp

data class HomeSheetCallbacks(
    val onSelectDirection: (ShuttleDirection) -> Unit,
    val onOpenTimetable: () -> Unit,
    val onRetry: () -> Unit,
    val onCloseBus: () -> Unit = {},
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
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(top = 20.dp, bottom = 16.dp)) {
            val selectedBus = state.selectedBus
            when {
                selectedBus != null -> {
                    BusDetailBlock(tracking = selectedBus, onClose = callbacks.onCloseBus)
                    return@Column
                }
                state.isLoading -> LoadingBlock()
                state.loadFailed -> NoTimetableBlock(onRetry = callbacks.onRetry)
                else -> ScheduleBlock(state, callbacks)
            }
            RowDivider(modifier = Modifier.padding(horizontal = Inset, vertical = 16.dp))
            LiveRow(state = state, modifier = Modifier.padding(horizontal = Inset))
        }
    }
}

@Composable
private fun ScheduleBlock(state: HomeUiState, callbacks: HomeSheetCallbacks) {
    Column {
        Text(
            modifier = Modifier.padding(horizontal = Inset),
            text = stringResource(R.string.home_where_to),
            style = MaterialTheme.typography.titleLarge,
        )
        if (state.directions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            DirectionPicker(
                directions = state.directions,
                selected = state.selectedDirection,
                onSelect = callbacks.onSelectDirection,
                contentPadding = PaddingValues(horizontal = Inset),
            )
        }
        state.outOfPeriod?.let { period ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                modifier = Modifier.padding(horizontal = Inset),
                text = stringResource(R.string.home_out_of_period, period),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.busColors.warning,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        AnimatedContent(
            modifier = Modifier.padding(horizontal = Inset),
            targetState = state.selectedDirection to state.upcoming,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "upcoming",
        ) { (direction, upcoming) ->
            when (upcoming) {
                null, UpcomingDepartures.NoService -> MessageBlock(
                    title = stringResource(R.string.home_no_service_title),
                    body = stringResource(R.string.home_no_service_body),
                )
                is UpcomingDepartures.Finished -> Column {
                    MessageBlock(
                        title = stringResource(R.string.home_finished_title),
                        body = stringResource(R.string.home_finished_first, formatMinute(upcoming.firstDeparture.minute)),
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    BusButton(
                        text = stringResource(R.string.home_view_timetable),
                        onClick = callbacks.onOpenTimetable,
                        style = BusButtonStyle.Secondary,
                    )
                }
                is UpcomingDepartures.Scheduled -> UpcomingBlock(direction?.origin, upcoming, state, callbacks)
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
    Column {
        Text(
            text = stringResource(
                if (upcoming.onDemandUntil != null) R.string.home_on_demand_now else R.string.home_next_shuttle,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = bus.textSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (next != null) {
            Countdown(minutesLeft = next.minute - state.nowMinute, departMinute = next.minute)
        } else {
            Text(text = stringResource(R.string.home_on_demand_title), style = MaterialTheme.typography.displaySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))
        origin?.let {
            Text(
                text = stringResource(R.string.home_board_at, stringResource(it.boardingRes())),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        val secondary = buildList {
            if (next == null && upcoming.onDemandUntil != null) {
                add(stringResource(R.string.home_on_demand_until, formatMinute(upcoming.onDemandUntil)))
            }
            val later = upcoming.next.drop(1)
            if (later.isNotEmpty()) {
                add(stringResource(R.string.home_later, later.joinToString(" · ") { formatMinute(it.minute) }))
            }
            next?.note?.let(::add)
        }
        if (secondary.isNotEmpty()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = secondary.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = bus.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

    }
}

/**
 * 1시간 안이면 "8 분 후 …… 14:10 출발", 그보다 멀면 "10:00 출발 …… 7시간 후".
 * 큰 숫자와 단위는 같은 기준선에 둔다.
 */
@Composable
private fun Countdown(minutesLeft: Int, departMinute: Int) {
    val far = minutesLeft >= 60
    val soon = minutesLeft <= 1
    Row(modifier = Modifier.fillMaxWidth()) {
        when {
            soon -> Text(
                modifier = Modifier.alignByBaseline(),
                text = stringResource(R.string.home_departing_now),
                style = MaterialTheme.typography.displaySmall,
            )
            else -> {
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = if (far) formatMinute(departMinute) else stringResource(R.string.home_minutes_left_value, minutesLeft),
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    modifier = Modifier.alignByBaseline(),
                    text = stringResource(if (far) R.string.home_depart_unit else R.string.home_minutes_left_unit),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.alignByBaseline(),
            text = if (far) {
                stringResource(R.string.home_in_hours, (minutesLeft + 30) / 60)
            } else {
                stringResource(R.string.home_depart_at, formatMinute(departMinute))
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.busColors.textSecondary,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun MessageBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.busColors.textSecondary)
    }
}

@Composable
private fun NoTimetableBlock(onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = Inset)) {
        MessageBlock(
            title = stringResource(R.string.home_no_timetable_title),
            body = stringResource(R.string.home_no_timetable_body),
        )
        Spacer(modifier = Modifier.height(20.dp))
        BusButton(text = stringResource(R.string.action_retry), onClick = onRetry)
    }
}

@Composable
private fun LoadingBlock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
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
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        if (state.liveBuses.isNotEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.live_tap_hint),
                style = MaterialTheme.typography.bodySmall,
                color = bus.textTertiary,
            )
        }
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
            ),
            callbacks = HomeSheetCallbacks({}, {}, {}),
        )
    }
}
