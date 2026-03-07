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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopScreen(
    shop: Shop,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    Log.d("EditShopScreen", "Displayed - shopId: ${shop.shopId}, shopName: ${shop.shopName}, shopType: ${shop.shopType}")

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
    var showCameraFor by remember { mutableStateOf<String?>(null) } // "logo" or "cover"
    var showImageOptionsFor by remember { mutableStateOf<String?>(null) }

    // Check if location is enabled
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                     permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (isLocationEnabled()) {
                fetchingLocation = true
                fetchLocationForEdit(context) { lat, lng ->
                    if (lat != 0.0 && lng != 0.0) {
                        latitude = String.format("%.6f", lat)
                        longitude = String.format("%.6f", lng)
                    }
                    fetchingLocation = false
                    Log.d("EditShopScreen", "Location fetched: $lat, $lng")
                }
            } else {
                showLocationSettingsDialog = true
            }
        }
    }

    // Function to request location
    fun requestLocation() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            // Check if location is enabled
            if (isLocationEnabled()) {
                fetchingLocation = true
                fetchLocationForEdit(context) { lat, lng ->
                    if (lat != 0.0 && lng != 0.0) {
                        latitude = String.format("%.6f", lat)
                        longitude = String.format("%.6f", lng)
                    } else {
                        Toast.makeText(context, "Could not get location. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                    fetchingLocation = false
                    Log.d("EditShopScreen", "Location fetched: $lat, $lng")
                }
            } else {
                // Location is off, show dialog to turn on
                showLocationSettingsDialog = true
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Location Settings Dialog
    if (showLocationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            title = { Text("Location is Off") },
            text = { Text("Please turn on location services to get your current location.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationSettingsDialog = false
                        // Open location settings
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            when (showImageOptionsFor) {
                "logo" -> {
                    Log.d("EditShopScreen", "Logo image selected")
                    logoUri = uri
                }
                "cover" -> {
                    Log.d("EditShopScreen", "Cover image selected")
                    coverUri = uri
                }
            }
            showImageOptionsFor = null
        }
    )

    val updateState by viewModel.updateShopState.collectAsState()

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            Log.d("EditShopScreen", "Shop updated successfully, navigating back")
            navController.popBackStack()
        }
    }

    if (showCameraFor != null) {
        CameraCaptureScreen(
            onImageCaptured = { uri ->
                when (showCameraFor) {
                    "logo" -> logoUri = uri
                    "cover" -> coverUri = uri
                }
                showCameraFor = null
            },
            onBackClick = { showCameraFor = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Shop Profile") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Logo and Cover Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Logo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            onClick = { showImageOptionsFor = "logo" }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    logoUri != null -> {
                                        // New image selected from gallery/camera
                                        Image(
                                            painter = rememberAsyncImagePainter(logoUri),
                                            contentDescription = "Logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    shop.logo.isNotBlank() -> {
                                        // Existing Base64 image from database
                                        Base64Image(
                                            imageSource = shop.logo,
                                            contentDescription = "Logo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        // No image - show placeholder
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("Add Logo", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Cover
                    Column(modifier = Modifier.weight(2f)) {
                        Text(text = "Cover", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            onClick = { showImageOptionsFor = "cover" }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    coverUri != null -> {
                                        // New image selected from gallery/camera
                                        Image(
                                            painter = rememberAsyncImagePainter(coverUri),
                                            contentDescription = "Cover",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    shop.coverImage.isNotBlank() -> {
                                        // Existing Base64 image from database
                                        Base64Image(
                                            imageSource = shop.coverImage,
                                            contentDescription = "Cover",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        // No image - show placeholder
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("Add Cover", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Basic Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        OutlinedTextField(
                            value = shopName,
                            onValueChange = { shopName = it },
                            label = { Text("Shop Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Contact Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it },
                            label = { Text("Contact Number") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Address", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Street Address") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            minLines = 2
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(text = "Location Coordinates", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))

                        // GPS Location Button
                        OutlinedButton(
                            onClick = { requestLocation() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !fetchingLocation
                        ) {
                            if (fetchingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetching Location...")
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Get Current Location")
                            }
                        }

                        // Show current coordinates
                        if (latitude.isNotBlank() && longitude.isNotBlank() &&
                            latitude != "0.0" && longitude != "0.0") {
                            Text(
                                text = "📍 Current: $latitude, $longitude",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = latitude,
                                onValueChange = { latitude = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = longitude,
                                onValueChange = { longitude = it },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Operating Hours", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = openingTime,
                                onValueChange = { openingTime = it },
                                label = { Text("Opening Time") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = closingTime,
                                onValueChange = { closingTime = it },
                                label = { Text("Closing Time") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (updateState is Resource.Error) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (updateState as Resource.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        Log.d("EditShopScreen", "Update Shop clicked - shopId: ${shop.shopId}")
                        Log.d("EditShopScreen", "Updated values - name: $shopName, city: $city, openingTime: $openingTime, closingTime: $closingTime, lat: $latitude, lng: $longitude")
                        viewModel.updateShopInfo(shop.shopId, shopName, description, shop.category)
                        viewModel.updateContactDetails(shop.shopId, contactNumber, email)
                        viewModel.updateShopAddress(shop.shopId, address, city, pincode)
                        viewModel.updateOperatingHours(shop.shopId, openingTime, closingTime)
                        try {
                            val lat = latitude.toDoubleOrNull() ?: shop.location.latitude
                            val lng = longitude.toDoubleOrNull() ?: shop.location.longitude
                            Log.d("EditShopScreen", "Updating location - lat: $lat, lng: $lng")
                            viewModel.updateShopLocation(shop.shopId, lat, lng)
                        } catch (e: Exception) {
                            Log.d("EditShopScreen", "Error parsing location: ${e.message}")
                        }
                        logoUri?.let {
                            Log.d("EditShopScreen", "Uploading logo")
                            viewModel.uploadLogo(shop.shopId, it)
                        }
                        coverUri?.let {
                            Log.d("EditShopScreen", "Uploading cover")
                            viewModel.uploadCover(shop.shopId, it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !(updateState is Resource.Loading)
                ) {
                    if (updateState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Shop")
                    }
                }
            }
        }
    }

    showImageOptionsFor?.let { type ->
        Log.d("EditShopScreen", "ImagePickerDialog shown for type: $type")
        ImagePickerDialog(
            showDialog = true,
            onDismiss = {
                Log.d("EditShopScreen", "ImagePickerDialog dismissed")
                showImageOptionsFor = null
            },
            onCameraClick = {
                Log.d("EditShopScreen", "Camera clicked for $type")
                showCameraFor = type
            },
            onGalleryClick = {
                Log.d("EditShopScreen", "Gallery clicked for $type")
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveClick = {
                Log.d("EditShopScreen", "Remove image clicked for $type")
                when (type) {
                    "logo" -> logoUri = null
                    "cover" -> coverUri = null
                }
                showImageOptionsFor = null
            }
        )
    }
}

/**
 * Fetch current location for EditShopScreen
 * Uses getCurrentLocation for fresh location instead of lastLocation
 */
@SuppressLint("MissingPermission")
private fun fetchLocationForEdit(
    context: Context,
    onLocationFetched: (latitude: Double, longitude: Double) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        // First try getCurrentLocation for fresh location
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: android.location.Location? ->
            if (location != null) {
                Log.d("EditShopScreen", "Fresh location fetched: ${location.latitude}, ${location.longitude}")
                onLocationFetched(location.latitude, location.longitude)
            } else {
                // Fallback to lastLocation
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLocation: android.location.Location? ->
                        if (lastLocation != null) {
                            Log.d("EditShopScreen", "Last location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                            onLocationFetched(lastLocation.latitude, lastLocation.longitude)
                        } else {
                            // Try LocationManager as final fallback
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                            if (lastKnown != null) {
                                Log.d("EditShopScreen", "LocationManager location: ${lastKnown.latitude}, ${lastKnown.longitude}")
                                onLocationFetched(lastKnown.latitude, lastKnown.longitude)
                            } else {
                                Log.d("EditShopScreen", "Could not get location from any source")
                                onLocationFetched(0.0, 0.0)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("EditShopScreen", "Failed to get last location", e)
                        onLocationFetched(0.0, 0.0)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("EditShopScreen", "Failed to get current location, trying lastLocation", e)
            // Fallback to lastLocation on failure
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: android.location.Location? ->
                    if (location != null) {
                        onLocationFetched(location.latitude, location.longitude)
                    } else {
                        onLocationFetched(0.0, 0.0)
                    }
                }
                .addOnFailureListener {
                    onLocationFetched(0.0, 0.0)
                }
        }
    } catch (e: Exception) {
        Log.e("EditShopScreen", "Exception in fetchLocationForEdit", e)
        onLocationFetched(0.0, 0.0)
    }
}
