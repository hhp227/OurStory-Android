package com.hhp227.application.fcm

import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.hhp227.application.app.AppController.Companion.getInstance
import com.hhp227.application.app.Config
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.UserItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class MyInstanceIDListenerService : FirebaseMessagingService() {
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        Log.e(TAG, "onTokenRefresh : $token")
        try {
            // Saving reg id to shared preferences
            storeRegIdInPref(token)

            // sending reg id to your server
            sendRegistrationToServer(token)
            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, true).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete token refresh", e)
            sharedPreferences.edit().putBoolean(Config.SENT_TOKEN_TO_SERVER, false).apply()
        }

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        val registrationComplete = Intent(Config.REGISTRATION_COMPLETE)
        registrationComplete.putExtra("token", token)
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete)
    }

    private fun sendRegistrationToServer(token: String) {
        fun registration(user: UserItem?) {

            // checking for valid login session
            val (id) = user
                ?: // TODO
                // user not found, redirecting him to login screen
                return
            val endPoint = URLs.URL_USER_FCM.replace("{USER_ID}", id.toString())
            Log.e(TAG, "endpoint: $endPoint")
            val strReq: StringRequest = object : StringRequest(Method.PUT, endPoint, Response.Listener { response ->
                Log.e(TAG, "response: $response")
                try {
                    val obj = JSONObject(response)

                    // check for error
                    if (!obj.getBoolean("error")) {
                        // broadcasting token sent to server
                        val registrationComplete = Intent(Config.SENT_TOKEN_TO_SERVER)
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(registrationComplete)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Unable to send fcm registration id to our sever. ${obj.getJSONObject("error").getString("message")}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "json parsing error: ${e.message}")
                    Toast.makeText(
                        applicationContext,
                        "Json parse error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                Log.e(TAG, "Volley error: ${error.message}, code: $networkResponse")
                Toast.makeText(
                    applicationContext,
                    "Volley error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }) {
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    params["fcm_registration_id"] = token
                    Log.e(TAG, "params: $params")
                    return params
                }
            }

            //Adding request to request queue
            getInstance().addToRequestQueue(strReq)
        }

        CoroutineScope(Dispatchers.Main).launch {
            getInstance().preferenceManager.userFlow.collect(::registration)
        }
    }

    private fun storeRegIdInPref(token: String) {
        val pref = applicationContext.getSharedPreferences(Config.SHARED_PREF, 0)
        val editor = pref.edit()
        editor.putString("regId", token)
        editor.commit()
    }

    companion object {
        private val TAG = MyInstanceIDListenerService::class.java.simpleName
    }
}