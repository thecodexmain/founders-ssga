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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProductEntity
import com.example.data.local.UserEntity
import com.example.data.local.VerificationRequestEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    var activeAdminTab by remember { mutableStateOf("ANALYTICS") } // ANALYTICS, KYC, PRODUCTS, USERS

    val allKycRequests by viewModel.allKycRequests.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allRecruitUsers by viewModel.allUsers.collectAsState()

    var rejectionReasonInput by remember { mutableStateOf("") }
    var selectedKycForRejection by remember { mutableStateOf<String?>(null) }

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
                    text = "ADMIN MODERATION HUB",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tab toggles grouping (ANALYTICS, KYC, PRODUCTS, USERS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                listOf("ANALYTICS", "KYC", "PRODUCTS", "USERS").forEach { tab ->
                    val active = activeAdminTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (active) NeonPink else Color.Transparent)
                            .clickable { activeAdminTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TAB VIEWS
            when (activeAdminTab) {
                "ANALYTICS" -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Metrics row grid card
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CyberCard(modifier = Modifier.weight(1f)) {
                                Text("GROSS VOLUME", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                                Text("$12,840", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = NeonCyan))
                            }
                            CyberCard(modifier = Modifier.weight(1f)) {
                                Text("TOTAL USERS", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                                Text("${allRecruitUsers.size}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = NeonPink))
                            }
                        }

                        // Analytics Card
                        CyberCard(modifier = Modifier.fillMaxWidth()) {
                            Text("COMMISSION MATRIX EARNINGS", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Draw a beautiful custom analytics bar chart using modifiers
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    Pair("Source codes", 0.55f),
                                    Pair("Scripts", 0.35f),
                                    Pair("APKs", 0.20f),
                                    Pair("Courses", 0.75f),
                                    Pair("Gaming tools", 0.45f)
                                ).forEach { bar ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(bar.first, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("${(bar.second * 100).toInt()}%", color = NeonCyan, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .background(CyberCardBorder, CircleShape)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(bar.second)
                                                    .height(6.dp)
                                                    .background(
                                                        Brush.horizontalGradient(
                                                            listOf(NeonCyan, NeonViolet)
                                                        ),
                                                        CircleShape
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Alerts summary
                        CyberCard(modifier = Modifier.fillMaxWidth()) {
                            Text("ADMIN SECURITY PROTOCOL STATUS", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("🛡️ SSL Decrypt channels active", color = Color.Green, fontSize = 11.sp)
                            Text("📁 Secure token storage: 256-bit", color = Color.Green, fontSize = 11.sp)
                            Text("🤝 Escrow ledger: ONLINE", color = Color.Green, fontSize = 11.sp)
                        }
                    }
                }

                "KYC" -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val pendingKyc = allKycRequests.filter { it.status == "PENDING" }

                        if (pendingKyc.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize().weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No pending verification requests cataloged.", color = TextTertiary)
                            }
                        } else {
                            pendingKyc.forEach { req ->
                                CyberCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("APPLICANT: @${req.userId.substringAfter("_")}", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Legal Name: ${req.realName}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyberCardBorder)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "ID PROOF: ${req.idProofLink}",
                                            fontSize = 11.sp,
                                            color = NeonCyan,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { selectedKycForRejection = req.userId }) {
                                            Text("REJECT", color = Color.Red, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        GlowButton(
                                            text = "APPROVE VERIFIED",
                                            onClick = { viewModel.adminApproveKyc(req.userId) },
                                            glowColor = Color.Green,
                                            testTag = "approve_kyc_${req.userId}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "PRODUCTS" -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        allProducts.forEach { prod ->
                            CyberCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("Category: ${prod.category} • Cost: $${prod.price}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    }

                                    IconButton(
                                        onClick = { viewModel.adminBanOrDeleteProduct(prod) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Red.copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Delete, "Remove listings", tint = Color.Red, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Highlight toggles buttons
                                    Button(
                                        onClick = { viewModel.adminToggleFeatured(prod.productId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (prod.isFeatured) NeonPink else CyberCardBorder
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (prod.isFeatured) "spotlight" else "make featured", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.adminToggleTrending(prod.productId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (prod.isTrending) NeonCyan else CyberCardBorder
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (prod.isTrending) "trending" else "make trending", fontSize = 10.sp, color = if (prod.isTrending) Color.Black else Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                "USERS" -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        allRecruitUsers.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CyberCard)
                                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(CyberCardBorder)
                                        ) {
                                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.fillMaxSize().padding(6.dp))
                                        }
                                        if (user.verifiedBadge) {
                                            VerifiedTick(size = 12.dp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(user.username, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(user.sellerLevel, color = TextSecondary, fontSize = 9.sp)
                                    }
                                }

                                Text("$${String.format("%.2f", user.walletBalance)}", color = NeonCyan, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // REJECTION NOTES INPUT DRAWER
        if (selectedKycForRejection != null) {
            AlertDialog(
                onDismissRequest = { selectedKycForRejection = null },
                title = { Text("REJECT KYC PROMPT", color = Color.White) },
                text = {
                    Column {
                        Text("Provide reasons for reject verification:", color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            placeholder = { Text("Quality of upload ID is blurry...", color = TextSecondary) },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonPink, unfocusedBorderColor = CyberCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.adminRejectKyc(selectedKycForRejection!!, rejectionReasonInput)
                            selectedKycForRejection = null
                            rejectionReasonInput = ""
                        }
                    ) {
                        Text("CONFIRM REJECTION", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedKycForRejection = null }) {
                        Text("CANCEL", color = TextSecondary)
                    }
                },
                containerColor = CyberCard,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
