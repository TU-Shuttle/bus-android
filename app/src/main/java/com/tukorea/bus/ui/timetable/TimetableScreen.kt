package com.tukorea.bus.ui.timetable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.DirectionPicker
import com.tukorea.bus.ui.common.EmptyState
import com.tukorea.bus.ui.common.RowDivider
import com.tukorea.bus.ui.common.SectionLabel
import com.tukorea.bus.ui.common.SegmentedControl
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.labelRes
import com.tukorea.bus.ui.theme.busColors

private val DAY_TYPES = listOf(DayType.WEEKDAY, DayType.SATURDAY)
private val Inset = 24.dp
private val HourColumn = 56.dp
private val MinuteWidth = 44.dp

/** 목록 위쪽 고정 항목 수(제목, 노선 선택). 자동 스크롤 위치 계산에 쓴다. */
private const val LEADING_ITEMS = 2

@Composable
fun TimetableRoute(viewModel: TimetableViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TimetableScreen(
        state = state,
        onSelectDayType = viewModel::selectDayType,
        onSelectDirection = viewModel::selectDirection,
        onRetry = viewModel::retry,
    )
}

/**
 * 시간대별 한 줄 시간표. 왼쪽에 시(時), 오른쪽에 분(分)만 적는 익숙한 버스 시간표 형식이다.
 * 오늘 시간표면 지난 시각은 흐리게, 다음 출발 하나만 검은 알약으로 강조하고 그 줄로 스크롤한다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen(
    state: TimetableUiState,
    onSelectDayType: (DayType) -> Unit,
    onSelectDirection: (ShuttleDirection) -> Unit,
    onRetry: () -> Unit,
) {
    val listState = rememberLazyListState()
    // 노선·요일을 바꿀 때만 맞춘다(15초마다 갱신되는 현재 시각 때문에 스크롤이 튀지 않게).
    LaunchedEffect(state.selectedDirection, state.selectedDayType, state.isLoading) {
        val focus = state.focusIndex
        if (focus != null && focus >= 2) {
            listState.scrollToItem(LEADING_ITEMS + focus - 1)
        } else {
            listState.scrollToItem(0)
        }
    }

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
            state.entries.isEmpty() -> item {
                EmptyState(title = stringResource(R.string.timetable_empty), body = "")
            }
            else -> {
                val lastKey = state.entries.last().key
                items(state.entries, key = { it.key }) { entry ->
                    val divider = entry.key != lastKey
                    when (entry) {
                        is TimetableEntry.Hour -> HourRowItem(entry, divider)
                        is TimetableEntry.OnDemand -> OnDemandRow(entry, divider)
                    }
                }
                if (state.notes.isNotEmpty()) {
                    item(key = "notes") {
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

/** `9시 │ 00  20  40` 한 줄. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HourRowItem(row: TimetableEntry.Hour, divider: Boolean) {
    val bus = MaterialTheme.busColors
    Column(modifier = Modifier.padding(horizontal = Inset)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            HourLabel(text = stringResource(R.string.timetable_hour, row.hour), dimmed = row.isPast)
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.cells.forEach { MinuteCell(it) }
            }
        }
        if (divider) RowDivider()
    }
}

@Composable
private fun HourLabel(text: String, dimmed: Boolean) {
    Box(modifier = Modifier.width(HourColumn).height(36.dp), contentAlignment = Alignment.CenterStart) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (dimmed) MaterialTheme.busColors.textTertiary else MaterialTheme.busColors.textSecondary,
        )
    }
}

/** 분 하나. 다음 출발=검은 알약, 지난 시각=흐린 글자, 안내 있음=오른쪽 위 주황 점. */
@Composable
private fun MinuteCell(cell: TimeCell) {
    val scheme = MaterialTheme.colorScheme
    val bus = MaterialTheme.busColors
    val minute = (cell.departure.minute % 60).toString().padStart(2, '0')
    val description = listOfNotNull(
        formatMinute(cell.departure.minute),
        stringResource(R.string.a11y_next_departure).takeIf { cell.isNext },
        stringResource(R.string.a11y_has_note).takeIf { cell.departure.note != null },
    ).joinToString(", ")
    val (container, content) = when {
        cell.isNext -> scheme.primary to scheme.onPrimary
        cell.isPast -> Color.Transparent to bus.textTertiary
        else -> Color.Transparent to scheme.onBackground
    }
    Surface(
        modifier = Modifier
            .size(width = MinuteWidth, height = 36.dp)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(12.dp),
        color = container,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = minute,
                style = MaterialTheme.typography.titleMedium,
                // 지난 시각은 굵기도 낮춰 앞으로 탈 시각과 경쟁하지 않게 한다.
                fontWeight = if (cell.isPast) FontWeight.Normal else null,
                color = content,
            )
            if (cell.departure.note != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(5.dp)
                        .background(bus.warning, CircleShape),
                )
            }
        }
    }
}

/** `17시 │ 00 ~ 18:00 수시운행` 한 줄. 시작 분은 다른 줄의 분과 같은 칸에 맞춘다. */
@Composable
private fun OnDemandRow(entry: TimetableEntry.OnDemand, divider: Boolean) {
    val bus = MaterialTheme.busColors
    val departure = entry.departure
    val onDemandTitle = stringResource(R.string.home_on_demand_title)
    val color = if (entry.isPast) bus.textTertiary else MaterialTheme.colorScheme.onBackground
    Column(modifier = Modifier.padding(horizontal = Inset)) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
            HourLabel(text = stringResource(R.string.timetable_hour, departure.minute / 60), dimmed = entry.isPast)
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.height(36.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(MinuteWidth), contentAlignment = Alignment.Center) {
                        Text(
                            text = (departure.minute % 60).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.titleMedium,
                            color = color,
                        )
                    }
                    Text(
                        text = "~ ${formatMinute(departure.endMinute ?: departure.minute)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = color,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = onDemandTitle, style = MaterialTheme.typography.bodyMedium, color = bus.textSecondary)
                }
                val note = departure.note?.takeUnless { it == onDemandTitle }
                if (note != null) {
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = bus.textSecondary,
                    )
                }
            }
        }
        if (divider) RowDivider()
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
