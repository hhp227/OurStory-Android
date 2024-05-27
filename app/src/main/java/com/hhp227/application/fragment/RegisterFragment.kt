package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentRegisterBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.RegisterViewModel

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
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.message?.isBlank() ?: false -> {
                    Toast.makeText(requireContext(), getString(R.string.register_complete), Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                state.message?.isNotBlank() ?: false -> {
                    requireActivity().currentFocus?.let {
                        Snackbar.make(it, state.message ?: "An unexpected error occured", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}