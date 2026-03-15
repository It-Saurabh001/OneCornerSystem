package com.saurabh.onecornersystem.presentation.customer

import android.util.Log
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
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreenCustomer(
    serviceId: String,
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel()
) {
    val serviceState by viewModel.serviceItemDetailsState.collectAsState()

    // Colors Palette
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.15f)

    LaunchedEffect(serviceId) {
        viewModel.getServiceItemDetails(serviceId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetShopDetails() }
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BACKGROUND BLOBS ---
        Box(modifier = Modifier.size(400.dp).offset(x = (-150).dp, y = (-100).dp).blur(120.dp).background(electricBlue.copy(alpha = 0.2f), CircleShape))
        Box(modifier = Modifier.size(300.dp).align(Alignment.BottomEnd).offset(x = 100.dp, y = 100.dp).blur(90.dp).background(electricBlue.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Service Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Share, "Share", tint = electricBlue)
                        }
                    }
                )
            },
            bottomBar = {
                if (serviceState is Resource.Success) {
                    val service = (serviceState as Resource.Success).data
                    Surface(
                        modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                        color = Color.Black.copy(alpha = 0.7f) // Glassy bottom bar
                    ) {
                        Button(
                            onClick = { navController.navigate("booking_form/${service.itemId}") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(60.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = electricBlue),
                            enabled = service.available
                        ) {
                            Text(
                                text = if (service.available) "Confirm Booking • ₹${service.price}" else "Currently Unavailable",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            when (val state = serviceState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = electricBlue)
                    }
                }
                is Resource.Success -> {
                    val service = state.data
                    Column(
                        modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
                    ) {
                        // 1. Service Banner (Liquid Style)
                        Box(modifier = Modifier.padding(16.dp).fillMaxWidth().height(260.dp)) {
                            if (service.images.isNotEmpty()) {
                                Base64Image(
                                    imageSource = service.images[0],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)).border(1.dp, outlineWhite, RoundedCornerShape(24.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxSize().border(1.dp, outlineWhite, RoundedCornerShape(24.dp)),
                                    color = glassWhite,
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Build, null, modifier = Modifier.size(80.dp), tint = electricBlue.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(service.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text(service.category, color = electricBlue, fontWeight = FontWeight.SemiBold)

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Stats Grid (Glassmorphism)
                            Surface(
                                modifier = Modifier.fillMaxWidth().border(1.dp, outlineWhite, RoundedCornerShape(20.dp)),
                                color = glassWhite,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    LiquidStatItem(Icons.Default.Schedule, service.duration, electricBlue)
                                    LiquidStatItem(Icons.Default.Star, "4.8", electricBlue)
                                    LiquidStatItem(Icons.Default.VerifiedUser, "Expert", electricBlue)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // 3. Description Section
                            Text("About Service", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = service.description.ifBlank { "Professional service provided by experts at your flow and convenience." },
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // 4. Features (Glass style)
                            Text("Service Highlights", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            LiquidFeatureTick(service.homeService, "Home Service Available", electricBlue)
                            LiquidFeatureTick(service.requiresAppointment, "Prior Appointment Needed", electricBlue)
                            LiquidFeatureTick(true, "Certified Professional", electricBlue)

                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    ErrorCardGlass(msg = state.message, blue = electricBlue, outline = outlineWhite) {
                        viewModel.getServiceItemDetails(serviceId)
                    }
                }
                else -> {}
            }
        }
    }
}

// --- SUB COMPONENTS ---

@Composable
fun LiquidStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, blue: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = blue)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun LiquidFeatureTick(active: Boolean, text: String, blue: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Icon(
            if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            null,
            tint = if (active) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = if (active) Color.White else Color.Gray)
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LiquidServiceDetailPreview() {
    val mockService = ShopItem(
        name = "Premium Car Detailing",
        category = "Automotive",
        price = 1299.0,
        duration = "2 hrs",
        description = "Full exterior and interior deep cleaning with wax polish and ceramic coating finish.",
        homeService = true,
        available = true
    )
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            // Simulated Success State
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text("Preview: Service Details", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                // Reuse existing components for preview
                Text(mockService.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}