package com.example.myapplication.faceDetection

import android.graphics.Rect
import android.util.Log
import com.example.myapplication.camerax.BaseImageAnalyzer
import com.example.myapplication.camerax.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class FaceContourDetectionProcessor(override val graphicOverlay: GraphicOverlay) :
    BaseImageAnalyzer<List<Face>>() {
    private val realTimeOpts by lazy {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build()
    }

    private val detector by lazy {
        FaceDetection.getClient(realTimeOpts)
    }

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun onSuccess(
        results: List<Face>,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    ) {
        graphicOverlay.clear()
        results.forEach {
            val faceGraphic = FaceContourGraphic(graphicOverlay, it, rect)
            graphicOverlay.add(faceGraphic)
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }
}