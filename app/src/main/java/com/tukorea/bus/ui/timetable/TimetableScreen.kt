package com.tukorea.bus.ui.timetable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.DayType
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.DirectionPicker
import com.tukorea.bus.ui.common.EmptyState
import com.tukorea.bus.ui.common.RowDivider
import com.tukorea.bus.ui.common.SegmentedControl
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.labelRes
import com.tukorea.bus.ui.common.weekdaysLabel
import com.tukorea.bus.ui.theme.busColors

private val DAY_TYPES = listOf(DayType.WEEKDAY, DayType.SATURDAY)
private val Inset = 24.dp
private const val RELATIVE_LIMIT_MINUTES = 120

@Composable
fun TimetableRoute(viewModel: TimetableViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TimetableScreen(
        state = state,
        onSelectDayType = viewModel::selectDayType,
        onSelectDirection = viewModel::selectDirection,
        onRetry = viewModel::retry,
        onTogglePast = viewModel::togglePast,
    )
}

/**
 * 앞으로 탈 출발만 한 줄씩 크게 보여 주는 시간표(우버 탑승 목록처럼).
 * 오늘 시간표면 남은 시간을 오른쪽에 붙이고, 지난 출발은 맨 위에 접어 둔다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen(
    state: TimetableUiState,
    onSelectDayType: (DayType) -> Unit,
    onSelectDirection: (ShuttleDirection) -> Unit,
    onRetry: () -> Unit,
    onTogglePast: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.selectedDirection, state.selectedDayType) { listState.scrollToItem(0) }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item(key = "title") {
            TitleRow(state = state, onSelectDayType = onSelectDayType)
        }
        // 노선 선택은 스크롤해도 위에 붙어 있다.
        stickyHeader(key = "directions") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                if (state.directions.isNotEmpty()) {
                    DirectionPicker(
                        modifier = Modifier.padding(vertical = 12.dp),
                        directions = state.directions,
                        selected = state.selectedDirection,
                        onSelect = onSelectDirection,
                        contentPadding = PaddingValues(horizontal = Inset),
                    )
                }
                RowDivider()
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
            state.past.isEmpty() && state.upcoming.isEmpty() -> item {
                EmptyState(title = stringResource(R.string.timetable_empty), body = "")
            }
            else -> {
                if (state.past.isNotEmpty()) {
                    item(key = "past-toggle") {
                        PastToggle(count = state.past.size, expanded = state.showPast, onClick = onTogglePast)
                    }
                    if (state.showPast) {
                        items(state.past, key = { "p-${it.key}" }) { DepartureRowItem(it) }
                    }
                }
                if (state.upcoming.isEmpty()) {
                    item(key = "finished") {
                        Text(
                            modifier = Modifier.padding(horizontal = Inset, vertical = 24.dp),
                            text = stringResource(R.string.timetable_finished_today),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.busColors.textSecondary,
                        )
                    }
                }
                items(state.upcoming, key = { "u-${it.key}" }) { DepartureRowItem(it) }
            }
        }
    }
}

@Composable
private fun TitleRow(state: TimetableUiState, onSelectDayType: (DayType) -> Unit) {
    val subtitle = when {
        state.title.isBlank() -> null
        state.period != null -> stringResource(R.string.timetable_period, state.title, state.period)
        else -> state.title
    }
    Column(modifier = Modifier.padding(start = Inset, end = Inset, top = 24.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.timetable_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            SegmentedControl(
                modifier = Modifier.width(148.dp),
                options = DAY_TYPES.map { stringResource(it.labelRes()) },
                selectedIndex = DAY_TYPES.indexOf(state.selectedDayType),
                onSelect = { onSelectDayType(DAY_TYPES[it]) },
            )
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.busColors.textSecondary)
        }
    }
}

@Composable
private fun PastToggle(count: Int, expanded: Boolean, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Inset, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.timetable_past_count, count),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.busColors.textSecondary,
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.busColors.textSecondary,
            )
        }
        RowDivider(modifier = Modifier.padding(horizontal = Inset))
    }
}

/**
 * 출발 한 줄: 왼쪽 큰 시각, 오른쪽 남은 시간.
 * 이번 셔틀은 아래에 "이번 셔틀"을 붙이고 남은 시간을 진하게, 지난 출발은 흐리게.
 */
@Composable
private fun DepartureRowItem(row: DepartureRow) {
    val bus = MaterialTheme.busColors
    val scheme = MaterialTheme.colorScheme
    val dep = row.departure
    val onDemandTitle = stringResource(R.string.home_on_demand_title)
    val timeText = if (dep.isOnDemand) {
        "${formatMinute(dep.minute)} ~ ${formatMinute(dep.endMinute ?: dep.minute)}"
    } else {
        formatMinute(dep.minute)
    }
    val days = dep.daysOfWeek?.let { stringResource(R.string.timetable_only_days, weekdaysLabel(it)) }
    val caption = listOfNotNull(
        stringResource(R.string.timetable_this_shuttle).takeIf { row.isNext },
        days,
    ).joinToString(" · ").ifEmpty { null }
    val note = dep.note?.takeUnless { it == onDemandTitle }
    val trailing = when {
        row.isRunningNow -> stringResource(R.string.timetable_running_now)
        dep.isOnDemand -> onDemandTitle
        // 두 시간 넘게 남은 출발까지 남은 시간을 붙이면 숫자만 늘어난다.
        row.minutesUntil != null && row.minutesUntil < RELATIVE_LIMIT_MINUTES -> relativeTime(row.minutesUntil)
        else -> null
    }

    Column(modifier = Modifier.padding(horizontal = Inset)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (row.isPast) bus.textTertiary else scheme.onBackground,
                )
                if (caption != null) {
                    Text(text = caption, style = MaterialTheme.typography.bodyMedium, color = scheme.onBackground)
                }
                if (note != null) {
                    Text(text = note, style = MaterialTheme.typography.bodySmall, color = bus.textSecondary)
                }
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = trailing,
                    style = if (row.isNext) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (row.isNext) FontWeight.Bold else null,
                    color = if (row.isNext) scheme.onBackground else bus.textSecondary,
                )
            }
        }
        RowDivider()
    }
}

@Composable
private fun relativeTime(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        minutes == 0 -> stringResource(R.string.timetable_departing_now)
        hours == 0 -> stringResource(R.string.timetable_in_minutes, minutes)
        rest == 0 -> stringResource(R.string.timetable_in_hours, hours)
        else -> stringResource(R.string.timetable_in_hours_minutes, hours, rest)
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
