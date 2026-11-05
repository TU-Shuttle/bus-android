package com.tukorea.bus.ui.timetable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.Departure
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.DirectionPicker
import com.tukorea.bus.ui.common.EmptyState
import com.tukorea.bus.ui.common.ScreenTitle
import com.tukorea.bus.ui.common.SectionLabel
import com.tukorea.bus.ui.common.SegmentedControl
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.labelRes
import com.tukorea.bus.ui.ride.RideSheet
import com.tukorea.bus.ui.theme.busColors

private val DAY_TYPES = listOf(DayType.WEEKDAY, DayType.SATURDAY)

@Composable
fun TimetableRoute(viewModel: TimetableViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(state.messageRes) {
        val res = state.messageRes ?: return@LaunchedEffect
        snackbar.showSnackbar(context.getString(res))
        viewModel.consumeMessage()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        TimetableScreen(
            state = state,
            onSelectDayType = viewModel::selectDayType,
            onSelectDirection = viewModel::selectDirection,
            onTapDeparture = viewModel::openRide,
            onRetry = viewModel::retry,
        )
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))
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

@Composable
fun TimetableScreen(
    state: TimetableUiState,
    onSelectDayType: (DayType) -> Unit,
    onSelectDirection: (com.tukorea.bus.domain.model.ShuttleDirection) -> Unit,
    onTapDeparture: (Departure) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenTitle(
                modifier = Modifier.padding(horizontal = 24.dp),
                title = stringResource(R.string.timetable_title),
                subtitle = when {
                    state.title.isBlank() -> null
                    state.period != null -> stringResource(R.string.timetable_period, state.title, state.period)
                    else -> state.title
                },
            )
        }
        item {
            SegmentedControl(
                modifier = Modifier.padding(horizontal = 24.dp),
                options = DAY_TYPES.map { stringResource(it.labelRes()) },
                selectedIndex = DAY_TYPES.indexOf(state.selectedDayType),
                onSelect = { onSelectDayType(DAY_TYPES[it]) },
            )
        }

        when {
            state.isLoading -> item { Loading() }
            state.loadFailed -> item {
                EmptyState(
                    title = stringResource(R.string.home_no_timetable_title),
                    body = stringResource(R.string.home_no_timetable_body),
                    action = { BusButton(text = stringResource(R.string.action_retry), onClick = onRetry) },
                )
            }
            state.directions.isEmpty() -> item {
                EmptyState(title = stringResource(R.string.timetable_empty), body = "")
            }
            else -> {
                item {
                    DirectionPicker(
                        directions = state.directions,
                        selected = state.selectedDirection,
                        onSelect = onSelectDirection,
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        unselectedColor = MaterialTheme.colorScheme.surface,
                    )
                }
                items(state.onDemand, key = { "od-${it.minute}" }) { OnDemandCard(it) }
                if (state.isToday && state.hours.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            text = stringResource(R.string.timetable_tap_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.busColors.textTertiary,
                        )
                    }
                }
                items(state.hours, key = { "h-${it.hour}" }) { row ->
                    HourRowItem(row = row, isToday = state.isToday, onTap = onTapDeparture)
                }
                if (state.notes.isNotEmpty()) {
                    item { SectionLabel(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.timetable_notes)) }
                    items(state.notes, key = { "n-${it.minute}" }) { dep ->
                        Text(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            text = "${formatMinute(dep.minute)}  ${dep.note}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnDemandCard(departure: Departure) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(
                    R.string.timetable_on_demand,
                    formatMinute(departure.minute),
                    formatMinute(departure.endMinute ?: departure.minute),
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            val note = departure.note?.takeUnless { it == stringResource(R.string.home_on_demand_title) }
            if (note != null) {
                Text(text = note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.busColors.textSecondary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HourRowItem(row: HourRow, isToday: Boolean, onTap: (Departure) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            modifier = Modifier
                .width(48.dp)
                .padding(top = 8.dp),
            text = stringResource(R.string.timetable_hour, row.hour),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isToday && row.cells.all { it.isPast }) MaterialTheme.busColors.textTertiary else MaterialTheme.colorScheme.onBackground,
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.cells.forEach { cell -> TimeChip(cell = cell, enabled = isToday && cell.isReservable, onClick = { onTap(cell.departure) }) }
        }
    }
}

@Composable
private fun TimeChip(cell: TimeCell, enabled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bus = MaterialTheme.busColors
    val (container, content) = when {
        cell.isNext -> scheme.primary to scheme.onPrimary
        cell.isPast -> scheme.background to bus.textTertiary
        else -> scheme.surface to scheme.onSurface
    }
    val time = formatMinute(cell.departure.minute)
    Surface(
        modifier = Modifier.semantics { contentDescription = time },
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = container,
        border = when {
            cell.isReserved -> BorderStroke(2.dp, bus.success)
            cell.isNext || cell.isPast -> null
            else -> BorderStroke(1.dp, scheme.outlineVariant)
        },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            text = time + if (cell.departure.note != null) "*" else "",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (cell.isNext) FontWeight.Bold else FontWeight.Medium,
            color = content,
        )
    }
}

@Composable
private fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
    }
}
