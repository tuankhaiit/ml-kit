package com.example.myapplication.textDetection

import android.graphics.Rect
import android.util.Log
import androidx.camera.view.PreviewView
import com.example.myapplication.camerax.BaseImageAnalyzer
import com.example.myapplication.camerax.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextContourDetectionProcessor(
    private val graphicOverlay: GraphicOverlay,
    private val scaleType: PreviewView.ScaleType,
    private val aspectRatio: Int
) : BaseImageAnalyzer<Text>() {
    private val detector by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override fun detectInImage(image: InputImage): Task<Text> {
        return detector.process(image)
    }

    override fun onSuccess(
        results: Text,
        rect: Rect
    ) {
        graphicOverlay.clear()
        val textGraphic = TextContourGraphic(graphicOverlay, scaleType, aspectRatio, results, rect)
        graphicOverlay.add(textGraphic)
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}