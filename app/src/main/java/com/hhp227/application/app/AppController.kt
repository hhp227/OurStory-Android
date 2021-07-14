package com.hhp227.application.app

import android.app.Application
import android.content.res.Resources
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.hhp227.application.helper.PreferenceManager

class AppController : Application() {
    val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(applicationContext) }

    val preferenceManager: PreferenceManager by lazy { PreferenceManager(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun <T> addToRequestQueue(req: Request<T>, tag: String?) {

        // 태그가 비어 있으면 기본 태그 세트
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue.add(req)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        requestQueue.add(req)
    }

    fun cancelPendingRequests(tag: Any?) {
        requestQueue.cancelAll(tag)
    }

    fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    companion object {
        private val TAG = AppController::class.simpleName

        @Volatile
        private var instance: AppController? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: AppController().also {
                instance = it
            }
        }
    }
}