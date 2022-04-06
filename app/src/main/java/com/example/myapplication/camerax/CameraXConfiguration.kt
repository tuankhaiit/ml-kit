package com.example.myapplication.camerax

import androidx.camera.view.PreviewView
import java.io.Serializable

class CameraXConfiguration private constructor(
    val cameraFacing: Int,
    val aspectRatio: Int,
    val previewScaleType: PreviewView.ScaleType,
    val enableFaceDetection: Boolean,
    val enableQRCodeDetection: Boolean,
    val enableTextReconization: Boolean,
) : Serializable {

    class Builder {
        private var cameraFacing = CameraXFragment.DEFAULT_CAMERA_FACING
        private var aspectRatio = CameraXFragment.DEFAULT_ASPECT_RATIO
        private var previewScaleType = CameraXFragment.DEFAULT_PREVIEW_SCALE_TYPE

        var enableFaceDetection = false
        var enableQRCodeDetection = false
        var enableTextReconization = false

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

        fun build(): CameraXConfiguration {
            return CameraXConfiguration(
                cameraFacing,
                aspectRatio,
                previewScaleType,
                enableFaceDetection,
                enableQRCodeDetection,
                enableTextReconization
            )
        }
    }
}