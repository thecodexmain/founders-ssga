package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 1. SHOPPING CART & SECURE CHECKOUT ---
@Composable
fun ShoppingCartScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLibrary: () -> Unit
) {
    var couponInput by remember { mutableStateOf("") }
    val cartProducts = viewModel.cartItems
    val subtotal by viewModel.cartSubtotal
    val discountPercent by viewModel.couponDiscountPercent.collectAsState()
    val totalAmount by viewModel.cartTotal
    val activeUser by viewModel.currentUser.collectAsState()

    var checkoutError by remember { mutableStateOf<String?>(null) }
    var checkoutSuccess by remember { mutableStateOf(false) }

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
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberCardBorder)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "ACQUISITION BASKET",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (cartProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Your digital acquisition vault is empty.", color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        GlowButton(
                            text = "Browse Catalog",
                            onClick = onNavigateBack,
                            glowColor = NeonCyan
                        )
                    }
                }
            } else {
                // Products in Cart scrolling area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cartProducts.forEach { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCard)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = prod.imageUrls.split(",")[0],
                                contentDescription = null,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(prod.category, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "$${prod.price}",
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = { viewModel.removeFromCart(prod) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CyberCardBorder)
                            ) {
                                Icon(Icons.Default.Delete, "Remove item", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Promotional Coupon System
                    Text("DOOMSDAY PROMO KEYCODES", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            placeholder = { Text("Use key 'PRIME20' for 20% off", color = TextTertiary) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("coupon_input")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        GlowButton(
                            text = "APPLY CODE",
                            onClick = {
                                viewModel.applyCoupon(couponInput)
                                couponInput = ""
                            },
                            glowColor = NeonPink
                        )
                    }

                    if (discountPercent > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        row {
                            Text("Coupon Applied: $discountPercent% Saved!", color = Color.Green, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Totals Board Summary
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Vault Subtotal", color = TextSecondary)
                            Text("$${String.format("%.2f", subtotal)}", color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Promo Deductions", color = TextSecondary)
                            Text("-$${String.format("%.2f", subtotal * (discountPercent / 100.0))}", color = NeonPink)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CyberCardBorder).padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TRANSACTION TOTAL", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format("%.2f", totalAmount)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = NeonCyan)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Final secure checkout button
                    GlowButton(
                        text = "DECRYPT & SET_CHECKOUT ESCROW",
                        onClick = {
                            viewModel.checkoutCart(
                                onSuccess = {
                                    checkoutSuccess = true
                                },
                                onError = { error ->
                                    checkoutError = error
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NeonCyan,
                        testTag = "checkout_secure"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SSL SECURED CODES CHECKOUTS", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Error message popup
        AnimatedVisibility(
            visible = checkoutError != null,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            if (checkoutError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE91E63))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("TRANSACTION TRANSACTION ERROR", color = Color.White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(checkoutError!!, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { checkoutError = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("UNDERSTOOD", color = Color.Black)
                        }
                    }
                }
            }
        }

        // Success dialog popup
        if (checkoutSuccess) {
            AlertDialog(
                onDismissRequest = { checkoutSuccess = false },
                title = { Text("PURCHASES UNLOCKED! 🌟", color = Color.White) },
                text = {
                    Text(
                        "Your transaction cleared standard protocols perfectly. The digital goods, temp secure download hashes, and static license activations are now live inside your Library vault.",
                        color = Color.White
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            checkoutSuccess = false
                            onNavigateToLibrary()
                        }
                    ) {
                        Text("PROCEED TO LIBRARY", color = NeonCyan)
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}


// --- 2. ORDERS / ACQUISITIONS LIBRARY ---
@Composable
fun OrdersLibraryScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val orders by viewModel.userOrdersFlow.collectAsState()

    var showDownloadMessage by remember { mutableStateOf<String?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberCardBorder)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "MY SOFTWARE VAULT",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(52.dp), tint = TextTertiary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No digital components purchased yet.", color = TextSecondary)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    orders.forEach { ord ->
                        CyberCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = ord.productName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // License keys and serials
                            Row {
                                Text("LICENSE ACTIVATION KEY:", color = TextSecondary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ord.licenseKey,
                                    color = NeonPink,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Temporary Download link module
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCardBorder.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "TEMP PROTECTED LINK (EXPIRING IN 24H):",
                                    fontSize = 9.sp,
                                    color = TextTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ord.secureDownloadLink,
                                    fontSize = 11.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("LIMIT COUNTER", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${ord.downloadsUsed}/${ord.maxDownloads} DECRYPTS",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                GlowButton(
                                    text = "DOWNLOAD ZIP",
                                    onClick = {
                                        showDownloadMessage = "Secure Connection established. Downloading content archive for '${ord.productName}' to Local system downloads..."
                                    },
                                    glowColor = NeonCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notification popups
        AnimatedVisibility(
            visible = showDownloadMessage != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (showDownloadMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.95f))
                        .padding(16.dp)
                ) {
                    Text(showDownloadMessage!!, color = Color.Black, fontWeight = FontWeight.Bold)
                    LaunchedEffect(showDownloadMessage) {
                        delay(4000)
                        showDownloadMessage = null
                    }
                }
            }
        }
    }
}


// --- 3. NOTIFICATIONS INBOX FEED ---
@Composable
fun NotificationsCenterScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val alerts by viewModel.notificationsFlow.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

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
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberCardBorder)
                    ) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = "ALERT LOGS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                if (alerts.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val user = activeUser
                            if (user != null) {
                                scope.launch {
                                    viewModel.markNotificationsRead(user.userId)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberCardBorder)
                    ) {
                        Icon(Icons.Default.DoneAll, "Mark all read", tint = NeonCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotificationsOff, null, modifier = Modifier.size(52.dp), tint = TextTertiary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No active notifications alerts recorded.", color = TextSecondary)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    alerts.forEach { alt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCard)
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon mapping
                            val altIcon = when (alt.type) {
                                "message" -> Icons.Default.ChatBubble
                                "sale" -> Icons.Default.TrendingUp
                                "follower" -> Icons.Default.PersonAdd
                                "order" -> Icons.Default.Check
                                else -> Icons.Default.Notifications
                            }
                            val iconColor = when (alt.type) {
                                "sale" -> Color.Green
                                "follower" -> NeonPink
                                "message" -> NeonCyan
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(CyberCardBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = altIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alt.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = alt.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        viewModel.deleteNotification(alt.notificationId)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Close, "Dismiss alerts notification", tint = TextTertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers
@Composable
private fun row(content: @Composable RowScope.() -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, content = content)
}
