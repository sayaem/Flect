package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.LavenderAccent
import kotlin.math.min

@Composable
fun SplitComparisonView(
    originalBitmap: Bitmap,
    renderedBitmap: Bitmap,
    isComparing: Boolean,
    splitPosition: Float,
    onSplitChange: (Float) -> Unit,
    showSplitMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val origImage = remember(originalBitmap) { originalBitmap.asImageBitmap() }
    val editedImage = remember(renderedBitmap) { renderedBitmap.asImageBitmap() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        if (isComparing) {
            // Full original view during hold-to-compare
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dstSize = calculateFitSize(size, Size(origImage.width.toFloat(), origImage.height.toFloat()))
                val dstOffset = Offset(
                    (size.width - dstSize.width) / 2f,
                    (size.height - dstSize.height) / 2f
                )
                drawImage(
                    image = origImage,
                    dstOffset = IntOffset(dstOffset.x.toInt(), dstOffset.y.toInt()),
                    dstSize = IntSize(dstSize.width.toInt(), dstSize.height.toInt())
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(CharcoalSurface.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ORIGINAL (HOLDING)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        letterSpacing = 1.2.sp
                    )
                )
            }
        } else if (showSplitMode) {
            // Split screen slider view
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val newPos = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                            onSplitChange(newPos)
                        }
                    }
            ) {
                val dstSize = calculateFitSize(size, Size(origImage.width.toFloat(), origImage.height.toFloat()))
                val dstOffset = Offset(
                    (size.width - dstSize.width) / 2f,
                    (size.height - dstSize.height) / 2f
                )

                val splitX = size.width * splitPosition

                // Draw edited on left
                clipRect(left = 0f, top = 0f, right = splitX, bottom = size.height) {
                    drawImage(
                        image = origImage,
                        dstOffset = IntOffset(dstOffset.x.toInt(), dstOffset.y.toInt()),
                        dstSize = IntSize(dstSize.width.toInt(), dstSize.height.toInt())
                    )
                }

                // Draw filtered on right
                clipRect(left = splitX, top = 0f, right = size.width, bottom = size.height) {
                    drawImage(
                        image = editedImage,
                        dstOffset = IntOffset(dstOffset.x.toInt(), dstOffset.y.toInt()),
                        dstSize = IntSize(dstSize.width.toInt(), dstSize.height.toInt())
                    )
                }

                // Draw vertical divider line
                drawLine(
                    color = Color.White,
                    start = Offset(splitX, 0f),
                    end = Offset(splitX, size.height),
                    strokeWidth = 2.5f
                )

                // Divider handle circle
                val centerY = size.height / 2f
                drawCircle(
                    color = CharcoalSurface,
                    radius = 18f,
                    center = Offset(splitX, centerY)
                )
                drawCircle(
                    color = LavenderAccent,
                    radius = 8f,
                    center = Offset(splitX, centerY)
                )
            }

            // Labels for split view
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Original",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Flect",
                    color = LavenderAccent,
                    fontSize = 11.sp
                )
            }
        } else {
            // Standard single view: filtered image
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dstSize = calculateFitSize(size, Size(editedImage.width.toFloat(), editedImage.height.toFloat()))
                val dstOffset = Offset(
                    (size.width - dstSize.width) / 2f,
                    (size.height - dstSize.height) / 2f
                )
                drawImage(
                    image = editedImage,
                    dstOffset = IntOffset(dstOffset.x.toInt(), dstOffset.y.toInt()),
                    dstSize = IntSize(dstSize.width.toInt(), dstSize.height.toInt())
                )
            }
        }
    }
}

private fun calculateFitSize(canvasSize: Size, imageSize: Size): Size {
    if (imageSize.width <= 0 || imageSize.height <= 0) return canvasSize
    val scale = min(
        canvasSize.width / imageSize.width,
        canvasSize.height / imageSize.height
    )
    return Size(imageSize.width * scale, imageSize.height * scale)
}
