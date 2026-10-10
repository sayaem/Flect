package com.example.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.R
import com.example.engine.FilterRenderer
import com.example.engine.PresetLibrary
import com.example.model.EditParameters
import com.example.model.FilterPreset
import com.example.model.ImageAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

data class LoadedImage(
    val uri: Uri?,
    val previewBitmap: Bitmap,
    val originalWidth: Int,
    val originalHeight: Int,
    val isSample: Boolean = false,
    val sampleResId: Int? = null
)

class ImageRepository(private val context: Context) {

    /**
     * Loads and normalizes an image from device URI with EXIF orientation correction
     * and memory-safe preview downsampling.
     */
    suspend fun loadImageFromUri(uri: Uri, maxDimension: Int = 1920): LoadedImage = withContext(Dispatchers.IO) {
        // 1. Measure dimensions without full decode
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        val origW = boundsOptions.outWidth
        val origH = boundsOptions.outHeight

        // 2. Determine sample size
        var sampleSize = 1
        val maxDim = max(origW, origH)
        while ((maxDim / sampleSize) > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val rawBitmap = context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: throw IllegalArgumentException("Could not decode image from selected URI")

        // 3. Handle EXIF rotation
        val rotationDegrees = getExifRotation(uri)
        val rotatedBitmap = if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            val rotated = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
            if (rotated != rawBitmap) rawBitmap.recycle()
            rotated
        } else {
            rawBitmap
        }

        LoadedImage(
            uri = uri,
            previewBitmap = rotatedBitmap,
            originalWidth = origW,
            originalHeight = origH,
            isSample = false
        )
    }

    /**
     * Loads a built-in photography sample asset for instant testing.
     */
    suspend fun loadSampleImage(resourceId: Int): LoadedImage = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
            ?: throw IllegalArgumentException("Failed to decode sample resource")

        LoadedImage(
            uri = null,
            previewBitmap = bitmap,
            originalWidth = bitmap.width,
            originalHeight = bitmap.height,
            isSample = true,
            sampleResId = resourceId
        )
    }

    /**
     * Generates true thumbnails for all presets rendered from the current image.
     */
    suspend fun generateThumbnails(
        previewBitmap: Bitmap,
        analysis: ImageAnalysisResult
    ): Map<String, Bitmap> = withContext(Dispatchers.Default) {
        // Crop or scale to 110x110 square for fast thumbnail generation
        val thumbSize = 110
        val srcSize = kotlin.math.min(previewBitmap.width, previewBitmap.height)
        val cropX = (previewBitmap.width - srcSize) / 2
        val cropY = (previewBitmap.height - srcSize) / 2

        val square = Bitmap.createBitmap(previewBitmap, cropX, cropY, srcSize, srcSize)
        val baseThumb = Bitmap.createScaledBitmap(square, thumbSize, thumbSize, true)
        if (square != previewBitmap && square != baseThumb) {
            square.recycle()
        }

        val result = mutableMapOf<String, Bitmap>()
        for (preset in PresetLibrary.allPresets) {
            val params = EditParameters(presetId = preset.id, presetIntensity = 1.0f)
            val filtered = FilterRenderer.render(baseThumb, params, analysis, preset)
            result[preset.id] = filtered
        }

        baseThumb.recycle()
        result
    }

    /**
     * Exports the edited image at full resolution and saves to device gallery MediaStore.
     */
    suspend fun exportToGallery(
        loadedImage: LoadedImage,
        parameters: EditParameters,
        analysis: ImageAnalysisResult,
        quality: Int = 92
    ): Uri = withContext(Dispatchers.IO) {
        // 1. Decode at high resolution
        val fullSource: Bitmap = if (loadedImage.isSample && loadedImage.sampleResId != null) {
            BitmapFactory.decodeResource(context.resources, loadedImage.sampleResId)
        } else if (loadedImage.uri != null) {
            val rotation = getExifRotation(loadedImage.uri)
            val stream = context.contentResolver.openInputStream(loadedImage.uri)
            val raw = BitmapFactory.decodeStream(stream) ?: loadedImage.previewBitmap
            if (rotation != 0 && raw != loadedImage.previewBitmap) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rot = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                if (rot != raw) raw.recycle()
                rot
            } else raw
        } else {
            loadedImage.previewBitmap
        }

        // 2. Render through same transformation engine
        val preset = PresetLibrary.getById(parameters.presetId)
        val exportedBitmap = FilterRenderer.render(fullSource, parameters, analysis, preset)

        // 3. Save to MediaStore (Pictures/Flect)
        val filename = "Flect_${System.currentTimeMillis()}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Flect")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val insertedUri = context.contentResolver.insert(contentUri, values)
            ?: throw IllegalStateException("Failed to create MediaStore entry")

        context.contentResolver.openOutputStream(insertedUri)?.use { outStream ->
            exportedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
        } ?: throw IllegalStateException("Could not open output stream for saved image")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(insertedUri, values, null, null)
        }

        if (fullSource != loadedImage.previewBitmap) {
            fullSource.recycle()
        }
        exportedBitmap.recycle()

        insertedUri
    }

    /**
     * Creates a shareable temporary file and returns its FileProvider content URI.
     */
    suspend fun createShareableUri(
        previewBitmap: Bitmap,
        parameters: EditParameters,
        analysis: ImageAnalysisResult
    ): Uri = withContext(Dispatchers.IO) {
        val preset = PresetLibrary.getById(parameters.presetId)
        val rendered = FilterRenderer.render(previewBitmap, parameters, analysis, preset)

        val shareDir = File(context.cacheDir, "images").apply { mkdirs() }
        val shareFile = File(shareDir, "flect_share_${System.currentTimeMillis()}.jpg")

        FileOutputStream(shareFile).use { fos ->
            rendered.compress(Bitmap.CompressFormat.JPEG, 94, fos)
        }
        rendered.recycle()

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile
        )
    }

    private fun getExifRotation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }
}
