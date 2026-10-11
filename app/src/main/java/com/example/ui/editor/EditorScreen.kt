package com.example.ui.editor

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PresetLibrary
import com.example.model.EditParameters
import com.example.model.FilterPreset
import com.example.ui.components.SplitComparisonView
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BorderActive
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FlectBrandGradient
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavySecondaryBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHigher
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EditorUiState
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.ExportState

enum class EditorBottomTab {
    FILTERS,
    ADJUST,
    CROP
}

@Composable
fun EditorScreen(
    state: EditorUiState.Ready,
    viewModel: EditorViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var bottomTab by remember { mutableStateOf(EditorBottomTab.FILTERS) }
    var adjustSubTab by remember { mutableIntStateOf(0) } // 0 = Adjustments, 1 = Filters
    var isSplitComparisonMode by remember { mutableStateOf(false) }

    val activePreset = remember(state.parameters.presetId) {
        PresetLibrary.getById(state.parameters.presetId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Mockup Top Bar: Back, Undo, Redo, Checkmark (Save)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.undo() },
                    enabled = state.canUndo,
                    modifier = Modifier.testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (state.canUndo) TextPrimary else TextMuted
                    )
                }

                IconButton(
                    onClick = { viewModel.redo() },
                    enabled = state.canRedo,
                    modifier = Modifier.testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (state.canRedo) TextPrimary else TextMuted
                    )
                }
            }

            IconButton(
                onClick = { viewModel.exportImage() },
                enabled = state.exportState !is ExportState.Exporting,
                modifier = Modifier.testTag("export_button")
            ) {
                if (state.exportState is ExportState.Exporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AccentViolet,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save and export",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Large Photo Preview Area (Mockups 4, 5, 6)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
        ) {
            SplitComparisonView(
                originalBitmap = state.loadedImage.previewBitmap,
                renderedBitmap = state.renderedBitmap,
                isComparing = state.isComparing,
                splitPosition = state.splitPosition,
                onSplitChange = { viewModel.setSplitPosition(it) },
                showSplitMode = isSplitComparisonMode,
                modifier = Modifier.fillMaxSize()
            )

            // In Split Comparison Mode: Show "Before" and "After" frosted pills
            if (isSplitComparisonMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Before",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "After",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick toggle button for Split Mode or Hold to compare
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSplitComparisonMode) AccentViolet else Color.Black.copy(alpha = 0.65f))
                        .clickable { isSplitComparisonMode = !isSplitComparisonMode }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("toggle_split_mode_button")
                ) {
                    Text(
                        text = if (isSplitComparisonMode) "Split View" else "Compare",
                        color = if (isSplitComparisonMode) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3. Middle Controls Area
        when (bottomTab) {
            EditorBottomTab.FILTERS -> {
                // Mockup 4: Filter Carousel + Intensity Slider
                FilterSelectionPanel(
                    state = state,
                    activePreset = activePreset,
                    onSelectPreset = { viewModel.selectPreset(it) },
                    onIntensityChange = { viewModel.setPresetIntensity(it) },
                    onIntensityCommit = { viewModel.commitPresetIntensity(it) }
                )
            }
            EditorBottomTab.ADJUST -> {
                // Mockup 5: Segmented [Adjustments] [Filters] + Compact adjustment rows
                AdjustmentsPanel(
                    state = state,
                    subTab = adjustSubTab,
                    onSubTabChange = { adjustSubTab = it },
                    onSelectPreset = { viewModel.selectPreset(it) },
                    onUpdateAdjustment = { exp, con, sat, warm, high, shad ->
                        viewModel.updateManualAdjustment(exp, con, sat, warm, high, shad, null, null)
                    },
                    onCommit = { viewModel.commitManualAdjustment() }
                )
            }
            EditorBottomTab.CROP -> {
                // Crop & Rotate Options
                CropOptionsPanel(
                    onRotate = { /* Safe rotate trigger */ }
                )
            }
        }

        // 4. Mockup Bottom Toolbar: [Filters, Adjust, Crop]
        BottomEditorBar(
            currentTab = bottomTab,
            onTabSelected = { bottomTab = it }
        )
    }
}

@Composable
private fun FilterSelectionPanel(
    state: EditorUiState.Ready,
    activePreset: FilterPreset?,
    onSelectPreset: (String?) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onIntensityCommit: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Horizontal Carousel of Filter Thumbnails
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Original" card
            item {
                MockupPresetCard(
                    title = "Original",
                    isSelected = state.parameters.presetId == null,
                    bitmap = state.loadedImage.previewBitmap,
                    onClick = { onSelectPreset(null) }
                )
            }

            // 12 Presets
            items(PresetLibrary.allPresets, key = { it.id }) { preset ->
                val isSelected = state.parameters.presetId == preset.id
                val thumb = state.thumbnails[preset.id] ?: state.loadedImage.previewBitmap

                MockupPresetCard(
                    title = preset.name,
                    isSelected = isSelected,
                    bitmap = thumb,
                    onClick = { onSelectPreset(preset.id) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Intensity Slider Row: "Intensity" ... [Slider] ... "70%"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Intensity",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    fontSize = 13.sp
                ),
                modifier = Modifier.width(64.dp)
            )

            Slider(
                value = state.parameters.presetIntensity,
                onValueChange = onIntensityChange,
                onValueChangeFinished = { onIntensityCommit(state.parameters.presetIntensity) },
                valueRange = 0f..1f,
                modifier = Modifier
                    .weight(1f)
                    .testTag("intensity_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = TextPrimary,
                    activeTrackColor = AccentViolet,
                    inactiveTrackColor = SurfaceHigher
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "${(state.parameters.presetIntensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                ),
                modifier = Modifier.width(42.dp)
            )
        }
    }
}

@Composable
private fun MockupPresetCard(
    title: String,
    isSelected: Boolean,
    bitmap: Bitmap,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .testTag("preset_card_$title"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) BorderActive else BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 11.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun AdjustmentsPanel(
    state: EditorUiState.Ready,
    subTab: Int,
    onSubTabChange: (Int) -> Unit,
    onSelectPreset: (String?) -> Unit,
    onUpdateAdjustment: (exp: Float?, con: Float?, sat: Float?, warm: Float?, high: Float?, shad: Float?) -> Unit,
    onCommit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        // Segmented Control: [Adjustments] [Filters]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceHigher)
                    .padding(3.dp)
            ) {
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (subTab == 0) SurfaceElevated else Color.Transparent)
                            .clickable { onSubTabChange(0) }
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Adjustments",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (subTab == 0) TextPrimary else TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (subTab == 1) SurfaceElevated else Color.Transparent)
                            .clickable { onSubTabChange(1) }
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (subTab == 1) TextPrimary else TextMuted,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        if (subTab == 0) {
            // Mockup 5: Compact adjustment rows
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                val p = state.parameters

                AdjustmentRow(
                    icon = Icons.Default.LightMode,
                    label = "Exposure",
                    value = p.manualExposure,
                    valueText = String.format("%+.2f", p.manualExposure),
                    onValueChange = { onUpdateAdjustment(it, null, null, null, null, null) },
                    onCommit = onCommit
                )

                AdjustmentRow(
                    icon = Icons.Default.Brightness6,
                    label = "Contrast",
                    value = p.manualContrast,
                    valueText = String.format("%+d", (p.manualContrast * 50).toInt()),
                    onValueChange = { onUpdateAdjustment(null, it, null, null, null, null) },
                    onCommit = onCommit
                )

                AdjustmentRow(
                    icon = Icons.Default.InvertColors,
                    label = "Saturation",
                    value = p.manualSaturation,
                    valueText = String.format("%+d", (p.manualSaturation * 50).toInt()),
                    onValueChange = { onUpdateAdjustment(null, null, it, null, null, null) },
                    onCommit = onCommit
                )

                AdjustmentRow(
                    icon = Icons.Default.WbSunny,
                    label = "Warmth",
                    value = p.manualWarmth,
                    valueText = String.format("%+d", (p.manualWarmth * 50).toInt()),
                    onValueChange = { onUpdateAdjustment(null, null, null, it, null, null) },
                    onCommit = onCommit
                )

                AdjustmentRow(
                    icon = Icons.Default.WbIncandescent,
                    label = "Highlights",
                    value = p.manualHighlights,
                    valueText = String.format("%+d", (p.manualHighlights * 50).toInt()),
                    onValueChange = { onUpdateAdjustment(null, null, null, null, it, null) },
                    onCommit = onCommit
                )

                AdjustmentRow(
                    icon = Icons.Default.FilterDrama,
                    label = "Shadows",
                    value = p.manualShadows,
                    valueText = String.format("%+d", (p.manualShadows * 50).toInt()),
                    onValueChange = { onUpdateAdjustment(null, null, null, null, null, it) },
                    onCommit = onCommit
                )
            }
        } else {
            // Filters view inside sub-tab
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PresetLibrary.allPresets) { preset ->
                    val isSelected = state.parameters.presetId == preset.id
                    val thumb = state.thumbnails[preset.id] ?: state.loadedImage.previewBitmap
                    MockupPresetCard(
                        title = preset.name,
                        isSelected = isSelected,
                        bitmap = thumb,
                        onClick = { onSelectPreset(preset.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustmentRow(
    icon: ImageVector,
    label: String,
    value: Float,
    valueText: String,
    onValueChange: (Float) -> Unit,
    onCommit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.width(72.dp)
        )

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onCommit,
            valueRange = -1f..1f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = TextPrimary,
                activeTrackColor = AccentViolet,
                inactiveTrackColor = SurfaceHigher
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = valueText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(42.dp)
        )
    }
}

@Composable
private fun CropOptionsPanel(
    onRotate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aspect Ratio",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 12.sp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Free", "1:1", "4:5", "16:9").forEach { ratio ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceHigher)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .clickable { }
                ) {
                    Text(text = ratio, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceHigher)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .clickable { onRotate() }
            ) {
                Icon(
                    imageVector = Icons.Default.Rotate90DegreesCw,
                    contentDescription = "Rotate",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomEditorBar(
    currentTab: EditorBottomTab,
    onTabSelected: (EditorBottomTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(NavyBackground)
            .border(0.5.dp, BorderSubtle, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(
            title = "Filters",
            icon = Icons.Default.InvertColors,
            isSelected = currentTab == EditorBottomTab.FILTERS,
            onClick = { onTabSelected(EditorBottomTab.FILTERS) }
        )

        BottomBarItem(
            title = "Adjust",
            icon = Icons.Default.Tune,
            isSelected = currentTab == EditorBottomTab.ADJUST,
            onClick = { onTabSelected(EditorBottomTab.ADJUST) }
        )

        BottomBarItem(
            title = "Crop",
            icon = Icons.Default.Crop,
            isSelected = currentTab == EditorBottomTab.CROP,
            onClick = { onTabSelected(EditorBottomTab.CROP) }
        )
    }
}

@Composable
private fun BottomBarItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) AccentViolet else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) AccentViolet else TextMuted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        )
    }
}
