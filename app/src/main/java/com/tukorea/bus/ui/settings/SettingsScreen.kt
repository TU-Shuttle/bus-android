package com.tukorea.bus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.ui.theme.*

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
            appVersion = uiState.appVersion,
            onItemClick = { itemId ->
                when (itemId) {
                    "push_notification" -> { }
                    "reservation_notification" -> { }
                    "profile" -> { }
                    "logout" -> { }
                    "app_info" -> { }
                    "help" -> { }
                }
            }
        )
    }
}


@Composable
private fun SettingsHeader() {
    Text(
        text = "설정",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = Gray900,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
private fun SettingsContent(
    appVersion: String,
    onItemClick: (String) -> Unit
) {
    SettingsSection(title = "알림") {
        SettingsItemRow(
            icon = Icons.Default.Notifications,
            title = "푸시 알림",
            subtitle = "버스 도착 알림을 받습니다",
            onClick = { onItemClick("push_notification") },
            showDivider = true
        )
        SettingsItemRow(
            icon = Icons.Default.Schedule,
            title = "예약 알림",
            subtitle = "예약된 시간 10분 전 알림",
            onClick = { onItemClick("reservation_notification") },
            showDivider = false
        )
    }
    
    SettingsSection(title = "계정") {
        SettingsItemRow(
            icon = Icons.Default.Person,
            title = "프로필",
            subtitle = "내 정보 관리",
            onClick = { onItemClick("profile") },
            showDivider = true
        )
        SettingsItemRow(
            icon = Icons.Default.Logout,
            title = "로그아웃",
            subtitle = null,
            onClick = { onItemClick("logout") },
            showDivider = false
        )
    }
    
    SettingsSection(title = "기타") {
        SettingsItemRow(
            icon = Icons.Default.Info,
            title = "앱 정보",
            subtitle = "버전 $appVersion",
            onClick = { onItemClick("app_info") },
            showDivider = true
        )
        SettingsItemRow(
            icon = Icons.Default.Help,
            title = "도움말",
            subtitle = null,
            onClick = { onItemClick("help") },
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


