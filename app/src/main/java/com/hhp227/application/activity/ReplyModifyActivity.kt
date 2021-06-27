package com.hhp227.application.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.StringRequest
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import org.json.JSONObject
import kotlin.properties.Delegates
import com.hhp227.application.R
import com.hhp227.application.databinding.ActivityReplyModifyBinding
import com.hhp227.application.databinding.InputTextBinding

class ReplyModifyActivity : AppCompatActivity() {
    private var replyId by Delegates.notNull<Int>()

    private var position by Delegates.notNull<Int>()

    private lateinit var binding: ActivityReplyModifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplyModifyBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initialize()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<ItemHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder = ItemHolder(InputTextBinding.inflate(layoutInflater))

                override fun getItemCount(): Int = 1

                override fun onBindViewHolder(holder: ItemHolder, position: Int) {
                    holder.bind(intent.getStringExtra("text"))
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.write, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        R.id.actionSend -> {
            val text = (binding.recyclerView.getChildViewHolder(binding.recyclerView.getChildAt(0)) as ItemHolder).binding.etText.text.toString()

            if (!TextUtils.isEmpty(text)) {
                val tagStringReq = "req_send"
                val stringRequest = object : StringRequest(Method.PUT, URLs.URL_REPLY.replace("{REPLY_ID}", replyId.toString()), Response.Listener { response ->
                    val jsonObject = JSONObject(response)

                    if (!jsonObject.getBoolean("error")) {
                        setResult(Activity.RESULT_OK, { intent: Intent ->
                            intent.putExtra("reply", text)
                            intent.putExtra("position", position)
                        }(Intent(this, PostDetailActivity::class.java)))
                        finish()
                        currentFocus?.let {
                            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0)
                        }
                    }
                }, Response.ErrorListener { error ->
                    VolleyLog.e(TAG, error.message)
                }) {
                    override fun getHeaders() = mapOf("Authorization" to AppController.getInstance().preferenceManager.user.apiKey)

                    override fun getParams() = mapOf("reply" to text, "status" to "0")
                }

                AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq)
            } else
                Toast.makeText(applicationContext, "내용을 입력하세요.", Toast.LENGTH_LONG).show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initialize() {
        replyId = intent.getIntExtra("reply_id", 0)
        position = intent.getIntExtra("position", 0)
    }

    inner class ItemHolder(val binding: InputTextBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text: String?) {
            binding.etText.setText(text)
        }
    }

    companion object {
        private val TAG = ReplyModifyActivity::class.simpleName
    }
}