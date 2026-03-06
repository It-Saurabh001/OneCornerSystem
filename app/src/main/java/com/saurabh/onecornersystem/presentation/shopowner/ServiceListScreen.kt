package com.saurabh.onecornersystem.presentation.shopowner

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    Log.d("ServiceListScreen", "Displayed - shopId: $shopId")
    val servicesState by viewModel.servicesState.collectAsState()

    LaunchedEffect(shopId) {
        Log.d("ServiceListScreen", "LaunchedEffect triggered for shopId: $shopId")
        if (shopId.isNotEmpty()) {
            Log.d("ServiceListScreen", "Fetching services for shopId: $shopId")
            viewModel.getServicesByShop(shopId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Services") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("add_service/$shopId")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Service")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = servicesState) {
            is Resource.Loading -> {
                Log.d("ServiceListScreen", "Loading services for shopId: $shopId")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                Log.d("ServiceListScreen", "Services loaded successfully - count: ${state.data.size}")
                if (state.data.isEmpty()) {
                    Log.d("ServiceListScreen", "No services found for shopId: $shopId")
                    EmptyServiceView(shopId, navController)
                } else {
                    Log.d("ServiceListScreen", "Displaying ${state.data.size} services")
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.data) { service ->
                            Log.d("ServiceListScreen", "Rendering service - id: ${service.itemId}, name: ${service.name}")
                            ServiceCard(
                                service = service,
                                onEditClick = {
                                    Log.d("ServiceListScreen", "Edit clicked for service: ${service.itemId}")
                                    navController.navigate("edit_service/${service.itemId}")
                                },
                                onViewDetails = {
                                    Log.d("ServiceListScreen", "View details clicked for service: ${service.itemId}")
                                    navController.navigate("service_details/${service.itemId}")
                                },
                                onToggleAvailability = {
                                    Log.d("ServiceListScreen", "Availability toggled for service: ${service.itemId}, new value: ${!service.isAvailable}")
                                    viewModel.toggleItemAvailability(service.itemId, !service.isAvailable)
                                }
                            )
                        }
                    }
                }
            }
            is Resource.Error -> {
                Log.d("ServiceListScreen", "Error loading services - ${state.message}")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            Log.d("ServiceListScreen", "Retry clicked for shopId: $shopId")
                            viewModel.getServicesByShop(shopId)
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                Log.d("ServiceListScreen", "Unknown state: ${state.javaClass.simpleName}")
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: ShopItem,
    onEditClick: () -> Unit,
    onViewDetails: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Log.d("ServiceCard", "Rendered - id: ${service.itemId}, name: ${service.name}, available: ${service.isAvailable}")
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (service.isAvailable)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Icon and Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Service Title and Category
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = service.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = service.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Availability Toggle
                IconButton(onClick = onToggleAvailability) {
                    Icon(
                        if (service.isAvailable) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = if (service.isAvailable) "Available" else "Unavailable",
                        tint = if (service.isAvailable) Color.Green else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price and Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${service.price}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Duration
                if (service.duration.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = service.duration,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Service Features
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (service.homeService) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("🏠 Home Service", fontSize = 12.sp)
                        },
                        enabled = false
                    )
                }

                if (service.requiresAppointment) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("📅 Appointment Required", fontSize = 12.sp)
                        },
                        enabled = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details")
                }

                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
fun EmptyServiceView(
    shopId: String,
    navController: NavController
) {
    Log.d("EmptyServiceView", "Displayed for shopId: $shopId - No services available")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Services Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add your first service to get started",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("add_service/$shopId") }
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Service")
        }
    }
}