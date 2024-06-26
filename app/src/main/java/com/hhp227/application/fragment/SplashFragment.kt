package com.hhp227.application.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentSplashBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MainViewModel

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
        viewModel.user.observe(viewLifecycleOwner) { user ->
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().popBackStack()
                findNavController().navigate(user?.let { R.id.mainFragment } ?: R.id.loginFragment)
                //requireActivity().overridePendingTransition(R.anim.splash_in, R.anim.splash_out)
            }, SPLASH_TIME_OUT)
        }
    }

    companion object {
        private const val SPLASH_TIME_OUT = 1250L
    }
}