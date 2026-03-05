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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
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
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOwnerHomeScreen1(
    navController: NavController,
    ownerId: String,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val myShopState by viewModel.myShopState.collectAsState()

    LaunchedEffect(ownerId) {
        viewModel.getMyShop(ownerId)
        viewModel.listenToMyShop(ownerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shop Dashboard") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (val shop = (myShopState as? Resource.Success)?.data) {
                        null -> navController.navigate("create_shop")
                        else -> navController.navigate("edit_shop/${shop.shopId}")
                    }
                }
            ) {
                Icon(
                    if (myShopState is Resource.Success) Icons.Default.Edit
                    else Icons.Default.Add,
                    contentDescription = "Manage Shop"
                )
            }
        }
    ) { paddingValues ->
        when (val state = myShopState) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val shop = state.data
                Log.d("TAG", "ShopOwnerHomeScreen1: ${Resource.Success(shop)}")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ShopHeaderCard(
                            shop = shop,
                            isOpen = viewModel.isShopOpen(shop),
                            statusMessage = viewModel.getShopStatusMessage(shop)
                        )
                    }

                    item {
                        StatsGrid(shop = shop)
                    }

                    item {
                        QuickActionsRow(
                            shopId = shop.shopId,
                            shopType = shop.shopType,
                            navController = navController
                        )
                    }

                    item {
                        when (shop.shopType) {
                            ShopType.PRODUCT -> {
                                ProductSection(
                                    shopId = shop.shopId,
                                    navController = navController
                                )
                            }
                            ShopType.SERVICE -> {
                                ServiceSection(
                                    shopId = shop.shopId,
                                    navController = navController
                                )
                            }
                        }
                    }

                    item {
                        RecentOrdersSection(
                            shopId = shop.shopId,
                            navController = navController
                        )
                    }
                }
            }
            is Resource.Error -> {

                Box(

                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Log.d("TAG", "ShopOwnerHomeScreen1: ${Resource.Error(state.message)}")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No shop found")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { navController.navigate("create_shop") }) {
                            Text("Create Shop")
                        }
                    }
                }
            }

            else -> {}
        }
    }
}

@Composable
fun ShopHeaderCard(
    shop: Shop,
    isOpen: Boolean,
    statusMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shop Logo/Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = shop.shopName.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Shop Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shop.shopName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = shop.category,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isOpen) Color.Green else Color.Red,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp
                    )
                }
            }

            // Shop Type Badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (shop.shopType == ShopType.PRODUCT)
                        Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = if (shop.shopType == ShopType.PRODUCT) "🛍️ Products" else "🔧 Services",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatsGrid(shop: Shop) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = shop.totalItems.toString(),
                label = if (shop.shopType == ShopType.PRODUCT) "Products" else "Services"
            )
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            StatItem(
                value = shop.totalOrders.toString(),
                label = "Orders"
            )
            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            StatItem(
                value = String.format("₹%.0f", shop.totalRevenue),
                label = "Revenue"
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun QuickActionsRow(
    shopId: String,
    shopType: ShopType,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton1(
                    icon = Icons.Default.Edit,
                    label = "Edit Shop",
                    onClick = { navController.navigate("edit_shop/$shopId") }
                )

                if (shopType == ShopType.PRODUCT) {
                    QuickActionButton1(
                        icon = Icons.Default.Add,
                        label = "Add Product",
                        onClick = { navController.navigate("add_product/$shopId") }
                    )
                } else {
                    QuickActionButton1(
                        icon = Icons.Default.Add,
                        label = "Add Service",
                        onClick = { navController.navigate("add_service/$shopId") }
                    )
                }

                QuickActionButton1(
                    icon = Icons.Default.ShoppingBag,
                    label = "Orders",
                    onClick = { navController.navigate("orders/$shopId") }
                )
            }
        }
    }
}

@Composable
fun QuickActionButton1(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// Placeholder sections - to be implemented with respective repositories
@Composable
fun ProductSection(shopId: String, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { navController.navigate("products/$shopId") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Products",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Manage your products",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View")
        }
    }
}

@Composable
fun ServiceSection(shopId: String, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { navController.navigate("services/$shopId") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Services",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Manage your services",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View")
        }
    }
}

@Composable
fun RecentOrdersSection(shopId: String, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { navController.navigate("orders/$shopId") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recent Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "View and manage orders",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "View")
        }
    }
}