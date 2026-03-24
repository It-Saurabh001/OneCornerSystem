package com.saurabh.onecornersystem.presentation.shopowner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.navigation.Screen
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource

@Composable
fun ShopOwnerHomeScreen1(
    navController: NavController,
    ownerId: String,
    viewModel: ShopViewModel = hiltViewModel(),
    shopItemViewModel: ShopItemViewModel = hiltViewModel()
) {
    // --- LIVE STATES ---
    val myShopState by viewModel.myShopState.collectAsState()


    var selectedTab by remember { mutableIntStateOf(0) }
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)

    LaunchedEffect(ownerId) {
        viewModel.getMyShop(ownerId)
        viewModel.listenToMyShop(ownerId)
    }

    LaunchedEffect(myShopState) {
        (myShopState as? Resource.Success)?.data?.let { shop ->
            viewModel.listenToShopBookings(shop.shopId)
            shopItemViewModel.getServicesByShop(shop.shopId)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BACKGROUND ---
        Box(modifier = Modifier.size(400.dp).offset(x = 180.dp, y = (-100).dp).blur(130.dp).background(amberOrange.copy(0.12f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                OwnerBottomBar(selectedTab, { selectedTab = it }, amberOrange)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = myShopState) {
                    is Resource.Loading -> Box(modifier = Modifier.padding(paddingValues)){
                        FullLoadingScreenLiquid()
                    }

                    is Resource.Success -> {
                        val shop = state.data
                        // --- SWITCHING CONTENT BASED ON BOTTOM BAR ---
                        when (selectedTab) {
                            0 -> HomeDashboardTab(shop, amberOrange, navController)
                            1 -> OrderManagementScreen(shop.shopId, navController, viewModel)
                            2 -> ServiceListScreen(shop.shopId, navController)
                        }
                    }
                    is Resource.Error -> ErrorCardGlass(state.message, amberOrange) { viewModel.getMyShop(ownerId) }
                    else -> {}
                }
            }
        }
    }
}

// ================= TAB 0: HOME (GRAPH + STATS) =================
@Composable
fun HomeDashboardTab(shop: Shop, accent: Color, navController: NavController) {
    val outline = Color.White.copy(alpha = 0.1f)
    Scaffold(containerColor = Color.Transparent,
        modifier = Modifier) {innerpadding->
        Box(modifier = Modifier.padding(innerpadding)
            .background(Color.Transparent)) {

            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item {
                    Column {
                        Text("Enterprise Hub", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text(shop.shopName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }

                // 1. REVENUE GRAPH (Custom Bar Chart)
                item {
                    BusinessPerformanceGraph(accent, outline)
                }

                // 2. LIVE STATS GRID
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RealtimeStatCard("Total Revenue", "₹${shop.totalRevenue.toInt()}", Icons.Default.Payments, Modifier.weight(1.2f), accent, outline)
                            RealtimeStatCard("Orders", shop.totalOrders.toString(), Icons.Default.ReceiptLong, Modifier.weight(0.8f), accent, outline)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RealtimeStatCard("Avg Rating", String.format("%.1f", shop.rating), Icons.Default.Star, Modifier.weight(1f), accent, outline)
                            RealtimeStatCard("Inventory", shop.totalItems.toString(), Icons.Default.Inventory2, Modifier.weight(1f), accent, outline)
                        }
                    }
                }

                // 3. QUICK ACTIONS
                item {
                    Text("Quick Management", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        OwnerActionTile("Edit Shop", Icons.Default.Settings, accent, Color.White.copy(0.05f), outline) { navController.navigate("edit_shop/${shop.shopId}") }
                        OwnerActionTile("New Service", Icons.Default.AddCircle, accent, Color.White.copy(0.05f), outline) {
                            val route = if(shop.shopType == ShopType.PRODUCT) "add_product/${shop.shopId}" else "add_service/${shop.shopId}"
                            navController.navigate(route)
                        }
                        OwnerActionTile("Profile", Icons.Default.Person, accent, Color.White.copy(0.05f), outline) {
                            /* More Stats */
                            navController.navigate(Screen.Profile.route)
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }

    }


}

// ================= CUSTOM GRAPH COMPONENT =================
@Composable
fun BusinessPerformanceGraph(accent: Color, outline: Color) {
    // Mock data for last 7 days performance
    val dataPoints = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 1f)
    val days = listOf("M", "T", "W", "T", "F", "S", "S")

    Surface(
        modifier = Modifier.fillMaxWidth().height(220.dp).border(1.dp, outline, RoundedCornerShape(28.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Weekly Revenue Trend", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dataPoints.forEachIndexed { index, value ->
                    val animatedHeight by animateFloatAsState(targetValue = value, animationSpec = tween(1000))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .fillMaxHeight(animatedHeight * 0.7f)
                                .background(
                                    Brush.verticalGradient(listOf(accent, accent.copy(0.2f))),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(days[index], color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ================= BOTTOM BAR NAVIGATION =================
@Composable
fun OwnerBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit, accent: Color) {
    val items = listOf(
        Triple("Home", Icons.Default.GridView, 0),
        Triple("Orders", Icons.AutoMirrored.Filled.ReceiptLong, 1),
        Triple("Inventory", Icons.Default.Inventory, 2),
    )

    Surface(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp).height(72.dp).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(36.dp)),
        color = Color(0xFF151515).copy(alpha = 0.95f),
        shape = RoundedCornerShape(36.dp),
        shadowElevation = 12.dp
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            items.forEach { (label, icon, index) ->
                val isSelected = selectedTab == index
                val color by animateColorAsState(if (isSelected) accent else Color.Gray)

                Column(
                    modifier = Modifier.clickable { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(if (isSelected) 20.dp else 17.dp))
                    Text(text = label, color= color, fontSize = 8.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(top = 2.dp))
                    if (isSelected) Box(Modifier.size(4.dp).background(accent, CircleShape).offset(y = 4.dp))
                }
            }
        }
    }
}

@Composable
fun RealtimeStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier, accent: Color, outline: Color) {
    Surface(
        modifier = modifier.height(110.dp).border(1.dp, outline, RoundedCornerShape(28.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = accent.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Column {
                Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
    }
}

@Composable
fun OwnerActionTile(label: String, icon: ImageVector, accent: Color, bg: Color, outline: Color, onClick: () -> Unit) {
    Column(modifier = Modifier.width(90.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(70.dp).border(1.dp, outline, RoundedCornerShape(20.dp)),
            color = bg,
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FullLoadingScreenLiquid() {
    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0A)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFFFF9100))
    }
}

@Composable
fun ErrorCardGlass(msg: String?, accent: Color, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(msg ?: "Error", color = Color.White)
            TextButton(onClick = onRetry) { Text("Retry", color = accent) }
        }
    }
}
