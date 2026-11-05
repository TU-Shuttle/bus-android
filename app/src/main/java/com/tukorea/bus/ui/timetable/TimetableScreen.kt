package com.tukorea.bus.ui.timetable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import com.tukorea.bus.ui.common.RowDivider
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
private val Inset = 24.dp

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen(
    state: TimetableUiState,
    onSelectDayType: (DayType) -> Unit,
    onSelectDirection: (com.tukorea.bus.domain.model.ShuttleDirection) -> Unit,
    onTapDeparture: (Departure) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            ScreenTitle(
                modifier = Modifier.padding(start = Inset, end = Inset, top = 24.dp, bottom = 20.dp),
                title = stringResource(R.string.timetable_title),
                subtitle = when {
                    state.title.isBlank() -> null
                    state.period != null -> stringResource(R.string.timetable_period, state.title, state.period)
                    else -> state.title
                },
            )
        }
        // 요일·노선 선택은 스크롤해도 위에 붙어 있다.
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 16.dp),
            ) {
                SegmentedControl(
                    modifier = Modifier.padding(horizontal = Inset),
                    options = DAY_TYPES.map { stringResource(it.labelRes()) },
                    selectedIndex = DAY_TYPES.indexOf(state.selectedDayType),
                    onSelect = { onSelectDayType(DAY_TYPES[it]) },
                )
                if (state.directions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DirectionPicker(
                        directions = state.directions,
                        selected = state.selectedDirection,
                        onSelect = onSelectDirection,
                        contentPadding = PaddingValues(horizontal = Inset),
                    )
                }
            }
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
                items(state.onDemand, key = { "od-${it.minute}" }) { OnDemandRow(it) }
                if (state.isCompact) {
                    item(key = "compact") {
                        CompactTimes(cells = state.hours.flatMap { it.cells }, isToday = state.isToday, onTap = onTapDeparture)
                    }
                } else {
                    items(state.hours, key = { "h-${it.hour}" }) { row ->
                        HourRowItem(row = row, isToday = state.isToday, onTap = onTapDeparture)
                    }
                }
                if (state.notes.isNotEmpty()) {
                    item {
                        SectionLabel(
                            modifier = Modifier.padding(horizontal = Inset),
                            text = stringResource(R.string.timetable_notes),
                        )
                    }
                    items(state.notes, key = { "n-${it.minute}" }) { dep -> NoteRow(dep) }
                }
            }
        }
    }
}

/** "08:40–10:00 수시운행" 한 줄 + 안내문. */
@Composable
private fun OnDemandRow(departure: Departure) {
    val bus = MaterialTheme.busColors
    Column(modifier = Modifier.padding(horizontal = Inset)) {
        Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${formatMinute(departure.minute)}–${formatMinute(departure.endMinute ?: departure.minute)}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_on_demand_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = bus.textSecondary,
                )
            }
            val note = departure.note?.takeUnless { it == stringResource(R.string.home_on_demand_title) }
            if (note != null) {
                Text(text = note, style = MaterialTheme.typography.bodySmall, color = bus.textSecondary)
            }
        }
        RowDivider()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HourRowItem(row: HourRow, isToday: Boolean, onTap: (Departure) -> Unit) {
    val allPast = isToday && row.cells.all { it.isPast }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Inset, end = Inset, top = 20.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            modifier = Modifier
                .width(48.dp)
                .padding(top = 10.dp),
            text = stringResource(R.string.timetable_hour, row.hour),
            style = MaterialTheme.typography.titleMedium,
            color = if (allPast) MaterialTheme.busColors.textTertiary else MaterialTheme.colorScheme.onBackground,
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.cells.forEach { cell ->
                TimeChip(cell = cell, enabled = isToday && cell.isReservable, onClick = { onTap(cell.departure) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactTimes(cells: List<TimeCell>, isToday: Boolean, onTap: (Departure) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Inset, end = Inset, top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cells.forEach { cell ->
            TimeChip(cell = cell, enabled = isToday && cell.isReservable, onClick = { onTap(cell.departure) })
        }
    }
}

/**
 * 고정 크기 시각 칩. 다음 출발=검정 채움, 지난 시각=흐린 글자, 예약함=초록 점, 안내 있음=주황 점.
 */
@Composable
private fun TimeChip(cell: TimeCell, enabled: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val bus = MaterialTheme.busColors
    val time = formatMinute(cell.departure.minute)
    val reservedLabel = stringResource(R.string.a11y_reserved)
    val noteLabel = stringResource(R.string.a11y_has_note)
    val (container, content) = when {
        cell.isNext -> scheme.primary to scheme.onPrimary
        cell.isPast -> scheme.background to bus.textTertiary
        else -> scheme.surfaceVariant to scheme.onSurface
    }
    Surface(
        modifier = Modifier
            .size(width = 56.dp, height = 40.dp)
            .semantics {
                contentDescription = listOfNotNull(
                    time,
                    reservedLabel.takeIf { cell.isReserved },
                    noteLabel.takeIf { cell.departure.note != null },
                ).joinToString(", ")
            },
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = container,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = time, style = MaterialTheme.typography.labelLarge, color = content)
            val dot = when {
                cell.isReserved -> bus.success
                cell.departure.note != null -> bus.warning
                else -> null
            }
            if (dot != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(6.dp)
                        .background(dot, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun NoteRow(departure: Departure) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Inset, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.busColors.warning, CircleShape),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = formatMinute(departure.minute), style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = departure.note.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.busColors.textSecondary,
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
