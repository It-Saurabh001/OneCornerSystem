package com.saurabh.onecornersystem.presentation.navigation

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saurabh.onecornersystem.presentation.auth.LoginScreen
import com.saurabh.onecornersystem.presentation.auth.RegisterScreen
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.customer.CustomerHomeScreen
import com.saurabh.onecornersystem.presentation.shopowner.ShopOwnerHomeScreen
import com.saurabh.onecornersystem.presentation.common.ProfileScreen

sealed class Screen(val route: String) {
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

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { role ->
                    if (role == "customer") {
                        navController.navigate(Screen.CustomerHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.ShopOwnerHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = { role ->
                    if (role == "customer") {
                        navController.navigate(Screen.CustomerHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.ShopOwnerHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.CustomerHome.route) {
            CustomerHomeScreen(
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
                }
            )
        }

        composable(Screen.ShopOwnerHome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ShopOwnerHomeScreen(
                onAddProduct = {
                    // Navigate to add product
                },
                onViewOrders = {
                    navController.navigate(Screen.Orders.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
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
            val authViewModel: AuthViewModel = hiltViewModel()
            val currentUser by authViewModel.currentUser.collectAsState()

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
        Spacer(modifier = Modifier.fillMaxWidth().padding(16.dp))
        Button(onClick = onBackClick) {
            Text("Back")
        }
    }
}


