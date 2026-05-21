package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MessageEntity
import com.example.data.local.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RealtimeChatScreen(
    viewModel: MarketplaceViewModel,
    onBack: () -> Unit
) {
    val partner by viewModel.activeChatPartner.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var messageInput by remember { mutableStateOf("") }
    var attachmentLinkInput by remember { mutableStateOf("") }
    var isAttachmentTrayShowing by remember { mutableStateOf(false) }

    val scrollState = rememberLazyListState()

    // Auto scroll to bottom when new messages loaded
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        GradientMeshBackground()

        if (partner == null) {
            // General inbox layout where you see chats history
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
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
                        text = "SECURE MESSAGE COMMS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // If no active chat selected
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Inbox Empty. Go to a digital product and click 'Chat Studio' to initialize security messages.",
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        } else {
            // Active chat dialogue panel
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Partner bar with details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard.copy(alpha = 0.95f))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .padding(12.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.closeChat() }) {
                        Icon(Icons.Default.Close, null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(contentAlignment = Alignment.BottomEnd) {
                        AsyncImage(
                            model = partner!!.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        // Online green active dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Green)
                                .border(1.5.dp, CyberCard, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = partner!!.username,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            if (partner!!.verifiedBadge) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedTick(size = 14.dp)
                            }
                        }
                        Text(
                            text = "ACTIVE SECURE CHANNEL • ONLINE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = NeonCyan
                        )
                    }

                    SellerLevelBadge(partner!!.sellerLevel)
                }

                // Chat Messages flow lane
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == currentUser?.userId
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeString = sdf.format(Date(msg.timestamp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isMe) 14.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 14.dp
                                        )
                                    )
                                    .background(if (isMe) NeonCyan else CyberCard)
                                    .border(
                                        1.dp,
                                        if (isMe) NeonCyan else CyberCardBorder,
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isMe) 14.dp else 2.dp,
                                            bottomEnd = if (isMe) 2.dp else 14.dp
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 260.dp)
                            ) {
                                Column {
                                    if (msg.attachmentUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = msg.attachmentUrl,
                                            contentDescription = "Attachment preview",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .padding(bottom = 6.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = msg.messageText,
                                        color = if (isMe) Color.Black else Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = timeString,
                                    fontSize = 9.sp,
                                    color = TextTertiary
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Message read",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ATTACHMENT TRAY OVERLAY
                AnimatedVisibility(
                    visible = isAttachmentTrayShowing,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("SHARE DIGITAL DOCUMENT", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = attachmentLinkInput,
                            onValueChange = { attachmentLinkInput = it },
                            placeholder = { Text("https://image.unsplash.com/your-screenshot.jpg", color = TextTertiary) },
                            textStyle = TextStyle(color = Color.White),
                            label = { Text("Dossier Link (Image / ZIP link)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan, unfocusedBorderColor = CyberCardBorder),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isAttachmentTrayShowing = false }) {
                                Text("CANCEL", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            GlowButton(
                                text = "ATTACH LINK",
                                onClick = {
                                    isAttachmentTrayShowing = false
                                },
                                glowColor = NeonCyan
                            )
                        }
                    }
                }

                // Input bar tray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberCard)
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isAttachmentTrayShowing = !isAttachmentTrayShowing },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyberCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach attachment files link",
                            tint = if (attachmentLinkInput.isNotBlank()) NeonPink else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text("Send secure message...", color = TextSecondary) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                viewModel.sendMessage(
                                    text = messageInput,
                                    attachmentUrl = attachmentLinkInput
                                )
                                messageInput = ""
                                attachmentLinkInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color.Black)
                    }
                }
            }
        }
    }
}
