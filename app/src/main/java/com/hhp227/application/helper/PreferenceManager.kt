package com.hhp227.application.helper

import android.content.Context
import android.util.Log
import com.hhp227.application.app.AppController.Companion.userDataStore
import com.hhp227.application.model.User
import com.hhp227.application.model.UserPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class PreferenceManager(context: Context) {
    private val userDataStore = context.userDataStore

    val userPreference: Flow<UserPreference>
        get() = userDataStore.data.catch { e ->
            if (e is IOException) {
                Log.e(TAG, "Error reading preference.", e)
                emit(UserPreference(null))
            } else {
                throw e
            }
        }

    val notifications: Flow<String?>
        get() = userPreference.map { it.notifications }

    fun getUserFlow() = userPreference.map { it.user }

    suspend fun storeUser(user: User?) {
        userDataStore.updateData { it.copy(user) }
    }

    suspend fun clearUser() {
        userDataStore.updateData { it.copy(null) }
    }

    suspend fun fetchInitialPreferences() = userPreference.first()

    suspend fun addNotification(notification: String) {
        userDataStore.updateData {
            var oldNotifications = it.notifications
            if (oldNotifications != null) oldNotifications += "|$notification" else oldNotifications = notification
            it.copy(notifications = oldNotifications)
        }
    }

    companion object {
        // LogCat tag
        private const val TAG = "세션메니져"
    }
}