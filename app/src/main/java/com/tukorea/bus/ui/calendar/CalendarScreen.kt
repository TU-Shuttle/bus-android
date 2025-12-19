package com.tukorea.bus.ui.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tukorea.bus.R
import com.tukorea.bus.ui.common.ConfirmDeleteDialog
import com.tukorea.bus.ui.theme.*

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

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(16.dp),
        color = White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 왼쪽 색상 바
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Min)
                    .background(PrimaryBlue)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // 날짜와 시간
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PrimaryBlue
                        )
                        Text(
                            text = reservation.days.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Blue50
                    ) {
                        Text(
                            text = reservation.time,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 출발지 -> 도착지
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = reservation.from,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Gray400
                    )
                    Text(
                        text = reservation.to,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 수정/삭제 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PrimaryBlue
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "수정",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Red500
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "삭제",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
fun CalendarScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val days = remember(uiState.days) { uiState.days }
    val times = remember(uiState.times) { uiState.times }
    val locations = remember(uiState.locations) { uiState.locations }
    val selectedDays = remember(uiState.selectedDays) { uiState.selectedDays }
    val selectedTimes = remember(uiState.selectedTimes) { uiState.selectedTimes }
    val selectedFrom = remember(uiState.selectedFrom) { uiState.selectedFrom }
    val selectedTo = remember(uiState.selectedTo) { uiState.selectedTo }
    val reservations = remember(uiState.reservations) { uiState.reservations }
    val editingReservationId =
        remember(uiState.editingReservationId) { uiState.editingReservationId }
    val isEditing = remember(editingReservationId) { editingReservationId != null }

    val isReservationEnabled = remember(selectedTimes, selectedDays) {
        selectedTimes.isNotEmpty() && selectedDays.isNotEmpty()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Bottom)
                    .padding(bottom = 0.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { data ->
                    Surface(
                        modifier = Modifier.wrapContentWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Gray900,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = data.visuals.message,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Gray50
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 80.dp)
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.calendar_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(id = R.string.calendar_subtitle),
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
                            text = stringResource(id = R.string.calendar_select_days_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )

                        // 7일을 균등하게 배치 (한 번에 모두 보이도록)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            days.forEach { day ->
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

                                val dayA11yText = if (isSelected) {
                                    stringResource(id = R.string.calendar_day_selected_a11y, day)
                                } else {
                                    stringResource(id = R.string.calendar_day_select_a11y, day)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .graphicsLayer(scaleX = scale, scaleY = scale)
                                        .background(backgroundColor, CircleShape)
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null,
                                            onClick = { viewModel.toggleDay(day) }
                                        )
                                        .semantics {
                                            contentDescription = dayA11yText
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected)
                                            Color.White
                                        else
                                            PrimaryBlue
                                    )
                                }
                            }
                        }

                        // 출발지
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.calendar_from_label),
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
                                    onDismissRequest = { expandedFrom = false },
                                    modifier = Modifier.background(Color.White)
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

                        // 도착지
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.calendar_to_label),
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
                                    onDismissRequest = { expandedTo = false },
                                    modifier = Modifier.background(Color.White)
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

                        Text(
                            text = stringResource(id = R.string.calendar_select_time_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )

                        Surface(
                            modifier = Modifier
                                .height(256.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Blue50
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = times,
                                    key = { time -> time }
                                ) { time ->
                                    val isSelected = selectedTimes.contains(time)
                                    val timeInt = time.split(":")[0].toIntOrNull() ?: 0
                                    val period = if (timeInt < 12)
                                        stringResource(id = R.string.quickride_morning)
                                    else
                                        stringResource(id = R.string.quickride_afternoon)

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

                                    val timeA11yText = if (isSelected) {
                                        stringResource(
                                            id = R.string.calendar_time_selected_a11y,
                                            period,
                                            time
                                        )
                                    } else {
                                        stringResource(
                                            id = R.string.calendar_time_select_a11y,
                                            period,
                                            time
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer(scaleX = scale, scaleY = scale)
                                            .semantics {
                                                contentDescription = timeA11yText
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = backgroundColor,
                                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            Gray300
                                        ) else null,
                                        onClick = { viewModel.selectTime(time) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else PrimaryBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "$period $time",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Gray900
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = isReservationEnabled,
                            enter = fadeIn(animationSpec = tween(300)) +
                                    expandVertically(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(200)) +
                                    shrinkVertically(animationSpec = tween(200))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))

                                // 수정 모드일 때는 수정/취소 버튼, 아닐 때는 추가 버튼
                                if (isEditing) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.cancelEdit() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Gray600
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.common_cancel),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.updateReservation() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(56.dp),
                                            enabled = isReservationEnabled,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isReservationEnabled) PrimaryBlue else Gray200,
                                                contentColor = if (isReservationEnabled) Color.White else Gray400
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.calendar_edit_complete_button),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
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
                                            text = stringResource(id = R.string.calendar_add_button),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                            text = stringResource(id = R.string.calendar_reserved_title, reservations.size),
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
                                    text = stringResource(id = R.string.calendar_reserved_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray400
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
                                            viewModel.startEditReservation(reservation)
                                        },
                                        onDelete = { viewModel.showDeleteDialog(reservation.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

                // 삭제 확인 다이얼로그
            ConfirmDeleteDialog(
                showDialog = uiState.deletingReservationId != null,
                onDismiss = { viewModel.hideDeleteDialog() },
                onConfirm = {
                    uiState.deletingReservationId?.let { reservationId ->
                        viewModel.deleteReservation(reservationId)
                    }
                }
            )
        }
    }
}
