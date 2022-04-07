package com.example.myapplication.barcodeDetection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.camera.view.PreviewView
import com.example.myapplication.camerax.GraphicOverlay
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeContourGraphic(
    overlay: GraphicOverlay,
    scaleType: PreviewView.ScaleType,
    aspectRatio: Int,
    private val barcode: Barcode,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay, scaleType, aspectRatio) {

    private val boxPaint: Paint by lazy {
        Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
    }

    override fun draw(canvas: Canvas?) {
        barcode.boundingBox?.let { boundingBox ->
            val rect = calculateRect(
                imageRect.height().toFloat(),
                imageRect.width().toFloat(),
                boundingBox
            )
            canvas?.drawRect(rect, boxPaint)
        }
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

}