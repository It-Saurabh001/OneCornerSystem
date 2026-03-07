package com.saurabh.onecornersystem.presentation.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Booking
import com.saurabh.onecornersystem.data.model.BookingStatus
import com.saurabh.onecornersystem.data.model.ServiceLocation
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bookingId: String,
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bookingDetailsState by viewModel.bookingDetailsState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        viewModel.getBookingDetails(bookingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Share booking
                    IconButton(onClick = { /* Share booking */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = bookingDetailsState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val booking = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Status Banner
                    StatusBanner(status = booking.status)

                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Shop and Service Info
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = booking.shopName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = booking.serviceName,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Price
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Price",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "₹${booking.servicePrice}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date & Time
                        InfoCard(
                            icon = Icons.Default.CalendarToday,
                            title = "Date & Time",
                            content = "${formatFullDate(booking.bookingDate)} at ${formatTime(booking.bookingTime)}"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Location
                        InfoCard(
                            icon = Icons.Default.LocationOn,
                            title = if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME)
                                "Home Service Address"
                            else
                                "Shop Location",
                            content = if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) {
                                "${booking.serviceAddress}\n${booking.customerCity} - ${booking.customerPincode}"
                            } else {
                                booking.shopAddress
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Notes (if any)
                        if (booking.notes.isNotBlank()) {
                            InfoCard(
                                icon = Icons.Default.Notes,
                                title = "Additional Notes",
                                content = booking.notes
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Cancellation Info (if cancelled)
                        if (booking.status == BookingStatus.CANCELLED && booking.cancellationReason.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE53935).copy(alpha = 0.1f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFE53935)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Cancellation Reason",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE53935).copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = booking.cancellationReason,
                                            fontSize = 14.sp,
                                            color = Color(0xFFE53935)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        when (booking.status) {
                            BookingStatus.PENDING -> {
                                Button(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE53935)
                                    )
                                ) {
                                    Text("Cancel Booking")
                                }
                            }
                            BookingStatus.CONFIRMED -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { /* Contact shop */ },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Contact")
                                    }
                                    Button(
                                        onClick = { /* Get directions */ },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Map, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Directions")
                                    }
                                }
                            }
                            BookingStatus.COMPLETED -> {
                                Button(
                                    onClick = { /* Rate service */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rate This Service")
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message ?: "Failed to load booking",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            viewModel.getBookingDetails(bookingId)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {}
        }
    }

    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        CancelBookingDialog(
            onConfirm = { reason ->
                currentUser?.userId?.let { userId ->
                    viewModel.cancelBooking(bookingId, reason, userId)
                    showCancelDialog = false
                    navController.popBackStack()
                }
            },
            onDismiss = { showCancelDialog = false }
        )
    }
}

@Composable
fun StatusBanner(status: BookingStatus) {
    val (backgroundColor, textColor, statusText) = when (status) {
        BookingStatus.PENDING -> Triple(
            Color(0xFFFFC107),
            Color.White,
            "PENDING"
        )
        BookingStatus.CONFIRMED -> Triple(
            Color(0xFF2196F3),
            Color.White,
            "CONFIRMED"
        )
        BookingStatus.IN_PROGRESS -> Triple(
            Color(0xFF9C27B0),
            Color.White,
            "IN PROGRESS"
        )
        BookingStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "COMPLETED"
        )
        BookingStatus.CANCELLED -> Triple(
            Color(0xFFE53935),
            Color.White,
            "CANCELLED"
        )
        BookingStatus.REJECTED -> Triple(
            Color(0xFFE53935),
            Color.White,
            "REJECTED"
        )
        BookingStatus.NO_SHOW -> Triple(
            Color(0xFF757575),
            Color.White,
            "NO SHOW"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = statusText,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = content,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CancelBookingDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Booking") },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to cancel this booking?",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for cancellation (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(reason) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Text("Confirm Cancel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Back")
            }
        }
    )
}

private fun formatFullDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.US)
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date)
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatTime(timeStr: String): String {
    return try {
        if (timeStr.contains(":")) {
            val parts = timeStr.split(":")
            var hour = parts[0].toInt()
            val minute = parts[1]
            val amPm = if (hour >= 12) "PM" else "AM"

            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12

            String.format("%d:%s %s", hour, minute, amPm)
        } else {
            timeStr
        }
    } catch (e: Exception) {
        timeStr
    }
}
