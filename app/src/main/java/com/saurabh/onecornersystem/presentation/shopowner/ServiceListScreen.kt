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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()

    // --- THEME COLORS ---
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    LaunchedEffect(shopId) {
        if (shopId.isNotEmpty()) {
            viewModel.getServicesByShop(shopId)
        }
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
                    title = { Text("My Services", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("add_service/$shopId") }) {
                            Icon(Icons.Default.Add, "Add", tint = amberOrange)
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = servicesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = amberOrange)
                    }
                }
                is Resource.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyServiceViewLiquid(shopId, navController, amberOrange)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.data) { service ->
                                ServiceCardLiquid(
                                    service = service,
                                    accent = amberOrange,
                                    outline = outlineWhite,
                                    bg = glassWhite,
                                    onEditClick = { navController.navigate("edit_service/${service.itemId}") },
                                    onViewDetails = { navController.navigate("service_details/${service.itemId}") },
                                    onToggleAvailability = { viewModel.toggleItemAvailability(service.itemId, !service.available) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorStateLiquid(state.message ?: "Unknown Error", amberOrange) { viewModel.getServicesByShop(shopId) }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ServiceCardLiquid(
    service: ShopItem,
    accent: Color,
    outline: Color,
    bg: Color,
    onEditClick: () -> Unit,
    onViewDetails: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = if (service.available) bg else bg.copy(alpha = 0.02f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(50.dp).background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, null, tint = accent, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                    Text(service.category, color = Color.Gray, fontSize = 12.sp)
                }
                IconButton(onClick = onToggleAvailability) {
                    Icon(
                        if (service.available) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = if (service.available) accent else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CurrencyRupee, null, tint = accent, modifier = Modifier.size(16.dp))
                    Text("${service.price}", color = accent, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
                if (service.duration.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(service.duration, color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
            if (service.homeService || service.requiresAppointment) {
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (service.homeService) ServiceTagLiquid("🏠 Home Service", accent)
                    if (service.requiresAppointment) ServiceTagLiquid("📅 Appointment", accent)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onViewDetails, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, outline)) {
                    Text("DETAILS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onEditClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                    Text("EDIT", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ServiceTagLiquid(label: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.05f), shape = CircleShape, modifier = Modifier.border(0.5.dp, accent.copy(alpha = 0.2f), CircleShape)) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyServiceViewLiquid(shopId: String, navController: NavController, accent: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Build, null, modifier = Modifier.size(80.dp), tint = Color.DarkGray.copy(alpha = 0.5f))
        Spacer(Modifier.height(16.dp))
        Text("No Services Yet", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Add your first service to go live", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))
        Button(onClick = { navController.navigate("add_service/$shopId") }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = accent)) {
            Icon(Icons.Default.Add, null, tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("ADD NEW SERVICE", color = Color.Black, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun ErrorStateLiquid(msg: String, accent: Color, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(msg, color = Color.Red, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text("RETRY", color = accent) }
    }
}

// ======================== PREVIEWS ========================

@Preview(name = "Amber Service Card", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun PreviewServiceCard() {
    val mockService = ShopItem(
        itemId = "1",
        name = "Full Bike Service",
        category = "Maintenance",
        price = 499.0,
        duration = "2 Hours",
        available = true,
        homeService = true,
        requiresAppointment = true
    )
    Box(modifier = Modifier.padding(16.dp)) {
        ServiceCardLiquid(
            service = mockService,
            accent = Color(0xFFFF9100),
            outline = Color.White.copy(alpha = 0.1f),
            bg = Color.White.copy(alpha = 0.05f),
            onEditClick = {},
            onViewDetails = {},
            onToggleAvailability = {}
        )
    }
}

@Preview(name = "Amber Service List Screen")
@Composable
fun PreviewServiceListScreen() {
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // Blobs
        Box(modifier = Modifier.size(200.dp).offset(x = 100.dp, y = (-50).dp).blur(80.dp).background(amberOrange.copy(alpha = 0.15f), CircleShape))

        Column {
            // Mock Top Bar
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                Spacer(Modifier.width(16.dp))
                Text("My Services", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Add, null, tint = amberOrange)
            }

            // Mock List
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(3) { index ->
                    ServiceCardLiquid(
                        service = ShopItem(
                            name = if(index == 0) "Oil Change" else "Brake Repair",
                            category = "Engine",
                            price = 250.0,
                            duration = "45 Mins",
                            available = index != 2
                        ),
                        accent = amberOrange,
                        outline = Color.White.copy(alpha = 0.1f),
                        bg = Color.White.copy(alpha = 0.05f),
                        onEditClick = {},
                        onViewDetails = {},
                        onToggleAvailability = {}
                    )
                }
            }
        }
    }
}