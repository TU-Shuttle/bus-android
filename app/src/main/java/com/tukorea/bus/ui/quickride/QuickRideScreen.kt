package com.tukorea.bus.ui.quickride

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.tukorea.bus.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.tukorea.bus.domain.model.RouteScheduleTemplate
import com.tukorea.bus.ui.navigation.Screen
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickRideScreen(
    onNavigateTo: (String) -> Unit, viewModel: QuickRideViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val destinations = remember(uiState.destinations) { uiState.destinations }

    val availableBuses = remember(uiState.availableBuses) { uiState.availableBuses }
    val currentLocation = remember(uiState.currentLocation) { uiState.currentLocation }
    val selectedDestination = remember(uiState.selectedDestination) { uiState.selectedDestination }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

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
            Column(
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.quickride_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.quickride_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.quickride_current_location_label),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray700
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Blue50,
                            border = BorderStroke(1.5.dp, Blue100)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryBlue, RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        tint = White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = uiState.currentLocation,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray900
                                )
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.quickride_destination_label),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray700
                        )
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = uiState.selectedDestination,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = Gray200,
                                    focusedTextColor = Gray900,
                                    unfocusedTextColor = Gray900,
                                    focusedLabelColor = Gray700,
                                    unfocusedLabelColor = Gray700,
                                    focusedPlaceholderColor = Gray500,
                                    unfocusedPlaceholderColor = Gray500,
                                    focusedSupportingTextColor = Gray500,
                                    unfocusedSupportingTextColor = Gray500,
                                    focusedTrailingIconColor = PrimaryBlue,
                                    unfocusedTrailingIconColor = Gray500
                                ),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                })
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                destinations.forEach { dest ->
                                    DropdownMenuItem(
                                        text = {
                                        Text(
                                            text = dest, color = Gray900
                                        )
                                    }, onClick = {
                                        viewModel.updateDestination(dest)
                                        expanded = false
                                    }, colors = MenuDefaults.itemColors(
                                        textColor = Gray900
                                    )
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.toggleTimeTable() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        ),
                        border = BorderStroke(2.dp, PrimaryBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.quickride_view_full_timetable),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.quickride_available_buses_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Surface(
                    shape = RoundedCornerShape(20.dp), color = Blue50
                ) {
                    Text(
                        text = stringResource(id = R.string.quickride_available_buses_count, availableBuses.size),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    repeat(3) {
                        BusCardSkeleton()
                    }
                }
            } else if (availableBuses.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsTransit,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Gray300
                        )
                        Text(
                            text = stringResource(id = R.string.quickride_no_running_bus_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray400
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    availableBuses.forEach { bus ->
                        BusCard(
                            bus = bus,
                            currentLocation = currentLocation,
                            destination = selectedDestination,
                            onClick = { onNavigateTo(Screen.Ride.route) })
                    }
                }
            }
        }
    }

    if (uiState.showTimeTable) {
        TimeTableModal(
            routes = uiState.timetableRoutes,
            onDismiss = { viewModel.toggleTimeTable() },
            screenHeight = screenHeight,
            density = density
        )
    }
}

@Composable
fun BusCard(
    bus: com.tukorea.bus.domain.model.Bus,
    currentLocation: String,
    destination: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "bus_card_scale"
    )

    val routeUi = remember(bus.route) { BusRouteUi.fromRaw(bus.route) }
    val seatUi = remember(bus.seats) { SeatStatusUi.fromRaw(bus.seats) }

    val routeColor = remember(routeUi) {
        when (routeUi) {
            BusRouteUi.A -> Pair(Blue100, Blue700)
            BusRouteUi.B -> Pair(Green100, Green700)
            BusRouteUi.C -> Pair(Orange100, Orange700)
            BusRouteUi.LOOP -> Pair(Orange100, Orange700)
            BusRouteUi.UNKNOWN -> Pair(Blue100, Blue700)
        }
    }

    val seatsColor = remember(seatUi) {
        when (seatUi) {
            SeatStatusUi.PLENTY -> Pair(Green100, Green700)
            SeatStatusUi.NORMAL -> Pair(Yellow100, Orange700)
            SeatStatusUi.CROWDED -> Pair(Red100, Red700)
            SeatStatusUi.UNKNOWN -> Pair(Gray200, Gray500)
        }
    }

    val elevation = remember(isPressed) {
        if (isPressed) 4.dp else 1.dp
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(
                interactionSource = interactionSource, onClick = onClick
            )
            .semantics {
                contentDescription = "${bus.route} 버스, ${bus.time} 도착 예정, ${bus.seats} 좌석 상태"
            }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(
            containerColor = Color.White
        ), elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp), color = routeColor.first
                        ) {
                            Text(
                                text = bus.route,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = routeColor.second
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp), color = seatsColor.first
                        ) {
                            Text(
                                text = bus.seats,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = seatsColor.second
                            )
                        }
                    }
                    Text(
                        text = bus.time,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${bus.stops}개 정류장 · 약 ${bus.stops * 5}분 소요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "도착 예정: ${bus.arrivalTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp), color = Gray100
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsTransit,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(28.dp),
                        tint = Gray700
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp), color = Gray200
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentLocation,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray500
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Gray500
                )
                Text(
                    text = "...", style = MaterialTheme.typography.labelSmall, color = Gray500
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Gray500
                )
                Text(
                    text = destination,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray500
                )
            }
        }
    }
}

@Composable
fun TimeTableModal(
    routes: List<RouteScheduleTemplate>,
    onDismiss: () -> Unit,
    screenHeight: Dp,
    density: Density
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 오버레이
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() }
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 0.75f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                TimeTableContent(
                    routes = routes,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun TimeTableContent(
    routes: List<RouteScheduleTemplate>,
    onDismiss: () -> Unit
) {
    data class RouteScheduleUi(
        val name: String,
        val color: Color,
        val bgColor: Color,
        val icon: ImageVector,
        val morningTimes: List<String>,
        val afternoonTimes: List<String>
    )

    // 도메인 모델(RouteScheduleTemplate)을 UI 전용 모델로 변환
    val uiRoutes = remember(routes) {
        routes.map { template ->
            val (color, bgColor, icon) = when (template.name) {
                "A노선" -> Triple(PrimaryBlue, Blue50, Icons.Default.DirectionsBus)
                "B노선" -> Triple(Green600, Green50, Icons.Default.DirectionsBus)
                "C노선" -> Triple(Orange600, Orange50, Icons.Default.DirectionsBus)
                "순환" -> Triple(Gray700, Gray100, Icons.Default.Loop)
                else -> Triple(PrimaryBlue, Blue50, Icons.Default.DirectionsBus)
            }
            RouteScheduleUi(
                name = template.name,
                color = color,
                bgColor = bgColor,
                icon = icon,
                morningTimes = template.morningTimes,
                afternoonTimes = template.afternoonTimes
                )
        }
    }

    var selectedRouteIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        uiRoutes.getOrNull(selectedRouteIndex)?.color ?: PrimaryBlue,
                        uiRoutes.getOrNull(selectedRouteIndex)?.color?.copy(alpha = 0.85f)
                            ?: PrimaryBlue.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(id = R.string.quickride_timetable_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = stringResource(id = R.string.quickride_timetable_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = White.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.background(White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.quickride_timetable_close_cd),
                    tint = White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiRoutes.size) { index ->
                val route = uiRoutes[index]
                val isSelected = selectedRouteIndex == index

                Surface(
                    onClick = { selectedRouteIndex = index },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) White else White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 16.dp, vertical = 10.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = route.icon,
                            contentDescription = null,
                            tint = if (isSelected) route.color else White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = route.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) route.color else White
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    val selectedRoute = uiRoutes.getOrNull(selectedRouteIndex)
    selectedRoute?.let { route ->
        TimeTableSection(
            title = stringResource(id = R.string.quickride_morning),
            subtitle = stringResource(id = R.string.quickride_morning_abbrev),
            times = route.morningTimes,
            accentColor = route.color,
            bgColor = route.bgColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        TimeTableSection(
            title = stringResource(id = R.string.quickride_afternoon),
            subtitle = stringResource(id = R.string.quickride_afternoon_abbrev),
            times = route.afternoonTimes,
            accentColor = route.color,
            bgColor = route.bgColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Gray50,
            border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.quickride_operation_info_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray700
                    )
                }
                Text(
                    text = stringResource(id = R.string.quickride_operation_info_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    lineHeight = 22.sp
                )
            }
        }
    }
}


@Composable
fun TimeTableSection(
    title: String, subtitle: String, times: List<String>, accentColor: Color, bgColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp), color = accentColor
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Text(
                text = stringResource(id = R.string.quickride_timetable_runs, times.size),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = bgColor.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                times.chunked(4).forEach { rowTimes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTimes.forEach { time ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = White,
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Gray900
                                    )
                                }
                            }
                        }
                        repeat(4 - rowTimes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Bus Card")
@Composable
private fun BusCardPreview() {
    BusTheme {
        val mockBus = com.tukorea.bus.domain.model.Bus(
            id = "1",
            route = "A노선",
            time = "09:00",
            seats = "여유",
            color = "blue",
            stops = 5,
            arrivalTime = "09:25",
            from = "기숙사",
            to = "본관"
        )
        BusCard(
            bus = mockBus, currentLocation = "기숙사", destination = "본관", onClick = {})
    }
}

@Preview(showBackground = true, name = "Time Table Content")
@Composable
private fun TimeTableContentPreview() {
    BusTheme {
        TimeTableContent(
            routes = listOf(
                RouteScheduleTemplate(
                    name = "A노선",
                    morningTimes = listOf("09:00", "09:30"),
                    afternoonTimes = listOf("13:00", "13:30")
                )
            ),
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Bus Card - B노선")
@Composable
private fun BusCardBLinePreview() {
    BusTheme {
        val mockBus = com.tukorea.bus.domain.model.Bus(
            id = "2",
            route = "B노선",
            time = "10:30",
            seats = "보통",
            color = "green",
            stops = 8,
            arrivalTime = "11:00",
            from = "역",
            to = "본관"
        )
        BusCard(
            bus = mockBus, currentLocation = "역", destination = "본관", onClick = {})
    }
}

@Composable
fun BusCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f, animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse
        ), label = "skeleton_alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 배지들
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(24.dp)
                                .background(
                                    Color.Gray.copy(alpha = alpha), RoundedCornerShape(999.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(50.dp)
                                .height(24.dp)
                                .background(
                                    Color.Gray.copy(alpha = alpha), RoundedCornerShape(999.dp)
                                )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(48.dp)
                            .background(
                                Color.Gray.copy(alpha = alpha), RoundedCornerShape(8.dp)
                            )
                    )


                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(16.dp)
                            .background(
                                Color.Gray.copy(alpha = alpha), RoundedCornerShape(4.dp)
                            )
                    )
                }


                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.Gray.copy(alpha = alpha * 0.5f), RoundedCornerShape(12.dp)
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Bus Card Skeleton")
@Composable
private fun BusCardSkeletonPreview() {
    BusTheme {
        BusCardSkeleton()
    }
}

@Preview(showBackground = true, name = "Bus Card - 혼잡")
@Composable
private fun BusCardCrowdedPreview() {
    BusTheme {
        val mockBus = com.tukorea.bus.domain.model.Bus(
            id = "3",
            route = "순환",
            time = "14:00",
            seats = "혼잡",
            color = "orange",
            stops = 12,
            arrivalTime = "14:40",
            from = "기숙사",
            to = "도서관"
        )
        BusCard(
            bus = mockBus, currentLocation = "기숙사", destination = "도서관", onClick = {})
    }
}

