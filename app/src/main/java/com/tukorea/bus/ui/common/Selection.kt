package com.tukorea.bus.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tukorea.bus.domain.model.ShuttleDirection
import com.tukorea.bus.ui.theme.busColors

/** 선택되면 검정(다크 모드에선 흰색)으로 채워지는 알약형 버튼. */
@Composable
fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(if (selected) scheme.primary else unselectedColor, label = "pill")
    val content by animateColorAsState(if (selected) scheme.onPrimary else scheme.onSurface, label = "pill_text")

    Surface(
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.Tab),
        shape = RoundedCornerShape(50),
        color = container,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )
    }
}

@Composable
fun DirectionPicker(
    directions: List<ShuttleDirection>,
    selected: ShuttleDirection?,
    onSelect: (ShuttleDirection) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    /** 회색 배경 위에 놓일 땐 surface(흰색)로 바꿔야 버튼 경계가 보인다. */
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
    ) {
        items(directions, key = { it.name }) { direction ->
            SelectablePill(
                text = direction.label(),
                selected = direction == selected,
                onClick = { onSelect(direction) },
                unselectedColor = unselectedColor,
            )
        }
    }
}

/** 두세 개 중 하나를 고르는 세그먼트 컨트롤. 그림자 없이 트랙과 선택 칸의 명도 차이로만 구분한다. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            options.forEachIndexed { index, option ->
                val selected = index == selectedIndex
                val thumb by animateColorAsState(
                    if (selected) MaterialTheme.busColors.segmentThumb else MaterialTheme.colorScheme.surfaceVariant,
                    label = "segment",
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(selected = selected, onClick = { onSelect(index) }, role = Role.Tab),
                    shape = RoundedCornerShape(10.dp),
                    color = thumb,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.busColors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
