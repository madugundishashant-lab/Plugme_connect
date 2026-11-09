package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qr.ProfileQrGenerator
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel
import com.example.util.DeviceContactsHelper
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: PlugmeViewModel,
    onNavigateToScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(profile.n) }
    var title by remember { mutableStateOf(profile.title) }
    var company by remember { mutableStateOf(profile.company) }
    var email by remember { mutableStateOf(profile.e) }
    var phone by remember { mutableStateOf(profile.effectivePhone) }
    var username by remember { mutableStateOf(profile.u) }
    var ageText by remember { mutableStateOf(profile.a?.toString() ?: "") }
    var gender by remember { mutableStateOf(profile.g) }

    var nameError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var showAdditionalDetails by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        name = profile.n
        title = profile.title
        company = profile.company
        email = profile.e
        phone = profile.effectivePhone
        username = profile.u
        ageText = profile.a?.toString() ?: ""
        gender = profile.g
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile Management",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = PlugmeText
        )
        Text(
            text = "Manage your digital business card & generate custom QR code",
            style = MaterialTheme.typography.bodySmall,
            color = PlugmeSub,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Avatar & Room Persistence Status Badge
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(SignatureGradient)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.initials,
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Persistence Indicator Pill
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9),
            modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Persisted Locally in Room Database",
                    style = MaterialTheme.typography.labelSmall,
                    color = PlugmeText,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Form Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Personal & Professional Details",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PlugmeText
                )
                Text(
                    text = "These details populate your personal QR code and vCard",
                    style = MaterialTheme.typography.bodySmall,
                    color = PlugmeSub
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Full Name *
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Alex Morgan") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PlugmeCyan)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Full name is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Job Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Job Title") },
                    placeholder = { Text("e.g. Senior Software Architect") },
                    leadingIcon = {
                        Text("💼", fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Company
                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company / Organization") },
                    placeholder = { Text("e.g. Plugme Technologies") },
                    leadingIcon = {
                        Text("🏢", fontSize = 16.sp, modifier = Modifier.padding(start = 12.dp, end = 4.dp))
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_company_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("e.g. alex@example.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = PlugmeCyan)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Phone Number
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("e.g. +1 (555) 789-2024") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = PlugmeCyan)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 6. Username *
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.removePrefix("@")
                        if (it.isNotBlank()) usernameError = false
                    },
                    label = { Text("Username *") },
                    placeholder = { Text("e.g. alex_m") },
                    leadingIcon = { Text("@", color = PlugmeCyan, fontWeight = FontWeight.Bold) },
                    isError = usernameError,
                    supportingText = if (usernameError) {
                        { Text("Username is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_username_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Additional Details Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdditionalDetails = !showAdditionalDetails }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAdditionalDetails) "Hide Demographic Info ▲" else "Add Age & Gender (Optional) ▼",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (showAdditionalDetails) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = {
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    ageText = it.take(3)
                                }
                            },
                            label = { Text("Age (1-150)") },
                            placeholder = { Text("17") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PlugmeCyan,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_age_input")
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            placeholder = { Text("e.g. Male") },
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PlugmeCyan,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("profile_gender_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Profile Button
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedUser = username.trim()

                        if (trimmedName.isBlank()) {
                            nameError = true
                            return@Button
                        }
                        if (trimmedUser.isBlank()) {
                            usernameError = true
                            return@Button
                        }

                        val parsedAge = ageText.toIntOrNull()?.coerceIn(1, 150)
                        viewModel.saveProfile(
                            name = trimmedName,
                            username = trimmedUser,
                            email = email.trim(),
                            title = title.trim(),
                            company = company.trim(),
                            phone = phone.trim(),
                            age = parsedAge,
                            gender = gender.trim()
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_profile_button")
                ) {
                    Text(
                        text = "Save Profile to Room DB & Update QR ✅",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal QR Code Preview & Sharing Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = PlugmeCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Personal QR Code",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PlugmeText
                        )
                        Text(
                            text = "Auto-generated from your Room-persisted profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Active Profile Summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = profile.n.ifBlank { "Your Name" },
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PlugmeText
                        )
                        if (profile.title.isNotBlank() || profile.company.isNotBlank()) {
                            Text(
                                text = listOf(profile.title, profile.company).filter { it.isNotBlank() }.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = PlugmeCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (profile.e.isNotBlank()) {
                            Text(
                                text = "✉️ ${profile.e}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PlugmeSub
                            )
                        }
                        if (profile.effectivePhone.isNotBlank()) {
                            Text(
                                text = "📞 ${profile.effectivePhone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PlugmeSub
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                            .clickable { onNavigateToScan() }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Personal Profile QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📱", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No personal QR code yet",
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PlugmeText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fill in your profile details above and tap 'Save Profile' to generate your custom QR code.",
                                style = MaterialTheme.typography.bodySmall,
                                color = PlugmeSub,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToScan,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("profile_view_qr_button")
                    ) {
                        Text(text = "QR Studio 🎨", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val shareIntent = ProfileQrGenerator.createShareIntent(context, profile, shareCardImage = true)
                                    val chooser = Intent.createChooser(shareIntent, "Share Digital Business Card")
                                    context.startActivity(chooser)
                                } catch (_: Exception) {
                                    val shareUrl = ProfileQrGenerator.createProfileShareUrl(profile)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Plugme Connect Card", shareUrl))
                                    viewModel.emitMessage("Card link copied to clipboard")
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("profile_share_card_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share 📤", fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Device Contacts export button
                OutlinedButton(
                    onClick = {
                        val saved = DeviceContactsHelper.saveToDeviceContacts(context, profile)
                        if (saved) {
                            viewModel.emitMessage("📇 Opening Device Contacts with profile details")
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("profile_export_contacts_button")
                ) {
                    Text(text = "Export to Phone Contacts 📇", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
