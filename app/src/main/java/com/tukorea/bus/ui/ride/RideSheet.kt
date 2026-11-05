package com.tukorea.bus.ui.ride

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.ui.common.BusButton
import com.tukorea.bus.ui.common.formatMinute
import com.tukorea.bus.ui.common.label
import com.tukorea.bus.ui.common.nameRes
import com.tukorea.bus.ui.theme.busColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideSheet(
    state: RideSheetState,
    dateLabel: String,
    onSelect: (ShuttleStop) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.ride_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.ride_sheet_subtitle,
                        dateLabel,
                        formatMinute(state.target.departMinute),
                        state.target.direction.label(),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.busColors.textSecondary,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.options.forEach { stop ->
                    DropOffOption(
                        name = stringResource(stop.nameRes()),
                        selected = stop == state.selected,
                        onClick = { onSelect(stop) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.ride_sheet_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.busColors.textTertiary,
            )

            BusButton(
                text = stringResource(R.string.ride_sheet_confirm),
                onClick = onConfirm,
                enabled = !state.isSubmitting,
            )
        }
    }
}

@Composable
private fun DropOffOption(name: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(12.dp),
        color = scheme.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(20.dp),
                    shape = CircleShape,
                    color = scheme.surface,
                    border = BorderStroke(2.dp, if (selected) scheme.primary else scheme.outline),
                ) {}
                if (selected) {
                    Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = scheme.primary) {}
                }
            }
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
