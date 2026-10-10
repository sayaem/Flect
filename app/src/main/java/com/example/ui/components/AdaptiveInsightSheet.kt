package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdaptiveAdjustments
import com.example.model.FilterPreset
import com.example.model.ImageAnalysisResult
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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveInsightSheet(
    analysis: ImageAnalysisResult,
    adaptiveAdjustments: AdaptiveAdjustments,
    preset: FilterPreset?,
    adaptiveEnabled: Boolean,
    onToggleAdaptive: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = LavenderAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Adaptive Intelligence",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Photo-specific dynamic tuning",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Switch toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Adaptive Filter Engine",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = if (adaptiveEnabled) "Active: Bounded adjustments applied" else "Bypassed: Pure fixed preset",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                    Switch(
                        checked = adaptiveEnabled,
                        onCheckedChange = onToggleAdaptive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBackground,
                            checkedTrackColor = LavenderAccent,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CharcoalBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Detected Characteristics
            Text(
                text = "IMAGE METRICS DETECTED",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ObsidianBackground),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    MetricRow(
                        label = "Scene Lighting Tone",
                        value = analysis.dominantTone.name.replace("_", " "),
                        highlightColor = when (analysis.dominantTone) {
                            ImageAnalysisResult.DominantTone.WARM_GOLDEN -> AmberWarmth
                            ImageAnalysisResult.DominantTone.COOL_SHADOW -> ElectricBlueAccent
                            ImageAnalysisResult.DominantTone.LOW_LIGHT_NIGHT -> LavenderAccent
                            else -> MintHighlight
                        }
                    )
                    MetricRow(
                        label = "Mean Luminance",
                        value = "${(analysis.meanLuminance * 100).toInt()}%",
                        highlightColor = TextPrimary
                    )
                    MetricRow(
                        label = "Dynamic Contrast",
                        value = if (analysis.contrastStdDev > 0.25f) "High Contrast" else if (analysis.contrastStdDev < 0.13f) "Soft / Flat" else "Balanced",
                        highlightColor = TextPrimary
                    )
                    MetricRow(
                        label = "Color Warmth Bias",
                        value = if (analysis.warmCoolBias > 0.08f) "Warm (+${(analysis.warmCoolBias * 100).toInt()}%)" else if (analysis.warmCoolBias < -0.08f) "Cool (${(analysis.warmCoolBias * 100).toInt()}%)" else "Neutral",
                        highlightColor = if (analysis.warmCoolBias > 0.08f) AmberWarmth else if (analysis.warmCoolBias < -0.08f) ElectricBlueAccent else TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Bounded Compensations
            Text(
                text = "ACTIVE BOUNDED COMPENSATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (preset == null) {
                        Text(
                            text = "Select a preset to view its real-time bounded compensations.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    } else if (!adaptiveEnabled) {
                        Text(
                            text = "Adaptive engine is disabled. Showing fixed preset defaults.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    } else {
                        InsightItem(
                            title = "Skin Tone Protection",
                            detail = if (adaptiveAdjustments.warmthOffset != 0f) {
                                "Warmth clamped by ${(adaptiveAdjustments.warmthOffset * 100).toInt()}% to prevent unnatural orange skin."
                            } else {
                                "Natural skin warmth maintained within safety margins."
                            },
                            isActive = adaptiveAdjustments.warmthOffset != 0f
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        InsightItem(
                            title = "Highlight Headroom Guard",
                            detail = if (adaptiveAdjustments.highlightCompression != 0f) {
                                "Highlight compression active (${(adaptiveAdjustments.highlightCompression * 100).toInt()}%) preventing blown skies and whites."
                            } else {
                                "Highlight levels are safe from digital clipping."
                            },
                            isActive = adaptiveAdjustments.highlightCompression != 0f
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        InsightItem(
                            title = "Shadow Detail Recovery",
                            detail = if (adaptiveAdjustments.shadowRescue != 0f) {
                                "Shadows gently lifted (+${(adaptiveAdjustments.shadowRescue * 100).toInt()}%) to reveal dark texture."
                            } else {
                                "Shadow depth balanced without crushing detail."
                            },
                            isActive = adaptiveAdjustments.shadowRescue != 0f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, highlightColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
        )
    }
}

@Composable
private fun InsightItem(title: String, detail: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Shield,
            contentDescription = null,
            tint = if (isActive) MintHighlight else TextMuted,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) TextPrimary else TextSecondary
                )
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}
