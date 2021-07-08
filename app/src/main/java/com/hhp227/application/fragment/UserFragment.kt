package com.hhp227.application.fragment

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
import com.hhp227.application.util.autoCleared

class UserFragment : DialogFragment() {
    private var binding: FragmentUserBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)

        dialog?.window?.apply {
            requestFeature(FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var userId: String? = null
        var name: String? = null
        var email: String? = null
        var profileImage: String? = null
        var createdAt: String? = null

        arguments?.apply {
            userId = getString("user_id")
            name = getString("name")
            profileImage = getString("profile_img")
            createdAt = getString("created_at")
        }
        activity?.let {
            Glide.with(it)
                .load("${URLs.URL_USER_PROFILE_IMAGE}$profileImage")
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
            binding.tvName.text = name
            binding.tvCreateAt.text = createdAt
        }
    }

    companion object {
        fun newInstance(): DialogFragment = UserFragment()
    }
}