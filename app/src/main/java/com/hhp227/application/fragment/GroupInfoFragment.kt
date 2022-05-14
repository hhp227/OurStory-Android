package com.hhp227.application.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentGroupInfoBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupInfoViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class GroupInfoFragment : DialogFragment() {
    private val viewModel: GroupInfoViewModel by viewModels {
        InjectorUtils.provideGroupInfoViewModelFactory(this)
    }

    private var binding: FragmentGroupInfoBinding by autoCleared()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                requestFeature(Window.FEATURE_NO_TITLE)
                setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isSignUp = findNavController().previousBackStackEntry?.destination?.id == R.id.findGroupFragment
        binding.tvName.text = viewModel.group.groupName
        binding.tvDescription.text = viewModel.group.description
        binding.bRequest.text = if (isSignUp) getString(R.string.request_join) else getString(R.string.request_cancel)

        binding.bRequest.setOnClickListener { viewModel.sendRequest(isSignUp) }
        binding.bClose.setOnClickListener { dismiss() }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when {
                    state.isSuccess -> {
                        if (isSignUp) {
                            setFragmentResult("result1", bundleOf())
                            findNavController().navigateUp()
                        } else {
                            setFragmentResult("${findNavController().previousBackStackEntry?.destination?.id}", bundleOf())
                        }
                        findNavController().navigateUp()
                    }
                    state.error.isNotBlank() -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        findNavController().navigateUp()
    }
}
