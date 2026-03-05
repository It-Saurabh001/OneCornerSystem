package com.saurabh.onecornersystem.presentation.navigation

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.auth.LoginScreen
import com.saurabh.onecornersystem.presentation.auth.RegisterScreen
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.common.ProfileScreen
import com.saurabh.onecornersystem.presentation.customer.CustomerHomeScreen
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.shopowner.AddProductScreen
import com.saurabh.onecornersystem.presentation.shopowner.AddServiceScreen
import com.saurabh.onecornersystem.presentation.shopowner.CreateShopScreen
import com.saurabh.onecornersystem.presentation.shopowner.EditProductScreen
import com.saurabh.onecornersystem.presentation.shopowner.EditServiceScreen
import com.saurabh.onecornersystem.presentation.shopowner.EditShopScreen
import com.saurabh.onecornersystem.presentation.shopowner.ProductListScreen
import com.saurabh.onecornersystem.presentation.shopowner.ServiceDetailsScreen
import com.saurabh.onecornersystem.presentation.shopowner.ServiceListScreen
import com.saurabh.onecornersystem.presentation.shopowner.ShopOwnerHomeScreen
import com.saurabh.onecornersystem.presentation.shopowner.ShopOwnerHomeScreen1
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.presentation.splash.SplashScreen
import com.saurabh.onecornersystem.utils.Resource


@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel,
    customerShopViewModel: CustomerShopViewModel,
    shopViewModel: ShopViewModel
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    val userRole by authViewModel.userRole.collectAsState(initial = null)
    val currentUser by authViewModel.currentUser.collectAsState()

    Log.d("AppNavGraph", "isLoggedIn: $isLoggedIn, userRole: $userRole, currentUser: $currentUser")

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ============= SPLASH =============
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

        // ============= AUTH =============
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

        // ============= CUSTOMER HOME =============
        composable(Screen.CustomerHome.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            Log.d("NavGraph_CustomerHome", "CustomerHome Screen displayed - userId: ${currentUser?.userId}")

            CustomerHomeScreen(
                currentUser = currentUser,
                onShopClick = { shopId ->
                    Log.d("NavGraph_CustomerHome", "Shop clicked - shopId: $shopId")
                    navController.navigate(Screen.ShopDetails.passShopId(shopId))
                },
                onCartClick = {
                    Log.d("NavGraph_CustomerHome", "Cart clicked")
                    navController.navigate(Screen.Cart.route)
                },
                onOrdersClick = {
                    Log.d("NavGraph_CustomerHome", "Orders clicked")
                    navController.navigate(Screen.Orders.route)
                },
                onProfileClick = {
                    Log.d("NavGraph_CustomerHome", "Profile clicked")
                    navController.navigate(Screen.Profile.route)
                },
                onProfileDrawerClick = {
                    Log.d("NavGraph_CustomerHome", "Profile drawer clicked")
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = { Log.d("NavGraph_CustomerHome", "Settings clicked") },
                onAboutClick = { Log.d("NavGraph_CustomerHome", "About clicked") },
                onThemeClick = { Log.d("NavGraph_CustomerHome", "Theme clicked") },
                onContactClick = { Log.d("NavGraph_CustomerHome", "Contact clicked") }
            )
        }

        // ============= SHOP OWNER HOME =============
        composable(Screen.ShopOwnerHome.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val ownerId = currentUser?.userId ?: ""
            Log.d("NavGraph_ShopOwnerHome", "ShopOwnerHome Screen displayed - ownerId: $ownerId")

            ShopOwnerHomeScreen1(
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
            val myShopState by shopViewModel.myShopState.collectAsState()

            Log.d("NavGraph_EditShop", "EditShop Screen - shopId: $shopId, state: ${myShopState.javaClass.simpleName}")

            LaunchedEffect(shopId) {
                if (shopId.isNotEmpty()) {
                    Log.d("NavGraph_EditShop", "Fetching shop data for shopId: $shopId")
                    shopViewModel.getShopById(shopId)
                }
            }

            when (val state = myShopState) {
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
                                shopViewModel.getShopById(shopId)
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
            // You'll need to pass the product object - this is a placeholder
            // Ideally fetch product from ViewModel using itemId
            EditProductScreen(
                product = ShopItem(itemId = itemId), // Replace with actual product
                navController = navController
            )
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
            // You'll need to pass the service object - this is a placeholder
            EditServiceScreen(
                service = ShopItem(itemId = itemId), // Replace with actual service
                navController = navController
            )
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
            Log.d("NavGraph_Profile", "Profile Screen displayed - userId: ${currentUser?.userId}")

            if (currentUser != null) {
                ProfileScreen(
                    user = currentUser!!,
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
                    }
                )
            } else {
                Log.d("NavGraph_Profile", "Current user is null")
                PlaceholderScreen(title = "Profile") {
                    navController.popBackStack()
                }
            }
        }

        // ============= SHOP DETAILS =============
        composable(
            route = Screen.ShopDetails.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            Log.d("NavGraph_ShopDetails", "ShopDetails Screen displayed - shopId: $shopId")
            // ShopDetailsScreen(shopId = shopId)
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


sealed class Screen(val route: String) {
    // Splash & Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Customer
    object CustomerHome : Screen("customer_home")
    object ShopDetails : Screen("shop_details/{shopId}") {
        fun passShopId(shopId: String) = "shop_details/$shopId"
    }
    object ProductDetails : Screen("product_details/{productId}") {
        fun passProductId(productId: String) = "product_details/$productId"
    }
    object ServiceDetails : Screen("service_details/{itemId}") {
        fun passItemId(itemId: String) = "service_details/$itemId"
    }

    // Shop Owner
    object ShopOwnerHome : Screen("shop_owner_home")
    object CreateShop : Screen("create_shop")
    object EditShop : Screen("edit_shop/{shopId}") {
        fun passShopId(shopId: String) = "edit_shop/$shopId"
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

    // Common
    object Profile : Screen("profile")
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Chat : Screen("chat/{chatId}") {
        fun passChatId(chatId: String) = "chat/$chatId"
    }
}

