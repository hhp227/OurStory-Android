package com.hhp227.application.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.hhp227.application.dto.UserItem

class PreferenceManager(context: Context) {

    // Shared pref mode
    private var PRIVATE_MODE = 0

    // Shared Preferences
    var pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    var editor: SharedPreferences.Editor = pref.edit()

    val user: UserItem?
        get() {
            if (pref.getInt(KEY_USER_ID, 0) != 0) {
                val id: Int = pref.getInt(KEY_USER_ID, 0)
                val name: String? = pref.getString(KEY_NAME, null)
                val email: String? = pref.getString(KEY_EMAIL, null)
                val apiKey: String? = pref.getString(KEY_APIKEY, null)
                val profileImage: String? = pref.getString(KEY_PROFILE_IMAGE, null)
                val createdAt: String? = pref.getString(KEY_CREATED_AT, null)
                return UserItem(id, name!!, email, apiKey!!, profileImage, createdAt)
            }
            return null
        }

    val notifications: String?
        get() = pref.getString(KEY_NOTIFICATIONS, null)

    fun storeUser(user: UserItem) {
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_APIKEY, user.apiKey)
        editor.putString(KEY_PROFILE_IMAGE, user.profileImage)
        editor.putString(KEY_CREATED_AT, user.createAt)
        editor.commit()
        Log.e(TAG, "사용자 Session 저장. " + user.name + ", " + user.email)
    }

    fun addNotification(notification: String) {

        // get old notifications
        var oldNotifications = notifications
        if (oldNotifications != null) oldNotifications += "|$notification" else oldNotifications = notification
        editor.putString(KEY_NOTIFICATIONS, oldNotifications)
        editor.commit()
    }

    fun clear() {
        editor.clear()
        editor.commit()
    }

    companion object {
        // LogCat tag
        private const val TAG = "세션메니져"

        // Shared preferences file name
        private const val PREF_NAME = "ApplicationLogin"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_APIKEY = "api_key"
        private const val KEY_PROFILE_IMAGE = "profile_img"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_NOTIFICATIONS = "notifications"
    }

}