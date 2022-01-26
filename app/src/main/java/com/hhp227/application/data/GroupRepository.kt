package com.hhp227.application.data

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.android.material.snackbar.Snackbar
import com.hhp227.application.R
import com.hhp227.application.activity.CreateGroupActivity
import com.hhp227.application.activity.FindGroupActivity
import com.hhp227.application.activity.GroupActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.fragment.GroupInfoFragment
import com.hhp227.application.util.Resource
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GroupRepository {
    fun getMyGroupList(apiKey: String) = callbackFlow<Resource<List<GroupItem>>> {
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

    fun getNotJoinedGroupList(apiKey: String, offset: Int) = callbackFlow<Resource<List<GroupItem>>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_GROUPS.replace("{OFFSET}", offset.toString()), null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                response.getJSONArray("groups").let { groups ->
                    val groupList = mutableListOf<GroupItem>()

                    for (i in 0 until groups.length()) {
                        with(groups.getJSONObject(i)) {
                            groupList += GroupItem.Group(
                                id = getInt("id"),
                                authorId = getInt("author_id"),
                                groupName = getString("name"),
                                image = getString("image"),
                                description = getString("description"),
                                createdAt = getString("created_at"),
                                joinType = getInt("join_type"),
                            )
                        }
                    }
                    trySendBlocking(Resource.Success(groupList))
                }
            }
        }, Response.ErrorListener { error ->
            //GroupItem.Empty(-1, getString(R.string.no_group))
            trySendBlocking(Resource.Error(error.message.toString(), listOf(GroupItem.Empty(-1, "그룹이 없습니다."))))
            error.message?.let { Log.e(FindGroupActivity::class.java.simpleName, it) }
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun getJoinRequestGroupList(apiKey: String) = callbackFlow<Resource<List<GroupItem>>> {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, "${URLs.URL_USER_GROUP}?status=1", null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                val groupList = mutableListOf<GroupItem>()

                response.getJSONArray("groups").let { groups ->
                    for (i in 0 until groups.length()) {
                        with(groups.getJSONObject(i)) {
                            groupList += GroupItem.Group(
                                id = getInt("id"),
                                authorId = getInt("author_id"),
                                groupName = getString("group_name"),
                                image = getString("image"),
                                description = getString("description"),
                                createdAt = getString("created_at"),
                                joinType = getInt("join_type")
                            )
                        }
                    }
                }
                trySendBlocking(Resource.Success(groupList))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString(), listOf(GroupItem.Empty(-1, "가입 신청한 그룹이 없습니다.")))) // getString(R.string.no_request_join)
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    fun requestToJoinOrCancel(apiKey: String, requestType: Int, joinType: Int, groupId: Int) = callbackFlow<Resource<Boolean>> {
        val stringRequest = object : StringRequest(if (requestType == GroupInfoFragment.TYPE_REQUEST) Method.POST else Method.DELETE, if (requestType == GroupInfoFragment.TYPE_REQUEST) URLs.URL_GROUP_JOIN_REQUEST else "${URLs.URL_LEAVE_GROUP}/${groupId}", Response.Listener { response ->
            if (!JSONObject(response).getBoolean("error")) {
                trySendBlocking(Resource.Success(true))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = hashMapOf("Authorization" to apiKey)

            override fun getParams() = hashMapOf(
                "group_id" to groupId.toString(),
                "status" to joinType.toString() // join type이 0이면 0 1이면 1
            )
        }

        AppController.getInstance().addToRequestQueue(stringRequest)
        awaitClose { close() }
    }

    fun addGroupImage(apiKey: String, bitMap: Bitmap) = callbackFlow<Resource<String>> {
        val multipartRequest = object : MultipartRequest(Method.POST, URLs.URL_GROUP_IMAGE, Response.Listener { response ->
            val jsonObject = JSONObject(String(response.data))
            val image = jsonObject.getString("image")

            if (!jsonObject.getBoolean("error"))
                trySendBlocking(Resource.Success(image))
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getByteData() = mapOf(
                "image" to DataPart("${System.currentTimeMillis()}.jpg", ByteArrayOutputStream().also {
                    bitMap.compress(Bitmap.CompressFormat.PNG, 80, it)
                }.toByteArray())
            )
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(multipartRequest)
        awaitClose { close() }
    }

    fun addGroup(apiKey: String, title: String, description: String, joinType: String, image: String?) = callbackFlow<Resource<GroupItem.Group>> {
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_GROUP,  Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(
                        Resource.Success(
                            GroupItem.Group(
                                id = jsonObject.getInt("group_id"),
                                groupName = jsonObject.getString("group_name")
                            )
                        )
                    )
                }
            } catch (e: JSONException) {
                trySendBlocking(Resource.Error(e.message.toString()))
            }
        }, Response.ErrorListener { error ->
            trySendBlocking(Resource.Error(error.message.toString()))
        }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)

            override fun getParams() = mapOf(
                "name" to title,
                "description" to description,
                "join_type" to joinType,
                "image" to image
            )
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest)
        awaitClose { close() }
    }
}