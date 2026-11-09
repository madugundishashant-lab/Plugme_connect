package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.model.PlatformCatalog
import com.example.ui.theme.AvatarGradients
import com.example.ui.theme.PastelCycleBorders
import com.example.ui.theme.PastelCycles
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeGreen
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import kotlin.math.roundToInt

@Composable
fun BubbleBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {
        // Decorative blurred ambient blobs
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset((-60).dp, (-40).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x3300AAFF),
                            Color(0x0000AAFF)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(80.dp, 120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x28FF7A59),
                            Color(0x00FF7A59)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .offset((-60).dp, 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x20FFB800),
                            Color(0x00FFB800)
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun BubbleCard(
    contact: Contact,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subtle staggered floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float_$index")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400 + (index % 4) * 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )

    val pastelColor = PastelCycles[index % PastelCycles.size]
    val borderColor = PastelCycleBorders[index % PastelCycleBorders.size]
    val avatarGrad = AvatarGradients[index % AvatarGradients.size]
    val isOnline = (index % 3 == 0) // Every 3rd card has online dot

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .testTag("contact_bubble_$index")
            .offset { IntOffset(0, floatAnim.roundToInt()) }
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.9f),
                        borderColor.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Bubble with Online indicator
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(avatarGrad))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initials,
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                }

                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(PlugmeGreen)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Name
            Text(
                text = contact.n.ifBlank { contact.u },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PlugmeText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Handle
            Text(
                text = "@${contact.u.ifBlank { "plug" }}",
                style = MaterialTheme.typography.labelSmall,
                color = PlugmeSub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Platform emoji strip (+N)
            val platformKeys = contact.platforms.keys.toList()
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val displayCount = 2
                val visibleKeys = platformKeys.take(displayCount)
                for (key in visibleKeys) {
                    val info = PlatformCatalog.get(key)
                    Text(
                        text = info.emoji,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }

                if (platformKeys.size > displayCount) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(pastelColor)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "+${platformKeys.size - displayCount}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlugmeText
                        )
                    }
                } else if (platformKeys.isEmpty()) {
                    Text(
                        text = "🫧",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatBubble(
    label: String,
    count: Int,
    accentColor: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 2.dp,
        modifier = modifier
            .border(
                width = 1.5.dp,
                color = accentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PlugmeSub,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FeaturedContactBubble(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(88.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.5.dp, SignatureGradient, CircleShape)
                .background(Brush.linearGradient(AvatarGradients[0]))
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.initials,
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.n.ifBlank { contact.u },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = PlugmeText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "★ Star",
            fontSize = 10.sp,
            color = PlugmeCyan,
            fontWeight = FontWeight.SemiBold
        )
    }
}
