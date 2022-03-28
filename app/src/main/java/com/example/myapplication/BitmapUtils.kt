package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object BitmapUtils {
    suspend fun loadBitmap(filePath: String): Bitmap? {
        return loadBitmap(File(filePath))
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadBitmap(file: File): Bitmap? {
        return if (file.exists()) {
            return withContext(Dispatchers.IO) {
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)

                    val exif = ExifInterface(file.path)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    val matrix = Matrix()

                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
                        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
                        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
                    }

                    val rotatedBitmap =
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    bitmap.recycle()
                    rotatedBitmap
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } else null
    }
}