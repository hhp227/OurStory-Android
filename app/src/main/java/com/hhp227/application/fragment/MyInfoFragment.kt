package com.hhp227.application.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMyinfoBinding
import com.hhp227.application.dto.User
import com.hhp227.application.util.Utils
import com.hhp227.application.util.autoCleared

class MyInfoFragment : Fragment() {
    private val user: User by lazy { AppController.getInstance().preferenceManager.user }

    private var binding: FragmentMyinfoBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMyinfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(user) {
            binding.tvName.text = name
            binding.tvEmail.text = email
            binding.tvCreateAt.text = "${Utils.getPeriodTimeGenerator(activity, createAt)} 가입"

            Glide.with(this@MyInfoFragment)
                .load(URLs.URL_USER_PROFILE_IMAGE + profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
            binding.ivProfileImage.setOnClickListener {
                registerForContextMenu(it)
                activity!!.openContextMenu(it)
                unregisterForContextMenu(it)
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("프로필 이미지 변경")
        activity?.menuInflater?.inflate(R.menu.myinfo, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.album -> {
            true
        }
        R.id.camera -> {
            true
        }
        R.id.remove -> {
            val stringRequest = object : StringRequest(Method.PUT, URLs.URL_PROFILE_EDIT, Response.Listener {
                Glide.with(context!!)
                    .load(user.profileImage)
                    .apply(RequestOptions.errorOf(R.drawable.profile_img_square))
                    .into(binding.ivProfileImage)
            }, Response.ErrorListener { error ->
                error.message?.let {
                    VolleyLog.e(TAG, it)
                }
            }) {
                override fun getHeaders() = mapOf("Authorization" to user.apiKey)

                override fun getParams() = mapOf("status" to "1")
            }

            AppController.getInstance().addToRequestQueue(stringRequest)
            true
        }
        else -> false
    }

    companion object {
        private val TAG = MyInfoFragment::class.simpleName

        fun newInstance(): Fragment = MyInfoFragment()
    }
}