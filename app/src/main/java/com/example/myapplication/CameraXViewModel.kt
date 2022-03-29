package com.example.myapplication

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

class CameraXViewModel : ViewModel() {
    val focusEvent = MutableSharedFlow<Pair<Float, Float>>()
}