package com.hhp227.application.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.hhp227.application.R
import com.hhp227.application.activity.FeedbackActivity
import com.hhp227.application.activity.MyInfoActivity
import com.hhp227.application.activity.VerInfoActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTab4Binding
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.User
import com.hhp227.application.util.autoCleared
import org.json.JSONException

class Tab4Fragment : Fragment(), View.OnClickListener {
    private var groupId = 0

    private var authorId = 0

    private var isAuth = false

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getInt(ARG_PARAM1)
            authorId = it.getInt(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.isRefreshing = false

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    return ViewHolder(FragmentTab4Binding.inflate(LayoutInflater.from(parent.context), parent, false))
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    holder.bind(AppController.getInstance().preferenceManager.user)
                }

                override fun getItemCount(): Int = 1
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile -> Intent(requireContext(), MyInfoActivity::class.java).also(::startActivity)
            R.id.ll_withdrawal -> AlertDialog.Builder(requireContext()).setMessage(if (isAuth) "폐쇄" else "탈퇴" + "하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                        Method.DELETE,
                        "${if (isAuth) URLs.URL_GROUP else URLs.URL_LEAVE_GROUP}/$groupId",
                        null,
                        Response.Listener { response ->
                            try {
                                if (!response.getBoolean("error")) {
                                    requireActivity().setResult(Activity.RESULT_OK, Intent(context, GroupFragment::class.java))
                                    requireActivity().finish()
                                    // 글쓰기나 글삭제후 그룹탈퇴하면 GroupFragment 목록이 새로고침이 되지 않음
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener { error ->
                            VolleyLog.e(TAG, error.message)
                        }) {
                        override fun getHeaders() = mapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)
                    }

                    AppController.getInstance().addToRequestQueue(jsonObjectRequest)
                }
                .setNegativeButton("아니오") { dialog, _ -> dialog.dismiss() }
                .show()
            R.id.appstore -> Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")).also(::startActivity)
            R.id.feedback -> Intent(requireContext(), FeedbackActivity::class.java).also(::startActivity)
            R.id.verinfo -> Intent(requireContext(), VerInfoActivity::class.java).also(::startActivity)
            R.id.share -> startActivity(Intent.createChooser(Intent().let { intent ->
                intent.setType("text/plain").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                intent.putExtra(Intent.EXTRA_TEXT,
                    """
     확인하세요
     GitHub Page :  https://localhost/Sample App : https://play.google.com/store/apps/details?id=
     """.trimIndent()
                )
            }, getString(R.string.app_name)))
        }
    }

    inner class ViewHolder(private val binding: FragmentTab4Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            isAuth = user.id == authorId
            pname.text = user.name
            pemail.text = user.email
            tvWithdrawal.text = "그룹" + if (isAuth) "폐쇄" else "탈퇴"

            Glide.with(binding.root)
                .load(URLs.URL_USER_PROFILE_IMAGE + user.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
            adView.loadAd(AdRequest.Builder().build())
        }

        init {
            with(binding) {
                profile.setOnClickListener(::onClick)
                llWithdrawal.setOnClickListener(::onClick)
                appstore.setOnClickListener(::onClick)
                feedback.setOnClickListener(::onClick)
                verinfo.setOnClickListener(::onClick)
                share.setOnClickListener(::onClick)
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "author_id"
        private val TAG = Tab4Fragment::class.java.simpleName

        fun newInstance(groupId: Int, authorId: Int) = Tab4Fragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PARAM1, groupId)
                putInt(ARG_PARAM2, authorId)
            }
        }
    }
}