package com.hhp227.application.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.R
import com.hhp227.application.activity.*
import com.hhp227.application.databinding.*
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.GroupViewModel

class GroupFragment : Fragment() {
    private val viewModel: GroupViewModel by viewModels()

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.itemList.clear()
            binding.rvGroup.adapter?.notifyDataSetChanged()
            fetchDataTask()
        }
    }

    private var binding: FragmentGroupBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.spanCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }

        (requireActivity() as? AppCompatActivity)?.run {
            title = getString(R.string.group_fragment)

            setSupportActionBar(binding.toolbar)
        }
        binding.bnvGroupButton.apply {
            menu.getItem(0).isCheckable = false

            setOnItemSelectedListener {
                it.isCheckable = false

                when (it.itemId) {
                    R.id.navigationFind -> {
                        activityResultLauncher.launch(Intent(context, FindGroupActivity::class.java))
                        true
                    }
                    R.id.navigationRequest -> {
                        startActivity(Intent(context, NotJoinedGroupActivity::class.java))
                        true
                    }
                    R.id.navigationCreate -> {
                        activityResultLauncher.launch(Intent(context, CreateGroupActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }
        binding.rvGroup.apply {
            layoutManager = GridLayoutManager(context, viewModel.spanCount).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int = if (binding.rvGroup.adapter!!.getItemViewType(position) == TYPE_TEXT) spanCount else 1
                }
            }
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
                    TYPE_TEXT -> HeaderHolder(ItemGridHeaderBinding.inflate(LayoutInflater.from(context), parent, false))
                    TYPE_GROUP -> ItemHolder(ItemGroupGridBinding.inflate(LayoutInflater.from(context), parent, false))
                    TYPE_AD -> AdHolder(ItemGridAdBinding.inflate(LayoutInflater.from(context), parent, false))
                    else -> throw NullPointerException()
                }
                override fun getItemCount(): Int = viewModel.itemList.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    when (holder) {
                        is HeaderHolder -> holder.bind(viewModel.itemList[position] as String)
                        is ItemHolder -> holder.bind(viewModel.itemList[position] as GroupItem)
                        is AdHolder -> holder.bind(viewModel.itemList[position] as String)
                    }
                }

                override fun getItemViewType(position: Int): Int = when {
                    viewModel.itemList[position] is String && viewModel.itemList[position] != "광고" -> TYPE_TEXT
                    viewModel.itemList[position] is GroupItem -> TYPE_GROUP
                    viewModel.itemList[position] is String && viewModel.itemList[position] == "광고" -> TYPE_AD
                    else -> 0
                }
            }

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    if (parent.adapter!!.getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_GROUP || parent.adapter!!.getItemViewType(parent.getChildAdapterPosition(view)) == TYPE_AD) {
                        outRect.apply {
                            top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
                            bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
                            left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (parent.getChildAdapterPosition(view) % viewModel.spanCount == 1) 14f else 7f, resources.displayMetrics).toInt()
                            right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (parent.getChildAdapterPosition(view) % viewModel.spanCount == 0) 14f else 7f, resources.displayMetrics).toInt()
                        }
                    }
                }
            })
        }
        binding.srlGroup.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.srlGroup.isRefreshing = false

                viewModel.itemList.clear()
                binding.rvGroup.adapter?.notifyDataSetChanged()
                fetchDataTask()
            }, 1000)
        }
        setDrawerToggle()
        fetchDataTask()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewModel.spanCount = when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }
        (binding.rvGroup.layoutManager as GridLayoutManager).spanCount = viewModel.spanCount

        binding.rvGroup.invalidateItemDecorations()
    }

    private fun setDrawerToggle() {
        val activityMainBinding = (requireActivity() as MainActivity).binding

        ActionBarDrawerToggle(requireActivity(), activityMainBinding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
            activityMainBinding.drawerLayout.addDrawerListener(it)
            it.syncState()
        }
    }

    private fun fetchDataTask() {
        val jsonObjectRequest = object : JsonObjectRequest(Method.GET, URLs.URL_USER_GROUP, null, Response.Listener { response ->
            if (!response.getBoolean("error")) {
                val jsonArray = response.getJSONArray("groups")

                for (i in 0 until jsonArray.length()) {
                    with(jsonArray.getJSONObject(i)) {
                        val groupItem = GroupItem(getInt("id"), getInt("author_id"), getString("group_name"), getString("author_name"), getString("image"), getString("description"), getString("created_at"), getInt("join_type"))

                        viewModel.itemList.add(groupItem)
                    }
                    binding.rvGroup.adapter?.notifyItemChanged(viewModel.itemList.size - 1)
                }
                setOtherItems()
            }

        }, Response.ErrorListener { error ->
            VolleyLog.e(TAG, error.message)
        }) {
            override fun getHeaders() = mapOf("Authorization" to viewModel.apiKey)
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    fun setOtherItems() {
        if (viewModel.itemList.isNotEmpty()) {
            viewModel.itemList.add(0, getString(R.string.joined_group))
            binding.rvGroup.adapter!!.notifyItemChanged(0)
            if (viewModel.itemList.size % 2 == 0) {
                viewModel.itemList.add("광고")
                binding.rvGroup.adapter!!.notifyItemInserted(viewModel.itemList.size - 1)
            }
        }
    }

    inner class HeaderHolder(val binding: ItemGridHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvTitle.text = title
        }
    }

    inner class ItemHolder(val binding: ItemGroupGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(groupItem: GroupItem) = with(binding) {
            tvTitle.text = groupItem.groupName

            Glide.with(root.context)
                .load(URLs.URL_GROUP_IMAGE_PATH + groupItem.image)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                .into(ivGroupImage)
            rlGroup.setOnClickListener {
                Intent(context, GroupActivity::class.java)
                    .putExtra("group_id", groupItem.id)
                    .putExtra("author_id", groupItem.authorId)
                    .putExtra("group_name", groupItem.groupName)
                    .also(activityResultLauncher::launch)
            }
        }
    }

    inner class AdHolder(val binding: ItemGridAdBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) = with(binding) {
            tvTitle.text = title
        }
    }

    companion object {
        const val CREATE_CODE = 10
        const val REGISTER_CODE = 20
        const val UPDATE_CODE = 30
        private const val PORTRAIT_SPAN_COUNT = 2
        private const val LANDSCAPE_SPAN_COUNT = 4
        private const val TYPE_TEXT = 0
        private const val TYPE_GROUP = 1
        private const val TYPE_AD = 2
        private val TAG = GroupFragment::class.java.simpleName

        fun newInstance(): Fragment = GroupFragment()
    }
}