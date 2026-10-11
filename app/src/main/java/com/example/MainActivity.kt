package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.editor.EditorScreen
import com.example.ui.home.HomeScreen
import com.example.ui.picker.PhotoPickerScreen
import com.example.ui.presets.MyStylesScreen
import com.example.ui.presets.PresetsLibraryScreen
import com.example.ui.saveshare.SaveShareScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.AccentViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EditorUiState
import com.example.ui.viewmodel.EditorViewModel
import com.example.ui.viewmodel.ExportState

enum class AppScreen {
    SPLASH,
    HOME,
    PHOTO_PICKER,
    EDITOR,
    SAVE_SHARE,
    PRESETS_LIBRARY,
    MY_STYLES,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val editorViewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
                val uiState by editorViewModel.uiState.collectAsStateWithLifecycle()

                when (currentScreen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onSplashComplete = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.HOME -> {
                        HomeScreen(
                            onSelectImageUri = { uri ->
                                editorViewModel.openImage(uri)
                                currentScreen = AppScreen.EDITOR
                            },
                            onSelectSampleImage = { isPortrait ->
                                editorViewModel.openSampleImage(isPortrait)
                                currentScreen = AppScreen.EDITOR
                            },
                            onOpenPhotoPicker = { currentScreen = AppScreen.PHOTO_PICKER },
                            onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                            onOpenMyStyles = { currentScreen = AppScreen.MY_STYLES }
                        )
                    }

                    AppScreen.PHOTO_PICKER -> {
                        PhotoPickerScreen(
                            onSelectImageUri = { uri ->
                                editorViewModel.openImage(uri)
                                currentScreen = AppScreen.EDITOR
                            },
                            onSelectSampleImage = { isPortrait ->
                                editorViewModel.openSampleImage(isPortrait)
                                currentScreen = AppScreen.EDITOR
                            },
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.EDITOR -> {
                        when (val state = uiState) {
                            is EditorUiState.Loading -> {
                                LoadingScreen(message = state.message)
                            }

                            is EditorUiState.Ready -> {
                                // Automatically navigate to Save & Share when export finishes
                                if (state.exportState is ExportState.Success) {
                                    SaveShareScreen(
                                        savedUri = state.exportState.savedUri,
                                        shareUri = state.exportState.shareUri,
                                        renderedBitmap = state.renderedBitmap,
                                        onBack = {
                                            editorViewModel.dismissExportState()
                                            currentScreen = AppScreen.HOME
                                        },
                                        onKeepEditing = {
                                            editorViewModel.dismissExportState()
                                        }
                                    )
                                } else {
                                    EditorScreen(
                                        state = state,
                                        viewModel = editorViewModel,
                                        onBack = {
                                            editorViewModel.closeEditor()
                                            currentScreen = AppScreen.HOME
                                        }
                                    )
                                }
                            }

                            is EditorUiState.Error -> {
                                ErrorScreen(
                                    message = state.message,
                                    onDismiss = {
                                        editorViewModel.closeEditor()
                                        currentScreen = AppScreen.HOME
                                    }
                                )
                            }

                            is EditorUiState.Empty -> {
                                currentScreen = AppScreen.HOME
                            }
                        }
                    }

                    AppScreen.SAVE_SHARE -> {
                        val state = uiState as? EditorUiState.Ready
                        if (state != null && state.exportState is ExportState.Success) {
                            SaveShareScreen(
                                savedUri = state.exportState.savedUri,
                                shareUri = state.exportState.shareUri,
                                renderedBitmap = state.renderedBitmap,
                                onBack = {
                                    editorViewModel.dismissExportState()
                                    currentScreen = AppScreen.HOME
                                },
                                onKeepEditing = {
                                    editorViewModel.dismissExportState()
                                    currentScreen = AppScreen.EDITOR
                                }
                            )
                        } else {
                            currentScreen = AppScreen.HOME
                        }
                    }

                    AppScreen.PRESETS_LIBRARY -> {
                        val readyState = uiState as? EditorUiState.Ready
                        PresetsLibraryScreen(
                            favoritePresetIds = readyState?.favoritePresetIds ?: emptySet(),
                            onToggleFavorite = { editorViewModel.toggleFavorite(it) },
                            onSelectPreset = { presetId ->
                                editorViewModel.openSampleImage(true)
                                editorViewModel.selectPreset(presetId)
                                currentScreen = AppScreen.EDITOR
                            },
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.MY_STYLES -> {
                        val readyState = uiState as? EditorUiState.Ready
                        MyStylesScreen(
                            favoritePresetIds = readyState?.favoritePresetIds ?: setOf("golden_hour", "cinemood", "vivid", "travel"),
                            onToggleFavorite = { editorViewModel.toggleFavorite(it) },
                            onSelectPreset = { presetId ->
                                editorViewModel.openSampleImage(true)
                                editorViewModel.selectPreset(presetId)
                                currentScreen = AppScreen.EDITOR
                            },
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                color = AccentViolet,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Notice",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentViolet,
                    contentColor = TextPrimary
                )
            ) {
                Text("Return to Home")
            }
        }
    }
}
