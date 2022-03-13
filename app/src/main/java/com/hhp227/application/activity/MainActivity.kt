package com.hhp227.application.activity

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.hhp227.application.fragment.LoungeFragment
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var registrationBroadcastReceiver: BroadcastReceiver

    private lateinit var binding: ActivityMainBinding

    private val myInfoActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is LoungeFragment) { // 임시코드, Lounge말고도 프로필 업데이트가 더 필요한 fragment가 생길경우 다른방법으로 처리
                fragment.onMyInfoActivityResult(result)
            }
        }
        if (result.resultCode == RESULT_OK) {
            updateProfileImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
                ivProfileImage.setOnClickListener { myInfoActivityResultLauncher.launch(Intent(root.context, MyInfoActivity::class.java)) }
            }
        } ?: logoutUser()
        supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, LoungeFragment()).commit()
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val fragment: Fragment? = when (menuItem.itemId) {
                R.id.nav_menu1 -> LoungeFragment.newInstance()
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
    }

    override fun onResume() {
        super.onResume()
        networkConnectionCheck()
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

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    private fun logoutUser() {
        AppController.getInstance().preferenceManager.clear()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun networkConnectionCheck() {
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
    }

    fun setAppBar(toolbar: Toolbar, appbarTitle: String) {
        title = appbarTitle

        setSupportActionBar(toolbar)
        ActionBarDrawerToggle(this, binding.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
            binding.drawerLayout.addDrawerListener(it)
            it.syncState()
        }
    }

    fun updateProfileImage() {
        Glide.with(baseContext)
            .load(URLs.URL_USER_PROFILE_IMAGE + AppController.getInstance().preferenceManager.user?.profileImage)
            .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
            .into(NavHeaderMainBinding.bind(binding.navigationView.getHeaderView(0)).ivProfileImage)
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
    }
}












