package com.hhp227.application.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.adapter.GroupListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityGroupFindBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.fragment.GroupInfoFragment
import com.hhp227.application.fragment.GroupInfoFragment.Companion.TYPE_REQUEST

class GroupFindActivity : AppCompatActivity() {
    private val groupList: MutableList<GroupItem> by lazy { mutableListOf<GroupItem>() }

    private lateinit var binding: ActivityGroupFindBinding

    private var offSet = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupFindBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GroupListAdapter().apply {
                submitList(groupList)
                setOnItemClickListener { _, position ->
                    if (position != RecyclerView.NO_POSITION) {
                        val groupItem = currentList[position]

                        GroupInfoFragment.newInstance().run {
                            arguments = Bundle().apply {
                                putInt("request_type", TYPE_REQUEST)
                                putInt("join_type", groupItem.joinType)
                                putInt("group_id", groupItem.id)
                                putString("group_name", groupItem.groupName)
                            }

                            show(supportFragmentManager, "dialog")
                        }
                    }
                }
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            }, 1000)
        }
        fetchGroupList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun fetchGroupList() {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_GROUPS.replace("{OFFSET}", offSet.toString()), null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                response.getJSONArray("groups").let { groups ->
                    for (i in 0 until groups.length()) {
                        groupList += GroupItem().apply {
                            with(groups.getJSONObject(i)) {
                                id = getInt("id")
                                authorId = getInt("author_id")
                                groupName = getString("name")
                                image = getString("image")
                                description = getString("description")
                                createdAt = getString("created_at")
                                joinType = getInt("join_type")
                            }
                        }
                        binding.recyclerView.adapter?.notifyItemChanged(groupList.size - 1)
                    }
                }
            }
        }, Response.ErrorListener { error ->
            error.message?.let { Log.e(GroupFindActivity::class.java.simpleName, it) }
        }) {
            override fun getHeaders() = mapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }
}
