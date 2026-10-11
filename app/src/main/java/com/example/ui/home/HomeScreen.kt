package com.example.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.PresetLibrary
import com.example.model.FilterPreset
import com.example.ui.components.FlectLogoMark
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.FlectBrandGradient
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onSelectImageUri: (Uri) -> Unit,
    onSelectSampleImage: (isPortrait: Boolean) -> Unit,
    onOpenPhotoPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMyStyles: () -> Unit
) {
    val systemPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSelectImageUri(uri)
        }
    }

    val favoritePresets = listOf(
        PresetLibrary.getById("golden_hour") ?: PresetLibrary.allPresets[0],
        PresetLibrary.getById("cinemood") ?: PresetLibrary.allPresets[2],
        PresetLibrary.getById("vivid") ?: PresetLibrary.allPresets[7],
        PresetLibrary.getById("mono") ?: PresetLibrary.allPresets[9]
    )

    val recentPresets = listOf(
        PresetLibrary.getById("travel") ?: PresetLibrary.allPresets[11],
        PresetLibrary.getById("portrait") ?: PresetLibrary.allPresets[10],
        PresetLibrary.getById("soft_film") ?: PresetLibrary.allPresets[1],
        PresetLibrary.getById("night") ?: PresetLibrary.allPresets[8]
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // 1. Compact Top Bar (Logo + Name + Settings icon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlectLogoMark(size = 28.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Flect",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Tagline
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "One tap. Your look.",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 3. Large Prominent "Edit a Photo" Gradient Button
        Button(
            onClick = onOpenPhotoPicker,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("pick_photo_button"),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TextPrimary
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FlectBrandGradient, RoundedCornerShape(27.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Edit a Photo",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick demo photo pill row (for instant zero-friction testing)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Or try demo: ",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 12.sp),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = "Portrait",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .clickable { onSelectSampleImage(true) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("try_sample_portrait_button")
            )
            Text(text = "•", color = TextMuted, modifier = Modifier.align(Alignment.CenterVertically))
            Text(
                text = "Travel",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AccentViolet,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .clickable { onSelectSampleImage(false) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .testTag("try_sample_travel_button")
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 4. "Favorite Styles" Section
        SectionHeader(
            title = "Favorite Styles",
            actionText = "See all",
            onActionClick = onOpenMyStyles
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(favoritePresets) { preset ->
                HomeStyleCard(
                    preset = preset,
                    isSamplePortrait = preset.id == "golden_hour" || preset.id == "vivid",
                    onClick = { onSelectSampleImage(preset.id == "golden_hour" || preset.id == "vivid") }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. "Recently Used" Section
        SectionHeader(
            title = "Recently Used",
            actionText = "See all",
            onActionClick = onOpenMyStyles
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(recentPresets) { preset ->
                HomeStyleCard(
                    preset = preset,
                    isSamplePortrait = preset.id == "portrait" || preset.id == "soft_film",
                    onClick = { onSelectSampleImage(preset.id == "portrait" || preset.id == "soft_film") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 17.sp
            )
        )

        Text(
            text = actionText,
            style = MaterialTheme.typography.bodySmall.copy(
                color = AccentViolet,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            ),
            modifier = Modifier
                .clickable { onActionClick() }
                .padding(vertical = 4.dp, horizontal = 2.dp)
        )
    }
}

@Composable
private fun HomeStyleCard(
    preset: FilterPreset,
    isSamplePortrait: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(78.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = painterResource(
                    id = if (isSamplePortrait) R.drawable.sample_portrait else R.drawable.sample_travel
                ),
                contentDescription = preset.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = preset.name,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}
