package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.messaging.FirebaseMessaging
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentMainBinding
import com.hhp227.application.databinding.NavHeaderMainBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MainViewModel

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels {
        InjectorUtils.provideMainViewModelFactory()
    }

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
        subscribeUi(NavHeaderMainBinding.bind(binding.navigationView.getHeaderView(0)))
        FirebaseMessaging.getInstance().subscribeToTopic("topic_" + "1") // 1번방의 메시지를 받아옴
        setFragmentResultListener(findNavController().currentDestination?.displayName ?: "") { _, b ->
            childFragmentManager.findFragmentById(R.id.nav_host_container)?.also { navHostFragment ->
                navHostFragment.childFragmentManager.fragments.forEach { fragment ->
                    when (fragment) {
                        is LoungeFragment -> fragment.onFragmentResult(b)
                        is GroupFragment -> fragment.onFragmentResult(b)
                    }
                }
            }
        }
    }

    private fun subscribeUi(navHeaderMainBinding: NavHeaderMainBinding) {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                with(navHeaderMainBinding) {
                    this.user = user
                    this.onProfileImageClickListener = View.OnClickListener {
                        findNavController().navigate(R.id.profileFragment)
                    }
                }
            } else {
                findNavController().popBackStack()
                findNavController().navigate(R.id.loginFragment)
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
                R.id.logout -> viewModel.clear()
                else -> it.onNavDestinationSelected(navController)
            }
            binding.drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }
}