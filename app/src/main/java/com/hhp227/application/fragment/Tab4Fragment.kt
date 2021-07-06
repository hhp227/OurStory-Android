package com.hhp227.application.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hhp227.application.R
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.databinding.FragmentTab4Binding
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.dto.User
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
        binding.swipeRefreshLayout.isRefreshing = false

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = object : RecyclerView.Adapter<ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                    return ViewHolder(FragmentTab4Binding.inflate(LayoutInflater.from(parent.context), parent, false))
                }

                override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                    holder.bind(AppController.getInstance().preferenceManager.user)
                }

                override fun getItemCount(): Int = 1
            }
        }
    }

    inner class ViewHolder(private val binding: FragmentTab4Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            pname.text = user.name
            pemail.text = user.email

            Glide.with(binding.root)
                .load(URLs.URL_USER_PROFILE_IMAGE + user.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
        }
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