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
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    val servicesState by viewModel.servicesState.collectAsState()

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
        // --- LIQUID BACKGROUND BLOBS ---
        Box(modifier = Modifier.size(350.dp).offset(x = 200.dp, y = (-100).dp).blur(130.dp).background(amberOrange.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(300.dp).align(Alignment.BottomStart).offset(x = (-80).dp, y = 100.dp).blur(110.dp).background(amberOrange.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("My Inventory", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("add_service/$shopId") }) {
                            Icon(Icons.Default.Add, "Add", tint = amberOrange, modifier = Modifier.size(28.dp))
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = servicesState) {
                is Resource.Loading -> FullLoadingScreenLiquid(amberOrange)

                is Resource.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyServiceViewLiquid(shopId, navController, amberOrange)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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

                is Resource.Error -> ErrorCardGlass5(state.message, amberOrange, outlineWhite) {
                    viewModel.getServicesByShop(shopId)
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
        onClick = onViewDetails,
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = if (service.available) bg.copy(alpha = 0.10f) else bg.copy(alpha = 0.12f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Box
                Box(modifier = Modifier.size(50.dp).background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).border(0.5.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, null, tint = accent, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(service.category, color = Color.Gray, fontSize = 12.sp)
                }

                // Visibility Toggle (Neon Styled)
                IconButton(onClick = onToggleAvailability) {
                    Icon(
                        imageVector = if (service.available) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = if (service.available) accent else Color.DarkGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Price
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹", color = accent, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(service.price.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }

                // Action Buttons
                Row(modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onViewDetails, modifier = Modifier.size(36.dp).background(accent.copy(alpha = 0.1f), CircleShape)) {
                        Icon(Icons.Default.ChevronRight, null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (service.homeService || service.requiresAppointment) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (service.homeService) MiniTagLiquid("🏠 Home", accent)
                    if (service.requiresAppointment) MiniTagLiquid("📅 Booking", accent)
                }
            }
        }
    }
}

@Composable
fun MiniTagLiquid(text: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(text, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun EmptyServiceViewLiquid(shopId: String, navController: NavController, accent: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(80.dp), tint = Color.DarkGray)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Your Shop is Empty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Start adding services to get bookings", color = Color.Gray, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("add_service/$shopId") },
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("ADD FIRST SERVICE", fontWeight = FontWeight.Black)
        }
    }
}


@Composable
fun ErrorCardGlass5(msg: String?, accent: Color, outline: Color, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.padding(24.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = msg ?: "Something went wrong", color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                Text("RETRY", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true)
@Composable
fun ServiceListAmberPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Amber Inventory", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                ServiceCardLiquid(
                    service = ShopItem(name = "Deep Cleaning", category = "Home Service", price = 499.0, available = true),
                    accent = Color(0xFFFF9100), outline = Color.White.copy(0.1f), bg = Color.White.copy(0.05f),
                    onEditClick = {}, onViewDetails = {}, onToggleAvailability = {}
                )
            }
        }
    }
}