package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import coil.compose.AsyncImage
import com.example.data.local.AppDatabase
import com.example.data.local.ProductEntity
import com.example.data.local.UserEntity
import com.example.data.repository.MarketplaceRepository
import com.example.ui.components.GradientMeshBackground
import com.example.ui.components.VerifiedTick
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.VerifiedBlue
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.MarketplaceViewModel
import com.example.viewmodel.MarketplaceViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var appDatabase: AppDatabase
    private lateinit var repository: MarketplaceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Build the Room database
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "prime_marketplace_db"
        ).fallbackToDestructiveMigration().build()

        repository = MarketplaceRepository(appDatabase)

        setContent {
            MyApplicationTheme {
                val viewModel: MarketplaceViewModel = viewModel(
                    factory = MarketplaceViewModelFactory(repository)
                )

                AppMainLayout(viewModel)
            }
        }
    }
}

// Parent controller managing navigation states
@Composable
fun AppMainLayout(viewModel: MarketplaceViewModel) {
    // Custom state router keys
    var currentScreen by remember { mutableStateOf("SPLASH") }
    var selectedProductForDetail by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedSellerForProfile by remember { mutableStateOf<String?>(null) }

    // Bottom Navigation tab states
    var activeBottomTab by remember { mutableStateOf("HOME") }

    val notifications by viewModel.notificationsFlow.collectAsState()
    val activeUser by viewModel.currentUser.collectAsState()

    // Back handling logic
    BackHandler(enabled = currentScreen != "SPLASH" && currentScreen != "AUTH_HUB") {
        when (currentScreen) {
            "PRODUCT_DETAIL" -> {
                currentScreen = "HOME_MAIN"
            }
            "SELLER_PROFILE" -> {
                if (selectedProductForDetail != null) currentScreen = "PRODUCT_DETAIL"
                else currentScreen = "HOME_MAIN"
            }
            "CART", "LIBRARY", "NOTIFICATIONS", "WALLET", "VERIFICATION", "ADMIN_PANEL" -> {
                currentScreen = "HOME_MAIN"
            }
            "HOME_MAIN" -> {
                // If in deep sub tabs on home, roll back to HOME tab
                if (activeBottomTab != "HOME") {
                    activeBottomTab = "HOME"
                } else {
                    currentScreen = "AUTH_HUB"
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        bottomBar = {
            if (currentScreen == "HOME_MAIN") {
                AnimatedBottomBar(
                    activeTab = activeBottomTab,
                    onTabSelected = { tab -> activeBottomTab = tab },
                    notificationsCount = notifications.count { !it.isRead },
                    unreadMessages = false // simple flag
                )
            }
        },
        contentWindowInsets = WindowInsets.navigationBars // Protect gestural indicators
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentScreen == "HOME_MAIN") 56.dp else 0.dp) // buffer for bottom navigation bar height
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "master_screen_router"
            ) { screen ->
                when (screen) {
                    "SPLASH" -> {
                        SplashScreen(
                            onSplashFinished = {
                                currentScreen = "AUTH_HUB"
                            }
                        )
                    }

                    "AUTH_HUB" -> {
                        AuthHubScreen(
                            viewModel = viewModel,
                            onAuthSuccess = {
                                currentScreen = "HOME_MAIN"
                                activeBottomTab = "HOME"
                            }
                        )
                    }

                    "HOME_MAIN" -> {
                        when (activeBottomTab) {
                            "HOME" -> {
                                MarketplaceHomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToProduct = { prod ->
                                        selectedProductForDetail = prod
                                        currentScreen = "PRODUCT_DETAIL"
                                    },
                                    onNavigateToSeller = { id ->
                                        selectedSellerForProfile = id
                                        currentScreen = "SELLER_PROFILE"
                                    },
                                    onNavigateToCart = {
                                        currentScreen = "CART"
                                    }
                                )
                            }

                            "SEARCH" -> {
                                AdvancedSearchScreen(
                                    viewModel = viewModel,
                                    onNavigateToProduct = { prod ->
                                        selectedProductForDetail = prod
                                        currentScreen = "PRODUCT_DETAIL"
                                    }
                                )
                            }

                            "CHAT" -> {
                                RealtimeChatScreen(
                                    viewModel = viewModel,
                                    onBack = {
                                        activeBottomTab = "HOME"
                                    }
                                )
                            }

                            "UPLOAD" -> {
                                UploadProductScreen(
                                    viewModel = viewModel,
                                    onSuccess = {
                                        activeBottomTab = "HOME"
                                    }
                                )
                            }

                            "PROFILE" -> {
                                MyAccountHubScreen(
                                    viewModel = viewModel,
                                    onNavigateToWallet = { currentScreen = "WALLET" },
                                    onNavigateToLibrary = { currentScreen = "LIBRARY" },
                                    onNavigateToNotifications = { currentScreen = "NOTIFICATIONS" },
                                    onNavigateToVerification = { currentScreen = "VERIFICATION" },
                                    onNavigateToAdmin = { currentScreen = "ADMIN_PANEL" },
                                    onLogout = {
                                        viewModel.logout()
                                        currentScreen = "AUTH_HUB"
                                    }
                                )
                            }
                        }
                    }

                    "PRODUCT_DETAIL" -> {
                        selectedProductForDetail?.let { prod ->
                            ProductDetailScreen(
                                product = prod,
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = "HOME_MAIN"
                                },
                                onNavigateToChat = { partner ->
                                    viewModel.selectActiveChat(partner)
                                    activeBottomTab = "CHAT"
                                    currentScreen = "HOME_MAIN"
                                },
                                onNavigateToSeller = { sId ->
                                    selectedSellerForProfile = sId
                                    currentScreen = "SELLER_PROFILE"
                                }
                            )
                        }
                    }

                    "SELLER_PROFILE" -> {
                        selectedSellerForProfile?.let { sId ->
                            SellerProfileScreen(
                                sellerId = sId,
                                viewModel = viewModel,
                                onBack = {
                                    if (selectedProductForDetail != null) {
                                        currentScreen = "PRODUCT_DETAIL"
                                    } else {
                                        currentScreen = "HOME_MAIN"
                                    }
                                },
                                onNavigateToProduct = { prod ->
                                    selectedProductForDetail = prod
                                    currentScreen = "PRODUCT_DETAIL"
                                },
                                onNavigateToChat = { partner ->
                                    viewModel.selectActiveChat(partner)
                                    activeBottomTab = "CHAT"
                                    currentScreen = "HOME_MAIN"
                                }
                            )
                        }
                    }

                    "CART" -> {
                        ShoppingCartScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = "HOME_MAIN" },
                            onNavigateToLibrary = { currentScreen = "LIBRARY" }
                        )
                    }

                    "LIBRARY" -> {
                        OrdersLibraryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = "HOME_MAIN" }
                        )
                    }

                    "NOTIFICATIONS" -> {
                        NotificationsCenterScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = "HOME_MAIN" }
                        )
                    }

                    "WALLET" -> {
                        WalletLedgerScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = "HOME_MAIN" }
                        )
                    }

                    "VERIFICATION" -> {
                        VerificationCenterScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = "HOME_MAIN" }
                        )
                    }

                    "ADMIN_PANEL" -> {
                        AdminDashboardScreen(
                            viewModel = viewModel,
                            onNavigateBack = { currentScreen = "HOME_MAIN" }
                        )
                    }
                }
            }
        }
    }
}

// --- ACCOUNT CENTER & METADATA SELECTOR ---
@Composable
fun MyAccountHubScreen(
    viewModel: MarketplaceViewModel,
    onNavigateToWallet: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Futuristic section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "USER SECURE PORTAL",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar display panel
        if (user != null) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = user!!.avatarUrl,
                    contentDescription = "My avatar photo",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .border(2.dp, NeonCyan, CircleShape),
                    contentScale = ContentScale.Crop
                )
                if (user!!.verifiedBadge) {
                    VerifiedTick(size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = user!!.username,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Text(
                text = user!!.email,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val level = user!!.sellerLevel
                Text("STORE LEVEL: ", color = TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(level.uppercase(), color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // WALLET COUNTER BOX (TAPPABLE LAUNCHER)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(NeonViolet.copy(alpha = 0.5f), NeonPink.copy(alpha = 0.5f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .clickable { onNavigateToWallet() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("LEDGER PRIME WALLET", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$${String.format("%.2f", user!!.walletBalance)}",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }
                Icon(Icons.Default.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Launcher tiles grid list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LauncherTile(
                    label = "My Software Vault (Library)",
                    desc = "Access temporary download credentials & keys",
                    icon = Icons.Default.LibraryBooks,
                    iconColor = NeonCyan,
                    onClick = onNavigateToLibrary,
                    testTag = "my_vault_btn"
                )

                LauncherTile(
                    label = "Alert logs Inbox",
                    desc = "In-app announcements & notifications logs",
                    icon = Icons.Default.Notifications,
                    iconColor = NeonPink,
                    onClick = onNavigateToNotifications
                )

                LauncherTile(
                    label = "Storefront Creator Verification",
                    desc = "Claim digital blue badging ticks",
                    icon = Icons.Default.VerifiedUser,
                    iconColor = VerifiedBlue,
                    onClick = onNavigateToVerification,
                    testTag = "kyc_btn"
                )

                // ADMIN DASHBOARD MASTER TRACE (For testing KYC approvals and spot control)
                LauncherTile(
                    label = "Admin Matrix Board Dashboard",
                    desc = "Review kyc, moderate assets listings, spot analytics",
                    icon = Icons.Default.AdminPanelSettings,
                    iconColor = Color.Yellow,
                    onClick = onNavigateToAdmin,
                    testTag = "admin_dashboard_btn"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillParentWidthPercentage(0.6f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Red.copy(alpha = 0.1f))
                    .clickable { onLogout() }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CLOSE ACCESS SESSION", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun LauncherTile(
    label: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String = "tile_launcher"
) {
    Row(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CyberCard)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CyberCardBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(desc, color = TextTertiary, fontSize = 10.sp)
        }

        Icon(Icons.Default.ArrowForwardIos, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
    }
}

// Helper extens
@Composable
fun Modifier.fillParentWidthPercentage(percentage: Float): Modifier = this.fillMaxWidth(percentage)

// --- ANIMATED BOTTOM NAVIGATION BAR ---
@Composable
fun AnimatedBottomBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    notificationsCount: Int,
    unreadMessages: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(CyberCard)
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.2f), NeonViolet.copy(alpha = 0.2f))),
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val menuItems = listOf(
            Triple("HOME", Icons.Default.Home, "home_tab"),
            Triple("SEARCH", Icons.Default.Search, "search_tab"),
            Triple("CHAT", Icons.Default.ChatBubbleOutline, "chat_tab"),
            Triple("UPLOAD", Icons.Default.Publish, "upload_tab"),
            Triple("PROFILE", Icons.Default.Person, "profile_tab")
        )

        menuItems.forEach { item ->
            val isActive = activeTab == item.first
            val iconColor = if (isActive) NeonCyan else TextSecondary

            Box(
                modifier = Modifier
                    .testTag(item.third)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(item.first) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = item.second,
                            contentDescription = item.first,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )

                        // Notification alert badges inline
                        if (item.first == "HOME" && notificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonPink)
                            )
                        } else if (item.first == "CHAT" && unreadMessages) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.Green)
                            )
                        }
                    }
                }
            }
        }
    }
}
