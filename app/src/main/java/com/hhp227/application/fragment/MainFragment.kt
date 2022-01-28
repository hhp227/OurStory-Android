package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.R
import com.hhp227.application.activity.MainActivity
import com.hhp227.application.activity.MainActivity.Companion.PROFILE_UPDATE_CODE
import com.hhp227.application.activity.PostDetailActivity
import com.hhp227.application.activity.WriteActivity
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentMainBinding
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.fragment.GroupFragment.Companion.UPDATE_CODE
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.MainViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import kotlin.properties.Delegates

class MainFragment : Fragment() {
    private val viewModel: MainViewModel by viewModels()

    private var hasRequestedMore by Delegates.notNull<Boolean>()

    private var offset = 0

    private var binding: FragmentMainBinding by autoCleared()

    private var scrollListener: RecyclerView.OnScrollListener by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 처음 캐시메모리 요청을 체크
        val entry = AppController.getInstance().requestQueue.cache[URLs.URL_POSTS]
        scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager
                if (!hasRequestedMore && dy > 0 && lm.findLastCompletelyVisibleItemPosition() >= lm.itemCount - 1) {
                    hasRequestedMore = true
                    offset = viewModel.itemList.size // footerloader가 추가되면 -1 해야 됨

                    fetchDataTask()
                }
            }
        }

        binding.recyclerView.apply {
            itemAnimator = null
            adapter = PostListAdapter().apply {
                submitList(viewModel.itemList)
                setOnItemClickListener { v, p ->
                    (currentList[p] as PostItem.Post).also { postItem ->
                        val postId = postItem.id
                        val userId = postItem.userId
                        val name = postItem.name
                        val timeStamp = postItem.timeStamp
                        val intent = Intent(context, PostDetailActivity::class.java)
                            .putExtra("post_id", postId)
                            .putExtra("user_id", userId)
                            .putExtra("name", name)
                            .putExtra("timestamp", timeStamp)
                            .putExtra("position", p)
                            .putExtra("is_bottom", v.id == R.id.ll_reply)

                        startActivityForResult(intent, Tab1Fragment.POST_INFO_CODE)
                    }
                }
            }

            addOnScrollListener(scrollListener)
        }
        (requireActivity() as? AppCompatActivity)?.run {
            title = getString(R.string.main_fragment)

            setSupportActionBar(binding.toolbar)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            }, 1000)
        }
        binding.fab.setOnClickListener {
            Intent(context, WriteActivity::class.java).also {
                it.putExtra("type", WriteActivity.TYPE_INSERT)
                startActivityForResult(it, UPDATE_CODE)
            }
        }
        setDrawerToggle()
        showProgressBar()
        entry?.let {
            try {
                val data = String(it.data, Charsets.UTF_8)

                try {
                    parseJson(JSONObject(data))
                    hideProgressBar()
                } catch (e: JSONException) {
                    Log.e(TAG, "에러$e")
                }
            } catch (e: UnsupportedEncodingException) {
                Log.e(TAG, "에러$e")
            }
        } ?: fetchDataTask()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Tab1Fragment.POST_INFO_CODE && resultCode == Tab1Fragment.POST_INFO_CODE) {
            with(data!!) {
                val position = getIntExtra("position", 0)
                viewModel.itemList[position] = (viewModel.itemList[position] as PostItem.Post).apply {
                    text = getStringExtra("text")
                    imageItemList = getParcelableArrayListExtra("images")!!
                    replyCount = getIntExtra("reply_count", 0)
                }

                binding.recyclerView.adapter!!.notifyItemChanged(position)
            }
        } else if ((requestCode == UPDATE_CODE || requestCode == Tab1Fragment.POST_INFO_CODE) && resultCode == RESULT_OK) {
            offset = 0

            binding.appBarLayout.setExpanded(true, false)
            viewModel.itemList.clear()
            fetchDataTask()
        } else if (requestCode == PROFILE_UPDATE_CODE && resultCode == RESULT_OK) {
            (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
                adapter.currentList
                    .mapIndexed { index, any -> index to any }
                    .filter { (_, a) -> a is PostItem.Post && a.userId == AppController.getInstance().preferenceManager.user.id }
                    .forEach { (i, _) ->
                        (viewModel.itemList[i] as PostItem.Post).apply { profileImage = AppController.getInstance().preferenceManager.user.profileImage }
                        adapter.notifyItemChanged(i)
                    }
            }
        }
    }

    private fun setDrawerToggle() {
        val activityMainBinding = (requireActivity() as MainActivity).binding

        ActionBarDrawerToggle(requireActivity(), activityMainBinding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
            activityMainBinding.drawerLayout.addDrawerListener(it)
            it.syncState()
        }
    }

    private fun fetchDataTask() {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POSTS.replace("{OFFSET}", offset.toString()), null, Response.Listener { response ->
            response?.let {
                parseJson(it)
                hideProgressBar()
            }
        }, Response.ErrorListener { error ->
            VolleyLog.e(TAG, error.message)
            hideProgressBar()
        }) {
            override fun getHeaders() = mapOf(
                "Content-Type" to "application/json",
                "api_key" to "xxxxxxxxxxxxxxx"
            )
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    private fun parseJson(jsonObject: JSONObject) {
        jsonObject.getJSONArray("posts").also { jsonArr ->
            hasRequestedMore = false

            for (i in 0 until jsonArr.length()) {
                viewModel.itemList.add(/*mItemList.size - 1, */PostItem.Post().apply {
                    with(jsonArr.getJSONObject(i)) {
                        id = getInt("id")
                        userId = getInt("user_id")
                        name = getString("name")
                        text = getString("text")
                        profileImage = getString("profile_img")
                        timeStamp = getString("created_at")
                        replyCount = getInt("reply_count")
                        likeCount = getInt("like_count")
                        imageItemList = getJSONObject("attachment").getJSONArray("images").let { images ->
                            ArrayList<ImageItem>().also { imageList ->
                                for (j in 0 until images.length()) {
                                    imageList += ImageItem().apply {
                                        with(images.getJSONObject(j)) {
                                            id = getInt("id")
                                            image = getString("image")
                                            tag = getString("tag")
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
                binding.recyclerView.adapter?.notifyItemInserted(viewModel.itemList.size - 1)
            }
        }
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        private val TAG = MainFragment::class.simpleName

        fun newInstance(): Fragment = MainFragment()
    }
}
