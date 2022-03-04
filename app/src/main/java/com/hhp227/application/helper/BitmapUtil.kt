package com.hhp227.application.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.FileNotFoundException
import java.io.IOException

class BitmapUtil(private val context: Context) {
    fun bitmapResize(uri: Uri?, resize: Int): Bitmap? {
        var result: Bitmap? = null
        val options = BitmapFactory.Options()

        try {
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri!!), null, options)
            var width = options.outWidth
            var height = options.outHeight
            var sampleSize = 1

            while (width / 2 >= resize && height / 2 >= resize) {
                width /= 2
                height /= 2
                sampleSize *= 2
            }
            options.inSampleSize = sampleSize
            result = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return result
    }

    fun uriToBitmap(uri: Uri?): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri != null) ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    context.contentResolver,
                    uri
                )
            ) else MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            null
        }
    }

    fun rotateImage(bitmap: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()

        matrix.postRotate(angle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}