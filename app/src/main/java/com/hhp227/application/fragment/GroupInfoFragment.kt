package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest

import com.hhp227.application.activity.JoinRequestGroupActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentGroupInfoBinding
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupInfoViewModel
import org.json.JSONObject

class GroupInfoFragment : DialogFragment() {
    private val viewModel: GroupInfoViewModel by viewModels()

    private var binding: FragmentGroupInfoBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.requestType = it.getInt("request_type")
            viewModel.joinType = it.getInt("join_type")
            viewModel.groupId = it.getInt("group_id")
            viewModel.groupName = it.getString("group_name")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGroupInfoBinding.inflate(inflater, container, false)

        dialog?.window?.run {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvName.text = viewModel.groupName
        binding.bRequest.text = if (viewModel.requestType == TYPE_REQUEST) "가입신청" else "신청취소"

        binding.bRequest.setOnClickListener {
            val stringRequest = object : StringRequest(if (viewModel.requestType == TYPE_REQUEST) Method.POST else Method.DELETE, if (viewModel.requestType == TYPE_REQUEST) URLs.URL_GROUP_JOIN_REQUEST else "${URLs.URL_LEAVE_GROUP}/${viewModel.groupId}", Response.Listener { response ->
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    if (viewModel.requestType == TYPE_REQUEST) {
                        requireActivity().setResult(RESULT_OK)
                        requireActivity().finish()
                    } else if (viewModel.requestType == TYPE_WITHDRAWAL) {
                        (requireActivity() as JoinRequestGroupActivity).refresh()
                        dismiss()
                    }
                }
            }, Response.ErrorListener { error ->
                VolleyLog.e(TAG, error.message)
            }) {
                override fun getHeaders() = hashMapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)

                override fun getParams() = hashMapOf(
                    "group_id" to viewModel.groupId.toString(),
                    "status" to viewModel.joinType.toString() // join type이 0이면 0 1이면 1
                )
            }

            AppController.getInstance().addToRequestQueue(stringRequest)
        }
        binding.bClose.setOnClickListener { dismiss() }
    }

    companion object {
        const val TYPE_REQUEST = 0
        const val TYPE_WITHDRAWAL = 1
        private val TAG = GroupInfoFragment::class.simpleName

        fun newInstance(): DialogFragment = GroupInfoFragment().apply {
            arguments = Bundle()
        }
    }
}
