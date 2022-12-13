package com.hhp227.application.fcm

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hhp227.application.activity.MainActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.Config
import com.hhp227.application.model.MessageItem
import com.hhp227.application.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MyFcmPushReceiver : FirebaseMessagingService() {
    private var notificationUtils: NotificationUtils? = null

    /**
     * Called when message is received.
     *
     * @param message
     */
    override fun onMessageReceived(message: RemoteMessage) {
        fun received(user: User?) {
            val from = message.from
            val bundle: Map<*, *> = message.data
            val title = bundle["title"].toString()
            val isBackground = java.lang.Boolean.valueOf(bundle["is_background"].toString())
            val flag = bundle["flag"].toString()
            val data = bundle["data"].toString()

            Log.d(TAG, "From: $from")
            Log.d(TAG, "title: $title")
            Log.d(TAG, "isBackground: $isBackground")
            Log.d(TAG, "flag: $flag")
            Log.d(TAG, "data: $data")
            if (user == null) {
                // user is not logged in, skipping push notification
                Log.e(TAG, "user is not logged in, skipping push notification")
                return
            }
            if (from!!.startsWith("/topics/")) {
                // message received from some topic.
            } else {
                // normal downstream message.
            }
            when (flag.toInt()) {
                Config.PUSH_TYPE_CHATROOM ->                 // push notification belongs to a chat room
                    processChatRoomPush(title, isBackground, data)
                Config.PUSH_TYPE_USER ->                 // push notification is specific to user
                    processUserMessage(title, isBackground, data)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            AppController.getInstance().preferenceManager.userFlow.collectLatest(::received)
        }
    }

    /**
     * Processing chat room push message
     * this message will be broadcasts to all the activities registered
     */
    private fun processChatRoomPush(title: String, isBackground: Boolean, data: String) {
        fun push(user: User?) {
            if (!isBackground) {
                try {
                    val datObj = JSONObject(data)
                    val chatRoomId = datObj.getInt("chat_room_id")
                    val mObj = datObj.getJSONObject("message")
                    val uObj = datObj.getJSONObject("user")

                    // skip the message if the message belongs to same user as
                    // the user would be having the same message when he was sending
                    // but it might differs in your scenario
                    if (uObj.getInt("user_id") == user?.id) {
                        Log.e(TAG, "Skipping the push message as it belongs to same user")
                        return
                    }
                    val user = User(
                        uObj.getInt("user_id"),
                        uObj.getString("name"),
                        uObj.getString("email"),
                        null,
                        uObj.getString("profile_img"),
                        null
                    )
                    val message = MessageItem(
                        mObj.getInt("message_id"),
                        mObj.getString("message"),
                        mObj.getString("created_at"),
                        user
                    )

                    // verifying whether the app is in background or foreground
                    if (!NotificationUtils.isAppIsInBackground(applicationContext)) {

                        // 앱이 포어그라운드에 있을때, 브로드캐스트로 푸시메시지를 전송
                        val pushNotification = Intent(Config.PUSH_NOTIFICATION)
                        pushNotification.putExtra("type", Config.PUSH_TYPE_CHATROOM)
                        pushNotification.putExtra("message", message)
                        pushNotification.putExtra("chat_room_id", chatRoomId)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

                        // 알림 사운드 재생
                        val notificationUtils = NotificationUtils()
                        notificationUtils.playNotificationSound()
                    } else {

                        // 앱이 백그라운드에 있을때, 알림메시지가 보인다.
                        //val resultIntent = Intent(applicationContext, ChatMessageActivity::class.java)
                        val resultIntent = Intent(applicationContext, MainActivity::class.java)
                        resultIntent.putExtra("chat_room_id", chatRoomId)
                        showNotificationMessage(
                            applicationContext,
                            title,
                            user.name + " : " + message.message,
                            message.time,
                            resultIntent
                        )
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "json parsing error: " + e.message)
                    Toast.makeText(
                        applicationContext,
                        "Json parse error: " + e.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // the push notification is silent, may be other operations needed
                // like inserting it in to SQLite
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            AppController.getInstance().preferenceManager.userFlow.collectLatest(::push)
        }
    }

    /**
     * Processing user specific push message
     * It will be displayed with / without image in push notification tray
     */
    private fun processUserMessage(title: String, isBackground: Boolean, data: String) {
        if (!isBackground) {
            try {
                val datObj = JSONObject(data)
                val imageUrl = datObj.getString("image")
                val mObj = datObj.getJSONObject("message")
                val uObj = datObj.getJSONObject("user")
                val user = User(
                    uObj.getInt("user_id"),
                    uObj.getString("name"),
                    uObj.getString("email"),
                    null,
                    null,
                    null
                )
                val message = MessageItem(
                    mObj.getInt("message_id"),
                    mObj.getString("message"),
                    mObj.getString("created_at"),
                    user
                )

                // verifying whether the app is in background or foreground
                if (!NotificationUtils.isAppIsInBackground(applicationContext)) {

                    // app is in foreground, broadcast the push message
                    val pushNotification = Intent(Config.PUSH_NOTIFICATION).putExtra("type", Config.PUSH_TYPE_USER).putExtra("message", message)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification)

                    // play notification sound
                    val notificationUtils = NotificationUtils()
                    notificationUtils.playNotificationSound()
                } else {

                    // app is in background. show the message in notification try
                    val resultIntent = Intent(applicationContext, MainActivity::class.java)

                    // check for push notification image attachment
                    if (TextUtils.isEmpty(imageUrl))
                        showNotificationMessage(applicationContext, title, user.name + " : " + message.message, message.time, resultIntent)
                    else {
                        // push notification contains image
                        // show it with the image
                        showNotificationMessageWithBigImage(applicationContext, title, message.message, message.time, resultIntent, imageUrl)
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG, "json parsing error: " + e.message)
                Toast.makeText(
                    applicationContext,
                    "Json parse error: " + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            // the push notification is silent, may be other operations needed
            // like inserting it in to SQLite
        }
    }

    /**
     * Showing notification with text only
     */
    private fun showNotificationMessage(context: Context, title: String, message: String, timeStamp: String?, intent: Intent) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils?.showNotificationMessage(title, message, timeStamp!!, intent)
    }

    /**
     * Showing notification with text and image
     */
    private fun showNotificationMessageWithBigImage(context: Context, title: String, message: String?, timeStamp: String?, intent: Intent, imageUrl: String) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils?.showNotificationMessage(title, message, timeStamp!!, intent, imageUrl)
    }

    companion object {
        private val TAG = MyFcmPushReceiver::class.java.simpleName
    }
}