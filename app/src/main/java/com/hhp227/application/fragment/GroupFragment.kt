package com.hhp227.application.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.activity.CreateGroupActivity
import com.hhp227.application.activity.GroupFindActivity
import com.hhp227.application.activity.NotJoinedGroupActivity
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.R
import com.hhp227.application.activity.GroupActivity
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.fragment_tab_host_layout.toolbar
import kotlinx.android.synthetic.main.item_group_grid.view.ivGroupImage
import kotlinx.android.synthetic.main.item_group_grid.view.rlGroup
import kotlinx.android.synthetic.main.item_group_grid.view.tvTitle
import kotlin.properties.Delegates

class GroupFragment : Fragment() {
    private val mApiKey: String? by lazy { AppController.getInstance().preferenceManager.user.apiKey }

    private val mItemList: MutableList<Any> by lazy { arrayListOf<Any>() }

    private var mSpanCount by Delegates.notNull<Int>()

    private lateinit var mActivity: AppCompatActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mActivity = activity as AppCompatActivity
        mSpanCount = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }

        mActivity.run {
            title = getString(R.string.group_fragment)

            setSupportActionBar(toolbar)
        }
        bnvGroupButton.setOnNavigationItemSelectedListener {
            it.isCheckable = false

            when (it.itemId) {
                R.id.navigationFind -> {
                    startActivityForResult(Intent(context, GroupFindActivity::class.java), REGISTER_CODE)
                    true
                }
                R.id.navigationRequest -> {
                    startActivity(Intent(context, NotJoinedGroupActivity::class.java))
                    true
                }
                R.id.navigationCreate -> {
                    startActivityForResult(Intent(context, CreateGroupActivity::class.java), CREATE_CODE)
                    true
                }
                else -> false
            }
        }
        rvGroup.apply {
            layoutManager = GridLayoutManager(context, mSpanCount).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int = if (rvGroup.adapter!!.getItemViewType(position) == TYPE_TEXT) mSpanCount else 1
                }
            }
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
                    TYPE_TEXT -> HeaderHolder(LayoutInflater.from(context).inflate(R.layout.item_grid_header, parent, false))
                    TYPE_GROUP -> ItemHolder(LayoutInflater.from(context).inflate(R.layout.item_group_grid, parent, false))
                    TYPE_AD -> AdHolder(LayoutInflater.from(context).inflate(R.layout.item_grid_ad, parent, false))
                    else -> throw NullPointerException()
                }
                override fun getItemCount(): Int = mItemList.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    when (holder) {
                        is HeaderHolder -> {
                            with(holder.containerView) {
                                tvTitle.text = mItemList[position] as String
                            }
                        }
                        is ItemHolder -> {
                            (mItemList[position] as GroupItem).let { groupItem ->
                                with(holder.containerView) {
                                    tvTitle.text = groupItem.groupName

                                    Glide.with(context)
                                        .load(URLs.URL_GROUP_IMAGE_PATH + groupItem.image)
                                        .apply(RequestOptions.errorOf(R.drawable.ic_launcher))
                                        .into(ivGroupImage)
                                    rlGroup.setOnClickListener {
                                        Intent(context, GroupActivity::class.java).run {
                                            putExtra("group_id", groupItem.id)
                                            putExtra("author_id", groupItem.authorId)
                                            putExtra("group_name", groupItem.groupName)
                                            startActivityForResult(this, UPDATE_CODE)
                                        }
                                    }
                                }
                            }
                        }
                        is AdHolder -> {
                            with(holder.containerView) {
                                tvTitle.text = mItemList[position] as String
                            }
                        }
                    }
                }

                override fun getItemViewType(position: Int): Int = when {
                    mItemList[position] is String && mItemList[position] != "광고" -> TYPE_TEXT
                    mItemList[position] is GroupItem -> TYPE_GROUP
                    mItemList[position] is String && mItemList[position] == "광고" -> TYPE_AD
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
                            left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (parent.getChildAdapterPosition(view) % mSpanCount == 1) 14f else 7f, resources.displayMetrics).toInt()
                            right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, if (parent.getChildAdapterPosition(view) % mSpanCount == 0) 14f else 7f, resources.displayMetrics).toInt()
                        }
                    }
                }
            })
        }
        srlGroup.setOnRefreshListener {
            Handler().postDelayed({
                srlGroup.isRefreshing = false

                mItemList.clear()
                fetchDataTask()
            }, 1000)
        }
        setDrawerToggle()
        fetchDataTask()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == CREATE_CODE || requestCode == REGISTER_CODE || requestCode == UPDATE_CODE) && resultCode == Activity.RESULT_OK) {
            mItemList.clear()
            fetchDataTask()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mSpanCount = when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT_SPAN_COUNT
            Configuration.ORIENTATION_LANDSCAPE -> LANDSCAPE_SPAN_COUNT
            else -> 0
        }
        (rvGroup.layoutManager as GridLayoutManager).spanCount = mSpanCount

        rvGroup.invalidateItemDecorations()
    }

    private fun setDrawerToggle() {
        ActionBarDrawerToggle(mActivity, mActivity.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close).let {
            mActivity.drawerLayout.addDrawerListener(it)
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

                        mItemList.add(groupItem)
                    }
                }
                rvGroup.adapter!!.notifyDataSetChanged()
                setOtherItems()
            }

        }, Response.ErrorListener { error ->
            VolleyLog.e(TAG, error.message)
        }) {
            override fun getHeaders() = mapOf("Authorization" to mApiKey)
        }

        AppController.getInstance().addToRequestQueue(jsonObjectRequest)
    }

    fun setOtherItems() {
        if (mItemList.isNotEmpty()) {
            mItemList.add(0, getString(R.string.joined_group))
            rvGroup.adapter!!.notifyItemChanged(0)
            if (mItemList.size % 2 == 0) {
                mItemList.add("광고")
                rvGroup.adapter!!.notifyItemInserted(mItemList.size - 1)
            }
        }
    }

    inner class HeaderHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    inner class ItemHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

    inner class AdHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer

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