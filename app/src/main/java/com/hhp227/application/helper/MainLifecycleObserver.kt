package com.hhp227.application.helper

import androidx.lifecycle.LifecycleOwner

interface MainLifecycleObserver {
    fun registerLifecycleOwner(owner: LifecycleOwner)
}