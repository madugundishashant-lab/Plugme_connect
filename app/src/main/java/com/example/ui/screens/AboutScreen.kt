package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("about_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PlugmeText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = PlugmeText
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SignatureGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🫧", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Plugme Connect",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = PlugmeText
            )
            Text(
                text = "The Modern One-Scan Digital Card",
                style = MaterialTheme.typography.bodyMedium,
                color = PlugmeCyan,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = PlugmeCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy & Local-First Design",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PlugmeText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Plugme Connect is architected local-first. Your contacts and identity are stored securely directly on your device via encrypted Preferences DataStore. When you exchange cards via QR code, data transfers peer-to-peer without intermediate servers. Cloud sync to Firebase Realtime Database is completely optional and isolated under your authenticated user record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PlugmeCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terms of Service",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PlugmeText
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "By using Plugme Connect, you agree to share only appropriate links and publicly visible social identifiers. You are in full control of your shared profile and can edit, wipe, or revoke your card at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Support Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Need Help or Feedback?",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "mspace119@gmail.com",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PlugmeCyan
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:mspace119@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Plugme Connect Feedback")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("contact_support_button")
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Email", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "A MS Tech Product",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Built by Shashant Madugundi, 17",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
