package com.saurabh.onecornersystem.presentation.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.auth.LoginScreen
import com.saurabh.onecornersystem.presentation.auth.RegisterScreen
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.common.ChatViewModel
import com.saurabh.onecornersystem.presentation.common.ProfileScreen
import com.saurabh.onecornersystem.presentation.components.ChatColors
import com.saurabh.onecornersystem.presentation.customer.*
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.shopowner.*
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.presentation.splash.SplashScreen
import com.saurabh.onecornersystem.utils.Resource

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    customerShopViewModel: CustomerShopViewModel,
    shopViewModel: ShopViewModel,
    chatViewModel: ChatViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    val userRole by authViewModel.userRole.collectAsState(initial = null)
    val currentUser by authViewModel.currentUser.collectAsState()

    Log.d("AppNavGraph", "isLoggedIn: $isLoggedIn, userRole: $userRole, currentUser: $currentUser")

    // This ensures new chat documents store the actual user name, not "Customer"
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            chatViewModel.updateUserInfo(name = user.name, image = user.profileImage)
            Log.d("AppNavGraph", "💬 Chat user info synced: name=${user.name}")
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            Log.d("NavGraph_Splash", "Splash Screen displayed - isLoggedIn: $isLoggedIn, userRole: $userRole")
            SplashScreen(
                onNavigateToHome = {
                    if (isLoggedIn && userRole != null) {
                        when (userRole) {
                            "customer" -> {
                                Log.d("NavGraph_Splash", "Navigating to CustomerHome")
                                navController.navigate(Screen.CustomerHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            "shop_owner" -> {
                                Log.d("NavGraph_Splash", "Navigating to ShopOwnerHome")
                                navController.navigate(Screen.ShopOwnerHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        Log.d("NavGraph_Splash", "Navigating to Login (not logged in)")
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            Log.d("NavGraph_Login", "Login Screen displayed")
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    Log.d("NavGraph_Login", "Navigating to Register")
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { role ->
                    Log.d("NavGraph_Login", "Login success - role: $role")
                    when (role) {
                        "customer" -> {
                            Log.d("NavGraph_Login", "Navigating to CustomerHome")
                            navController.navigate(Screen.CustomerHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "shop_owner" -> {
                            Log.d("NavGraph_Login", "Navigating to ShopOwnerHome")
                            navController.navigate(Screen.ShopOwnerHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            Log.d("NavGraph_Register", "Register Screen displayed")
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    Log.d("NavGraph_Register", "Navigating back from Register")
                    navController.popBackStack()
                },
                onRegisterSuccess = { role ->
                    Log.d("NavGraph_Register", "Register success - role: $role")
                    when (role) {
                        "customer" -> {
                            Log.d("NavGraph_Register", "Navigating to CustomerHome")
                            navController.navigate(Screen.CustomerHome.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                        "shop_owner" -> {
                            Log.d("NavGraph_Register", "Navigating to ShopOwnerHome")
                            navController.navigate(Screen.ShopOwnerHome.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // ============= CUSTOMER SCREENS =============

        // Customer Home
        composable(Screen.CustomerHome.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            Log.d("NavGraph_CustomerHome", "CustomerHome Screen displayed - userId: ${currentUser?.userId}")

            CustomerHomeScreen(
                navController = navController,
                currentUser = currentUser,
                viewModel = customerShopViewModel
            )
        }

        // Service Shop Details
        composable(
            route = Screen.ServiceShop.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_ServiceShop", "ServiceShop Screen displayed - shopId: $shopId")

            ServiceShopDetailsScreen(
                shopId = shopId,
                navController = navController,
                shopViewModel = customerShopViewModel
            )
        }

        // Booking Form
        composable(
            route = Screen.BookingForm.route,
            arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
            Log.d("NavGraph_BookingForm", "BookingForm Screen displayed - serviceId: $serviceId")

            BookingFormScreen(
                serviceId = serviceId,
                navController = navController,
                customerViewModel = customerShopViewModel,
                authViewModel = authViewModel
            )
        }

        // My Bookings - FIXED: Pass viewModel and authViewModel
        composable(Screen.MyBookings.route) {
            Log.d("NavGraph_MyBookings", "MyBookings Screen displayed")

            MyBookingsScreen(
                navController = navController,
                viewModel = customerShopViewModel,
                authViewModel = authViewModel
            )
        }

        // Booking Details - FIXED: Pass viewModel and authViewModel
        composable(
            route = Screen.BookingDetails.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            Log.d("NavGraph_BookingDetails", "BookingDetails Screen displayed - bookingId: $bookingId")

            BookingDetailsScreen(
                bookingId = bookingId,
                navController = navController,
                viewModel = customerShopViewModel,
                authViewModel = authViewModel
            )
        }

        // Favorites
        composable(Screen.Favorites.route) {
            Log.d("NavGraph_Favorites", "Favorites Screen displayed")

            FavoritesScreen(
                navController = navController,
                viewModel = customerShopViewModel
            )
        }

        // Search Results
        composable(
            route = Screen.SearchResults.route,
            arguments = listOf(
                navArgument("query") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            val type = backStackEntry.arguments?.getString("type") ?: "SERVICE"
            Log.d("NavGraph_SearchResults", "SearchResults Screen displayed - query: $query, type: $type")

            PlaceholderScreen(title = "Search Results for '$query'") {
                navController.popBackStack()
            }
        }

        // All Services
        composable(Screen.AllServices.route) {
            Log.d("NavGraph_AllServices", "All Services Screen displayed")
            PlaceholderScreen(title = "All Services") {
                navController.popBackStack()
            }
        }

        // ============= SHOP OWNER HOME =============
        composable(Screen.ShopOwnerHome.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val ownerId = currentUser?.userId ?: ""
            Log.d("NavGraph_ShopOwnerHome", "ShopOwnerHome Screen displayed - ownerId: $ownerId")

            ShopOwnerHomeScreen(
                navController = navController,
                ownerId = ownerId
            )
        }

        // ============= SHOP CREATION & EDIT =============
        composable(Screen.CreateShop.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val shopType = currentUser?.shopType ?: ShopType.PRODUCT
            Log.d("NavGraph_CreateShop", "CreateShop Screen displayed - ownerId: ${currentUser?.userId}, shopType: $shopType")

            CreateShopScreen(
                navController = navController,
                ownerId = currentUser?.userId ?: "",
                shopType = shopType
            )
        }

        composable(
            route = Screen.EditShop.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            val shopDetailsState by shopViewModel.shopDetailsState.collectAsState()

            Log.d("NavGraph_EditShop", "EditShop Screen - shopId: $shopId, state: ${shopDetailsState.javaClass.simpleName}")

            LaunchedEffect(shopId) {
                if (shopId.isNotEmpty()) {
                    Log.d("NavGraph_EditShop", "Fetching shop data for shopId: $shopId")
                    shopViewModel.getShopDetails(shopId)
                    shopViewModel.listenToShopDetails(shopId)
                }
            }

            when (val state = shopDetailsState) {
                is Resource.Loading -> {
                    Log.d("NavGraph_EditShop", "Loading shop data")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    state.data?.let { shop ->
                        Log.d("NavGraph_EditShop", "Shop loaded - name: ${shop.shopName}, id: ${shop.shopId}")
                        EditShopScreen(
                            shop = shop,
                            navController = navController
                        )
                    }
                }
                is Resource.Error -> {
                    Log.d("NavGraph_EditShop", "Error loading shop - ${state.message}")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                Log.d("NavGraph_EditShop", "Retry clicked")
                                shopViewModel.getShopDetails(shopId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Log.d("NavGraph_EditShop", "Unknown state")
                }
            }
        }

        // My Shop Details Screen
        composable(
            route = Screen.MyShopDetails.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            val shopDetailsState by shopViewModel.shopDetailsState.collectAsState()

            Log.d("NavGraph_MyShopDetails", "MyShopDetails Screen - shopId: $shopId")

            LaunchedEffect(shopId) {
                if (shopId.isNotEmpty()) {
                    Log.d("NavGraph_MyShopDetails", "Fetching shop data for shopId: $shopId")
                    shopViewModel.getShopDetails(shopId)
                }
            }

            when (val state = shopDetailsState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    state.data?.let { shop ->
                        Log.d("NavGraph_MyShopDetails", "Shop loaded - name: ${shop.shopName}")
                        ShopDetailsScreen(
                            shop = shop,
                            navController = navController
                        )
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                shopViewModel.getShopDetails(shopId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // ============= ORDER MANAGEMENT - FIXED =============
        composable(
            route = Screen.OrdersByShop.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_OrdersByShop", "📦 Order Management Screen displayed - shopId: $shopId")

            // FIXED: Use OrderManagementScreen instead of PlaceholderScreen
            OrderManagementScreen(
                shopId = shopId,
                navController = navController,
                viewModel = shopViewModel
            )
        }

        // Shop Booking Details
        composable(
            route = Screen.ShopBookingDetails.route,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            Log.d("NavGraph_ShopBookingDetails", "Shop Booking Details Screen displayed - bookingId: $bookingId")

            ShopBookingDetailsScreen(
                bookingId = bookingId,
                navController = navController,
                viewModel = shopViewModel
            )
        }

        // ============= PRODUCT ROUTES =============
        composable(
            route = Screen.ProductList.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_ProductList", "ProductList Screen displayed - shopId: $shopId")
            ProductListScreen(
                shopId = shopId,
                navController = navController
            )
        }

        composable(
            route = Screen.AddProduct.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_AddProduct", "AddProduct Screen displayed - shopId: $shopId")
            AddProductScreen(
                shopId = shopId,
                navController = navController
            )
        }

        composable(
            route = Screen.EditProduct.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            Log.d("NavGraph_EditProduct", "EditProduct Screen displayed - itemId: $itemId")

            val shopItemViewModel: ShopItemViewModel = hiltViewModel()
            val itemState by shopItemViewModel.itemState.collectAsState()

            LaunchedEffect(itemId) {
                if (itemId.isNotEmpty()) {
                    Log.d("NavGraph_EditProduct", "Fetching product data for itemId: $itemId")
                    shopItemViewModel.getItemById(itemId)
                }
            }

            when (val state = itemState) {
                is Resource.Loading -> {
                    Log.d("NavGraph_EditProduct", "Loading product data")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val product = state.data
                    Log.d("NavGraph_EditProduct", "Product loaded - name: ${product.name}, id: ${product.itemId}")
                    EditProductScreen(
                        product = product,
                        navController = navController
                    )
                }
                is Resource.Error -> {
                    Log.d("NavGraph_EditProduct", "Error loading product - ${state.message}")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                shopItemViewModel.getItemById(itemId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Log.d("NavGraph_EditProduct", "Idle state - fetching product")
                    LaunchedEffect(Unit) {
                        shopItemViewModel.getItemById(itemId)
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // ============= SERVICE ROUTES =============
        composable(
            route = Screen.ServiceList.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_ServiceList", "ServiceList Screen displayed - shopId: $shopId")
            ServiceListScreen(
                shopId = shopId,
                navController = navController
            )
        }

        composable(
            route = Screen.AddService.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_AddService", "AddService Screen displayed - shopId: $shopId")
            AddServiceScreen(
                shopId = shopId,
                navController = navController
            )
        }

        composable(
            route = Screen.EditService.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            Log.d("NavGraph_EditService", "EditService Screen displayed - itemId: $itemId")

            val shopItemViewModel: ShopItemViewModel = hiltViewModel()
            val itemState by shopItemViewModel.itemState.collectAsState()

            LaunchedEffect(itemId) {
                if (itemId.isNotEmpty()) {
                    Log.d("NavGraph_EditService", "Fetching service data for itemId: $itemId")
                    shopItemViewModel.getItemById(itemId)
                }
            }

            when (val state = itemState) {
                is Resource.Loading -> {
                    Log.d("NavGraph_EditService", "Loading service data")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val service = state.data
                    Log.d("NavGraph_EditService", "Service loaded - name: ${service.name}, id: ${service.itemId}")
                    EditServiceScreen(
                        service = service,
                        navController = navController
                    )
                }
                is Resource.Error -> {
                    Log.d("NavGraph_EditService", "Error loading service - ${state.message}")
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                shopItemViewModel.getItemById(itemId)
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Log.d("NavGraph_EditService", "Idle state - fetching service")
                    LaunchedEffect(Unit) {
                        shopItemViewModel.getItemById(itemId)
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        composable(
            route = Screen.ServiceDetails.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            Log.d("NavGraph_ServiceDetails", "ServiceDetails Screen displayed - itemId: $itemId")
            ServiceDetailsScreen(
                serviceId = itemId,
                navController = navController
            )
        }
        composable(
            route = Screen.ServiceDetailsCustomer.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            backstaskEntry ->
            val itemId = backstaskEntry.arguments?.getString("itemId") ?: ""
            Log.d("NavGraph_ServiceDetailsCustomer", "ServiceDetailsCustomer Screen displayed - itemId")
            ServiceDetailScreenCustomer(
                serviceId = itemId,
                navController = navController,
                viewModel = customerShopViewModel
            )
        }

        composable(Screen.CustomerChatList.route) {
            CustomerChatListScreen(navController = navController, chatViewModel)
        }

        // Customer Chat — receives shopId/shopName/bookingId as nav args
        // so the screen itself (not the caller) calls startChat* on the correct VM
        composable(
            route = Screen.CustomerChat.route,
            arguments = listOf(
                navArgument("bookingId") { type = NavType.StringType; defaultValue = "" },
                navArgument("shopId")    { type = NavType.StringType; defaultValue = "" },
                navArgument("shopName")  { type = NavType.StringType; defaultValue = "" },
                navArgument("shopImage") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
            val shopId    = backStackEntry.arguments?.getString("shopId")    ?: ""
            val shopName  = Uri.decode(backStackEntry.arguments?.getString("shopName")  ?: "")
            val shopImage = Uri.decode(backStackEntry.arguments?.getString("shopImage") ?: "")
            Log.d("NavGraph_CustomerChat", "CustomerChat opened — bookingId=$bookingId shopId=$shopId shopName=$shopName")
            CustomerChatScreen(
                navController = navController,
                viewModel     = chatViewModel,
                bookingId     = bookingId,
                shopId        = shopId,
                shopName      = shopName,
                shopImage     = shopImage
            )
        }

        composable(Screen.ShopChatList.route) {
            // Ensure the shop is loaded before rendering the list screen.
            // getMyShop is idempotent in ShopViewModel — calling it again when
            // myShopState is already Success is a no-op.
            LaunchedEffect(currentUser?.userId) {
                currentUser?.userId?.takeIf { it.isNotBlank() }?.let {
                    Log.d("NavGraph", "🏪 ShopChatList: calling getMyShop(userId=$it)")
                    shopViewModel.getMyShop(it)
                }
            }
            val myShopState by shopViewModel.myShopState.collectAsState()
            val shopId = (myShopState as? Resource.Success)?.data?.shopId ?: ""

            // Only render the screen once shopId is resolved.
            // This eliminates the need for the screen to do its own shop lookup.
            if (shopId.isBlank()) {
                Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChatColors.AmberOrange)
                }
                Log.d("NavGraph", "🏪 ShopChatList: waiting for shopId (myShopState=${myShopState::class.simpleName})")
            } else {
                Log.d("NavGraph", "🏪 ShopChatList: shopId resolved='$shopId', rendering screen")
                ShopChatListScreen(navController = navController, viewModel = chatViewModel, shopId = shopId)
            }
        }

// Shop Chat
        composable(
            route = Screen.ShopChat.route,
            arguments = listOf(
                navArgument("shopId")      { type = NavType.StringType },
                navArgument("shopName")    { type = NavType.StringType },
                navArgument("customerId")  { type = NavType.StringType },
                navArgument("customerName") { type = NavType.StringType },
                navArgument("bookingId")   { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val shopId       = backStackEntry.arguments?.getString("shopId") ?: ""
            val shopName     = backStackEntry.arguments?.getString("shopName")?.let { android.net.Uri.decode(it) } ?: ""
            val customerId   = backStackEntry.arguments?.getString("customerId") ?: ""
            val customerName = backStackEntry.arguments?.getString("customerName")?.let { android.net.Uri.decode(it) } ?: ""
            val bookingId    = backStackEntry.arguments?.getString("bookingId") ?: ""

            Log.d("NavGraph_ShopChat", "ShopChat opened — shop=$shopName customer=$customerName bookingId=$bookingId")

            ShopChatScreen(
                navController = navController,
                shopId        = shopId,
                shopName      = shopName,
                customerId    = customerId,
                customerName  = customerName,
                bookingId     = bookingId,
                viewModel     = chatViewModel
            )
        }

        // ============= COMMON SCREENS =============
        composable(Screen.Cart.route) {
            Log.d("NavGraph_Cart", "Cart Screen displayed")
            PlaceholderScreen(title = "Shopping Cart") {
                navController.popBackStack()
            }
        }

        composable(Screen.Orders.route) {
            Log.d("NavGraph_Orders", "Orders Screen displayed")
            PlaceholderScreen(title = "My Orders") {
                navController.popBackStack()
            }
        }

        composable(Screen.Profile.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val myShopState by shopViewModel.myShopState.collectAsState()

            Log.d("NavGraph_Profile", "Profile Screen displayed - userId: ${currentUser?.userId}, role: ${currentUser?.role}")

            LaunchedEffect(currentUser) {
                if (currentUser?.role == "shop_owner" && currentUser?.userId?.isNotEmpty() == true) {
                    Log.d("NavGraph_Profile", "Fetching shop for owner: ${currentUser?.userId}")
                    shopViewModel.getMyShop(currentUser!!.userId)
                }
            }

            if (currentUser != null) {
                val shop = when (val state = myShopState) {
                    is Resource.Success -> state.data
                    else -> null
                }

                ProfileScreen(
                    user = currentUser!!,
                    shop = shop,
                    onBackClick = {
                        Log.d("NavGraph_Profile", "Back clicked")
                        navController.popBackStack()
                    },
                    onEditClick = {
                        Log.d("NavGraph_Profile", "Edit clicked")
                    },
                    onLogoutClick = {
                        Log.d("NavGraph_Profile", "Logout clicked")
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onShopClick = {
                        shop?.let {
                            Log.d("NavGraph_Profile", "Shop clicked - navigating to shop details")
                            navController.navigate(Screen.MyShopDetails.passShopId(it.shopId))
                        }
                    },
                    onHomeClick = {
                        navController.navigate(Screen.CustomerHome.route) {
                            popUpTo(Screen.CustomerHome.route) { inclusive = true }
                        }
                    },
                    onBookingsClick = {
                        navController.navigate(Screen.MyBookings.route)
                    },
                    onFavoritesClick = {
                        navController.navigate(Screen.Favorites.route)
                    },
                )
            } else {
                Log.d("NavGraph_Profile", "Current user is null")
                PlaceholderScreen(title = "Profile") {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Log.d("NavGraph_PlaceholderScreen", "PlaceholderScreen displayed - title: $title")
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp))
        Button(onClick = onBackClick) {
            Text("Back")
        }
    }
}

// ============= SCREEN ROUTES =============
sealed class Screen(val route: String) {
    // Splash & Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Customer Screens
    object CustomerHome : Screen("customer_home")
    object ServiceShop : Screen("service_shop/{shopId}") {
        fun passShopId(shopId: String) = "service_shop/$shopId"
    }
    object BookingForm : Screen("booking_form/{serviceId}") {
        fun passServiceId(serviceId: String) = "booking_form/$serviceId"
    }
    object MyBookings : Screen("my_bookings")
    object BookingDetails : Screen("booking_details/{bookingId}") {
        fun passBookingId(bookingId: String) = "booking_details/$bookingId"
    }
    object Favorites : Screen("favorites")
    object SearchResults : Screen("search_results?query={query}&type={type}") {
        fun passQuery(query: String, type: String) = "search_results?query=$query&type=$type"
    }
    object AllServices : Screen("all_services")

    // Customer Chat Routes
    object CustomerChatList : Screen("customer_chat_list")
    object CustomerChat : Screen("customer_chat?bookingId={bookingId}&shopId={shopId}&shopName={shopName}&shopImage={shopImage}") {
        /** Build a navigation URI — all params are URL-encoded. */
        fun navigate(
            shopId: String,
            shopName: String,
            shopImage: String = "",
            bookingId: String = ""
        ) = "customer_chat?bookingId=${Uri.encode(bookingId)}&shopId=${Uri.encode(shopId)}&shopName=${Uri.encode(shopName)}&shopImage=${Uri.encode(shopImage)}"
    }

    // Shop Owner Chat Routes
    object ShopChatList : Screen("shop_chat_list")
    object ShopChat : Screen("shop_chat/{shopId}/{shopName}/{customerId}/{customerName}?bookingId={bookingId}") {
        fun passArgs(
            shopId: String,
            shopName: String,
            customerId: String,
            customerName: String,
            bookingId: String = ""
        ): String {
            return "shop_chat/$shopId/${Uri.encode(shopName)}/$customerId/${Uri.encode(customerName)}?bookingId=${Uri.encode(bookingId)}"
        }
    }
    // Customer Product Screens
    object ShopDetails : Screen("shop_details/{shopId}") {
        fun passShopId(shopId: String) = "shop_details/$shopId"
    }
    object ProductDetails : Screen("product_details/{productId}") {
        fun passProductId(productId: String) = "product_details/$productId"
    }
    object ServiceDetails : Screen("service_details/{itemId}") {
        fun passItemId(itemId: String) = "service_details/$itemId"
    }

    // Shop Owner Screens
    object ShopOwnerHome : Screen("shop_owner_home")
    object CreateShop : Screen("create_shop")
    object MyShopDetails : Screen("my_shop_details/{shopId}") {
        fun passShopId(shopId: String) = "my_shop_details/$shopId"
    }
    object EditShop : Screen("edit_shop/{shopId}") {
        fun passShopId(shopId: String) = "edit_shop/$shopId"
    }

    // Order Management
    object OrdersByShop : Screen("orders/{shopId}") {
        fun passShopId(shopId: String) = "orders/$shopId"
    }
    object ShopBookingDetails : Screen("shop_booking_details/{bookingId}") {
        fun passBookingId(bookingId: String) = "shop_booking_details/$bookingId"
    }

    // Products
    object ProductList : Screen("products/{shopId}") {
        fun passShopId(shopId: String) = "products/$shopId"
    }
    object AddProduct : Screen("add_product/{shopId}") {
        fun passShopId(shopId: String) = "add_product/$shopId"
    }
    object EditProduct : Screen("edit_product/{itemId}") {
        fun passItemId(itemId: String) = "edit_product/$itemId"
    }

    // Services
    object ServiceList : Screen("services/{shopId}") {
        fun passShopId(shopId: String) = "services/$shopId"
    }
    object AddService : Screen("add_service/{shopId}") {
        fun passShopId(shopId: String) = "add_service/$shopId"
    }
    object EditService : Screen("edit_service/{itemId}") {
        fun passItemId(itemId: String) = "edit_service/$itemId"
    }

    // Customer Service Details
    object ServiceDetailsCustomer : Screen("service_details_customer/{itemId}") {
        fun passItemId(itemId: String) = "service_details_customer/$itemId"
    }

    // Common
    object Profile : Screen("profile")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Chat : Screen("chat/{chatId}") {
        fun passChatId(chatId: String) = "chat/$chatId"
    }
}