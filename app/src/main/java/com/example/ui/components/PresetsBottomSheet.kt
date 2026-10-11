package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PresetLibrary
import com.example.model.FilterPreset
import com.example.model.PresetCategory
import com.example.ui.theme.AmberWarmth
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceVariant
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Bottom Sheet displaying full preset library in a responsive grid layout.
 * Users can tap any preset card to toggle the filter on their photograph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsBottomSheet(
    selectedPresetId: String?,
    thumbnails: Map<String, Bitmap>,
    favoritePresetIds: Set<String>,
    onTogglePreset: (String?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedCategory by remember { mutableStateOf(PresetCategory.ALL) }

    val categories = remember { PresetCategory.values() }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == PresetCategory.ALL) {
            PresetLibrary.allPresets
        } else {
            PresetLibrary.allPresets.filter { it.category == selectedCategory }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(LavenderAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Filter Presets",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Tap to toggle filter style on your photo",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_presets_sheet")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = CharcoalSurface,
                contentColor = TextPrimary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val index = categories.indexOf(selectedCategory)
                    if (index in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = LavenderAccent,
                            height = 2.dp
                        )
                    }
                },
                divider = { Box(Modifier.fillMaxWidth().height(1.dp).background(CharcoalBorder)) }
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        modifier = Modifier.height(44.dp),
                        text = {
                            Text(
                                text = when (category) {
                                    PresetCategory.ALL -> "All (12)"
                                    PresetCategory.ESSENTIALS -> "Essentials"
                                    PresetCategory.FILM -> "Film"
                                    PresetCategory.CINEMATIC -> "Cinematic"
                                    PresetCategory.MOOD -> "Mood"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LavenderAccent else TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Presets Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .testTag("presets_grid_sheet"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPresets, key = { it.id }) { preset ->
                    val isSelected = selectedPresetId == preset.id
                    val isFavorite = favoritePresetIds.contains(preset.id)
                    val thumbBitmap = thumbnails[preset.id]

                    PresetGridCard(
                        preset = preset,
                        isSelected = isSelected,
                        isFavorite = isFavorite,
                        thumbnail = thumbBitmap,
                        onClick = {
                            // Toggle filter
                            if (isSelected) {
                                onTogglePreset(null)
                            } else {
                                onTogglePreset(preset.id)
                            }
                        },
                        onToggleFavorite = { onToggleFavorite(preset.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Reset / Done Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedPresetId != null) {
                    Button(
                        onClick = { onTogglePreset(null) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CharcoalSurfaceVariant,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset to Original")
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderAccent,
                        contentColor = ObsidianBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply & Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PresetGridCard(
    preset: FilterPreset,
    isSelected: Boolean,
    isFavorite: Boolean,
    thumbnail: Bitmap?,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("preset_grid_card_${preset.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CharcoalSurfaceVariant else ObsidianBackground
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) LavenderAccent else CharcoalBorder
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = preset.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CharcoalSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = LavenderAccent,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // Favorite button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) AmberWarmth else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Selected badge
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(LavenderAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ObsidianBackground,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) LavenderAccent else TextPrimary
                    )
                )
                Text(
                    text = if (isSelected) "Active" else "Tap",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) LavenderAccent else TextMuted,
                        fontSize = 10.sp
                    )
                )
            }

            Text(
                text = preset.tagline,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                ),
                maxLines = 2
            )
        }
    }
}
