package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Booking
import com.saurabh.onecornersystem.data.model.BookingStatus
import com.saurabh.onecornersystem.data.model.ServiceLocation
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ShopBookingDetailsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopBookingDetailsScreen(
    bookingId: String,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val bookingDetailsState by viewModel.shopBookingDetailsState.collectAsStateWithLifecycle()
    val updateStatusState by viewModel.updateBookingStatusState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showActionDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf<BookingStatus?>(null) }
    var actionReason by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch booking details
    LaunchedEffect(bookingId) {
        Log.d(TAG, "📞 Fetching booking details for ID: $bookingId")
        viewModel.getShopBookingDetails(bookingId)
    }

    // Handle status update state
    LaunchedEffect(updateStatusState) {
        when (val state = updateStatusState) {
            is Resource.Success -> {
                isLoading = false
                Log.d(TAG, "✅ Booking status updated successfully")
                snackbarHostState.showSnackbar(
                    message = "Booking status updated successfully",
                    duration = SnackbarDuration.Short
                )
                // Refresh booking details
                viewModel.getShopBookingDetails(bookingId)
                // Close dialog
                showActionDialog = false
                actionReason = ""
                actionType = null
            }
            is Resource.Error -> {
                isLoading = false
                val errorMsg = state.message ?: "Failed to update booking"
                Log.e(TAG, "❌ Error updating booking: $errorMsg")
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Long
                )
            }
            is Resource.Loading -> {
                isLoading = true
                Log.d(TAG, "⏳ Updating booking status...")
            }
            else -> {}
        }
    }

    // Action Dialog (Reject/Cancel)
    if (showActionDialog && actionType != null) {
        AlertDialog(
            onDismissRequest = {
                showActionDialog = false
                actionReason = ""
                actionType = null
            },
            title = {
                Text(
                    when (actionType) {
                        BookingStatus.REJECTED -> "Reject Booking"
                        BookingStatus.CANCELLED -> "Cancel Booking"
                        else -> "Confirm Action"
                    }
                )
            },
            text = {
                Column {
                    Text(
                        when (actionType) {
                            BookingStatus.REJECTED -> "Are you sure you want to reject this booking?"
                            BookingStatus.CANCELLED -> "Are you sure you want to cancel this booking?"
                            else -> "Are you sure you want to perform this action?"
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionReason,
                        onValueChange = { actionReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        actionType?.let { status ->
                            when (val state = bookingDetailsState) {
                                is Resource.Success -> {
                                    val booking = state.data
                                    viewModel.updateBookingStatus(
                                        bookingId = booking.bookingId,
                                        status = status,
                                        shopId = booking.shopId,
                                        reason = actionReason
                                    )
                                }
                                else -> {}
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = when (actionType) {
                            BookingStatus.REJECTED, BookingStatus.CANCELLED -> Color(0xFFE53935)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showActionDialog = false
                    actionReason = ""
                    actionType = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            when (val state = bookingDetailsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading booking details...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                        ShopBookingStatusBanner(status = booking.status)

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Booking ID
                            Text(
                                text = "Booking #${booking.bookingId.takeLast(8)}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Customer Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Customer Information",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    InfoRow(
                                        icon = Icons.Default.Person,
                                        label = "Name",
                                        value = booking.customerName
                                    )
                                    InfoRow(
                                        icon = Icons.Default.Phone,
                                        label = "Phone",
                                        value = booking.customerPhone
                                    )
                                    InfoRow(
                                        icon = Icons.Default.Email,
                                        label = "Email",
                                        value = booking.customerEmail
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Service Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Service Details",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    InfoRow(
                                        icon = Icons.Default.Build,
                                        label = "Service",
                                        value = booking.serviceName
                                    )
                                    InfoRow(
                                        icon = Icons.Default.Schedule,
                                        label = "Duration",
                                        value = booking.serviceDuration
                                    )
                                    InfoRow(
                                        icon = Icons.Default.AttachMoney,
                                        label = "Price",
                                        value = "₹${booking.servicePrice}"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Booking Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Booking Information",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    InfoRow(
                                        icon = Icons.Default.CalendarToday,
                                        label = "Date",
                                        value = formatFullDate(booking.bookingDate)
                                    )
                                    InfoRow(
                                        icon = Icons.Default.Schedule,
                                        label = "Time",
                                        value = formatTime(booking.bookingTime)
                                    )
                                    InfoRow(
                                        icon = if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME)
                                            Icons.Default.Home else Icons.Default.Store,
                                        label = "Location Type",
                                        value = if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME)
                                            "Home Service" else "At Shop"
                                    )

                                    if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) {
                                        InfoRow(
                                            icon = Icons.Default.LocationOn,
                                            label = "Address",
                                            value = booking.serviceAddress
                                        )
                                        InfoRow(
                                            icon = Icons.Default.LocationCity,
                                            label = "City",
                                            value = "${booking.customerCity} - ${booking.customerPincode}"
                                        )
                                    }

                                    if (booking.notes.isNotBlank()) {
                                        InfoRow(
                                            icon = Icons.Default.Notes,
                                            label = "Notes",
                                            value = booking.notes
                                        )
                                    }

                                    if (booking.cancellationReason.isNotBlank()) {
                                        InfoRow(
                                            icon = Icons.Default.Warning,
                                            label = if (booking.status == BookingStatus.REJECTED)
                                                "Rejection Reason" else "Cancellation Reason",
                                            value = booking.cancellationReason,
                                            valueColor = Color(0xFFE53935)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Buttons based on status
                            when (booking.status) {
                                BookingStatus.PENDING -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.updateBookingStatus(
                                                    bookingId = booking.bookingId,
                                                    status = BookingStatus.CONFIRMED,
                                                    shopId = booking.shopId
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF4CAF50)
                                            )
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White
                                                )
                                            } else {
                                                Text("Accept")
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                actionType = BookingStatus.REJECTED
                                                showActionDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE53935)
                                            )
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = Color.White
                                                )
                                            } else {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }

                                BookingStatus.CONFIRMED -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.updateBookingStatus(
                                                    bookingId = booking.bookingId,
                                                    status = BookingStatus.COMPLETED,
                                                    shopId = booking.shopId
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            enabled = !isLoading
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Text("Mark Completed")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { navController.popBackStack() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Back")
                                        }
                                    }
                                }

                                BookingStatus.COMPLETED -> {
                                    Button(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text("Back to Bookings")
                                    }
                                }

                                else -> {
                                    Button(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text("Back to Bookings")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Failed to load booking",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = state.message ?: "Something went wrong",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { navController.popBackStack() }
                                ) {
                                    Text("Go Back")
                                }

                                Button(
                                    onClick = {
                                        viewModel.getShopBookingDetails(bookingId)
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                else -> {}
            }

            // Loading Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Processing...",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopBookingStatusBanner(status: BookingStatus) {
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
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        }
    }
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