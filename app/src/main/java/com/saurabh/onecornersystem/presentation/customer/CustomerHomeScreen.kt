package com.saurabh.onecornersystem.presentation.customer

import android.Manifest
import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.saurabh.onecornersystem.data.model.CategoryWithType
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.presentation.common.AppNavigationDrawer
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.utils.LocationUtils
import com.saurabh.onecornersystem.utils.Resource
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

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
    val isDrawerOpen = drawerState.targetValue != DrawerValue.Closed

    val blurRadius by animateDpAsState(
        targetValue = if(isDrawerOpen) 15.dp else 0.dp,
        label = "blur Animation"
    )


    // UI States
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isSearching by remember { mutableStateOf(false) }

    // Liquid Colors Palette
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.15f)

    // ViewModel States
    val userLocation by viewModel.userLocation.collectAsState()
    var isLocationLoading by remember { mutableStateOf(false) }
    val nearbyServiceItemsState by viewModel.nearbyServiceItemsState.collectAsState()
    val serviceCategoriesState by viewModel.serviceCategoriesState.collectAsState()
    val searchServicesState by viewModel.searchServicesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Location Logic
    val gpsResolutionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                isLocationLoading = true
                val freshLoc = LocationUtils.getFreshCurrentLocation(context)
                isLocationLoading = false
                viewModel.updateUserLocation(freshLoc)
                freshLoc?.let { viewModel.getNearbyServiceItems(it.latitude, it.longitude) }
            }
        }
    }

    var triggerLocationRefresh: () -> Unit = {}

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) triggerLocationRefresh()
    }

    triggerLocationRefresh = {
        if (LocationUtils.hasLocationPermission(context)) {
            LocationUtils.checkLocationSettings(context, onResolutionRequired = { intentSender ->
                gpsResolutionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }, onAlreadySatisfied = {
                isLocationLoading = true
                scope.launch {
                    val loc = LocationUtils.getFreshCurrentLocation(context) ?: LocationUtils.getCurrentLocation(context)
                    isLocationLoading = false
                    viewModel.updateUserLocation(loc)
                    loc?.let { viewModel.getNearbyServiceItems(it.latitude, it.longitude) }
                }
            })
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(Unit) {
        if (userLocation == null) triggerLocationRefresh()
        viewModel.getServiceCategories()
    }

    val categories = remember { mutableStateListOf<String>().apply { add("All") } }
    LaunchedEffect(serviceCategoriesState) {
        if (serviceCategoriesState is Resource.Success) {
            categories.clear()
            categories.add("All")
            categories.addAll((serviceCategoriesState as Resource.Success).data.map { it.categoryName })
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = deepBlack.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.7f),
            ) {
                AppNavigationDrawer(
                    user = currentUser,
                    onProfileClick = { scope.launch { drawerState.close() }; navController.navigate("profile") },
                    onSettingsClick = { scope.launch { drawerState.close() } },
                    onAboutClick = { scope.launch { drawerState.close() } },
                    onThemeClick = { scope.launch { drawerState.close() } },
                    onContactClick = { scope.launch { drawerState.close() } }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize().blur(blurRadius),
                containerColor = deepBlack.copy(alpha = 0.95f),
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                        title = { Text("OneCorner", fontWeight = FontWeight.Black, fontSize = 24.sp) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Notifications, "Notifications", tint = electricBlue)
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
                                selected = label == "Home",
                                onClick = { if (label != "Home") navController.navigate(route) },
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
                Box(modifier = Modifier.fillMaxSize()) {
                    // --- LIQUID BLOBS (Electric Blue Glow) ---
                    Box(modifier = Modifier.size(300.dp).offset(x = (-50).dp, y = 100.dp).blur(120.dp).background(electricBlue.copy(alpha = 0.2f), CircleShape))
                    Box(modifier = Modifier.size(250.dp).align(Alignment.TopEnd).offset(x = 50.dp, y = (-20).dp).blur(100.dp).background(electricBlue.copy(alpha = 0.15f), CircleShape))

                    if (isLocationLoading || (isLoading && nearbyServiceItemsState is Resource.Loading)) {
                        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = electricBlue)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Liquid Search Bar
                            item {
                                LiquidSearchBar(
                                    query = searchQuery,
                                    onQueryChange = {
                                        searchQuery = it
                                        if (it.length >= 2) { isSearching = true; viewModel.searchServices(it) }
                                        else { isSearching = false; viewModel.clearSearchResults() }
                                    },
                                    blue = electricBlue,
                                    outline = outlineWhite
                                )
                            }

                            if (isSearching && searchQuery.isNotBlank()) {
                                item { Text("Search Results", color = Color.Gray, modifier = Modifier.padding(start = 20.dp), fontSize = 14.sp) }
                                when (val state = searchServicesState) {
                                    is Resource.Success -> {
                                        items(state.data) { service ->
                                            LiquidServiceCard(service, electricBlue, outlineWhite) {
                                                navController.navigate("service_details_customer/${service.itemId}")
                                            }
                                        }
                                    }
                                    else -> {}
                                }
                            } else {
                                // 2. Liquid Categories Row
                                item {
                                    LiquidCategoriesRow(
                                        categories = categories,
                                        selected = selectedCategory,
                                        blue = electricBlue,
                                        onCategorySelected = { selectedCategory = it }
                                    )
                                }

                                // 3. Section Header
                                item {
                                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Popular Services", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        TextButton(onClick = {}) { Text("View All", color = electricBlue) }
                                    }
                                }

                                // 4. Main Service List
                                when (val state = nearbyServiceItemsState) {
                                    is Resource.Success -> {
                                        val filtered = if (selectedCategory == "All") state.data else state.data.filter { it.category == selectedCategory }
                                        if (filtered.isEmpty()) {
                                            item { EmptyStateGlass(selectedCategory, electricBlue, outlineWhite) { triggerLocationRefresh() } }
                                        } else {
                                            items(filtered) { service ->
                                                LiquidServiceCard(service, electricBlue, outlineWhite) {
                                                    navController.navigate("service_details_customer/${service.itemId}")
                                                }
                                            }
                                        }
                                    }
                                    is Resource.Idle -> item { if(userLocation == null) LocationRequiredGlass(electricBlue, outlineWhite) { triggerLocationRefresh() } }
                                    is Resource.Error -> item { ErrorCardGlass(state.message, electricBlue, outlineWhite) { triggerLocationRefresh() } }
                                    else -> {}
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }
                    }
                }
            }
        }

    }
}

// --- LIQUID REUSABLE COMPONENTS ---

@Composable
fun LiquidSearchBar(query: String, onQueryChange: (String) -> Unit, blue: Color, outline: Color) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().height(56.dp).border(1.dp, outline, RoundedCornerShape(20.dp)),
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Default.Search, null, tint = blue)
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search services...", color = Color.Gray, fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }
    }
}

@Composable
fun LiquidCategoriesRow(categories: List<String>, selected: String, blue: Color, onCategorySelected: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(categories) { category ->
            val isSel = category == selected
            Surface(
                onClick = { onCategorySelected(category) },
                modifier = Modifier.height(42.dp).border(1.dp, if (isSel) blue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                color = if (isSel) blue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (category != "All") {
                        Icon(
                            imageVector = when(category.lowercase()) {
                                "automotive" -> Icons.Default.CarRepair
                                "plumbing" -> Icons.Default.Plumbing
                                "cleaning" -> Icons.Default.CleaningServices
                                else -> Icons.Default.Build
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSel) Color.White else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(category, color = if (isSel) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
fun LiquidServiceCard(service: ShopItem, blue: Color, outline: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outline, Color.Transparent)), RoundedCornerShape(28.dp)),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Box
                Box(modifier = Modifier.size(64.dp).background(blue.copy(alpha = 0.1f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Build, null, tint = blue, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(service.category, color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${service.price}", color = blue, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        if (service.duration.isNotBlank()) {
                            Text(" • ${service.duration}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
            }

            // Home Service Badge
            if (service.homeService) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("🏠 HOME SERVICE AVAILABLE", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

// --- STATE CARDS ---

@Composable
fun LocationRequiredGlass(blue: Color, outline: Color, onRetry: () -> Unit) {
    Surface(modifier = Modifier.padding(20.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)), color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocationOff, null, tint = blue, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Location Required", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Enable GPS to find expert services in your area.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = blue), shape = RoundedCornerShape(12.dp)) { Text("Allow Location") }
        }
    }
}

@Composable
fun ErrorCardGlass(msg: String?, blue: Color, outline: Color, onRetry: () -> Unit) {
    Surface(modifier = Modifier.padding(20.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)), color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(msg ?: "Oops! Connection lost", color = Color.White)
            TextButton(onClick = onRetry) { Text("RETRY", color = blue) }
        }
    }
}

@Composable
fun EmptyStateGlass(cat: String, blue: Color, outline: Color, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.SearchOff, null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("No $cat services near you", color = Color.White, fontWeight = FontWeight.Medium)
        TextButton(onClick = onRetry) { Text("Refresh Area", color = blue) }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LiquidHomePreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            // Preview logic here
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                item { LiquidSearchBar("", {}, Color(0xFF2979FF), Color.White.copy(alpha = 0.1f)) }
                item { Spacer(modifier = Modifier.height(20.dp)) }
                item {
                    LiquidServiceCard(
                        ShopItem(name = "Electrician Pro", category = "Electrical", price = 250.0, duration = "45 mins", homeService = true, available = true),
                        Color(0xFF2979FF),
                        Color.White.copy(alpha = 0.1f)
                    ) {}
                }
            }
        }
    }
}