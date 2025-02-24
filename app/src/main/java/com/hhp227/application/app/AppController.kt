package com.hhp227.application.app

import android.app.Application
import android.content.Context
import androidx.datastore.dataStore
import com.hhp227.application.helper.PhotoUriManager
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.helper.UserSerializer

class AppController : Application() {
    val preferenceManager: PreferenceManager by lazy { PreferenceManager.getInstance(this) }

    val photoUriManager: PhotoUriManager by lazy { PhotoUriManager(this) }

    val networkConnectivityObserver by lazy { NetworkConnectivityObserver(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    companion object {
        val Context.userDataStore by dataStore("user-preferences.json", UserSerializer)

        @Volatile
        private var instance: AppController? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: AppController().also {
                instance = it
            }
        }
    }
}