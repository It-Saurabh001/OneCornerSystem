package com.saurabh.onecornersystem.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.saurabh.onecornersystem.data.model.User
@Composable
fun AppNavigationDrawer(
    user: User?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onThemeClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val deepBlack = Color(0xFF0A0A0A)
    val electricBlue = Color(0xFF2979FF)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    // ✅ ROOT KO BOX BANAYA: Taaki background blobs content ko niche na dhakelein
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // --- 1. BACKGROUND DECORATION (Ab ye piche rahega) ---
        Box(
            modifier = Modifier
                .size(150.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .blur(60.dp)
                .background(electricBlue.copy(alpha = 0.15f), CircleShape)
        )

        // --- 2. ACTUAL CONTENT (Ab ye ekdum top se shuru hoga) ---
        Column(
            modifier = Modifier.fillMaxSize()
                .background(color = Color.Transparent,)

        ) {
            // Profile Header Section (Ab ye upar aa jayega)
            ProfileDrawerHeader(
                user = user,
                onProfileClick = onProfileClick,
                blue = electricBlue,
                outline = outlineWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Thin Glassy Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 24.dp)
                    .background(outlineWhite)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Menu Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DrawerMenuItem(Icons.Default.Settings, "Settings", electricBlue, onSettingsClick)
                DrawerMenuItem(Icons.Default.Info, "About Us", electricBlue, onAboutClick)
                DrawerMenuItem(Icons.Default.DarkMode, "Appearance", electricBlue, onThemeClick)
                DrawerMenuItem(Icons.Default.Help, "Support Center", electricBlue, onContactClick)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Text(
                text = "OneCorner v1.0.4",
                color = Color.Gray,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun ProfileDrawerHeader(
    user: User?,
    onProfileClick: () -> Unit,
    blue: Color,
    outline: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(24.dp)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with Electric Glow
        Box(
            modifier = Modifier
                .size(70.dp)
                .border(2.dp, blue.copy(alpha = 0.5f), CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(blue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (user != null) {
                Text(
                    text = user.name.firstOrNull()?.toString()?.uppercase() ?: "U",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            } else {
                Icon(Icons.Default.Person, "User", modifier = Modifier.size(32.dp), tint = blue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = user?.name ?: "Guest User",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // User Email
        Text(
            text = user?.email ?: "Sign in to your account",
            fontSize = 13.sp,
            color = Color.Gray
        )

        if (user != null) {
            Spacer(modifier = Modifier.height(12.dp))
            // Glassy Role Badge
            Surface(
                color = blue.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.border(0.5.dp, blue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = user.role.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = blue,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    blue: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent, // Invisible background until hover/click
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).background(Color.Transparent),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = blue
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Gray.copy(alpha = 0.4f)
            )
        }
    }
}

// --- SHARED ERROR COMPONENT (As requested from CustomerHomeScreen) ---

// --- PREVIEW ---

@Preview(showBackground = true)
@Composable
fun DrawerLiquidPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AppNavigationDrawer(
                user = User(name = "Saurabh Dev", email = "saurabh@example.com", role = "shop_owner"),
                onProfileClick = {},
                onSettingsClick = {},
                onAboutClick = {},
                onThemeClick = {},
                onContactClick = {}
            )
        }
    }
}