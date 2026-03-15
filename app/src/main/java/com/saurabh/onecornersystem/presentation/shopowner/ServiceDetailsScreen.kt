package com.saurabh.onecornersystem.presentation.shopowner

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    serviceId: String,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    val itemState by viewModel.currentItemState.collectAsState()

    // --- LIQUID THEME COLORS ---
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    LaunchedEffect(serviceId) {
        viewModel.getItemById(serviceId)
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BACKGROUND BLOBS ---
        Box(modifier = Modifier.size(300.dp).offset(x = 180.dp, y = (-50).dp).blur(100.dp).background(amberOrange.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp).blur(90.dp).background(amberOrange.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Service Details", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            (itemState as? Resource.Success)?.data?.let {
                                navController.navigate("edit_service/${it.itemId}")
                            }
                        }) {
                            Icon(Icons.Default.Edit, "Edit", tint = amberOrange)
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = itemState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = amberOrange)
                    }
                }
                is Resource.Success -> {
                    val service = state.data
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                    ) {
                        // 1. Service Banner Glass
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(240.dp).border(1.dp, outlineWhite, RoundedCornerShape(28.dp)),
                            color = glassWhite,
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            if (service.images.isNotEmpty()) {
                                Base64Image(
                                    imageSource = service.images[0],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Build, null, modifier = Modifier.size(64.dp), tint = amberOrange.copy(alpha = 0.4f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 2. Title & Pricing Section
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(service.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text(service.category, color = amberOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Column {
                                    Text("Service Price", color = Color.Gray, fontSize = 12.sp)
                                    Text("₹${service.price}", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                }
                                if (service.duration.isNotBlank()) {
                                    Surface(color = amberOrange.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.border(0.5.dp, amberOrange.copy(alpha = 0.3f), CircleShape)) {
                                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, null, tint = amberOrange, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text(service.duration, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // 3. Stats Dashboard (Glass Cards)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailStatCard("Total Bookings", service.totalBookings.toString(), Icons.Default.History, Modifier.weight(1f), amberOrange, outlineWhite)
                            DetailStatCard("Service Rating", String.format("%.1f", service.rating), Icons.Default.Star, Modifier.weight(1f), amberOrange, outlineWhite)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 4. Description (Glassy)
                        Text("About this Service", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outlineWhite, Color.Transparent)), RoundedCornerShape(20.dp)),
                            color = glassWhite,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = service.description.ifEmpty { "Professional service tailored for your specific needs." },
                                modifier = Modifier.padding(16.dp),
                                color = Color.Gray,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 5. Configuration List
                        Text("Service Config", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        ConfigRow("Home Delivery/Service", service.homeService, amberOrange)
                        ConfigRow("Advance Appointment", service.requiresAppointment, amberOrange)

                        Spacer(modifier = Modifier.height(40.dp))

                        // 6. Back Button
                        Button(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = amberOrange.copy(alpha = 0.15f), contentColor = amberOrange)
                        ) {
                            Text("GO BACK TO LIST", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                is Resource.Error -> {
                    ErrorCardGlass2(msg = state.message, accent = amberOrange, outline = outlineWhite) {
                        viewModel.getItemById(serviceId)
                    }
                }
                else -> {}
            }
        }
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun DetailStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, accent: Color, outline: Color) {
    Surface(
        modifier = modifier.border(1.dp, outline, RoundedCornerShape(20.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConfigRow(title: String, active: Boolean, accent: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (active) Icons.Default.CheckCircle else Icons.Default.Cancel,
            null,
            tint = if (active) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = if(active) Color.White else Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun ErrorCardGlass2(msg: String?, accent: Color, outline: Color, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.padding(20.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = msg ?: "Connection lost", color = Color.White, textAlign = TextAlign.Center)
            TextButton(onClick = onRetry) { Text("TRY AGAIN", color = accent, fontWeight = FontWeight.Bold) }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ServiceDetailsAmberPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Amber Detail View", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                DetailStatCard("Preview Stat", "124", Icons.Default.Star, Modifier.fillMaxWidth(), Color(0xFFFF9100), Color.White.copy(0.1f))
            }
        }
    }
}