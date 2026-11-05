package com.tukorea.bus.ui.ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.RowDivider
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.label
import com.tukorea.bus.ui.common.nameRes
import com.tukorea.bus.ui.theme.busColors

/**
 * 탑승 예정 확인 시트. 내릴 곳이 하나면 확인만, 여러 곳이면 고르게 한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideSheet(
    state: RideSheetState,
    dateLabel: String,
    onSelect: (ShuttleStop) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val time = formatMinute(state.target.departMinute)
    val single = state.options.size == 1
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.ride_sheet_single_title, time),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.ride_sheet_meta, state.target.direction.label(), dateLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.busColors.textSecondary,
            )

            if (!single) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.ride_sheet_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.busColors.textSecondary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                state.options.forEachIndexed { index, stop ->
                    if (index > 0) RowDivider()
                    DropOffRow(
                        name = stringResource(stop.nameRes()),
                        selected = stop == state.selected,
                        onClick = { onSelect(stop) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.ride_sheet_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.busColors.textSecondary,
            )
            Spacer(modifier = Modifier.height(20.dp))
            BusButton(
                text = stringResource(R.string.ride_sheet_confirm),
                onClick = onConfirm,
                enabled = !state.isSubmitting,
            )
        }
    }
}

@Composable
private fun DropOffRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.busColors.textSecondary,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
