package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ProductEntity
import com.example.data.local.ReviewEntity
import com.example.data.local.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import java.io.Serializable

// --- 1. PRODUCT DETAILS SCREEN ---
@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (UserEntity) -> Unit,
    onNavigateToSeller: (String) -> Unit
) {
    val context = LocalContext.current
    var activeImageIndex by remember { mutableStateOf(0) }
    val imageUrlList = product.imageUrls.split(",").filter { it.isNotBlank() }
    
    // Loaded states
    val sellerState = remember { mutableStateOf<UserEntity?>(null) }
    val reviews by viewModel.activeProductReviews.collectAsState()
    val userFlow by viewModel.currentUser.collectAsState()

    // Post review state
    var reviewRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    // Buy confirm alert box
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Load Seller details and reviews
    LaunchedEffect(product) {
        viewModel.selectProductReviews(product.productId)
        kotlinx.coroutines.Dispatchers.IO.run {
            sellerState.value = viewModel.allUsers.value.find { it.userId == product.sellerId }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        GradientMeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // buffer space for action tray
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBack() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberCardBorder)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back Arrow", tint = Color.White)
                }

                Text(
                    text = "VAULT FILE DETAIL",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                    color = Color.White
                )

                IconButton(
                    onClick = { viewModel.addToCart(product) },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberCardBorder)
                ) {
                    Icon(Icons.Default.AddShoppingCart, "Add to Cart Icon", tint = NeonCyan)
                }
            }

            // Image Carousel Slot
            if (imageUrlList.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = imageUrlList[activeImageIndex],
                        contentDescription = "File Screenshot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Index banner indicators
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        imageUrlList.forEachIndexed { idx, _ ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (idx == activeImageIndex) NeonCyan else Color.White.copy(alpha = 0.4f))
                                    .clickable { activeImageIndex = idx }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Meta Details Block
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Download, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${product.downloadsCount} DLs", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }

                    Row {
                        Icon(Icons.Default.Star, null, tint = GoldColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${product.rating} (${product.ratingCount} reviews)", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Compatibility and File info Grid Cards
                CyberCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("FILE SIZE", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                            Text(product.fileSize, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("VERSION", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                            Text(product.versionHistory.substringBefore(" "), color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("COMPATIBLE", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                            Text(product.compatibility, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text("DESCRIPTION", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Seller Store Segment Card
                val seller = sellerState.value
                if (seller != null) {
                    Text("VERIFIED CREATOR STUDIO", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCard)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                            .clickable { onNavigateToSeller(seller.userId) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = seller.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (seller.verifiedBadge) {
                                VerifiedTick(modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = seller.username,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                SellerLevelBadge(seller.sellerLevel)
                            }
                            Text(
                                text = "${seller.totalSales} Sales • ★ ${seller.rating}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        // Chat button
                        IconButton(
                            onClick = { onNavigateToChat(seller) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberCardBorder)
                        ) {
                            Icon(Icons.Default.Chat, "Chat seller", tint = NeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // REVIEWS FEED MODULE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("USER FEEDBACKS", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = { /* Refresh reviews */ }) {
                        Icon(Icons.Default.Refresh, "Refresh button", tint = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Review insertion container
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("SUBMIT YOUR RATING", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..5) {
                            val active = i <= reviewRating
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (active) GoldColor else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { reviewRating = i }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("Write active rating review comments...", color = TextTertiary) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GlowButton(
                        text = "Broadcast Review",
                        onClick = {
                            if (reviewComment.isNotBlank()) {
                                viewModel.addReview(product.productId, reviewRating, reviewComment, "")
                                reviewComment = ""
                                successMessage = "Review submitted successfully!"
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        glowColor = NeonPink
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (reviews.isEmpty()) {
                    Text(
                        text = "No user reviews written for this product yet. Be the pioneer first!",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    reviews.forEach { rev ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCard)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = rev.userAvatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(rev.userName, color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        for (i in 1..5) {
                                            Icon(
                                                Icons.Default.Star,
                                                null,
                                                tint = if (i <= rev.rating) GoldColor else Color.White.copy(alpha = 0.1f),
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified Buyer", color = VerifiedBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(rev.comment, style = MaterialTheme.typography.bodyMedium, color = Color.White)

                            if (rev.replyFromSeller.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberCardBorder.copy(alpha = 0.4f))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "REPLY FROM CREATOR:",
                                        style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan, fontWeight = FontWeight.Black)
                                    )
                                    Text(rev.replyFromSeller, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING ACTION BUY BAR TRAY
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CyberCard.copy(alpha = 0.95f))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ACQUISITION PRICE", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = NeonCyan)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        viewModel.addToCart(product)
                        successMessage = "Added to Cart"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCardBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, null, tint = Color.White)
                }

                GlowButton(
                    text = "ACQUIRE NOW",
                    onClick = { showCheckoutDialog = true },
                    modifier = Modifier.width(180.dp),
                    glowColor = NeonPink,
                    testTag = "instant_buy"
                )
            }
        }

        // CHECKOUT DIALOG / SUCCESS DISCLOSURES
        if (showCheckoutDialog) {
            AlertDialog(
                onDismissRequest = { showCheckoutDialog = false },
                title = { Text("CONFIRM DIGITAL PURCHASE", color = Color.White) },
                text = {
                    Text(
                        "Are you certain you wish to buy '${product.title}' for $${product.price}? This file will be unlocked instantly and added to your encrypted vault library.",
                        color = Color.White
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCheckoutDialog = false
                            viewModel.instantBuyProduct(
                                product,
                                onSuccess = {
                                    successMessage = "SUCCESS! Check library downloads."
                                },
                                onError = { err ->
                                    alertMessage = err
                                }
                            )
                        }
                    ) {
                        Text("ACQUIRE", color = NeonCyan)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // SUCCESS ALERT BAR
        AnimatedVisibility(
            visible = successMessage != null,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            if (successMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.8f))
                        .padding(16.dp)
                ) {
                    Text(successMessage!!, color = Color.White, fontWeight = FontWeight.Bold)
                    LaunchedEffect(successMessage) {
                        delay(2500)
                        successMessage = null
                    }
                }
            }
        }

        // ERROR ALERT BAR
        AnimatedVisibility(
            visible = alertMessage != null,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            if (alertMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE91E63).copy(alpha = 0.95f))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("ERROR", color = Color.White, fontWeight = FontWeight.Black)
                        Text(alertMessage!!, color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        GlowButton(
                            text = "DISMISS",
                            onClick = { alertMessage = null },
                            modifier = Modifier.align(Alignment.End),
                            glowColor = Color.White
                        )
                    }
                }
            }
        }
    }
}


// --- 2. UPLOAD PRODUCT FORM ---
@Composable
fun UploadProductScreen(
    viewModel: MarketplaceViewModel,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var discountPriceText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Source codes") }
    var imageUrls by remember { mutableStateOf("") }
    var downloadUrl by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var compatibility by remember { mutableStateOf("Android 9.0+") }
    var fileSize by remember { mutableStateOf("12 MB") }

    val categories = listOf("Source codes", "Scripts", "APKs", "Courses", "Templates", "Gaming tools", "Designs", "E-books")
    var dropdownExpanded by remember { mutableStateOf(false) }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        GradientMeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .padding(bottom = 72.dp)
        ) {
            FuturisticHeader(
                title = "LIST DIGITAL VALUE",
                subtitle = "Upload source codes, APK files, overlay tools, or script sheets inside the decentralized vault"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title", color = TextSecondary) },
                    textStyle = TextStyle(color = Color.White),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Details / Description", color = TextSecondary) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price ($)", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = discountPriceText,
                        onValueChange = { discountPriceText = it },
                        label = { Text("Discount Price ($)", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CATEGORY SELECTION DROPDOWN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                        .background(CyberCard)
                        .clickable { dropdownExpanded = true }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Category: $selectedCategory", color = Color.White, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = NeonCyan)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(CyberCard)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    selectedCategory = cat
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = imageUrls,
                    onValueChange = { imageUrls = it },
                    label = { Text("Image Link / Screenshot link (comma-separated)", color = TextSecondary) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("https://images.unsplash.com/...csv", color = TextTertiary) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = downloadUrl,
                    onValueChange = { downloadUrl = it },
                    label = { Text("Source Zip / Playable Download Link", color = TextSecondary) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags / Search Tokens (comma-separated)", color = TextSecondary) },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = compatibility,
                        onValueChange = { compatibility = it },
                        label = { Text("SDK Version compatibility", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fileSize,
                        onValueChange = { fileSize = it },
                        label = { Text("File Size (MB)", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                GlowButton(
                    text = "DEPLOY TO PRIME WORLD",
                    onClick = {
                        val p = priceText.toDoubleOrNull() ?: 0.0
                        val dp = discountPriceText.toDoubleOrNull() ?: 0.0
                        if (title.isBlank() || description.isBlank() || priceText.isBlank()) {
                            feedbackMsg = "Fields cannot remain empty."
                        } else {
                            viewModel.uploadProduct(
                                title = title,
                                description = description,
                                price = p,
                                discountedPrice = dp,
                                category = selectedCategory,
                                imageUrls = imageUrls,
                                downloadUrl = downloadUrl,
                                tags = tags,
                                compatibility = compatibility,
                                fileSize = fileSize,
                                onSuccess = {
                                    onSuccess()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonCyan
                )
            }
        }

        // Notification toast popup
        AnimatedVisibility(
            visible = feedbackMsg != null,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            if (feedbackMsg != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE91E63))
                        .padding(16.dp)
                ) {
                    Text(feedbackMsg!!, color = Color.White, fontWeight = FontWeight.Bold)
                    LaunchedEffect(feedbackMsg) {
                        delay(2500)
                        feedbackMsg = null
                    }
                }
            }
        }
    }
}


// --- 3. ADVANCED SEARCH & DISCOVER ---
@Composable
fun AdvancedSearchScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToProduct: (ProductEntity) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val listProducts by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filterVerified by viewModel.filterVerifiedOnly.collectAsState()
    val activeSort by viewModel.sortOption.collectAsState()

    val categories = listOf("All", "Source codes", "Scripts", "APKs", "Courses", "Templates", "Gaming tools", "Designs", "E-books")
    val suggestions = listOf("Compose", "Telegram", "Bot", "Notion", "Cyberpunk", "Python", "Masterclass", "Overlay")

    var showDrawerFilters by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        GradientMeshBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .padding(bottom = 72.dp)
        ) {
            Text(
                text = "ENCRYPTED CATALOG",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black, fontSize = 24.sp),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Live Search Bar Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search title, tags, creators...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = NeonCyan) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, null, tint = TextSecondary)
                            }
                        }
                    },
                    textStyle = TextStyle(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_field_input")
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { showDrawerFilters = !showDrawerFilters },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberCardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Search Filter Control Panel",
                        tint = if (filterVerified || activeSort != "Latest") NeonPink else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // SUGGESTION CHIPS CAPSULES
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(suggestions) { sug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCard)
                            .border(0.5.dp, CyberCardBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.updateSearchQuery(sug) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$sug",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SHOW DYNAMIC DRAWER FILTERS
            AnimatedVisibility(
                visible = showDrawerFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SORT PROTOCOL", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        IconButton(onClick = { showDrawerFilters = false }) {
                            Icon(Icons.Default.Close, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Sort Buttons Horizontal grid Group
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Latest", "Price Low-High", "Price High-Low", "Downloads", "Rating").forEach { sort ->
                            val active = activeSort == sort
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (active) NeonPink else CyberCardBorder)
                                    .clickable { viewModel.setSortOption(sort) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(sort, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified Creator switch toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("VERIFIED CREATORS ONLY", color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Excludes accounts missing verified signatures", color = TextTertiary, fontSize = 10.sp)
                        }
                        Switch(
                            checked = filterVerified,
                            onCheckedChange = { viewModel.toggleVerifiedOnly() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextTertiary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Result listings grid list
            if (listProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(52.dp), tint = TextTertiary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No matching digital goods found.", color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("Clear filters or search alternative hashtags.", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Text(
                    text = "SEARCH RESULTS: ${listProducts.size} FILES FOUND",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    FlowRowGrid(listProducts, onNavigateToProduct)
                }
            }
        }
    }
}

// Custom simple scrolling Flow Column grid
@Composable
fun FlowRowGrid(
    products: List<ProductEntity>,
    onItemClick: (ProductEntity) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        products.forEach { prod ->
            CyberCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(prod) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = prod.imageUrls.split(",")[0],
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prod.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "by ${prod.sellerId.substringAfter("_")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCardBorder)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(prod.category, color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("★ ${prod.rating}", color = GoldColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "$${prod.price}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = NeonCyan)
                        )
                        Text(
                            text = prod.fileSize,
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
