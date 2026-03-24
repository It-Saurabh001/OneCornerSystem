package com.saurabh.onecornersystem.presentation.shopowner

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// --- CROPPER IMPORTS ---
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions

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

    // Image Flags
    var isLogoRemoved by remember { mutableStateOf(false) }
    var isCoverRemoved by remember { mutableStateOf(false) }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }

    var showCameraFor by remember { mutableStateOf<String?>(null) }
    var showImageOptionsFor by remember { mutableStateOf<String?>(null) }
    var pickingFor by remember { mutableStateOf<String?>(null) }

    val TAG = "EditShopScreen_Log"

    // 1. Cropper Launcher (Handles the cropped result from BOTH Gallery and Camera)
    @Suppress("DEPRECATION")
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            Log.d(TAG, "Cropper Success: Cropped URI -> $croppedUri")
            when (pickingFor) {
                "logo" -> {
                    logoUri = croppedUri
                    isLogoRemoved = false
                }
                "cover" -> {
                    coverUri = croppedUri
                    isCoverRemoved = false
                }
            }
        } else {
            Log.e(TAG, "Cropper error: ${result.error}")
        }
        pickingFor = null // Reset state after cropper is done
    }

    // 2. Photo Picker Launcher (Handles raw gallery selection & passes to cropper)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                Log.d(TAG, "Gallery Success: Raw URI -> $uri")
                @Suppress("DEPRECATION")
                val cropOptions = CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = false,
                        fixAspectRatio = true, // Lock aspect ratio
                        aspectRatioX = if (pickingFor == "cover") 16 else 1,
                        aspectRatioY = if (pickingFor == "cover") 9 else 1,
                        // FIX: Changed toolbarTitle to activityTitle
                        activityTitle = if (pickingFor == "cover") "Crop Cover Image" else "Crop Shop Logo"
                    )
                )

                imageCropLauncher.launch(cropOptions)

            } else {
                Log.d(TAG, "Gallery Cancelled: No image selected")
                pickingFor = null
            }
        }
    )

    // Collect all states to prevent early back navigation
    val updateState by viewModel.updateShopState.collectAsState()
    val coverUploadState by viewModel.coverUploadState.collectAsState()
    val logoUploadState by viewModel.logoUploadState.collectAsState()

    // Safe Navigation (Waits for both text AND images to finish)
    LaunchedEffect(updateState, coverUploadState, logoUploadState) {
        val isBasicSuccess = updateState is Resource.Success
        val isCoverSuccess = if (coverUri != null) coverUploadState is Resource.Success else true
        val isLogoSuccess = if (logoUri != null) logoUploadState is Resource.Success else true

        if (isBasicSuccess && isCoverSuccess && isLogoSuccess) {
            Log.d(TAG, "All uploads & updates successful!")
            Toast.makeText(context, "Changes saved successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetAllStates()
        }
    }

    if (showCameraFor != null) {
        Log.d(TAG, "Opening Camera for: $showCameraFor")
        CameraCaptureScreen(
            onImageCaptured = { uri ->
                Log.d(TAG, "Camera Success: Captured for $showCameraFor -> $uri")

                // Set pickingFor so cropper knows what aspect ratio to use
                pickingFor = showCameraFor
                @Suppress("DEPRECATION")
                val cropOptions = CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = CropImageOptions(
                        imageSourceIncludeGallery = false,
                        imageSourceIncludeCamera = false,
                        fixAspectRatio = true,
                        aspectRatioX = if (showCameraFor == "cover") 16 else 1,
                        aspectRatioY = if (showCameraFor == "cover") 9 else 1,
                        // FIX: Changed toolbarTitle to activityTitle
                        activityTitle = if (showCameraFor == "cover") "Crop Cover Image" else "Crop Shop Logo"
                    )
                )

                // Hide camera screen and launch cropper
                showCameraFor = null
                imageCropLauncher.launch(cropOptions)
            },
            onBackClick = {
                Log.d(TAG, "Camera Cancelled")
                showCameraFor = null
            }
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // --- LOGO SECTION ---
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Shop Logo", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = { showImageOptionsFor = "logo" },
                                modifier = Modifier.size(120.dp).aspectRatio(1f).border(1.dp, outlineWhite, RoundedCornerShape(24.dp)),
                                color = glassWhite,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        logoUri != null -> Image(rememberAsyncImagePainter(logoUri), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        shop.logo.isNotBlank() && !isLogoRemoved -> Base64Image(shop.logo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        else -> Icon(Icons.Default.Store, null, tint = amberOrange, modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }

                        // --- COVER SECTION ---
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Cover Banner", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                onClick = { showImageOptionsFor = "cover" },
                                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).border(1.dp, outlineWhite, RoundedCornerShape(24.dp)),
                                color = glassWhite,
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        coverUri != null -> Image(rememberAsyncImagePainter(coverUri), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        shop.coverImage.isNotBlank() && !isCoverRemoved -> Base64Image(shop.coverImage, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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
                            Log.d(TAG, "ACTION: Sync All Changes Clicked")
                            // Text Updates
                            viewModel.updateShopInfo(shop.shopId, shopName, description, shop.category)
                            viewModel.updateContactDetails(shop.shopId, contactNumber, email)
                            viewModel.updateShopAddress(shop.shopId, address, city, pincode)
                            viewModel.updateOperatingHours(shop.shopId, openingTime, closingTime)

                            // Safely handle uploads and deletions
                            if (isCoverRemoved && coverUri == null) {
                                viewModel.removeCover(shop.shopId)
                            } else if (coverUri != null) {
                                viewModel.uploadCover(shop.shopId, coverUri!!)
                            }

                            if (isLogoRemoved && logoUri == null) {
                                viewModel.removeLogo(shop.shopId)
                            } else if (logoUri != null) {
                                viewModel.uploadLogo(shop.shopId, logoUri!!)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = amberOrange),
                        enabled = updateState !is Resource.Loading
                    ) {
                        if (updateState is Resource.Loading || coverUploadState is Resource.Loading || logoUploadState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("SAVE ALL CHANGES", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    showImageOptionsFor?.let { type ->
        ImagePickerDialog(
            showDialog = true,
            onDismiss = { showImageOptionsFor = null },
            onCameraClick = {
                showCameraFor = type
                showImageOptionsFor = null
            },
            onGalleryClick = {
                pickingFor = type
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                showImageOptionsFor = null
            },
            onRemoveClick = {
                if (type == "logo") {
                    logoUri = null
                    isLogoRemoved = true
                } else {
                    coverUri = null
                    isCoverRemoved = true
                }
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