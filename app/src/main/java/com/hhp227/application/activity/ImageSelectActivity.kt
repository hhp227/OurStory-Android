package com.hhp227.application.activity

import android.content.ContentUris
import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hhp227.application.R
import com.hhp227.application.adapter.ImageSelectAdapter
import com.hhp227.application.databinding.ActivityImageSelectBinding
import com.hhp227.application.dto.GalleryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

class ImageSelectActivity : AppCompatActivity() {
    private val imageList = mutableListOf<GalleryItem>()

    private val itemDecoration by lazy(::ImageDecoration)

    private lateinit var binding: ActivityImageSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageSelectBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Log.e("TEST", "type: ${intent.getIntExtra(SELECT_TYPE, -1)}")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, SPAN_COUNT)
            adapter = ImageSelectAdapter().apply {
                submitList(imageList)
                setOnItemClickListener { _, p ->
                    if (intent.getIntExtra(SELECT_TYPE, -1) == SINGLE_SELECT_TYPE) {
                        setResult(RESULT_OK, Intent().setData(imageList[p].uri))
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
        CoroutineScope(Dispatchers.Default).launch {
            fetchImageList()
        }
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
                setResult(RESULT_OK, Intent().putExtra("data", imageList.asSequence().filter(GalleryItem::isSelected).map(GalleryItem::uri).toList().toTypedArray()))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private suspend fun fetchImageList() {
        coroutineScope {
            contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                null,
                null,
                null
            )?.use { imageCursor ->
                while (imageCursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        imageCursor.getLong(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID))
                    )

                    runOnUiThread {
                        imageList.add(GalleryItem(uri, false))
                        binding.recyclerView.adapter?.notifyItemChanged(imageList.size - 1)
                    }
                }
            }
        }
    }

    inner class ImageDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.apply {
                top = resources.getDimension(R.dimen.image_item).toInt()
                bottom = resources.getDimension(R.dimen.image_item).toInt()
                left = resources.getDimension(R.dimen.image_item).toInt()
                right = resources.getDimension(R.dimen.image_item).toInt()
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