package com.tukorea.bus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.ui.theme.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        SettingsHeader()

        SettingsContent(
            isPushEnabled = uiState.isPushNotificationEnabled,
            isReservationEnabled = uiState.isReservationNotificationEnabled,
            onPushToggle = { enabled -> viewModel.onPushNotificationChanged(enabled) },
            onReservationToggle = { enabled -> viewModel.onReservationNotificationChanged(enabled) }
        )
    }
}

@Composable
private fun SettingsHeader() {
    Text(
        text = stringResource(id = R.string.settings_title),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = Gray900,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
private fun SettingsContent(
    isPushEnabled: Boolean,
    isReservationEnabled: Boolean,
    onPushToggle: (Boolean) -> Unit,
    onReservationToggle: (Boolean) -> Unit
) {
    // 알림 섹션 (토글)
    SettingsSection(title = stringResource(id = R.string.settings_section_notification)) {
        SettingsToggleRow(
            icon = Icons.Default.Notifications,
            title = stringResource(id = R.string.settings_push_title),
            subtitle = stringResource(id = R.string.settings_push_subtitle),
            checked = isPushEnabled,
            onCheckedChange = onPushToggle,
            showDivider = true
        )
        SettingsToggleRow(
            icon = Icons.Default.Schedule,
            title = stringResource(id = R.string.settings_reservation_title),
            subtitle = stringResource(id = R.string.settings_reservation_subtitle),
            checked = isReservationEnabled,
            onCheckedChange = onReservationToggle,
            showDivider = false
        )
    }

    // 기타 섹션 (클릭형) - 앱 버전/앱 정보 항목 제거, 추후 필요 시 확장 가능
    SettingsSection(title = stringResource(id = R.string.settings_section_etc)) {
        SettingsItemRow(
            icon = Icons.Default.Help,
            title = stringResource(id = R.string.settings_help_title),
            subtitle = null,
            onClick = { /* TODO: 도움말 화면 이동이 필요하면 여기에서 처리 */ },
            showDivider = false
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray700,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Switch,
                    interactionSource = interactionSource,
                    indication = null   // ✅ 눌렀을 때 어두워지는 효과 제거
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Gray900
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.CenterVertically)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Gray200
            )
        }
    }
}

/**
 * ▶ 클릭 타입 아이템 (앱 정보, 도움말 등)
 */
@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Gray900
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Gray200
            )
        }
    }
}
