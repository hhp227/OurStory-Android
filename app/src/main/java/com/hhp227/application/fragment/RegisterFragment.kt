package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentRegisterBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.RegisterViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding by autoCleared()

    private val viewModel: RegisterViewModel by viewModels {
        InjectorUtils.provideRegisterViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val passwordCheck = binding.etConfirmPassword.text.toString().trim()

            viewModel.register(name, email, password, passwordCheck)
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                when {
                    state.isLoading -> showProgressBar()
                    state.error?.isBlank() ?: false -> {
                        hideProgressBar()
                        Toast.makeText(requireContext(), getString(R.string.register_complete), Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                    state.error?.isNotBlank() ?: false -> {
                        hideProgressBar()
                        requireActivity().currentFocus?.let { Snackbar.make(it, state.error ?: "An unexpected error occured", Snackbar.LENGTH_LONG).show() }
                    }
                }
            }
            .launchIn(lifecycleScope)
        viewModel.registerFormState
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                state.nameError?.let { error -> binding.etName.error = getString(error) }
                state.emailError?.let { error -> binding.etEmail.error = getString(error) }
                state.passwordError?.let { error -> binding.etPassword.error = getString(error) }
                state.passwordCheckError?.let { error -> binding.etConfirmPassword.error = getString(error) }
            }
            .launchIn(lifecycleScope)
        viewModel.userFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { user ->
                if (user != null) {
                    findNavController().navigateUp()
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun showProgressBar() {
        if (binding.progressBar.visibility == View.GONE)
            binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.progressBar.visibility == View.VISIBLE)
            binding.progressBar.visibility = View.GONE
    }
}