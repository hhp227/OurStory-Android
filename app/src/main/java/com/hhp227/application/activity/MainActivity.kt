package com.hhp227.application.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil.setContentView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.R
import com.hhp227.application.app.Config
import com.hhp227.application.databinding.ActivityMainBinding
import com.hhp227.application.model.ChatItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels {
        InjectorUtils.provideMainViewModelFactory()
    }

    private lateinit var registrationBroadcastReceiver: BroadcastReceiver

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        registrationBroadcastReceiver = RegistrationBroadcastReceiver()

        installSplashScreen().setKeepOnScreenCondition {
            !viewModel.isReady
        }

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this) {
            "ca-app-pub-3940256099942544~3347511713"
        }
        //FirebaseMessaging.getInstance().subscribeToTopic("topic_" + "1") // 1번방의 메시지를 받아옴*/
        permissionCheck()
    }

    override fun onResume() {
        super.onResume()
        /*networkConnectionCheck()
        AppController.getInstance().setConnectivityListener { isConnected ->
            Log.e("TESTR", "$isConnected")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver, IntentFilter(Config.REGISTRATION_COMPLETE))
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver, IntentFilter(Config.PUSH_NOTIFICATION))
        NotificationUtils.clearNotifications()*/
    }

    override fun onPause() {
        super.onPause()
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "Permission is denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Permission is granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*private fun networkConnectionCheck() {
        if (!ConnectivityReceiver.isConnected) {
            AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.connection_error_title))
                .setMessage(getString(R.string.connection_error_message))
                .setPositiveButton(getString(R.string.connection_error_positive_button)) { _, _ ->
                    when (supportFragmentManager.findFragmentById(binding.contentFrame.id)) {
                        is LoungeFragment -> {
                            supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, LoungeFragment.newInstance()).commit()
                        }
                        is GroupFragment -> {
                            supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, GroupFragment.newInstance()).commit()
                        }
                        is ChatFragment -> {
                            supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, ChatFragment.newInstance()).commit()
                        }
                    }
                    networkConnectionCheck()
                }
                .setNegativeButton(getString(R.string.connection_error_negative_button)) { _, _ -> exitProcess(0) }
                .create()
                .show()
        }
    }*/

    fun setAppBar(toolbar: Toolbar, appbarTitle: String) {
        /*title = appbarTitle

        setSupportActionBar(toolbar)
        ActionBarDrawerToggle(this, binding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
            binding.drawerLayout.addDrawerListener(it)
            it.syncState()
        }*/
    }

    private fun permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    inner class RegistrationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                Config.REGISTRATION_COMPLETE -> FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)
                Config.SENT_TOKEN_TO_SERVER -> Log.e(TAG, "FCM registration id is sent to our server")
                Config.PUSH_NOTIFICATION -> {
                    val message = intent.getSerializableExtra("message") as ChatItem.Message

                    when (intent.getIntExtra("type", -1)) {
                        Config.PUSH_TYPE_CHATROOM -> {
                            Toast.makeText(applicationContext, "${message.message} ${intent.getIntExtra("chat_room_id", -1)}", Toast.LENGTH_LONG).show()
                        }
                        Config.PUSH_TYPE_USER -> Toast.makeText(applicationContext, "New push: ${message.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object {
        private val TAG: String? = MainActivity::class.simpleName
        private const val PERMISSION_REQUEST_CODE = 5000
    }
}