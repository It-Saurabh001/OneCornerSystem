package com.saurabh.onecornersystem.presentation.shopowner

import android.net.Uri
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
fun AddProductScreen(
    shopId: String,
    navController: NavController,
    viewModel: ShopItemViewModel = hiltViewModel()
) {
    val createState by viewModel.createItemState.collectAsState()

    // Liquid Theme Colors
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    // Form States
    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var stockQuantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // UI Logic States
    var showCamera by remember { mutableStateOf(false) }
    var showImageOptions by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Restaurant", "Grocery", "Medical", "Bakery", "Electronics", "Fashion")
    val units = listOf("piece", "kg", "dozen", "liter", "box")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(createState) {
        if (createState is Resource.Success) {
            navController.popBackStack()
            navController.navigate("products/$shopId")
        }
    }

    if (showCamera) {
        CameraCaptureScreen(
            onImageCaptured = { uri -> imageUri = uri; showCamera = false },
            onBackClick = { showCamera = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
            // --- LIQUID BLOBS ---
            Box(modifier = Modifier.size(300.dp).offset(x = 200.dp, y = (-50).dp).blur(120.dp).background(electricBlue.copy(alpha = 0.15f), CircleShape))
            Box(modifier = Modifier.size(250.dp).align(Alignment.BottomStart).offset(x = (-50).dp, y = 50.dp).blur(100.dp).background(electricBlue.copy(alpha = 0.1f), CircleShape))

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White),
                        title = { Text("Add Product", fontWeight = FontWeight.Black) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
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
                        modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, outlineWhite, RoundedCornerShape(28.dp)),
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
                                // Edit Overlay
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = electricBlue, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Add Product Photo", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("High quality images sell faster", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Form Fields (Glass)
                    Surface(
                        modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outlineWhite, Color.Transparent)), RoundedCornerShape(28.dp)),
                        color = glassWhite,
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            LiquidInputField(productName, { productName = it }, "Product Name *", Icons.Default.ShoppingBag, electricBlue)

                            // Category Dropdown
                            ExposedDropdownMenuBox(
                                expanded = categoryExpanded,
                                onExpandedChange = { categoryExpanded = !categoryExpanded }
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Select Category *", color = Color.Gray) },
                                    leadingIcon = { Icon(Icons.Default.Category, null, tint = electricBlue) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = electricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedContainerColor = Color.White.copy(alpha = 0.02f), unfocusedContainerColor = Color.Transparent,
                                        focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                    )
                                )
                                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                    categories.forEach { item ->
                                        DropdownMenuItem(text = { Text(item) }, onClick = { category = item; categoryExpanded = false })
                                    }
                                }
                            }

                            LiquidInputField(description, { description = it }, "Description", Icons.Default.Description, electricBlue)

                            LiquidInputField(price, { price = it }, "Price (₹) *", Icons.Default.Payments, electricBlue, KeyboardType.Number)

                            // Stock Row
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LiquidInputField(stockQuantity, { stockQuantity = it }, "Stock *", Icons.Default.Inventory, electricBlue, KeyboardType.Number)
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                                        OutlinedTextField(
                                            value = unit, onValueChange = {}, readOnly = true,
                                            label = { Text("Unit", color = Color.Gray, fontSize = 12.sp) },
                                            modifier = Modifier.menuAnchor(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = electricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.1f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                        )
                                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                            units.forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false }) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Error Handling
                    if (createState is Resource.Error) {
                        ErrorCardGlass(msg = (createState as Resource.Error).message, blue = electricBlue, outline = outlineWhite) {}
                    }

                    // 4. Action Button
                    Button(
                        onClick = {
                            viewModel.createProduct(shopId, productName, description, category, price.toDoubleOrNull() ?: 0.0, stockQuantity.toIntOrNull() ?: 0, unit, imageUri)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = electricBlue),
                        enabled = productName.isNotBlank() && category.isNotBlank() && price.isNotBlank() && !(createState is Resource.Loading)
                    ) {
                        if (createState is Resource.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Text("SAVE PRODUCT", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
        onGalleryClick = {
            photoPickerLauncher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemoveClick = if (imageUri != null) { { imageUri = null } } else null
    )
}

// --- SHARED COMPONENTS (Based on CustomerHomeScreen Pattern) ---

@Composable
fun LiquidInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, blue: Color, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, null, tint = blue, modifier = Modifier.size(20.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = type),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = blue, unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.02f), unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White, unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun ErrorCardGlass(msg: String?, blue: Color, outline: Color, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth().border(1.dp, outline, RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg ?: "An error occurred", color = Color.White, textAlign = TextAlign.Center, fontSize = 13.sp)
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddProductLiquidPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Add Product Preview", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                // Sample Glassy Input
                Surface(
                    modifier = Modifier.fillMaxWidth().height(56.dp).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp)),
                    color = Color.White.copy(0.05f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Icon(Icons.Default.ShoppingBag, null, tint = Color(0xFF2979FF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Product Name", color = Color.Gray)
                    }
                }
            }
        }
    }
}