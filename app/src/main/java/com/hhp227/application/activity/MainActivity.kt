package com.hhp227.application.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.Config
import com.hhp227.application.app.ConnectivityReceiver
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityMainBinding
import com.hhp227.application.databinding.NavHeaderMainBinding
import com.hhp227.application.dto.MessageItem
import com.hhp227.application.fcm.NotificationUtils
import com.hhp227.application.fragment.ChatFragment
import com.hhp227.application.fragment.GroupFragment
import com.hhp227.application.fragment.MainFragment
import com.hhp227.application.helper.PreferenceManager

class MainActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var registrationBroadcastReceiver: BroadcastReceiver

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        preferenceManager = PreferenceManager(this)
        registrationBroadcastReceiver = RegistrationBroadcastReceiver()

        setContentView(binding.root)

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this) {
            "ca-app-pub-3940256099942544~3347511713"
        }
        AppController.getInstance().preferenceManager.user?.let { user ->
            with(NavHeaderMainBinding.bind(binding.navigationView.getHeaderView(0))) {
                tvName.text = user.name
                tvEmail.text = user.email

                Glide.with(baseContext)
                    .load(URLs.URL_USER_PROFILE_IMAGE + user.profileImage)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                    .into(ivProfileImage)
                ivProfileImage.setOnClickListener { startActivityForResult(Intent(root.context, MyInfoActivity::class.java), PROFILE_UPDATE_CODE) }
            }
        } ?: logoutUser()
        supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, MainFragment()).commit()
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val fragment: Fragment? = when (menuItem.itemId) {
                R.id.nav_menu1 -> MainFragment.newInstance()
                R.id.nav_menu2 -> GroupFragment.newInstance()
                R.id.nav_menu3 -> ChatFragment.newInstance()
                R.id.nav_menu4 -> {
                    logoutUser()
                    null
                }
                else -> null
            }

            fragment?.let { supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, it).commit() }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        FirebaseMessaging.getInstance().subscribeToTopic("topic_" + "1") // 1번방의 메시지를 받아옴
        Log.e("TEST", "${ConnectivityReceiver.isConnected}")
    }

    override fun onResume() {
        super.onResume()
        AppController.getInstance().setConnectivityListener { isConnected ->
            Log.e("TESTR", "$isConnected")
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver, IntentFilter(Config.REGISTRATION_COMPLETE))
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver, IntentFilter(Config.PUSH_NOTIFICATION))
        NotificationUtils.clearNotifications()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.fragments.forEach { fragment -> fragment.onActivityResult(requestCode, resultCode, data) }
        if (resultCode == RESULT_OK) {
            Glide.with(baseContext)
                .load(URLs.URL_USER_PROFILE_IMAGE + AppController.getInstance().preferenceManager.user.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(NavHeaderMainBinding.bind(binding.navigationView.getHeaderView(0)).ivProfileImage)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    private fun logoutUser() {
        preferenceManager.clear()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    inner class RegistrationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                Config.REGISTRATION_COMPLETE -> FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL)
                Config.SENT_TOKEN_TO_SERVER -> Log.e(TAG, "FCM registration id is sent to our server")
                Config.PUSH_NOTIFICATION -> {
                    val message = intent.getSerializableExtra("message") as MessageItem

                    when (intent.getIntExtra("type", -1)) {
                        Config.PUSH_TYPE_CHATROOM -> {
                            intent.getStringExtra("chat_room_id")?.let {
                                Toast.makeText(applicationContext, "${message.message} $it", Toast.LENGTH_LONG).show()
                            }
                        }
                        Config.PUSH_TYPE_USER -> Toast.makeText(applicationContext, "New push: ${message.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    companion object {
        const val PROFILE_UPDATE_CODE = 200
        private val TAG: String? = MainActivity::class.simpleName
    }
}