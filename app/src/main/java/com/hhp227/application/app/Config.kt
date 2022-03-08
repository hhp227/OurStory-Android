package com.hhp227.application.app

object Config {
    // flag to identify whether to show single line
    // or multi line test push notification tray
    @JvmField
    var appendNotificationMessages = true

    // global topic to receive app wide push notifications
    const val TOPIC_GLOBAL = "global"

    // broadcast receiver intent filters
    const val SENT_TOKEN_TO_SERVER = "sentTokenToServer"
    const val REGISTRATION_COMPLETE = "registrationComplete"
    const val PUSH_NOTIFICATION = "pushNotification"

    // type of push messages
    const val PUSH_TYPE_CHATROOM = 1
    const val PUSH_TYPE_USER = 2

    // id to handle the notification in the notification try
    const val NOTIFICATION_ID = 100
    const val NOTIFICATION_ID_BIG_IMAGE = 101
    const val SHARED_PREF = "ah_firebase"
}