package com.saurabh.onecornersystem.presentation.customer


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.navigation.Screen
import com.saurabh.onecornersystem.utils.Resource



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreenCustomer(
    serviceId: String,
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel()
) {
    val serviceState by viewModel.serviceItemDetailsState.collectAsState()

    // Screen load hote hi data fetch karo
    LaunchedEffect(serviceId) {
        viewModel.getServiceItemDetails(serviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // "Book Now" Button at the bottom
            if (serviceState is Resource.Success) {
                val service = (serviceState as Resource.Success<ShopItem>).data
                Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 10.dp) {
                    Button(
                        onClick = { navController.navigate(Screen.BookingForm.passServiceId(service.itemId)) },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = service.isAvailable
                    ) {
                        Text(if (service.isAvailable) "Book Now - ₹${service.price}" else "Currently Unavailable")
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
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {
                    // Service Banner
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = service.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                        Text(text = service.category, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Price & Duration
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "₹${service.price}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(text = "⏱️ ${service.duration}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 20.dp),
                            thickness = 0.5.dp,
                            color = DividerDefaults.color
                        )

                        // Description
                        Text(text = "Description", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = service.description.ifBlank { "No detailed description provided." }, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Tags (Home Service / Appointment)
                        if (service.homeService) DetailFeatureChip(Icons.Default.Home, "Home Service Available")
                        if (service.requiresAppointment) DetailFeatureChip(Icons.Default.CalendarToday, "Appointment Required")
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
fun DetailFeatureChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}