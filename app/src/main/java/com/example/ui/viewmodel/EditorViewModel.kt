package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.PreferencesManager
import com.example.engine.AdaptiveEngine
import com.example.engine.FilterRenderer
import com.example.engine.ImageAnalyzer
import com.example.engine.PresetLibrary
import com.example.model.AdaptiveAdjustments
import com.example.model.EditParameters
import com.example.model.FilterPreset
import com.example.model.ImageAnalysisResult
import com.example.repository.ImageRepository
import com.example.repository.LoadedImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EditorUiState {
    object Empty : EditorUiState
    data class Loading(val message: String) : EditorUiState
    data class Ready(
        val loadedImage: LoadedImage,
        val renderedBitmap: Bitmap,
        val analysis: ImageAnalysisResult,
        val parameters: EditParameters,
        val adaptiveAdjustments: AdaptiveAdjustments,
        val thumbnails: Map<String, Bitmap>,
        val isRendering: Boolean = false,
        val isComparing: Boolean = false,
        val splitPosition: Float = 0.5f, // 0.0 to 1.0 for split preview slider
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val favoritePresetIds: Set<String> = emptySet(),
        val recentPresetIds: List<String> = emptyList(),
        val exportState: ExportState = ExportState.Idle
    ) : EditorUiState
    data class Error(val message: String) : EditorUiState
}

sealed interface ExportState {
    object Idle : ExportState
    object Exporting : ExportState
    data class Success(val savedUri: Uri, val shareUri: Uri) : ExportState
    data class Failure(val error: String) : ExportState
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ImageRepository(application)
    private val preferencesManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Empty)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Undo / Redo history
    private val undoStack = mutableListOf<EditParameters>()
    private val redoStack = mutableListOf<EditParameters>()
    private val maxHistory = 30

    private var renderJob: Job? = null
    private var lastRenderedBitmap: Bitmap? = null

    init {
        // Observe favorites and recents
        viewModelScope.launch {
            preferencesManager.favoritePresetsFlow.collectLatest { favorites ->
                val current = _uiState.value
                if (current is EditorUiState.Ready) {
                    _uiState.update { current.copy(favoritePresetIds = favorites) }
                }
            }
        }
        viewModelScope.launch {
            preferencesManager.recentPresetsFlow.collectLatest { recents ->
                val current = _uiState.value
                if (current is EditorUiState.Ready) {
                    _uiState.update { current.copy(recentPresetIds = recents) }
                }
            }
        }
    }

    fun openImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading("Loading image...")
            try {
                val loaded = repository.loadImageFromUri(uri)
                processNewImage(loaded)
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Failed to load selected photo")
            }
        }
    }

    fun openSampleImage(isPortrait: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading("Loading sample photo...")
            try {
                val resId = if (isPortrait) R.drawable.sample_portrait else R.drawable.sample_travel
                val loaded = repository.loadSampleImage(resId)
                processNewImage(loaded)
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(e.message ?: "Failed to load sample photo")
            }
        }
    }

    private suspend fun processNewImage(loaded: LoadedImage) {
        _uiState.value = EditorUiState.Loading("Analyzing image lighting & tone...")
        val analysis = ImageAnalyzer.analyze(loaded.previewBitmap)

        _uiState.value = EditorUiState.Loading("Generating style previews...")
        val thumbnails = repository.generateThumbnails(loaded.previewBitmap, analysis)

        undoStack.clear()
        redoStack.clear()

        // Default initial parameters
        val initialParams = EditParameters(presetId = null, presetIntensity = 1.0f)
        val initialRender = loaded.previewBitmap

        _uiState.value = EditorUiState.Ready(
            loadedImage = loaded,
            renderedBitmap = initialRender,
            analysis = analysis,
            parameters = initialParams,
            adaptiveAdjustments = AdaptiveAdjustments.ZERO,
            thumbnails = thumbnails,
            canUndo = false,
            canRedo = false
        )
    }

    fun selectPreset(presetId: String?) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        if (current.parameters.presetId == presetId) return

        pushHistory(current.parameters)
        val preset = PresetLibrary.getById(presetId)
        val newParams = current.parameters.copy(
            presetId = presetId,
            presetIntensity = preset?.defaultIntensity ?: 1.0f
        )
        updateParametersInternal(newParams)

        if (presetId != null) {
            viewModelScope.launch {
                preferencesManager.addRecentPreset(presetId)
            }
        }
    }

    fun setPresetIntensity(intensity: Float) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        val newParams = current.parameters.copy(presetIntensity = intensity)
        updateParametersInternal(newParams, saveHistory = false)
    }

    fun commitPresetIntensity(intensity: Float) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        pushHistory(current.parameters)
        val newParams = current.parameters.copy(presetIntensity = intensity)
        updateParametersInternal(newParams, saveHistory = false)
    }

    fun toggleAdaptive(enabled: Boolean) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        pushHistory(current.parameters)
        val newParams = current.parameters.copy(adaptiveEnabled = enabled)
        updateParametersInternal(newParams)
    }

    fun updateManualAdjustment(
        exposure: Float? = null,
        contrast: Float? = null,
        saturation: Float? = null,
        warmth: Float? = null,
        highlights: Float? = null,
        shadows: Float? = null,
        vignette: Float? = null,
        grain: Float? = null
    ) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        val p = current.parameters
        val newParams = p.copy(
            manualExposure = exposure ?: p.manualExposure,
            manualContrast = contrast ?: p.manualContrast,
            manualSaturation = saturation ?: p.manualSaturation,
            manualWarmth = warmth ?: p.manualWarmth,
            manualHighlights = highlights ?: p.manualHighlights,
            manualShadows = shadows ?: p.manualShadows,
            manualVignette = vignette ?: p.manualVignette,
            manualGrain = grain ?: p.manualGrain
        )
        updateParametersInternal(newParams, saveHistory = false)
    }

    fun commitManualAdjustment() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        pushHistory(current.parameters)
    }

    fun resetManualAdjustments() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        if (!current.parameters.hasManualAdjustments) return
        pushHistory(current.parameters)
        updateParametersInternal(current.parameters.resetManual())
    }

    fun resetAll() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        if (current.parameters.isOriginal) return
        pushHistory(current.parameters)
        updateParametersInternal(EditParameters.ORIGINAL)
    }

    fun undo() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        if (undoStack.isEmpty()) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(current.parameters)
        updateParametersInternal(previous, isUndoRedo = true)
    }

    fun redo() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(current.parameters)
        updateParametersInternal(next, isUndoRedo = true)
    }

    fun setComparing(isComparing: Boolean) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        _uiState.update { current.copy(isComparing = isComparing) }
    }

    fun setSplitPosition(pos: Float) {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        _uiState.update { current.copy(splitPosition = pos.coerceIn(0.0f, 1.0f)) }
    }

    fun toggleFavorite(presetId: String) {
        viewModelScope.launch {
            preferencesManager.toggleFavorite(presetId)
        }
    }

    private fun pushHistory(params: EditParameters) {
        undoStack.add(params)
        if (undoStack.size > maxHistory) undoStack.removeAt(0)
        redoStack.clear()
    }

    private fun updateParametersInternal(
        newParams: EditParameters,
        saveHistory: Boolean = false,
        isUndoRedo: Boolean = false
    ) {
        val current = _uiState.value as? EditorUiState.Ready ?: return

        val preset = PresetLibrary.getById(newParams.presetId)
        val adaptive = if (newParams.adaptiveEnabled && preset != null) {
            AdaptiveEngine.computeAdjustments(current.analysis, preset)
        } else {
            AdaptiveAdjustments.ZERO
        }

        _uiState.update {
            current.copy(
                parameters = newParams,
                adaptiveAdjustments = adaptive,
                isRendering = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }

        // Cancel previous pending render job and schedule debounced render
        renderJob?.cancel()
        renderJob = viewModelScope.launch {
            // Small delay for buttery smooth slider drag responsiveness
            delay(16)
            val rendered = if (newParams.isOriginal) {
                current.loadedImage.previewBitmap
            } else {
                FilterRenderer.render(
                    sourceBitmap = current.loadedImage.previewBitmap,
                    parameters = newParams,
                    analysis = current.analysis,
                    preset = preset
                )
            }

            // Recycle previously created intermediate preview bitmap if needed
            if (lastRenderedBitmap != null && lastRenderedBitmap != current.loadedImage.previewBitmap) {
                lastRenderedBitmap?.recycle()
            }
            lastRenderedBitmap = rendered

            _uiState.update { state ->
                if (state is EditorUiState.Ready) {
                    state.copy(
                        renderedBitmap = rendered,
                        isRendering = false
                    )
                } else state
            }
        }
    }

    fun exportImage() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        viewModelScope.launch {
            _uiState.update { current.copy(exportState = ExportState.Exporting) }
            try {
                val savedUri = repository.exportToGallery(
                    loadedImage = current.loadedImage,
                    parameters = current.parameters,
                    analysis = current.analysis
                )
                val shareUri = repository.createShareableUri(
                    previewBitmap = current.loadedImage.previewBitmap,
                    parameters = current.parameters,
                    analysis = current.analysis
                )
                _uiState.update {
                    current.copy(
                        exportState = ExportState.Success(savedUri = savedUri, shareUri = shareUri)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    current.copy(
                        exportState = ExportState.Failure(e.message ?: "Failed to export image")
                    )
                }
            }
        }
    }

    fun dismissExportState() {
        val current = _uiState.value as? EditorUiState.Ready ?: return
        _uiState.update { current.copy(exportState = ExportState.Idle) }
    }

    fun closeEditor() {
        if (lastRenderedBitmap != null) {
            val current = _uiState.value as? EditorUiState.Ready
            if (lastRenderedBitmap != current?.loadedImage?.previewBitmap) {
                lastRenderedBitmap?.recycle()
            }
            lastRenderedBitmap = null
        }
        _uiState.value = EditorUiState.Empty
    }

    override fun onCleared() {
        super.onCleared()
        lastRenderedBitmap?.recycle()
    }
}
