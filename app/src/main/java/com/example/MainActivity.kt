package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.Contact
import com.example.qr.QrCodeHelper
import com.example.ui.components.BubbleBackground
import com.example.ui.components.WelcomeOverlay
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ContactDetailScreen
import com.example.ui.screens.EditContactScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.screens.SocialsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.PlugmeTheme
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PlugmeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming deep link on launch
        processDeepLink(intent)

        setContent {
            PlugmeTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processDeepLink(intent)
    }

    private fun processDeepLink(intent: Intent?) {
        val dataUri = intent?.data ?: return
        val rawUriString = dataUri.toString()
        val paramD = dataUri.getQueryParameter("d")

        if (!paramD.isNullOrBlank()) {
            viewModel.handleDeepLink(paramD)
            intent.data = null // Clear intent to prevent re-processing on rotation
        } else if (rawUriString.contains("?d=")) {
            viewModel.handleDeepLink(rawUriString)
            intent.data = null
        }
    }
}

sealed class Screen(val route: String, val title: String, val emoji: String) {
    object Vault : Screen("vault", "Bubble", "🫧")
    object Scan : Screen("scan", "Scan", "📷")
    object Socials : Screen("socials", "Links", "🔗")
    object Profile : Screen("profile", "Me", "👤")
    object Settings : Screen("settings", "Setup", "⚙️")

    object ContactDetail : Screen("contact_detail", "Contact Detail", "")
    object EditContact : Screen("edit_contact", "Edit Contact", "")
    object Auth : Screen("auth", "Auth", "")
    object About : Screen("about", "About", "")
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Vault,
    Screen.Scan,
    Screen.Socials,
    Screen.Profile,
    Screen.Settings
)

@Composable
fun MainAppScreen(viewModel: PlugmeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Vault.route

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val welcomed by viewModel.welcomed.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val selectedContact by viewModel.selectedContact.collectAsStateWithLifecycle()

    var showWelcomeDialog by remember { mutableStateOf(false) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }

    // Listen for snackbar messages from ViewModel
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // First-run Welcome Overlay: show 600ms after launch if plugme_welcomed unset
    LaunchedEffect(welcomed) {
        if (!welcomed) {
            delay(600)
            showWelcomeDialog = true
        } else {
            showWelcomeDialog = false
        }
    }

    val isTopLevelRoute = currentRoute in BOTTOM_NAV_ITEMS.map { it.route }

    BubbleBackground {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            bottomBar = {
                if (isTopLevelRoute && !isCameraScannerOpen) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .height(72.dp)
                                .testTag("bottom_nav_bar")
                        ) {
                            BOTTOM_NAV_ITEMS.forEach { screen ->
                                val selected = (currentRoute == screen.route)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Text(
                                            text = screen.emoji,
                                            fontSize = if (selected) 22.sp else 18.sp
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontFamily = SyneFontFamily,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PlugmeCyan,
                                        selectedTextColor = PlugmeCyan,
                                        unselectedIconColor = PlugmeSub,
                                        unselectedTextColor = PlugmeSub,
                                        indicatorColor = Color(0xFFE0F4FF)
                                    ),
                                    modifier = Modifier.testTag("nav_item_${screen.route}")
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                // Fixed FABs: "+" (58.dp, gradient) + "⚡" share (52.dp, white).
                // Share FAB hidden on Scan route and while scanner is open.
                // Both hidden on sub-routes.
                if (isTopLevelRoute && !isCameraScannerOpen) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp, end = 4.dp)
                    ) {
                        // "⚡" Share FAB (hidden on Scan route)
                        AnimatedVisibility(
                            visible = currentRoute != Screen.Scan.route,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            val scope = androidx.compose.runtime.rememberCoroutineScope()
                            FloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            val shareIntent = com.example.qr.ProfileQrGenerator.createShareIntent(context, profile, shareCardImage = true)
                                            context.startActivity(Intent.createChooser(shareIntent, "Share your Plugme card"))
                                        } catch (_: Exception) {
                                            val shareUrl = com.example.qr.ProfileQrGenerator.createProfileShareUrl(profile)
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Plugme Connect Card", shareUrl))
                                            viewModel.emitMessage("Copied link to clipboard: $shareUrl")
                                        }
                                    }
                                },
                                shape = CircleShape,
                                containerColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(1.5.dp, Color(0xFFE2E8F0), CircleShape)
                                    .testTag("fab_share_card")
                            ) {
                                Text(text = "⚡", fontSize = 22.sp)
                            }
                        }

                        // "+" FAB (58.dp, gradient) -> opens Add Contact
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(SignatureGradient)
                                .clickable {
                                    viewModel.selectContact(null)
                                    navController.navigate(Screen.EditContact.route)
                                }
                                .testTag("fab_add_contact"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Contact",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Vault.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Vault.route) {
                    isCameraScannerOpen = false
                    VaultScreen(
                        viewModel = viewModel,
                        onContactClick = { contact ->
                            viewModel.selectContact(contact)
                            navController.navigate(Screen.ContactDetail.route)
                        }
                    )
                }

                composable(Screen.Scan.route) {
                    ScanScreen(
                        viewModel = viewModel,
                        onNavigateToVault = {
                            navController.navigate(Screen.Vault.route) {
                                popUpTo(Screen.Vault.route) { inclusive = true }
                            }
                        },
                        onScannerStateChanged = { isOpen ->
                            isCameraScannerOpen = isOpen
                        }
                    )
                }

                composable(Screen.Socials.route) {
                    isCameraScannerOpen = false
                    SocialsScreen(
                        viewModel = viewModel,
                        onNavigateToAuth = {
                            navController.navigate(Screen.Auth.route)
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    isCameraScannerOpen = false
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToScan = {
                            navController.navigate(Screen.Scan.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    isCameraScannerOpen = false
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToAuth = {
                            navController.navigate(Screen.Auth.route)
                        },
                        onNavigateToAbout = {
                            navController.navigate(Screen.About.route)
                        }
                    )
                }

                composable(Screen.ContactDetail.route) {
                    isCameraScannerOpen = false
                    val contactToDisplay = selectedContact ?: Contact(n = "Contact")
                    ContactDetailScreen(
                        contact = contactToDisplay,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onEdit = { contact ->
                            viewModel.selectContact(contact)
                            navController.navigate(Screen.EditContact.route)
                        }
                    )
                }

                composable(Screen.EditContact.route) {
                    isCameraScannerOpen = false
                    EditContactScreen(
                        contact = selectedContact,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Auth.route) {
                    isCameraScannerOpen = false
                    AuthScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.About.route) {
                    isCameraScannerOpen = false
                    AboutScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Welcome Overlay (First-run)
        WelcomeOverlay(
            visible = showWelcomeDialog,
            onDismiss = {
                showWelcomeDialog = false
                viewModel.setWelcomed()
            }
        )
    }
}
