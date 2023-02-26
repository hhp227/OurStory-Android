package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.hhp227.application.BuildConfig
import com.hhp227.application.databinding.FragmentVerinfoBinding
import com.hhp227.application.util.autoCleared

class VerInfoFragment : Fragment() {
    private var binding: FragmentVerinfoBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVerinfoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.appVersion = BuildConfig.VERSION_NAME
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setupWithNavController(findNavController())
    }
}