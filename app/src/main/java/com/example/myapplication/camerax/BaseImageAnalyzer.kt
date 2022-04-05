package com.example.myapplication.camerax

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage

abstract class BaseImageAnalyzer<T> : ImageAnalysis.Analyzer {

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
                    onSuccess(
                        results,
                        mediaImage.cropRect
                    )
                }
                .addOnFailureListener {
                    onFailure(it)
                }.addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    protected abstract fun detectInImage(image: InputImage): Task<T>

    abstract fun stop()

    protected abstract fun onSuccess(
        results: T,
        rect: Rect
    )

    protected abstract fun onFailure(e: Exception)
}