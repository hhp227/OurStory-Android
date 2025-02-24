package com.hhp227.application.helper

import android.content.Context
import android.util.Log
import com.hhp227.application.app.AppController.Companion.userDataStore
import com.hhp227.application.model.User
import com.hhp227.application.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferenceManager private constructor(context: Context) {
    private val dataStore = context.userDataStore

    val userPreferences: Flow<UserPreferences>
        get() = dataStore.data.catch { e ->
            if (e is IOException) {
                Log.e(TAG, "Error reading preference.", e)
                emit(UserPreferences(null))
            } else {
                throw e
            }
        }

    val userFlow: Flow<User?>
        get() = userPreferences.map { it.user }

    val notificationsFlow: Flow<String?>
        get() = userPreferences.map { it.notifications }

    suspend fun storeUser(user: User?) {
        dataStore.updateData { it.copy(user) }
    }

    suspend fun addNotification(notification: String) {
        dataStore.updateData {
            var oldNotifications = it.notifications
            if (oldNotifications != null) oldNotifications += "|$notification" else oldNotifications = notification
            it.copy(notifications = oldNotifications)
        }
    }

    suspend fun fetchInitialPreferences() = userPreferences.first()

    companion object {
        private val TAG = PreferenceManager::class.java.simpleName

        @Volatile
        private var instance: PreferenceManager? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: PreferenceManager(context).also { instance = it }
            }
    }
}