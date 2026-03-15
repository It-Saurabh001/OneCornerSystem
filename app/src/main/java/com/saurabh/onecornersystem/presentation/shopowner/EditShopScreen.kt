package com.saurabh.onecornersystem.presentation.shopowner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.presentation.CameraCaptureScreen
import com.saurabh.onecornersystem.presentation.ImagePickerDialog
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopScreen(
    shop: Shop,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // --- THEME COLORS ---
    val amberOrange = Color(0xFFFF9100)
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    // States
    var shopName by remember { mutableStateOf(shop.shopName) }
    var description by remember { mutableStateOf(shop.description) }
    var address by remember { mutableStateOf(shop.address) }
    var city by remember { mutableStateOf(shop.city) }
    var pincode by remember { mutableStateOf(shop.pincode) }
    var contactNumber by remember { mutableStateOf(shop.contactNumber) }
    var email by remember { mutableStateOf(shop.email) }
    var openingTime by remember { mutableStateOf(shop.openingTime) }
    var closingTime by remember { mutableStateOf(shop.closingTime) }
    var latitude by remember { mutableStateOf(shop.location.latitude.toString()) }
    var longitude by remember { mutableStateOf(shop.location.longitude.toString()) }

    var fetchingLocation by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var showCameraFor by remember { mutableStateOf<String?>(null) }
    var showImageOptionsFor by remember { mutableStateOf<String?>(null) }

    // ✅ PHOTO PICKER LAUNCHER (Defined at top level of Composable)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            when (showImageOptionsFor) {
                "logo" -> logoUri = uri
                "cover" -> coverUri = uri
            }
            showImageOptionsFor = null
        }
    )

    val updateState by viewModel.updateShopState.collectAsState()

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) navController.popBackStack()
    }

    if (showCameraFor != null) {
        CameraCaptureScreen(
            onImageCaptured = { uri ->
                if (showCameraFor == "logo") logoUri = uri else coverUri = uri
                showCameraFor = null
            },
            onBackClick = { showCameraFor = null }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
            // --- LIQUID BLOBS ---
            Box(modifier = Modifier.size(400.dp).offset(x = 150.dp, y = (-100).dp).blur(130.dp).background(amberOrange.copy(alpha = 0.15f), CircleShape))
            Box(modifier = Modifier.size(300.dp).align(Alignment.BottomStart).offset(x = (-80).dp, y = 50.dp).blur(100.dp).background(amberOrange.copy(alpha = 0.1f), CircleShape))

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                        title = { Text("Business Profile", fontWeight = FontWeight.Black) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Branding Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // --- LOGO SECTION (1:1 Ratio) ---
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally // Logo ko center mein rakhne ke liye
                        ) {
                            Text(
                                "Shop Logo",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth() // Label left-aligned rahega
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = { showImageOptionsFor = "logo" },
                                modifier = Modifier
                                    .size(120.dp) // Fixed square size for Logo
                                    .aspectRatio(1f) // Enforcing 1:1
                                    .border(1.dp, outlineWhite, RoundedCornerShape(24.dp)),
                                color = glassWhite,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        logoUri != null -> Image(rememberAsyncImagePainter(logoUri), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        shop.logo.isNotBlank() -> Base64Image(shop.logo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        else -> Icon(Icons.Default.Store, null, tint = amberOrange, modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }

                        // --- COVER SECTION (16:9 Ratio) ---
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Cover Banner", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = { showImageOptionsFor = "cover" },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f) // Enforcing 16:9 Ratio
                                    .border(1.dp, outlineWhite, RoundedCornerShape(24.dp)),
                                color = glassWhite,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        coverUri != null -> Image(rememberAsyncImagePainter(coverUri), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        shop.coverImage.isNotBlank() -> Base64Image(shop.coverImage, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddPhotoAlternate, null, tint = amberOrange, modifier = Modifier.size(32.dp))
                                            Text("Add Cover", color = amberOrange, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    GlassySectionCard("Basic Information", outlineWhite) {
                        LiquidInputField(shopName, { shopName = it }, "Shop Name", Icons.Default.Business, amberOrange)
                        LiquidInputField(description, { description = it }, "About Business", Icons.Default.Description, amberOrange)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassySectionCard("Contact & Address", outlineWhite) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { LiquidInputField(contactNumber, { contactNumber = it }, "Phone", Icons.Default.Phone, amberOrange, KeyboardType.Phone) }
                            Box(modifier = Modifier.weight(1f)) { LiquidInputField(email, { email = it }, "Email", Icons.Default.Email, amberOrange, KeyboardType.Email) }
                        }
                        LiquidInputField(address, { address = it }, "Street Address", Icons.Default.Map, amberOrange)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { LiquidInputField(city, { city = it }, "City", Icons.Default.LocationCity, amberOrange) }
                            Box(modifier = Modifier.weight(1f)) { LiquidInputField(pincode, { pincode = it }, "Pincode", Icons.Default.Pin, amberOrange, KeyboardType.Number) }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GlassySectionCard("Operating Hours", outlineWhite) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(modifier = Modifier.weight(1f)) { LiquidInputField(openingTime, { openingTime = it }, "Open", Icons.Default.Schedule, amberOrange) }
                            Box(modifier = Modifier.weight(1f)) { LiquidInputField(closingTime, { closingTime = it }, "Close", Icons.Default.History, amberOrange) }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.updateShopInfo(shop.shopId, shopName, description, shop.category)
                            viewModel.updateContactDetails(shop.shopId, contactNumber, email)
                            viewModel.updateShopAddress(shop.shopId, address, city, pincode)
                            viewModel.updateOperatingHours(shop.shopId, openingTime, closingTime)
                            logoUri?.let { viewModel.uploadLogo(shop.shopId, it) }
                            coverUri?.let { viewModel.uploadCover(shop.shopId, it) }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = amberOrange),
                        enabled = !(updateState is Resource.Loading)
                    ) {
                        if (updateState is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("SYNC ALL CHANGES", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // ✅ DIALOGS (Inside EditShopScreen scope, can access photoPickerLauncher)
    showImageOptionsFor?.let { type ->
        ImagePickerDialog(
            showDialog = true,
            onDismiss = { showImageOptionsFor = null },
            onCameraClick = { showCameraFor = type; showImageOptionsFor = null },
            onGalleryClick = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemoveClick = {
                if (type == "logo") logoUri = null else coverUri = null
                showImageOptionsFor = null
            }
        )
    }
}

// --- SUB COMPONENTS ---

@Composable
fun GlassySectionCard(title: String, outline: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f), shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun EditShopLiquidPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Edit Shop Amber UI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Surface(modifier = Modifier.fillMaxWidth().height(100.dp).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)), color = Color.White.copy(0.05f), shape = RoundedCornerShape(24.dp)) { }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationForEdit(context: Context, onLocationFetched: (Double, Double) -> Unit) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { loc -> if (loc != null) onLocationFetched(loc.latitude, loc.longitude) else onLocationFetched(0.0, 0.0) }
            .addOnFailureListener { onLocationFetched(0.0, 0.0) }
    } catch (e: Exception) { onLocationFetched(0.0, 0.0) }
}