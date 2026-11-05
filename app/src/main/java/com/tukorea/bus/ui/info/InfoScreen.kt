package com.tukorea.bus.ui.info

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.BuildConfig
import com.tukorea.bus.R
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(title = stringResource(R.string.info_title))

        SectionLabel(text = stringResource(R.string.info_section_stops))
        InfoCard {
            InfoItem(stringResource(R.string.stop_jeongwang_station), stringResource(R.string.info_stop_jeongwang_body))
            Divider()
            InfoItem(stringResource(R.string.stop_campus1), stringResource(R.string.info_stop_campus1_body))
            Divider()
            InfoItem(stringResource(R.string.stop_campus1_west_gate), stringResource(R.string.info_stop_west_gate_body))
            Divider()
            InfoItem(stringResource(R.string.stop_campus2), stringResource(R.string.info_stop_campus2_body))
        }

        SectionLabel(text = stringResource(R.string.info_section_notice))
        InfoCard {
            Bullet(stringResource(R.string.info_notice_traffic))
            Bullet(stringResource(R.string.info_notice_crowded))
            Bullet(stringResource(R.string.info_notice_drink))
        }

        SectionLabel(text = stringResource(R.string.info_section_contact))
        InfoCard {
            ActionItem(
                title = stringResource(R.string.info_contact_name),
                body = stringResource(R.string.info_contact_phone),
                icon = Icons.Outlined.Call,
                onClick = { uriHandler.openUri(CONTACT_TEL.replace(" ", "")) },
            )
        }

        SectionLabel(text = stringResource(R.string.info_section_source))
        InfoCard {
            val sourceBody = listOfNotNull(
                state.timetableTitle?.let { title -> state.period?.let { "$title · $it" } ?: title },
                state.lastChecked?.let { stringResource(R.string.info_source_updated, it) },
                stringResource(R.string.info_source_body),
            ).joinToString("\n")
            ActionItem(
                title = stringResource(R.string.info_source_open),
                body = sourceBody,
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                onClick = { uriHandler.openUri(SCHOOL_SHUTTLE_URL) },
            )
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.info_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.busColors.textTertiary,
        )
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
    }
}

@Composable
private fun InfoItem(title: String, body: String) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.busColors.textSecondary)
    }
}

@Composable
private fun Bullet(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        text = "·  $text",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ActionItem(title: String, body: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.busColors.textSecondary)
        }
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)
}
