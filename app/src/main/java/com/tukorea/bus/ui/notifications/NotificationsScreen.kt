package com.tukorea.bus.ui.notifications

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.domain.model.Notification
import com.tukorea.bus.domain.model.NotificationType
import com.tukorea.bus.ui.theme.*

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications = uiState.notifications

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
            
            item {
                NotificationHeader(
                    notifications = notifications,
                    onMarkAllAsRead = { viewModel.markAllAsRead() }
                )
            }
            
            items(
                items = notifications,
                key = { notification -> notification.id }
            ) { notification ->
                val index = notifications.indexOf(notification)
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index * 50
                        )
                    ) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = tween(300, delayMillis = index * 50)
                    )
                ) {
                    NotificationCard(
                        notification = notification,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationHeader(
    notifications: List<Notification>,
    onMarkAllAsRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "알림",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "공지사항 및 운행 안내",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray500
            )
        }
        
        val hasUnreadNotifications = notifications.any { !it.read }
        if (hasUnreadNotifications) {
            MarkAllAsReadButton(onClick = onMarkAllAsRead)
        }
    }
}

@Composable
private fun MarkAllAsReadButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Blue50,
        modifier = Modifier.semantics {
            contentDescription = "모든 알림을 읽음 상태로 표시"
        }
    ) {
        Text(
            text = "모두 읽음",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    viewModel: NotificationsViewModel
) {
    val config = remember(notification.type) {
        when (notification.type) {
            NotificationType.WARNING -> NotificationConfig(
                icon = Icons.Default.Notifications,
                bgColor = Orange100,
                iconColor = Orange600,
                borderColor = Orange600
            )
            NotificationType.BUS -> NotificationConfig(
                icon = Icons.Default.DirectionsTransit,
                bgColor = Blue100,
                iconColor = PrimaryBlue,
                borderColor = PrimaryBlue
            )
            NotificationType.INFO -> NotificationConfig(
                icon = Icons.Default.Info,
                bgColor = Gray100,
                iconColor = Gray600,
                borderColor = Gray300
            )
            NotificationType.SUCCESS -> NotificationConfig(
                icon = Icons.Default.DateRange,
                bgColor = Green100,
                iconColor = Green600,
                borderColor = Green600
            )
            NotificationType.SYSTEM -> NotificationConfig(
                icon = Icons.Default.Settings,
                bgColor = Gray100,
                iconColor = Gray600,
                borderColor = Gray300
            )
        }
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    val elevation = remember(isPressed) {
        if (isPressed) 2.dp else 1.dp
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .then(
                if (notification.read) {
                    Modifier.alpha(0.6f)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (!notification.read) {
                        viewModel.markAsRead(notification.id)
                    }
                    // TODO: 알림 상세 화면 구현
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = elevation
    ) {
        NotificationCardContent(
            notification = notification,
            config = config
        )
    }
}


private data class NotificationConfig(
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color,
    val borderColor: Color
)

@Composable
private fun NotificationCardContent(
    notification: Notification,
    config: NotificationConfig
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(config.borderColor)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = config.bgColor
            ) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp),
                    tint = config.iconColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Text(
                        text = notification.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    lineHeight = 20.sp
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Notification Card - Warning")
@Composable
private fun NotificationCardWarningPreview() {
    BusTheme {
        val mockNotification = Notification(
            id = 1,
            type = NotificationType.WARNING,
            title = "운행 변경 안내",
            content = "11월 25일(월) A노선이 일시적으로 운행 중단됩니다. B노선을 이용해 주세요.",
            time = "10분 전",
            read = false,
            important = true
        )
        NotificationCard(
            notification = mockNotification,
            viewModel = hiltViewModel()
        )
    }
}
