package com.saurabh.onecornersystem.presentation.shopowner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.CameraCaptureScreen
import com.saurabh.onecornersystem.presentation.ImagePickerDialog
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource

private const val TAG = "CreateShopScreen"

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShopScreen(
    navController: NavController,
    ownerId: String,
    shopType: ShopType,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var shopName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var openingTime by remember { mutableStateOf("09:00") }
    var closingTime by remember { mutableStateOf("21:00") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var locationFetched by remember { mutableStateOf(false) }
    var fetchingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var showCameraFor by remember { mutableStateOf<String?>(null) }
    var showImageOptionsFor by remember { mutableStateOf<String?>(null) }

    // Location settings resolution launcher (opens system GPS toggle)
    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Log.d(TAG, "📍 LOCATION: User enabled location services")
            fetchingLocation = true
            locationError = null
            fetchCurrentLocation(context) { lat, lng, error ->
                latitude = lat
                longitude = lng
                locationFetched = lat != 0.0 || lng != 0.0
                fetchingLocation = false
                locationError = error
                Log.d(TAG, "📍 LOCATION: After settings enable - lat=$lat, lng=$lng, error=$error")
            }
        } else {
            Log.d(TAG, "📍 LOCATION: User declined to enable location")
            locationError = "Location services are required to register your shop"
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        Log.d(TAG, "📍 LOCATION: Permission result - fine=$fineGranted, coarse=$coarseGranted")

        if (fineGranted || coarseGranted) {
            // Permission granted — now check if location services are ON
            checkAndFetchLocation(context, locationSettingsLauncher) { lat, lng, error ->
                latitude = lat
                longitude = lng
                locationFetched = lat != 0.0 || lng != 0.0
                fetchingLocation = false
                locationError = error
            }
            fetchingLocation = true
            locationError = null
        } else {
            locationError = "Location permission is needed so customers can find your shop"
            Log.d(TAG, "📍 LOCATION: Permission denied")
        }
    }

    // Function to request location (checks permission → services → fetches)
    fun requestLocation() {
        Log.d(TAG, "📍 LOCATION: requestLocation() called")
        locationError = null

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "📍 LOCATION: Permission status=$hasPermission")

        if (hasPermission) {
            fetchingLocation = true
            checkAndFetchLocation(context, locationSettingsLauncher) { lat, lng, error ->
                latitude = lat
                longitude = lng
                locationFetched = lat != 0.0 || lng != 0.0
                fetchingLocation = false
                locationError = error
                Log.d(TAG, "📍 LOCATION: Result - lat=$lat, lng=$lng, fetched=$locationFetched, error=$error")
            }
        } else {
            Log.d(TAG, "📍 LOCATION: Requesting permission...")
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-fetch location when screen opens
    LaunchedEffect(Unit) {
        Log.d(TAG, "📍 LOCATION: Screen opened — auto-fetching location")
        requestLocation()
    }

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

    val createState by viewModel.createShopState.collectAsState()

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            navController.popBackStack()
            navController.navigate("shop_owner_home")
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
                    title = { Text("Create Your Shop") },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (shopType == ShopType.PRODUCT) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFF2196F3).copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (shopType == ShopType.PRODUCT) Icons.Default.ShoppingBag else Icons.Default.Build,
                            contentDescription = null,
                            tint = if (shopType == ShopType.PRODUCT) Color(0xFF4CAF50) else Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = if (shopType == ShopType.PRODUCT) "Product Shop" else "Service Shop", fontWeight = FontWeight.Bold)
                            Text(text = if (shopType == ShopType.PRODUCT) "You'll be selling products" else "You'll be offering services", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Shop Logo", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    onClick = { showImageOptionsFor = "logo" }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(logoUri),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Add Logo", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Shop Cover", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    onClick = { showImageOptionsFor = "cover" }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (coverUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(coverUri),
                                contentDescription = "Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Add Cover Image", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Contact Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        OutlinedTextField(
                            value = contactNumber,
                            onValueChange = { contactNumber = it },
                            label = { Text("Contact Number *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Shop Location", fontWeight = FontWeight.Bold, fontSize = 16.sp)

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
                                if (locationFetched) {
                                    Text("📍 Location Captured")
                                } else {
                                    Text("Get Current Location")
                                }
                            }
                        }

                        if (locationFetched) {
                            Text(
                                text = "✅ Coordinates: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        // Show location error
                        if (locationError != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ $locationError",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Street Address *") },
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
                                label = { Text("City *") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pincode,
                                onValueChange = { pincode = it },
                                label = { Text("Pincode *") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("09:00") }
                            )
                            OutlinedTextField(
                                value = closingTime,
                                onValueChange = { closingTime = it },
                                label = { Text("Closing Time") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("21:00") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (createState is Resource.Error) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (createState as Resource.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        viewModel.createShop(
                            ownerId = ownerId,
                            shopName = shopName,
                            category = when (shopType) { ShopType.PRODUCT -> "General Store" else -> "General Service" },
                            description = description,
                            address = address,
                            city = city,
                            pincode = pincode,
                            contactNumber = contactNumber,
                            email = email,
                            latitude = latitude,
                            longitude = longitude,
                            openingTime = openingTime,
                            closingTime = closingTime
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = shopName.isNotBlank() && address.isNotBlank() && city.isNotBlank() && pincode.isNotBlank() && contactNumber.isNotBlank() && locationFetched && !(createState is Resource.Loading)
                ) {
                    if (createState is Resource.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Create Shop")
                    }
                }
            }
        }
    }

    showImageOptionsFor?.let { type ->
        ImagePickerDialog(
            showDialog = true,
            onDismiss = { showImageOptionsFor = null },
            onCameraClick = { showCameraFor = type },
            onGalleryClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveClick = {
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
 * Check location settings, then fetch location. If GPS is off, prompts user to enable it.
 */
private fun checkAndFetchLocation(
    context: Context,
    settingsLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>,
    onResult: (latitude: Double, longitude: Double, error: String?) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
    val settingsRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)
        .build()

    LocationServices.getSettingsClient(context)
        .checkLocationSettings(settingsRequest)
        .addOnSuccessListener {
            Log.d(TAG, "📍 LOCATION: Location services enabled — fetching location")
            fetchCurrentLocation(context, onResult)
        }
        .addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                Log.d(TAG, "📍 LOCATION: Location services OFF — showing enable dialog")
                try {
                    settingsLauncher.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "📍 LOCATION: Failed to launch settings", e)
                    onResult(0.0, 0.0, "Could not open location settings")
                }
            } else {
                Log.e(TAG, "📍 LOCATION: Settings check failed", exception)
                onResult(0.0, 0.0, "Location services are unavailable")
            }
        }
}

/**
 * Fetch current location using FusedLocationProviderClient.
 * Uses getCurrentLocation (fresh) with lastLocation as fallback.
 * Never silently returns 0,0 — reports errors via callback.
 */
@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    onResult: (latitude: Double, longitude: Double, error: String?) -> Unit
) {
    try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        Log.d(TAG, "📍 LOCATION: Requesting fresh location...")

        // Try fresh location first (most reliable)
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                Log.d(TAG, "📍 LOCATION: Fresh location success: ${location.latitude}, ${location.longitude}")
                onResult(location.latitude, location.longitude, null)
            } else {
                Log.d(TAG, "📍 LOCATION: Fresh location null, trying lastLocation...")
                // Fallback to lastLocation
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            Log.d(TAG, "📍 LOCATION: lastLocation success: ${lastLoc.latitude}, ${lastLoc.longitude}")
                            onResult(lastLoc.latitude, lastLoc.longitude, null)
                        } else {
                            // Final fallback: LocationManager
                            Log.d(TAG, "📍 LOCATION: lastLocation null, trying LocationManager...")
                            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            val managerLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                            if (managerLoc != null) {
                                Log.d(TAG, "📍 LOCATION: LocationManager success: ${managerLoc.latitude}, ${managerLoc.longitude}")
                                onResult(managerLoc.latitude, managerLoc.longitude, null)
                            } else {
                                Log.e(TAG, "📍 LOCATION: All methods failed")
                                onResult(0.0, 0.0, "Could not detect location. Please ensure GPS is on and try again.")
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "📍 LOCATION: lastLocation failed", e)
                        onResult(0.0, 0.0, "Failed to get location: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "📍 LOCATION: getCurrentLocation failed", e)
            onResult(0.0, 0.0, "Location request failed: ${e.message}")
        }
    } catch (e: Exception) {
        Log.e(TAG, "📍 LOCATION: Exception in fetchCurrentLocation", e)
        onResult(0.0, 0.0, "Location error: ${e.message}")
    }
}
