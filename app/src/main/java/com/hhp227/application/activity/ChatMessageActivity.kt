package com.hhp227.application.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.R
import com.hhp227.application.adapter.MessageListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.Config
import com.hhp227.application.app.URLs
import com.hhp227.application.data.ChatRepository
import com.hhp227.application.databinding.ActivityChatBinding
import com.hhp227.application.dto.MessageItem
import com.hhp227.application.dto.UserItem
import com.hhp227.application.fcm.NotificationUtils
import com.hhp227.application.viewmodel.ChatMessageViewModel
import com.hhp227.application.viewmodel.ChatMessageViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONException
import org.json.JSONObject

class ChatMessageActivity : AppCompatActivity() {
    private val viewModel: ChatMessageViewModel by viewModels {
        ChatMessageViewModelFactory(ChatRepository(), this, intent.extras)
    }

    private val registrationBroadcastReceiver: BroadcastReceiver by lazy { RegistrationBroadcastReceiver() }

    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = MessageListAdapter(AppController.getInstance().preferenceManager.user.id)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    /*if (!viewModel.state.value.hasRequestedMore && !recyclerView.canScrollVertically(-1)) {
                        if (recyclerView.adapter?.itemCount == viewModel.state.value.previousMessageCnt) return
                        viewModel.state.value.offset = recyclerView.adapter?.itemCount ?: 0
                        viewModel.state.value.hasRequestedMore = true

                        fetchChatThread()
                    }*/
                    if (!recyclerView.canScrollVertically(-1)) {
                        viewModel.fetchNextPage()
                    }
                }
            })
        }
        binding.etInputMsg.doOnTextChanged { text, _, _, _ ->
            binding.tvSend.setBackgroundResource(if (!TextUtils.isEmpty(text)) R.drawable.background_sendbtn_p else R.drawable.background_sendbtn_n)
            binding.tvSend.setTextColor(ContextCompat.getColor(applicationContext, if (!TextUtils.isEmpty(text)) android.R.color.white else android.R.color.darker_gray))
        }
        binding.tvSend.setOnClickListener {
            /*if (binding.etInputMsg.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                sendMessage()

                // 전송하면 텍스트창 리셋
                binding.etInputMsg.setText("")
            } else {
                Toast.makeText(applicationContext, "입력하고 전송하세요", Toast.LENGTH_LONG).show()
            }*/
            binding.etInputMsg.run {
                viewModel.sendMessage(text.trim().toString())
                setText("")
            }

        }
        //fetchChatThread()
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.listMessages.isNotEmpty() -> {
                    (binding.rvMessages.adapter as MessageListAdapter).submitList(state.listMessages)
                }
                state.messageId >= 0 -> {
                    binding.etInputMsg.setText("")
                }
            }
        }.launchIn(lifecycleScope)
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
        val endPoint = URLs.URL_CHAT_THREAD.replace("{CHATROOM_ID}", "${viewModel.chatRoomId}").replace("{OFFSET}", viewModel.state.value.offset.toString())
        val strReq = StringRequest(Request.Method.GET, endPoint, { response ->
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
                        val message = MessageItem(
                            commentId,
                            commentText,
                            createdAt,
                            UserItem(userId, userName, null, "", profileImage, null)
                        )

                        viewModel.state.value.listMessages.add(0, message)
                        binding.rvMessages.adapter?.notifyItemChanged(0)
                    }
                    onLoadMoreItems(if (viewModel.state.value.hasRequestedMore) commentsObj.length() else viewModel.state.value.listMessages.size - 1)
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

        AppController.getInstance().addToRequestQueue(strReq)
    }

    private fun handlePushNotification(intent: Intent) {
        val message = intent.getSerializableExtra("message") as MessageItem?
        val chatRoomId = intent.getStringExtra("chat_room_id")

        if (message != null && chatRoomId != null) {
            viewModel.state.value.listMessages.add(message)
            binding.rvMessages.apply {
                adapter?.notifyItemChanged(viewModel.state.value.listMessages.size - 1)
                scrollToPosition(viewModel.state.value.listMessages.size - 1)
            }
        }
    }

    private fun sendMessage() {
        val textMessage = binding.etInputMsg.text.toString().trim { it <= ' ' }
        val endPoint = URLs.URL_CHAT_SEND.replace("{CHATROOM_ID}", "${viewModel.chatRoomId}")
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
                    val message = MessageItem(
                        commentId,
                        commentText,
                        createdAt,
                        UserItem(userId, userName, null, "", null, null)
                    )

                    viewModel.state.value.listMessages.add(message)
                    binding.rvMessages.apply {
                        adapter?.notifyItemChanged(viewModel.state.value.listMessages.size - 1)
                        scrollToPosition(viewModel.state.value.listMessages.size - 1)
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
            override fun getHeaders() = mapOf("Authorization" to (AppController.getInstance().preferenceManager.user.apiKey ?: ""))

            override fun getParams() = mapOf("message" to textMessage)
        }

        // disabling retry policy so that it won't make
        // multiple http calls
        val socketTimeout = 0
        stringRequest.retryPolicy = DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest)
    }

    private fun onLoadMoreItems(addCount: Int) {
        viewModel.state.value.previousMessageCnt = viewModel.state.value.listMessages.size - addCount
        viewModel.state.value.hasRequestedMore = false

        (binding.rvMessages.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(addCount, 10)
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
        private val TAG = ChatMessageActivity::class.simpleName
    }
}