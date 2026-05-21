package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.delay

@Composable
fun WalletLedgerScreen(
    viewModel: MarketplaceViewModel,
    onNavigateBack: () -> Unit
) {
    val activeUser by viewModel.currentUser.collectAsState()
    val transactions by viewModel.walletTransactionsFlow.collectAsState()

    var depositAmountText by remember { mutableStateOf("") }
    var withdrawAmountText by remember { mutableStateOf("") }
    var withdrawDestinationText by remember { mutableStateOf("") }

    var alertText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }

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
                .verticalScroll(rememberScrollState())
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
                    text = "PRIME LEDGER WALLET",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REFINED CREDIT CARD OVERLAY LAYOUT (Bold Typography Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(CyberCard, Color.Black)
                        )
                    )
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AVAILABLE CREDITS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(0.5.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "PREMIUM ELITE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = NeonCyan
                            )
                        }
                    }

                    Column {
                        val balance = activeUser?.walletBalance ?: 0.00
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$${String.format("%.0f", balance.toInt().toDouble())}",
                                style = MaterialTheme.typography.displayMedium,
                                color = TextPrimary
                            )
                            val cents = ((balance - balance.toInt()) * 100).toInt()
                            Text(
                                text = ".${String.format("%02d", cents)}",
                                style = MaterialTheme.typography.displaySmall,
                                color = TextTertiary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeUser?.email?.uppercase() ?: "ANONYMOUS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = TextTertiary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SECURE PORT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, color = NeonCyan)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DEPOSIT SEGMENT FORM
            Text("SECURE TOPUP SIMULATOR", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = depositAmountText,
                    onValueChange = { depositAmountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Enter dollar amount to add (e.g. 150)", color = TextTertiary) },
                    textStyle = TextStyle(color = Color.White),
                    label = { Text("Deposit Amount ($)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_field")
                )

                Spacer(modifier = Modifier.height(10.dp))

                GlowButton(
                    text = "Simulate Payout Credit",
                    onClick = {
                        val amt = depositAmountText.toDoubleOrNull()
                        if (amt == null || amt <= 0.0) {
                            alertText = "Enter a valid positive deposit amount."
                        } else {
                            viewModel.depositFunds(amt)
                            depositAmountText = ""
                            successText = "Deposit completed. Balances updated."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // WITHDRAW SEGMENT FORM
            Text("LEDGER TOKENS WITHDRAWAL", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = withdrawAmountText,
                    onValueChange = { withdrawAmountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("0.00", color = TextTertiary) },
                    textStyle = TextStyle(color = Color.White),
                    label = { Text("Withdraw Amount ($)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = withdrawDestinationText,
                    onValueChange = { withdrawDestinationText = it },
                    placeholder = { Text("e.g. PayPal / Crypto ID: 0x82fA92...", color = TextTertiary) },
                    textStyle = TextStyle(color = Color.White),
                    label = { Text("Destination Secure Account ID", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                GlowButton(
                    text = "EXECUTE WITHDRAWAL PROTOCOL",
                    onClick = {
                        val amt = withdrawAmountText.toDoubleOrNull()
                        if (amt == null || amt <= 0.0) {
                            alertText = "Enter a valid positive withdrawal amount."
                        } else if (withdrawDestinationText.isBlank()) {
                            alertText = "Destination secure account parameter is required."
                        } else {
                            viewModel.withdrawFunds(
                                amount = amt,
                                destAccount = withdrawDestinationText,
                                onSuccess = {
                                    withdrawAmountText = ""
                                    withdrawDestinationText = ""
                                    successText = "Withdrawal initiated successfully."
                                },
                                onError = { err ->
                                    alertText = err
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = NeonPink
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // LEDGER TRANSACTION HISTORY
            Text("CENTRAL DEFI TRANSACTION LEDGER", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
            Spacer(modifier = Modifier.height(10.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged in this cycle yet.", color = TextTertiary)
                }
            } else {
                transactions.forEach { txn ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCard)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (txn.type) {
                                "DEPOSIT" -> Icons.Default.TrendingUp
                                "WITHDRAWAL" -> Icons.Default.TrendingDown
                                "SALE" -> Icons.Default.Payment
                                else -> Icons.Default.ShoppingCart
                            }
                            val color = when (txn.type) {
                                "DEPOSIT" -> Color.Green
                                "SALE" -> Color.Green
                                "WITHDRAWAL" -> Color.Red
                                else -> Color.White
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberCardBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(txn.description, color = Color.White, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(txn.transactionId, color = TextTertiary, fontSize = 9.sp)
                            }
                        }

                        Text(
                            text = if (txn.type == "DEPOSIT" || txn.type == "SALE") "+$${txn.amount}" else "-$${txn.amount}",
                            color = if (txn.type == "DEPOSIT" || txn.type == "SALE") Color.Green else NeonPink,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Float alert successes
        AnimatedVisibility(
            visible = successText != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50))
                    .padding(16.dp)
            ) {
                Text(successText!!, color = Color.White, fontWeight = FontWeight.Bold)
                LaunchedEffect(successText) {
                    delay(3000)
                    successText = null
                }
            }
        }

        // Float alert errors
        AnimatedVisibility(
            visible = alertText != null,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE91E63))
                    .padding(16.dp)
            ) {
                Text(alertText!!, color = Color.White, fontWeight = FontWeight.Bold)
                LaunchedEffect(alertText) {
                    delay(3000)
                    alertText = null
                }
            }
        }
    }
}
