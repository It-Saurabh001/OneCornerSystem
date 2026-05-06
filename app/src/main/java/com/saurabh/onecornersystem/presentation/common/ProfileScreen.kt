package com.saurabh.onecornersystem.presentation.common

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.utils.LocationUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    shop: Shop? = null,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onShopClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onBookingsClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {}
) {
    val isOwner = user.role == "shop_owner"

    // --- THEME COLOR LOGIC ---
    val accentColor = if (isOwner) Color(0xFFFF9100) else Color(0xFF2979FF) // Orange for Owner, Blue for Customer
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(300.dp).offset(x = (-100).dp, y = (-50).dp).blur(120.dp).background(accentColor.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(200.dp).align(Alignment.BottomEnd).offset(x = 50.dp, y = 50.dp).blur(100.dp).background(accentColor.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Profile", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = onEditClick) {
                            Icon(Icons.Default.Edit, "Edit", tint = accentColor)
                        }
                    }
                )
            },
            bottomBar = {
                if (!isOwner) {
                    NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                        val navItems = listOf(
                            Triple("Home", Icons.Default.Home, onHomeClick),
                            Triple("Bookings", Icons.Default.Bookmark, onBookingsClick),
                            Triple("Favorites", Icons.Default.Favorite, onFavoritesClick),
                            Triple("Profile", Icons.Default.Person, {})
                        )
                        navItems.forEach { (label, icon, action) ->
                            NavigationBarItem(
                                selected = label == "Profile",
                                onClick = action,
                                icon = { Icon(icon, null) },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accentColor,
                                    selectedTextColor = accentColor,
                                    unselectedIconColor = Color.Gray,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // 1. HEADER SECTION
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(2.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(user.name.uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp).border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = user.role.replace("_", " ").uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.sp
                        )
                    }
                }

                // 2. USER DETAILS CARD (GLASS)
                GlassyProfileCard(outlineWhite, glassWhite) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProfileInfoRow(Icons.Default.Email, "Email", user.email, accentColor)
                        ProfileInfoRow(Icons.Default.Phone, "Phone", user.phone, accentColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. SHOP INFO CARD (IF OWNER)
                if (isOwner && shop != null) {
                    Text("My Business", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
                    GlassyProfileCard(outlineWhite, glassWhite) {
                        Column(modifier = Modifier.padding(16.dp).clickable { onShopClick() }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(45.dp).background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Store, null, tint = accentColor)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(shop.shopName, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(shop.category, color = Color.Gray, fontSize = 12.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MiniStat(shop.totalItems.toString(), if(shop.shopType == ShopType.PRODUCT) "Products" else "Services", accentColor)
                                MiniStat(shop.totalOrders.toString(), "Orders", accentColor)
                                MiniStat(String.format("%.1f", shop.rating), "Rating", accentColor)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 4. LOCATION CARD
                LocationSectionCardLiquid(accentColor, outlineWhite, glassWhite)

                Spacer(modifier = Modifier.height(32.dp))

                // 5. LOGOUT BUTTON
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("LOGOUT / लॉग आउट", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---

@Composable
fun GlassyProfileCard(outline: Color, bg: Color, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = bg,
        shape = RoundedCornerShape(24.dp),
        content = content
    )
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun MiniStat(value: String, label: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accent, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

@Composable
fun LocationSectionCardLiquid(accent: Color, outline: Color, bg: Color) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = bg,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = accent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Location", color = Color.White, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = {
                    scope.launch {
                        isLoading = true
                        currentLocation = LocationUtils.getFreshCurrentLocation(context)
                        isLoading = false
                    }
                }) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = accent, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, null, tint = accent)
                }
            }

            if (currentLocation != null) {
                Text(
                    text = "Lat: ${String.format("%.4f", currentLocation!!.latitude)}, Lng: ${String.format("%.4f", currentLocation!!.longitude)}",
                    color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text("Tap refresh to get coordinates", color = Color.DarkGray, fontSize = 12.sp)
            }
        }
    }
}

// --- SHARED ERROR CARD (As used in Home) ---
@Composable
fun ErrorCardGlass(msg: String?, blue: Color, outline: Color, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.padding(20.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(msg ?: "An error occurred", color = Color.White, textAlign = TextAlign.Center)
            TextButton(onClick = onRetry) { Text("RETRY", color = blue) }
        }
    }
}



@Preview(showBackground = true, name = "Shop Owner Profile (Orange)")
@Composable
fun PreviewOwnerProfile() {
    ProfileScreen(
        user = User(name = "OneCorner Admin", email = "admin@onecorner.com", phone = "1122334455", role = "shop_owner"),
        shop = Shop(shopName = "Mega Service Center", category = "Electronics", totalItems = 12, rating = 4.5),
        onBackClick = {}, onEditClick = {}, onLogoutClick = {}
    )
}