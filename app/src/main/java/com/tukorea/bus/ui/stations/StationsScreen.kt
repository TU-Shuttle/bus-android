package com.tukorea.bus.ui.stations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.tukorea.bus.domain.model.Station
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationsScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: StationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val routes = uiState.routes
    val selectedRoute = uiState.selectedRoute
    val currentStations = uiState.stations
    var selectedStation by remember { mutableStateOf<Station?>(null) }
    var refreshing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "정류장 안내",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "노선별 정류장 확인",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray500
                    )
                }
                val scope = rememberCoroutineScope()
                IconButton(
                    onClick = {
                        refreshing = true
                        scope.launch {
                            delay(1000)
                            refreshing = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "새로고침",
                        tint = Gray700,
                        modifier = if (refreshing) {
                            Modifier
                        } else {
                            Modifier
                        }
                    )
                }
            }

            LazyRow(
                modifier = Modifier.padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = routes,
                    key = { route -> route }
                ) { route ->
                    Surface(
                        onClick = { viewModel.selectRoute(route) },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selectedRoute == route)
                            PrimaryBlue
                        else
                            Color.White,
                        shadowElevation = if (selectedRoute == route) 4.dp else 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = route,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedRoute == route)
                                Color.White
                            else
                                Gray700
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Blue50,
                                    Blue100
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "현재 노선",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedRoute,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = Blue900
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "운행 간격",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.interval.ifEmpty { "-" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Blue900
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    currentStations.forEachIndexed { index, station ->
                        StationItem(
                            station = station,
                            index = index,
                            totalCount = currentStations.size,
                            onStationClick = { selectedStation = station }
                        )
                        if (index < currentStations.size - 1) {
                            HorizontalDivider(
                                color = Gray200,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }

    selectedStation?.let { station ->
        StationDetailModal(
            station = station,
            route = selectedRoute,
            onDismiss = { selectedStation = null },
            onNavigateToQuickRide = {
                selectedStation = null
                onNavigateTo(Screen.QuickRide.route)
            }
        )
    }
}


@Composable
private fun StationItem(
    station: Station,
    index: Int,
    totalCount: Int,
    onStationClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "station_item_scale"
    )

    val isActive = remember(station.active) { station.active }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource,
                onClick = onStationClick
            )
            .semantics {
                contentDescription = "${station.name} 정류장, 다음 버스 ${station.time}"
            },
        color = if (isActive)
            Blue50
        else
            Color.Transparent,
        shape = if (index == totalCount - 1)
            RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else
            RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isActive)
                            PrimaryBlue
                        else
                            Gray200,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive)
                        Color.White
                    else
                        Gray600
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "다음 버스: ${station.time}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive)
                            PrimaryBlue
                        else
                            Gray500
                    )

                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = PrimaryBlue
                        ) {
                            Text(
                                text = "곧 도착",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (isActive)
                    PrimaryBlue
                else
                    Gray400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


@Composable
fun StationDetailModal(
    station: Station,
    route: String,
    onDismiss: () -> Unit,
    onNavigateToQuickRide: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { }
                }
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = station.name,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = route,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gray400
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Blue50
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "다음 버스 도착",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = station.time,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Gray100
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "이후 도착 예정",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            station.nextTimes.forEach { time ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                Gray400,
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = Gray700
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Green50
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "도착 알림 받기",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Green900
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "버스 도착 5분 전 알림",
                                style = MaterialTheme.typography.bodySmall,
                                color = Green600
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Green600,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToQuickRide,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "이 정류장에서 탑승하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Station Item")
@Composable
private fun StationItemPreview() {
    BusTheme {
        val mockStation = Station(
            name = "기숙사",
            time = "5분 후",
            active = true,
            nextTimes = listOf("20분 후", "35분 후")
        )
        StationItem(
            station = mockStation,
            index = 0,
            totalCount = 5,
            onStationClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Station Item - Inactive")
@Composable
private fun StationItemInactivePreview() {
    BusTheme {
        val mockStation = Station(
            name = "본관",
            time = "12분 후",
            active = false,
            nextTimes = listOf("27분 후", "42분 후")
        )
        StationItem(
            station = mockStation,
            index = 1,
            totalCount = 5,
            onStationClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Station Detail Modal")
@Composable
private fun StationDetailModalPreview() {
    BusTheme {
        val mockStation = Station(
            name = "기숙사",
            time = "5분 후",
            active = true,
            nextTimes = listOf("20분 후", "35분 후")
        )
        StationDetailModal(
            station = mockStation,
            route = "A노선",
            onDismiss = {},
            onNavigateToQuickRide = {}
        )
    }
}

@Preview(showBackground = true, name = "Stations Screen")
@Composable
private fun StationsScreenPreview() {
    BusTheme {
        StationsScreen(onNavigateTo = {})
    }
}

