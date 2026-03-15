package com.saurabh.onecornersystem.presentation.shopowner

import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.presentation.CameraCaptureScreen
import com.saurabh.onecornersystem.presentation.ImagePickerDialog
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.Resource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    val createState by viewModel.createItemState.collectAsState()

    // --- LIQUID AMBER THEME COLORS ---
    val amberOrange = Color(0xFFFF9100) // ✅ Updated Color
    val deepBlack = Color(0xFF0A0A0A)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    // Form States
    var serviceName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var homeServiceEnabled by remember { mutableStateOf(false) }
    var requiresAppointment by remember { mutableStateOf(false) }

    // Image States
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }

    val categories = listOf("Automotive", "Beauty", "Repair", "Cleaning", "Plumbing", "Electrical")
    var categoryExpanded by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            navController.popBackStack()
            navController.navigate("services/$shopId")
        }
    }

    if (showCamera) {
        CameraCaptureScreen(
            onImageCaptured = { uri -> imageUri = uri; showCamera = false },
            onBackClick = { showCamera = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
            // --- LIQUID BLOBS (Amber Glow) ---
            Box(modifier = Modifier.size(350.dp).offset(x = 200.dp, y = (-100).dp).blur(120.dp).background(amberOrange.copy(alpha = 0.15f), CircleShape))
            Box(modifier = Modifier.size(250.dp).align(Alignment.CenterStart).offset(x = (-80).dp, y = 100.dp).blur(100.dp).background(amberOrange.copy(alpha = 0.1f), CircleShape))

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                        title = { Text("Activate New Service", fontWeight = FontWeight.Black) },
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
                    // 1. Image Picker Glass Card
                    Surface(
                        onClick = { showImageOptions = true },
                        modifier = Modifier.fillMaxWidth().height(180.dp).border(1.dp, outlineWhite, RoundedCornerShape(28.dp)),
                        color = glassWhite,
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = amberOrange, modifier = Modifier.size(48.dp))
                                    Text("Service Banner", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Tap to upload", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Form Container (Glass)
                    Surface(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outlineWhite, Color.Transparent)), RoundedCornerShape(28.dp)),
                        color = glassWhite,
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            LiquidInputField1(serviceName, { serviceName = it }, "Service Name *", Icons.Default.Handyman, amberOrange)

                            // Category Dropdown
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = category, onValueChange = {}, readOnly = true,
                                    placeholder = { Text("Select Category *", color = Color.Gray) },
                                    leadingIcon = { Icon(Icons.Default.Category, null, tint = amberOrange) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = amberOrange, unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent
                                    )
                                )
                                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryExpanded = false })
                                    }
                                }
                            }

                            LiquidInputField1(description, { description = it }, "Service Description", Icons.Default.Description, amberOrange)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LiquidInputField1(price, { price = it }, "Price (₹)", Icons.Default.Payments, amberOrange, KeyboardType.Number)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LiquidInputField1(duration, { duration = it }, "Duration", Icons.Default.Timer, amberOrange)
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

                            // Service Options
                            Text("Service Logic", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            ServiceToggleRow("Home Service", "Available at client site", homeServiceEnabled, amberOrange) { homeServiceEnabled = it }
                            ServiceToggleRow("Appointment", "Require advance booking", requiresAppointment, amberOrange) { requiresAppointment = it }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 3. Error Handling
                    if (createState is Resource.Error) {
                        ErrorCardGlass1(msg = (createState as Resource.Error).message, blue = amberOrange, outline = outlineWhite) {}
                    }

                    // 4. Save Button
                    Button(
                        onClick = {
                            viewModel.createService(shopId, serviceName, description, category, price.toDoubleOrNull() ?: 0.0, duration, homeServiceEnabled, requiresAppointment, imageUri)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = amberOrange),
                        enabled = serviceName.isNotBlank() && category.isNotBlank() && price.isNotBlank() && !(createState is Resource.Loading)
                    ) {
                        if (createState is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Text("ACTIVATE SERVICE", fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    ImagePickerDialog(
        showDialog = showImageOptions,
        onDismiss = { showImageOptions = false },
        onCameraClick = { showCamera = true },
        onGalleryClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemoveClick = if (imageUri != null) { { imageUri = null } } else null
    )
}

// --- SUB COMPONENTS ---

@Composable
fun ServiceToggleRow(title: String, desc: String, checked: Boolean, accent: Color, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(desc, color = Color.Gray, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}

@Composable
fun LiquidInputField1(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = type),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.02f),
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun ErrorCardGlass1(msg: String?, blue: Color, outline: Color, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg ?: "Something went wrong", color = Color.White, textAlign = TextAlign.Center, fontSize = 13.sp)
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddServiceAmberPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Amber Theme Preview", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))
                // Sample Glassy Card
                Surface(
                    modifier = Modifier.fillMaxWidth().height(150.dp).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp)),
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Handyman, null, tint = Color(0xFFFF9100), modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}