package com.saurabh.onecornersystem.presentation.customer

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

private const val TAG = "MyBookingsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val myBookingsState by viewModel.myBookingsState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val cancelBookingState by viewModel.cancelBookingState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    val tabs = listOf("All", "Pending", "Confirmed", "Completed")

    // ============= FETCH BOOKINGS WHEN USER CHANGES =============
    LaunchedEffect(currentUser) {
        currentUser?.userId?.let { userId ->
            Log.d(TAG, "📞 Fetching bookings for user: $userId")
            isRefreshing = true
            viewModel.getMyBookings(userId)
        } ?: run {
            Log.d(TAG, "⏳ Waiting for user to load...")
        }
    }

    // ============= HANDLE BOOKING STATE CHANGES =============
    LaunchedEffect(myBookingsState) {
        when (myBookingsState) {
            is Resource.Loading -> {
                isRefreshing = true
                Log.d(TAG, "⏳ Loading bookings...")
            }
            is Resource.Success -> {
                isRefreshing = false
                val count = (myBookingsState as Resource.Success).data.size
                Log.d(TAG, "✅ Loaded $count bookings")
            }
            is Resource.Error -> {
                isRefreshing = false
                val error = (myBookingsState as Resource.Error).message
                Log.e(TAG, "❌ Error loading bookings: $error")
            }
            else -> {}
        }
    }

    // ============= HANDLE CANCELLATION STATE =============
    LaunchedEffect(cancelBookingState) {
        when (val state = cancelBookingState) {
            is Resource.Success -> {
                Log.d(TAG, "✅ Booking cancelled successfully")
                snackbarHostState.showSnackbar(
                    message = "Booking cancelled successfully",
                    duration = SnackbarDuration.Short
                )
                // Refresh bookings
                currentUser?.userId?.let { userId ->
                    viewModel.getMyBookings(userId)
                }
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Cancel failed: ${state.message}")
                snackbarHostState.showSnackbar(
                    message = state.message ?: "Failed to cancel booking",
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Long
                )
            }
            is Resource.Loading -> {
                Log.d(TAG, "⏳ Cancelling booking...")
            }
            else -> {}
        }
    }

    // ============= CANCEL DIALOG =============
    if (showCancelDialog && selectedBooking != null) {
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
                cancelReason = ""
                selectedBooking = null
            },
            title = { Text("Cancel Booking") },
            text = {
                Column {
                    Text("Are you sure you want to cancel this booking?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
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
                            Log.d(TAG, "Cancelling booking: ${booking.bookingId}")
                            viewModel.cancelBooking(
                                bookingId = booking.bookingId,
                                reason = cancelReason,
                                customerId = currentUser?.userId ?: ""
                            )
                        }
                        showCancelDialog = false
                        cancelReason = ""
                        selectedBooking = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    cancelReason = ""
                    selectedBooking = null
                }) {
                    Text("Back")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = {
                            currentUser?.userId?.let { userId ->
                                isRefreshing = true
                                viewModel.getMyBookings(userId)
                            }
                        }
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            // Bookings List
            when (val state = myBookingsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading your bookings...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is Resource.Success -> {
                    val filteredBookings = filterBookingsByTab(state.data, selectedTab)

                    if (filteredBookings.isEmpty()) {
                        EmptyBookingsPlaceholder(
                            tabName = tabs[selectedTab],
                            onBrowseClick = {
                                navController.popBackStack()
                            }
                        )
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
                                    onCancelClick = {
                                        selectedBooking = booking
                                        showCancelDialog = true
                                    },
                                    onClick = {
                                        navController.navigate("booking_details/${booking.bookingId}")
                                    }
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                                text = "Failed to load bookings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (state.message?.contains("index") == true)
                                    "Database is being set up. Please wait a moment and try again."
                                else state.message ?: "Something went wrong",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    currentUser?.userId?.let { userId ->
                                        viewModel.getMyBookings(userId)
                                    }
                                }
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onCancelClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (booking.status) {
                BookingStatus.PENDING -> Color(0xFFFFF3E0)
                BookingStatus.CONFIRMED -> Color(0xFFE3F2FD)
                BookingStatus.COMPLETED -> Color(0xFFE8F5E8)
                BookingStatus.CANCELLED, BookingStatus.REJECTED -> Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop Name
                Text(
                    text = booking.shopName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Status Chip
                BookingStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Service Name
            Text(
                text = booking.serviceName,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date and Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatBookingDate(booking.bookingDate),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTime(booking.bookingTime),
                        fontSize = 12.sp
                    )
                }

                // Price
                Text(
                    text = "₹${booking.servicePrice}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.serviceAddress.take(30) +
                                if (booking.serviceAddress.length > 30) "..." else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Cancellation Reason
            if ((booking.status == BookingStatus.CANCELLED || booking.status == BookingStatus.REJECTED)
                && booking.cancellationReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFE53935)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = booking.cancellationReason,
                        fontSize = 11.sp,
                        color = Color(0xFFE53935),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons based on status
            when (booking.status) {
                BookingStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = onClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Details")
                        }
                    }
                }
                BookingStatus.CONFIRMED -> {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Details")
                    }
                }
                else -> {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusChip(status: BookingStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        BookingStatus.PENDING -> Triple(
            Color(0xFFFFC107).copy(alpha = 0.2f),
            Color(0xFFFFC107),
            "PENDING"
        )
        BookingStatus.CONFIRMED -> Triple(
            Color(0xFF2196F3).copy(alpha = 0.2f),
            Color(0xFF2196F3),
            "CONFIRMED"
        )
        BookingStatus.IN_PROGRESS -> Triple(
            Color(0xFF9C27B0).copy(alpha = 0.2f),
            Color(0xFF9C27B0),
            "IN PROGRESS"
        )
        BookingStatus.COMPLETED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF4CAF50),
            "COMPLETED"
        )
        BookingStatus.CANCELLED -> Triple(
            Color(0xFFE53935).copy(alpha = 0.2f),
            Color(0xFFE53935),
            "CANCELLED"
        )
        BookingStatus.REJECTED -> Triple(
            Color(0xFFE53935).copy(alpha = 0.2f),
            Color(0xFFE53935),
            "REJECTED"
        )
        BookingStatus.NO_SHOW -> Triple(
            Color(0xFF757575).copy(alpha = 0.2f),
            Color(0xFF757575),
            "NO SHOW"
        )
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyBookingsPlaceholder(
    tabName: String,
    onBrowseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (tabName == "All") "No Bookings Yet" else "No $tabName Bookings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (tabName) {
                "All" -> "Book a service to see it here"
                "Pending" -> "You don't have any pending bookings"
                "Confirmed" -> "No confirmed bookings yet"
                "Completed" -> "No completed bookings yet"
                else -> "Book a service to see it here"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBrowseClick
        ) {
            Text("Browse Services")
        }
    }
}

private fun filterBookingsByTab(bookings: List<Booking>, tabIndex: Int): List<Booking> {
    return when (tabIndex) {
        0 -> bookings
        1 -> bookings.filter { it.status == BookingStatus.PENDING }
        2 -> bookings.filter { it.status == BookingStatus.CONFIRMED }
        3 -> bookings.filter { it.status == BookingStatus.COMPLETED }
        else -> bookings
    }
}

private fun formatBookingDate(dateStr: String): String {
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