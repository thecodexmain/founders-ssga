package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel

// --- AUTHENTICATION & LOGIN HUB ---
@Composable
fun AuthHubScreen(
    viewModel: MarketplaceViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var loginType by remember { mutableStateOf("EMAIL") } // EMAIL, OTP, GOOGLE

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }

    // Error logging info toast states
    var alertMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground),
        contentAlignment = Alignment.Center
    ) {
        GradientMeshBackground()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shiny header icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan, NeonViolet)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isRegisterMode) "CREATE CORE ID" else "SECURE DECRYPT",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = Color.White
            )

            Text(
                text = "PRIME DECENTRALIZED DATA BRIDGE",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tab toggles for Auth method (Email vs OTP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                    .padding(4.dp)
            ) {
                listOf("EMAIL", "OTP").forEach { type ->
                    val isTabActive = loginType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTabActive) NeonCyan else Color.Transparent)
                            .clickable { loginType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isTabActive) Color.Black else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CyberCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loginType == "EMAIL") {
                    // EMAIL + PASSWORD FORM
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Storefront Username", color = TextSecondary) },
                            textStyle = TextStyle(color = Color.White),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Profile Email Address", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Security Access Word (Password)", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = CyberCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GlowButton(
                        text = if (isRegisterMode) "GENERATE PROTON ID" else "DECRYPT SESSION KEYS",
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                alertMsg = "All fields are required"
                            } else {
                                viewModel.loginOrRegister(emailInput, usernameInput) {
                                    onAuthSuccess()
                                }
                            }
                        },
                        glowColor = if (isRegisterMode) NeonPink else NeonCyan,
                        modifier = Modifier.fillMaxWidth()
                    )

                } else {
                    // OTP MOBILE SYSTEM FORM
                    if (!otpSent) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Mobile Phone Number (OTP)", color = TextSecondary) },
                            textStyle = TextStyle(color = Color.White),
                            singleLine = true,
                            placeholder = { Text("+1 (555) 234-9210", color = TextTertiary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CyberCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GlowButton(
                            text = "TRANSMIT VERIFY CODE",
                            onClick = {
                                if (phoneInput.isBlank() || phoneInput.length < 7) {
                                    alertMsg = "Enter a valid phone number"
                                } else {
                                    otpSent = true
                                    alertMsg = "A simulated OTP (code 2026) has been dispatched! Enjoy the ride."
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { otpInput = it },
                            label = { Text("OTP Authorization Code", color = TextSecondary) },
                            textStyle = TextStyle(color = Color.White),
                            singleLine = true,
                            placeholder = { Text("Enter 4-digit code (Use 2026)", color = TextTertiary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonViolet,
                                unfocusedBorderColor = CyberCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        GlowButton(
                            text = "VERIFY DIGITAL SIGNATURE",
                            onClick = {
                                if (otpInput == "2026") {
                                    viewModel.loginOrRegister("otp_user@prime.io", "OTP Ranger") {
                                        onAuthSuccess()
                                    }
                                } else {
                                    alertMsg = "Incorrect OTP. Please utilize code: 2026"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = NeonViolet
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "RESEND CODE PIN IN 45s",
                            color = TextTertiary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Login Simulation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCard)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.loginOrRegister("google.buyer@prime.net", "Google Elite") {
                            onAuthSuccess()
                        }
                    }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "BYPASS WITH SECURE ID / GOOGLE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Under toggle links
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegisterMode) "Already have credentials? " else "Need a virtual store footprint? ",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isRegisterMode) "DECRYPT SESSION" else "INITIALIZE ID",
                    color = NeonCyan,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { isRegisterMode = !isRegisterMode }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Toast Alert Banner logic
            AnimatedVisibility(
                visible = alertMsg != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                if (alertMsg != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE91E63).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFE91E63), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, null, tint = Color(0xFFE91E63))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alertMsg!!,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { alertMsg = null }) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
