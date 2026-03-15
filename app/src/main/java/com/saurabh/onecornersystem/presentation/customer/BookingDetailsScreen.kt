package com.saurabh.onecornersystem.presentation.customer

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
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "BookingDetailsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bookingId: String,
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bookingDetailsState by viewModel.bookingDetailsState.collectAsStateWithLifecycle()
    val cancelBookingState by viewModel.cancelBookingState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch booking details
    LaunchedEffect(bookingId) {
        Log.d(TAG, "📞 Fetching booking details for ID: $bookingId")
        viewModel.getBookingDetails(bookingId)
    }

    // Handle cancellation state
    LaunchedEffect(cancelBookingState) {
        when (val state = cancelBookingState) {
            is Resource.Success -> {
                isLoading = false
                Log.d(TAG, "✅ Booking cancelled successfully")
                snackbarHostState.showSnackbar(
                    message = "Booking cancelled successfully",
                    duration = SnackbarDuration.Short
                )
                navController.popBackStack()
            }
            is Resource.Error -> {
                isLoading = false
                val errorMsg = state.message ?: "Failed to cancel booking"
                Log.e(TAG, "❌ Cancel failed: $errorMsg")
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Long
                )
            }
            is Resource.Loading -> {
                isLoading = true
                Log.d(TAG, "⏳ Cancelling booking...")
            }
            else -> {}
        }
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
                    IconButton(onClick = { /* Share booking */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
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

                    Log.d(TAG, "✅ Booking loaded: ${booking.bookingId}")
                    Log.d(TAG, "   Location type: ${booking.serviceLocation}")
                    Log.d(TAG, "   Shop address: ${booking.shopAddress}")
                    Log.d(TAG, "   Service address: ${booking.serviceAddress}")

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
                            // Booking ID
                            Text(
                                text = "Booking #${booking.bookingId.takeLast(8)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Shop and Service Info
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
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

                            // ============= FIXED LOCATION DISPLAY WITH FULL ADDRESS =============
                            if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) {
                                // Home Service Address - Full address with city and pincode
                                InfoCard(
                                    icon = Icons.Default.Home,
                                    title = "Home Service Address",
                                    content = buildString {
                                        append(booking.serviceAddress)
                                        if (booking.customerCity.isNotBlank()) {
                                            append("\n${booking.customerCity}")
                                            if (booking.customerPincode.isNotBlank()) {
                                                append(" - ${booking.customerPincode}")
                                            }
                                        }
                                    }
                                )
                            } else {
                                // Shop Location - Full address with all details
                                InfoCard(
                                    icon = Icons.Default.Store,
                                    title = "Shop Location",
                                    content = buildString {
                                        // Shop address
                                        if (booking.shopAddress.isNotBlank()) {
                                            append(booking.shopAddress)
                                        } else {
                                            append(booking.shopName)
                                        }

                                        // Shop city if available
                                        if (booking.customerCity.isNotBlank()) {
                                            append("\n${booking.customerCity}")
                                        }

                                        // Shop pincode if available
                                        if (booking.customerPincode.isNotBlank()) {
                                            append(" - ${booking.customerPincode}")
                                        }

                                        // Shop contact as fallback
                                        if (booking.shopAddress.isBlank() &&
                                            booking.customerCity.isBlank() &&
                                            booking.customerPincode.isBlank()) {
                                            append("\n📍 Contact shop for exact location")
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Duration
                            if (booking.serviceDuration.isNotBlank()) {
                                InfoCard(
                                    icon = Icons.Default.Schedule,
                                    title = "Duration",
                                    content = booking.serviceDuration
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Customer Contact (for shop to contact)
                            if (booking.customerPhone.isNotBlank()) {
                                InfoCard(
                                    icon = Icons.Default.Phone,
                                    title = "Customer Contact",
                                    content = "${booking.customerName} - ${booking.customerPhone}"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Notes (if any)
                            if (booking.notes.isNotBlank()) {
                                InfoCard(
                                    icon = Icons.Default.Notes,
                                    title = "Additional Notes",
                                    content = booking.notes
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Cancellation Info (if cancelled or rejected)
                            if ((booking.status == BookingStatus.CANCELLED || booking.status == BookingStatus.REJECTED)
                                && booking.cancellationReason.isNotBlank()) {
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
                                                text = if (booking.status == BookingStatus.CANCELLED)
                                                    "Cancellation Reason" else "Rejection Reason",
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
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Buttons
                            when (booking.status) {
                                BookingStatus.PENDING -> {
                                    Button(
                                        onClick = { showCancelDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
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
                                            Text("Cancel Booking")
                                        }
                                    }
                                }
                                BookingStatus.CONFIRMED -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                // Open phone dialer
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Contact Shop")
                                        }
                                        Button(
                                            onClick = {
                                                // Open Google Maps with coordinates or address
                                            },
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
                                        onClick = {
                                            // Rate service
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rate This Service")
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { navController.popBackStack() },
                                        modifier = Modifier.fillMaxWidth()
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
                                        viewModel.getBookingDetails(bookingId)
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

    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
                cancelReason = ""
            },
            title = { Text("Cancel Booking") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to cancel this booking?",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentUser?.userId?.let { userId ->
                            viewModel.cancelBooking(bookingId, cancelReason, userId)
                        }
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    cancelReason = ""
                }) {
                    Text("Back")
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
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
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
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

private fun formatTime1(timeStr: String): String {
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