package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Contact
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    contact: Contact?,
    viewModel: PlugmeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val isNew = (contact == null)
    val originalKey = remember { contact?.u ?: contact?.n ?: "" }

    var name by remember { mutableStateOf(contact?.n ?: "") }
    var username by remember { mutableStateOf(contact?.u ?: "") }
    var title by remember { mutableStateOf(contact?.title ?: "") }
    var company by remember { mutableStateOf(contact?.company ?: "") }
    var email by remember { mutableStateOf(contact?.e ?: "") }
    var phone by remember { mutableStateOf(contact?.effectivePhone ?: "") }
    var ageText by remember { mutableStateOf(contact?.a?.toString() ?: "") }
    var gender by remember { mutableStateOf(contact?.g ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNew) "Add New Plug" else "Edit Contact",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("edit_contact_back")) {
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
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Contact Details",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Name*
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Alex Rivera") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("Name is required", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCyan,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username*
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it.removePrefix("@")
                            if (it.isNotBlank()) usernameError = false
                        },
                        label = { Text("Username *") },
                        placeholder = { Text("e.g. arivera") },
                        isError = usernameError,
                        supportingText = if (usernameError) {
                            { Text("Username is required", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        leadingIcon = { Text("@", color = PlugmeCyan, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCyan,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Job Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Job Title") },
                        placeholder = { Text("e.g. Lead Engineer") },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCyan,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Company
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company") },
                        placeholder = { Text("e.g. Cyberdeck") },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCyan,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        placeholder = { Text("e.g. alex@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCyan,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. +1 (555) 345-9871") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PlugmeCyan,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Age and Gender
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = {
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    ageText = it.take(3)
                                }
                            },
                            label = { Text("Age (1-150)") },
                            placeholder = { Text("21") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PlugmeCyan,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("contact_age_input")
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            placeholder = { Text("e.g. Non-binary") },
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PlugmeCyan,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("contact_gender_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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

                            val updated = Contact(
                                n = trimmedName,
                                u = trimmedUser,
                                e = email.trim(),
                                title = title.trim(),
                                company = company.trim(),
                                phone = phone.trim(),
                                a = parsedAge,
                                g = gender.trim(),
                                platforms = contact?.platforms ?: emptyMap(),
                                gms = contact?.gms ?: emptyList()
                            )

                            viewModel.saveEditedContact(originalKey, updated) {
                                onBack()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_contact_button")
                    ) {
                        Text(
                            text = if (isNew) "Add to Vault ⚡" else "Save Changes ✅",
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
