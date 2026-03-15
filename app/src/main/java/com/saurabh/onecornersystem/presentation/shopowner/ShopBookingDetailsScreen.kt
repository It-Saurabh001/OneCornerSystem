package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.BookingStatus
import com.saurabh.onecornersystem.data.model.ServiceLocation
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopBookingDetailsScreen(
    bookingId: String,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val bookingDetailsState by viewModel.shopBookingDetailsState.collectAsStateWithLifecycle()
    val updateStatusState by viewModel.updateBookingStatusState.collectAsStateWithLifecycle()

    // --- THEME COLORS ---
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    var showActionDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf<BookingStatus?>(null) }
    var actionReason by remember { mutableStateOf("") }

    LaunchedEffect(bookingId) {
        viewModel.getShopBookingDetails(bookingId)
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(300.dp).offset(x = 180.dp, y = (-50).dp).blur(100.dp).background(amberOrange.copy(alpha = 0.12f), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-80).dp, y = 100.dp).blur(90.dp).background(amberOrange.copy(alpha = 0.08f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Booking Hub", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = bookingDetailsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = amberOrange)
                    }
                }
                is Resource.Success -> {
                    val booking = state.data
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
                    ) {
                        // 1. Sleek Status Banner
                        ShopBookingStatusBannerLiquid(status = booking.status, accent = amberOrange)

                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("TICKET ID: #${booking.bookingId.takeLast(8).uppercase()}", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                            Spacer(modifier = Modifier.height(20.dp))

                            // 2. Customer Section
                            GlassySectionCard("Customer Information", outlineWhite) {
                                InfoRowLiquid(Icons.Default.Person, "Client Name", booking.customerName, amberOrange)
                                InfoRowLiquid(Icons.Default.Phone, "Contact", booking.customerPhone, amberOrange)
                                InfoRowLiquid(Icons.Default.Email, "Email Address", booking.customerEmail, amberOrange)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 3. Service Details
                            GlassySectionCard("Service Highlights", outlineWhite) {
                                InfoRowLiquid(Icons.Default.Build, "Service", booking.serviceName, amberOrange)
                                InfoRowLiquid(Icons.Default.Schedule, "Duration", booking.serviceDuration, amberOrange)
                                InfoRowLiquid(Icons.Default.Payments, "Final Price", "₹${booking.servicePrice}", amberOrange)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4. Appointment Schedule
                            GlassySectionCard("Booking Schedule", outlineWhite) {
                                InfoRowLiquid(Icons.Default.CalendarToday, "Scheduled Date", formatFullDate(booking.bookingDate), amberOrange)
                                InfoRowLiquid(Icons.Default.AccessTime, "Time Slot", formatTime(booking.bookingTime), amberOrange)
                                InfoRowLiquid(
                                    icon = if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) Icons.Default.Home else Icons.Default.Store,
                                    label = "Location Mode",
                                    value = if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) "Doorstep Service" else "In-Shop Visit",
                                    accent = amberOrange
                                )
                                if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) {
                                    InfoRowLiquid(Icons.Default.LocationOn, "Full Address", "${booking.serviceAddress}, ${booking.customerCity}", amberOrange)
                                }
                            }

                            if (booking.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                GlassySectionCard("Client Notes", outlineWhite) {
                                    Text(booking.notes, color = Color.Gray, fontSize = 13.sp, lineHeight = 20.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // 5. Action Controls
                            BookingActionRow(
                                status = booking.status,
                                isLoading = updateStatusState is Resource.Loading,
                                onAccept = { viewModel.updateBookingStatus(booking.bookingId, BookingStatus.CONFIRMED, booking.shopId) },
                                onReject = { actionType = BookingStatus.REJECTED; showActionDialog = true },
                                onComplete = { viewModel.updateBookingStatus(booking.bookingId, BookingStatus.COMPLETED, booking.shopId) },
                                accent = amberOrange
                            )

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorCardGlass2(msg = state.message, accent = amberOrange, outline = outlineWhite) {
                        viewModel.getShopBookingDetails(bookingId)
                    }
                }
                else -> {}
            }
        }
    }

    // Action Dialog (Updated to Dark Glassy Theme)
    if (showActionDialog && actionType != null) {
        AlertDialog(
            containerColor = Color(0xFF151515),
            onDismissRequest = { showActionDialog = false },
            title = { Text("Confirm ${actionType?.name}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please provide a reason for this action (optional):", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = actionReason,
                        onValueChange = { actionReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = amberOrange, unfocusedBorderColor = outlineWhite, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    (bookingDetailsState as? Resource.Success)?.data?.let {
                        viewModel.updateBookingStatus(it.bookingId, actionType!!, it.shopId, actionReason)
                    }
                    showActionDialog = false
                }) { Text("PROCEED", color = amberOrange, fontWeight = FontWeight.Black) }
            }
        )
    }
}

@Composable
fun ShopBookingStatusBannerLiquid(status: BookingStatus, accent: Color) {
    val color = when (status) {
        BookingStatus.PENDING -> Color(0xFFFFC107)
        BookingStatus.CONFIRMED -> Color(0xFF2196F3)
        BookingStatus.COMPLETED -> Color(0xFF4CAF50)
        BookingStatus.CANCELLED, BookingStatus.REJECTED -> Color(0xFFE53935)
        else -> accent
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(Modifier.width(12.dp))
                Text(status.name, color = color, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun InfoRowLiquid(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, accent: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
fun BookingActionRow(status: BookingStatus, isLoading: Boolean, onAccept: () -> Unit, onReject: () -> Unit, onComplete: () -> Unit, accent: Color) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = accent)
        }
    } else {
        when (status) {
            BookingStatus.PENDING -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onAccept, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                        Text("ACCEPT", fontWeight = FontWeight.Black)
                    }
                    Button(onClick = onReject, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) {
                        Text("REJECT", fontWeight = FontWeight.Black)
                    }
                }
            }
            BookingStatus.CONFIRMED -> {
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                    Text("MARK AS COMPLETED", fontWeight = FontWeight.Black)
                }
            }
            else -> { /* No actions for other states */ }
        }
    }
}

private fun formatFullDate(dateStr: String): String = try {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
    SimpleDateFormat("dd MMM yyyy", Locale.US).format(date!!)
} catch (e: Exception) { dateStr }

private fun formatTime(timeStr: String): String = try {
    val parts = timeStr.split(":")
    var hour = parts[0].toInt()
    val amPm = if (hour >= 12) "PM" else "AM"
    hour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
    "$hour:${parts[1]} $amPm"
} catch (e: Exception) { timeStr }

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ShopBookingDetailsPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Booking Details Preview", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                ShopBookingStatusBannerLiquid(BookingStatus.PENDING, Color(0xFFFF9100))
                Spacer(modifier = Modifier.height(20.dp))
                GlassySectionCard("Mock Section", Color.White.copy(0.1f)) {
                    Text("Sample Detail Row", color = Color.Gray)
                }
            }
        }
    }
}