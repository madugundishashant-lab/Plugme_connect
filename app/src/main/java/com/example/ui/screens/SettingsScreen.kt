package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel

@Composable
fun SettingsScreen(
    viewModel: PlugmeViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vault by viewModel.vault.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncManager.syncStatus.collectAsStateWithLifecycle()

    var showFirstWipeDialog by remember { mutableStateOf(false) }
    var showSecondWipeDialog by remember { mutableStateOf(false) }
    var wipeConfirmationText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Settings & Setup",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = PlugmeText
        )
        Text(
            text = "App preferences, account management, and storage",
            style = MaterialTheme.typography.bodySmall,
            color = PlugmeSub
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Version / Plan / Storage Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plugme Connect",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PlugmeText
                        )
                        Text(
                            text = "Version 1.0.0 (Native Android)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPro) Color(0xFFE8F5E9) else Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPro) "PRO PLAN ⚡" else "FREE PLAN",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isPro) Color(0xFF059669) else PlugmeSub
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Stored Plugs in Vault:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PlugmeSub
                    )
                    Text(
                        text = "${vault.size} contacts",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = PlugmeCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Storage & Cache:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PlugmeSub
                    )
                    Text(
                        text = "Room SQLite (100% Offline)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PlugmeCyan
                    )
                }

                if (syncStatus != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (syncStatus!!.contains("restricted", ignoreCase = true) || syncStatus!!.contains("rules", ignoreCase = true))
                                    Color(0xFFFFF7ED) else Color(0xFFF0FDF4)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Cloud Sync Status",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PlugmeSub
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = syncStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (syncStatus!!.contains("restricted", ignoreCase = true) || syncStatus!!.contains("rules", ignoreCase = true))
                                PlugmeCoral else Color(0xFF16A34A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options List Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Manage Account or Unlock Pro
                SettingsItemRow(
                    icon = if (isPro) Icons.Default.Person else Icons.Default.Star,
                    title = if (isPro) "Manage Account" else "Unlock Pro (Cloud Sync & 25 Links)",
                    subtitle = if (isPro) "View active Pro profile & credentials" else "Upgrade for Realtime cloud sync & higher limits",
                    iconTint = if (isPro) PlugmeCyan else Color(0xFFFFB800),
                    onClick = onNavigateToAuth,
                    testTag = "settings_manage_account"
                )

                // Pro-only Sync Now
                if (isPro) {
                    SettingsItemRow(
                        icon = Icons.Default.CloudSync,
                        title = "Sync Now",
                        subtitle = "Push local card and vault to Firebase Realtime Database",
                        iconTint = PlugmeCyan,
                        onClick = { viewModel.syncNow() },
                        testTag = "settings_sync_now"
                    )
                }

                // Show Intro Again
                SettingsItemRow(
                    icon = Icons.Default.RestartAlt,
                    title = "Show Intro Again",
                    subtitle = "Replay the first-launch onboarding bubble tour",
                    iconTint = PlugmeSub,
                    onClick = { viewModel.showWelcomeAgain() },
                    testTag = "settings_show_intro"
                )

                // About
                SettingsItemRow(
                    icon = Icons.Default.Info,
                    title = "About Plugme Connect",
                    subtitle = "Privacy policy, terms, credits, and creator info",
                    iconTint = PlugmeCyan,
                    onClick = onNavigateToAbout,
                    testTag = "settings_about"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Danger Zone: Wipe All Data
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = PlugmeCoral
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Danger Zone",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PlugmeCoral
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Erase all local contacts, personal cards, and reset to initial state.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PlugmeSub
                )
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showFirstWipeDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCoral),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wipe_data_button")
                ) {
                    Text("Wipe All Data (Double Confirm)", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Step 1 Wipe Confirmation Dialog
    if (showFirstWipeDialog) {
        AlertDialog(
            onDismissRequest = { showFirstWipeDialog = false },
            title = {
                Text(
                    text = "⚠️ Are you sure?",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will permanently clear your local vault, contacts, and custom links from DataStore. Do you want to proceed to confirmation?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFirstWipeDialog = false
                        wipeConfirmationText = ""
                        showSecondWipeDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Continue to Wipe", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirstWipeDialog = false }) {
                    Text("Cancel", color = PlugmeSub)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Step 2 Wipe Confirmation Dialog (Requires typing "RESET" or confirming)
    if (showSecondWipeDialog) {
        AlertDialog(
            onDismissRequest = { showSecondWipeDialog = false },
            title = {
                Text(
                    text = "Final Confirmation",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = PlugmeCoral
                )
            },
            text = {
                Column {
                    Text("To prevent accidental data loss, please type RESET below to confirm data wipe:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = wipeConfirmationText,
                        onValueChange = { wipeConfirmationText = it },
                        placeholder = { Text("RESET") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCoral,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wipe_confirm_text_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSecondWipeDialog = false
                        viewModel.wipeAllData {
                            // Data wiped and reset
                        }
                    },
                    enabled = wipeConfirmationText.trim().equals("RESET", ignoreCase = true),
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCoral),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("wipe_final_button")
                ) {
                    Text("Erase Everything Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSecondWipeDialog = false }) {
                    Text("Cancel", color = PlugmeSub)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PlugmeText
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = PlugmeSub
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
