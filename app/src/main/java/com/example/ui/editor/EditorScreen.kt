package com.example.ui.editor

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Filter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PresetLibrary
import com.example.model.FilterPreset
import com.example.model.PresetCategory
import com.example.ui.components.AdaptiveInsightSheet
import com.example.ui.components.ExportSuccessDialog
import com.example.ui.components.SplitComparisonView
import com.example.ui.theme.AmberWarmth
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceVariant
import com.example.ui.theme.ElectricBlueAccent
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.MintHighlight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EditorUiState
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.ExportState

@Composable
fun EditorScreen(
    state: EditorUiState.Ready,
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Presets, 1 = Manual Adjust
    var filterCategory by remember { mutableStateOf(PresetCategory.ALL) }
    var showAdaptiveSheet by remember { mutableStateOf(false) }
    var isSplitMode by remember { mutableStateOf(false) }

    val activePreset = remember(state.parameters.presetId) {
        PresetLibrary.getById(state.parameters.presetId)
    }

    // Export Success / Failure Dialogs
    when (val export = state.exportState) {
        is ExportState.Success -> {
            ExportSuccessDialog(
                savedUri = export.savedUri,
                shareUri = export.shareUri,
                onDismiss = { viewModel.dismissExportState() }
            )
        }
        is ExportState.Failure -> {
            // Error snackbar/dialog
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { viewModel.dismissExportState() },
                containerColor = CharcoalSurface,
                title = { Text("Export Error", color = TextPrimary) },
                text = { Text(export.error, color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissExportState() }) {
                        Text("OK", color = LavenderAccent)
                    }
                }
            )
        }
        else -> Unit
    }

    if (showAdaptiveSheet) {
        AdaptiveInsightSheet(
            analysis = state.analysis,
            adaptiveAdjustments = state.adaptiveAdjustments,
            preset = activePreset,
            adaptiveEnabled = state.parameters.adaptiveEnabled,
            onToggleAdaptive = { viewModel.toggleAdaptive(it) },
            onDismiss = { showAdaptiveSheet = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Top Navigation Bar
        EditorTopBar(
            canUndo = state.canUndo,
            canRedo = state.canRedo,
            isOriginal = state.parameters.isOriginal,
            adaptiveEngaged = state.adaptiveAdjustments.isEngaged && state.parameters.adaptiveEnabled,
            isExporting = state.exportState is ExportState.Exporting,
            onBack = onBack,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onReset = { viewModel.resetAll() },
            onOpenAdaptive = { showAdaptiveSheet = true },
            onExport = { viewModel.exportImage() }
        )

        // 2. Main Image Preview Area (takes maximum space)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
        ) {
            SplitComparisonView(
                originalBitmap = state.loadedImage.previewBitmap,
                renderedBitmap = state.renderedBitmap,
                isComparing = state.isComparing,
                splitPosition = state.splitPosition,
                onSplitChange = { viewModel.setSplitPosition(it) },
                showSplitMode = isSplitMode,
                modifier = Modifier.fillMaxSize()
            )

            // Hold-to-compare overlay button (bottom left of preview)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CharcoalSurface.copy(alpha = 0.85f))
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.setComparing(true)
                                tryAwaitRelease()
                                viewModel.setComparing(false)
                            }
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("hold_compare_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hold to compare",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Split Mode toggle button (bottom right of preview)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSplitMode) LavenderAccent else CharcoalSurface.copy(alpha = 0.85f))
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(20.dp))
                    .clickable { isSplitMode = !isSplitMode }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("toggle_split_mode_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Compare,
                        contentDescription = null,
                        tint = if (isSplitMode) ObsidianBackground else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSplitMode) "Split ON" else "Split View",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSplitMode) ObsidianBackground else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Subtle rendering indicator
            if (state.isRendering) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(CharcoalSurface.copy(alpha = 0.7f), CircleShape)
                        .padding(6.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = LavenderAccent
                    )
                }
            }
        }

        // 3. Compact Lower Controls Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface)
        ) {
            // Mode Tabs: "Presets" vs "Manual Adjust"
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CharcoalSurface,
                contentColor = TextPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LavenderAccent,
                        height = 2.5.dp
                    )
                },
                divider = { Box(Modifier.fillMaxWidth().height(1.dp).background(CharcoalBorder)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.height(46.dp).testTag("tab_presets"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Filter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adaptive Presets", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.height(46.dp).testTag("tab_adjust"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.parameters.hasManualAdjustments) "Adjust •" else "Adjust",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }

            // Tab Content
            if (selectedTab == 0) {
                // Presets tab content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    // Category Filter Pills: All, Favorites, Film, Cine, Mood
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PresetCategory.values()) { cat ->
                            val isSelected = filterCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) LavenderAccent else CharcoalSurfaceVariant)
                                    .clickable { filterCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = when (cat) {
                                        PresetCategory.ALL -> "All (${PresetLibrary.allPresets.size})"
                                        PresetCategory.ESSENTIALS -> "Essentials"
                                        PresetCategory.FILM -> "Film"
                                        PresetCategory.CINEMATIC -> "Cinematic"
                                        PresetCategory.MOOD -> "Mood"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) ObsidianBackground else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Filter Carousel with Real Thumbnails
                    val filteredList = remember(filterCategory) {
                        if (filterCategory == PresetCategory.ALL) {
                            PresetLibrary.allPresets
                        } else {
                            PresetLibrary.allPresets.filter { it.category == filterCategory }
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Original / None Card
                        item {
                            OriginalPresetCard(
                                isSelected = state.parameters.presetId == null,
                                originalBitmap = state.loadedImage.previewBitmap,
                                onClick = { viewModel.selectPreset(null) }
                            )
                        }

                        items(filteredList, key = { it.id }) { preset ->
                            val isSelected = state.parameters.presetId == preset.id
                            val isFavorite = state.favoritePresetIds.contains(preset.id)
                            val thumbBitmap = state.thumbnails[preset.id]

                            PresetCard(
                                preset = preset,
                                isSelected = isSelected,
                                isFavorite = isFavorite,
                                thumbnailBitmap = thumbBitmap,
                                onClick = { viewModel.selectPreset(preset.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(preset.id) }
                            )
                        }
                    }

                    // Intensity Slider: visible when a filter is active
                    AnimatedVisibility(visible = activePreset != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${activePreset?.name} Intensity",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "${(state.parameters.presetIntensity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = LavenderAccent
                                    )
                                )
                            }

                            Slider(
                                value = state.parameters.presetIntensity,
                                onValueChange = { viewModel.setPresetIntensity(it) },
                                onValueChangeFinished = {
                                    viewModel.commitPresetIntensity(state.parameters.presetIntensity)
                                },
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
            } else {
                // Manual Adjustments Tab
                ManualAdjustmentsPanel(
                    parameters = state.parameters,
                    onUpdateAdjustment = { exp, con, sat, warm, high, shad, vig, grn ->
                        viewModel.updateManualAdjustment(exp, con, sat, warm, high, shad, vig, grn)
                    },
                    onCommit = { viewModel.commitManualAdjustment() },
                    onResetManual = { viewModel.resetManualAdjustments() }
                )
            }
        }
    }
}

@Composable
private fun EditorTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    isOriginal: Boolean,
    adaptiveEngaged: Boolean,
    isExporting: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onReset: () -> Unit,
    onOpenAdaptive: () -> Unit,
    onExport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back
        IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to home",
                tint = TextPrimary
            )
        }

        // Center Actions: Undo, Redo, Reset, Adaptive Pill
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onUndo,
                enabled = canUndo,
                modifier = Modifier.testTag("undo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    tint = if (canUndo) TextPrimary else TextMuted
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo,
                modifier = Modifier.testTag("redo_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    tint = if (canRedo) TextPrimary else TextMuted
                )
            }

            IconButton(
                onClick = onReset,
                enabled = !isOriginal,
                modifier = Modifier.testTag("reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset",
                    tint = if (!isOriginal) TextPrimary else TextMuted
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Adaptive Insight Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (adaptiveEngaged) LavenderAccent.copy(alpha = 0.2f) else CharcoalSurfaceVariant)
                    .border(
                        1.dp,
                        if (adaptiveEngaged) LavenderAccent.copy(alpha = 0.6f) else CharcoalBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onOpenAdaptive() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("adaptive_chip")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (adaptiveEngaged) LavenderAccent else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (adaptiveEngaged) "Adaptive" else "Fixed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (adaptiveEngaged) LavenderAccent else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Save / Export Button
        Button(
            onClick = onExport,
            enabled = !isExporting,
            modifier = Modifier.testTag("export_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = LavenderAccent,
                contentColor = ObsidianBackground
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = ObsidianBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun OriginalPresetCard(
    isSelected: Boolean,
    originalBitmap: Bitmap,
    onClick: () -> Unit
) {
    val imageBitmap = remember(originalBitmap) { originalBitmap.asImageBitmap() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
            .testTag("preset_item_original")
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) LavenderAccent else CharcoalBorder,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Original photo",
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
private fun PresetCard(
    preset: FilterPreset,
    isSelected: Boolean,
    isFavorite: Boolean,
    thumbnailBitmap: Bitmap?,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
            .testTag("preset_item_${preset.id}")
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) LavenderAccent else CharcoalBorder,
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap.asImageBitmap(),
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

            // Favorite button in corner
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
                    contentDescription = "Favorite",
                    tint = if (isFavorite) AmberWarmth else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }

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
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = preset.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) LavenderAccent else TextPrimary,
                fontSize = 11.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun ManualAdjustmentsPanel(
    parameters: com.example.model.EditParameters,
    onUpdateAdjustment: (
        exposure: Float?,
        contrast: Float?,
        saturation: Float?,
        warmth: Float?,
        highlights: Float?,
        shadows: Float?,
        vignette: Float?,
        grain: Float?
    ) -> Unit,
    onCommit: () -> Unit,
    onResetManual: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FINE ADJUSTMENTS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            if (parameters.hasManualAdjustments) {
                TextButton(onClick = onResetManual) {
                    Text("Reset Adjustments", color = LavenderAccent, fontSize = 12.sp)
                }
            }
        }

        AdjustmentSlider(
            label = "Brightness / Exposure",
            value = parameters.manualExposure,
            range = -1f..1f,
            testTag = "manual_slider_exposure",
            onValueChange = { onUpdateAdjustment(it, null, null, null, null, null, null, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Contrast",
            value = parameters.manualContrast,
            range = -1f..1f,
            testTag = "manual_slider_contrast",
            onValueChange = { onUpdateAdjustment(null, it, null, null, null, null, null, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Saturation",
            value = parameters.manualSaturation,
            range = -1f..1f,
            testTag = "manual_slider_saturation",
            onValueChange = { onUpdateAdjustment(null, null, it, null, null, null, null, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Warmth",
            value = parameters.manualWarmth,
            range = -1f..1f,
            testTag = "manual_slider_warmth",
            onValueChange = { onUpdateAdjustment(null, null, null, it, null, null, null, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Highlights",
            value = parameters.manualHighlights,
            range = -1f..1f,
            testTag = "manual_slider_highlights",
            onValueChange = { onUpdateAdjustment(null, null, null, null, it, null, null, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Shadows",
            value = parameters.manualShadows,
            range = -1f..1f,
            testTag = "manual_slider_shadows",
            onValueChange = { onUpdateAdjustment(null, null, null, null, null, it, null, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Vignette",
            value = parameters.manualVignette,
            range = 0f..1f,
            testTag = "manual_slider_vignette",
            onValueChange = { onUpdateAdjustment(null, null, null, null, null, null, it, null) },
            onCommit = onCommit
        )

        AdjustmentSlider(
            label = "Film Grain",
            value = parameters.manualGrain,
            range = 0f..1f,
            testTag = "manual_slider_grain",
            onValueChange = { onUpdateAdjustment(null, null, null, null, null, null, null, it) },
            onCommit = onCommit
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    testTag: String,
    onValueChange: (Float) -> Unit,
    onCommit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            Text(
                text = "${(value * 100).toInt()}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (value != 0f) LavenderAccent else TextMuted
                )
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onCommit,
            valueRange = range,
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            colors = SliderDefaults.colors(
                thumbColor = LavenderAccent,
                activeTrackColor = LavenderAccent,
                inactiveTrackColor = CharcoalBorder
            )
        )
    }
}
