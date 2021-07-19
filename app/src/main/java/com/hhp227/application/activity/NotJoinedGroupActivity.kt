package com.hhp227.application.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.R
import com.hhp227.application.adapter.GroupListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityGroupFindBinding
import com.hhp227.application.dto.EmptyItem
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.fragment.GroupInfoFragment
import com.hhp227.application.fragment.GroupInfoFragment.Companion.TYPE_WITHDRAWAL

class NotJoinedGroupActivity : AppCompatActivity() {
    private val groupList: MutableList<Any> = mutableListOf()

    private lateinit var binding: ActivityGroupFindBinding

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
                        val groupItem = currentList[position] as GroupItem

                        GroupInfoFragment.newInstance().run {
                            arguments = Bundle().apply {
                                putInt("request_type", TYPE_WITHDRAWAL)
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
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, "${URLs.URL_USER_GROUP}?status=1", null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                response.getJSONArray("groups").let { groups ->
                    for (i in 0 until groups.length()) {
                        groupList += GroupItem().apply {
                            with(groups.getJSONObject(i)) {
                                id = getInt("id")
                                authorId = getInt("author_id")
                                groupName = getString("group_name")
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
            if (groupList.isEmpty())
                groupList.add(EmptyItem(-1, getString(R.string.no_request_join)))
            binding.recyclerView.adapter?.notifyItemChanged(0)
            VolleyLog.e(TAG, error.message)
        }) {
            override fun getHeaders() = mapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    fun refresh() {
        Toast.makeText(applicationContext, "새로 고침", Toast.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = NotJoinedGroupActivity::class.simpleName
    }
}
