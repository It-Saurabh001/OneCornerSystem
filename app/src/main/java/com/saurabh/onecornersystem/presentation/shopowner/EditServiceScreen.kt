package com.saurabh.onecornersystem.presentation.shopowner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.presentation.CameraCaptureScreen
import com.saurabh.onecornersystem.presentation.ImagePickerDialog
import com.saurabh.onecornersystem.presentation.components.Base64Image
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    service: ShopItem,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    val updateState by viewModel.updateItemState.collectAsState()
    val deleteState by viewModel.deleteItemState.collectAsState()

    // --- CYBER AMBER THEME ---
    val amberOrange = Color(0xFFFF9100) // Exact color requested by user
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.12f)

    // Form States
    var serviceName by remember { mutableStateOf(service.name) }
    var description by remember { mutableStateOf(service.description) }
    var price by remember { mutableStateOf(service.price.toString()) }
    var duration by remember { mutableStateOf(service.duration) }
    var category by remember { mutableStateOf(service.category) }
    var homeServiceEnabled by remember { mutableStateOf(service.homeService) }
    var requiresAppointment by remember { mutableStateOf(service.requiresAppointment) }

    // UI States
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Automotive", "Beauty", "Repair", "Cleaning", "Plumbing", "Electrical")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(updateState, deleteState) {
        if (updateState is Resource.Success || deleteState is Resource.Success) {
            navController.popBackStack()
            navController.navigate("services/${service.shopId}")
        }
    }

    if (showCamera) {
        CameraCaptureScreen(
            onImageCaptured = { uri -> imageUri = uri; showCamera = false },
            onBackClick = { showCamera = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
            // --- DYNAMIC AMBER GLOW ---
            Box(modifier = Modifier.size(350.dp).offset(x = 100.dp, y = (-100).dp).blur(130.dp).background(amberOrange.copy(alpha = 0.12f), CircleShape))
            Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp).blur(110.dp).background(amberOrange.copy(alpha = 0.08f), CircleShape))

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                        title = { Text("Service Dashboard", fontWeight = FontWeight.Black) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. IMAGE BANNER
                    Surface(
                        onClick = { showImageOptions = true },
                        modifier = Modifier.fillMaxWidth().height(180.dp).padding(vertical = 12.dp).border(1.dp, outlineWhite, RoundedCornerShape(28.dp)),
                        color = glassWhite,
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (imageUri != null) {
                                Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)), contentScale = ContentScale.Crop)
                            } else if (service.images.isNotEmpty()) {
                                Base64Image(imageSource = service.images[0], contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Default.PhotoCamera, null, tint = amberOrange, modifier = Modifier.size(40.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. FORM SECTION
                    Surface(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outlineWhite, Color.Transparent)), RoundedCornerShape(28.dp)),
                        color = glassWhite,
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            LiquidInputField(serviceName, { serviceName = it }, "Service Name", Icons.Default.Edit, amberOrange)

                            // Category Dropdown
                            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                                OutlinedTextField(
                                    value = category, onValueChange = {}, readOnly = true,
                                    leadingIcon = { Icon(Icons.Default.Category, null, tint = amberOrange) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = amberOrange, unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryExpanded = false })
                                    }
                                }
                            }

                            LiquidInputField(description, { description = it }, "Description", Icons.Default.Description, amberOrange)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LiquidInputField(price, { price = it }, "Price (₹)", Icons.Default.Payments, amberOrange, KeyboardType.Number)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LiquidInputField(duration, { duration = it }, "Duration", Icons.Default.Timer, amberOrange)
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            LiquidToggleRow("Home Delivery Service", homeServiceEnabled, amberOrange) { homeServiceEnabled = it }
                            LiquidToggleRow("Require Appointment", requiresAppointment, amberOrange) { requiresAppointment = it }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. UPDATE BUTTON
                    Button(
                        onClick = {
                            val updates = mapOf(
                                "name" to serviceName, "description" to description,
                                "category" to category, "price" to (price.toDoubleOrNull() ?: 0.0),
                                "duration" to duration, "homeService" to homeServiceEnabled,
                                "requiresAppointment" to requiresAppointment
                            )
                            viewModel.updateItem(service.itemId, updates, imageUri)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = amberOrange),
                        enabled = serviceName.isNotBlank() && !(updateState is Resource.Loading)
                    ) {
                        if (updateState is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("SAVE CHANGES", fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Error Handling
                    if (updateState is Resource.Error) {
                        ErrorCardGlass((updateState as Resource.Error).message, amberOrange, outlineWhite) {}
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // --- DIALOGS ---
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = Color(0xFF151515),
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Service?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Removing this service will cancel all pending bookings. Confirm?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteItem(service.itemId, service.shopId, service.itemType); showDeleteDialog = false }) {
                    Text("DELETE", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL", color = Color.White) }
            }
        )
    }

    ImagePickerDialog(
        showDialog = showImageOptions,
        onDismiss = { showImageOptions = false },
        onCameraClick = { showCamera = true },
        onGalleryClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemoveClick = if (imageUri != null || service.images.isNotEmpty()) { { imageUri = null } } else null
    )
}

// --- SHARED UI ---

@Composable
fun LiquidToggleRow(title: String, checked: Boolean, accent: Color, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent))
    }
}


// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun AmberThemeEditPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Amber Orange Dashboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Surface(modifier = Modifier.fillMaxWidth().height(100.dp).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)), color = Color.White.copy(0.05f), shape = RoundedCornerShape(24.dp)) { }
            }
        }
    }
}