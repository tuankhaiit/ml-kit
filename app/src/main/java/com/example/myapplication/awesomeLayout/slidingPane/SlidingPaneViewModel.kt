package com.example.myapplication.awesomeLayout.slidingPane

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapplication.awesomeLayout.simpleList.Item
import kotlinx.coroutines.flow.MutableStateFlow

class SlidingPaneViewModel : ViewModel() {
    val openItemEvent = MutableStateFlow<Item?>(null)

    override fun onCleared() {
        super.onCleared()
        Log.e("khaitdt", "onCleared")
    }
}