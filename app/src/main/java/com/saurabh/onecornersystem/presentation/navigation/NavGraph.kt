package com.saurabh.onecornersystem.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.saurabh.onecornersystem.presentation.auth.LoginScreen
import com.saurabh.onecornersystem.presentation.auth.RegisterScreen
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.common.ProfileScreen
import com.saurabh.onecornersystem.presentation.customer.CustomerHomeScreen
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.shopowner.ShopOwnerHomeScreen
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.presentation.splash.SplashScreen


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

    // Debug logging
    Log.d("AppNavGraph", "isLoggedIn: $isLoggedIn, userRole: $userRole, currentUser: $currentUser")

    // Determine the start destination based on login status
    val startDestination = Screen.Splash.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    // After splash, check login status
                    if (isLoggedIn && userRole != null) {
                        when (userRole) {
                            "customer" -> {
                                navController.navigate(Screen.CustomerHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            "shop_owner" -> {
                                navController.navigate(Screen.ShopOwnerHome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { role ->
                    when (role) {
                        "customer" -> {
                            navController.navigate(Screen.CustomerHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "shop_owner" -> {
                            navController.navigate(Screen.ShopOwnerHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { role ->
                    when (role) {
                        "customer" -> {
                            navController.navigate(Screen.CustomerHome.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                        "shop_owner" -> {
                            navController.navigate(Screen.ShopOwnerHome.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.CustomerHome.route) {
            val currentUser by authViewModel.currentUser.collectAsState()

            CustomerHomeScreen(
                currentUser = currentUser,
                onShopClick = { shopId ->
                    navController.navigate(Screen.ShopDetails.passShopId(shopId))
                },
                onCartClick = {
                    navController.navigate(Screen.Cart.route)
                },
                onOrdersClick = {
                    navController.navigate(Screen.Orders.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProfileDrawerClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    // Navigate to settings
                },
                onAboutClick = {
                    // Navigate to about
                },
                onThemeClick = {
                    // Handle theme change
                },
                onContactClick = {
                    // Navigate to contact
                }
            )
        }

        composable(Screen.ShopOwnerHome.route) {
            val currentUser by authViewModel.currentUser.collectAsState()

            ShopOwnerHomeScreen(
                currentUser = currentUser,
                onAddProduct = {
                    // Navigate to add product
                },
                onViewOrders = {
                    navController.navigate(Screen.Orders.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onProfileDrawerClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onSettingsClick = {
                    // Navigate to settings
                },
                onAboutClick = {
                    // Navigate to about
                },
                onThemeClick = {
                    // Handle theme change
                },
                onContactClick = {
                    // Navigate to contact
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Cart.route) {
            // Cart Screen - placeholder
            PlaceholderScreen(title = "Shopping Cart") {
                navController.popBackStack()
            }
        }

        composable(Screen.Orders.route) {
            // Orders Screen - placeholder
            PlaceholderScreen(title = "My Orders") {
                navController.popBackStack()
            }
        }

        composable(Screen.Profile.route) {
            val currentUser by authViewModel.currentUser.collectAsState()

            Log.d("TAG", "AppNavGraph: $currentUser")
            if (currentUser != null) {
                ProfileScreen(
                    user = currentUser!!,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditClick = {
                        // Navigate to edit profile
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            } else {
                PlaceholderScreen(title = "Profile") {
                    navController.popBackStack()
                }
            }
        }

        composable(
            route = Screen.ShopDetails.route,
            arguments = listOf(navArgument("shopId") { type = NavType.StringType })
        ) { backStackEntry ->
            val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
            // ShopDetailsScreen(shopId = shopId)
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    onBackClick: () -> Unit
) {
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
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object CustomerHome : Screen("customer_home")
    object ShopOwnerHome : Screen("shop_owner_home")
    object Profile : Screen("profile")
    object ShopDetails : Screen("shop_details/{shopId}") {
        fun passShopId(shopId: String) = "shop_details/$shopId"
    }
    object ProductDetails : Screen("product_details/{productId}") {
        fun passProductId(productId: String) = "product_details/$productId"
    }
    object Cart : Screen("cart")
    object Orders : Screen("orders")
    object Chat : Screen("chat/{chatId}") {
        fun passChatId(chatId: String) = "chat/$chatId"
    }
}


