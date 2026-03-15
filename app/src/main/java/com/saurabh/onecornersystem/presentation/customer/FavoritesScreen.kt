package com.saurabh.onecornersystem.presentation.customer

import androidx.compose.foundation.Image
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.ImageUtils
import com.saurabh.onecornersystem.utils.LocationUtils
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.firestore.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: CustomerShopViewModel = hiltViewModel()
) {
    val favoriteShopsState by viewModel.favoriteShopsState.collectAsStateWithLifecycle()
    val favoriteProductShopsState by viewModel.favoriteProductShopsState.collectAsStateWithLifecycle()
    val favoriteServiceShopsState by viewModel.favoriteServiceShopsState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Products", "Services")

    val context = LocalContext.current
    val userLocation = remember { LocationUtils.getCurrentLocation(context) }

    // Liquid Theme Colors
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.15f)

    LaunchedEffect(Unit) {
        viewModel.getFavoriteShops()
        viewModel.getFavoriteProductShops()
        viewModel.getFavoriteServiceShops()
    }

    Box(modifier = Modifier.fillMaxSize().background(deepBlack.copy(alpha = 0.95f))) {
        // --- LIQUID BACKGROUND BLOBS ---
        Box(modifier = Modifier.size(350.dp).offset(x = 150.dp, y = 100.dp).blur(120.dp).background(electricBlue.copy(alpha = 0.15f), CircleShape))
        Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = (-20).dp).blur(100.dp).background(electricBlue.copy(alpha = 0.1f), CircleShape))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                    title = { Text("Favorites", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                    val navItems = listOf(
                        Triple("Home", Icons.Default.Home, "customer_home"),
                        Triple("Bookings", Icons.Default.Bookmark, "my_bookings"),
                        Triple("Favorites", Icons.Default.Favorite, "favorites"),
                        Triple("Profile", Icons.Default.Person, "profile")
                    )
                    navItems.forEach { (label, icon, route) ->
                        NavigationBarItem(
                            selected = label == "Favorites",
                            onClick = { if (label != "Favorites") navController.navigate(route) },
                            icon = { Icon(icon, null) },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = electricBlue,
                                selectedTextColor = electricBlue,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // 1. Transparent Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = electricBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = electricBlue,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 14.sp,
                                    fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if(selectedTab == index) Color.White else Color.Gray
                                )
                            }
                        )
                    }
                }

                // 2. Conditional List Content
                val currentState = when (selectedTab) {
                    0 -> favoriteShopsState
                    1 -> favoriteProductShopsState
                    else -> favoriteServiceShopsState
                }

                FavoriteListLiquid(
                    state = currentState,
                    userLocation = userLocation,
                    blue = electricBlue,
                    outline = outlineWhite,
                    onShopClick = { shopId ->
                        val route = if (selectedTab == 1) "shop_details/$shopId" else "service_shop/$shopId"
                        navController.navigate(route)
                    },
                    onRemoveClick = { shop -> viewModel.removeFromFavorites(shop) }
                )
            }
        }
    }
}

@Composable
fun FavoriteListLiquid(
    state: Resource<List<Shop>>,
    userLocation: android.location.Location?,
    blue: Color,
    outline: Color,
    onShopClick: (String) -> Unit,
    onRemoveClick: (Shop) -> Unit
) {
    when (state) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = blue)
            }
        }
        is Resource.Success -> {
            if (state.data.isEmpty()) {
                EmptyFavoritesGlass(blue, outline)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.data) { shop ->
                        FavoriteShopCardGlass(shop, userLocation, blue, outline, { onShopClick(shop.shopId) }, { onRemoveClick(shop) })
                    }
                }
            }
        }
        is Resource.Error -> {
            ErrorCardGlass(state.message, blue, outline) { /* Retry handled by Refresh */ }
        }
        else -> {}
    }
}

@Composable
fun FavoriteShopCardGlass(
    shop: Shop,
    userLocation: android.location.Location?,
    blue: Color,
    outline: Color,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Shop Image
            Box(modifier = Modifier.size(65.dp).clip(RoundedCornerShape(16.dp)).background(blue.copy(alpha = 0.1f))) {
                if (shop.hasLogo && shop.logo.isNotBlank()) {
                    val bitmap = ImageUtils.base64ToBitmap(shop.logo)
                    if (bitmap != null) {
                        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                } else {
                    Icon(if (shop.shopType == ShopType.PRODUCT) Icons.Default.Storefront else Icons.Default.Handyman, null, tint = blue, modifier = Modifier.align(Alignment.Center).size(28.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(shop.shopName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(shop.category, color = Color.Gray, fontSize = 12.sp)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                    Text(" ${shop.rating}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    if (userLocation != null) {
                        val distance = LocationUtils.calculateDistance(userLocation.latitude, userLocation.longitude, shop.location.latitude, shop.location.longitude)
                        Text(" • ${String.format("%.1f km", distance)}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun EmptyFavoritesGlass(blue: Color, outline: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.FavoriteBorder, null, modifier = Modifier.size(80.dp), tint = Color.Gray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Your Heart is Empty", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text("Save your favorite experts to find them easily later.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesLiquidPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(16.dp)) {
                FavoriteShopCardGlass(
                    Shop(shopName = "Urban Salon", category = "Beauty", rating = 4.9, shopType = ShopType.SERVICE, location = GeoPoint(0.0,0.0), address = "Lucknow"),
                    null, Color(0xFF2979FF), Color.White.copy(0.1f), {}, {}
                )
            }
        }
    }
}