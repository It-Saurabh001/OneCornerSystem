package com.saurabh.onecornersystem.presentation.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.onecornersystem.data.model.Shop
import com.saurabh.onecornersystem.data.model.ShopItem
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.presentation.customer.viewmodel.CustomerShopViewModel
import com.saurabh.onecornersystem.presentation.shopowner.viewmodel.ShopItemViewModel
import com.saurabh.onecornersystem.utils.ImageUtils
import com.saurabh.onecornersystem.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceShopDetailsScreen(
    shopId: String,
    navController: NavController,
    shopViewModel: CustomerShopViewModel = hiltViewModel(),
    itemViewModel: ShopItemViewModel = hiltViewModel()
) {
    val shopState by shopViewModel.shopDetailsState.collectAsState()
    val servicesState by itemViewModel.servicesState.collectAsState()
    val isLoading by shopViewModel.isLoading.collectAsState()

    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(shopId) {
        shopViewModel.getShopDetails(shopId)
        itemViewModel.getServicesByShop(shopId)
        isFavorite = shopViewModel.isFavorite(shopId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = shopState) {
                        is Resource.Success -> {
                            Text(
                                text = state.data.shopName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        else -> Text("Shop Details")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Favorite Button
                    IconButton(onClick = {
                        when (val state = shopState) {
                            is Resource.Success -> {
                                if (isFavorite) {
                                    shopViewModel.removeFromFavorites(state.data)
                                } else {
                                    shopViewModel.addToFavorites(state.data)
                                }
                                isFavorite = !isFavorite
                            }
                            else -> {}
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share Button
                    IconButton(onClick = { /* Share shop */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Shop Cover Image
                item {
                    when (val state = shopState) {
                        is Resource.Success -> {
                            val shop = state.data

                            // Cover Image
                            if (shop.hasCover && shop.coverImage.isNotBlank()) {
                                val bitmap = ImageUtils.base64ToBitmap(shop.coverImage)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Shop Cover",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    CoverPlaceholder()
                                }
                            } else {
                                CoverPlaceholder()
                            }

                            // Shop Info Section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Shop Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Shop Logo
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        if (shop.hasLogo && shop.logo.isNotBlank()) {
                                            val logoBitmap = ImageUtils.base64ToBitmap(shop.logo)
                                            if (logoBitmap != null) {
                                                Image(
                                                    bitmap = logoBitmap.asImageBitmap(),
                                                    contentDescription = "Shop Logo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                LogoPlaceholder()
                                            }
                                        } else {
                                            LogoPlaceholder()
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // Shop Name and Type
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = shop.shopName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Shop Type Badge
                                        Surface(
                                            color = if (shop.shopType == ShopType.SERVICE)
                                                Color(0xFF2196F3).copy(alpha = 0.1f)
                                            else
                                                Color(0xFF4CAF50).copy(alpha = 0.1f),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = if (shop.shopType == ShopType.SERVICE)
                                                    "🔧 Service Provider"
                                                else "🛍️ Retail Shop",
                                                fontSize = 12.sp,
                                                color = if (shop.shopType == ShopType.SERVICE)
                                                    Color(0xFF2196F3)
                                                else
                                                    Color(0xFF4CAF50),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // Open/Closed Status
                                    Surface(
                                        color = if (shop.open)
                                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        else
                                            Color(0xFFE53935).copy(alpha = 0.1f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = if (shop.open) "OPEN" else "CLOSED",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (shop.open) Color(0xFF4CAF50) else Color(0xFFE53935),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Rating Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(5) { index ->
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = if (index < shop.rating.toInt()) Color(0xFFFFB800) else Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f", shop.rating),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " (${shop.totalRatings} reviews)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Address with Map Icon
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${shop.address}, ${shop.city} - ${shop.pincode}",
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { /* Open in Maps */ }) {
                                        Icon(
                                            Icons.Default.Map,
                                            contentDescription = "View on Map",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Contact Info
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = shop.contactNumber,
                                        fontSize = 14.sp
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = shop.email,
                                        fontSize = 14.sp
                                    )
                                }

                                // Timing
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${shop.openingTime} - ${shop.closingTime}",
                                        fontSize = 14.sp
                                    )
                                }

                                // Description (if available)
                                if (shop.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "About",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = shop.description,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // Services Section Header
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Services Offered",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { /* View all services */ }) {
                            Text("View All")
                        }
                    }
                }

                // Services List
                when (val state = servicesState) {
                    is Resource.Success -> {
                        if (state.data.isEmpty()) {
                            item {
                                EmptyServicesMessage()
                            }
                        } else {
                            items(state.data) { service ->
                                ServiceCard(
                                    service = service,
                                    onBookClick = {
                                        navController.navigate("booking_form/${service.itemId}")
                                    }
                                )
                            }
                        }
                    }
                    is Resource.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is Resource.Error -> {
                        item {
                            ErrorCard(state.message)
                        }
                    }
                    else -> {}
                }

                // Bottom Spacer
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CoverPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Store,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun LogoPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ServiceCard(
    service: ShopItem,
    onBookClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Service Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Service Icon
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Service Name and Category
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = service.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = service.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Price
                Text(
                    text = "₹${service.price}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (service.description.isNotBlank()) {
                Text(
                    text = service.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Service Features Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Duration
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = service.duration,
                        fontSize = 12.sp
                    )
                }

                // Home Service Tag
                if (service.homeService == true) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "Home Service",
                                fontSize = 10.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                // Appointment Required Tag
                if (service.requiresAppointment == true) {
                    Surface(
                        color = Color(0xFF2196F3).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFF2196F3)
                            )
                            Text(
                                text = "Appointment",
                                fontSize = 10.sp,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Book Button
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = service.available,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (service.available) "Book Now" else "Currently Unavailable",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyServicesMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Services Available",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "This shop hasn't added any services yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}