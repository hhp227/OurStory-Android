package com.hhp227.application.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.ImageSelectAdapter
import com.hhp227.application.adapter.ItemLoadStateAdapter
import com.hhp227.application.databinding.ActivityImageSelectBinding
import com.hhp227.application.dto.GalleryItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.viewmodel.ImageSelectViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ImageSelectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageSelectBinding

    private val viewModel: ImageSelectViewModel by viewModels {
        InjectorUtils.provideImageSelectViewModelFactory()
    }

    private val itemDecoration by lazy(::ImageDecoration)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSelectBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.apply {
            adapter = ImageSelectAdapter().run {
                setOnItemClickListener { _, p ->
                    if (intent.getIntExtra(SELECT_TYPE, -1) == SINGLE_SELECT_TYPE) {
                        setResult(RESULT_OK, Intent().setData(snapshot()[p]?.uri))
                        finish()
                    } else {
                        currentPosition = p
                        snapshot()[p]?.isSelected = !(snapshot()[p]?.isSelected ?: false)
                    }
                }
                withLoadStateFooter(ItemLoadStateAdapter(::retry))
            }
            (layoutManager as? GridLayoutManager)?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter?.getItemViewType(position) == 0) 1 else SPAN_COUNT
                }
            }
            itemAnimator?.changeDuration = 0

            addItemDecoration(itemDecoration)
        }
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                ((binding.recyclerView.adapter as? ConcatAdapter)?.adapters?.first() as? ImageSelectAdapter)?.submitData(state.data)
            }
            .launchIn(lifecycleScope)
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
                        ((binding.recyclerView.adapter as? ConcatAdapter)?.adapters?.first() as? ImageSelectAdapter)?.snapshot()
                            ?.mapNotNull { it }
                            ?.filter(GalleryItem::isSelected)
                            ?.map(GalleryItem::uri)
                            ?.toTypedArray()
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