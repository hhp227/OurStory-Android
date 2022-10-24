package com.hhp227.application.helper

import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.hhp227.application.app.AppController
import com.hhp227.application.app.NetworkConnectivityObserver
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainLifecycleObserverImpl : MainLifecycleObserver, DefaultLifecycleObserver {
    override fun registerLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        owner.lifecycle.addObserver(this)
        AppController.getInstance().networkConnectivityObserver.observe().onEach { status ->
            when (status) {
                NetworkConnectivityObserver.Status.Available -> {
                    Toast.makeText(AppController.getInstance(), "Available", Toast.LENGTH_LONG).show()
                }
                NetworkConnectivityObserver.Status.Losing -> {
                    Toast.makeText(AppController.getInstance(), "Losing", Toast.LENGTH_LONG).show()
                }
                NetworkConnectivityObserver.Status.Lost -> {
                    Toast.makeText(AppController.getInstance(), "Lost", Toast.LENGTH_LONG).show()
                }
                NetworkConnectivityObserver.Status.Unavailable -> {
                    Toast.makeText(AppController.getInstance(), "Unavailable", Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(owner.lifecycleScope)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        owner.lifecycle.removeObserver(this)
    }
}