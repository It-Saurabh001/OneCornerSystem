package com.saurabh.onecornersystem.presentation.customer

import android.Manifest
import android.content.Intent
import android.location.Location
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.presentation.common.AppNavigationDrawer
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.LocationUtils
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    navController: NavController,
    currentUser: User?,
    viewModel: CustomerShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedShopType by remember { mutableStateOf<ShopType?>(ShopType.SERVICE) } // Default to SERVICE
    var isSearching by remember { mutableStateOf(false) }

    // Location states
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLocationLoading by remember { mutableStateOf(true) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }

    // ViewModel states
    val nearbyShopsState by viewModel.nearbyServiceShopsState.collectAsState() // Only service shops
    val serviceCategoriesState by viewModel.serviceCategoriesState.collectAsState()
    val searchServicesState by viewModel.searchServicesState.collectAsState() // Search results
    val isLoading by viewModel.isLoading.collectAsState()

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            // Permission granted, fetch location
            scope.launch {
                if (LocationUtils.isLocationEnabled(context)) {
                    isLocationLoading = true
                    userLocation = LocationUtils.getFreshCurrentLocation(context)
                    isLocationLoading = false

                    // Fetch nearby shops with new location
                    userLocation?.let { location ->
                        Log.d("CustomerHomeScreen", "Fetching shops for location: ${location.latitude}, ${location.longitude}")
                        viewModel.getNearbyServiceShops(location.latitude, location.longitude)
                    }
                } else {
                    showLocationSettingsDialog = true
                    isLocationLoading = false
                }
            }
        } else {
            isLocationLoading = false
        }
    }

    // Fetch fresh location when screen opens
    LaunchedEffect(Unit) {
        Log.d("CustomerHomeScreen", "Checking location permission...")

        if (LocationUtils.hasLocationPermission(context)) {
            if (LocationUtils.isLocationEnabled(context)) {
                isLocationLoading = true
                Log.d("CustomerHomeScreen", "Fetching fresh current location...")

                // Fetch fresh location
                userLocation = LocationUtils.getFreshCurrentLocation(context)
                isLocationLoading = false

                userLocation?.let { location ->
                    Log.d("CustomerHomeScreen", "Location fetched: ${location.latitude}, ${location.longitude}")
                    viewModel.getNearbyServiceShops(location.latitude, location.longitude)
                } ?: run {
                    Log.d("CustomerHomeScreen", "Could not get location")
                }
            } else {
                // Location is off
                showLocationSettingsDialog = true
                isLocationLoading = false
            }
        } else {
            // Request permission
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Categories from ViewModel
    val categories = remember { mutableStateListOf<String>().apply { add("All") } }

    LaunchedEffect(Unit) {
        viewModel.getServiceCategories()
    }

    // Update categories when loaded
    LaunchedEffect(serviceCategoriesState) {
        val state = serviceCategoriesState
        Log.d("TAG", "CustomerHomeScreen: $state")
        if (state is Resource.Success<List<CategoryWithType>>) {
            categories.clear()
            categories.add("All")
            categories.addAll(state.data.map { it.categoryName })
        }
    }

    // Location Settings Dialog
    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            title = { Text("Location is Off") },
            text = { Text("Please turn on location services to find services near you.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationSettingsDialog = false
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.65f)
            ) {
                AppNavigationDrawer(
                    user = currentUser,
                    onProfileClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("profile")
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    },
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        // Show about
                    },
                    onThemeClick = {
                        scope.launch { drawerState.close() }
                        // Toggle theme
                    },
                    onContactClick = {
                        scope.launch { drawerState.close() }
                        // Show contact
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "OneCorner Services",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        // Notifications button
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("my_bookings") },
                        icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookings") },
                        label = { Text("Bookings") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("favorites") },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { paddingValues ->
            if (isLoading && nearbyShopsState is Resource.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Search Bar
                    item {
                        ServiceSearchBar(
                            query = searchQuery,
                            onQueryChange = { query ->
                                searchQuery = query
                                if (query.isNotBlank() && query.length >= 2) {
                                    isSearching = true
                                    viewModel.searchServices(query)
                                } else {
                                    isSearching = false
                                    viewModel.clearSearchResults()
                                }
                            },
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    isSearching = true
                                    viewModel.searchServices(searchQuery)
                                }
                            },
                            onClear = {
                                searchQuery = ""
                                isSearching = false
                                viewModel.clearSearchResults()
                            }
                        )
                    }

                    // Show search results if searching
                    if (isSearching && searchQuery.isNotBlank()) {
                        // Search Results Header
                        item {
                            Text(
                                text = "Search Results for \"$searchQuery\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Search Results
                        when (val state = searchServicesState) {
                            is Resource.Loading -> {
                                item { SearchLoading() }
                            }
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    item { EmptySearchResults(query = searchQuery) }
                                } else {
                                    items(state.data) { service ->
                                        ServiceSearchResultCard(
                                            service = service,
                                            onBookClick = {
                                                navController.navigate("booking_form/${service.itemId}")
                                            }
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                item {
                                    SearchError(
                                        message = state.message,
                                        onRetry = { viewModel.searchServices(searchQuery) }
                                    )
                                }
                            }
                            else -> {}
                        }
                    } else {
                        // Normal content when not searching

                        // Service Categories
                        item {
                            ServiceCategoriesRow(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = {
                                    selectedCategory = it
                                    // Filter shops by category
                                }
                            )
                        }

                        // Popular Services Section
                        item {
                        SectionHeader(
                            title = "Popular Services Near You",
                            onViewAllClick = {
//                                navController.navigate("all_services")
                            }
                        )
                    }

                    // Service Shops Grid
                    when (val state = nearbyShopsState) {
                        is Resource.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Finding services near you...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Log.d("CustomerHomeScreen", "Loading nearby shops...")
                        }
                        is Resource.Success -> {
                            val filteredShops = if (selectedCategory == "All") {
                                state.data
                            } else {
                                state.data.filter { it.category == selectedCategory }
                            }

                            Log.d("CustomerHomeScreen", "Loaded ${state.data.size} shops, filtered: ${filteredShops.size}")

                            if (filteredShops.isEmpty()) {
                                // No shops available
                                item {
                                    NoShopsAvailableCard(
                                        selectedCategory = selectedCategory,
                                        hasLocation = userLocation != null,
                                        onRefresh = {
                                            scope.launch {
                                                if (LocationUtils.hasLocationPermission(context)) {
                                                    isLocationLoading = true
                                                    userLocation = LocationUtils.getFreshCurrentLocation(context)
                                                    isLocationLoading = false
                                                    userLocation?.let { location ->
                                                        viewModel.getNearbyServiceShops(location.latitude, location.longitude)
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                                items(filteredShops) { shop ->
                                    ServiceShopCard(
                                        shop = shop,
                                        userLocation = userLocation,
                                        onClick = {
                                            Log.d("CustomerHomeScreen", "Navigating to shop: ${shop.shopId} - ${shop.shopName}")
                                            navController.navigate("service_shop/${shop.shopId}")
                                        }
                                    )
                                }
                            }
                        }
                        is Resource.Error -> {
                            item {
                                Log.e("CustomerHomeScreen", "Error loading shops: ${state.message}")
                                ErrorCard(
                                    message = state.message,
                                    onRetry = {
                                        userLocation?.let { location ->
                                            viewModel.getNearbyServiceShops(location.latitude, location.longitude)
                                        }
                                    }
                                )
                            }
                        }
                        is Resource.Idle -> {
                            // Location not fetched yet
                            item {
                                if (userLocation == null && !isLocationLoading) {
                                    LocationRequiredCard(
                                        onEnableLocation = {
                                            if (!LocationUtils.hasLocationPermission(context)) {
                                                locationPermissionLauncher.launch(
                                                    arrayOf(
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    )
                                                )
                                            } else if (!LocationUtils.isLocationEnabled(context)) {
                                                showLocationSettingsDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        else -> {
                            // Handle any other state
                        }
                    }

                    // Bottom Spacer
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    } // End of else (not searching) block
                }
            }
        }
    }
}

@Composable
fun ServiceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit = {}
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Search for services (plumber, salon, repair...)") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun ServiceCategoriesRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        category,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = if (category != "All") {
                    {
                        Icon(
                            when (category.lowercase()) {
                                "automotive" -> Icons.Default.Build
                                "beauty" -> Icons.Default.Face
                                "repair" -> Icons.Default.Handyman
                                "cleaning" -> Icons.Default.CleaningServices
                                "plumbing" -> Icons.Default.Plumbing
                                "electrical" -> Icons.Default.ElectricalServices
                                else -> Icons.Default.Build
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
fun ServiceShopCard(
    shop: Shop,
    userLocation: android.location.Location?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Shop Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop Image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (shop.hasLogo && shop.logo.isNotBlank()) {
                        // Load actual logo
                        // Image(painter = rememberAsyncImagePainter(shop.logo), ...)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(30.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Shop Name and Rating
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.shopName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

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

                        Spacer(modifier = Modifier.width(8.dp))

                        // Open/Closed Status
                        Surface(
                            color = if (shop.open)
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else
                                Color(0xFFE53935).copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (shop.open) "OPEN" else "CLOSED",
                                fontSize = 10.sp,
                                color = if (shop.open) Color(0xFF4CAF50) else Color(0xFFE53935),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shop Category
            Text(
                text = shop.category,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location and Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = shop.address.take(25) + if (shop.address.length > 25) "..." else "",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

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
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Services Tags (Sample)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(listOf("Repair", "Maintenance", "Emergency")) { tag ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Book Button
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Book Service",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String?,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message ?: "Something went wrong",
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

/**
 * Card shown when no shops are available in user's area
 */
@Composable
fun NoShopsAvailableCard(
    selectedCategory: String,
    hasLocation: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (selectedCategory == "All")
                    "No Services Available"
                else
                    "No $selectedCategory Services",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasLocation)
                    "Sorry, there are no service providers available in your area right now. Please try again later or check nearby areas."
                else
                    "We couldn't determine your location. Please enable location to find services near you.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onRefresh
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}

/**
 * Card shown when location is required but not available
 */
@Composable
fun LocationRequiredCard(
    onEnableLocation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location Required",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enable location services to discover service providers near you.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onEnableLocation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Location")
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onViewAllClick) {
            Text("View All", fontSize = 13.sp)
        }
    }
}