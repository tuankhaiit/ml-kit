package com.example.myapplication.camerax

import androidx.camera.view.PreviewView
import java.io.Serializable

class CameraXConfiguration private constructor(
    val cameraFacing: Int,
    val aspectRatio: Int,
    val previewScaleType: PreviewView.ScaleType,
    val enableFaceDetection: Boolean,
    val enableBarcodeDetection: Boolean,
    val enableTextRecognition: Boolean,
) : Serializable {

    class Builder {
        private var cameraFacing = CameraXFragment.DEFAULT_CAMERA_FACING
        private var aspectRatio = CameraXFragment.DEFAULT_ASPECT_RATIO
        private var previewScaleType = CameraXFragment.DEFAULT_PREVIEW_SCALE_TYPE

        private var enableFaceDetection = false
        private var enableBarcodeDetection = false
        private var enableTextRecognition = false

        fun setAspectRatio(aspectRatio: Int): Builder {
            this.aspectRatio = aspectRatio
            return this
        }

        fun setCameraFacing(cameraFacing: Int): Builder {
            this.cameraFacing = cameraFacing
            return this
        }

        fun setPreviewScaleType(previewScaleType: PreviewView.ScaleType): Builder {
            this.previewScaleType = previewScaleType
            return this
        }

        fun enableFaceDetection(): Builder {
            enableFaceDetection = true
            enableTextRecognition = false
            enableBarcodeDetection = false
            return this
        }

        fun enableBarcodeDetection(): Builder {
            enableFaceDetection = false
            enableTextRecognition = false
            enableBarcodeDetection = true
            return this
        }

        fun enableTextDetection(): Builder {
            enableFaceDetection = false
            enableTextRecognition = true
            enableBarcodeDetection = false
            return this
        }

        fun build(): CameraXConfiguration {
            return CameraXConfiguration(
                cameraFacing,
                aspectRatio,
                previewScaleType,
                enableFaceDetection,
                enableBarcodeDetection,
                enableTextRecognition
            )
        }
    }
}