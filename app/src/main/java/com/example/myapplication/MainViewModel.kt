package com.example.myapplication

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

class MainViewModel : ViewModel() {
    val takePictureFlow = MutableSharedFlow<Bitmap>()
}