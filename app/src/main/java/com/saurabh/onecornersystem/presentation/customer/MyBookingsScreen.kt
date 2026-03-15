package com.saurabh.onecornersystem.presentation.customer

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.tooling.preview.Preview

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
    val tabs = listOf("All", "Pending", "Confirmed", "Completed")

    // Theme Colors
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.15f)

    LaunchedEffect(currentUser) {
        currentUser?.userId?.let { viewModel.getMyBookings(it) }
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack.copy(alpha = 0.95f))) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(300.dp).offset(x = (-100).dp, y = 200.dp).blur(130.dp).background(electricBlue.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.TopEnd).offset(x = 50.dp, y = (-50).dp).blur(100.dp).background(electricBlue.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("My Bookings", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                    val navItems = listOf(
                        Triple("Home", Icons.Default.Home, "customer_home"),
                        Triple("Bookings", Icons.Default.Bookmark, "my_bookings"),
                        Triple("Favorites", Icons.Default.Favorite, "favorites"),
                        Triple("Profile", Icons.Default.Person, "profile")
                    )
                    navItems.forEach { (label, icon, route) ->
                        NavigationBarItem(
                            selected = label == "Bookings", // Yahan "Bookings" active rahega
                            onClick = {
                                if (label != "Bookings") {
                                    navController.navigate(route) {
                                        popUpTo("customer_home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(icon, null) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = electricBlue,
                                selectedTextColor = electricBlue,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // 1. Futuristic Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = electricBlue,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = electricBlue,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 14.sp, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal, color = if(selectedTab == index) Color.White else Color.Gray) }
                        )
                    }
                }

                // 2. Bookings List
                when (val state = myBookingsState) {
                    is Resource.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = electricBlue)
                        }
                    }
                    is Resource.Success -> {
                        val filtered = filterBookingsByTab(state.data, selectedTab)
                        if (filtered.isEmpty()) {
                            EmptyBookingsGlass(tabs[selectedTab], electricBlue, outlineWhite) { navController.popBackStack() }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filtered) { booking ->
                                    BookingCardGlass(booking, electricBlue, outlineWhite) {
                                        navController.navigate("booking_details/${booking.bookingId}")
                                    }
                                }
                            }
                        }
                    }
                    is Resource.Error -> {
                        ErrorCardGlass(state.message, electricBlue, outlineWhite) {
                            currentUser?.userId?.let { viewModel.getMyBookings(it) }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun BookingCardGlass(booking: Booking, blue: Color, outline: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.shopName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(booking.serviceName, color = Color.Gray, fontSize = 13.sp)
                }
                BookingStatusChipLiquid(booking.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = blue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatBookingDate(booking.bookingDate), color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.Schedule, null, tint = blue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(formatTime1(booking.bookingTime), color = Color.White, fontSize = 12.sp)
                }
                Text("₹${booking.servicePrice}", color = blue, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }

            if (booking.serviceLocation == ServiceLocation.CUSTOMER_HOME) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(booking.serviceAddress.take(35) + "...", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun BookingStatusChipLiquid(status: BookingStatus) {
    val color = when (status) {
        BookingStatus.PENDING -> Color(0xFFFFC107)
        BookingStatus.CONFIRMED -> Color(0xFF2196F3)
        BookingStatus.COMPLETED -> Color(0xFF4CAF50)
        else -> Color(0xFFE53935)
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = CircleShape,
        modifier = Modifier.border(0.5.dp, color.copy(alpha = 0.5f), CircleShape)
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun EmptyBookingsGlass(tab: String, blue: Color, outline: Color, onBrowse: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.BookmarkBorder, null, modifier = Modifier.size(80.dp), tint = Color.Gray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No $tab Bookings", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Your scheduled services will appear here.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBrowse, colors = ButtonDefaults.buttonColors(containerColor = blue), shape = RoundedCornerShape(12.dp)) {
            Text("Find a Service")
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyBookingsLiquidPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(16.dp)) {
                BookingCardGlass(
                    Booking(shopName = "Cyber Mechanic", serviceName = "Engine Tuning", servicePrice = 1500.0, bookingDate = "2026-03-20", bookingTime = "10:30", status = BookingStatus.CONFIRMED),
                    Color(0xFF2979FF),
                    Color.White.copy(0.1f)
                ) {}
            }
        }
    }
}

// Helper functions (same as your logic)
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
        outputFormat.format(inputFormat.parse(dateStr)!!)
    } catch (e: Exception) { dateStr }
}

private fun formatTime1(timeStr: String): String {
    return try {
        val parts = timeStr.split(":")
        var hour = parts[0].toInt()
        val amPm = if (hour >= 12) "PM" else "AM"
        if (hour > 12) hour -= 12
        if (hour == 0) hour = 12
        String.format("%d:%s %s", hour, parts[1], amPm)
    } catch (e: Exception) { timeStr }
}