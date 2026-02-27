package com.saurabh.onecornersystem.presentation.shopowner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.presentation.common.AppNavigationDrawer
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopOwnerHomeScreen(
    currentUser: User?,
    onAddProduct: () -> Unit,
    onViewOrders: () -> Unit,
    onProfileClick: () -> Unit,
    onProfileDrawerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onThemeClick: () -> Unit,
    onContactClick: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val currentUserState by authViewModel.currentUser.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Sample data
    val products = remember {
        listOf(
            ProductItem("1", "Pizza", "Delicious cheese pizza", "₹299", 50, true),
            ProductItem("2", "Burger", "Veg burger with fries", "₹149", 30, true),
            ProductItem("3", "Pasta", "White sauce pasta", "₹199", 25, false),
            ProductItem("4", "Sandwich", "Grilled sandwich", "₹99", 0, true)
        )
    }


    val recentOrders = remember {
        listOf(
            OrderItem("ORD001", "Rajesh Kumar", "₹599", "Preparing", "12:30 PM"),
            OrderItem("ORD002", "Priya Singh", "₹299", "Pending", "12:15 PM"),
            OrderItem("ORD003", "Amit Shah", "₹449", "Out for Delivery", "11:45 AM")
        )
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Surface(
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                AppNavigationDrawer(
                    user = currentUser,
                    onProfileClick = {
                        scope.launch { drawerState.close() }
                        onProfileDrawerClick()
                    },
                    onSettingsClick = {
                        scope.launch { drawerState.close() }
                        onSettingsClick()
                    },
                    onAboutClick = {
                        scope.launch { drawerState.close() }
                        onAboutClick()
                    },
                    onThemeClick = {
                        scope.launch { drawerState.close() }
                        onThemeClick()
                    },
                    onContactClick = {
                        scope.launch { drawerState.close() }
                        onContactClick()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "My Shop",
                                fontSize = 22.sp
                            )
                            Text(
                            text = "Pizza House",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                actions = {
                    // Shop Status Toggle
                    Switch(
                        checked = currentUser?.isActive ?: true,
                        onCheckedChange = { isActive ->
                            authViewModel.updateShopOwnerActiveStatus(isActive)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Notifications
                    IconButton(onClick = {}) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text("2")
                        }
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }

//                    // Profile
//                    IconButton(onClick = onProfileClick) {
//                        Icon(Icons.Default.Person, contentDescription = "Profile")
//                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Products") },
                    label = { Text("Products") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onViewOrders() },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                    label = { Text("Insights") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardTab(paddingValues,recentOrders)
            1 -> ProductsTab(paddingValues, products)
            3 -> InsightsTab(paddingValues)
        }
    }
    }
}


@Composable
fun DashboardTab(paddingValues: PaddingValues,
                 recentOrders: List<OrderItem> ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Today's Orders",
                    value = "24",
                    change = "+12%",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Revenue",
                    value = "₹8,499",
                    change = "+8%",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Total Products",
                    value = "156",
                    change = "+4",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Rating",
                    value = "4.5",
                    change = "★",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Add,
                            title = "Add Product",
                            onClick = { /*TODO*/ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Edit,
                            title = "Update Stock",
                            onClick = { /*TODO*/ }
                        )
                        QuickActionButton(
                            icon = Icons.Default.LocalOffer,
                            title = "Add Offer",
                            onClick = { /*TODO*/ }
                        )
                    }
                }
            }
        }

        // Recent Orders
        item {
            SectionHeader(
                title = "Recent Orders",
                onViewAllClick = { /*TODO*/ }
            )
        }

        items(recentOrders) { order ->
            RecentOrderItem(
                orderId = order.orderId,
                customerName = order.customerName,
                amount = order.amount,
                status = order.status
            )
        }
    }
}



@Composable
fun ProductsTab(
    paddingValues: PaddingValues,
    products: List<ProductItem>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
        }

        // Product List
        items(products) { product ->
            ProductManagementCard(
                product = product,
                onEdit = { /*TODO*/ },
                onStockUpdate = { /*TODO*/ },
                onToggleAvailability = { /*TODO*/ }
            )
        }
    }
}




@Composable
fun InsightsTab(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = "Business Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Chart placeholder
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Sales",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simple bar chart representation
                repeat(7) { day ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when(day) {
                                0 -> "Mon"
                                1 -> "Tue"
                                2 -> "Wed"
                                3 -> "Thu"
                                4 -> "Fri"
                                5 -> "Sat"
                                6 -> "Sun"
                                else -> ""
                            }
                        )

                        LinearProgressIndicator(
                        progress = { (day + 1) * 0.1f },
                        modifier = Modifier
                                                        .weight(1f)
                                                        .padding(horizontal = 8.dp)
                                                        .height(8.dp),
                        color = ProgressIndicatorDefaults.linearColor,
                        trackColor = ProgressIndicatorDefaults.linearTrackColor,
                        strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )

                        Text(text = "₹${(day + 1) * 500}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


// Helper Composables
@Composable
fun StatCard(
    title: String,
    value: String,
    change: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = change,
                fontSize = 12.sp,
                color = if (change.contains("+"))
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}


@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = title,
            fontSize = 12.sp
        )
    }
}



@Composable
fun SectionHeader(
    title: String,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onViewAllClick) {
            Text("View All")
        }
    }
}


@Composable
fun RecentOrderItem(
    orderId: String,
    customerName: String,
    amount: String,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = orderId,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = customerName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = when(status) {
                        "New" -> MaterialTheme.colorScheme.primaryContainer
                        "Preparing" -> MaterialTheme.colorScheme.secondaryContainer
                        "Ready" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}




@Composable
fun ProductManagementCard(
    product: ProductItem,
    onEdit: () -> Unit,
    onStockUpdate: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
                Text(
                    text = product.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Stock Status
                Surface(
                    color = if (product.inStock && product.stock > 0)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (product.inStock && product.stock > 0)
                            "In Stock: ${product.stock}"
                        else
                            "Out of Stock",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onStockUpdate) {
                        Icon(Icons.Default.Inventory, contentDescription = "Update Stock")
                    }
                    IconButton(onClick = onToggleAvailability) {
                        Icon(
                            if (product.isActive) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Availability"
                        )
                    }
                }
            }
        }
    }
}


// Data Classes
data class ProductItem(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val stock: Int,
    val isActive: Boolean
) {
    val inStock: Boolean = stock > 0
}

data class OrderItem(
    val orderId: String,
    val customerName: String,
    val amount: String,
    val status: String,
    val time: String
)