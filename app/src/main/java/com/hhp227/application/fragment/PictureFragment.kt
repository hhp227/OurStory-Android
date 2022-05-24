package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.hhp227.application.adapter.PicturePagerAdapter
import com.hhp227.application.databinding.FragmentPictureBinding
import com.hhp227.application.dto.ListItem
import com.hhp227.application.util.autoCleared

class PictureFragment : Fragment() {
    private var binding: FragmentPictureBinding by autoCleared()

    private val onPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            binding.tvCount.text = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = arguments?.getInt("position", 0) ?: 0
        val list = arguments?.getParcelableArray("images")?.toList() as List<ListItem.Image>

        binding.toolbar.setupWithNavController(findNavController())
        binding.viewPager.apply {
            adapter = PicturePagerAdapter().apply {
                submitList(list)
            }

            registerOnPageChangeCallback(onPageChangeListener)
            setCurrentItem(position, false)
        }
        binding.tvCount.apply {
            visibility = if (binding.viewPager.adapter?.itemCount ?: 0 > 1) View.VISIBLE else View.GONE
            text = "${position + 1}/${binding.viewPager.adapter?.itemCount}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPager.unregisterOnPageChangeCallback(onPageChangeListener)
    }
}