package com.saurabh.onecornersystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.navigation.AppNavGraph
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.ui.theme.OneCornerSystemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        setContent {
            OneCornerSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialize all ViewModels
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val customerShopViewModel: CustomerShopViewModel = hiltViewModel()
                    val shopViewModel: ShopViewModel = hiltViewModel()

                    // Pass all ViewModels to AppNavGraph
                    // This will show the custom Compose splash screen
                    AppNavGraph(
                        authViewModel = authViewModel,
                        customerShopViewModel = customerShopViewModel,
                        shopViewModel = shopViewModel
                    )
                }
            }
        }
    }
}
