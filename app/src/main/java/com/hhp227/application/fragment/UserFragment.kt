package com.hhp227.application.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.FEATURE_NO_TITLE
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.hhp227.application.R
import com.hhp227.application.util.URLs
import com.hhp227.application.databinding.FragmentUserBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.UserViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

// WIP
class UserFragment : DialogFragment() {
    private val viewModel: UserViewModel by viewModels {
        InjectorUtils.provideUserViewModelFactory(this)
    }

    private var binding: FragmentUserBinding by autoCleared()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                requestFeature(FEATURE_NO_TITLE)
                setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvName.text = viewModel.user?.name
        binding.tvCreateAt.text = viewModel.user?.createdAt

        binding.ivProfileImage.load("${URLs.URL_USER_PROFILE_IMAGE}${viewModel.user?.profileImage}") {
            placeholder(R.drawable.profile_img_circle)
            error(R.drawable.profile_img_circle)
            transformations(CircleCropTransformation())
        }
        binding.bSend.setOnClickListener { viewModel.addFriend() }
        binding.bClose.setOnClickListener { dismiss() }
        combine(viewModel.state, viewModel.userFlow) { state, user ->
            when {
                state.isLoading -> {

                }
                state.result.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.result, Toast.LENGTH_LONG).show()
                }
                user?.id != viewModel.user?.id -> {
                    binding.bSend.text = getString(if (!state.isFriend) R.string.add_friend else R.string.remove_friend)
                }
                state.error.isNotEmpty() -> {
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