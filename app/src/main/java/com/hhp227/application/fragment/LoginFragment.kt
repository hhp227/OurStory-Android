package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentLoginBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.LoginViewModel

class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels {
        InjectorUtils.provideLoginViewModelFactory()
    }

    private var binding: FragmentLoginBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvRegister.setOnClickListener { findNavController().navigate(R.id.registerFragment) }
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.user != null -> {
                    viewModel.storeUser(state.user)
                }
                state.message.isNotBlank() -> {
                    Snackbar.make(requireView(), state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                findNavController().popBackStack()
                findNavController().navigate(R.id.mainFragment)
            }
        }
    }
}
