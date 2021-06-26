package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest

import com.hhp227.application.R
import com.hhp227.application.activity.NotJoinedGroupActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.util.autoCleared
import kotlinx.android.synthetic.main.fragment_group_info.*
import org.json.JSONObject
import kotlin.properties.Delegates

class GroupInfoFragment : DialogFragment() {
    private var requestType by Delegates.notNull<Int>()

    private var joinType by Delegates.notNull<Int>()

    private var groupId by Delegates.notNull<Int>()

    private var groupName: String by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            requestType = it.getInt("request_type")
            joinType = it.getInt("join_type")
            groupId = it.getInt("group_id")
            groupName = it.getString("group_name")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.run {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        return inflater.inflate(R.layout.fragment_group_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_name.text = groupName
        b_request.text = if (requestType == TYPE_REQUEST) "가입신청" else "신청취소"

        b_request.setOnClickListener {
            val stringRequest = object : StringRequest(if (requestType == TYPE_REQUEST) Method.POST else Method.DELETE, if (requestType == TYPE_REQUEST) URLs.URL_GROUP_JOIN_REQUEST else "${URLs.URL_LEAVE_GROUP}/$groupId", Response.Listener { response ->
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    if (requestType == TYPE_REQUEST) {
                        activity!!.setResult(RESULT_OK)
                        activity!!.finish()
                    } else if (requestType == TYPE_WITHDRAWAL) {
                        (activity!! as NotJoinedGroupActivity).refresh()
                        dismiss()
                    }
                }
            }, Response.ErrorListener { error ->
                VolleyLog.e(TAG, error.message)
            }) {
                override fun getHeaders() = hashMapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)

                override fun getParams() = hashMapOf(
                    "group_id" to groupId.toString(),
                    "status" to joinType.toString() // join type이 0이면 0 1이면 1
                )
            }

            AppController.getInstance().addToRequestQueue(stringRequest)
        }
        b_close.setOnClickListener { dismiss() }
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
