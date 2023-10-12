package com.hhp227.application.fcm

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.text.Html
import android.text.TextUtils
import android.util.Patterns
import androidx.core.app.NotificationCompat
import com.hhp227.application.R
import com.hhp227.application.app.AppController.Companion.getInstance
import com.hhp227.application.app.Config
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class NotificationUtils() {
    private var context: Context? = null

    constructor(context: Context?) : this() {
        this.context = context
    }

    @JvmOverloads
    fun showNotificationMessage(title: String, message: String?, timeStamp: String, intent: Intent, imageUrl: String? = null) {
        // Check for empty push message
        if (TextUtils.isEmpty(message)) return

        // notification icon
        val icon = R.drawable.ic_launcher
        val resultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val mBuilder = NotificationCompat.Builder(context!!)
        val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context?.packageName + "/raw/notification")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl != null && imageUrl.length > 4 && Patterns.WEB_URL.matcher(imageUrl).matches()) {
                val bitmap = getBitmapFromURL(imageUrl)

                if (bitmap != null)
                    showBigNotification(bitmap, mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
                else
                    showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
            }
        } else {
            showSmallNotification(mBuilder, icon, title, message, timeStamp, resultPendingIntent, alarmSound)
            playNotificationSound()
        }
    }

    private fun showSmallNotification(mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String?, timeStamp: String, resultPendingIntent: PendingIntent, alarmSound: Uri) {
        val inboxStyle = NotificationCompat.InboxStyle()
        if (Config.appendNotificationMessages) {
            // store the notification in shared pref first
            getInstance().preferenceManager.addNotification(message!!)

            // get the notifications from shared preferences
            val oldNotification = getInstance().preferenceManager.notifications
            val messages = listOf(*oldNotification!!.split("\\|".toRegex()).toTypedArray())

            for (i in messages.indices.reversed()) {
                inboxStyle.addLine(messages[i])
            }
        } else {
            inboxStyle.addLine(message)
        }
        val notification: Notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentIntent(resultPendingIntent)
            .setSound(alarmSound)
            .setStyle(inboxStyle)
            .setWhen(getTimeMilliSec(timeStamp))
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context?.resources, icon))
            .setContentText(message)
            .build()
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(Config.NOTIFICATION_ID, notification)
    }

    private fun showBigNotification(bitmap: Bitmap, mBuilder: NotificationCompat.Builder, icon: Int, title: String, message: String?, timeStamp: String, resultPendingIntent: PendingIntent, alarmSound: Uri) {
        val bigPictureStyle = NotificationCompat.BigPictureStyle()
            .setBigContentTitle(title)
            .setSummaryText(Html.fromHtml(message).toString())
            .bigPicture(bitmap)
        val notification: Notification = mBuilder.setSmallIcon(icon).setTicker(title).setWhen(0)
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentIntent(resultPendingIntent)
            .setSound(alarmSound)
            .setStyle(bigPictureStyle)
            .setWhen(getTimeMilliSec(timeStamp))
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(context?.resources, icon))
            .setContentText(message)
            .build()
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(Config.NOTIFICATION_ID_BIG_IMAGE, notification)
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    private fun getBitmapFromURL(strURL: String?): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // Playing notification sound
    fun playNotificationSound() {
        try {
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getInstance().applicationContext.packageName + "/raw/unsure")
            val r = RingtoneManager.getRingtone(getInstance().applicationContext, alarmSound)

            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = NotificationUtils::class.java.simpleName

        /**
         * Method checks if the app is in background or not
         */
        fun isAppIsInBackground(context: Context): Boolean {
            var isInBackground = true
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses

            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) if (activeProcess == context.packageName) isInBackground =
                        false
                }
            }
            return isInBackground
        }

        // Clears notification tray messages
        fun clearNotifications() {
            val notificationManager = getInstance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.cancelAll()
        }

        fun getTimeMilliSec(timeStamp: String?): Long {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            try {
                val date = format.parse(timeStamp)
                return date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return 0
        }
    }
}