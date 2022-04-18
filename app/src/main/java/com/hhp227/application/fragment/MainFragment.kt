package com.hhp227.application.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMainBinding
import com.hhp227.application.databinding.NavHeaderMainBinding
import com.hhp227.application.util.autoCleared
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

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
                    ivProfileImage.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
                }
            } else {
                findNavController().popBackStack()
                findNavController().navigate(R.id.loginFragment)
            }
        }.launchIn(lifecycleScope)
        FirebaseMessaging.getInstance().subscribeToTopic("topic_" + "1") // 1번방의 메시지를 받아옴
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { _, b ->
            childFragmentManager.findFragmentById(R.id.nav_host_container)?.also { navHostFragment ->
                navHostFragment.childFragmentManager.fragments.forEach { fragment ->
                    if (fragment is LoungeFragment) {
                        fragment.onFragmentResult(b)
                    }
                }
            }
        }
    }

    fun setNavAppbar(toolbar: MaterialToolbar) {
        val navController = (childFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment).findNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loungeFragment, R.id.groupFragment, R.id.friendFragment, R.id.chatFragment), binding.drawerLayout
        )

        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.logout -> lifecycleScope.launch {
                    (requireActivity().application as AppController).preferenceManager.clearUser()
                }
                else -> it.onNavDestinationSelected(navController)
            }
            binding.drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }
}