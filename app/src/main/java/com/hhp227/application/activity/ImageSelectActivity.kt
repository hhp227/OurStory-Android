package com.hhp227.application.activity

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.ImageSelectAdapter
import com.hhp227.application.data.ImageRepository
import com.hhp227.application.databinding.ActivityImageSelectBinding
import com.hhp227.application.dto.GalleryItem
import com.hhp227.application.viewmodel.ImageSelectViewModel
import com.hhp227.application.viewmodel.ImageSelectViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ImageSelectActivity : AppCompatActivity() {
    private val viewModel: ImageSelectViewModel by viewModels {
        ImageSelectViewModelFactory(ImageRepository.getInstance())
    }

    private val itemDecoration by lazy(::ImageDecoration)

    private lateinit var binding: ActivityImageSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSelectBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, SPAN_COUNT)
            adapter = ImageSelectAdapter().apply {
                setOnItemClickListener { _, p ->
                    if (intent.getIntExtra(SELECT_TYPE, -1) == SINGLE_SELECT_TYPE) {
                        setResult(RESULT_OK, Intent().setData(currentList[p].uri))
                        finish()
                    } else {
                        currentPosition = p
                        currentList[p].isSelected = !currentList[p].isSelected
                    }
                }
            }
            itemAnimator?.changeDuration = 0

            addItemDecoration(itemDecoration)
        }
        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.imageList.isNotEmpty() -> {
                    (binding.recyclerView.adapter as ImageSelectAdapter).submitList(state.imageList)
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.recyclerView.removeItemDecoration(itemDecoration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (intent.getIntExtra(SELECT_TYPE, -1) == MULTI_SELECT_TYPE) {
            menuInflater.inflate(R.menu.attach, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.attach -> {
                setResult(
                    RESULT_OK,
                    Intent().putExtra(
                        "data",
                        (binding.recyclerView.adapter as ImageSelectAdapter).currentList
                            .filter(GalleryItem::isSelected)
                            .map(GalleryItem::uri)
                            .toTypedArray()
                    )
                )
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    inner class ImageDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.apply {
                top = resources.getDimensionPixelOffset(R.dimen.image_item)
                bottom = resources.getDimensionPixelOffset(R.dimen.image_item)
                left = resources.getDimensionPixelOffset(R.dimen.image_item)
                right = resources.getDimensionPixelOffset(R.dimen.image_item)
            }
        }
    }

    companion object {
        const val SELECT_TYPE = "type"
        const val SINGLE_SELECT_TYPE = 0
        const val MULTI_SELECT_TYPE = 1

        private const val SPAN_COUNT = 3
    }
}