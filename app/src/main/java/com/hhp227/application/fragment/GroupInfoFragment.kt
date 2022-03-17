package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hhp227.application.R
import com.hhp227.application.activity.JoinRequestGroupActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.databinding.FragmentGroupInfoBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupInfoViewModel
import com.hhp227.application.viewmodel.GroupInfoViewModelFactory
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
        binding.tvName.text = viewModel.group.groupName
        binding.bRequest.text = if (viewModel.requestType == TYPE_REQUEST) getString(R.string.request_join) else getString(R.string.request_cancel)

        binding.bRequest.setOnClickListener { viewModel.sendRequest() }
        binding.bClose.setOnClickListener { dismiss() }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isSuccess -> {
                    if (viewModel.requestType == TYPE_REQUEST) {
                        requireActivity().setResult(RESULT_OK)
                        requireActivity().finish()
                    } else if (viewModel.requestType == TYPE_WITHDRAWAL) {
                        (requireActivity() as JoinRequestGroupActivity).refresh()
                        dismiss()
                    }
                }
                state.error.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    companion object {
        const val TYPE_REQUEST = 0
        const val TYPE_WITHDRAWAL = 1

        fun newInstance(): DialogFragment = GroupInfoFragment().apply {
            arguments = Bundle()
        }
    }
}
