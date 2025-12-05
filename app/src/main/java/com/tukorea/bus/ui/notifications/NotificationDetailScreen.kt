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
                        text = "알림 상세",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
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
                        text = "알림을 찾을 수 없습니다",
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
        when (notification.type) {
            NotificationType.WARNING -> NotificationDetailConfig(
                icon = Icons.Default.Notifications,
                bgColor = Orange100,
                iconColor = Orange600,
                borderColor = Orange600,
                typeText = "중요 공지"
            )
            NotificationType.BUS -> NotificationDetailConfig(
                icon = Icons.Default.DirectionsTransit,
                bgColor = Blue100,
                iconColor = PrimaryBlue,
                borderColor = PrimaryBlue,
                typeText = "버스 운행"
            )
            NotificationType.INFO -> NotificationDetailConfig(
                icon = Icons.Default.Info,
                bgColor = Gray100,
                iconColor = Gray600,
                borderColor = Gray300,
                typeText = "일반 안내"
            )
            NotificationType.SUCCESS -> NotificationDetailConfig(
                icon = Icons.Default.DateRange,
                bgColor = Green100,
                iconColor = Green600,
                borderColor = Green600,
                typeText = "예약 안내"
            )
            NotificationType.SYSTEM -> NotificationDetailConfig(
                icon = Icons.Default.Settings,
                bgColor = Gray100,
                iconColor = Gray600,
                borderColor = Gray300,
                typeText = "시스템"
            )
        }
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
                    text = config.typeText,
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
                        text = "이 알림은 중요 공지사항입니다. 반드시 확인해주세요.",
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
    val typeText: String
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
