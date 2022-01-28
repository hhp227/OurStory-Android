package com.hhp227.application.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isEmpty
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.ImageSelectActivity
import com.hhp227.application.activity.ImageSelectActivity.Companion.SELECT_TYPE
import com.hhp227.application.activity.ImageSelectActivity.Companion.SINGLE_SELECT_TYPE
import com.hhp227.application.activity.MyInfoActivity
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMyinfoBinding
import com.hhp227.application.helper.BitmapUtil
import com.hhp227.application.util.Utils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MyInfoViewModel
import com.hhp227.application.volley.util.MultipartRequest
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MyInfoFragment : Fragment() {
    private val viewModel: MyInfoViewModel by viewModels()

    private val permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Intent(requireContext(), ImageSelectActivity::class.java).also { intent ->
                intent.putExtra(SELECT_TYPE, SINGLE_SELECT_TYPE)
                startActivityForResult(intent, CAMERA_PICK_IMAGE_REQUEST_CODE)
            }
        }
    }

    private var binding: FragmentMyinfoBinding by autoCleared()

    private lateinit var snackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyinfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvName.text = viewModel.user.name
            tvEmail.text = viewModel.user.email
            tvCreateAt.text = "${Utils.getPeriodTimeGenerator(requireContext(), viewModel.user.createAt)} 가입"

            Glide.with(this@MyInfoFragment)
                .load(URLs.URL_USER_PROFILE_IMAGE + viewModel.user.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
            ivProfileImage.setOnClickListener {
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
            permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            true
        }
        R.id.camera -> {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                    val photoFile: File? = try {
                        File.createTempFile(
                            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}_", /* prefix */
                            ".jpg", /* suffix */
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) /* directory */
                        ).apply { viewModel.currentPhotoPath = absolutePath }
                    } catch (ex: IOException) {
                        null
                    }

                    photoFile?.also {
                        viewModel.photoURI = FileProvider.getUriForFile(requireContext(), requireContext().packageName, it)

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.photoURI)
                        startActivityForResult(takePictureIntent, WriteActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE)
                    }
                }
            }
            true
        }
        R.id.remove -> {
            Glide.with(this@MyInfoFragment)
                .load(resources.getDrawable(R.drawable.profile_img_circle, null))
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
            inflateMenu {
                showProgressBar()
                actionUpdate("null")
            }
            true
        }
        else -> false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var bitmap: Bitmap? = null

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_PICK_IMAGE_REQUEST_CODE) {
                bitmap = BitmapUtil(requireContext()).bitmapResize(data?.data, 200)
            } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                try {
                    bitmap = BitmapUtil(requireContext()).bitmapResize(viewModel.photoURI, 200)?.let {
                        val ei = ExifInterface(viewModel.currentPhotoPath)
                        val orientation =
                            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

                        BitmapUtil(requireContext()).rotateImage(
                            it, when (orientation) {
                                ExifInterface.ORIENTATION_ROTATE_90 -> 90F
                                ExifInterface.ORIENTATION_ROTATE_180 -> 180F
                                ExifInterface.ORIENTATION_ROTATE_270 -> 270F
                                else -> 0F
                            }
                        )
                    }
                } catch (e: IOException) {
                    Log.e(TAG, e.message!!)
                }
            }
            Glide.with(this@MyInfoFragment)
                .load(bitmap)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(binding.ivProfileImage)
            inflateMenu {
                if (bitmap != null) {
                    showProgressBar()
                    actionSave(bitmap)
                }
            }
        }
    }

    private fun actionSave(bitmap: Bitmap) {
        val multiPartRequest = object : MultipartRequest(Method.POST, URLs.URL_USER_PROFILE_IMAGE_UPLOAD, Response.Listener { response ->
            if (!JSONObject(String(response.data)).getBoolean("error")) {
                actionUpdate(JSONObject(String(response.data)).getString("profile_img"))
            }
        }, Response.ErrorListener {
            VolleyLog.e(TAG, it.message)
        }) {
            override fun getHeaders() = mapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)

            override fun getByteData() = mapOf(
                /**
                 *  프로필 이미지가 아이디 기준으로 일치 하지 않고 시간대로 해버리면 수정이 일어날때마다
                 *  모든 프로필 이미지가 포함된item들을 set해줘야함 추후 수정
                 */
                "profile_img" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
                }.toByteArray())
            )
        }

        AppController.getInstance().addToRequestQueue(multiPartRequest)
    }

    private fun actionUpdate(imageUrl: String) {
        val stringRequest = object : StringRequest(Method.PUT, URLs.URL_PROFILE_EDIT, Response.Listener {
            hideProgressBar()
            requireActivity().setResult(RESULT_OK)
            AppController.getInstance().preferenceManager.also { pm ->
                pm.storeUser(pm.user.apply { profileImage = imageUrl })
            }
            parentFragmentManager.fragments.forEach { fragment -> if (fragment is MyPostFragment) fragment.profileUpdateResult() }
            Snackbar.make(requireView(), "수정되었습니다.", Snackbar.LENGTH_LONG).show()
        }, Response.ErrorListener { error ->
            error.message?.let {
                VolleyLog.e(TAG, it)
            }
        }) {
            override fun getHeaders() = mapOf("Authorization" to viewModel.user.apiKey)

            override fun getParams() = mapOf(
                "profile_img" to imageUrl,
                "status" to "1"
            )
        }

        AppController.getInstance().addToRequestQueue(stringRequest)
    }

    private fun inflateMenu(action: () -> Unit) {
        (requireActivity() as MyInfoActivity).also { activity ->
            if (activity.binding.toolbar.menu.isEmpty()) {
                activity.menuInflater.inflate(R.menu.save, activity.binding.toolbar.menu)
            }
            activity.binding.toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.save -> {
                        action.invoke()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun showProgressBar() {
        Snackbar.make(requireView(), "전송중...", Snackbar.LENGTH_INDEFINITE).let {
            snackbar = it

            it.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup
        }.also {
            it.addView(ProgressBar(requireContext()))
        }
        if (!snackbar.isShown)
            snackbar.show()
    }

    private fun hideProgressBar() = snackbar.takeIf(Snackbar::isShown)?.apply { dismiss() }

    companion object {
        private val TAG = MyInfoFragment::class.simpleName
        const val CAMERA_PICK_IMAGE_REQUEST_CODE = 10
        const val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 20
        private const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045

        fun newInstance(): Fragment = MyInfoFragment()
    }
}