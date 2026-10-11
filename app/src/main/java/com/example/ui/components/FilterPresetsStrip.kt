package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.MintHighlight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Modern horizontal scrollable strip of filter presets.
 * Users can tap to toggle filters on or off, adjust intensity, and filter by category.
 */
@Composable
fun FilterPresetsStrip(
    selectedPresetId: String?,
    presetIntensity: Float,
    originalBitmap: Bitmap,
    thumbnails: Map<String, Bitmap>,
    favoritePresetIds: Set<String>,
    onTogglePreset: (String?) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onIntensityCommit: (Float) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenBottomSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(PresetCategory.ALL) }
    val listState = rememberLazyListState()

    val activePreset = remember(selectedPresetId) {
        PresetLibrary.getById(selectedPresetId)
    }

    val displayedPresets = remember(selectedCategory, favoritePresetIds) {
        when (selectedCategory) {
            PresetCategory.ALL -> PresetLibrary.allPresets
            else -> PresetLibrary.allPresets.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CharcoalSurface)
            .padding(vertical = 10.dp)
    ) {
        // Top row: Category chips + "All Presets" Bottom Sheet button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(PresetCategory.values()) { category ->
                    val isCatSelected = selectedCategory == category
                    val count = if (category == PresetCategory.ALL) {
                        PresetLibrary.allPresets.size
                    } else {
                        PresetLibrary.allPresets.count { it.category == category }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCatSelected) LavenderAccent else CharcoalSurfaceVariant)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = when (category) {
                                PresetCategory.ALL -> "All ($count)"
                                PresetCategory.ESSENTIALS -> "Essentials"
                                PresetCategory.FILM -> "Film"
                                PresetCategory.CINEMATIC -> "Cinematic"
                                PresetCategory.MOOD -> "Mood"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isCatSelected) ObsidianBackground else TextSecondary,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Button to open Full Presets Bottom Sheet
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CharcoalSurfaceVariant)
                    .clickable { onOpenBottomSheet() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("open_presets_bottom_sheet_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Browse All Presets",
                        tint = LavenderAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sheet",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = LavenderAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Scrollable Strip of Presets
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("filter_presets_horizontal_strip"),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Original (None) Preset Toggle Item
            item {
                OriginalPresetStripItem(
                    isSelected = selectedPresetId == null,
                    originalBitmap = originalBitmap,
                    onClick = { onTogglePreset(null) }
                )
            }

            // Presets
            items(displayedPresets, key = { it.id }) { preset ->
                val isSelected = selectedPresetId == preset.id
                val isFavorite = favoritePresetIds.contains(preset.id)
                val thumb = thumbnails[preset.id]

                PresetStripCard(
                    preset = preset,
                    isSelected = isSelected,
                    isFavorite = isFavorite,
                    thumbnail = thumb,
                    onClick = {
                        // Toggle: if already selected, clicking it turns it off (resets to original)
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

        // Intensity slider: dynamically slides open when a preset is toggled on
        AnimatedVisibility(visible = activePreset != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${activePreset?.name}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Intensity",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Text(
                        text = "${(presetIntensity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = LavenderAccent
                        )
                    )
                }

                Slider(
                    value = presetIntensity,
                    onValueChange = onIntensityChange,
                    onValueChangeFinished = { onIntensityCommit(presetIntensity) },
                    valueRange = 0f..1f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("intensity_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = LavenderAccent,
                        activeTrackColor = LavenderAccent,
                        inactiveTrackColor = CharcoalBorder
                    )
                )
            }
        }
    }
}

@Composable
private fun OriginalPresetStripItem(
    isSelected: Boolean,
    originalBitmap: Bitmap,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) LavenderAccent else CharcoalBorder,
        animationSpec = spring(),
        label = "border_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clickable { onClick() }
            .testTag("preset_item_original")
    ) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Image(
                bitmap = originalBitmap.asImageBitmap(),
                contentDescription = "Original unedited photo",
                modifier = Modifier.fillMaxSize()
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(LavenderAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ObsidianBackground,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "None",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Original",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) LavenderAccent else TextSecondary,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun PresetStripCard(
    preset: FilterPreset,
    isSelected: Boolean,
    isFavorite: Boolean,
    thumbnail: Bitmap?,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) LavenderAccent else CharcoalBorder,
        animationSpec = spring(),
        label = "border_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(74.dp)
            .clickable { onClick() }
            .testTag("preset_item_${preset.id}")
    ) {
        Box(
            modifier = Modifier
                .size(74.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(14.dp)
                )
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
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = LavenderAccent
                    )
                }
            }

            // Favorite star/heart icon button
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
                    .size(20.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .clickable { onToggleFavorite() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite preset",
                    tint = if (isFavorite) AmberWarmth else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(11.dp)
                )
            }

            // Selected checkmark indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(LavenderAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ObsidianBackground,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Category tag indicator on bottom
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        if (isSelected) LavenderAccent.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.65f)
                    )
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.name,
                    color = if (isSelected) ObsidianBackground else Color.White,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = preset.category.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) LavenderAccent else TextMuted,
                fontSize = 10.sp
            )
        )
    }
}
