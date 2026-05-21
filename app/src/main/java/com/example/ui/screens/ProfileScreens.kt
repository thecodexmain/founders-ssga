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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.ProductEntity
import com.example.data.local.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay

// --- 1. SELLER & USER PROFILE SCREEN ---
@Composable
fun SellerProfileScreen(
    sellerId: String,
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit,
    onNavigateToProduct: (ProductEntity) -> Unit,
    onNavigateToChat: (UserEntity) -> Unit
) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Find requested seller entity
    val seller = allUsers.find { it.userId == sellerId }
    val isCurrentUser = currentUser?.userId == sellerId

    // Products uploaded by this exact seller
    val sellerProducts = allProducts.filter { it.sellerId == sellerId }

    val amIFollowing = currentUser?.followingIds?.split(",")?.contains(sellerId) == true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        GradientMeshBackground()

        if (seller == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Searching holographic profile...", color = TextSecondary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 72.dp)
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
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }

                    Text(
                        text = "CREATOR MATRIX",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            if (!isCurrentUser) {
                                onNavigateToChat(seller)
                            }
                        },
                        enabled = !isCurrentUser,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentUser) Color.Transparent else CyberCardBorder)
                    ) {
                        if (!isCurrentUser) {
                            Icon(Icons.Default.Chat, null, tint = NeonCyan)
                        }
                    }
                }

                // Banner + Profile avatar card stack
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    // Futuristic linear brush background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(NeonViolet.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    )

                    // Profile pic floating
                    AsyncImage(
                        model = seller.avatarUrl,
                        contentDescription = "Seller image avatar",
                        modifier = Modifier
                            .size(92.dp)
                            .align(Alignment.BottomCenter)
                            .clip(CircleShape)
                            .border(4.dp, CyberCard, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Profile Identity Group
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = seller.username,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        if (seller.verifiedBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedTick(size = 20.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    SellerLevelBadge(seller.sellerLevel)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = seller.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Metrics grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberCard)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RATING", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = GoldColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (seller.rating > 0f) "${seller.rating}" else "5.0",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SALES", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${seller.totalSales}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FOLLOWERS", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${seller.followersCount}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Follow and Shop Social Action Buttons
                    if (!isCurrentUser) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.toggleFollowSeller(sellerId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (amIFollowing) CyberCardBorder else NeonCyan
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("follow_seller_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (amIFollowing) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = if (amIFollowing) Color.White else Color.Black
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (amIFollowing) "FOLLOWED SHOP" else "FOLLOW SHOP",
                                        fontWeight = FontWeight.Bold,
                                        color = if (amIFollowing) Color.White else Color.Black
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onNavigateToChat(seller) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberCardBorder)
                            ) {
                                Icon(Icons.Default.MailOutline, null, tint = Color.White)
                            }
                        }
                    } else {
                        // Current user's own statistics tag
                        Text(
                            text = "ESTABLISHED: ${seller.joinedDate} • CURRENT DEV ALIAS",
                            color = NeonPink,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // EXCLUSIVE PRODUCTS LIST
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "LISTED FILES DIRECTORY",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (sellerProducts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No digital goods listed by this creator yet.", color = TextTertiary)
                        }
                    } else {
                        sellerProducts.forEach { p ->
                            CyberCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clickable { onNavigateToProduct(p) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = p.imageUrls.split(",")[0],
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.title, color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(p.category, color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "$${p.price}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, color = NeonCyan)
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


// --- 2. VERIFICATION CENTER KYC PAGE ---
@Composable
fun VerificationCenterScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit
) {
    var realName by remember { mutableStateOf("") }
    var idLink by remember { mutableStateOf("") }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    val myRequest by viewModel.myKycRequest.collectAsState()

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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CyberCardBorder)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "VERIFICATION CENTER",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Verified Tick Benefits Display Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = VerifiedBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PETITION BLUE CERTIFICATION",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Text(
                            text = "Prove transparency & claim Elite Verified storefront badging",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                listOf(
                    "📈 2.5x Placement priority in search indexing algorithm",
                    "⭐ Direct display verified 'Blue Tick badge' trust badge",
                    "💰 Commission reduction fees: platform fees reduced by 40%",
                    "🔐 Unlocks Level: 'Verified Elite' store status",
                    "🌟 High-visibility placement spotlight in Featured Banner sliders"
                ).forEach { benefit ->
                    Text(
                        text = benefit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic view based on request status
            val req = myRequest
            if (req != null) {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SUBMITTED DOSSIER STATUS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (req.status) {
                                    "APPROVED" -> Color.Green.copy(alpha = 0.1f)
                                    "REJECTED" -> Color.Red.copy(alpha = 0.1f)
                                    else -> Color.Yellow.copy(alpha = 0.1f)
                                }
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (req.status) {
                                "APPROVED" -> Icons.Default.Check
                                "REJECTED" -> Icons.Default.Warning
                                else -> Icons.Default.Refresh
                            },
                            contentDescription = null,
                            tint = when (req.status) {
                                "APPROVED" -> Color.Green
                                "REJECTED" -> Color.Red
                                else -> Color.Yellow
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "STATUS: ${req.status}",
                            fontWeight = FontWeight.Black,
                            color = when (req.status) {
                                "APPROVED" -> Color.Green
                                "REJECTED" -> Color.Red
                                else -> Color.Yellow
                            }
                        )
                    }

                    if (req.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Moderation Admin Notes:", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                        Text(req.notes, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Input Request forms
                Text("DOSSIER SUBMISSION FORM", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(10.dp))

                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = realName,
                        onValueChange = { realName = it },
                        label = { Text("Legal Government Full Name", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = idLink,
                        onValueChange = { idLink = it },
                        label = { Text("URL Link to Verification ID (JPEG/PDF Link)", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        placeholder = { Text("https://my-drive.com/id_file.jpg", color = TextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GlowButton(
                        text = "SUBMIT KYC PETITION",
                        onClick = {
                            if (realName.isNotBlank() && idLink.isNotBlank()) {
                                viewModel.submitKycVerification(realName, idLink) {
                                    showSuccessSnackbar = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = NeonCyan
                    )
                }
            }
        }

        // Notification popups
        AnimatedVisibility(
            visible = showSuccessSnackbar,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Dossier loaded successfully. Stand by for administrative verify audit.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                LaunchedEffect(showSuccessSnackbar) {
                    kotlinx.coroutines.delay(3000)
                    showSuccessSnackbar = false
                }
            }
        }
    }
}
