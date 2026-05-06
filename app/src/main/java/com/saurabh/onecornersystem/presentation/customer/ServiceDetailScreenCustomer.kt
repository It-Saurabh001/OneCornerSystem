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
import com.saurabh.onecornersystem.presentation.common.ChatViewModel
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreenCustomer(
    serviceId: String,
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel(),
    chatViewModel: ChatViewModel = hiltViewModel()
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
                        // 👈 CHAT ICON IN TOP BAR
                        IconButton(onClick = {
                            val service = (serviceState as? Resource.Success)?.data
                            if (service != null) {
                                val encodedName = android.net.Uri.encode(service.name)
                                navController.navigate(
                                    "customer_chat?bookingId=&shopId=${service.shopId}&shopName=$encodedName&shopImage="
                                )
                            }
                        }) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = "Chat with Shop",
                                tint = electricBlue
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (serviceState is Resource.Success) {
                    val service = (serviceState as Resource.Success).data
                    Surface(
                        modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                        color = Color.Black.copy(alpha = 0.85f)
                    ) {
                        // 👈 BOTTOM BAR WITH CHAT + BOOK BUTTON
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // CHAT BUTTON
                            OutlinedButton(
                                onClick = {
                                    val encodedName = android.net.Uri.encode(service.name)
                                    navController.navigate(
                                        "customer_chat?bookingId=&shopId=${service.shopId}&shopName=$encodedName&shopImage="
                                    )
                                },
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, electricBlue.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = electricBlue
                                )
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    tint = electricBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "CHAT",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = electricBlue
                                )
                            }

                            // BOOK BUTTON
                            Button(
                                onClick = { navController.navigate("booking_form/${service.itemId}") },
                                modifier = Modifier
                                    .weight(0.6f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = electricBlue),
                                enabled = service.available
                            ) {
                                Text(
                                    text = if (service.available) "BOOK NOW • ₹${service.price}" else "Unavailable",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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
                        // 1. Service Banner
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
                            // 👈 SHOP NAME ROW WITH CHAT BUTTON
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(service.name, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    Text(service.category, color = electricBlue, fontWeight = FontWeight.SemiBold)
                                }

                                // 👈 QUICK CHAT ICON
                                IconButton(
                                    onClick = {
                                        val encodedName = android.net.Uri.encode(service.name)
                                        navController.navigate(
                                            "customer_chat?bookingId=&shopId=${service.shopId}&shopName=$encodedName&shopImage="
                                        )
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            electricBlue.copy(alpha = 0.1f),
                                            CircleShape
                                        )
                                        .border(1.dp, electricBlue.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Chat,
                                        contentDescription = "Chat",
                                        tint = electricBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Stats Grid
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

                            // 4. Features
                            Text("Service Highlights", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            LiquidFeatureTick(service.homeService, "Home Service Available", electricBlue)
                            LiquidFeatureTick(service.requiresAppointment, "Prior Appointment Needed", electricBlue)
                            LiquidFeatureTick(true, "Certified Professional", electricBlue)

                            // 👈 INLINE CHAT PROMPT
                            Spacer(modifier = Modifier.height(32.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = electricBlue.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .border(1.dp, electricBlue.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Chat,
                                        null,
                                        tint = electricBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Have questions?",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Chat with the shop about this service",
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                    TextButton(onClick = {
                                        val encodedName = android.net.Uri.encode(service.name)
                                        navController.navigate(
                                            "customer_chat?bookingId=&shopId=${service.shopId}&shopName=$encodedName&shopImage="
                                        )
                                    }) {
                                        Text("CHAT", color = electricBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(100.dp)) // Bottom bar ke liye space
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
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text("Preview: Service Details", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Premium Car Detailing", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}