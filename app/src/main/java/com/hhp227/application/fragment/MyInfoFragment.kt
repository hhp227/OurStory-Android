package com.hhp227.application.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.activity.ImageSelectActivity
import com.hhp227.application.activity.ImageSelectActivity.Companion.SELECT_TYPE
import com.hhp227.application.activity.ImageSelectActivity.Companion.SINGLE_SELECT_TYPE
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMyinfoBinding
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.User
import com.hhp227.application.helper.BitmapUtil
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
            binding.tvCreateAt.text = "${Utils.getPeriodTimeGenerator(requireContext(), createAt)} 가입"

            Glide.with(this@MyInfoFragment)
                .load(URLs.URL_USER_PROFILE_IMAGE + profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
            binding.ivProfileImage.setOnClickListener {
                registerForContextMenu(it)
                requireActivity().openContextMenu(it)
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
            ContextCompat.checkSelfPermission(requireContext(), requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_REQUEST).toString())
            true
        }
        R.id.camera -> {
            true
        }
        R.id.remove -> {
            val stringRequest = object : StringRequest(Method.PUT, URLs.URL_PROFILE_EDIT, Response.Listener {
                Glide.with(requireContext())
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent(requireContext(), ImageSelectActivity::class.java).also { intent ->
                        intent.putExtra(SELECT_TYPE, SINGLE_SELECT_TYPE)
                        startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val data = data?.data

        }
    }

    companion object {
        private val TAG = MyInfoFragment::class.simpleName
        const val CAMERA_PICK_IMAGE_REQUEST_CODE = 10
        const val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 20
        private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

        fun newInstance(): Fragment = MyInfoFragment()
    }
}