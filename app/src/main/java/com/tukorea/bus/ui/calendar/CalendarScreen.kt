package com.tukorea.bus.ui.calendar

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.tukorea.bus.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val days = remember(uiState.days) { uiState.days }
    val times = remember(uiState.times) { uiState.times }
    val locations = remember(uiState.locations) { uiState.locations }
    val selectedDays = remember(uiState.selectedDays) { uiState.selectedDays }
    val selectedTime = remember(uiState.selectedTime) { uiState.selectedTime }
    val selectedFrom = remember(uiState.selectedFrom) { uiState.selectedFrom }
    val selectedTo = remember(uiState.selectedTo) { uiState.selectedTo }
    val reservations = remember(uiState.reservations) { uiState.reservations }

    val isReservationEnabled = remember(selectedTime, selectedDays) {
        selectedTime.isNotEmpty() && selectedDays.isNotEmpty()
    }

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
                    text = "예약하기",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "반복 일정으로 편리하게",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray500
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    Text(
                        text = "날짜를 선택해주세요",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = days,
                            key = { day -> day }
                        ) { day ->
                            val isSelected = selectedDays.contains(day)

                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()

                            val backgroundColor: Color by animateColorAsState(
                                targetValue = if (isSelected) PrimaryBlue else Blue50,
                                animationSpec = tween(durationMillis = 200),
                                label = "day_button_background"
                            )

                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.9f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "day_button_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .graphicsLayer(scaleX = scale, scaleY = scale)
                                    .background(backgroundColor, CircleShape)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        onClick = { viewModel.toggleDay(day) }
                                    )
                                    .semantics {
                                        contentDescription = if (isSelected) "$day 선택됨" else "$day 선택"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected)
                                        Color.White
                                    else
                                        PrimaryBlue
                                )
                            }
                        }
                    }

                    Text(
                        text = "시간 선택",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )

                    Surface(
                        modifier = Modifier
                            .height(256.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Gray100
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = times,
                                key = { time -> time }
                            ) { time ->
                                val isSelected = selectedTime == time

                                val interactionSource = remember { MutableInteractionSource() }
                                val isPressed by interactionSource.collectIsPressedAsState()

                                val backgroundColor: Color by animateColorAsState(
                                    targetValue = if (isSelected) PrimaryBlue else Color.White,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "time_button_background"
                                )

                                val scale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.97f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "time_button_scale"
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer(scaleX = scale, scaleY = scale)
                                        .semantics {
                                            contentDescription = if (isSelected) "$time 선택됨" else "$time 선택"
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = backgroundColor,
                                    onClick = { viewModel.selectTime(time) }
                                ) {
                                    Text(
                                        text = time,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected)
                                            Color.White
                                        else
                                            Gray900
                                    )
                                }
                            }
                        }
                    }

                    if (isReservationEnabled) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 출발지
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "출발지",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray700
                                )
                                var expandedFrom by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedFrom,
                                    onExpandedChange = { expandedFrom = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedFrom,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom)
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedFrom,
                                        onDismissRequest = { expandedFrom = false }
                                    ) {
                                        locations.forEach { location ->
                                            DropdownMenuItem(
                                                text = { Text(location) },
                                                onClick = {
                                                    viewModel.selectFrom(location)
                                                    expandedFrom = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "도착지",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray700
                                )
                                var expandedTo by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedTo,
                                    onExpandedChange = { expandedTo = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedTo,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo)
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedTo,
                                        onDismissRequest = { expandedTo = false }
                                    ) {
                                        locations.filter { it != selectedFrom }
                                            .forEach { location ->
                                                DropdownMenuItem(
                                                    text = { Text(location) },
                                                    onClick = {
                                                        viewModel.selectTo(location)
                                                        expandedTo = false
                                                    }
                                                )
                                            }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.addReservation() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = isReservationEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isReservationEnabled) PrimaryBlue else Gray200,
                                    contentColor = if (isReservationEnabled) Color.White else Gray400
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "예약 추가",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "예정된 예약 (${reservations.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (reservations.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Gray400.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "예약된 일정이 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray400 // text-gray-400
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            reservations.forEach { reservation ->
                                ReservationItem(
                                    reservation = reservation,
                                    onEdit = { 
                                        // 수정은 ViewModel에 추가... 하든가
                                    },
                                    onDelete = { viewModel.deleteReservation(reservation.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: 에러 메세지 처리..
//    uiState.errorMessage?.let { error ->
//        LaunchedEffect(error) {
//
//        }
//    }
}

@Composable
fun ReservationItem(
    reservation: com.tukorea.bus.domain.model.Reservation,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reservation_item_scale"
    )

    val reservationText = remember(reservation.days, reservation.time) {
        "${reservation.days.joinToString(", ")} ${reservation.time}"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(16.dp),
        color = Gray100,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reservationText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reservation.from,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(horizontal = 4.dp),
                        tint = Gray600
                    )
                    Text(
                        text = reservation.to,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = "수정",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Red500
                    )
                ) {
                    Text(
                        text = "삭제",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Reservation Item")
@Composable
private fun ReservationItemPreview() {
    BusTheme {
        val mockReservation = com.tukorea.bus.domain.model.Reservation(
            id = 1,
            days = listOf("월", "수", "금"),
            time = "09:00",
            from = "기숙사",
            to = "본관"
        )
        ReservationItem(
            reservation = mockReservation,
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true, name = "Reservation Item - Single Day")
@Composable
private fun ReservationItemSingleDayPreview() {
    BusTheme {
        val mockReservation = com.tukorea.bus.domain.model.Reservation(
            id = 2,
            days = listOf("화"),
            time = "13:30",
            from = "본관",
            to = "역"
        )
        ReservationItem(
            reservation = mockReservation,
            onEdit = {},
            onDelete = {}
        )
    }
}

