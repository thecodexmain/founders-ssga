package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// 1. Futuristic Glassmorphism Card Container
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color = NeonCyan.copy(alpha = 0.25f),
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "border_glow")
    val animAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            NeonCyan.copy(alpha = animAlpha),
            NeonViolet.copy(alpha = 1f - animAlpha),
            NeonPink.copy(alpha = animAlpha)
        )
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(CyberGlassSurface) // Semi-transparent Glass Surface
            .border(borderWidth, borderBrush, RoundedCornerShape(cornerRadius))
            .padding(16.dp),
        content = content
    )
}

// 2. High-Fidelity Glowing Cyber Button
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowColor: Color = NeonCyan,
    icon: ImageVector? = null,
    testTag: String = "glow_button"
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(NeonCyan, NeonViolet)
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                // Render shadow bloom
                drawCircle(
                    color = glowColor.copy(alpha = 0.35f),
                    radius = size.width / 1.8f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }
            .then(
                if (enabled) Modifier.background(gradientBrush)
                else Modifier.background(Color.Gray.copy(alpha = 0.3f))
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (enabled) Color.Black else Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}

// 3. Official Verified Blue Tick Badge
@Composable
fun VerifiedTick(modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "Verified Blue Badge",
        tint = VerifiedBlue,
        modifier = modifier.size(size)
    )
}

// 4. Seller Level Badge Maker
@Composable
fun SellerLevelBadge(level: String, modifier: Modifier = Modifier) {
    val (label, tint, bgTint) = when (level) {
        "Verified Elite" -> Triple("ELITE", LevelVerifiedElite, LevelVerifiedElite.copy(alpha = 0.12f))
        "Premium" -> Triple("PREMIUM", LevelPremium, LevelPremium.copy(alpha = 0.12f))
        "Trusted" -> Triple("TRUSTED", LevelTrusted, LevelTrusted.copy(alpha = 0.12f))
        else -> Triple("BEGINNER", LevelBeginner, LevelBeginner.copy(alpha = 0.12f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgTint)
            .border(0.5.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
                letterSpacing = 0.5.sp
            )
        )
    }
}

// 5. Cyber Mesh Dynamic Neon Background (ambient gradient dots)
@Composable
fun GradientMeshBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(5000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale1"
    )
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(4200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Blurry floating pink spot
        Box(
            modifier = Modifier
                .offset(x = 50.dp, y = (-100).dp)
                .size(400.dp)
                .drawBehind {
                    drawCircle(
                        color = NeonPink.copy(alpha = 0.08f * scale1),
                        radius = size.width / 2f
                    )
                }
                .blur(80.dp)
        )

        // Blurry floating violet spot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 150.dp)
                .size(500.dp)
                .drawBehind {
                    drawCircle(
                        color = NeonViolet.copy(alpha = 0.07f * scale2),
                        radius = size.width / 2f
                    )
                }
                .blur(90.dp)
        )

        // Blurry floating cyan spot
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-120).dp, y = 100.dp)
                .size(350.dp)
                .drawBehind {
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.06f * scale1),
                        radius = size.width / 2f
                    )
                }
                .blur(60.dp)
        )
    }
}

// 6. Skeleton Loader block with shimmering effect
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: Dp = 150.dp,
    cornerRadius: Dp = 12.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_val"
    )

    val shimmerColors = listOf(
        CyberCard,
        CyberCardBorder.copy(alpha = 0.5f),
        CyberCard
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
            .border(1.dp, CyberCardBorder.copy(alpha = 0.3f), RoundedCornerShape(cornerRadius))
    )
}

// 7. Dynamic Cinematic Header
@Composable
fun FuturisticHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Text(
            text = subtitle.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
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
}
