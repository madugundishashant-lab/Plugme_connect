package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.model.PlatformCatalog
import com.example.ui.theme.AvatarGradients
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel
import com.example.util.DeviceContactsHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContactDetailScreen(
    contact: Contact,
    viewModel: PlugmeViewModel,
    onBack: () -> Unit,
    onEdit: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = contact.n.ifBlank { contact.u },
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PlugmeText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(contact) }, modifier = Modifier.testTag("detail_edit_action")) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Contact",
                            tint = PlugmeCyan
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }, modifier = Modifier.testTag("detail_delete_action")) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Contact",
                            tint = PlugmeCoral
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
            // Big Avatar Bubble
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(AvatarGradients[0]))
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.initials,
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = contact.n.ifBlank { contact.u },
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = PlugmeText
            )

            Text(
                text = "@${contact.u}",
                style = MaterialTheme.typography.bodyLarge,
                color = PlugmeCyan,
                fontWeight = FontWeight.SemiBold
            )

            if (contact.title.isNotBlank() || contact.company.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOf(contact.title, contact.company).filter { it.isNotBlank() }.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PlugmeText,
                    fontWeight = FontWeight.Medium
                )
            }

            // Age, Gender, Phone pill
            val metaParts = mutableListOf<String>()
            if (contact.effectivePhone.isNotBlank()) metaParts.add("📞 ${contact.effectivePhone}")
            if (contact.a != null && contact.a in 1..150) metaParts.add("${contact.a} yrs")
            if (contact.g.isNotBlank()) metaParts.add(contact.g)
            if (contact.e.isNotBlank()) metaParts.add(contact.e)

            if (metaParts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    metaParts.forEach { part ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        ) {
                            Text(
                                text = part,
                                style = MaterialTheme.typography.labelMedium,
                                color = PlugmeSub,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social & Web Links section
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Socials & Links (${contact.platforms.size})",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (contact.platforms.isEmpty()) {
                        Text(
                            text = "No links shared yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            contact.platforms.forEach { (key, value) ->
                                val info = PlatformCatalog.get(key)
                                val destinationUrl = PlatformCatalog.buildUrl(key, value)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                        .clickable {
                                            try {
                                                uriHandler.openUri(destinationUrl)
                                            } catch (_: Exception) {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Plug Link", destinationUrl))
                                                viewModel.emitMessage("Copied $destinationUrl to clipboard")
                                            }
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = info.emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = info.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PlugmeSub
                                            )
                                            Text(
                                                text = value,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PlugmeText
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = "Open Link",
                                            tint = PlugmeCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gaming section
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Gaming Tags (${contact.gms.size})",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (contact.gms.isEmpty()) {
                        Text(
                            text = "No gaming tags shared.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            contact.gms.forEach { game ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFF8FAFC),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Gamer Tag", game.i))
                                            viewModel.emitMessage("Copied ${game.i} to clipboard")
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "🎮", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = game.n,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = PlugmeSub
                                                )
                                                Text(
                                                    text = game.i,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PlugmeText
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Tap to Copy",
                                            fontSize = 11.sp,
                                            color = PlugmeCyan,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save to Device Contacts Action
            Button(
                onClick = {
                    val launched = DeviceContactsHelper.saveToDeviceContacts(context, contact)
                    if (launched) {
                        viewModel.emitMessage("📇 Opening Contacts app for ${contact.n.ifBlank { contact.u }}...")
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("contact_save_to_device_button")
            ) {
                Text(text = "📇", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save to Device Contacts",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Edit (secondary) & Remove (danger)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEdit(contact) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("contact_edit_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Edit Contact", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCoral),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("contact_remove_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Remove", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Confirmation dialog for removing a contact
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Remove Plug?",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to remove ${contact.n.ifBlank { contact.u }} from your Plugme vault? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.removeContact(contact) {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = PlugmeSub)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
