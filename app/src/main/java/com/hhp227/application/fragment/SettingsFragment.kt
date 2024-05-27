package com.hhp227.application.fragment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.gms.ads.AdRequest
import com.hhp227.application.R
import com.hhp227.application.databinding.FragmentTabBinding
import com.hhp227.application.databinding.ItemSettingsBinding
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.User
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.URLs
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// WIP
class SettingsFragment : Fragment(), View.OnClickListener {
    private val viewModel: SettingsViewModel by viewModels {
        InjectorUtils.provideSettingsViewModelFactory(this)
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

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.isLoading -> {
                    // TODO
                }
                state.isSuccess -> if (findNavController().currentDestination?.id == R.id.groupDetailFragment) {
                    requireParentFragment().setFragmentResult(findNavController().previousBackStackEntry?.destination?.displayName ?: "", bundleOf("group" to viewModel.group))
                    requireParentFragment().findNavController().navigateUp()
                }
                state.message.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile -> findNavController().navigate(R.id.profileFragment)
            R.id.ll_withdrawal -> AlertDialog.Builder(requireContext())
                .setMessage(getString(if (viewModel.isAuth) R.string.question_delete_group else R.string.question_leave_group))
                .setPositiveButton(getString(android.R.string.ok)) { _, _ -> viewModel.deleteGroup() }
                .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .show()
            R.id.notice -> findNavController().navigate(R.id.noticeFragment)
            R.id.appstore -> Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")).also(::startActivity)
            R.id.feedback -> findNavController().navigate(R.id.feedbackFragment)
            R.id.verinfo -> findNavController().navigate(R.id.verInfoFragment)
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
            R.id.privacy_policy -> startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://hong227.dothome.co.kr/privacyInformationCollection.html")))
        }
    }

    inner class ViewHolder(private val binding: ItemSettingsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User?) = with(binding) {
            pname.text = user?.name
            pemail.text = user?.email
            tvWithdrawal.text = getString(if (viewModel.isAuth) R.string.delete_group else R.string.leave_group)

            ivProfileImage.load(URLs.URL_USER_PROFILE_IMAGE + user?.profileImage) {
                placeholder(R.drawable.profile_img_circle)
                error(R.drawable.profile_img_circle)
                transformations(CircleCropTransformation())
            }
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
                privacyPolicy.setOnClickListener(::onClick)
            }
        }
    }

    companion object {
        private const val ARG_PARAM = "group"

        fun newInstance(group: GroupItem.Group) = SettingsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM, group)
            }
        }
    }
}