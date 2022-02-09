import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.hhp227.application.data.PostRepository
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.ImageItem
import com.hhp227.application.dto.PostItem
import com.hhp227.application.fragment.TabHostLayoutFragment
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.Tab1ViewModel
import com.hhp227.application.viewmodel.Tab1ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class Tab1Fragment : Fragment() {
    private val viewModel: Tab1ViewModel by viewModels {
        Tab1ViewModelFactory(PostRepository(), this, arguments)
    }

    private val postDetailActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == POST_INFO_CODE) {
            //result.data?.let(viewModel::updatePost)
            val position = result.data?.getIntExtra("position", 0) ?: 0
            viewModel.postItems[position] = result.data?.getParcelableExtra("post") ?: PostItem.Post()

            binding.recyclerView.adapter!!.notifyItemChanged(position)
        } else if (result.resultCode == RESULT_OK) {
            // 삭제할때
            offset = 0

            viewModel.postItems.clear()
            viewModel.postItems.add(PostItem.Loader)
            fetchPostList()
            (parentFragment as? TabHostLayoutFragment)?.binding?.appBarLayout?.setExpanded(true)
        }
    }

    private var binding: FragmentTabBinding by autoCleared()

    private var offset = 0

    private var hasRequestedMore = false // 데이터 불러올때 중복안되게 하기위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.groupId = it.getInt(ARG_PARAM1)
            viewModel.groupName = it.getString(ARG_PARAM2)
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
                submitList(viewModel.postItems)
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (!hasRequestedMore && dy > 0 && layoutManager != null && (layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() >= layoutManager!!.itemCount - 1) {
                            hasRequestedMore = true
                            offset = viewModel.postItems.size - 1

                            setLoaderVisibility(View.VISIBLE)
                            notifyItemChanged(viewModel.postItems.size - 1)
                            fetchPostList()
                        }
                    }
                })
                setOnItemClickListener { v, p ->
                    (currentList[p] as PostItem.Post).also { post ->
                        val intent = Intent(requireContext(), PostDetailActivity::class.java)
                            .putExtra("post", post)
                            .putExtra("position", p)
                            .putExtra("is_bottom", v.id == R.id.ll_reply)
                            .putExtra("group_name", viewModel.groupName)

                        postDetailActivityResultLauncher.launch(intent)
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
        if (requestCode == POST_INFO_CODE && resultCode == RESULT_OK) {
            // 추가할때
            offset = 0

            viewModel.postItems.clear()
            viewModel.postItems.add(PostItem.Loader)
            fetchPostList()
            (parentFragment as? TabHostLayoutFragment)?.binding?.appBarLayout?.setExpanded(true)
        } else if (requestCode == PROFILE_UPDATE_CODE && resultCode == RESULT_OK) {
            (binding.recyclerView.adapter as PostListAdapter).also { adapter ->
                adapter.currentList
                    .mapIndexed { index, any -> index to any }
                    .filter { (_, a) -> a is PostItem.Post && a.userId == AppController.getInstance().preferenceManager.user.id }
                    .forEach { (i, _) ->
                        (viewModel.postItems[i] as PostItem.Post).apply { profileImage = AppController.getInstance().preferenceManager.user.profileImage }
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
                viewModel.postItems.add(viewModel.postItems.size - 1, PostItem.Post().apply {
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
                binding.recyclerView.adapter?.notifyItemInserted(viewModel.postItems.size - 1)
            }
        }
        (binding.recyclerView.adapter as PostListAdapter).setLoaderVisibility(View.INVISIBLE)
    }

    private fun fetchPostList() {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_POSTS.replace("{OFFSET}", offset.toString()).replace("{GROUP_ID}", viewModel.groupId.toString()), null, Response.Listener { response ->
            if (response != null) {
                parseJson(response)
                hideProgressBar()
            }
        }, Response.ErrorListener { error ->
            VolleyLog.e(TAG, "Volley에러 : " + error.message)
            (binding.recyclerView.adapter as PostListAdapter).setLoaderVisibility(View.GONE)
            binding.recyclerView.adapter?.notifyItemChanged(viewModel.postItems.size - 1)
            if (viewModel.postItems.size < 2) {
                viewModel.postItems.add(0, PostItem.Empty(R.drawable.ic_baseline_library_add_72, getString(R.string.add_message)))
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