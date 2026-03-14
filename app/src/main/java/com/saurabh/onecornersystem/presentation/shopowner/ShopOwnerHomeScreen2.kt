package com.saurabh.onecornersystem.presentation.shopowner


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOwnerHomeScreen1(
    navController: NavController,
    ownerId: String,
    viewModel: ShopViewModel = hiltViewModel(),
    shopItemViewModel: ShopItemViewModel = hiltViewModel()
) {
    Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Displayed with ownerId=$ownerId")
    val context = LocalContext.current
    val myShopState by viewModel.myShopState.collectAsState()
    val servicesState by shopItemViewModel.servicesState.collectAsState()

    // Location state
    var locationUpdated by remember { mutableStateOf(false) }
    var showPermissionPamphlet by remember { mutableStateOf(false) }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val grantedCoarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (grantedFine || grantedCoarse) {
            showPermissionPamphlet = false
            // Fetch and update shop location
            val shop = (myShopState as? Resource.Success)?.data
            if (shop != null && !locationUpdated) {
                fetchAndUpdateShopLocation(context, shop.shopId, viewModel) {
                    locationUpdated = true
                }
            }
        }
    }

    // अगर ownerId empty है तो loading दिखाओ
    if (ownerId.isEmpty()) {
        Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Waiting for ownerId...")
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(ownerId) {
        if (ownerId.isNotEmpty()) {
            Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Setting up shop listener for ownerId=$ownerId")
            viewModel.getMyShop(ownerId)
            viewModel.listenToMyShop(ownerId)
        }
    }

    // Auto-fetch location when shop is loaded and location is (0,0)
    LaunchedEffect(myShopState) {
        val shop = (myShopState as? Resource.Success)?.data
        if (shop != null && !locationUpdated) {
            // Check if shop location is default (0,0)
            if (shop.location.latitude == 0.0 && shop.location.longitude == 0.0) {
                Log.d("ShopOwnerHome_Screen", "Shop location is (0,0), requesting location update")

                val hasFineLocation = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val hasCoarseLocation = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasFineLocation || hasCoarseLocation) {
                    fetchAndUpdateShopLocation(context, shop.shopId, viewModel) {
                        locationUpdated = true
                    }
                } else {
                    showPermissionPamphlet = true
                }
            }
        }
    }

    // Permission Pamphlet
    if (showPermissionPamphlet) {
        ShopLocationPermissionPamphlet(
            onDismiss = { showPermissionPamphlet = false },
            onGrantClick = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Shop Dashboard") },
                actions = {
                    IconButton(onClick = {
                        Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Profile button clicked")
                        navController.navigate("profile")
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            when (val state = myShopState) {
                is Resource.Success -> {
                    val shop = state.data
                    // 🎯 Add Item का option हमेशा दिखाओ
                    FloatingActionButton(
                        onClick = {
                            when (shop.shopType) {
                                ShopType.PRODUCT -> {
                                    Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Add Product clicked for shopId=${shop.shopId}")
                                    navController.navigate("add_product/${shop.shopId}")
                                }
                                ShopType.SERVICE -> {
                                    Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Add Service clicked for shopId=${shop.shopId}")
                                    navController.navigate("add_service/${shop.shopId}")
                                }}
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = if (shop.shopType == ShopType.PRODUCT)
                                "Add Product" else "Add Service"
                        )
                    }
                }
                is Resource.Error -> {
                    // Shop नहीं मिली - Create Shop का option
                    FloatingActionButton(
                        onClick = {
                            Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Create Shop clicked")
                            navController.navigate("create_shop")
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Shop"
                        )
                    }
                }
                else -> {}
            }
        }
    ) { paddingValues ->
        when (val state = myShopState) {
            is Resource.Loading -> {
                Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Loading state")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                val shop = state.data

                if (shop.shopType == ShopType.SERVICE) {
                    LaunchedEffect(shop.shopId) {
                        if (shop.shopId.isNotEmpty()) {
                            shopItemViewModel.getServicesByShop(shop.shopId)
                        }
                    }
                }

                val itemCount = when {
                    shop.shopType == ShopType.SERVICE && servicesState is Resource.Success -> {
                        (servicesState as Resource.Success<List<ShopItem>>).data.size
                    }
                    else -> shop.totalItems
                }

                Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Shop loaded - shopId=${shop.shopId}, totalItems=${shop.totalItems}, totalOrders=${shop.totalOrders}, totalRevenue=${shop.totalRevenue}")

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
                            statusMessage = viewModel.getShopStatusMessage(shop),
                            onShopClick = {
                                Log.d("ShopOwnerHome_Screen", "Shop header clicked - navigating to shop details")
                                navController.navigate("my_shop_details/${shop.shopId}")
                            }
                        )
                    }

                    item {
                        StatsGrid(shop = shop, itemCount = itemCount)
                    }

                    // 🎯 अगर items = 0 हैं, तो Empty State Message दिखाओ
                    if (itemCount == 0) {
                        item {
                            EmptyItemsMessage(shop = shop, itemCount = itemCount)
                        }
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
                Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Error - ${state.message}")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No shop found")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            Log.d("ShopOwnerHome_Screen", "ShopOwnerHomeScreen1: Create Shop button clicked from error state")
                            navController.navigate("create_shop")
                        }) {
                            Text("Create Shop")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

/**
 * Custom Location Permission Pamphlet for Shop Owners
 */
@Composable
fun ShopLocationPermissionPamphlet(
    onDismiss: () -> Unit,
    onGrantClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Set Your Shop Location",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "To help customers find your shop easily, we need to save your shop's GPS coordinates.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PermissionRow(
                            title = "Precise Location",
                            description = "Ensures customers see your shop exactly where it is on the map.",
                            icon = Icons.Default.GpsFixed
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PermissionRow(
                            title = "Approximate Location",
                            description = "Used to categorize your shop in the correct locality/city.",
                            icon = Icons.Default.LocationCity
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onGrantClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("Enable Location Access", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Skip for Now", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp).padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * 🎯 EMPTY ITEMS MESSAGE - जब totalItems = 0 हो, सिर्फ message दिखाओ
 */
@Composable
fun EmptyItemsMessage(shop: Shop, itemCount: Int) {
    Log.d("ShopOwnerHome_Empty", "EmptyItemsMessage: Displayed for shopType=${shop.shopType}")
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
            Icon(
                imageVector = if (shop.shopType == ShopType.PRODUCT)
                    Icons.Default.ShoppingBag
                else
                    Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (shop.shopType == ShopType.PRODUCT)
                        "No Products Yet"
                    else
                        "No Services Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = if (shop.shopType == ShopType.PRODUCT)
                        "Click the + button to add your first product"
                    else
                        "Click the + button to add your first service",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ShopHeaderCard(
    shop: Shop,
    isOpen: Boolean,
    statusMessage: String,
    onShopClick: () -> Unit = {}
) {
    Log.d("ShopOwnerHome_Header", "ShopHeaderCard: Displayed for shopName=${shop.shopName}, category=${shop.category}, isOpen=$isOpen")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = onShopClick
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

            // View Details Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Shop Details",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Shop Type Badge Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tap to view shop details",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
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

@SuppressLint("DefaultLocale")
@Composable
fun StatsGrid(shop: Shop, itemCount: Int) {
    Log.d("ShopOwnerHome_Stats", "StatsGrid: Displayed with totalItems=${shop.totalItems}, totalOrders=${shop.totalOrders}, totalRevenue=${shop.totalRevenue}")
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
                value = itemCount.toString(),
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
    Log.d("ShopOwnerHome_Stat", "StatItem: Displayed with value=$value, label=$label")
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
    Log.d("ShopOwnerHome_Actions", "QuickActionsRow: Displayed for shopId=$shopId, shopType=$shopType")
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
                    onClick = {
                        Log.d("ShopOwnerHome_Actions", "QuickActionsRow: Edit Shop clicked for shopId=$shopId")
                        navController.navigate("edit_shop/$shopId") // Will be handled by NavGraph
                    }
                )

                if (shopType == ShopType.PRODUCT) {
                    QuickActionButton1(
                        icon = Icons.Default.Add,
                        label = "Add Product",
                        onClick = {
                            Log.d("ShopOwnerHome_Actions", "QuickActionsRow: Add Product clicked for shopId=$shopId")
                            navController.navigate("add_product/$shopId")
                        }
                    )
                } else {
                    QuickActionButton1(
                        icon = Icons.Default.Add,
                        label = "Add Service",
                        onClick = {
                            Log.d("ShopOwnerHome_Actions", "QuickActionsRow: Add Service clicked for shopId=$shopId")
                            navController.navigate("add_service/$shopId")
                        }
                    )
                }

                QuickActionButton1(
                    icon = Icons.Default.ShoppingBag,
                    label = "Orders",
                    onClick = {
                        Log.d("ShopOwnerHome_Actions", "QuickActionsRow: Orders clicked for shopId=$shopId")
                        navController.navigate("orders/$shopId")
                    }
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
    Log.d("ShopOwnerHome_Button", "QuickActionButton1: Displayed with label=$label")
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

@Composable
fun ProductSection(shopId: String, navController: NavController) {
    Log.d("ShopOwnerHome_Product", "ProductSection: Displayed for shopId=$shopId")
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            Log.d("ShopOwnerHome_Product", "ProductSection: Clicked to view products for shopId=$shopId")
            navController.navigate("products/$shopId")
        }
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
    Log.d("ShopOwnerHome_Service", "ServiceSection: Displayed for shopId=$shopId")
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            Log.d("ShopOwnerHome_Service", "ServiceSection: Clicked to view services for shopId=$shopId")
            navController.navigate("services/$shopId")
        }
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
    Log.d("ShopOwnerHome_Orders", "RecentOrdersSection: Displayed for shopId=$shopId")
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            Log.d("ShopOwnerHome_Orders", "RecentOrdersSection: Clicked to view orders for shopId=$shopId")
            navController.navigate("orders/$shopId")
        }
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

/**
 * Fetch current location and update shop location in Firestore
 */
@SuppressLint("MissingPermission")
private fun fetchAndUpdateShopLocation(
    context: Context,
    shopId: String,
    viewModel: ShopViewModel,
    onComplete: () -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                if (location != null) {
                    Log.d("ShopOwnerHome_Location", "Location fetched: ${location.latitude}, ${location.longitude}")
                    viewModel.updateShopLocation(shopId, location.latitude, location.longitude)
                    onComplete()
                } else {
                    // Try LocationManager as fallback
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                    if (lastKnown != null) {
                        Log.d("ShopOwnerHome_Location", "LocationManager location: ${lastKnown.latitude}, ${lastKnown.longitude}")
                        viewModel.updateShopLocation(shopId, lastKnown.latitude, lastKnown.longitude)
                    } else {
                        Log.d("ShopOwnerHome_Location", "Could not get location")
                    }
                    onComplete()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ShopOwnerHome_Location", "Failed to get location", e)
                onComplete()
            }
    } catch (e: Exception) {
        Log.e("ShopOwnerHome_Location", "Exception in fetchAndUpdateShopLocation", e)
        onComplete()
    }
}
