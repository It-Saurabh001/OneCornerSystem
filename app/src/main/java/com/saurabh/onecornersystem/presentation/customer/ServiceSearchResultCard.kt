package com.saurabh.onecornersystem.presentation.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.onecornersystem.data.model.ShopItem
import androidx.compose.ui.tooling.preview.Preview

/**
 * Premium Glassmorphic Card for search results
 */
@Composable
fun ServiceSearchResultCard(
    service: ShopItem,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val electricBlue = Color(0xFF2979FF)
    val outlineWhite = Color.White.copy(alpha = 0.1f)

    Surface(
        onClick = onBookClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(outlineWhite, Color.Transparent)),
                shape = RoundedCornerShape(24.dp)
            ),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service Icon (Liquid Circle)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(electricBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = electricBlue
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Service Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = service.category,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${service.price.toInt()}",
                        color = electricBlue,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text(
                        text = " ${service.duration}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            // Arrow indicator
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray.copy(alpha = 0.5f))
        }
    }
}

/**
 * Empty search results (Glassy Placeholder)
 */
@Composable
fun EmptySearchResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.Gray.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No ripples found",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We couldn't find anything for \"$query\"",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}

/**
 * Search loading (Neon Spinner)
 */
@Composable
fun SearchLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2979FF),
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
    }
}

/**
 * Search error (Glassy Alert)
 */
@Composable
fun SearchError(message: String, onRetry: () -> Unit) {
    val electricBlue = Color(0xFF2979FF)

    Surface(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ErrorOutline, null, tint = Color.Red, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = Color.White, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text("RETRY", color = electricBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- PREVIEW ---

@Preview(showBackground = true)
@Composable
fun SearchComponentsPreview() {
    val mockService = ShopItem(
        name = "Deep House Cleaning",
        category = "Cleaning",
        price = 799.0,
        duration = "2 hrs",
        available = true
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A)) // Deep Black Background
                .padding(16.dp)
        ) {
            Text("Result Card:", color = Color.Gray, modifier = Modifier.padding(8.dp))
            ServiceSearchResultCard(mockService, {})

            Spacer(modifier = Modifier.height(20.dp))

            Text("Loading State:", color = Color.Gray, modifier = Modifier.padding(8.dp))
            SearchLoading()

            Spacer(modifier = Modifier.height(20.dp))

            Text("Error State:", color = Color.Gray, modifier = Modifier.padding(8.dp))
            SearchError("Connection lost") {}
        }
    }
}