package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily

@Composable
fun WelcomeOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_overlay_card")
                    .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating bubble logo
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SignatureGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🫧", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Plugme Connect",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = PlugmeText
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your digital business card that shares everything in one scan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PlugmeSub,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Onboarding Steps
                    WelcomeStepRow(
                        emoji = "⚡",
                        title = "1. Set Up Your Card",
                        description = "Add your social handles, gaming tags, and profile."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WelcomeStepRow(
                        emoji = "📷",
                        title = "2. One-Scan Exchange",
                        description = "Share your QR code or scan friends to plug them into your vault."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WelcomeStepRow(
                        emoji = "☁️",
                        title = "3. Offline-First & Synced",
                        description = "Everything works offline instantly. Upgrade to Pro for cloud backup."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("lets_go_button")
                    ) {
                        Text(
                            text = "Let's Go! 🚀",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStepRow(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PlugmeText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = PlugmeSub
            )
        }
    }
}
