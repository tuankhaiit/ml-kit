package com.example.myapplication.camerax

import androidx.camera.core.AspectRatio
import androidx.camera.view.PreviewView
import java.io.Serializable

class CameraXConfiguration private constructor(
    val aspectRatio: Int,
    val previewScaleType: PreviewView.ScaleType
) : Serializable {

    class Builder {
        private var aspectRatio = AspectRatio.RATIO_16_9
        private var previewScaleType = PreviewView.ScaleType.FIT_END

        fun setAspectRatio(aspectRatio: Int): CameraXConfiguration.Builder {
            this.aspectRatio = aspectRatio
            return this
        }

        fun setPreviewScaleType(previewScaleType: PreviewView.ScaleType): CameraXConfiguration.Builder {
            this.previewScaleType = previewScaleType
            return this
        }

        fun build(): CameraXConfiguration {
            return CameraXConfiguration(aspectRatio, previewScaleType)
        }
    }
}