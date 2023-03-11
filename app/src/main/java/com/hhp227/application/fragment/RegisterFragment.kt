package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
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

// WIP
class RegisterFragment : Fragment() {
    private var binding: FragmentRegisterBinding by autoCleared()

    private val viewModel: RegisterViewModel by viewModels {
        InjectorUtils.provideRegisterViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { state ->
                when {
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