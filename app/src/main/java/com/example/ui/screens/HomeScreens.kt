package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ProductEntity
import com.example.data.local.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.delay

// --- 1. SPLASH SCREEN ---
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        val duration = 1800f
        val steps = 30
        val delayTime = (duration / steps).toLong()
        for (i in 1..steps) {
            delay(delayTime)
            progress = i / steps.toFloat()
        }
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GradientMeshBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shiny futuristic branding logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonCyan, NeonViolet)
                        )
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PRIME",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = Color.White
            )

            Text(
                text = "MARKETPLACE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = NeonCyan
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(listOf(NeonCyan, NeonPink)),
                            CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "LOADING CRYPTO PROTOCOLS... ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

// --- 2. MARKETPLACE DASHBOARD HOME ---
@Composable
fun MarketplaceHomeScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToProduct: (ProductEntity) -> Unit,
    onNavigateToSeller: (String) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val featured by viewModel.featuredProducts.collectAsState()
    val trending by viewModel.trendingProducts.collectAsState()
    val topSellers by viewModel.topSellers.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val cartSize = viewModel.cartItems.size

    val categories = listOf("All", "Source codes", "Scripts", "APKs", "Courses", "Templates", "Gaming tools", "Designs", "E-books")

    // Live countdown timer for flash deals simulation
    var timeLeft by remember { mutableStateOf("01h 54m 32s") }
    LaunchedEffect(Unit) {
        var sec = 32
        var min = 54
        var hr = 1
        while (true) {
            delay(1000)
            sec--
            if (sec < 0) {
                sec = 59
                min--
                if (min < 0) {
                    min = 59
                    hr--
                    if (hr < 0) {
                        hr = 2
                    }
                }
            }
            timeLeft = String.format("%02dh %02dm %02ds", hr, min, sec)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GradientMeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 72.dp) // padding for navigation bar
        ) {
            // Toolbar banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPLORE DIGITAL VAULTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PRIME",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Text(
                            text = ".",
                            style = MaterialTheme.typography.displaySmall,
                            color = NeonCyan
                        )
                    }
                }

                // Shopping cart icon box with cart badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CyberCardBorder)
                        .clickable { onNavigateToCart() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    if (cartSize > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(NeonPink)
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartSize.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Category Horizontal Lane (Sticky view template style)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) NeonCyan else CyberCard)
                            .border(
                                1.dp,
                                if (isSelected) NeonCyan else CyberCardBorder,
                                CircleShape
                            )
                            .clickable { viewModel.updateSelectedCategory(cat) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat.uppercase(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                        )
                    }
                }
            }

            // If selectedCategory is "All", show Featured and Flash Deals
            if (selectedCategory == "All") {
                // FEATURED BANNER SLIDER
                FuturisticHeader(
                    title = "Featured Spotlight",
                    subtitle = "Handcrafted by verified creators",
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp)
                )

                // Carousel/Slider Horizontal List
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(featured) { prod ->
                        CyberCard(
                            modifier = Modifier
                                .width(300.dp)
                                .clickable { onNavigateToProduct(prod) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = prod.imageUrls.split(",")[0],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonPink)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .align(Alignment.TopEnd)
                                        .margin(8.dp)
                                ) {
                                    Text(
                                        text = "FEATURED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = prod.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${prod.rating}", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                }

                                Text(
                                    text = "$${prod.price}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = NeonCyan
                                    )
                                )
                            }
                        }
                    }
                }

                // FLASH DEALS MODULE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF230D42), Color(0xFF0F0B1E))
                            )
                        )
                        .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = NeonPink, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FLASH DEALS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                            }
                            // Simulated countdown clock
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = timeLeft,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Show 1 flash deal item
                        val flashProduct = trending.firstOrNull()
                        if (flashProduct != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToProduct(flashProduct) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = flashProduct.imageUrls.split(",")[0],
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = flashProduct.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$${flashProduct.price}",
                                            style = MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                            color = TextTertiary,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$${flashProduct.discountedPrice.ifZero(12.00)}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NeonPink
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TOP VERIFIED CREATORS
            FuturisticHeader(
                title = "Elite Sellers",
                subtitle = "Follow verified shops for high-grade source listings",
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(topSellers) { seller ->
                    Column(
                        modifier = Modifier
                            .width(86.dp)
                            .clickable { onNavigateToSeller(seller.userId) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = seller.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (seller.verifiedBadge) VerifiedBlue else CyberCardBorder,
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            if (seller.verifiedBadge) {
                                VerifiedTick(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = seller.username,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        SellerLevelBadge(seller.sellerLevel, modifier = Modifier.scale(0.85f))
                    }
                }
            }

            // TRENDING & GENERAL RECENT LISTINGS
            val listCategoryProducts = viewModel.filteredProducts.collectAsState().value
            val isCustomCategory = selectedCategory != "All"

            FuturisticHeader(
                title = if (isCustomCategory) selectedCategory else "Trending Drops",
                subtitle = if (isCustomCategory) "Showing matches under this vault category" else "Highest circulation files this week",
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)
            )

            if (listCategoryProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, null, modifier = Modifier.size(48.dp), tint = TextTertiary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No products listed under this category yet", color = TextSecondary)
                    }
                }
            } else {
                for (prod in listCategoryProducts) {
                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .clickable { onNavigateToProduct(prod) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = prod.imageUrls.split(",")[0],
                                contentDescription = null,
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CyberCardBorder)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = prod.category,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = NeonCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.Default.Download, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${prod.downloadsCount}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, null, tint = GoldColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${prod.rating}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                if (prod.discountedPrice > 0.0) {
                                    Text(
                                        text = "$${prod.price}",
                                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                        color = TextTertiary
                                    )
                                    Text(
                                        text = "$${prod.discountedPrice}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = NeonPink
                                        )
                                    )
                                } else {
                                    Text(
                                        text = "$${prod.price}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = NeonCyan
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers
private fun Double.ifZero(default: Double): Double {
    return if (this == 0.0) default else this
}

private fun Modifier.margin(size: androidx.compose.ui.unit.Dp) = this.padding(size)
