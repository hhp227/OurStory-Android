package com.hhp227.application.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.R
import com.hhp227.application.activity.LoginActivity
import com.hhp227.application.activity.MyInfoActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMainBinding
import com.hhp227.application.databinding.NavHeaderMainBinding
import com.hhp227.application.util.autoCleared
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private var binding: FragmentMainBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                requireActivity().finish()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(requireContext()) {
            "ca-app-pub-3940256099942544~3347511713"
        }
        AppController.getInstance().preferenceManager.userFlow.onEach { user ->
            if (user != null) {
                with(NavHeaderMainBinding.bind(binding.navigationView.getHeaderView(0))) {
                    tvName.text = user.name
                    tvEmail.text = user.email

                    Glide.with(requireContext())
                        .load(URLs.URL_USER_PROFILE_IMAGE + user.profileImage)
                        .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                        .into(ivProfileImage)
                    ivProfileImage.setOnClickListener { startActivity(Intent(root.context, MyInfoActivity::class.java)) }
                }
            } else {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
        }.launchIn(lifecycleScope)
        requireActivity().supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, LoungeFragment()).commit()
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            val fragment: Fragment? = when (menuItem.itemId) {
                R.id.nav_menu1 -> LoungeFragment.newInstance()
                R.id.nav_menu2 -> GroupFragment.newInstance()
                R.id.nav_menu3 -> ChatFragment.newInstance()
                R.id.nav_menu4 -> {
                    lifecycleScope.launch {
                        AppController.getInstance().preferenceManager.clearUser()
                    }
                    null
                }
                else -> null
            }

            fragment?.let { requireActivity().supportFragmentManager.beginTransaction().replace(binding.contentFrame.id, it).commit() }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        FirebaseMessaging.getInstance().subscribeToTopic("topic_" + "1") // 1번방의 메시지를 받아옴
    }

    
}