package com.saurabh.onecornersystem.presentation.customer

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.ServiceLocation
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.TimeSlot
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "BookingFormScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    serviceId: String,
    navController: NavController,
    shopItemViewModel: ShopItemViewModel = hiltViewModel(),
    customerViewModel: CustomerShopViewModel = hiltViewModel()
) {
    Log.d(TAG, "Displayed - serviceId: $serviceId")

    val itemState by shopItemViewModel.itemState.collectAsState()
    val timeSlotsState by customerViewModel.availableTimeSlotsState.collectAsState()
    val createBookingState by customerViewModel.createBookingState.collectAsState()

    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(ServiceLocation.SHOP_LOCATION) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    val availableDates = remember { generateNext7Days() }

    LaunchedEffect(serviceId) {
        Log.d(TAG, "Fetching service item: $serviceId")
        shopItemViewModel.getItemById(serviceId)
    }

    LaunchedEffect(selectedDate) {
        if (selectedDate.isNotEmpty()) {
            val service = (itemState as? Resource.Success)?.data
            service?.let {
                Log.d(TAG, "Fetching time slots for date: $selectedDate, shopId: ${it.shopId}")
                customerViewModel.getAvailableTimeSlots(it.shopId, selectedDate)
            }
        }
    }

    // Log state changes
    LaunchedEffect(itemState) {
        when (itemState) {
            is Resource.Success -> Log.d(TAG, "Service loaded: ${(itemState as Resource.Success).data.name}")
            is Resource.Error -> Log.e(TAG, "Service load error: ${(itemState as Resource.Error).message}")
            is Resource.Loading -> Log.d(TAG, "Loading service...")
            else -> {}
        }
    }

    LaunchedEffect(createBookingState) {
        when (createBookingState) {
            is Resource.Success -> {
                Log.d(TAG, "Booking created successfully")
                navController.navigate("my_bookings") {
                    popUpTo("booking_form/$serviceId") { inclusive = true }
                }
            }
            is Resource.Error -> Log.e(TAG, "Booking creation failed: ${(createBookingState as Resource.Error).message}")
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Service") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = itemState) {
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
                        Text("Loading service details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is Resource.Error -> {
                // Service load failed
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
                            text = "Failed to Load Service",
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
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { navController.popBackStack() }) {
                                Text("Go Back")
                            }
                            Button(onClick = { shopItemViewModel.getItemById(serviceId) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            is Resource.Success -> {
                val service = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Service Info Card
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
                                text = service.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = service.category,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Price: ₹${service.price}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Duration: ${service.duration}",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date Selection
                    Text(
                        text = "Select Date",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableDates) { date ->
                            val isSelected = date == selectedDate
                            val displayDate = displayDateFormatter.format(
                                dateFormatter.parse(date)!!
                            )

                            Card(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(80.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                onClick = { selectedDate = date }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = displayDate.split(" ")[0],
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = displayDate.split(" ")[1],
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Slots
                    if (selectedDate.isNotEmpty()) {
                        Text(
                            text = "Select Time",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        when (val timeState = timeSlotsState) {
                            is Resource.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is Resource.Success -> {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(timeState.data) { slot ->
                                        val isSelected = selectedTime == slot.startTime
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (slot.isAvailable) {
                                                    selectedTime = slot.startTime
                                                }
                                            },
                                            label = {
                                                Text(
                                                    formatTime(slot.startTime),
                                                    fontSize = 12.sp
                                                )
                                            },
                                            enabled = slot.isAvailable
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Text(
                                    text = timeState.message ?: "Error loading slots",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Location Selection
                    Text(
                        text = "Service Location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedLocation == ServiceLocation.SHOP_LOCATION,
                            onClick = { selectedLocation = ServiceLocation.SHOP_LOCATION },
                            label = { Text("At Shop") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        FilterChip(
                            selected = selectedLocation == ServiceLocation.CUSTOMER_HOME,
                            onClick = { selectedLocation = ServiceLocation.CUSTOMER_HOME },
                            label = { Text("At Home") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    if (selectedLocation == ServiceLocation.CUSTOMER_HOME) {
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City *") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Confirm Button
                    Button(
                        onClick = {
                            // Create booking
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedDate.isNotEmpty() &&
                                selectedTime.isNotEmpty() &&
                                (selectedLocation != ServiceLocation.CUSTOMER_HOME ||
                                        (address.isNotBlank() && city.isNotBlank() && pincode.isNotBlank()))
                    ) {
                        Text(
                            text = "Confirm Booking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            is Resource.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message ?: "Error loading service",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {}
        }
    }
}

private fun generateNext7Days(): List<String> {
    val dates = mutableListOf<String>()
    val calendar = Calendar.getInstance()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    repeat(7) {
        dates.add(formatter.format(calendar.time))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return dates
}

private fun formatTime(time24: String): String {
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