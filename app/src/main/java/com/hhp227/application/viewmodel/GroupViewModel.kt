package com.hhp227.application.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.hhp227.application.R
import com.hhp227.application.data.GroupRepository
import com.hhp227.application.helper.PreferenceManager
import com.hhp227.application.model.GroupItem
import com.hhp227.application.model.GroupType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GroupViewModel internal constructor(
    private val repository: GroupRepository,
    preferenceManager: PreferenceManager
) : ViewModel() {
    private lateinit var apiKey: String

    val state = MutableLiveData(State())

    override fun onCleared() {
        super.onCleared()
        Log.e("TEST", "GroupViewModel onCleared")
    }

    private fun setPagingData(pagingData: PagingData<GroupItem>?) {
        state.value = state.value?.copy(pagingData = pagingData)
    }

    fun onItemEmpty() {
        val pagingData = PagingData.empty<GroupItem>().insertHeaderItem(item = GroupItem.Empty(0, 0))//PagingData.from<GroupItem>(listOf(GroupItem.Empty(0, 0)))
        Log.e("TEST", "onChangeLoadState")

        setPagingData(pagingData)
    }

    init {
        viewModelScope.launch {
            preferenceManager.userFlow
                .flatMapConcat {
                    apiKey = it?.apiKey ?: ""
                    return@flatMapConcat repository.getGroupList(apiKey, GroupType.Joined)
                }
                //.map { it.insertHeaderItem(item = GroupItem.Title(R.string.joined_group)) }
                .cachedIn(viewModelScope)
                .collectLatest(::setPagingData)
        }
    }

    data class State(
        val isLoading: Boolean = false,
        val pagingData: PagingData<GroupItem>? = PagingData.empty()
    )
}

class GroupViewModelFactory(
    private val repository: GroupRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            return GroupViewModel(repository, preferenceManager) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }
}
