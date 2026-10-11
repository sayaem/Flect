package com.example.ui.presets

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.PresetLibrary
import com.example.model.FilterPreset
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHigher
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PresetsLibraryScreen(
    favoritePresetIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onSelectPreset: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) } // 0 = All, 1 = Favorites, 2 = Recent

    val presets = remember(selectedFilterIndex, favoritePresetIds) {
        when (selectedFilterIndex) {
            1 -> PresetLibrary.allPresets.filter { favoritePresetIds.contains(it.id) }
            2 -> PresetLibrary.allPresets.take(6)
            else -> PresetLibrary.allPresets
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar: Back, "Presets"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("presets_lib_back")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Presets",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            )
        }

        // Segmented Pills: [All] [Favorites] [Recent]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceHigher)
                    .padding(3.dp)
            ) {
                Row {
                    listOf("All", "Favorites", "Recent").forEachIndexed { index, title ->
                        val isSelected = selectedFilterIndex == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) AccentViolet else Color.Transparent)
                                .clickable { selectedFilterIndex = index }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical List of Presets
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets, key = { it.id }) { preset ->
                val isFav = favoritePresetIds.contains(preset.id)
                PresetRowItem(
                    preset = preset,
                    isFavorite = isFav,
                    onClick = { onSelectPreset(preset.id) },
                    onToggleFavorite = { onToggleFavorite(preset.id) }
                )
            }
        }
    }
}

@Composable
private fun PresetRowItem(
    preset: FilterPreset,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Image(
                painter = painterResource(
                    id = if (preset.category.name == "FILM" || preset.id == "portrait") {
                        R.drawable.sample_portrait
                    } else {
                        R.drawable.sample_travel
                    }
                ),
                contentDescription = preset.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Titles
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = preset.tagline,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 1
            )
        }

        // Heart Icon
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) AccentViolet else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
