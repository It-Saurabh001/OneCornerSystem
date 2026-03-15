package com.saurabh.onecornersystem.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.saurabh.onecornersystem.data.model.ShopType
import com.saurabh.onecornersystem.data.model.User
import com.saurabh.onecornersystem.presentation.auth.viewmodel.AuthViewModel
import com.saurabh.onecornersystem.utils.Resource

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    viewModel: AuthViewModel
) {
    val registerState by viewModel.registerState.collectAsState()

    LaunchedEffect(Unit) { viewModel.resetStates() }

    LaunchedEffect(registerState) {
        if (registerState is Resource.Success) {
            onRegisterSuccess((registerState as Resource.Success).data.role)
        }
    }

    RegisterContent(
        registerState = registerState,
        onRegisterClick = { e, p, n, ph, r, st -> viewModel.register(e, p, n, ph, r, st) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun RegisterContent(
    registerState: Resource<User>,
    onRegisterClick: (String, String, String, String, String, ShopType?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("customer") }
    var selectedShopType by remember { mutableStateOf<ShopType?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Colors
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val glassWhite = Color.White.copy(alpha = 0.05f)
    val outlineWhite = Color.White.copy(alpha = 0.15f)

    Box(modifier = Modifier.fillMaxSize().background(deepBlack)) {
        // --- LIQUID BLOBS ---
        Box(modifier = Modifier.size(300.dp).offset(x = (-50).dp, y = (-50).dp).blur(100.dp).background(electricBlue.copy(alpha = 0.2f), CircleShape))
        Box(modifier = Modifier.size(200.dp).align(Alignment.BottomEnd).offset(x = 50.dp, y = 50.dp).blur(80.dp).background(electricBlue.copy(alpha = 0.15f), CircleShape))

        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Back Button
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("Join OneCorner", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text("Create Ripples", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = electricBlue)
                Text("Start your journey with us", color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp))

                // --- GLASS CARD ---
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Brush.linearGradient(listOf(outlineWhite, Color.Transparent)), RoundedCornerShape(28.dp)),
                    color = glassWhite,
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        RegisterTextField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person, electricBlue = electricBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        RegisterTextField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.AlternateEmail, electricBlue = electricBlue, keyboardType = KeyboardType.Email)
                        Spacer(modifier = Modifier.height(12.dp))
                        RegisterTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone, electricBlue = electricBlue, keyboardType = KeyboardType.Phone)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Password", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Fingerprint, null, tint = electricBlue) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = electricBlue, unfocusedBorderColor = outlineWhite, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Confirm Password", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.LockReset, null, tint = electricBlue) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = electricBlue, unfocusedBorderColor = outlineWhite, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Role Selection
                        Text("Register as:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RoleCard(title = "Customer", icon = Icons.Default.Person, isSelected = selectedRole == "customer", onClick = { selectedRole = "customer" }, modifier = Modifier.weight(1f), electricBlue = electricBlue)
                            RoleCard(title = "Shop Owner", icon = Icons.Default.Store, isSelected = selectedRole == "shop_owner", onClick = { selectedRole = "shop_owner" }, modifier = Modifier.weight(1f), electricBlue = electricBlue)
                        }

                        // Shop Type Selection
                        if (selectedRole == "shop_owner") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Shop Type:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                RoleCard(title = "Products", icon = Icons.Default.ShoppingBag, isSelected = selectedShopType == ShopType.PRODUCT, onClick = { selectedShopType = ShopType.PRODUCT }, modifier = Modifier.weight(1f), electricBlue = electricBlue)
                                RoleCard(title = "Services", icon = Icons.Default.Build, isSelected = selectedShopType == ShopType.SERVICE, onClick = { selectedShopType = ShopType.SERVICE }, modifier = Modifier.weight(1f), electricBlue = electricBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Error State
                        if (registerState is Resource.Error) {
                            Text(text = (registerState as Resource.Error).message, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        // Register Button
                        Button(
                            onClick = { onRegisterClick(email, password, name, phone, selectedRole, selectedShopType) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = electricBlue),
                            enabled = !registerState.isLoading()
                        ) {
                            if (registerState is Resource.Loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            else Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun RegisterTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, electricBlue: Color, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, null, tint = electricBlue) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = electricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.15f), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )
}

@Composable
fun RoleCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier, electricBlue: Color) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp).border(1.dp, if (isSelected) electricBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
        color = if (isSelected) electricBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) electricBlue else Color.Gray, modifier = Modifier.size(24.dp))
            Text(title, color = if (isSelected) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    MaterialTheme {
        RegisterContent(registerState = Resource.Idle, onRegisterClick = { _, _, _, _, _, _ -> }, onNavigateBack = {})
    }
}