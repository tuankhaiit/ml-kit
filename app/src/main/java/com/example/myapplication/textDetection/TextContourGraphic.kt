package com.example.myapplication.textDetection

import android.graphics.*
import androidx.camera.view.PreviewView
import com.example.myapplication.camerax.GraphicOverlay
import com.google.mlkit.vision.text.Text

class TextContourGraphic(
    overlay: GraphicOverlay,
    scaleType: PreviewView.ScaleType,
    aspectRatio: Int,
    private val text: Text,
    private val imageRect: Rect
) : GraphicOverlay.Graphic(overlay, scaleType, aspectRatio) {

    private val blockPaint: Paint by lazy {
        Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
    }

    private val linePaint: Paint by lazy {
        Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
    }

    private val elementPaint: Paint by lazy {
        Paint().apply {
            color = Color.BLUE
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
        }
    }

    override fun draw(canvas: Canvas?) {
        for (block in text.textBlocks) {
            block.boundingBox?.let {
                val rect = calculateRect(
                    imageRect.height().toFloat(),
                    imageRect.width().toFloat(),
                    it
                )
                drawRectangle(canvas, blockPaint, rect, 20)
            }
//            for (line in block.lines) {
//                line.boundingBox?.let {
//                    val rect = calculateRect(
//                        imageRect.height().toFloat(),
//                        imageRect.width().toFloat(),
//                        it
//                    )
//                    drawRectangle(canvas, linePaint, rect, 10)
//                }
//                for (element in line.elements) {
//                    element.boundingBox?.let {
//                        val rect = calculateRect(
//                            imageRect.height().toFloat(),
//                            imageRect.width().toFloat(),
//                            it
//                        )
//                        drawRectangle(canvas, elementPaint, rect, 5)
//                    }
//                }
//            }
        }
    }

    private fun drawRectangle(canvas: Canvas?, paint: Paint, rect: RectF, padding: Int = 0) {
        val left = rect.left - padding
        val right = rect.right + padding
        val top = rect.top - padding
        val bottom = rect.bottom + padding
        val newRect = RectF(left, top, right, bottom)
        canvas?.drawRect(newRect, paint)
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 3.0f
    }

}