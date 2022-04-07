package com.example.myapplication.barcodeDetection

import android.graphics.Rect
import android.util.Log
import androidx.camera.view.PreviewView
import com.example.myapplication.camerax.BaseImageAnalyzer
import com.example.myapplication.camerax.GraphicOverlay
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeContourDetectionProcessor(
    private val graphicOverlay: GraphicOverlay,
    private val scaleType: PreviewView.ScaleType,
    private val aspectRatio: Int
) : BaseImageAnalyzer<List<Barcode>>() {
    private val realTimeOpts by lazy {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE
            )
            .build()
    }

    private val detector by lazy {
        BarcodeScanning.getClient(realTimeOpts)
    }

    override fun detectInImage(image: InputImage): Task<List<Barcode>> {
        return detector.process(image)
    }

    override fun onSuccess(
        results: List<Barcode>,
        rect: Rect
    ) {
        graphicOverlay.clear()
        results.forEach {
            val barcodeGraphic = BarcodeContourGraphic(
                graphicOverlay,
                scaleType,
                aspectRatio,
                it,
                rect
            )
            graphicOverlay.add(barcodeGraphic)
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