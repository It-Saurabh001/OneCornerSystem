package com.saurabh.onecornersystem.presentation.shopowner

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.presentation.components.Base64Image

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailsScreen(
    shop: Shop,
    navController: NavController
) {
    // --- THEME COLORS ---
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(350.dp).offset(x = 150.dp, y = (-50).dp).blur(120.dp).background(amberOrange.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(300.dp).align(Alignment.CenterStart).offset(x = (-80).dp, y = 150.dp).blur(100.dp).background(amberOrange.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Business Profile", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
//                    actions = {
//                        IconButton(onClick = { navController.navigate("edit_shop/${shop.shopId}") }) {
//                            Icon(Icons.Default.Edit, "Edit", tint = amberOrange)
//                        }
//                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
            ) {
                // 1. BRANDING HEADER (Cover & Logo)
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    // Cover Image (16:9 feel)
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
                        color = glassWhite
                    ) {
                        if (shop.coverImage.isNotEmpty()) {
                            Base64Image(shop.coverImage, null, Modifier.fillMaxSize(), ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(amberOrange.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Store, null, Modifier.size(64.dp), tint = amberOrange.copy(alpha = 0.2f))
                            }
                        }
                    }

                    // Logo Overlay (1:1 Square with Glow)
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).size(100.dp).border(3.dp, amberOrange, RoundedCornerShape(24.dp)).padding(4.dp),
                        color = Color.Black,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        if (shop.logo.isNotEmpty()) {
                            Base64Image(shop.logo, null, Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), ContentScale.Crop)
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(shop.shopName.take(1).uppercase(), color = amberOrange, fontSize = 40.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // 2. SHOP TITLE & STATUS
                    Text(shop.shopName.uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(shop.category, color = amberOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = (if (shop.open) Color(0xFF4CAF50) else Color(0xFFE53935)).copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.border(0.5.dp, (if (shop.open) Color(0xFF4CAF50) else Color(0xFFE53935)).copy(alpha = 0.5f), CircleShape)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(if (shop.open) Color(0xFF4CAF50) else Color(0xFFE53935), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(if (shop.open) "OPEN" else "CLOSED", fontSize = 10.sp, fontWeight = FontWeight.Black, color = if (shop.open) Color(0xFF4CAF50) else Color(0xFFE53935))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text("${shop.openingTime} - ${shop.closingTime}", color = Color.Gray, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. STATS DASHBOARD (Amber Tiles)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BusinessStatTile(shop.totalOrders.toString(), "Orders", Modifier.weight(1f), amberOrange, outlineWhite)
                        BusinessStatTile("₹${shop.totalRevenue.toInt()}", "Revenue", Modifier.weight(1f), amberOrange, outlineWhite)
                        BusinessStatTile(String.format("%.1f", shop.rating), "Rating", Modifier.weight(1f), amberOrange, outlineWhite)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 4. ABOUT SECTION
                    if (shop.description.isNotEmpty()) {
                        GlassyDetailSection("About Business", outlineWhite) {
                            Text(shop.description, color = Color.Gray, fontSize = 14.sp, lineHeight = 22.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 5. CONTACT & LOCATION
                    GlassyDetailSection("Contact Details", outlineWhite) {
                        DetailInfoRow(Icons.Default.LocationOn, "Address", buildString {
                            append(shop.address)
                            if (shop.city.isNotEmpty()) append(", ${shop.city}")
                        }, amberOrange)

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = DividerDefaults.Thickness,
                            color = Color.White.copy(alpha = 0.05f)
                        )

                        DetailInfoRow(Icons.Default.Call, "Phone", shop.contactNumber.ifEmpty { "Not set" }, amberOrange)

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = DividerDefaults.Thickness,
                            color = Color.White.copy(alpha = 0.05f)
                        )

                        DetailInfoRow(Icons.Default.Email, "Official Email", shop.email.ifEmpty { "Not set" }, amberOrange)
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // 6. ACTION BUTTON
                    Button(
                        onClick = { navController.navigate("edit_shop/${shop.shopId}") },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = amberOrange)
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text("MANAGE SHOP SETTINGS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

// --- SHARED COMPONENTS ---

@Composable
fun BusinessStatTile(value: String, label: String, modifier: Modifier, accent: Color, outline: Color) {
    Surface(
        modifier = modifier.border(1.dp, outline, RoundedCornerShape(20.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun GlassyDetailSection(title: String, outline: Color, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, accent: Color) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(32.dp).background(accent.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = accent)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 14.sp)
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun ShopDetailsAmberPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Amber Shop Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                BusinessStatTile("₹45k", "Revenue", Modifier.width(120.dp), Color(0xFFFF9100), Color.White.copy(0.1f))
            }
        }
    }
}