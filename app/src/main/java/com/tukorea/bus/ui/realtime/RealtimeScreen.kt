package com.tukorea.bus.ui.realtime

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect.Companion.dashPathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: RealtimeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingMinutes = uiState.remainingMinutes
    val progress = uiState.progress
    val remainingStops = uiState.remainingStops

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progress"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "실시간 위치",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Blue50
                        ) {
                            Text(
                                text = uiState.route,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateTo(Screen.Home.route) }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "뒤로",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Gray50)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(White)
            ) {
                RouteVisualization(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-40).dp * (animatedProgress - 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp * pulseScale)
                            .alpha(pulseAlpha)
                            .background(
                                PrimaryBlue.copy(alpha = 0.2f),
                                CircleShape
                            )
                            .align(Alignment.Center)
                    )
                    Box(
                        modifier = Modifier
                            .size(70.dp * (1f + (pulseScale - 1f) * 0.5f))
                            .alpha(pulseAlpha * 1.5f)
                            .background(
                                PrimaryBlue.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .align(Alignment.Center)
                    )
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center),
                        shape = CircleShape,
                        color = PrimaryBlue,
                        shadowElevation = 8.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsTransit,
                            contentDescription = "버스",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(32.dp),
                            tint = White
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (remainingMinutes > 0) "${remainingMinutes}분" else "곧",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingMinutes <= 1) WarningOrange else PrimaryBlue
                        )
                        Text(
                            text = if (remainingMinutes > 0) " 후" else " 도착",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "도착 예정",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray500
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.from,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = uiState.to,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray400
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Gray100, RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(10.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(PrimaryBlue, Blue400)
                                        ),
                                        RoundedCornerShape(5.dp)
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(16.dp),
                            color = Blue50
                        ) {
                            Text(
                                text = if (remainingStops > 0) "${remainingStops}개 정류장 남음" else "다음 정류장 도착",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "경유 정류장",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    uiState.stations.forEachIndexed { index, station ->
                        StationItem(
                            name = station.name,
                            passed = index < uiState.remainingStops,
                            current = index == uiState.remainingStops
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: 도착 알림 설정 기능 구현 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    ),
                    border = BorderStroke(2.dp, PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("알림 받기")
                }

                Button(
                    onClick = { /* TODO: 위치 공유 기능 구현 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("공유하기")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StationItem(
    name: String,
    passed: Boolean,
    current: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (current) 16.dp else 12.dp)
                .background(
                    when {
                        current -> PrimaryBlue
                        passed -> Green600
                        else -> Gray300
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (passed && !current) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                    tint = White
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
            color = when {
                current -> PrimaryBlue
                passed -> Green600
                else -> Gray500
            }
        )
        
        if (current) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = PrimaryBlue
            ) {
                Text(
                    text = "현재 위치",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = White
                )
            }
        }
    }
}

@Composable
fun RouteVisualization(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.padding(40.dp)) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(width * 0.2f, height * 0.8f)
            cubicTo(
                width * 0.3f, height * 0.4f,
                width * 0.7f, height * 0.6f,
                width * 0.8f, height * 0.2f
            )
        }

        drawPath(
            path = path,
            color = Gray200,
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round
            )
        )

        drawPath(
            path = path,
            color = PrimaryBlue,
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                pathEffect = dashPathEffect(
                    intervals = floatArrayOf(
                        size.width * progress * 2,
                        size.width * (1 - progress) * 2
                    )
                )
            )
        )

        val stations = listOf(
            Offset(width * 0.2f, height * 0.8f),
            Offset(width * 0.4f, height * 0.5f),
            Offset(width * 0.6f, height * 0.4f),
            Offset(width * 0.8f, height * 0.2f)
        )
        
        stations.forEachIndexed { index, offset ->
            val stationProgress = index / (stations.size - 1f)
            val isPassed = progress >= stationProgress
            
            drawCircle(
                color = if (isPassed) PrimaryBlue else Gray200,
                radius = if (isPassed) 12f else 10f,
                center = offset
            )
            drawCircle(
                color = Color.White,
                radius = if (isPassed) 6f else 5f,
                center = offset
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RealtimeScreenPreview() {
    BusTheme {
        RealtimeScreen(onNavigateTo = {})
    }
}

