package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.activity.MainActivity.Companion.PROFILE_UPDATE_CODE
import com.hhp227.application.adapter.MemberGridAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.MemberItem
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MemberViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import kotlin.jvm.Throws

class MemberFragment : Fragment() {
    private val viewModel: MemberViewModel by viewModels()

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.groupId = it.getInt(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
            adapter = MemberGridAdapter().apply {
                submitList(viewModel.memberItems)
                setOnItemClickListener { _, p ->
                    val (userId, name, email, profileImage, createdAt) = viewModel.memberItems[p]
                    val newFragment = UserFragment.newInstance().apply {
                        arguments = Bundle().apply {
                            putInt("user_id", userId)
                            putString("name", name)
                            putString("email", email)
                            putString("profile_img", profileImage)
                            putString("created_at", createdAt)
                        }
                    }

                    newFragment.show(childFragmentManager, "dialog")
                }
            }

            addOnScrollListener(object : RecyclerView.OnScrollListener() {

            })
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false

                viewModel.memberItems.clear()
                fetchDataTask()
            }
        }
        showProgressBar()
        fetchDataTask()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PROFILE_UPDATE_CODE && resultCode == RESULT_OK) {
            with(AppController.getInstance().preferenceManager) {
                viewModel.memberItems.find { it.id == user.id }
                    .let(viewModel.memberItems::indexOf)
                    .also { i ->
                        viewModel.memberItems[i].profileImage = user.profileImage

                        binding.recyclerView.adapter?.notifyItemChanged(i)
                    }
            }
        }
    }

    private fun fetchDataTask() {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, "${URLs.URL_MEMBER}/${viewModel.groupId}", null, { response ->
            VolleyLog.d(TAG, "응답$response")
            response?.let {
                parseJson(it)
                hideProgressBar()
            }
        }) { error ->
            VolleyLog.d(TAG, "응답" + error.message)
            hideProgressBar()
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    private fun parseJson(jsonObject: JSONObject) {
        jsonObject.getJSONArray("users").also { jsonArray ->
            for (i in 0 until jsonArray.length()) {
                viewModel.memberItems.add(MemberItem().apply {
                    with(jsonArray.getJSONObject(i)) {
                        id = getInt("id")
                        name = getString("name")
                        profileImage = getString("profile_img")
                        timeStamp = getString("created_at")
                    }
                })
                binding.recyclerView.adapter?.notifyItemInserted(viewModel.memberItems.size - 1)
            }
        }
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private const val SPAN_COUNT = 4
        private const val ARG_PARAM1 = "group_id"
        private val TAG = MemberFragment::class.java.simpleName

        fun newInstance(groupId: Int) =
            MemberFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                }
            }
    }
}