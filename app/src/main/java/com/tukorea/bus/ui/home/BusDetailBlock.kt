package com.tukorea.bus.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tukorea.bus.R
import com.tukorea.bus.domain.model.BusTracking
import com.tukorea.bus.domain.model.LiveBus
import com.tukorea.bus.domain.model.LiveBusStatus
import com.tukorea.bus.domain.model.ShuttleStop
import com.tukorea.bus.ui.common.RowDivider
import com.tukorea.bus.ui.common.StatusDot
import com.tukorea.bus.ui.common.nameRes
import com.tukorea.bus.ui.theme.BusTheme
import com.tukorea.bus.ui.theme.busColors
import kotlinx.coroutines.delay

private val Inset = 24.dp

/**
 * 지도에서 버스를 눌렀을 때의 시트 내용(내비게이션처럼).
 * 맨 위 큰 줄에 "정왕역까지 약 3분", 아래 평평한 행에 남은 거리·속도·위치 갱신 시각.
 */
@Composable
fun BusDetailBlock(tracking: BusTracking, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val bus = MaterialTheme.busColors
    val live = tracking.bus
    val (statusRes, dotColor) = when (live.status) {
        LiveBusStatus.RUNNING -> R.string.bus_detail_running to bus.success
        LiveBusStatus.WAITING -> R.string.bus_detail_waiting to bus.warning
        LiveBusStatus.STOP -> R.string.bus_detail_stopped to bus.textTertiary
    }

    Column(modifier = modifier.padding(horizontal = Inset)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusDot(color = dotColor, pulsing = live.status == LiveBusStatus.RUNNING)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(statusRes),
                style = MaterialTheme.typography.labelLarge,
                color = bus.textSecondary,
            )
            CloseButton(onClick = onClose)
        }
        Spacer(modifier = Modifier.height(4.dp))

        val stop = tracking.nextStop
        val eta = tracking.etaMinutes
        Text(
            text = when {
                stop == null -> stringResource(R.string.bus_detail_off_route)
                eta == null || eta <= 1 -> stringResource(R.string.bus_detail_arriving, stringResource(stop.nameRes()))
                else -> stringResource(R.string.bus_detail_eta, stringResource(stop.nameRes()), eta)
            },
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))

        tracking.remainingMeters?.let {
            DetailRow(label = stringResource(R.string.bus_detail_remaining), value = formatDistance(it))
            RowDivider()
        }
        DetailRow(
            label = stringResource(R.string.bus_detail_speed),
            value = tracking.speedKmh?.let { stringResource(R.string.bus_detail_speed_value, it) }
                ?: stringResource(R.string.bus_detail_unknown),
        )
        RowDivider()
        DetailRow(label = stringResource(R.string.bus_detail_updated), value = secondsAgo(live.updatedAtMillis))

        if (stop != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.bus_detail_estimate_note),
                style = MaterialTheme.typography.bodySmall,
                color = bus.textTertiary,
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.busColors.textSecondary)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CloseButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(36.dp),
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.action_close),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun formatDistance(meters: Int): String =
    if (meters < 1000) {
        stringResource(R.string.bus_detail_meters, meters / 10 * 10)
    } else {
        stringResource(R.string.bus_detail_km, "%.1f".format(meters / 1000.0))
    }

/** 1초마다 다시 그려 "3초 전"이 실제로 흘러가게 한다. */
@Composable
private fun secondsAgo(updatedAtMillis: Long): String {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val seconds = ((now - updatedAtMillis) / 1000).coerceAtLeast(0)
    return if (seconds < 2) {
        stringResource(R.string.bus_detail_just_now)
    } else {
        stringResource(R.string.bus_detail_seconds_ago, seconds)
    }
}

@Preview(showBackground = true)
@Composable
private fun BusDetailPreview() {
    BusTheme {
        BusDetailBlock(
            tracking = BusTracking(
                bus = LiveBus("bus1", 37.34, 126.73, LiveBusStatus.RUNNING, System.currentTimeMillis()),
                headingDegrees = 40.0,
                speedKmh = 32,
                nextStop = ShuttleStop.JEONGWANG_STATION,
                remainingMeters = 1240,
                etaMinutes = 3,
            ),
            onClose = {},
        )
    }
}
