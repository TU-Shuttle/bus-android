package com.tukorea.bus.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.tukorea.bus.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.domain.model.Notification
import com.tukorea.bus.domain.model.NotificationType
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
    notificationId: Int,
    onBackPress: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notification = uiState.notifications.find { it.id == notificationId }

    // 알림을 읽음 처리
    LaunchedEffect(notification) {
        if (notification != null && !notification.read) {
            viewModel.markAsRead(notificationId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.notification_detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.notification_detail_back_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = Gray900
                )
            )
        }
    ) { paddingValues ->
        if (notification != null) {
            NotificationDetailContent(
                notification = notification,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // 알림을 찾을 수 없는 경우
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Gray400
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.notification_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailContent(
    notification: Notification,
    modifier: Modifier = Modifier
) {
    val config = remember(notification.type) {
        val typeTextResId = when (notification.type) {
            NotificationType.WARNING -> R.string.notification_type_warning
            NotificationType.BUS -> R.string.notification_type_bus
            NotificationType.INFO -> R.string.notification_type_info
            NotificationType.SUCCESS -> R.string.notification_type_success
            NotificationType.SYSTEM -> R.string.notification_type_system
        }

        NotificationDetailConfig(
            icon = when (notification.type) {
                NotificationType.WARNING -> Icons.Default.Notifications
                NotificationType.BUS -> Icons.Default.DirectionsTransit
                NotificationType.INFO -> Icons.Default.Info
                NotificationType.SUCCESS -> Icons.Default.DateRange
                NotificationType.SYSTEM -> Icons.Default.Settings
            },
            bgColor = when (notification.type) {
                NotificationType.WARNING -> Orange100
                NotificationType.BUS -> Blue100
                NotificationType.INFO -> Gray100
                NotificationType.SUCCESS -> Green100
                NotificationType.SYSTEM -> Gray100
            },
            iconColor = when (notification.type) {
                NotificationType.WARNING -> Orange600
                NotificationType.BUS -> PrimaryBlue
                NotificationType.INFO -> Gray600
                NotificationType.SUCCESS -> Green600
                NotificationType.SYSTEM -> Gray600
            },
            borderColor = when (notification.type) {
                NotificationType.WARNING -> Orange600
                NotificationType.BUS -> PrimaryBlue
                NotificationType.INFO -> Gray300
                NotificationType.SUCCESS -> Green600
                NotificationType.SYSTEM -> Gray300
            },
            typeTextResId = typeTextResId
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // 알림 타입 배지
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = config.bgColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = config.iconColor
                )
                Text(
                    text = stringResource(id = config.typeTextResId),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = config.iconColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 제목
        Text(
            text = notification.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 시간
        Text(
            text = notification.time,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 구분선
        Divider(color = Gray200, thickness = 1.dp)

        Spacer(modifier = Modifier.height(24.dp))

        // 내용
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = White
        ) {
            Text(
                text = notification.content,
                style = MaterialTheme.typography.bodyLarge,
                color = Gray700,
                lineHeight = 26.sp,
                modifier = Modifier.padding(20.dp)
            )
        }

        // 중요 알림인 경우 추가 안내
        if (notification.important) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Orange50,
                border = BorderStroke(1.dp, Orange100)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = Orange600,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                    text = stringResource(id = R.string.notification_important_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Orange700,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class NotificationDetailConfig(
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color,
    val borderColor: Color,
    @StringRes val typeTextResId: Int
)

@Preview(showBackground = true)
@Composable
private fun NotificationDetailScreenPreview() {
    BusTheme {
        NotificationDetailContent(
            notification = Notification(
                id = 1,
                type = NotificationType.WARNING,
                title = "운행 변경 안내",
                content = "11월 25일(월) A노선이 일시적으로 운행 중단됩니다. B노선을 이용해 주세요.\n\n운행 중단 시간: 오전 9시 ~ 오후 2시\n사유: 정기 점검\n\n이용에 불편을 드려 죄송합니다.",
                time = "10분 전",
                read = false,
                important = true
            )
        )
    }
}
