package com.hhp227.application.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.R
import com.hhp227.application.adapter.MessagesListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.Config
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.ActivityChatBinding
import com.hhp227.application.dto.Message
import com.hhp227.application.dto.User
import com.hhp227.application.fcm.NotificationUtils
import org.json.JSONException
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {
    private val listMessages by lazy { arrayListOf<Message>() }

    private val registrationBroadcastReceiver: BroadcastReceiver by lazy { RegistrationBroadcastReceiver() }

    private val textWatcher: TextWatcher by lazy {
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvSend.setBackgroundResource(if (s!!.isNotEmpty()) R.drawable.background_sendbtn_p else R.drawable.background_sendbtn_n)
                binding.tvSend.setTextColor(resources.getColor(if (s.isNotEmpty()) android.R.color.white else android.R.color.darker_gray))
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }
    }

    private lateinit var chatRoomId: String

    private lateinit var apikey: String

    private lateinit var binding: ActivityChatBinding

    private var offSet = 0

    private var previousMessageCnt = 0

    private var hasRequestedMore = false // 데이터 불러올때 중복안되게 하기위한 변수

    var currentScrollState = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (myUserId, _, _, apiKey) = AppController.getInstance().preferenceManager.user
        apikey = apiKey!!
        chatRoomId = "1" // 현재는 메인 채팅방이 1로 지정
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.listViewMessages.apply {
            adapter = MessagesListAdapter(context, listMessages, myUserId)

            setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                    currentScrollState = scrollState
                }

                override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                    if (!hasRequestedMore && firstVisibleItem == 0 && currentScrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        if (totalItemCount == previousMessageCnt) return
                        offSet = totalItemCount
                        hasRequestedMore = true

                        fetchChatThread()
                    }
                }
            })
        }
        binding.etInputMsg.addTextChangedListener(textWatcher)
        binding.tvSend.setOnClickListener {
            if (binding.etInputMsg.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                sendMessage()

                // 전송하면 텍스트창 리셋
                binding.etInputMsg.setText("")
            } else {
                Toast.makeText(applicationContext, "입력하고 전송하세요", Toast.LENGTH_LONG).show()
            }
        }
        fetchChatThread()
    }

    override fun onResume() {
        super.onResume()

        // registering the receiver for new notification
        LocalBroadcastManager.getInstance(this).registerReceiver(registrationBroadcastReceiver, IntentFilter(Config.PUSH_NOTIFICATION))
        NotificationUtils.clearNotifications()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationBroadcastReceiver)
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun fetchChatThread() {
        val endPoint = URLs.URL_CHAT_THREAD.replace("{CHATROOM_ID}", chatRoomId)
        val strReq = StringRequest(Request.Method.GET, endPoint + offSet, { response ->
            Log.e(TAG, "response: $response")
            try {
                val obj = JSONObject(response)

                // check for error
                if (!obj.getBoolean("error")) {
                    val commentsObj = obj.getJSONArray("messages")

                    for (i in 0 until commentsObj.length()) {
                        val commentObj = commentsObj[i] as JSONObject
                        val commentId = commentObj.getInt("message_id")
                        val commentText = commentObj.getString("message")
                        val createdAt = commentObj.getString("created_at")
                        val userObj = commentObj.getJSONObject("user")
                        val userId = userObj.getInt("user_id")
                        val userName = userObj.getString("username")
                        val profileImage = userObj.getString("profile_img")
                        val message = Message(
                            commentId,
                            commentText,
                            createdAt,
                            User(userId, userName, null, null, profileImage, null)
                        )

                        listMessages.add(0, message)
                    }
                    (binding.listViewMessages.adapter as MessagesListAdapter).notifyDataSetChanged()
                    onLoadMoreItems(commentsObj.length())
                } else {
                    Toast.makeText(applicationContext, "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                Log.e(TAG, "json parsing error: " + e.message)
                Toast.makeText(applicationContext, "json parse error: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }) { error ->
            val networkResponse = error.networkResponse

            Log.e(TAG, "Volley error: " + error.message + ", code: " + networkResponse)
            Toast.makeText(applicationContext, "Volley error: " + error.message, Toast.LENGTH_SHORT).show()
        }

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq)
    }

    private fun handlePushNotification(intent: Intent) {
        val message = intent.getSerializableExtra("message") as Message?
        val chatRoomId = intent.getStringExtra("chat_room_id")

        if (message != null && chatRoomId != null) {
            listMessages.add(message)

            binding.listViewMessages.apply {
                (adapter as MessagesListAdapter).notifyDataSetChanged()
                setSelection(count)
            }
        }
    }

    private fun sendMessage() {
        val textMessage = binding.etInputMsg.text.toString().trim { it <= ' ' }
        val endPoint = URLs.URL_CHAT_SEND.replace("{CHATROOM_ID}", chatRoomId)
        val stringRequest: StringRequest = object : StringRequest(Method.POST, endPoint, Response.Listener { response ->
            Log.e(TAG, "response: $response")
            try {
                val obj = JSONObject(response)

                if (!obj.getBoolean("error")) {
                    val commentObj = obj.getJSONObject("message")
                    val commentId = commentObj.getInt("message_id")
                    val commentText = commentObj.getString("message")
                    val createdAt = commentObj.getString("created_at")
                    val userObj = obj.getJSONObject("user")
                    val userId = userObj.getInt("user_id")
                    val userName = userObj.getString("name")
                    val message = Message(
                        commentId,
                        commentText,
                        createdAt,
                        User(userId, userName, null, null, null, null)
                    )

                    listMessages.add(message)
                    binding.listViewMessages.apply {
                        (adapter as MessagesListAdapter).notifyDataSetChanged()
                        setSelection(count)
                    }
                } else {
                    Toast.makeText(applicationContext, "에러 : " + obj.getString("message"), Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                Log.e(TAG, "json parsing error: " + e.message)
                Toast.makeText(applicationContext, "json parse error: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }, Response.ErrorListener { error ->
            val networkResponse = error.networkResponse
            Log.e(TAG, "Volley error: " + error.message + ", code: " + networkResponse)
            Toast.makeText(applicationContext, "Volley error: " + error.message, Toast.LENGTH_SHORT).show()
            binding.etInputMsg.setText(textMessage)
        }) {
            override fun getHeaders(): Map<String, String> = mapOf("Authorization" to apikey)

            override fun getParams(): Map<String, String> = mapOf("message" to textMessage)
        }

        // disabling retry policy so that it won't make
        // multiple http calls
        val socketTimeout = 0
        stringRequest.retryPolicy = DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest)
    }

    private fun onLoadMoreItems(addCount: Int) {
        if (hasRequestedMore) {
            val firstVisPos: Int = binding.listViewMessages.firstVisiblePosition
            val firstVisView: View = binding.listViewMessages.getChildAt(0)
            val top = firstVisView.top

            binding.listViewMessages.setSelectionFromTop(firstVisPos + addCount, top)
        } else {
            binding.listViewMessages.setSelection(binding.listViewMessages.count)
        }
        previousMessageCnt = listMessages.size - addCount
        hasRequestedMore = false
    }

    inner class RegistrationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Config.PUSH_NOTIFICATION) {
                // new push message is received
                handlePushNotification(intent)
            }
        }
    }

    companion object {
        private val TAG = ChatActivity::class.simpleName
    }
}