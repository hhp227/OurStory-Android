package com.hhp227.application.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentUserBinding
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.autoCleared

class UserFragment : DialogFragment() {
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
        val user = arguments?.getParcelable<UserItem>("user")

        activity?.let {
            binding.tvName.text = user?.name
            binding.tvCreateAt.text = user?.createAt

            Glide.with(it)
                .load("${URLs.URL_USER_PROFILE_IMAGE}${user?.profileImage}")
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
        }
    }

    companion object {
        fun newInstance(): DialogFragment = UserFragment()
    }
}