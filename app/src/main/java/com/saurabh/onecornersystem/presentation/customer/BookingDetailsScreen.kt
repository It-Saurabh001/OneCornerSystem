package com.saurabh.onecornersystem.presentation.customer

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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

// Liquid Theme Colors (matching CustomerHomeScreen)
private val DeepBlack = Color(0xFF0A0A0A)
private val ElectricBlue = Color(0xFF2979FF)
private val ElectricBlueDark = Color(0xFF1565C0)
private val GlassWhite = Color.White.copy(alpha = 0.05f)
private val GlassWhiteStrong = Color.White.copy(alpha = 0.08f)
private val OutlineWhite = Color.White.copy(alpha = 0.15f)
private val SurfaceCard = Color.White.copy(alpha = 0.04f)

// Status Colors (adjusted for dark theme)
private val StatusPending = Color(0xFFFFB74D)      // Warm orange
private val StatusConfirmed = ElectricBlue          // Electric blue
private val StatusInProgress = Color(0xFFCE93D8)   // Soft purple
private val StatusCompleted = Color(0xFF81C784)    // Soft green
private val StatusCancelled = Color(0xFFEF5350)    // Soft red
private val StatusRejected = Color(0xFFEF5350)     // Soft red
private val StatusNoShow = Color(0xFF9E9E9E)       // Gray

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

    // Chat button colors
    val chatColor = Color(0xFF4CAF50)  // Green for chat

    LaunchedEffect(bookingId) {
        Log.d(TAG, "📞 Fetching booking details for ID: $bookingId")
        viewModel.getBookingDetails(bookingId)
    }

    LaunchedEffect(cancelBookingState) {
        when (val state = cancelBookingState) {
            is Resource.Success -> {
                isLoading = false
                snackbarHostState.showSnackbar("Booking cancelled successfully")
                navController.popBackStack()
            }
            is Resource.Error -> {
                isLoading = false
                snackbarHostState.showSnackbar(state.message ?: "Failed to cancel")
            }
            is Resource.Loading -> { isLoading = true }
            else -> {}
        }
    }

    Scaffold(
        modifier = Modifier.background(DeepBlack),
        containerColor = DeepBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Text(
                        "Booking Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Chat Icon in Top Bar
                    when (val state = bookingDetailsState) {
                        is Resource.Success -> {
                            val booking = state.data
                            if (booking.status == BookingStatus.PENDING ||
                                booking.status == BookingStatus.CONFIRMED ||
                                booking.status == BookingStatus.IN_PROGRESS) {
                                IconButton(onClick = {
                                    val encodedName = android.net.Uri.encode(booking.shopName)
                                    navController.navigate(
                                        "customer_chat?bookingId=${booking.bookingId}&shopId=${booking.shopId}&shopName=$encodedName&shopImage="
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.Chat,
                                        contentDescription = "Chat with Shop",
                                        tint = chatColor
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    // Share Button
                    IconButton(onClick = { /* Share booking */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = ElectricBlue)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepBlack)
        ) {
            // --- LIQUID BLOBS (Background effect matching CustomerHomeScreen) ---
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(x = (-50).dp, y = 100.dp)
                    .blur(120.dp)
                    .background(ElectricBlue.copy(alpha = 0.15f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 50.dp, y = (-20).dp)
                    .blur(100.dp)
                    .background(ElectricBlue.copy(alpha = 0.1f), CircleShape)
            )

            when (val state = bookingDetailsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ElectricBlue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Loading booking details...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
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
                        // Status Banner (updated to match liquid theme)
                        LiquidStatusBanner(status = booking.status)

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            // Booking ID
                            Text(
                                text = "Booking #${booking.bookingId.takeLast(8)}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Shop and Service Info Card (Glassmorphism)
                            LiquidInfoCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        booking.shopName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        booking.serviceName,
                                        fontSize = 16.sp,
                                        color = ElectricBlue,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Text(
                                            "Price",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            "₹${booking.servicePrice}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricBlue
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Date & Time
                            LiquidDetailCard(
                                icon = Icons.Default.CalendarToday,
                                title = "Date & Time",
                                content = "${formatFullDate(booking.bookingDate)} at ${formatTime7(booking.bookingTime)}"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Location
                            if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) {
                                LiquidDetailCard(
                                    icon = Icons.Default.Home,
                                    title = "Home Service Address",
                                    content = buildString {
                                        append(booking.serviceAddress)
                                        if (booking.customerCity.isNotBlank()) append("\n${booking.customerCity}")
                                        if (booking.customerPincode.isNotBlank()) append(" - ${booking.customerPincode}")
                                    }
                                )
                            } else {
                                LiquidDetailCard(
                                    icon = Icons.Default.Store,
                                    title = "Shop Location",
                                    content = buildString {
                                        if (booking.shopAddress.isNotBlank()) append(booking.shopAddress)
                                        else append(booking.shopName)
                                        if (booking.customerCity.isNotBlank()) append("\n${booking.customerCity}")
                                        if (booking.customerPincode.isNotBlank()) append(" - ${booking.customerPincode}")
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Duration
                            if (booking.serviceDuration.isNotBlank()) {
                                LiquidDetailCard(
                                    icon = Icons.Default.Schedule,
                                    title = "Duration",
                                    content = booking.serviceDuration
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Customer Contact
                            if (booking.customerPhone.isNotBlank()) {
                                LiquidDetailCard(
                                    icon = Icons.Default.Phone,
                                    title = "Customer Contact",
                                    content = "${booking.customerName} - ${booking.customerPhone}"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Notes
                            if (booking.notes.isNotBlank()) {
                                LiquidDetailCard(
                                    icon = Icons.Default.Notes,
                                    title = "Additional Notes",
                                    content = booking.notes
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Cancellation Info
                            if ((booking.status == BookingStatus.CANCELLED || booking.status == BookingStatus.REJECTED)
                                && booking.cancellationReason.isNotBlank()) {
                                LiquidWarningCard(
                                    reason = booking.cancellationReason,
                                    isRejected = booking.status == BookingStatus.REJECTED
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // ========== CHAT BUTTON (For active bookings) ==========
                            if (booking.status == BookingStatus.PENDING ||
                                booking.status == BookingStatus.CONFIRMED ||
                                booking.status == BookingStatus.IN_PROGRESS) {

                                Button(
                                    onClick = {
                                        val encodedName = android.net.Uri.encode(booking.shopName)
                                        navController.navigate(
                                            "customer_chat?bookingId=${booking.bookingId}&shopId=${booking.shopId}&shopName=$encodedName&shopImage="
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = chatColor),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("MESSAGE SHOP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Buttons (Liquid Style)
                            when (booking.status) {
                                BookingStatus.PENDING -> {
                                    LiquidDangerButton(
                                        onClick = { showCancelDialog = true },
                                        isLoading = isLoading,
                                        text = "Cancel Booking"
                                    )
                                }
                                BookingStatus.CONFIRMED -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        LiquidOutlinedButton(
                                            onClick = { /* Open phone */ },
                                            icon = Icons.Default.Phone,
                                            text = "Call Shop",
                                            modifier = Modifier.weight(1f)
                                        )
                                        LiquidPrimaryButton(
                                            onClick = { /* Open maps */ },
                                            icon = Icons.Default.Map,
                                            text = "Directions",
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                BookingStatus.COMPLETED -> {
                                    LiquidPrimaryButton(
                                        onClick = { /* Rate service */ },
                                        icon = Icons.Default.Star,
                                        text = "Rate This Service",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                else -> {
                                    LiquidOutlinedButton(
                                        onClick = { navController.popBackStack() },
                                        icon = Icons.Default.ArrowBack,
                                        text = "Back to Bookings",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = StatusCancelled
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Failed to load booking",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                state.message ?: "Something went wrong",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LiquidOutlinedButton(
                                    onClick = { navController.popBackStack() },
                                    icon = Icons.Default.ArrowBack,
                                    text = "Go Back"
                                )
                                LiquidPrimaryButton(
                                    onClick = { viewModel.getBookingDetails(bookingId) },
                                    icon = Icons.Default.Refresh,
                                    text = "Retry"
                                )
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
                        .background(Color.Black.copy(alpha = 0.7f))
                        .blur(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.padding(24.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = GlassWhiteStrong,
                        border = BorderStroke(1.dp, OutlineWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = ElectricBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Processing...",
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Cancel Dialog (FIXED for Material3)
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false; cancelReason = "" },
            shape = RoundedCornerShape(20.dp),
            containerColor = SurfaceCard,
            title = {
                Text(
                    "Cancel Booking",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to cancel this booking?",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Reason for cancellation (optional)", color = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = OutlineWhite,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = ElectricBlue,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        )
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
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusCancelled)
                ) {
                    Text("Confirm Cancel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false; cancelReason = "" },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                ) {
                    Text("Back")
                }
            }
        )
    }
}

// ========== LIQUID COMPONENTS (Matching CustomerHomeScreen Style) ==========

@Composable
fun LiquidStatusBanner(status: BookingStatus) {
    val (backgroundColor, statusText) = when (status) {
        BookingStatus.PENDING -> Pair(StatusPending.copy(alpha = 0.15f), "PENDING")
        BookingStatus.CONFIRMED -> Pair(StatusConfirmed.copy(alpha = 0.15f), "CONFIRMED")
        BookingStatus.IN_PROGRESS -> Pair(StatusInProgress.copy(alpha = 0.15f), "IN PROGRESS")
        BookingStatus.COMPLETED -> Pair(StatusCompleted.copy(alpha = 0.15f), "COMPLETED")
        BookingStatus.CANCELLED -> Pair(StatusCancelled.copy(alpha = 0.15f), "CANCELLED")
        BookingStatus.REJECTED -> Pair(StatusRejected.copy(alpha = 0.15f), "REJECTED")
        BookingStatus.NO_SHOW -> Pair(StatusNoShow.copy(alpha = 0.15f), "NO SHOW")
    }

    val textColor = when (status) {
        BookingStatus.PENDING -> StatusPending
        BookingStatus.CONFIRMED -> StatusConfirmed
        BookingStatus.IN_PROGRESS -> StatusInProgress
        BookingStatus.COMPLETED -> StatusCompleted
        BookingStatus.CANCELLED -> StatusCancelled
        BookingStatus.REJECTED -> StatusRejected
        BookingStatus.NO_SHOW -> StatusNoShow
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(0.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(textColor)
                )
                Text(
                    text = statusText,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun LiquidInfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .border(1.dp, OutlineWhite, RoundedCornerShape(20.dp)),
        color = SurfaceCard,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun LiquidDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, OutlineWhite, RoundedCornerShape(16.dp)),
        color = GlassWhite,
        shape = RoundedCornerShape(16.dp)
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
                tint = ElectricBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun LiquidWarningCard(
    reason: String,
    isRejected: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, StatusCancelled.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        color = StatusCancelled.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                null,
                tint = StatusCancelled,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    if (isRejected) "Rejection Reason" else "Cancellation Reason",
                    fontSize = 12.sp,
                    color = StatusCancelled.copy(alpha = 0.7f),
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    reason,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = StatusCancelled
                )
            }
        }
    }
}

@Composable
fun LiquidPrimaryButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        } else {
            Icon(icon, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun LiquidOutlinedButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, OutlineWhite)
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = ElectricBlue)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
    }
}

@Composable
fun LiquidDangerButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = StatusCancelled),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        } else {
            Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

// ========== HELPER FUNCTIONS ==========

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

private fun formatTime7(timeStr: String): String {
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