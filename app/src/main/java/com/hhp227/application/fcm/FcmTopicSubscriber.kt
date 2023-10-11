package com.hhp227.application.fcm

import com.google.firebase.messaging.FirebaseMessaging

class FcmTopicSubscriber {
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

    // TODO logout할때 사용될것
    fun unsubscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }
}