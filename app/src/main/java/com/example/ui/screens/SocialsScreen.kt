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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameEntry
import com.example.model.PlatformCatalog
import com.example.model.SocialLink
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeGold
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel

@Composable
fun SocialsScreen(
    viewModel: PlugmeViewModel,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()

    val maxSocials = if (isPro) 25 else 6
    val maxGames = if (isPro) 15 else 5

    // Local mutable state for editing links and gaming tags
    val socialRows = remember { mutableStateListOf<SocialLink>() }
    val gameRows = remember { mutableStateListOf<GameEntry>() }

    // Initialize from profile when profile loads
    LaunchedEffect(profile) {
        if (socialRows.isEmpty() && profile.platforms.isNotEmpty()) {
            socialRows.clear()
            profile.platforms.forEach { (k, v) ->
                socialRows.add(SocialLink(k, v))
            }
        }
        if (gameRows.isEmpty() && profile.gms.isNotEmpty()) {
            gameRows.clear()
            gameRows.addAll(profile.gms)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Links & Gaming",
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = PlugmeText
        )
        Text(
            text = "Configure the handles that appear when someone scans you",
            style = MaterialTheme.typography.bodySmall,
            color = PlugmeSub
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pro Status / Upgrade Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isPro) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isPro) Color(0xFFA5D6A7) else Color(0xFFFFCC80),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isPro) "⚡" else "⭐", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPro) "Pro Account Active" else "Free Plan Limits",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PlugmeText
                        )
                        Text(
                            text = if (isPro) "Up to 25 socials & 15 games enabled" else "Up to 6 socials & 5 games (Pro for 25/15)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )
                    }
                }

                if (!isPro) {
                    OutlinedButton(
                        onClick = onNavigateToAuth,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("upgrade_pro_banner_button")
                    ) {
                        Text("Get Pro", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SOCIAL PLATFORMS SECTION
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
                        text = "Social Links (${socialRows.size}/$maxSocials)",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PlugmeText
                    )

                    if (socialRows.size < maxSocials) {
                        OutlinedButton(
                            onClick = {
                                val available = (if (isPro) PlatformCatalog.ALL_PLATFORMS else PlatformCatalog.FREE_PLATFORMS)
                                val unused = available.firstOrNull { p -> socialRows.none { it.k == p.key } } ?: available.first()
                                socialRows.add(SocialLink(unused.key, ""))
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("add_social_row_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (socialRows.size >= maxSocials && !isPro) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 Free limit reached (6 links). Sign in or upgrade to Pro for up to 25 links!",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeCoral,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (socialRows.isEmpty()) {
                    Text(
                        text = "No links added yet. Tap Add to attach Instagram, Twitter, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        socialRows.forEachIndexed { index, link ->
                            SocialRowEditor(
                                link = link,
                                isPro = isPro,
                                onUpdate = { updated -> socialRows[index] = updated },
                                onRemove = { socialRows.removeAt(index) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // GAMING SECTION
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
                        text = "Gaming Tags (${gameRows.size}/$maxGames)",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PlugmeText
                    )

                    if (gameRows.size < maxGames) {
                        OutlinedButton(
                            onClick = {
                                gameRows.add(GameEntry("", ""))
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("add_game_row_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (gameRows.isEmpty()) {
                    Text(
                        text = "No gaming tags added yet. Tap Add to add Valorant, Fortnite, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlugmeSub
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        gameRows.forEachIndexed { index, game ->
                            GameRowEditor(
                                game = game,
                                onUpdate = { updated -> gameRows[index] = updated },
                                onRemove = { gameRows.removeAt(index) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                val platformMap = socialRows
                    .filter { it.v.isNotBlank() }
                    .associate { it.k to it.v.trim() }
                val validGames = gameRows
                    .filter { it.n.isNotBlank() && it.i.isNotBlank() }

                viewModel.saveSocialsAndGames(platformMap, validGames)
            },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PlugmeCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_socials_button")
        ) {
            Text(
                text = "Save Links & Games ⚡",
                fontFamily = SyneFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SocialRowEditor(
    link: SocialLink,
    isPro: Boolean,
    onUpdate: (SocialLink) -> Unit,
    onRemove: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    val currentPlatform = PlatformCatalog.get(link.k)
    val availablePlatforms = if (isPro) PlatformCatalog.ALL_PLATFORMS else PlatformCatalog.FREE_PLATFORMS

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Platform Selector Box
        Box {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier
                    .width(110.dp)
                    .height(54.dp)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                    .clickable { dropdownExpanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = currentPlatform.emoji, fontSize = 18.sp)
                    Text(
                        text = currentPlatform.name.take(7),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PlugmeText
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = PlugmeSub,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                availablePlatforms.forEach { platform ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = platform.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = platform.name)
                                if (platform.isPro) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "PRO", fontSize = 10.sp, color = PlugmeCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        onClick = {
                            dropdownExpanded = false
                            onUpdate(link.copy(k = platform.key))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Value text field
        OutlinedTextField(
            value = link.v,
            onValueChange = { onUpdate(link.copy(v = it)) },
            placeholder = { Text(currentPlatform.placeholder, style = MaterialTheme.typography.bodySmall) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlugmeCyan,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Delete button
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Link",
                tint = PlugmeSub
            )
        }
    }
}

@Composable
private fun GameRowEditor(
    game: GameEntry,
    onUpdate: (GameEntry) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Game name input
        OutlinedTextField(
            value = game.n,
            onValueChange = { onUpdate(game.copy(n = it)) },
            placeholder = { Text("Game Name (e.g. Valorant)", style = MaterialTheme.typography.bodySmall) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlugmeCyan,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Gamer ID input
        OutlinedTextField(
            value = game.i,
            onValueChange = { onUpdate(game.copy(i = it)) },
            placeholder = { Text("GamerTag / ID", style = MaterialTheme.typography.bodySmall) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlugmeCyan,
                unfocusedBorderColor = Color(0xFFE2E8F0)
            ),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete Game",
                tint = PlugmeSub
            )
        }
    }
}
