package com.hhp227.application.data

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.R
import com.hhp227.application.api.GroupService
import com.hhp227.application.app.AppController
import com.hhp227.application.util.URLs
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.Resource
import com.hhp227.application.fragment.FindGroupFragment
import com.hhp227.application.volley.util.MultipartRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GroupRepository(private val groupService: GroupService) {

    fun getMyGroupList(apiKey: String): LiveData<PagingData<GroupItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 5),
            pagingSourceFactory = { GroupGridPagingSource(groupService, apiKey) }
        ).liveData
    }

    fun getNotJoinedGroupList(apiKey: String): LiveData<PagingData<GroupItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 5),
            pagingSourceFactory = { GroupListPagingSource(groupService, apiKey, 0) }
        ).liveData
    }

    fun getJoinRequestGroupList(apiKey: String): LiveData<PagingData<GroupItem>> {
        return Pager(
            config = PagingConfig(enablePlaceholders = false, pageSize = 5),
            pagingSourceFactory = { GroupListPagingSource(groupService, apiKey, 1) }
        ).liveData
    }

    // TODO
    fun requestToJoinOrCancel(apiKey: String, isSignUp: Boolean, joinType: Int, groupId: Int) = callbackFlow<Resource<Boolean>> {
        val stringRequest = object : StringRequest(if (isSignUp) Method.POST else Method.DELETE, if (isSignUp) URLs.URL_GROUP_JOIN_REQUEST else "${URLs.URL_LEAVE_GROUP}/${groupId}", Response.Listener { response ->
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

    // TODO
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

    // TODO
    fun addGroup(apiKey: String, title: String, description: String, joinType: String, image: String?) = callbackFlow<Resource<GroupItem.Group>> {
        val stringRequest = object : StringRequest(Method.POST, URLs.URL_GROUP,  Response.Listener { response ->
            try {
                val jsonObject = JSONObject(response)

                if (!jsonObject.getBoolean("error")) {
                    trySendBlocking(
                        Resource.Success(
                            GroupItem.Group(
                                id = jsonObject.getJSONObject("result").getInt("id"),
                                authorId = jsonObject.getJSONObject("result").getInt("author_id"),
                                groupName = jsonObject.getJSONObject("result").getString("name"),
                                image = jsonObject.getJSONObject("result").getString("image"),
                                description = jsonObject.getJSONObject("result").getString("description"),
                                joinType = jsonObject.getJSONObject("result").getInt("join_type")
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

            override fun getParams(): Map<String, String> {
                val map = mutableMapOf<String, String>()
                map["name"] = title
                map["description"] = description
                map["join_type"] = joinType

                if (image != null) {
                    map["image"] = image
                }
                return map
            }
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(stringRequest)
        awaitClose { close() }
    }

    // TODO
    fun removeGroup(apiKey: String, groupId: Int, isAuth: Boolean) = callbackFlow<Resource<Boolean>> {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.DELETE,
            "${if (isAuth) URLs.URL_GROUP else URLs.URL_LEAVE_GROUP}/${groupId}",
            null,
            Response.Listener { response ->
                try {
                    if (!response.getBoolean("error")) {
                        trySendBlocking(Resource.Success(true))
                    }
                } catch (e: JSONException) {
                    trySendBlocking(Resource.Error(e.message.toString()))
                }
            },
            Response.ErrorListener { error ->
                trySendBlocking(Resource.Error(error.message.toString()))
            }) {
            override fun getHeaders() = mapOf("Authorization" to apiKey)
        }

        trySend(Resource.Loading())
        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
        awaitClose { close() }
    }

    companion object {
        @Volatile private var instance: GroupRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: GroupRepository(GroupService.create()).also {
                instance = it
            }
        }
    }
}