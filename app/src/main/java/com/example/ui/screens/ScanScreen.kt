package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.qr.ProfileQrGenerator
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr.QrCodeHelper
import com.example.scanner.CameraScannerView
import com.example.model.Contact
import com.example.model.PlatformCatalog
import com.example.ui.theme.AvatarGradients
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel
import com.example.util.DeviceContactsHelper

@Composable
fun ScanScreen(
    viewModel: PlugmeViewModel,
    onNavigateToVault: () -> Unit,
    onScannerStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingQr.collectAsStateWithLifecycle()
    val selectedQrColor by viewModel.selectedQrColor.collectAsStateWithLifecycle()
    val embedCenterBadge by viewModel.embedCenterBadge.collectAsStateWithLifecycle()

    var isScanningMode by remember { mutableStateOf(false) }
    var scannedContact by remember { mutableStateOf<Contact?>(null) }
    var showFullscreenQr by remember { mutableStateOf(false) }

    // Notify parent if scanner is open to hide floating action buttons
    onScannerStateChanged(isScanningMode)

    BackHandler(enabled = isScanningMode) {
        isScanningMode = false
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isScanningMode = true
        } else {
            viewModel.emitMessage("📷 Camera access denied")
        }
    }

    fun startScanning() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            isScanningMode = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val scope = rememberCoroutineScope()
    var isSharingCard by remember { mutableStateOf(false) }

    fun shareProfile(asImage: Boolean = true) {
        scope.launch {
            isSharingCard = true
            try {
                val shareIntent = ProfileQrGenerator.createShareIntent(context, profile, shareCardImage = asImage)
                val shareChooser = Intent.createChooser(shareIntent, "Share your Plugme card")
                context.startActivity(shareChooser)
            } catch (_: Exception) {
                val shareUrl = ProfileQrGenerator.createProfileShareUrl(profile)
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Plugme Connect Card", shareUrl))
                viewModel.emitMessage("Link copied to clipboard: $shareUrl")
            } finally {
                isSharingCard = false
            }
        }
    }

    if (isScanningMode) {
        // Camera Scanner View
        Box(modifier = modifier.fillMaxSize()) {
            CameraScannerView(
                onQrDetected = { scannedString ->
                    val contact = QrCodeHelper.decodeFromUrlOrRaw(scannedString)
                    if (contact != null && (contact.n.isNotBlank() || contact.u.isNotBlank())) {
                        scannedContact = contact
                    } else {
                        viewModel.emitMessage("❌ Could not read contact data from QR code")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Bar Overlay
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scan Digital Business Card",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = { isScanningMode = false },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .testTag("close_scanner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Scanner",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Instructions Overlay
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Align any business card QR code within the frame",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ML Kit scanner automatically detects and parses Plugme cards, vCards, and social handles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { isScanningMode = false },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Switch to My QR Code", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // My QR Code View
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Plug Card",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = PlugmeText
            )
            Text(
                text = "Share your entire digital presence with one scan",
                style = MaterialTheme.typography.bodySmall,
                color = PlugmeSub,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // High-resolution 240.dp QR Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Header in QR Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SignatureGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.initials,
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = profile.n.ifBlank { "Plugme Card" },
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = PlugmeText
                            )
                            Text(
                                text = "@${profile.u.ifBlank { "plug" }}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PlugmeCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Display Container (240.dp) - Tap to expand
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .clickable { showFullscreenQr = true }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.n.isBlank() && profile.u.isBlank()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "👤", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Profile Not Set",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PlugmeText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Set up your card in the 'Me' tab to display your personal QR code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeSub,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else if (isGenerating || qrBitmap == null) {
                            CircularProgressIndicator(
                                color = PlugmeCyan,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Plugme QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("my_qr_code_image")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tap QR to enlarge for instant scanning 🔍",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeCyan,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QR Styling Studio (Theme Colors & Center Badge)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom QR Studio 🎨",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PlugmeText
                        )
                        Text(
                            text = "Live Auto-Update",
                            style = MaterialTheme.typography.labelSmall,
                            color = PlugmeCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Accent Color:",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset color options
                    val colorOptions = listOf(
                        "Cyan" to android.graphics.Color.rgb(0, 170, 255),
                        "Coral" to android.graphics.Color.rgb(255, 122, 89),
                        "Gold" to android.graphics.Color.rgb(255, 184, 0),
                        "Violet" to android.graphics.Color.rgb(139, 92, 246),
                        "Emerald" to android.graphics.Color.rgb(16, 185, 129),
                        "Obsidian" to android.graphics.Color.rgb(26, 29, 41)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colorOptions.forEach { (label, colorInt) ->
                            val isSelected = (selectedQrColor == colorInt)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) PlugmeText else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setQrColor(colorInt) }
                                    .testTag("qr_color_${label.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Avatar Badge",
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = PlugmeText
                            )
                            Text(
                                text = "Embed initials in center of QR",
                                style = MaterialTheme.typography.bodySmall,
                                color = PlugmeSub
                            )
                        }

                        FilterChip(
                            selected = embedCenterBadge,
                            onClick = { viewModel.toggleCenterBadge() },
                            label = {
                                Text(
                                    text = if (embedCenterBadge) "Badge: ON 🏷️" else "Badge: OFF",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE0F4FF),
                                selectedLabelColor = PlugmeCyan
                            ),
                            modifier = Modifier.testTag("toggle_badge_chip")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: ⚡ Share / 📷 Scan / 🔄 Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { shareProfile() },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("share_qr_button")
                ) {
                    Text(text = "⚡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Share", fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = { startScanning() },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFE0F4FF),
                        contentColor = PlugmeCyan
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("open_camera_scan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Scan", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.refreshQr() },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("refresh_qr_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh QR",
                        tint = PlugmeSub,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary presentation & link row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showFullscreenQr = true },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("present_fullscreen_qr_button")
                ) {
                    Icon(imageVector = Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Present 🔍", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val shareUrl = ProfileQrGenerator.createProfileShareUrl(profile)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Plugme Connect Card", shareUrl))
                        viewModel.emitMessage("🔗 Profile link copied to clipboard!")
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("copy_profile_link_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Link 🔗", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Modal dialog displayed when a QR card is scanned
    if (scannedContact != null) {
        val detected = scannedContact!!
        AlertDialog(
            onDismissRequest = { scannedContact = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📇", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scanned Digital Card",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PlugmeText
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(AvatarGradients[0]))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = detected.initials,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = detected.n.ifBlank { detected.u },
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PlugmeText
                    )

                    Text(
                        text = "@${detected.u.ifBlank { "plug" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PlugmeCyan
                    )

                    if (detected.title.isNotBlank() || detected.company.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = listOf(detected.title, detected.company).filter { it.isNotBlank() }.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = PlugmeSub
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (detected.title.isNotBlank()) {
                                Text(
                                    text = "💼 ${detected.title}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeText
                                )
                            }
                            if (detected.company.isNotBlank()) {
                                Text(
                                    text = "🏢 ${detected.company}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeText
                                )
                            }
                            if (detected.e.isNotBlank()) {
                                Text(
                                    text = "✉️ ${detected.e}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeText
                                )
                            }
                            val phone = detected.effectivePhone
                            if (phone.isNotBlank()) {
                                Text(
                                    text = "📞 $phone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeText
                                )
                            }
                            val linkCount = detected.platforms.filterKeys { it != "ph" }.size
                            if (linkCount > 0) {
                                Text(
                                    text = "🔗 $linkCount social & web links",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeSub
                                )
                            }
                            if (detected.gms.isNotEmpty()) {
                                Text(
                                    text = "🎮 ${detected.gms.size} gamer tags",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlugmeSub
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Button: Save directly to Device Contacts
                    Button(
                        onClick = {
                            val launched = DeviceContactsHelper.saveToDeviceContacts(context, detected)
                            viewModel.saveEditedContact("", detected) {
                                if (launched) {
                                    viewModel.emitMessage("📇 Opening Contacts & saved to Vault!")
                                } else {
                                    viewModel.emitMessage("🎉 Added ${detected.n.ifBlank { detected.u }} to Vault!")
                                }
                                scannedContact = null
                                isScanningMode = false
                                onNavigateToVault()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("scanned_save_device_contacts_button")
                    ) {
                        Text(text = "📇", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save to Phone Contacts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Button: Save to Vault Only
                    FilledTonalButton(
                        onClick = {
                            viewModel.saveEditedContact("", detected) {
                                viewModel.emitMessage("🎉 Added ${detected.n.ifBlank { detected.u }} to Vault!")
                                scannedContact = null
                                isScanningMode = false
                                onNavigateToVault()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = PlugmeText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("scanned_save_vault_button")
                    ) {
                        Text(text = "🫧 Save to Vault Only", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { scannedContact = null },
                    modifier = Modifier.testTag("scanned_cancel_button")
                ) {
                    Text("Scan Another", color = PlugmeSub)
                }
            }
        )
    }

    // Fullscreen Presentation Mode for high-visibility scanning across rooms / tables
    if (showFullscreenQr) {
        AlertDialog(
            onDismissRequest = { showFullscreenQr = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(SignatureGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.initials,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.n.ifBlank { "My Plugme Card" },
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = PlugmeText
                    )
                    Text(
                        text = "@${profile.u.ifBlank { "plug" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PlugmeCyan
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = "Fullscreen QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = PlugmeCyan)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hold up for others to scan with any camera app",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFullscreenQr = false },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val shareUrl = ProfileQrGenerator.createProfileShareUrl(profile)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Plugme Connect Card", shareUrl))
                        viewModel.emitMessage("🔗 Link copied to clipboard!")
                    }
                ) {
                    Text("Copy Link", color = PlugmeCyan)
                }
            }
        )
    }
}
