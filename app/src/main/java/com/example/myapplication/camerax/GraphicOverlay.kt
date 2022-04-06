package com.example.myapplication.camerax

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import kotlin.math.max

open class GraphicOverlay(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()
    var cameraFacing: Int = CameraXFragment.DEFAULT_CAMERA_FACING

    fun isFrontMode() = cameraFacing == CameraSelector.LENS_FACING_FRONT

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        synchronized(lock) {
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }

    abstract class Graphic(
        private val overlay: GraphicOverlay,
        private val scaleType: PreviewView.ScaleType,
        private val aspectRatio: Int
    ) {

        abstract fun draw(canvas: Canvas?)

        fun calculateRect(imageHeight: Float, imageWidth: Float, boundingBoxT: Rect): RectF {
            val screenWidth = overlay.width.toFloat()
            val screenHeight = overlay.height.toFloat()

            // for land scape
            fun isLandScapeMode(): Boolean {
                return overlay.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }

            // Default the image was rotate 90 degree.
            // We will swap values of width and height if PORTRAIT mode
            fun getImageWidth(): Float {
                return when (isLandScapeMode()) {
                    true -> imageWidth
                    false -> imageHeight
                }
            }

            // Default the image was rotate 90 degree.
            // We will swap values of width and height if PORTRAIT mode
            fun getImageHeight(): Float {
                return when (isLandScapeMode()) {
                    true -> imageHeight
                    false -> imageWidth
                }
            }

            fun getRatio(): Float = when (aspectRatio) {
                AspectRatio.RATIO_16_9 -> 16 / 9f
                else -> 4 / 3f
            }

            fun getPreviewWidth(): Float {
                return when (isLandScapeMode()) {
                    true -> when (scaleType) {
                        PreviewView.ScaleType.FILL_START,
                        PreviewView.ScaleType.FILL_CENTER,
                        PreviewView.ScaleType.FILL_END -> screenWidth
                        else -> screenHeight * getRatio()
                    }
                    false -> when (scaleType) {
                        PreviewView.ScaleType.FILL_START,
                        PreviewView.ScaleType.FILL_CENTER,
                        PreviewView.ScaleType.FILL_END -> screenHeight / getRatio()
                        else -> screenWidth
                    }
                }
            }

            fun getPreviewHeight(): Float {
                return when (isLandScapeMode()) {
                    true -> when (scaleType) {
                        PreviewView.ScaleType.FILL_START,
                        PreviewView.ScaleType.FILL_CENTER,
                        PreviewView.ScaleType.FILL_END -> screenWidth / getRatio()
                        else -> screenHeight
                    }
                    false -> when (scaleType) {
                        PreviewView.ScaleType.FILL_START,
                        PreviewView.ScaleType.FILL_CENTER,
                        PreviewView.ScaleType.FILL_END -> screenHeight
                        else -> screenWidth * getRatio()
                    }
                }
            }

            fun getPreviewOffsetX(): Float = when (isLandScapeMode()) {
                true -> when (scaleType) {
                    PreviewView.ScaleType.FIT_START -> 0f
                    PreviewView.ScaleType.FIT_END -> screenWidth - getPreviewWidth()
                    else -> (screenWidth - getPreviewWidth()) / 2
                }
                false -> when (scaleType) {
                    PreviewView.ScaleType.FILL_START -> 0f
                    PreviewView.ScaleType.FILL_END -> screenWidth - getPreviewWidth()
                    else -> (screenWidth - getPreviewWidth()) / 2
                }
            }

            fun getPreviewOffsetY(): Float = when (isLandScapeMode()) {
                true -> when (scaleType) {
                    PreviewView.ScaleType.FILL_START,
                    PreviewView.ScaleType.FIT_START,
                    PreviewView.ScaleType.FIT_CENTER,
                    PreviewView.ScaleType.FIT_END -> 0f
                    PreviewView.ScaleType.FILL_END -> screenHeight - getPreviewHeight()
                    else -> (screenHeight - getPreviewHeight()) / 2
                }
                false -> when (scaleType) {
                    PreviewView.ScaleType.FIT_START -> 0f
                    PreviewView.ScaleType.FIT_END -> screenHeight - getPreviewHeight()
                    else -> (screenHeight - getPreviewHeight()) / 2
                }
            }

            val previewWidth = getPreviewWidth()
            val previewHeight = getPreviewHeight()

            val scaleX = previewWidth / getImageWidth()
            val scaleY = previewHeight / getImageHeight()
            val scale = max(scaleX, scaleY)

            // Calculate offset (we need to center the overlay on the target)
            val offsetX = (previewWidth - getImageWidth() * scale) / 2.0f + getPreviewOffsetX()
            val offsetY = (previewHeight - getImageHeight() * scale) / 2.0f + getPreviewOffsetY()

            val mappedBox = RectF().apply {
                left = boundingBoxT.right * scale + offsetX
                top = boundingBoxT.top * scale + offsetY
                right = boundingBoxT.left * scale + offsetX
                bottom = boundingBoxT.bottom * scale + offsetY
            }

            // for front mode
            if (overlay.isFrontMode()) {
                val centerX = screenWidth / 2f
                val offset = when (scaleType) {
                    PreviewView.ScaleType.FILL_START -> screenWidth - previewWidth
                    PreviewView.ScaleType.FILL_END -> previewWidth - screenWidth
                    else -> 0f
                }
                mappedBox.apply {
                    left = centerX + (centerX - left) - offset
                    right = centerX - (right - centerX) - offset
                }
            }
            return mappedBox
        }
    }

}