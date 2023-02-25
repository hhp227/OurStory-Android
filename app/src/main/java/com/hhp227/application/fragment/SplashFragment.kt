package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentSplashBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// WIP
class SplashFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels {
        InjectorUtils.provideMainViewModelFactory()
    }

    private var binding: FragmentSplashBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.userFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { user ->
                delay(SPLASH_TIME_OUT)
                findNavController().popBackStack()
                findNavController().navigate(user?.let { R.id.mainFragment } ?: R.id.loginFragment)
                //requireActivity().overridePendingTransition(R.anim.splash_in, R.anim.splash_out)
            }
            .launchIn(lifecycleScope)
    }

    companion object {
        private const val SPLASH_TIME_OUT = 1250L
    }
}