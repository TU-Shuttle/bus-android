package com.tukorea.bus.ui.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.BuildConfig
import com.tukorea.bus.R
import com.tukorea.bus.ui.common.BulletText
import com.tukorea.bus.ui.common.RowDivider
import com.tukorea.bus.ui.common.ScreenTitle
import com.tukorea.bus.ui.common.SectionLabel
import com.tukorea.bus.ui.theme.busColors

private const val SCHOOL_SHUTTLE_URL = "https://tukorea.ac.kr/tukorea/1136/subview.do"
private const val CONTACT_TEL = "tel:0318041 0082"

@Composable
fun InfoRoute(viewModel: InfoViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InfoScreen(state = state)
}

@Composable
fun InfoScreen(state: InfoUiState) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.info_title))

        SectionLabel(text = stringResource(R.string.info_section_stops))
        InfoRow(stringResource(R.string.stop_jeongwang_station), stringResource(R.string.info_stop_jeongwang_body))
        RowDivider()
        InfoRow(stringResource(R.string.stop_campus1), stringResource(R.string.info_stop_campus1_body))
        RowDivider()
        InfoRow(stringResource(R.string.stop_campus1_west_gate), stringResource(R.string.info_stop_west_gate_body))
        RowDivider()
        InfoRow(stringResource(R.string.stop_campus2), stringResource(R.string.info_stop_campus2_body))

        SectionLabel(text = stringResource(R.string.info_section_notice))
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BulletText(stringResource(R.string.info_notice_traffic))
            BulletText(stringResource(R.string.info_notice_crowded))
            BulletText(stringResource(R.string.info_notice_drink))
        }

        SectionLabel(text = stringResource(R.string.info_section_contact))
        ActionRow(
            title = stringResource(R.string.info_contact_name),
            body = stringResource(R.string.info_contact_phone),
            icon = Icons.Outlined.Call,
            onClick = { uriHandler.openUri(CONTACT_TEL.replace(" ", "")) },
        )

        SectionLabel(text = stringResource(R.string.info_section_source))
        val sourceBody = listOfNotNull(
            state.timetableTitle?.let { title -> state.period?.let { "$title · $it" } ?: title },
            state.lastChecked?.let { stringResource(R.string.info_source_updated, it) },
            stringResource(R.string.info_source_body),
        ).joinToString("\n")
        ActionRow(
            title = stringResource(R.string.info_source_open),
            body = sourceBody,
            icon = Icons.AutoMirrored.Outlined.OpenInNew,
            onClick = { uriHandler.openUri(SCHOOL_SHUTTLE_URL) },
        )

        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = stringResource(R.string.info_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.busColors.textTertiary,
        )
    }
}

@Composable
private fun InfoRow(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.busColors.textSecondary)
    }
}

@Composable
private fun ActionRow(title: String, body: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.busColors.textSecondary)
        }
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
    }
}
