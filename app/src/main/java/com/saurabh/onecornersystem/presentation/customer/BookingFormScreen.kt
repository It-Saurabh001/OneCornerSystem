package com.saurabh.onecornersystem.presentation.customer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.ServiceLocation
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.TimeSlot
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "BookingFormScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BookingFormScreen(
    serviceId: String,
    navController: NavController,
    shopItemViewModel: ShopItemViewModel = hiltViewModel(),
    customerViewModel: CustomerShopViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val itemState by shopItemViewModel.itemState.collectAsState()
    val timeSlotsState by customerViewModel.availableTimeSlotsState.collectAsState()
    val createBookingState by customerViewModel.createBookingState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val shopDetailsState by customerViewModel.shopDetailsState.collectAsState()

    // Form States
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(ServiceLocation.SHOP_LOCATION) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Liquid Theme Colors
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.15f)

    val availableDates = remember { generateNext7Days() }
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Side Effects
    LaunchedEffect(serviceId) { shopItemViewModel.getItemById(serviceId) }

    LaunchedEffect(itemState) {
        if (itemState is Resource.Success) {
            customerViewModel.getShopDetails((itemState as Resource.Success).data.shopId)
        }
    }

    LaunchedEffect(selectedDate) {
        if (selectedDate.isNotEmpty()) {
            (itemState as? Resource.Success)?.data?.let {
                customerViewModel.getAvailableTimeSlots(it.shopId, selectedDate)
            }
        }
    }

    LaunchedEffect(createBookingState) {
        when (val state = createBookingState) {
            is Resource.Success -> {
                Log.d(TAG, "✅ Booking created successfully — navigating to my_bookings")
                navController.navigate("my_bookings") { popUpTo("booking_form/$serviceId") { inclusive = true } }
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Booking creation failed: ${state.message}")
                // Error banner shown in UI below
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(350.dp).offset(x = 200.dp, y = (-50).dp).blur(100.dp).background(electricBlue.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp).blur(90.dp).background(electricBlue.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Booking Details", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = itemState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = electricBlue)
                    }
                }
                is Resource.Success -> {
                    val service = state.data
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(20.dp)
                    ) {
                        // 1. Service Summary (Glass)
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, outlineWhite, RoundedCornerShape(20.dp)),
                            color = glassWhite,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(48.dp).background(electricBlue.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Build, null, tint = electricBlue, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(service.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Price: ₹${service.price} • ${service.duration}", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Date Selection
                        Text("Select Date", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(availableDates) { date ->
                                val isSel = date == selectedDate
                                val parsed = dateFormatter.parse(date) ?: Date()
                                val dayName = SimpleDateFormat("EEE", Locale.US).format(parsed)
                                val dayNum = SimpleDateFormat("dd", Locale.US).format(parsed)

                                Surface(
                                    onClick = { selectedDate = date },
                                    modifier = Modifier.size(width = 65.dp, height = 85.dp).border(1.dp, if (isSel) electricBlue else outlineWhite, RoundedCornerShape(16.dp)),
                                    color = if (isSel) electricBlue.copy(alpha = 0.2f) else glassWhite,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text(dayName, color = if (isSel) Color.White else Color.Gray, fontSize = 12.sp)
                                        Text(dayNum, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. Time Slots
                        if (selectedDate.isNotEmpty()) {
                            Text("Available Slots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            when (val timeState = timeSlotsState) {
                                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = electricBlue)
                                is Resource.Success -> {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        timeState.data.forEach { slot ->
                                            val isSel = selectedTime == slot.startTime
                                            Surface(
                                                onClick = { if (slot.isAvailable) selectedTime = slot.startTime },
                                                modifier = Modifier.height(40.dp).border(1.dp, if (isSel) electricBlue else outlineWhite.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                                color = if (isSel) electricBlue else if (slot.isAvailable) glassWhite else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp),
                                                enabled = slot.isAvailable
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                                    Text(formatTime(slot.startTime), color = if (slot.isAvailable) Color.White else Color.DarkGray, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                                is Resource.Error -> Text("No slots found for this date", color = Color.Gray, fontSize = 13.sp)
                                else -> {}
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 4. Location
                        Text("Service Location", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LocationTypeCard("At Shop", Icons.Default.Store, selectedLocation == ServiceLocation.SHOP_LOCATION, electricBlue, outlineWhite, Modifier.weight(1f)) {
                                selectedLocation = ServiceLocation.SHOP_LOCATION
                            }
                            LocationTypeCard("At Home", Icons.Default.Home, selectedLocation == ServiceLocation.CUSTOMER_HOME, electricBlue, outlineWhite, Modifier.weight(1f)) {
                                selectedLocation = ServiceLocation.CUSTOMER_HOME
                            }
                        }

                        if (selectedLocation == ServiceLocation.CUSTOMER_HOME) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LiquidInputField(address, { address = it }, "Detailed Address *", Icons.Default.Map, electricBlue)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.weight(1f)) { LiquidInputField(city, { city = it }, "City *", Icons.Default.LocationCity, electricBlue) }
                                Box(modifier = Modifier.weight(1f)) { LiquidInputField(pincode, { pincode = it }, "Pincode *", Icons.Default.Pin, electricBlue, KeyboardType.Number) }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        LiquidInputField(notes, { notes = it }, "Any special instructions?", Icons.Default.EditNote, electricBlue)

                        Spacer(modifier = Modifier.height(40.dp))

                        // Confirm Button
                        val isBookingInProgress = createBookingState is Resource.Loading
                        val shop = (shopDetailsState as? Resource.Success)?.data

                        // Error banner
                        if (createBookingState is Resource.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFB71C1C).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "❌ ${(createBookingState as Resource.Error).message ?: "Booking failed. Please try again."}",
                                    color = Color(0xFFEF9A9A),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Shop not ready warning
                        if (shop == null && !isBookingInProgress) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFE65100).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "⏳ Loading shop details...",
                                    color = Color(0xFFFFCC80),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                Log.d(TAG, "🔘 CONFIRM BOOKING clicked")
                                Log.d(TAG, "👤 currentUser=${currentUser?.userId}, shop=${shop?.shopId}")
                                Log.d(TAG, "📅 date=$selectedDate, time=$selectedTime, location=$selectedLocation")

                                val user = currentUser
                                if (user == null) {
                                    Log.e(TAG, "❌ Cannot book — currentUser is null (not logged in?)")
                                    return@Button
                                }
                                if (shop == null) {
                                    Log.e(TAG, "❌ Cannot book — shopDetails not loaded yet (shopDetailsState=${shopDetailsState::class.simpleName})")
                                    return@Button
                                }

                                Log.d(TAG, "🚀 Calling createBooking — shopId=${shop.shopId}, serviceId=${service.itemId}")
                                customerViewModel.createBooking(
                                    user.userId, user.name, user.phone, user.email,
                                    service.shopId, shop.shopName, shop.ownerId,
                                    service.itemId, service.name, service.price, service.duration,
                                    selectedDate, selectedTime, selectedLocation,
                                    address, city, pincode, notes
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = electricBlue),
                            enabled = !isBookingInProgress && shop != null && selectedDate.isNotEmpty() && selectedTime.isNotEmpty() &&
                                    (selectedLocation != ServiceLocation.CUSTOMER_HOME || (address.isNotBlank() && city.isNotBlank()))
                        ) {
                            if (isBookingInProgress) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("CONFIRM BOOKING", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                is Resource.Error -> {
                    ErrorCardGlass(msg = state.message, blue = electricBlue, outline = outlineWhite) {
                        shopItemViewModel.getItemById(serviceId)
                    }
                }
                else -> {}
            }
        }
    }
}

// --- TOP-LEVEL HELPER FUNCTIONS (Public) ---

fun generateNext7Days(): List<String> {
    val dates = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    repeat(7) {
        dates.add(formatter.format(calendar.time))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return dates
}

fun formatTime(time24: String): String {
    return try {
        val parts = time24.split(":")
        var hour = parts[0].toInt()
        val minute = parts[1]
        val amPm = if (hour >= 12) "PM" else "AM"
        if (hour > 12) hour -= 12
        if (hour == 0) hour = 12
        String.format("%d:%s %s", hour, minute, amPm)
    } catch (e: Exception) {
        time24
    }
}

@Composable
fun LocationTypeCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSel: Boolean, blue: Color, outline: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(54.dp).border(1.dp, if (isSel) blue else outline, RoundedCornerShape(12.dp)),
        color = if (isSel) blue.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSel) blue else Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = if (isSel) Color.White else Color.Gray, fontSize = 14.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun LiquidInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, blue: Color, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, null, tint = blue, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = type),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = blue, unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.02f), unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White
        )
    )
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BookingFormPreview() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Booking Form Preview", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                modifier = Modifier.fillMaxWidth().height(60.dp).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)),
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp)
            ) {}
        }
    }
}