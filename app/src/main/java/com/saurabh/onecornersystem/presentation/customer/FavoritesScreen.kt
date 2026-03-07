package com.saurabh.onecornersystem.presentation.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.ImageUtils
import com.saurabh.onecornersystem.utils.LocationUtils
import com.saurabh.onecornersystem.utils.Resource

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

    // Get user location for distance calculation
    val context = LocalContext.current
    val userLocation = remember { LocationUtils.getCurrentLocation(context) }

    LaunchedEffect(Unit) {
        viewModel.getFavoriteShops()
        viewModel.getFavoriteProductShops()
        viewModel.getFavoriteServiceShops()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            // Favorites List based on selected tab
            when (selectedTab) {
                0 -> FavoriteList(
                    state = favoriteShopsState,
                    userLocation = userLocation,
                    onShopClick = { shopId ->
                        navController.navigate("service_shop/$shopId")
                    },
                    onRemoveClick = { shop ->
                        viewModel.removeFromFavorites(shop)
                    },
                    emptyMessage = "No favorite shops yet"
                )
                1 -> FavoriteList(
                    state = favoriteProductShopsState,
                    userLocation = userLocation,
                    onShopClick = { shopId ->
                        navController.navigate("shop_details/$shopId")
                    },
                    onRemoveClick = { shop ->
                        viewModel.removeFromFavorites(shop)
                    },
                    emptyMessage = "No favorite product shops"
                )
                2 -> FavoriteList(
                    state = favoriteServiceShopsState,
                    userLocation = userLocation,
                    onShopClick = { shopId ->
                        navController.navigate("service_shop/$shopId")
                    },
                    onRemoveClick = { shop ->
                        viewModel.removeFromFavorites(shop)
                    },
                    emptyMessage = "No favorite service shops"
                )
            }
        }
    }
}

@Composable
fun FavoriteList(
    state: Resource<List<Shop>>,
    userLocation: android.location.Location?,
    onShopClick: (String) -> Unit,
    onRemoveClick: (Shop) -> Unit,
    emptyMessage: String
) {
    when (state) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            if (state.data.isEmpty()) {
                EmptyFavoritesPlaceholder(message = emptyMessage)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.data) { shop ->
                        FavoriteShopCard(
                            shop = shop,
                            userLocation = userLocation,
                            onShopClick = { onShopClick(shop.shopId) },
                            onRemoveClick = { onRemoveClick(shop) }
                        )
                    }
                }
            }
        }
        is Resource.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message ?: "Failed to load favorites",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        // Retry logic
                    }) {
                        Text("Retry")
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun FavoriteShopCard(
    shop: Shop,
    userLocation: android.location.Location?,
    onShopClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onShopClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shop Image/Logo
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (shop.hasLogo && shop.logo.isNotBlank()) {
                    val bitmap = ImageUtils.base64ToBitmap(shop.logo)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        PlaceholderIcon(shop.shopType)
                    }
                } else {
                    PlaceholderIcon(shop.shopType)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Shop Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Shop Name and Type
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shop.shopName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Shop Type Badge
                    Surface(
                        color = if (shop.shopType == ShopType.PRODUCT)
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else
                            Color(0xFF2196F3).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (shop.shopType == ShopType.PRODUCT) "🛍️" else "🔧",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Category
                Text(
                    text = shop.category,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating and Distance Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (shop.rating >= 4.0) Color(0xFFFFB800) else Color.Gray
                        )
                        Text(
                            text = String.format("%.1f", shop.rating),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                        Text(
                            text = " (${shop.totalRatings})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Distance
                    if (userLocation != null) {
                        val distance = LocationUtils.calculateDistance(
                            userLocation.latitude, userLocation.longitude,
                            shop.location.latitude, shop.location.longitude
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = String.format("%.1f km", distance),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Address (shortened)
                Text(
                    text = shop.address.take(30) + if (shop.address.length > 30) "..." else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Remove Button
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PlaceholderIcon(shopType: ShopType) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (shopType == ShopType.PRODUCT)
                Icons.Default.Store
            else
                Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(30.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EmptyFavoritesPlaceholder(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Favorites Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Navigate to home */ }
        ) {
            Text("Browse Shops")
        }
    }
}