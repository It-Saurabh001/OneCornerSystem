package com.saurabh.onecornersystem.presentation.shopowner

import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.presentation.CameraCaptureScreen
import com.saurabh.onecornersystem.presentation.ImagePickerDialog
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopViewModel
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShopScreen(
    shop: Shop,
    navController: NavController,
    viewModel: ShopViewModel = hiltViewModel()
) {
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

    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var showCameraFor by remember { mutableStateOf<String?>(null) } // "logo" or "cover"
    var showImageOptionsFor by remember { mutableStateOf<String?>(null) }

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
                                val imageToShow = if (logoUri != null) {
                                    rememberAsyncImagePainter(logoUri)
                                } else if (shop.logo.isNotBlank()) {
                                    rememberAsyncImagePainter(shop.logo)
                                } else {
                                    null
                                }

                                if (imageToShow != null) {
                                    Image(
                                        painter = imageToShow,
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
                                val imageToShow = if (coverUri != null) {
                                    rememberAsyncImagePainter(coverUri)
                                } else if (shop.coverImage.isNotBlank()) {
                                    rememberAsyncImagePainter(shop.coverImage)
                                } else {
                                    null
                                }

                                if (imageToShow != null) {
                                    Image(
                                        painter = imageToShow,
                                        contentDescription = "Cover",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text("Add Cover", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
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
                        Log.d("EditShopScreen", "Updated values - name: $shopName, city: $city, openingTime: $openingTime, closingTime: $closingTime")
                        viewModel.updateShopInfo(shop.shopId, shopName, description, shop.category)
                        viewModel.updateContactDetails(shop.shopId, contactNumber, email)
                        viewModel.updateShopAddress(shop.shopId, address, city, pincode)
                        viewModel.updateOperatingHours(shop.shopId, openingTime, closingTime)
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