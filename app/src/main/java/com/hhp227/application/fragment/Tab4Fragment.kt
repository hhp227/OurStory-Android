package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.util.autoCleared

class Tab4Fragment : Fragment() {
    private var groupId = 0

    private var authorId = 0

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getInt(ARG_PARAM1)
            authorId = it.getInt(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "author_id"
        private val TAG = Tab4Fragment::class.java.simpleName

        fun newInstance(groupId: Int, authorId: Int) = Tab4Fragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PARAM1, groupId)
                putInt(ARG_PARAM2, authorId)
            }
        }
    }
}