package com.example.myapplication.camerax

import android.annotation.SuppressLint
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage

abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer {
    abstract val graphicOverlay: GraphicOverlay

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let { mediaImage ->
            detectInImage(
                InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
            )
                .addOnSuccessListener { results ->
                    Log.e("khaitdt, ","frame width = ${graphicOverlay.width} - height = ${graphicOverlay.height}")
                    Log.e("khaitdt", "crop left = ${mediaImage.cropRect.left} - top = ${mediaImage.cropRect.top} - width = ${mediaImage.cropRect.width()} - height = ${mediaImage.cropRect.height()}")
                    onSuccess(
                        results,
                        graphicOverlay,
                        mediaImage.cropRect
                    )
                    imageProxy.close()
                }
                .addOnFailureListener {
                    onFailure(it)
                    imageProxy.close()
                }
        }
    }

    protected abstract fun detectInImage(image: InputImage): Task<T>

    abstract fun stop()

    protected abstract fun onSuccess(
        results: T,
        graphicOverlay: GraphicOverlay,
        rect: Rect
    )

    protected abstract fun onFailure(e: Exception)
}