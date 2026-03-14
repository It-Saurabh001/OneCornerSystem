package com.saurabh.onecornersystem.presentation.customer

import android.Manifest
import android.app.Activity
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.presentation.common.AppNavigationDrawer
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.LocationUtils
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.firestore.GeoPoint
import com.saurabh.onecornersystem.presentation.components.NearbyShopItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CustomerHomeScreenPreview() {
    val mockUserLocation = android.location.Location("dummy_provider").apply {
        latitude = 26.9325569
        longitude = 80.9402406
    }
    val mockCategories = listOf("All", "Repair Services", "Plumbing", "Cleaning")
    val mockShops = listOf(
        Shop(
            shopId = "shop_1",
            shopName = "A1 Expert Plumbers",
            category = "Plumbing",
            rating = 4.8,
            totalRatings = 124,
            open = true,
            address = "Gomti Nagar, Lucknow, Uttar Pradesh",
            location = GeoPoint(26.850000, 80.949999),
            hasLogo = false,
            logo = "",
        ),
        Shop(
            shopId = "shop_2",
            shopName = "Quick Fix Electricals",
            category = "Repair Services",
            rating = 4.2,
            totalRatings = 56,
            open = true,
            address = "Hazratganj, Lucknow",
            location = GeoPoint(26.860000, 80.950000),
            hasLogo = false,
            logo = ""
        ),
        Shop(
            shopId = "shop_3",
            shopName = "Shine Cleaning Co.",
            category = "Cleaning",
            rating = 3.9,
            totalRatings = 12,
            open = false,
            address = "Aliganj, Lucknow",
            location = GeoPoint(26.880000, 80.940000),
            hasLogo = false,
            logo = ""
        )
    )
    MaterialTheme {
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
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, "") }, label = { Text("Home") })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Bookmark, "") }, label = { Text("Bookings") })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Favorite, "") }, label = { Text("Favorites") })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, "") }, label = { Text("Profile") })
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ServiceSearchBar(query = "", onQueryChange = {}, onSearch = {}, onClear = {})
                }
                item {
                    ServiceCategoriesRow(
                        categories = mockCategories,
                        selectedCategory = "All",
                        onCategorySelected = {}
                    )
                }
                item {
                    SectionHeader(title = "Popular Services Near You", onViewAllClick = { })
                }
                items(mockShops) { shop ->
                    ServiceShopCard(
                        shop = shop,
                        userLocation = mockUserLocation,
                        onClick = {}
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

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
    val lifecycleOwner = LocalLifecycleOwner.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedShopType by remember { mutableStateOf<ShopType?>(ShopType.SERVICE) }
    var isSearching by remember { mutableStateOf(false) }

    val userLocation by viewModel.userLocation.collectAsState()
    var isLocationLoading by remember { mutableStateOf(false) }

    val nearbyServiceItemsState by viewModel.nearbyServiceItemsState.collectAsState()
    val nearbyShopsState by viewModel.nearbyServiceShopsState.collectAsState()
    val serviceCategoriesState by viewModel.serviceCategoriesState.collectAsState()
    val searchServicesState by viewModel.searchServicesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val gpsResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("CustomerHomeScreen", "GPS Resolution OK")
            scope.launch {
                isLocationLoading = true
                val freshLoc = LocationUtils.getFreshCurrentLocation(context)
                isLocationLoading = false

                // Location ViewModel me update karo
                viewModel.updateUserLocation(freshLoc)

                freshLoc?.let { location ->
                    viewModel.getNearbyServiceItems(location.latitude, location.longitude)
                }
            }
        }
    }

    var triggerLocationRefresh: () -> Unit = {}

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val grantedCoarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (grantedFine || grantedCoarse) {
            triggerLocationRefresh()
        } else {
            isLocationLoading = false
        }
    }

    triggerLocationRefresh = {
        if (LocationUtils.hasLocationPermission(context)) {
            LocationUtils.checkLocationSettings(
                context = context,
                onResolutionRequired = { intentSender ->
                    gpsResolutionLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                },
                onAlreadySatisfied = {
                    isLocationLoading = true
                    scope.launch {
                        Log.d("CustomerHomeScreen", "Fetching fresh location...")
                        var loc = LocationUtils.getFreshCurrentLocation(context)

                        if (loc == null) {
                            loc = LocationUtils.getCurrentLocation(context)
                        }

                        isLocationLoading = false
                        // Location ViewModel me update karo
                        viewModel.updateUserLocation(loc)

                        loc?.let { location ->
                            viewModel.getNearbyServiceShops(location.latitude, location.longitude)
                        }
                    }
                }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (userLocation == null) {
            triggerLocationRefresh()
        }
    }

    val categories = remember { mutableStateListOf<String>().apply { add("All") } }

    LaunchedEffect(Unit) {
        viewModel.getServiceCategories()
    }

    LaunchedEffect(serviceCategoriesState) {
        val state = serviceCategoriesState
        if (state is Resource.Success<List<CategoryWithType>>) {
            categories.clear()
            categories.add("All")
            categories.addAll(state.data.map { it.categoryName })
        }
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
                    onAboutClick = { scope.launch { drawerState.close() } },
                    onThemeClick = { scope.launch { drawerState.close() } },
                    onContactClick = { scope.launch { drawerState.close() } }
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
            if (isLocationLoading || (isLoading && nearbyServiceItemsState is Resource.Loading)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        if (isLocationLoading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Getting location...")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    if (isSearching && searchQuery.isNotBlank()) {
                        item {
                            Text(
                                text = "Search Results for \"$searchQuery\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        when (val state = searchServicesState) {
                            is Resource.Loading -> item { SearchLoading() }
                            is Resource.Success -> {
                                if (state.data.isEmpty()) {
                                    item { EmptySearchResults(query = searchQuery) }
                                } else {
                                    items(state.data) { service ->
                                        NearbyShopItemCard(item = service) {
                                            navController.navigate("service_details_customer/${service.itemId}")
                                        }
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
                        item {
                            ServiceCategoriesRow(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = {
                                    selectedCategory = it
                                }
                            )
                        }

                        item {
                            SectionHeader(
                                title = "Popular Services Near You",
                                onViewAllClick = { }
                            )
                        }

                        when (val state = nearbyServiceItemsState) {
                            is Resource.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
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
                            }
                            is Resource.Success -> {
                                val filteredServiece = if (selectedCategory == "All") {
                                    state.data
                                } else {
                                    state.data.filter { it.category == selectedCategory }
                                }

                                if (filteredServiece.isEmpty()) {
                                    item {
                                        NoShopsAvailableCard(
                                            selectedCategory = selectedCategory,
                                            hasLocation = userLocation != null,
                                            onRefresh = { triggerLocationRefresh() }
                                        )
                                    }
                                } else {
                                    items(filteredServiece) { service ->
                                        NearbyShopItemCard(
                                            item = service,
                                            onClick = {
                                                navController.navigate("service_details_customer/${service.itemId}")
                                            }
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                item {
                                    ErrorCard(
                                        message = state.message,
                                        onRetry = { triggerLocationRefresh() }
                                    )
                                }
                            }
                            is Resource.Idle -> {
                                item {
                                    if (userLocation == null && !isLocationLoading) {
                                        LocationRequiredCard(
                                            onEnableLocation = {
                                                triggerLocationRefresh()
                                            }
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
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
    // Keyboard hatane ke liye focus manager
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = {
            Text(
                text = "Search for services (plumber, salon...)",
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(0.75f)
            )
        },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = {
                    onClear()
                    focusManager.clearFocus() // Clear dabane par keyboard hide ho jayega
                }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus() // Search dabane par keyboard hide ho jayega
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun ServiceCategoriesRow(categories: List<String>,selectedCategory: String, onCategorySelected: (String) -> Unit) {
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
                                "automotive" -> Icons.Default.CarRepair
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
fun ServiceShopCard(shop: Shop,userLocation: android.location.Location?,onClick: () -> Unit) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.shopName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                        Surface(
                            color = if (shop.open) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFE53935).copy(alpha = 0.1f),
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

            Text(
                text = shop.category,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.small
            ) {
                Text(text = "Book Service", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ErrorCard(message: String?, onRetry: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message ?: "Something went wrong", color = MaterialTheme.colorScheme.onErrorContainer, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun NoShopsAvailableCard(selectedCategory: String, hasLocation: Boolean, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (selectedCategory == "All") "No Services Available" else "No $selectedCategory Services",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasLocation) "Sorry, there are no service providers available in your area right now. Please try again later or check nearby areas." else "We couldn't determine your location. Please enable location to find services near you.",
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh")
            }
        }
    }
}

@Composable
fun LocationRequiredCard(onEnableLocation: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocationOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Location Required", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Please enable location services to discover service providers near you.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onEnableLocation, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enable Location")
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onViewAllClick) { Text("View All", fontSize = 13.sp) }
    }
}

@Composable
fun SearchLoading() {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun EmptySearchResults(query: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No results found for \"$query\"", fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SearchError(message: String?, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Error: $message", color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun ServiceSearchResultCard(service: com.saurabh.onecornersystem.data.model.ShopItem, onBookClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold)
                Text(service.category, fontSize = 12.sp, color = Color.Gray)
                Text("₹${service.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onBookClick) { Text("Book") }
        }
    }
}