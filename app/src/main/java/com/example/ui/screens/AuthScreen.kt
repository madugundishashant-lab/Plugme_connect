package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeGold
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: PlugmeViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val scope = rememberCoroutineScope()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val currentUser by viewModel.syncManager.currentUser.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncManager.syncStatus.collectAsStateWithLifecycle()

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isPro) "Plugme Pro Account" else "Plugme Account",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("auth_back_button")) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isPro && currentUser != null) {
                // ACTIVE PRO ACCOUNT STATE
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚡", fontSize = 32.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Plugme Pro Active",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PlugmeText
                        )

                        Text(
                            text = currentUser?.email ?: "Signed in with Firebase",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PlugmeSub
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Pro Perks Card
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Your Pro Perks:",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PlugmeText
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "✔ Realtime Database cloud backup", style = MaterialTheme.typography.bodySmall, color = PlugmeText)
                                Text(text = "✔ Up to 25 social platform links", style = MaterialTheme.typography.bodySmall, color = PlugmeText)
                                Text(text = "✔ Up to 15 gamer tags & identifiers", style = MaterialTheme.typography.bodySmall, color = PlugmeText)
                                Text(text = "✔ Cross-device restore on sign-in", style = MaterialTheme.typography.bodySmall, color = PlugmeText)
                            }
                        }

                        if (syncStatus != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (syncStatus!!.contains("restricted", ignoreCase = true) || syncStatus!!.contains("rules", ignoreCase = true))
                                    Color(0xFFFFF7ED) else Color(0xFFF0FDF4),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = syncStatus!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (syncStatus!!.contains("restricted", ignoreCase = true) || syncStatus!!.contains("rules", ignoreCase = true))
                                            PlugmeCoral else Color(0xFF16A34A)
                                    )
                                    if (syncStatus!!.contains("restricted", ignoreCase = true) || syncStatus!!.contains("rules", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "💡 Local data remains safely stored. To enable cloud sync, allow authenticated read/write under users/\$uid in your Firebase Console Realtime Database rules.",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = PlugmeSub
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.syncNow()
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("pro_sync_button")
                        ) {
                            Text("Sync Cloud Data Now ☁️", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.syncManager.signOut()
                                viewModel.emitMessage("Signed out. Switched to Free mode.")
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("pro_logout_button")
                        ) {
                            Text("Log Out", color = PlugmeCoral, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // SIGN IN / SIGN UP FORM
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
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PlugmeGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSignUp) "Create Pro Account" else "Sign In to Plugme Pro",
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PlugmeText
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSignUp) "Sign up to backup your vault to Realtime Database and expand link limits."
                            else "Sign in to restore your cloud contacts and enable Pro features.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email input
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text("Email Address") },
                            placeholder = { Text("name@example.com") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = PlugmeCyan)
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
                                .testTag("auth_email_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password input (min 6 chars)
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PlugmeCyan)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PlugmeCyan,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = PlugmeCoral,
                                fontWeight = FontWeight.Medium
                            )

                            // Quick CTA to switch to Sign In if the user is already registered
                            if (isSignUp && (
                                errorMessage!!.contains("already in use", ignoreCase = true) ||
                                errorMessage!!.contains("already registered", ignoreCase = true) ||
                                errorMessage!!.contains("sign in instead", ignoreCase = true)
                            )) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        isSignUp = false
                                        errorMessage = null
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PlugmeCyan),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_switch_to_signin_cta")
                                ) {
                                    Text("Switch to Sign In 🔑", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button with Loading Spinner
                        Button(
                            onClick = {
                                val cleanEmail = email.trim()
                                val cleanPass = password.trim()

                                if (cleanEmail.isBlank()) {
                                    errorMessage = "Please enter your email"
                                    return@Button
                                }
                                if (cleanPass.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null

                                scope.launch {
                                    val result = if (isSignUp) {
                                        viewModel.syncManager.signUp(cleanEmail, cleanPass)
                                    } else {
                                        viewModel.syncManager.signIn(cleanEmail, cleanPass)
                                    }
                                    isLoading = false

                                    result.fold(
                                        onSuccess = {
                                            viewModel.emitMessage(
                                                if (isSignUp) "🎉 Pro account created! Synced to cloud."
                                                else "☁️ Welcome back! Pro account synced."
                                            )
                                            onBack()
                                        },
                                        onFailure = { ex ->
                                            errorMessage = ex.message ?: "Authentication failed"
                                            viewModel.emitMessage("❌ $errorMessage")
                                        }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text(
                                    text = if (isSignUp) "Sign Up for Pro ⚡" else "Sign In 🚀",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toggle Sign In / Sign Up
                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                errorMessage = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PlugmeCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
