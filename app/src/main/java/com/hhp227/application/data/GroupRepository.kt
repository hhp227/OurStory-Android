package com.hhp227.application.data

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow

class GroupRepository {
    fun getGroupList(apiKey: String) = callbackFlow<Resource<List<GroupItem>>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_USER_GROUP, null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                val jsonArray = response.getJSONArray("groups")
                val groupItems = mutableListOf<GroupItem>()

                for (i in 0 until jsonArray.length()) {
                    with(jsonArray.getJSONObject(i)) {
                        val groupItem = GroupItem.Group(
                            id = getInt("id"),
                            authorId = getInt("author_id"),
                            groupName = getString("group_name"),
                            authorName = getString("author_name"),
                            image = getString("image"),
                            description = getString("description"),
                            createdAt = getString("created_at"),
                            joinType = getInt("join_type")
                        )

                        groupItems.add(groupItem)
                    }
                }
                setOtherItems(groupItems)
                trySendBlocking(Resource.Success(groupItems))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun setOtherItems(groupItems: MutableList<GroupItem>) {
        if (groupItems.isNotEmpty()) {
            //viewModel.itemList.add(0, getString(R.string.joined_group))
            groupItems.add(0, GroupItem.Title("가입중인 그룹"))
            if (groupItems.size % 2 == 0) {
                groupItems.add(GroupItem.Ad("광고"))
            }
        }
    }
}