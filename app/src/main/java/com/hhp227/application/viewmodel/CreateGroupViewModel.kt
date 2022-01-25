package com.hhp227.application.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.BitmapUtil
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

class CreateGroupViewModel : ViewModel() {
    val state = MutableStateFlow(State())

    val repository = GroupRepository()

    lateinit var uri: Uri

    lateinit var currentPhotoPath: String

    val apiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    var bitMap: Bitmap? = null

    var joinType = false

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "CreateGroupViewModel onCleared")
    }

    fun setBitmap(bitmapUtil: BitmapUtil) {
        bitMap = try {
            bitmapUtil.bitmapResize(uri, 200)?.let {
                val ei = currentPhotoPath.let(::ExifInterface)
                val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                return@let bitmapUtil.rotateImage(it, when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                    else -> 0F
                })
            }
        } catch (e: IOException) {
            null
        }
    }

    fun createGroup(title: String, description: String, joinType: String) {
        if (title.isNotEmpty() && description.isNotEmpty()) {
            if (bitMap != null) {
                repository.addGroupImage(title, description, joinType)
            } else {
                repository.addGroup(title, null, description, joinType)
                //addGroup(title, null, description, joinType)
            }
        } else {
            state.value = state.value.copy(
                error = ""
            )
        }
    }

    fun addGroup(title: String, image: String?, description: String, joinType: String) {
        repository.addGroup(title, null, description, joinType)
    }

    data class State(
        val isLoading: Boolean = false,
        val error: String = ""
    )
}