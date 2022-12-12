package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentLoginBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.LoginViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels {
        InjectorUtils.provideLoginViewModelFactory()
    }

    private var binding: FragmentLoginBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로그인 버튼 클릭 이벤트
        binding.bLogin.setOnClickListener { viewModel.login() }

        // 가입하기 클릭 이벤트
        binding.tvRegister.setOnClickListener { findNavController().navigate(R.id.registerFragment) }
        binding.etEmail.doAfterTextChanged { viewModel.email.value = it.toString() }
        binding.etPassword.doAfterTextChanged { viewModel.password.value = it.toString() }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.user != null -> {
                        hideProgressBar()
                        viewModel.storeUser(state.user)
                    }
                    state.error.isNotBlank() -> {
                        hideProgressBar()
                        Snackbar.make(requireView(), state.error, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.loginFormState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                state.emailError?.let { error -> binding.etEmail.error = getString(error) }
                state.passwordError?.let { error -> binding.etPassword.error = getString(error) }
            }
            .launchIn(lifecycleScope)
        viewModel.userFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { user ->
                if (user != null) {
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.mainFragment)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.run { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.run { visibility = View.GONE }
}