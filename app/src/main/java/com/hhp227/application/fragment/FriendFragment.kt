package com.hhp227.application.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hhp227.application.R
import com.hhp227.application.adapter.FriendListAdapter
import com.hhp227.application.databinding.FragmentFriendBinding
import com.hhp227.application.databinding.MenuSearchBinding
import com.hhp227.application.util.InjectorUtils
import com.hhp227.application.util.autoCleared
import com.hhp227.application.viewmodel.FriendViewModel

class FriendFragment : Fragment(), MenuProvider {
    private val viewModel: FriendViewModel by viewModels {
        InjectorUtils.provideFriendViewModelFactory()
    }

    private var binding: FragmentFriendBinding by autoCleared()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerView.adapter = FriendListAdapter().apply {
            setOnItemClickListener { user ->

            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireParentFragment().parentFragment as? MainFragment)?.setNavAppbar(binding.toolbar)
        (requireActivity() as AppCompatActivity).addMenuProvider(this)
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when {
                state.message.isNotBlank() -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).removeMenuProvider(this)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.search, menu)
        if (menu.hasVisibleItems()) {
            menu.findItem(R.id.search).actionView = MenuSearchBinding.inflate(layoutInflater).run {
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Toast.makeText(requireContext(), "query: $query", Toast.LENGTH_LONG).show()
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
                return@run root
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem) = false
}