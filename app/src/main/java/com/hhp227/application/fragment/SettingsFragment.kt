package com.hhp227.application.fragment

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.hhp227.application.R
import com.hhp227.application.activity.*
import com.hhp227.application.app.AppController
import com.hhp227.application.app.URLs
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.databinding.ItemSettingsBinding
import com.hhp227.application.dto.UserItem
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.SettingsViewModel
import com.hhp227.application.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(), View.OnClickListener {
    private val viewModel: SettingsViewModel by viewModels {
        InjectorUtils.provideSettingsViewModelFactory(this)
    }

    private val myInfoActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // TODO
    }

    private var binding: FragmentTabBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefreshLayout.isRefreshing = false
        binding.recyclerView.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return ViewHolder(ItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                viewModel.userFlow.onEach(holder::bind).launchIn(lifecycleScope)
            }

            override fun getItemCount(): Int = 1
        }

        viewModel.state.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).onEach { state ->
            when {
                state.isLoading -> {
                    // TODO
                }
                state.isSuccess -> {
                    requireActivity().setResult(RESULT_OK, Intent(context, GroupFragment::class.java))
                    requireActivity().finish()
                }
                state.error.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile -> Intent(requireContext(), MyInfoActivity::class.java).also(myInfoActivityResultLauncher::launch)
            R.id.ll_withdrawal -> AlertDialog.Builder(requireContext())
                .setMessage(getString(if (viewModel.isAuth) R.string.question_delete_group else R.string.question_leave_group))
                .setPositiveButton(getString(android.R.string.ok)) { _, _ -> viewModel.deleteGroup() }
                .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .show()
            R.id.notice -> Intent(requireContext(), NoticeActivity::class.java).also(::startActivity)
            R.id.appstore -> Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")).also(::startActivity)
            R.id.feedback -> Intent(requireContext(), FeedbackActivity::class.java).also(::startActivity)
            R.id.verinfo -> Intent(requireContext(), VerInfoActivity::class.java).also(::startActivity)
            R.id.share -> startActivity(Intent.createChooser(Intent().let { intent ->
                intent.setType("text/plain").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                intent.putExtra(Intent.EXTRA_TEXT,
                    """
     확인하세요
     GitHub Page :  https://localhost/Sample App : https://play.google.com/store/apps/details?id=
     """.trimIndent()
                )
            }, getString(R.string.app_name)))
        }
    }

    inner class ViewHolder(private val binding: ItemSettingsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserItem?) = with(binding) {
            pname.text = user?.name
            pemail.text = user?.email
            tvWithdrawal.text = getString(if (viewModel.isAuth) R.string.delete_group else R.string.leave_group)

            Glide.with(binding.root)
                .load(URLs.URL_USER_PROFILE_IMAGE + user?.profileImage)
                .apply(RequestOptions.errorOf(R.drawable.profile_img_circle).circleCrop())
                .into(ivProfileImage)
            adView.loadAd(AdRequest.Builder().build())
        }

        init {
            with(binding) {
                profile.setOnClickListener(::onClick)
                llWithdrawal.setOnClickListener(::onClick)
                notice.setOnClickListener(::onClick)
                appstore.setOnClickListener(::onClick)
                feedback.setOnClickListener(::onClick)
                verinfo.setOnClickListener(::onClick)
                share.setOnClickListener(::onClick)
            }
        }
    }

    companion object {
        private const val ARG_PARAM1 = "group_id"
        private const val ARG_PARAM2 = "author_id"

        fun newInstance(groupId: Int, authorId: Int) = SettingsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PARAM1, groupId)
                putInt(ARG_PARAM2, authorId)
            }
        }
    }
}