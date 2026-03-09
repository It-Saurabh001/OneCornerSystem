package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

private const val TAG = "BookingManagementScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    // ============= STATES =============
    val shopBookingsState by viewModel.shopBookingsState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingBookingsCount.collectAsStateWithLifecycle()
    val updateStatusState by viewModel.updateBookingStatusState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }
    var showLoadingOverlay by remember { mutableStateOf(false) }

    val tabs = listOf("Pending", "Confirmed", "Completed", "Cancelled/Rejected")

    // ============= REAL-TIME LISTENER =============
    LaunchedEffect(shopId) {
        Log.d(TAG, "Setting up real-time listener for shopId: $shopId")
        viewModel.listenToShopBookings(shopId)
    }

    // ============= PROPER WHEN HANDLING FOR UPDATE STATUS =============
    LaunchedEffect(updateStatusState) {
        when (updateStatusState) {
            is Resource.Success -> {
                // ✅ SUCCESS STATE
                isLoading = false
                showLoadingOverlay = false
                snackbarHostState.showSnackbar(
                    message = "Booking status updated successfully",
                    duration = SnackbarDuration.Short
                )
                Log.d(TAG, "✅ Update successful")

                // Reset states
                selectedBooking = null
                rejectReason = ""
                showRejectDialog = false
            }

            is Resource.Error -> {
                // ❌ ERROR STATE
                isLoading = false
                showLoadingOverlay = false
                val error = updateStatusState as Resource.Error
                val errorMessage = error.message ?: "Unknown error occurred"

                snackbarHostState.showSnackbar(
                    message = "Error: $errorMessage",
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Long
                )
                Log.e(TAG, "❌ Update failed: $errorMessage")
            }

            is Resource.Loading -> {
                // LOADING STATE
                isLoading = true
                showLoadingOverlay = true
                Log.d(TAG, "⏳ Processing request...")
            }

            is Resource.Idle -> {
                // 💤 IDLE STATE
                isLoading = false
                showLoadingOverlay = false
                Log.d(TAG, "ℹReady for next action")
            }

            else -> {
                // ❓ UNKNOWN STATE
                isLoading = false
                showLoadingOverlay = false
                Log.d(TAG, "ℹ️ Unknown state: ${updateStatusState::class.simpleName}")
            }
        }
    }

    // ============= HANDLE SNACKBAR ACTION (RETRY) =============
    val onRetry: (String) -> Unit = {
        selectedBooking?.let { booking ->
            when (booking.status) {
                BookingStatus.PENDING -> {
                    // Retry accept/reject
                    viewModel.updateBookingStatus(
                        bookingId = booking.bookingId,
                        status = BookingStatus.CONFIRMED,
                        shopId = shopId
                    )
                }
                else -> {
                    // Retry loading bookings
                    viewModel.listenToShopBookings(shopId)
                }
            }
        } ?: run {
            // No selected booking, just reload
            viewModel.listenToShopBookings(shopId)
        }
    }

    // ============= FILTER BOOKINGS BY TAB =============
    val filteredBookings = when (val state = shopBookingsState) {
        is Resource.Success -> {
            when (selectedTab) {
                0 -> state.data.filter { it.status == BookingStatus.PENDING }
                1 -> state.data.filter { it.status == BookingStatus.CONFIRMED }
                2 -> state.data.filter { it.status == BookingStatus.COMPLETED }
                3 -> state.data.filter {
                    it.status == BookingStatus.CANCELLED ||
                            it.status == BookingStatus.REJECTED
                }
                else -> emptyList()
            }
        }
        else -> emptyList()
    }

    // Log bookings count
    LaunchedEffect(shopBookingsState) {
        when (val state = shopBookingsState) {
            is Resource.Success -> {
                Log.d(TAG, "📊 Total bookings: ${state.data.size}")
                Log.d(TAG, "   Pending: ${state.data.count { it.status == BookingStatus.PENDING }}")
                Log.d(TAG, "   Confirmed: ${state.data.count { it.status == BookingStatus.CONFIRMED }}")
                Log.d(TAG, "   Completed: ${state.data.count { it.status == BookingStatus.COMPLETED }}")
                Log.d(TAG, "   Cancelled/Rejected: ${state.data.count { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED }}")
            }
            else -> {}
        }
    }

    // ============= REJECT DIALOG =============
    if (showRejectDialog && selectedBooking != null) {
        AlertDialog(
            onDismissRequest = {
                showRejectDialog = false
                rejectReason = ""
                selectedBooking = null
            },
            title = { Text("Reject Booking") },
            text = {
                Column {
                    Text("Are you sure you want to reject this booking?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Reason (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedBooking?.let { booking ->
                            Log.d(TAG, "Rejecting booking: ${booking.bookingId}")
                            viewModel.updateBookingStatus(
                                bookingId = booking.bookingId,
                                status = BookingStatus.REJECTED,
                                shopId = shopId,
                                reason = rejectReason
                            )
                        }
                        showRejectDialog = false
                        rejectReason = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    rejectReason = ""
                    selectedBooking = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ============= MAIN UI =============
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Booking Management")
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE53935)
                                )
                            ) {
                                Text(
                                    text = pendingCount.toString(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // ============= MAIN CONTENT =============
            when (val state = shopBookingsState) {
                // ⏳ LOADING STATE
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
                                text = "Loading bookings...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ✅ SUCCESS STATE
                is Resource.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Tab Row
                        TabRow(selectedTabIndex = selectedTab) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            title,
                                            fontSize = 12.sp
                                        )
                                    }
                                )
                            }
                        }

                        // Bookings List
                        if (filteredBookings.isEmpty()) {
                            // Empty state for current tab
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyBookingsPlaceholder(
                                    tabIndex = selectedTab,
                                    tabName = tabs[selectedTab]
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredBookings) { booking ->
                                    BookingCard(
                                        booking = booking,
                                        isLoading = isLoading,
                                        onAccept = {
                                            Log.d(TAG, "Accepting booking: ${booking.bookingId}")
                                            viewModel.updateBookingStatus(
                                                bookingId = booking.bookingId,
                                                status = BookingStatus.CONFIRMED,
                                                shopId = shopId
                                            )
                                        },
                                        onReject = {
                                            selectedBooking = booking
                                            showRejectDialog = true
                                        },
                                        onComplete = {
                                            Log.d(TAG, "Completing booking: ${booking.bookingId}")
                                            viewModel.updateBookingStatus(
                                                bookingId = booking.bookingId,
                                                status = BookingStatus.COMPLETED,
                                                shopId = shopId
                                            )
                                        },
                                        onViewDetails = {
                                            Log.d(TAG, "Viewing booking details: ${booking.bookingId}")
                                            navController.navigate("shop_booking_details/${booking.bookingId}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ❌ ERROR STATE
                is Resource.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error loading bookings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message ?: "Something went wrong",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.listenToShopBookings(shopId) }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                // 💤 IDLE STATE
                is Resource.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ready to load bookings...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.listenToShopBookings(shopId) }
                            ) {
                                Text("Load Bookings")
                            }
                        }
                    }
                }

                // ❓ ELSE STATE (should never happen)
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unknown state occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // ============= LOADING OVERLAY =============
            if (showLoadingOverlay) {
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
                                text = when (updateStatusState) {
                                    is Resource.Loading -> "Processing..."
                                    else -> "Please wait..."
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============= BOOKING CARD COMPOSABLE =============
@Composable
fun BookingCard(
    booking: Booking,
    isLoading: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (booking.status) {
                BookingStatus.PENDING -> Color(0xFFFFF3E0)      // Light Yellow
                BookingStatus.CONFIRMED -> Color(0xFFE3F2FD)    // Light Blue
                BookingStatus.COMPLETED -> Color(0xFFE8F5E8)    // Light Green
                BookingStatus.CANCELLED, BookingStatus.REJECTED -> Color(0xFFFFEBEE) // Light Red
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Booking #${booking.bookingId.takeLast(6)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                BookingStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = booking.customerName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = " • ${booking.customerPhone}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Service Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.serviceName,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "₹${booking.servicePrice}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Date & Time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${formatDate(booking.bookingDate)} at ${formatTime(booking.bookingTime)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Location (if home service)
            if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME && booking.serviceAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.serviceAddress,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // Cancellation/Rejection Reason
            if ((booking.status == BookingStatus.REJECTED || booking.status == BookingStatus.CANCELLED)
                && booking.cancellationReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (booking.status == BookingStatus.REJECTED)
                            "Rejected: ${booking.cancellationReason}"
                        else "Cancelled: ${booking.cancellationReason}",
                        fontSize = 11.sp,
                        color = Color(0xFFE53935),
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ============= ACTION BUTTONS BASED ON STATUS =============
            when (booking.status) {
                BookingStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAccept,
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
                            onClick = onReject,
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onComplete,
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
                            onClick = onViewDetails,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Text("Details")
                        }
                    }
                }

                BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.REJECTED -> {
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("View Details")
                    }
                }

                else -> {
                    // For any other status (IN_PROGRESS, NO_SHOW, etc.)
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

// ============= BOOKING STATUS CHIP =============
@Composable
fun BookingStatusChip(status: BookingStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        BookingStatus.PENDING -> Triple(
            Color(0xFFFFC107).copy(alpha = 0.2f),
            Color(0xFFFFC107),
            "Pending"
        )
        BookingStatus.CONFIRMED -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.2f),
            Color(0xFF2196F3),
            "Confirmed"
        )
        BookingStatus.IN_PROGRESS -> Triple(
            Color(0xFF9C27B0).copy(alpha = 0.2f),
            Color(0xFF9C27B0),
            "In Progress"
        )
        BookingStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
            "Completed"
        )
        BookingStatus.CANCELLED -> Triple(
            Color(0xFFE53935).copy(alpha = 0.2f),
            Color(0xFFE53935),
            "Cancelled"
        )
        BookingStatus.REJECTED -> Triple(
            Color(0xFFE53935).copy(alpha = 0.2f),
            Color(0xFFE53935),
            "Rejected"
        )
        BookingStatus.NO_SHOW -> Triple(
            Color(0xFF757575).copy(alpha = 0.2f),
            Color(0xFF757575),
            "No Show"
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

// ============= EMPTY BOOKINGS PLACEHOLDER =============
@Composable
fun EmptyBookingsPlaceholder(
    tabIndex: Int,
    tabName: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = when (tabIndex) {
                0 -> Icons.Default.HourglassEmpty
                1 -> Icons.Default.CheckCircle
                2 -> Icons.Default.DoneAll
                else -> Icons.Default.Cancel
            },
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No $tabName",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (tabIndex) {
                0 -> "You don't have any pending bookings"
                1 -> "No confirmed bookings yet"
                2 -> "No completed bookings yet"
                else -> "No cancelled or rejected bookings"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

// ============= HELPER FUNCTIONS =============
private fun formatDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("dd MMM", Locale.US)
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