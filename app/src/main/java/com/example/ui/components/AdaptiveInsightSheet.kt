package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdaptiveAdjustments
import com.example.model.FilterPreset
import com.example.model.ImageAnalysisResult
import com.example.ui.theme.AmberWarmth
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceVariant
import com.example.ui.theme.ElectricBlueAccent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.MintHighlight
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurfaceDark
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
        containerColor = ObsidianSurfaceDark,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
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
                            .size(38.dp)
                            .background(LavenderAccent.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, GlassBorderActive, CircleShape),
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
                                color = TextPrimary,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Text(
                            text = "Live 32-bit Dynamic Calibration",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(GlassSurface, CircleShape)
                        .border(1.dp, GlassBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Master Engine Toggle Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (adaptiveEnabled) GlassBorderActive else GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Intelligent Tuning",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            if (adaptiveEnabled) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MintHighlight)
                                )
                            }
                        }
                        Text(
                            text = if (adaptiveEnabled) "Active: Bounded tone compensations applied" else "Bypassed: Pure rigid preset",
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

            // Visual Luminance Distribution Bar
            Text(
                text = "DYNAMIC RANGE & LUMINANCE PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Visual Spectrum Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF1A1A24),
                                        Color(0xFF3F465C),
                                        Color(0xFF7E8BA6),
                                        Color(0xFFE0E5F5)
                                    )
                                )
                            )
                    ) {}

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SHADOW (P10)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("${(analysis.p10Shadows * 100).toInt()}%", fontSize = 12.sp, color = ElectricBlueAccent, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MIDTONES (P50)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("${(analysis.p50Midtones * 100).toInt()}%", fontSize = 12.sp, color = LavenderAccent, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("HIGHLIGHT (P90)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text("${(analysis.p90Highlights * 100).toInt()}%", fontSize = 12.sp, color = ChampagneGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MetricRow(
                        label = "Scene Lighting Profile",
                        value = analysis.dominantTone.name.replace("_", " "),
                        highlightColor = when (analysis.dominantTone) {
                            ImageAnalysisResult.DominantTone.WARM_GOLDEN -> AmberWarmth
                            ImageAnalysisResult.DominantTone.COOL_SHADOW -> ElectricBlueAccent
                            ImageAnalysisResult.DominantTone.LOW_LIGHT_NIGHT -> LavenderAccent
                            else -> MintHighlight
                        }
                    )
                    MetricRow(
                        label = "Color Warmth Bias",
                        value = if (analysis.warmCoolBias > 0.08f) "Warm (+${(analysis.warmCoolBias * 100).toInt()}%)" else if (analysis.warmCoolBias < -0.08f) "Cool (${(analysis.warmCoolBias * 100).toInt()}%)" else "Neutral Balance",
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
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (preset == null) {
                        Text(
                            text = "Toggle any preset style in the strip to view its live bounded compensations.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    } else if (!adaptiveEnabled) {
                        Text(
                            text = "Adaptive engine is disabled. Showing static fixed parameters.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    } else {
                        InsightItem(
                            title = "Skin Tone Orange-Guard",
                            detail = if (adaptiveAdjustments.warmthOffset != 0f) {
                                "Warmth dampened by ${(adaptiveAdjustments.warmthOffset * 100).toInt()}% preventing unnatural saturated skin."
                            } else {
                                "Skin warmth levels within natural safety threshold."
                            },
                            isActive = adaptiveAdjustments.warmthOffset != 0f
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InsightItem(
                            title = "Highlight Headroom Guard",
                            detail = if (adaptiveAdjustments.highlightCompression != 0f) {
                                "Highlight compression active (${(adaptiveAdjustments.highlightCompression * 100).toInt()}%) protecting skies and bright textures."
                            } else {
                                "Highlights clean and safe from digital blowout."
                            },
                            isActive = adaptiveAdjustments.highlightCompression != 0f
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InsightItem(
                            title = "Shadow Detail Rescue",
                            detail = if (adaptiveAdjustments.shadowRescue != 0f) {
                                "Shadows lifted (+${(adaptiveAdjustments.shadowRescue * 100).toInt()}%) to recover dark subject texture."
                            } else {
                                "Shadow detail preserved without noisy lifting."
                            },
                            isActive = adaptiveAdjustments.shadowRescue != 0f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .background(if (isActive) MintHighlight.copy(alpha = 0.15f) else CharcoalSurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Shield,
                contentDescription = null,
                tint = if (isActive) MintHighlight else TextMuted,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) TextPrimary else TextSecondary,
                    fontSize = 13.sp
                )
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
            )
        }
    }
}
