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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.app.URLs.Companion.URL_USER_FRIEND
import com.hhp227.application.databinding.FragmentUserBinding
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.autoCleared
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class UserFragment : DialogFragment() {
    private var binding: FragmentUserBinding by autoCleared()

    var apiKey = ""

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
        AppController.getInstance().preferenceManager.userFlow.onEach { user ->
            apiKey = user?.apiKey ?: ""
        }.launchIn(lifecycleScope)
        binding.bSend.setOnClickListener {
            Toast.makeText(requireContext(), "${URL_USER_FRIEND.replace("{USER_ID}", user?.id.toString())}", Toast.LENGTH_LONG).show()
            AppController.getInstance().addToRequestQueue(object : JsonObjectRequest(Method.GET, URL_USER_FRIEND.replace("{USER_ID}", user?.id.toString()), null, Response.Listener { response ->
                if (!response.getBoolean("error")) {
                    Toast.makeText(requireContext(), "${response.getString("result")}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "error", Toast.LENGTH_LONG).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(requireContext(), "${it.message}, ${it.networkResponse.statusCode}", Toast.LENGTH_LONG).show()
            }) {
                override fun getHeaders(): MutableMap<String, String?> = hashMapOf("Authorization" to apiKey)
            })
        }
        binding.bClose.setOnClickListener { dismiss() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        findNavController().navigateUp()
    }
}