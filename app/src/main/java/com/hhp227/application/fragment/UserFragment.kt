package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.Window.*
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.URLs
import kotlinx.android.synthetic.main.fragment_user.*

class UserFragment : DialogFragment() {
    companion object {
        fun newInstance(): DialogFragment = UserFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.let {
            it.window?.apply {
                requestFeature(FEATURE_NO_TITLE)
                setBackgroundDrawableResource(android.R.color.transparent)
            }
        }
        return inflater.inflate(R.layout.fragment_user, container, false)
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
                .into(ivProfileImage)
            tvName.text = name
            tvCreateAt.text = createdAt
        }
    }
}