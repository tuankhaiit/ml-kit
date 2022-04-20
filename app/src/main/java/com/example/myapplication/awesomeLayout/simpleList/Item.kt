package com.example.myapplication.awesomeLayout.simpleList

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: Int,
    val title: String,
    val content: String
) : Parcelable {
    companion object {
        fun generateList(fromIndex: Int): List<Item> {
            return (fromIndex..(fromIndex + 100)).map {
                Item(
                    it,
                    "This is title: $it",
                    "This is content: $it",
                )
            }
        }
    }
}