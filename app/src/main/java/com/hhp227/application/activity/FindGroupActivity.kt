package com.hhp227.application.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.adapter.GroupListAdapter
import com.hhp227.application.databinding.ActivityGroupFindBinding
import com.hhp227.application.dto.GroupItem
import com.hhp227.application.fragment.GroupInfoFragment
import com.hhp227.application.fragment.GroupInfoFragment.Companion.TYPE_REQUEST
import com.hhp227.application.viewmodel.FindGroupViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FindGroupActivity : AppCompatActivity() {
    private val viewModel: FindGroupViewModel by viewModels()

    private lateinit var binding: ActivityGroupFindBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupFindBinding.inflate(layoutInflater)
        binding.recyclerView.adapter = GroupListAdapter().apply {
            setOnItemClickListener { _, position ->
                if (position != RecyclerView.NO_POSITION) {
                    val groupItem = currentList[position] as GroupItem.Group

                    GroupInfoFragment.newInstance().run {
                        arguments = Bundle().apply {
                            putInt("request_type", TYPE_REQUEST)
                            putParcelable("group", groupItem)
                        }
                        return@run show(supportFragmentManager, "dialog")
                    }
                }
            }
        }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            }, 1000)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> showProgressBar()
                state.groupList.isNotEmpty() -> {
                    hideProgressBar()
                    (binding.recyclerView.adapter as GroupListAdapter).submitList(state.groupList)
                }
                state.error.isNotBlank() -> {
                    hideProgressBar()
                    Toast.makeText(this, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showProgressBar() {
        if (binding.progressBar.visibility == View.GONE)
            binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        if (binding.progressBar.visibility == View.VISIBLE)
            binding.progressBar.visibility = View.GONE
    }
}
