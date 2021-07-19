import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.hhp227.application.R
import com.hhp227.application.activity.MainActivity.Companion.PROFILE_UPDATE_CODE
import com.hhp227.application.activity.PostDetailActivity
import com.hhp227.application.adapter.PostListAdapter
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.EmptyItem
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.fragment.GroupFragment.Companion.UPDATE_CODE
import com.hhp227.application.util.autoCleared
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class Tab1Fragment : Fragment() {
    private var binding: FragmentTabBinding by autoCleared()

    private val postItems by lazy { arrayListOf(Any()) }

    private var offset = 0

    private var groupId: Int = 0

    private var groupName: String? = null

    private var hasRequestedMore = false // 데이터 불러올때 중복안되게 하기위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getInt(ARG_PARAM1)
            groupName = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 처음 캐시메모리 요청을 체크
        val cache = AppController.getInstance().requestQueue.cache
        val entry = cache[URLs.URL_POSTS]

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PostListAdapter().apply {
                setLoaderVisibility(View.INVISIBLE)
                submitList(postItems)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!hasRequestedMore && dy > 0 && layoutManager != null && (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() >= layoutManager!!.itemCount - 1) {
                            hasRequestedMore = true
                            offset = postItems.size - 1

                            setLoaderVisibility(View.VISIBLE)
                            notifyItemChanged(postItems.size - 1)
                            fetchPostList()
                        }
                    }
                })
                setOnItemClickListener { v, p ->
                    (currentList[p] as PostItem).also { (postId, userId, name, _, _, _, _, timeStamp) ->
                        startActivityForResult(Intent(requireContext(), PostDetailActivity::class.java).let { intent ->
                            intent.putExtra("post_id", postId)
                            intent.putExtra("user_id", userId)
                            intent.putExtra("name", name)
                            intent.putExtra("timestamp", timeStamp)
                            intent.putExtra("position", p)
                            intent.putExtra("is_bottom", v.id == R.id.ll_reply)
                            intent.putExtra("group_id", groupId)
                            intent.putExtra("group_name", groupName)
                        }, POST_INFO_CODE)
                    }
                }
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                delay(1000)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        showProgressBar()

        /**
         * 여기서부터 주석을 지우면 캐시메모리에서 저장된 json을 불러온다.
         * 즉 새로고침 한번만 함
         */
        entry?.let {
            // 캐시메모리에서 데이터 인출
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
        } ?: fetchPostList()
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == POST_INFO_CODE && resultCode == POST_INFO_CODE) { // 피드 수정이 일어나면 클라이언트측에서 피드아이템을 수정
            with(data!!) {
                val position = getIntExtra("position", 0)
                postItems[position] = (postItems[position] as PostItem).apply {
                    text = getStringExtra("text")
                    imageItemList = getParcelableArrayListExtra("images")!!
                    replyCount = getIntExtra("reply_count", 0)
                }

                binding.recyclerView.adapter!!.notifyItemChanged(position)
            }
        } else if ((requestCode == UPDATE_CODE || requestCode == POST_INFO_CODE) && resultCode == RESULT_OK) {
            offset = 0

            postItems.clear()
            postItems.add(Any())
            fetchPostList()
        } else if (requestCode == PROFILE_UPDATE_CODE && resultCode == RESULT_OK) {
            (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
                adapter.currentList
                    .mapIndexed { index, any -> index to any }
                    .filter { (_, a) -> a is PostItem && a.userId == AppController.getInstance().preferenceManager.user.id }
                    .forEach { (i, _) ->
                        (postItems[i] as PostItem).apply { profileImage = AppController.getInstance().preferenceManager.user.profileImage }
                        adapter.notifyItemChanged(i)
                    }
            }
        }
    }

    @Throws(JSONException::class)
    private fun parseJson(jsonObject: JSONObject) {
        jsonObject.getJSONArray("posts").also { jsonArr ->
            hasRequestedMore = false

            for (i in 0 until jsonArr.length()) {
                postItems.add(postItems.size - 1, PostItem().apply {
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
                            List(images.length()) { j ->
                                ImageItem().apply {
                                    with(images.getJSONObject(j)) {
                                        id = getInt("id")
                                        image = getString("image")
                                        tag = getString("tag")
                                    }
                                }
                            }
                        }
                    }
                })
                binding.recyclerView.adapter?.notifyItemInserted(postItems.size - 1)
            }
        }
        (binding.recyclerView.adapter as PostListAdapter).setLoaderVisibility(View.INVISIBLE)
    }

    private fun fetchPostList() {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET, "${URLs.URL_POSTS.replace("{OFFSET}", offset.toString())}&group_id=$groupId", null, Response.Listener { response ->
            if (response != null) {
                parseJson(response)
                hideProgressBar()
            }
        }, Response.ErrorListener { error ->
            VolleyLog.e(TAG, "Volley에러 : " + error.message)
            (binding.recyclerView.adapter as PostListAdapter).setLoaderVisibility(View.GONE)
            binding.recyclerView.adapter?.notifyItemChanged(postItems.size - 1)
            if (postItems.size < 2) {
                postItems.add(0, EmptyItem(R.drawable.ic_baseline_library_add_72, getString(R.string.add_message)))
            }
            hideProgressBar()
        }) {
            override fun getHeaders(): Map<String, String> = mapOf(
                "Content-Type" to "application/json",
                "api_key" to "xxxxxxxxxxxxxxx"
            )
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    private fun showProgressBar() = binding.progressBar.takeIf { it.visibility == View.GONE }?.apply { visibility = View.VISIBLE }

    private fun hideProgressBar() = binding.progressBar.takeIf { it.visibility == View.VISIBLE }?.apply { visibility = View.GONE }

    companion object {
        const val POST_INFO_CODE = 100
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "group_name"
        private val TAG = Tab1Fragment::class.java.simpleName

        fun newInstance(groupId: Int, groupName: String) =
            Tab1Fragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, groupId)
                    putString(ARG_PARAM2, groupName)
                }
            }
    }
}