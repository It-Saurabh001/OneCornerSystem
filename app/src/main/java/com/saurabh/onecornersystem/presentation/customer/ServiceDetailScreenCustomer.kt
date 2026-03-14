package com.saurabh.onecornersystem.presentation.customer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreenCustomer(
    serviceId: String,
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel()
) {
    val serviceState by viewModel.serviceItemDetailsState.collectAsState()

    // 1. Initial Effect: Screen load hote hi details fetch karo
    LaunchedEffect(serviceId) {
        viewModel.getServiceItemDetails(serviceId)
    }

    // 2. MANUAL RESET: Screen se bahar nikalte hi data clear karo
    // Isse 'Ghosting' issue (purana data dikhna) khatam ho jata hai
    DisposableEffect(Unit) {
        onDispose {
            Log.d("CustomerDetails", "Leaving screen, resetting details state...")
            viewModel.resetShopDetails()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Success hone par hi "Book Now" dikhao
            if (serviceState is Resource.Success) {
                val service = (serviceState as Resource.Success<ShopItem>).data
                Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 12.dp) {
                    Button(
                        onClick = {
                            // Booking form par le jao
                            navController.navigate("booking_form/${service.itemId}")
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = service.available
                    ) {
                        Text(
                            text = if (service.available) "Book Now • ₹${service.price}" else "Currently Unavailable",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when (val state = serviceState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val service = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Service Banner
                    if (service.images.isNotEmpty()) {
                        Base64Image(
                            imageSource = service.images[0],
                            contentDescription = "Service Image",
                            modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(250.dp).background(MaterialTheme.colorScheme.primaryContainer).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Build, "Service", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(service.name, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text(service.category, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Price & Stats Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ServiceStatItem(Icons.Default.Schedule, service.duration)
                            ServiceStatItem(Icons.Default.Star, "4.8 Rating") // Hardcoded for now
                            ServiceStatItem(Icons.Default.History, "${service.totalBookings} Bookings")
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 24.dp),
                            thickness = 0.5.dp,
                            color = DividerDefaults.color
                        )

                        // Description Section
                        Text("About this service", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = service.description.ifBlank { "Professional service provided by experts at your convenience." },
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Features Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Why choose us?", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                FeatureTick(service.homeService, "Home Service Available")
                                FeatureTick(service.requiresAppointment, "Prior Appointment Recommended")
                                FeatureTick(true, "Expert Professional Assistance")
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom button
                    }
                }
            }
            is Resource.Error -> {
                ErrorCard(message = state.message) { viewModel.getServiceItemDetails(serviceId) }
            }
            else -> {}
        }
    }
}

@Composable
fun ServiceStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FeatureTick(active: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(
            if (active) Icons.Default.CheckCircle else Icons.Default.Cancel,
            null,
            tint = if (active) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp)
    }
}