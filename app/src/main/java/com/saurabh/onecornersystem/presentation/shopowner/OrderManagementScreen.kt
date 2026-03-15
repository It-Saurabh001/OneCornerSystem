package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderManagementScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val shopBookingsState by viewModel.shopBookingsState.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingBookingsCount.collectAsStateWithLifecycle()
    val updateStatusState by viewModel.updateBookingStatusState.collectAsStateWithLifecycle()

    // --- THEME COLORS ---
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    var selectedTab by remember { mutableStateOf(0) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    val tabs = listOf("Pending", "Confirmed", "Completed", "History")

    LaunchedEffect(shopId) {
        viewModel.listenToShopBookings(shopId)
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(300.dp).offset(x = 150.dp, y = (-50).dp).blur(100.dp).background(amberOrange.copy(alpha = 0.12f), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 100.dp).blur(80.dp).background(amberOrange.copy(alpha = 0.08f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Orders", fontWeight = FontWeight.Black)
                            if (pendingCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(color = Color.Red, shape = CircleShape) {
                                    Text(pendingCount.toString(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // 1. LIQUID TAB ROW
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = amberOrange,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = amberOrange
                        )
                    },
                    divider = { HorizontalDivider(color = outlineWhite) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // 2. CONTENT HANDLING
                when (val state = shopBookingsState) {
                    is Resource.Loading -> FullLoadingScreenLiquid(amberOrange)
                    is Resource.Success -> {
                        val filtered = filterBookings(state.data, selectedTab)
                        if (filtered.isEmpty()) {
                            EmptyBookingsPlaceholderLiquid(selectedTab, tabs[selectedTab], amberOrange)
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filtered) { booking ->
                                    BookingCardLiquid(
                                        booking = booking,
                                        accent = amberOrange,
                                        outline = outlineWhite,
                                        bg = glassWhite,
                                        onAccept = { viewModel.updateBookingStatus(booking.bookingId, BookingStatus.CONFIRMED, shopId) },
                                        onReject = { selectedBooking = booking; showRejectDialog = true },
                                        onComplete = { viewModel.updateBookingStatus(booking.bookingId, BookingStatus.COMPLETED, shopId) },
                                        onViewDetails = { navController.navigate("shop_booking_details/${booking.bookingId}") }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> ErrorCardGlass2(state.message, amberOrange, outlineWhite) { viewModel.listenToShopBookings(shopId) }
                    else -> {}
                }
            }
        }

        // 3. REJECT DIALOG (Amber Glassy)
        if (showRejectDialog && selectedBooking != null) {
            AlertDialog(
                containerColor = Color(0xFF151515),
                onDismissRequest = { showRejectDialog = false },
                title = { Text("Reject Request", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Please specify why you are rejecting this booking.", color = Color.Gray, fontSize = 14.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = rejectReason, onValueChange = { rejectReason = it },
                            placeholder = { Text("Reason (Optional)", color = Color.DarkGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = amberOrange, unfocusedBorderColor = outlineWhite, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateBookingStatus(selectedBooking!!.bookingId, BookingStatus.REJECTED, shopId, rejectReason)
                        showRejectDialog = false
                    }) { Text("CONFIRM REJECT", color = Color.Red, fontWeight = FontWeight.Bold) }
                }
            )
        }

        // 4. LOADING OVERLAY
        if (updateStatusState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = amberOrange)
            }
        }
    }
}

@Composable
fun BookingCardLiquid(
    booking: Booking,
    accent: Color,
    outline: Color,
    bg: Color,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = bg,
        shape = RoundedCornerShape(24.dp),
        onClick = onViewDetails
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("#${booking.bookingId.takeLast(6).uppercase()}", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                BookingStatusChipLiquid(booking.status, accent)
            }

            Spacer(Modifier.height(12.dp))

            Text(booking.customerName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text(booking.serviceName, color = accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("${formatDate(booking.bookingDate)} • ${formatTime(booking.bookingTime)}", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            // ACTIONS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (booking.status) {
                    BookingStatus.PENDING -> {
                        Button(onClick = onAccept, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text("ACCEPT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(onClick = onReject, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) {
                            Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    BookingStatus.CONFIRMED -> {
                        Button(onClick = onComplete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                            Text("COMPLETE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    else -> {
                        OutlinedButton(onClick = onViewDetails, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), border = borderStroke(outline)) {
                            Text("VIEW DETAILS", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusChipLiquid(status: BookingStatus, accent: Color) {
    val color = when (status) {
        BookingStatus.PENDING -> Color(0xFFFFC107)
        BookingStatus.CONFIRMED -> Color(0xFF2196F3)
        BookingStatus.COMPLETED -> Color(0xFF4CAF50)
        else -> Color.Red
    }
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.border(0.5.dp, color.copy(alpha = 0.4f), CircleShape)) {
        Text(status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun EmptyBookingsPlaceholderLiquid(index: Int, name: String, accent: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(60.dp), tint = Color.DarkGray)
        Spacer(Modifier.height(16.dp))
        Text("No $name orders", color = Color.White, fontWeight = FontWeight.Bold)
        Text("New requests will appear here", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 13.sp)
    }
}

@Composable
fun FullLoadingScreenLiquid(accent: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = accent)
    }
}


// --- HELPERS ---
private fun filterBookings(list: List<Booking>, tab: Int): List<Booking> = when (tab) {
    0 -> list.filter { it.status == BookingStatus.PENDING }
    1 -> list.filter { it.status == BookingStatus.CONFIRMED }
    2 -> list.filter { it.status == BookingStatus.COMPLETED }
    else -> list.filter { it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REJECTED }
}

private fun formatDate(s: String): String = try {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s)
    SimpleDateFormat("dd MMM", Locale.US).format(date!!)
} catch (e: Exception) { s }

private fun formatTime(s: String): String = try {
    val parts = s.split(":")
    var h = parts[0].toInt()
    val amPm = if (h >= 12) "PM" else "AM"
    h = if (h > 12) h - 12 else if (h == 0) 12 else h
    "$h:${parts[1]} $amPm"
} catch (e: Exception) { s }

private fun borderStroke(c: Color) = androidx.compose.foundation.BorderStroke(1.dp, c)

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun OrderManagementPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Amber Order Management", color = Color.White, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(20.dp))
                BookingStatusChipLiquid(BookingStatus.PENDING, Color(0xFFFF9100))
            }
        }
    }
}