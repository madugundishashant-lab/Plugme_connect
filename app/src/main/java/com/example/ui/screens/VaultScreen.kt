package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Contact
import com.example.ui.components.BubbleCard
import com.example.ui.components.FeaturedContactBubble
import com.example.ui.components.StatBubble
import com.example.ui.theme.PlugmeCoral
import com.example.ui.theme.PlugmeCyan
import com.example.ui.theme.PlugmeGold
import com.example.ui.theme.PlugmeSub
import com.example.ui.theme.PlugmeText
import com.example.ui.theme.SignatureGradient
import com.example.ui.theme.SyneFontFamily
import com.example.ui.viewmodel.PlugmeViewModel
import com.example.ui.viewmodel.VaultFilter

@Composable
fun VaultScreen(
    viewModel: PlugmeViewModel,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    val vault by viewModel.vault.collectAsStateWithLifecycle()
    val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Calculate stat totals
    val totalPlugs = vault.size
    val totalGames = vault.sumOf { it.gms.size }
    val totalSocials = vault.sumOf { it.platforms.size }

    // Featured row conditions: filter == ALL, searchQuery is blank, and at least 3 contacts exist
    val showFeatured = selectedFilter == VaultFilter.ALL && searchQuery.isBlank() && vault.size >= 3
    val featuredContacts = if (showFeatured) vault.take(3) else emptyList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("vault_grid")
    ) {
        // Hero Banner with Pill
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Plugme Vault",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = PlugmeText
                        )
                        Text(
                            text = "Instant digital connections",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlugmeSub
                        )
                    }

                    // "{n} Plugs" pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SignatureGradient)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$totalPlugs Plugs ⚡",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Debounced Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = {
                        Text(
                            "Search plugs, gamer tags, socials...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PlugmeSub
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PlugmeCyan
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = PlugmeSub
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                        focusedBorderColor = PlugmeCyan,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips (All / Recent / Gaming / Social)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VaultFilter.entries.forEach { filter ->
                        val isSelected = (selectedFilter == filter)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFilterSelect(filter) },
                            label = {
                                Text(
                                    text = when (filter) {
                                        VaultFilter.ALL -> "All (${vault.size})"
                                        VaultFilter.RECENT -> "Recent"
                                        VaultFilter.GAMING -> "Gaming"
                                        VaultFilter.SOCIAL -> "Social"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PlugmeCyan,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = PlugmeText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFFE2E8F0),
                                selectedBorderColor = PlugmeCyan
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3 Stat Bubbles (Plugs cyan / Games coral / Socials gold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBubble(
                        label = "Plugs",
                        count = totalPlugs,
                        accentColor = PlugmeCyan,
                        emoji = "🫧",
                        modifier = Modifier.weight(1f)
                    )
                    StatBubble(
                        label = "Games",
                        count = totalGames,
                        accentColor = PlugmeCoral,
                        emoji = "🎮",
                        modifier = Modifier.weight(1f)
                    )
                    StatBubble(
                        label = "Socials",
                        count = totalSocials,
                        accentColor = PlugmeGold,
                        emoji = "🔗",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Featured row of 3 circular bubbles (only when filter=All, no search, size>=3)
                if (showFeatured) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Featured Connections",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PlugmeText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (contact in featuredContacts) {
                            FeaturedContactBubble(
                                contact = contact,
                                onClick = { onContactClick(contact) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (searchQuery.isNotBlank()) "Search Results (${filteredContacts.size})" else "All Plugs (${filteredContacts.size})",
                    fontFamily = SyneFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PlugmeText
                )
            }
        }

        // Empty States
        if (filteredContacts.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "🔍" else "🫧",
                            fontSize = 44.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No plugs matching \"$searchQuery\"" else "No plugs in your vault yet",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PlugmeText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try another keyword or username" else "Tap 📷 Scan or + to add your first plug!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PlugmeSub,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // 3-Column Grid of BubbleCards
            itemsIndexed(
                items = filteredContacts,
                key = { _, item -> "${item.u}_${item.n}" }
            ) { index, contact ->
                BubbleCard(
                    contact = contact,
                    index = index,
                    onClick = { onContactClick(contact) }
                )
            }
        }
    }
}
